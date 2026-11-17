/* FILE PURPOSE: IMPORT TOOL | Pack four complete shield-holding character poses with a shared foot anchor.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Packs complete guard artwork without erasing, recoloring or rebuilding body pixels. */
public final class GuardSpriteImporter {
    private static final int SIZE=64,BASE=63;
    private record Packed(BufferedImage image,double footX,int footY){}
    // [BLOCK: ART TOOL] GuardSpriteImporter.GuardSpriteImporter - Prevent instances of this static helper class.
    private GuardSpriteImporter(){}
    // [BLOCK: ART TOOL] GuardSpriteImporter.main - Entry point for this developer tool.
    public static void main(String[] args)throws IOException{
        Path input=Path.of(args.length>0?args[0]:"art/source/player/agyu-guard-sheet.png");
        Path rearInput=Path.of(args.length>1?args[1]:"art/source/player/agyu-guard-rear-v2.png");
        Path sideInput=Path.of(args.length>2?args[2]:"art/source/player/agyu-guard-forward-v3.png");
        BufferedImage source=read(input),rear=read(rearInput),sides=read(sideInput);
        int cellW=source.getWidth()/2,cellH=source.getHeight()/2;
        int[][] cells={{0,0},{1,0},{1,1},{0,1}}; // DOWN, UP, LEFT, RIGHT
        String[] names={"down","up","left","right"};
        int[][] bounds=new int[4][];
        int maxHeight=0;
        for(int dir=0;dir<4;dir++){
            bounds[dir]=bounds(source,cells[dir][0]*cellW,cells[dir][1]*cellH,cellW,cellH);
            maxHeight=Math.max(maxHeight,bounds[dir][3]-bounds[dir][1]+1);
        }
        double commonScale=maxHeight/51.0;
        int[] rearBounds=bounds(rear,0,0,rear.getWidth(),rear.getHeight());
        Path output=Path.of("res/player");Files.createDirectories(output);Files.createDirectories(Path.of("build"));
        StringBuilder manifest=new StringBuilder("direction,source_file,source_left,source_top,source_right,source_bottom,feet_x,feet_y,source_pixels_per_art_pixel\n");
        BufferedImage preview=new BufferedImage(960,300,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=preview.createGraphics();
        try{
            g.setColor(new Color(0x263A35));g.fillRect(0,0,960,300);
            g.setFont(new Font("Monospaced",Font.PLAIN,16));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            for(int dir=0;dir<4;dir++){
                boolean replaceRear=dir==Player.Dir.UP.ordinal();
                boolean replaceSide=dir==Player.Dir.LEFT.ordinal()||dir==Player.Dir.RIGHT.ordinal();
                BufferedImage art=replaceRear?rear:replaceSide?sides:source;
                int width=replaceRear?rear.getWidth():replaceSide?sides.getWidth()/2:cellW;
                int height=replaceRear?rear.getHeight():replaceSide?sides.getHeight()/2:cellH;
                int originX=replaceRear?0:cells[dir][0]*width,originY=replaceRear?0:cells[dir][1]*height;
                int[] b=replaceRear?rearBounds:replaceSide?bounds(sides,originX,originY,width,height):bounds[dir];
                // The replacement rear pose has its own resolution, but the same 51px art height.
                double scale=replaceRear||replaceSide?(b[3]-b[1]+1)/51.0:commonScale;
                Packed packed=pack(art,b,originX,originY,width,height,scale,dir==0,replaceRear||replaceSide);
                if(!ImageIO.write(packed.image(),"png",output.resolve("guard_"+names[dir]+".png").toFile()))
                    throw new IOException("PNG writer unavailable");
                manifest.append(names[dir]).append(',').append((replaceRear?rearInput:replaceSide?sideInput:input).getFileName()).append(',')
                        .append(b[0]).append(',').append(b[1]).append(',').append(b[2]).append(',').append(b[3]).append(',')
                        .append(packed.footX()).append(',').append(packed.footY()).append(',').append(scale).append('\n');
                g.setColor(new Color(0xF1C979));g.drawString(names[dir].toUpperCase(java.util.Locale.ROOT),dir*240+75,25);
                g.drawImage(packed.image(),dir*240+24,40,192,192,null);
                g.setColor(new Color(0x73846B));g.drawLine(dir*240+24,232,dir*240+215,232);
            }
        }finally{g.dispose();}
        Files.writeString(output.resolve("guard-frames.csv"),manifest,StandardCharsets.UTF_8);
        ImageIO.write(preview,"png",Path.of("build/preview-guard-assets.png").toFile());
        System.out.println("Packed forward-pointing side guards; original front and corrected rear remain unchanged.");
    }
    // [BLOCK: ART TOOL] GuardSpriteImporter.read - Load or validate read data.
    private static BufferedImage read(Path input)throws IOException{
        BufferedImage image=ImageIO.read(input.toFile());
        if(image==null||!image.getColorModel().hasAlpha())throw new IOException("Transparent guard artwork required: "+input);
        return image;
    }
    // [BLOCK: ART TOOL] GuardSpriteImporter.bounds - Handle bounds for GuardSpriteImporter.
    private static int[] bounds(BufferedImage source,int x0,int y0,int width,int height)throws IOException{
        int minX=x0+width,minY=y0+height,maxX=-1,maxY=-1;
        for(int y=y0;y<y0+height;y++)for(int x=x0;x<x0+width;x++)
            if((source.getRGB(x,y)>>>24)>48){
                minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);
            }
        if(maxX<minX)throw new IOException("Missing guard pose");
        return new int[]{minX,minY,maxX,maxY};
    }
    // [BLOCK: ART TOOL] GuardSpriteImporter.pack - Handle pack for GuardSpriteImporter.
    private static Packed pack(BufferedImage source,int[] b,int originX,int originY,int width,int height,
            double scale,boolean mirror,boolean alignOpaqueFeet)throws IOException{
        long totalX=0;int count=0;
        // Boots establish the anchor; weapon and shield bounds never recenter the body.
        int bootBand=Math.max(18,(int)Math.ceil(2*scale));
        for(int y=Math.max(b[1],b[3]-bootBand);y<=b[3];y++)for(int x=b[0];x<=b[2];x++){
            int p=source.getRGB(x,y),r=(p>>16)&255,green=(p>>8)&255,blue=p&255;
            if((p>>>24)>128&&r>20&&r>green&&green>blue){totalX+=x;count++;}
        }
        if(count==0)throw new IOException("Boot anchor missing");
        double footX=(double)totalX/count;int footY=b[3];
        if(alignOpaqueFeet){
            // A translucent export fringe below the soles must not become the baseline.
            while(footY>b[1]){
                int visible=0;
                for(int x=b[0];x<=b[2];x++)if((source.getRGB(x,footY)>>>24)>128)visible++;
                if(visible>=Math.ceil(3*scale))break;
                footY--;
            }
        }
        BufferedImage packed=new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){
            int sx=(int)Math.round(footX+(x-SIZE/2.0)*scale);
            int sy=(int)Math.round(footY+(y-BASE)*scale);
            if(sx>=originX&&sx<originX+width&&sy>=originY&&sy<originY+height)
                packed.setRGB(mirror?SIZE-1-x:x,y,source.getRGB(sx,sy));
        }
        return new Packed(packed,footX,footY);
    }
}
