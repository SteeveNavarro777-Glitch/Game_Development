import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;

/** Shared village sprites and deterministic 32-pixel terrain, cached at startup. */
final class VillageArt {
    static final BufferedImage[] houses=new BufferedImage[8],paving=new BufferedImage[3];
    static BufferedImage bridge;
    private VillageArt(){}
    static void load() throws IOException {
        for(int i=0;i<4;i++){
            String path="res/village/house_"+i+".png";
            try(InputStream stream=VillageArt.class.getResourceAsStream("/"+path)){
                houses[i]=stream==null?ImageIO.read(new File(path)):ImageIO.read(stream);
            }
            if(houses[i]==null||houses[i].getWidth()!=224||houses[i].getHeight()!=192)
                throw new IOException("Invalid village house: "+path);
        }
        Random random=new Random(218);
        for(int v=0;v<8;v++){
            BufferedImage image=tile();Graphics2D g=image.createGraphics();
            g.setColor(new Color(0x65804D));g.fillRect(0,0,32,32);
            for(int i=0;i<50;i++){
                g.setColor(new Color(i%3==0?0x738C56:0x597444));
                int x=random.nextInt(32),y=random.nextInt(32);g.fillRect(x,y,1+random.nextInt(2),1);
            }
            if(v==4||v==5)for(int i=0;i<3;i++){
                int x=4+random.nextInt(24),y=6+random.nextInt(20);
                g.setColor(new Color(0x92A968));g.drawLine(x,y,x-2,y-3);g.drawLine(x,y,x+2,y-4);
            }
            if(v>=6){
                int x=5+random.nextInt(22),y=6+random.nextInt(20);
                g.setColor(new Color(v==6?0xDFDEAD:0xDFC36F));
                g.fillRect(x-1,y,3,1);g.fillRect(x,y-1,1,3);
            }
            g.dispose();Assets.grass[v]=image;
        }
        for(int v=0;v<3;v++){
            BufferedImage image=tile();Graphics2D g=image.createGraphics();
            g.setColor(new Color(0xA48B5E));g.fillRect(0,0,32,32);
            for(int i=0;i<44;i++){
                g.setColor(new Color(i%2==0?0xB59C6B:0x927B52));
                g.fillRect(random.nextInt(32),random.nextInt(32),2,1);
            }
            g.dispose();Assets.dirt[v]=image;
            image=tile();g=image.createGraphics();g.setColor(new Color(0x766F55));g.fillRect(0,0,32,32);
            for(int y=0;y<32;y+=8)for(int x=-8;x<32;x+=16){
                int xx=x+(y/8%2)*8;
                g.setColor(new Color(v==0?0xA9A080:v==1?0xAAA081:0xA29A7A));g.fillRect(xx+1,y+1,15,7);
                g.setColor(new Color(0xC7BC98));g.drawLine(xx+2,y+1,xx+14,y+1);
                g.setColor(new Color(0x928A6B));g.drawLine(xx+2,y+7,xx+14,y+7);
                int noise=random.nextInt(9);g.setColor(new Color(0xB6AC8A));g.fillRect(xx+4+noise,y+3,2,1);
            }
            g.dispose();paving[v]=image;
        }
        for(int f=0;f<2;f++){
            BufferedImage image=tile();Graphics2D g=image.createGraphics();
            g.setColor(new Color(0x477E81));g.fillRect(0,0,32,32);
            for(int i=0;i<8;i++){
                int x=(i*11+f*3)%27,y=(i*7+4)%30;
                g.setColor(new Color(i%2==0?0x78A9A3:0x578E8C));g.fillRect(x,y,4,1);
            }
            g.dispose();Assets.water[f]=image;
        }
        BufferedImage rock=tile();Graphics2D g=rock.createGraphics();
        g.setColor(new Color(0x4E513D));g.fillRect(0,0,32,32);
        for(int y=0;y<32;y+=8)for(int x=-8;x<32;x+=16){
            int xx=x+(y/8%2)*8;g.setColor(new Color(0x7B7C5C));g.fillRect(xx+1,y+1,15,7);
            g.setColor(new Color(0x9B9E77));g.drawLine(xx+2,y+1,xx+14,y+1);
            g.setColor(new Color(0x5B6243));g.drawLine(xx+1,y+7,xx+14,y+7);
        }
        g.dispose();Assets.rock=rock;
        bridge=tile();g=bridge.createGraphics();g.setColor(new Color(0x4B442C));g.fillRect(0,0,32,32);
        for(int y=0;y<32;y+=6){
            g.setColor(new Color(y%12==0?0x9F8151:0xA88B59));g.fillRect(0,y+1,32,5);
            g.setColor(new Color(0xC1A36B));g.drawLine(0,y+1,31,y+1);
            g.setColor(new Color(0x75613E));g.drawLine(7,y+3,21,y+3);
        }
        g.dispose();
        RpgArt.load();
    }
    private static BufferedImage tile(){return new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);}
}
