/* FILE PURPOSE: IMPORT TOOL | Pack the 72 current body poses and record their spear-grip anchors.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/** Packs complete combat bodies with one scale, fixed row roots and explicit grip overrides. */
public final class CombatBodyImporter {
    private static final int SIZE=64,BASE=63;
    private static final String[] DIRS={"right","down","up"},PHASES={"WINDUP","DRIVE","FOLLOW","RECOVER"};
    private static final class Frame {
        String dir,file;int stage,phase,x0,y0,x1,y1;int[] bounds,hand;
        Path sourceFile;BufferedImage source,packed;double rootX,rootY,handSourceX,handSourceY;
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.CombatBodyImporter - Prevent instances of this static helper class.
    private CombatBodyImporter(){}
    // [BLOCK: ART TOOL] CombatBodyImporter.main - Entry point for this developer tool.
    public static void main(String[] args)throws IOException{
        Path input=args.length>0?Path.of(args[0]):Path.of("art/source/player");
        Path output=args.length>1?Path.of(args[1]):Path.of("res/player/combat");
        Path overridePath=args.length>2?Path.of(args[2]):output.resolve("grip-overrides.csv");
        Map<String,int[]> overrides=overrides(overridePath);
        List<Frame> frames=new ArrayList<>();int maxHeight=0;
        for(String dir:DIRS){
            Path file=input.resolve("agyu-combat-"+dir+".png");BufferedImage source=ImageIO.read(file.toFile());
            if(source==null||!source.getColorModel().hasAlpha())throw new IOException("RGBA source required: "+file);
            long clear=0;for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)
                if((source.getRGB(x,y)>>>24)<16)clear++;
            if(clear<(long)source.getWidth()*source.getHeight()*.15)throw new IOException("Opaque background: "+file);
            int sheetMin=Integer.MAX_VALUE,sheetMax=0;
            for(int row=0;row<6;row++){
                Frame[] rowFrames=new Frame[4];int sole=-1;
                for(int col=0;col<4;col++){
                    Frame f=new Frame();f.dir=dir;f.stage=row;f.phase=col;f.file=dir+"_"+row+"_"+col+".png";
                    f.source=source;f.sourceFile=file;
                    f.x0=part(col,source.getWidth(),4);f.x1=part(col+1,source.getWidth(),4);
                    f.y0=part(row,source.getHeight(),6);f.y1=part(row+1,source.getHeight(),6);
                    int minX=f.x1,minY=f.y1,maxX=-1,maxY=-1;
                    for(int y=f.y0;y<f.y1;y++)for(int x=f.x0;x<f.x1;x++)if((source.getRGB(x,y)>>>24)>128){
                        minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);
                    }
                    if(maxX<minX)throw new IOException("Empty frame: "+f.file);
                    f.bounds=new int[]{minX,minY,maxX,maxY};f.rootX=(f.x0+f.x1-1)/2.0;
                    sole=Math.max(sole,maxY);int h=maxY-minY+1;maxHeight=Math.max(maxHeight,h);
                    sheetMin=Math.min(sheetMin,h);sheetMax=Math.max(sheetMax,h);rowFrames[col]=f;
                }
                for(Frame f:rowFrames){f.rootY=sole;frames.add(f);}
            }
            System.out.println(dir+" body heights: "+sheetMin+".."+sheetMax+" source pixels");
        }
        double scale=maxHeight/51.0;Files.createDirectories(output);Files.createDirectories(Path.of("build"));
        StringBuilder csv=new StringBuilder("direction,stage,phase,hand_x,hand_y,source_file,source_left,source_top,source_right,source_bottom,root_x,root_y,source_hand_x,source_hand_y,source_pixels_per_art_pixel,file\n");
        List<String> warnings=new ArrayList<>();
        for(Frame f:frames){
            f.packed=new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){
                int sx=(int)Math.round(f.rootX+(x-32)*scale),sy=(int)Math.round(f.rootY+(y-BASE)*scale);
                if(sx>=f.x0&&sx<f.x1&&sy>=f.y0&&sy<f.y1)f.packed.setRGB(x,y,f.source.getRGB(sx,sy));
            }
            int opaque=0;boolean edge=false;
            for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)if((f.packed.getRGB(x,y)>>>24)>128){
                opaque++;if(x==0||x==63||y==0)edge=true;
            }
            if(opaque<200||edge)throw new IOException("Packed body too small or clipped: "+f.file+" pixels="+opaque);
            String key=f.dir+","+f.stage+","+f.phase;
            if(overrides.containsKey(key)){
                f.hand=overrides.get(key);
                f.handSourceX=f.rootX+f.hand[0]*scale;f.handSourceY=f.rootY+f.hand[1]*scale;
            }else try{f.hand=hand(f,scale);}catch(IOException problem){warnings.add(f.file+": "+problem.getMessage());}
            ImageIO.write(f.packed,"png",output.resolve(f.file).toFile());
            csv.append(f.dir).append(',').append(f.stage).append(',').append(f.phase).append(',');
            if(f.hand!=null)csv.append(f.hand[0]);csv.append(',');
            if(f.hand!=null)csv.append(f.hand[1]);csv.append(',');
            csv.append(f.sourceFile.getFileName()).append(',').append(f.bounds[0]).append(',').append(f.bounds[1]).append(',')
                .append(f.bounds[2]).append(',').append(f.bounds[3]).append(',').append(num(f.rootX)).append(',').append(num(f.rootY)).append(',');
            if(f.hand!=null)csv.append(num(f.handSourceX));csv.append(',');
            if(f.hand!=null)csv.append(num(f.handSourceY));csv.append(',');
            csv.append(num(scale)).append(',').append(f.file).append('\n');
        }
        Files.writeString(output.resolve("frames.csv"),csv,StandardCharsets.UTF_8);
        previews(frames);
        System.out.printf(Locale.ROOT,"Packed %d bodies; common scale %.8f.%n",frames.size(),scale);
        if(!warnings.isEmpty()){
            for(String warning:warnings)System.err.println("GRIP REVIEW: "+warning);
            throw new IOException(warnings.size()+" uncertain grip(s); PNGs/CSV/previews saved. Add explicit grip-overrides.csv and rerun.");
        }
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.part - Handle part for CombatBodyImporter.
    private static int part(int i,int extent,int count){return (int)Math.round((double)i*extent/count);}
    // [BLOCK: ART TOOL] CombatBodyImporter.num - Handle num for CombatBodyImporter.
    private static String num(double value){return String.format(Locale.ROOT,"%.8f",value);}
    // [BLOCK: ART TOOL] CombatBodyImporter.overrides - Handle overrides for CombatBodyImporter.
    private static Map<String,int[]> overrides(Path file)throws IOException{
        Map<String,int[]> result=new HashMap<>();if(!Files.exists(file))return result;
        for(String line:Files.readAllLines(file,StandardCharsets.UTF_8)){
            if(line.isBlank()||line.startsWith("#")||line.startsWith("direction,"))continue;
            String[] p=line.split(",",-1);
            if(p.length!=5)throw new IOException("Expected override direction,stage,phase,hand_x,hand_y: "+line);
            try{
                int stage=Integer.parseInt(p[1].trim()),phase=Integer.parseInt(p[2].trim());
                int x=Integer.parseInt(p[3].trim()),y=Integer.parseInt(p[4].trim());
                if(stage<0||stage>5||phase<0||phase>3||x< -31||x>31||y< -63||y>0)throw new NumberFormatException();
                result.put(p[0].trim()+","+stage+","+phase,new int[]{x,y});
            }catch(NumberFormatException error){throw new IOException("Invalid grip override: "+line,error);}
        }
        return result;
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.skin - Handle skin for CombatBodyImporter.
    private static boolean skin(int p){
        int r=(p>>16)&255,g=(p>>8)&255,b=p&255;
        return (p>>>24)>128&&r>=140&&g>=65&&b<=185&&r>g*1.10&&g>b*1.1&&b>=g*.42&&r<g*2.35;
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.hand - Handle hand for CombatBodyImporter.
    private static int[] hand(Frame f,double scale)throws IOException{
        int[] b=f.bounds;int w=b[2]-b[0]+1,top=(int)Math.round(b[1]+(b[3]-b[1]+1)*.46);
        int bottom=(int)Math.round(b[1]+(b[3]-b[1]+1)*.79),h=bottom-top+1;
        boolean[] seen=new boolean[w*h];int[] queue=new int[w*h];
        double bestX=Double.NEGATIVE_INFINITY,bestY=0;int bestRight=-1;
        for(int y=top;y<=bottom;y++)for(int x=b[0];x<=b[2];x++){
            int first=(y-top)*w+x-b[0];
            if(seen[first]||!skin(f.source.getRGB(x,y)))continue;
            int head=0,tail=0,right=x;queue[tail++]=first;seen[first]=true;
            while(head<tail){
                int p=queue[head++],px=b[0]+p%w,py=top+p/w;right=Math.max(right,px);
                for(int d=0;d<4;d++){
                    int nx=px+(d==0?-1:d==1?1:0),ny=py+(d==2?-1:d==3?1:0);
                    if(nx<b[0]||nx>b[2]||ny<top||ny>bottom)continue;
                    int next=(ny-top)*w+nx-b[0];
                    if(!seen[next]&&skin(f.source.getRGB(nx,ny))){seen[next]=true;queue[tail++]=next;}
                }
            }
            if(tail<Math.max(3,scale*scale*.65)||right<f.rootX+scale||right<=bestRight)continue;
            long totalX=0,totalY=0;int count=0,band=Math.max(1,(int)Math.round(3*scale));
            for(int i=0;i<tail;i++){int px=b[0]+queue[i]%w,py=top+queue[i]/w;
                if(px>=right-band+1){totalX+=px;totalY+=py;count++;}}
            if(count>0){bestX=(double)totalX/count;bestY=(double)totalY/count;bestRight=right;}
        }
        if(!Double.isFinite(bestX))throw new IOException("No connected screen-right fist candidate");
        int hx=(int)Math.round((bestX-f.rootX)/scale),hy=(int)Math.round((bestY-f.rootY)/scale);
        if(hx<1||hx>29||hy> -10||hy< -41)throw new IOException("Implausible torso grip "+hx+","+hy);
        f.handSourceX=bestX;f.handSourceY=bestY;return new int[]{hx,hy};
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.mirror - Handle mirror for CombatBodyImporter.
    private static BufferedImage mirror(BufferedImage image){
        BufferedImage out=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<64;y++)for(int x=0;x<64;x++)out.setRGB(63-x,y,image.getRGB(x,y));return out;
    }
    // [BLOCK: ART TOOL] CombatBodyImporter.previews - Handle previews for CombatBodyImporter.
    private static void previews(List<Frame> frames)throws IOException{
        String[] dirs={"right","left","down","up"};int cw=192,ch=218,width=cw*4,height=ch*6;
        BufferedImage all=new BufferedImage(width*4,height,BufferedImage.TYPE_INT_RGB);Graphics2D combined=all.createGraphics();
        try{for(int view=0;view<4;view++){
            String dir=dirs[view],from="left".equals(dir)?"right":dir;
            BufferedImage sheet=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=sheet.createGraphics();
            try{
                g.setColor(new Color(0x213632));g.fillRect(0,0,width,height);g.setFont(new Font("Monospaced",Font.PLAIN,12));
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                for(Frame f:frames)if(f.dir.equals(from)){
                    int x=f.phase*cw,y=f.stage*ch;g.setColor(new Color(0xF1C979));
                    g.drawString(dir.toUpperCase(Locale.ROOT)+" / "+(f.stage+1)+" / "+PHASES[f.phase],x+4,y+14);
                    g.drawImage("left".equals(dir)?mirror(f.packed):f.packed,x,y+20,192,192,null);
                    g.setColor(new Color(0x61756B));g.drawLine(x,y+211,x+191,y+211);
                    if(f.hand!=null){
                        int hx="left".equals(dir)?-f.hand[0]-1:f.hand[0];
                        int px=x+(32+hx)*3+1,py=y+20+(63+f.hand[1])*3+1;
                        g.setColor(Color.WHITE);g.drawOval(px-4,py-4,8,8);g.setColor(new Color(0xF74994));
                        g.drawLine(px-5,py,px+5,py);g.drawLine(px,py-5,px,py+5);
                    }else{g.setColor(new Color(0xFF9C8B));g.drawString("GRIP REVIEW",x+4,y+ch-4);}
                }
            }finally{g.dispose();}
            ImageIO.write(sheet,"png",Path.of("build/preview-combat-bodies-"+dir+".png").toFile());combined.drawImage(sheet,view*width,0,null);
        }}finally{combined.dispose();}
        ImageIO.write(all,"png",Path.of("build/preview-combat-bodies.png").toFile());
    }
}
