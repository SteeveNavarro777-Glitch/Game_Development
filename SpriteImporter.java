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

/**
 * Deterministically cuts the supplied 941x1672 RGBA sheet.
 * Uses only JDK APIs. Original alpha/pixel colors are preserved; scale is 1:3.
 */
public final class SpriteImporter {
    private static final int CELL=64, SCALE=3, BASELINE=63;
    private static final int[][] Y={{0,181},{181,358},{358,541},{541,720},{720,898},
            {898,1080},{1080,1253},{1253,1421},{1421,1620}};
    private static final int[][] X={
        {0,127,232,336,443,551,671,790,923},
        {0,121,231,340,449,561,677,794,923},
        {0,115,231,343,455,566,681,799,923},
        {0,119,236,349,461,572,686,801,923},
        {0,119,234,349,463,574,686,801,923},
        {0,120,232,340,459,569,682,798,923},
        {0,121,236,350,457,576,690,805,923},
        {0,120,235,351,459,574,685,802,923},
        {0,118,232,346,460,574,692}
    };
    private static final String[] NAMES={"front","back","idle","walk","run","action","attack","reaction","back_reaction"};
    private static BufferedImage source;
    private SpriteImporter(){}
    private static boolean boot(int pixel){
        int a=pixel>>>24,r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
        return a>128&&r>12&&r<170&&g<140&&g<r&&b<r*0.9;
    }
    private static boolean blue(int pixel){
        int r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
        return (pixel>>>24)>0&&b>100&&g>95&&b>r*0.95;
    }
    private static int sample(int row,int frame,int x,int y){
        if(y<Y[row][0]||y>=Y[row][1]||x<0||x>=source.getWidth())return 0;
        int left=X[row][frame],right=X[row][frame+1];
        int pixel=source.getRGB(x,y);
        // The supplied sixth slash spills into the following frame.
        // Borrow its pale-blue/white pixels without borrowing the next warrior.
        if(row==6&&frame==5&&x>=right&&x<714)return blue(pixel)?pixel:0;
        if(x<left||x>=right)return 0;
        // Discard only the preceding slash fragment from frame seven.
        if(row==6&&frame==6&&x<714&&blue(pixel))return 0;
        return pixel;
    }
    private static int[] anchor(int row,int frame){
        int left=X[row][frame],right=X[row][frame+1],top=Y[row][0],bottom=Y[row][1];
        int coreRight=left+(right-left)*3/4,footY=-1;
        for(int y=top+(bottom-top)/2;y<bottom;y++)
            for(int x=left;x<coreRight;x++)if(boot(sample(row,frame,x,y)))footY=Math.max(footY,y);
        if(footY<0)throw new IllegalStateException("No feet found: "+row+"/"+frame);
        long sum=0;int count=0;
        for(int y=Math.max(top,footY-34);y<=footY;y++)
            for(int x=left;x<coreRight;x++)if(boot(sample(row,frame,x,y))){sum+=x;count++;}
        if(count==0)throw new IllegalStateException("No body anchor");
        return new int[]{(int)Math.round((double)sum/count),footY};
    }
    private static BufferedImage frame(int row,int frame,int[] anchor){
        BufferedImage result=new BufferedImage(CELL,CELL,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<CELL;y++)for(int x=0;x<CELL;x++)
            result.setRGB(x,y,sample(row,frame,anchor[0]+(x-CELL/2)*SCALE,anchor[1]+(y-BASELINE)*SCALE));
        return result;
    }
    private static BufferedImage death(){
        BufferedImage result=new BufferedImage(96,CELL,BufferedImage.TYPE_INT_ARGB);
        int left=694,right=891,top=1520,bottom=1604,cx=(left+right)/2;
        int footY=1599;
        for(int y=0;y<CELL;y++)for(int x=0;x<result.getWidth();x++){
            int sx=cx+(x-result.getWidth()/2)*SCALE,sy=footY+(y-BASELINE)*SCALE;
            if(sx>=left&&sx<right&&sy>=top&&sy<bottom)result.setRGB(x,y,source.getRGB(sx,sy));
        }
        return result;
    }
    public static void main(String[] args)throws IOException{
        Path input=Path.of(args.length>0?args[0]:"res/agyu-sheet.png");
        Path output=Path.of(args.length>1?args[1]:"res/agyu-v2");
        source=ImageIO.read(input.toFile());
        if(source==null||source.getWidth()!=941||source.getHeight()!=1672)
            throw new IOException("Expected the supplied 941x1672 PNG: "+input);
        Files.createDirectories(output);Files.createDirectories(Path.of("build"));
        BufferedImage preview=new BufferedImage(1200,1350,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=preview.createGraphics();
        g.setColor(new Color(0x263A35));g.fillRect(0,0,preview.getWidth(),preview.getHeight());
        g.setFont(new Font("Monospaced",Font.PLAIN,16));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        StringBuilder manifest=new StringBuilder("name,row,column,source_left,source_top,source_right,source_bottom,anchor_x,anchor_y,scale\n");
        int count=0;
        try{
            for(int row=0;row<Y.length;row++){
                g.setColor(new Color(0xF1C979));g.drawString(NAMES[row],8,row*145+22);
                for(int col=0;col<X[row].length-1;col++){
                    int[] anchor=anchor(row,col);BufferedImage image=frame(row,col,anchor);
                    String name=NAMES[row]+"_"+col;
                    if(!ImageIO.write(image,"png",output.resolve(name+".png").toFile()))throw new IOException("PNG writer unavailable");
                    manifest.append(name).append(',').append(row+1).append(',').append(col+1).append(',')
                            .append(X[row][col]).append(',').append(Y[row][0]).append(',').append(X[row][col+1]).append(',')
                            .append(Y[row][1]).append(',').append(anchor[0]).append(',').append(anchor[1]).append(",3\n");
                    int dx=150+col*130,dy=row*145+16;
                    g.setColor(new Color(0x73846B));g.drawLine(dx,dy+127,dx+127,dy+127);
                    g.drawImage(image,dx,dy,128,128,null);
                    count++;
                }
            }
            BufferedImage dead=death();
            ImageIO.write(dead,"png",output.resolve("die.png").toFile());
            g.drawImage(dead,150+6*130,8*145+16,192,128,null);count++;
        }finally{g.dispose();}
        Files.writeString(output.resolve("frames.csv"),manifest,StandardCharsets.UTF_8);
        ImageIO.write(preview,"png",Path.of("build/sprite-contact-sheet.png").toFile());
        System.out.println("Imported "+count+" frames from "+input+" into "+output);
    }
}
