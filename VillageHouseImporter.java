import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Packs four disconnected buildings at one uniform scale, preserving source alpha. */
public final class VillageHouseImporter {
    private record Part(int left,int top,int right,int bottom,int count){}
    private VillageHouseImporter(){}
    public static void main(String[] args) throws Exception {
        File source=new File(args.length>0?args[0]:"res/village-houses-sheet.png");
        File folder=new File(args.length>1?args[1]:"res/village");
        BufferedImage sheet=ImageIO.read(source);
        if(sheet==null||!sheet.getColorModel().hasAlpha())throw new IllegalArgumentException("RGBA atlas required");
        int w=sheet.getWidth(),h=sheet.getHeight();
        boolean[] seen=new boolean[w*h];int[] queue=new int[w*h];
        List<Part> parts=new ArrayList<>();
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int start=y*w+x;
            if(seen[start]||(sheet.getRGB(x,y)>>>24)<32)continue;
            int first=0,last=0,l=x,t=y,r=x,b=y;queue[last++]=start;seen[start]=true;
            while(first<last){
                int index=queue[first++],px=index%w,py=index/w;
                l=Math.min(l,px);r=Math.max(r,px);t=Math.min(t,py);b=Math.max(b,py);
                for(int dir=0;dir<4;dir++){
                    int nx=px+(dir==0?-1:dir==1?1:0),ny=py+(dir==2?-1:dir==3?1:0);
                    if(nx<0||ny<0||nx>=w||ny>=h)continue;
                    int next=ny*w+nx;
                    if(!seen[next]&&(sheet.getRGB(nx,ny)>>>24)>=32){seen[next]=true;queue[last++]=next;}
                }
            }
            if(last>5000)parts.add(new Part(l,t,r,b,last));
        }
        parts.sort(Comparator.comparingInt(Part::count).reversed());
        if(parts.size()<4)throw new IllegalStateException("Four separate complete buildings required");
        Part[] ordered=new Part[4];
        for(Part part:parts.subList(0,4)){
            int i=((part.top+part.bottom)/2<h/2?0:2)+((part.left+part.right)/2<w/2?0:1);
            if(ordered[i]!=null)throw new IllegalStateException("Ambiguous building order");
            ordered[i]=part;
        }
        int maxW=0,maxH=0;
        for(Part p:ordered){
            if(p==null)throw new IllegalStateException("Missing atlas quadrant");
            maxW=Math.max(maxW,p.right-p.left+1);maxH=Math.max(maxH,p.bottom-p.top+1);
        }
        double scale=Math.min(190.0/maxW,174.0/maxH);
        Files.createDirectories(folder.toPath());
        StringBuilder csv=new StringBuilder("style,source_left,source_top,source_right,source_bottom,scale,file\n");
        for(int i=0;i<4;i++){
            Part p=ordered[i];
            // Include a small edge fringe, while using the opaque connected body to ignore alpha-zero RGB.
            int l=Math.max(0,p.left-2),t=Math.max(0,p.top-2),r=Math.min(w-1,p.right+2),b=Math.min(h-1,p.bottom+2);
            BufferedImage image=new BufferedImage(224,192,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int dw=(int)Math.round((r-l+1)*scale),dh=(int)Math.round((b-t+1)*scale);
            g.drawImage(sheet,(224-dw)/2,192-dh,(224-dw)/2+dw,192,l,t,r+1,b+1,null);g.dispose();
            String name="house_"+i+".png";ImageIO.write(image,"png",new File(folder,name));
            csv.append(i).append(',').append(l).append(',').append(t).append(',').append(r).append(',').append(b)
                .append(',').append(scale).append(',').append(name).append('\n');
            System.out.println(name+" source="+l+","+t+".."+r+","+b+" output="+dw+"x"+dh);
        }
        Files.writeString(new File(folder,"frames.csv").toPath(),csv,StandardCharsets.UTF_8);
    }
}
