/* FILE PURPOSE: MOUNTAIN ART CACHE | Detailed pixel mountain, four falling-water phases and soft pool mist.
 * Course examples: arrays, deterministic iteration and shared image caches; docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Art is built once at half resolution and enlarged with nearest-neighbor pixels. */
final class MountainArt {
    private static BufferedImage mountain;
    // [SECTION: DSA - ANIMATION ARRAYS] Four immutable waterfall/mist phases are selected by the simulation clock.
    private static final BufferedImage[] falls=new BufferedImage[4],clouds=new BufferedImage[4];
    // [BLOCK: CACHE UTILITY] MountainArt.MountainArt - Prevent instances of this shared artwork cache.
    private MountainArt(){}
    // [BLOCK: CACHE STARTUP] MountainArt.load - Publish one mountain and four reusable waterfall and mist frames.
    private static synchronized void load(){
        if(mountain!=null)return;
        BufferedImage rock=buildMountain();
        for(int phase=0;phase<4;phase++){falls[phase]=buildFall(phase);clouds[phase]=buildMist(phase);}
        mountain=rock;
    }
    // [BLOCK: RENDER LOOKUP] MountainArt.mountain - Return the 1024 by 1280 mountain, with the waterfall source at local 512,704.
    static BufferedImage mountain(){load();return mountain;}
    // [BLOCK: ANIMATION ARRAY] MountainArt.waterfall - Return a cached 160 by 448 falling-water phase.
    static BufferedImage waterfall(int phase){load();return falls[Math.floorMod(phase,falls.length)];}
    // [BLOCK: ANIMATION ARRAY] MountainArt.mist - Return a cached 256 by 96 pool mist phase, centered below the waterfall foot.
    static BufferedImage mist(int phase){load();return clouds[Math.floorMod(phase,clouds.length)];}
    // [BLOCK: PIXEL ART CACHE] MountainArt.buildMountain - Build an asymmetric range with stratified cliffs, moss ledges and wooded foothills.
    private static BufferedImage buildMountain(){
        BufferedImage image=new BufferedImage(512,640,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        Random random=new Random(0x4D0A17A1L);
        Polygon outline=polygon(new int[]{6,16,40,54,86,114,140,164,187,207,228,259,286,302,333,356,381,397,431,450,476,493,506,480,448,372,320,256,192,120,66,27},
                new int[]{590,512,448,360,335,252,214,108,25,102,142,58,127,183,115,191,228,302,335,398,445,531,591,618,630,637,639,635,639,632,622,617});
        try{
            color(g,0x283E3B);g.fillPolygon(outline);g.clip(outline);
            // Rear ridges separate the major peaks instead of forming one flat triangular slope.
            fill(g,0x536658,new int[]{23,140,187,205,229,190,160,116,62},new int[]{513,214,25,112,187,230,305,367,468});
            fill(g,0x77806A,new int[]{140,164,187,193,176,173,160,152,128},new int[]{214,108,25,119,141,197,229,285,319});
            fill(g,0x40534A,new int[]{187,217,238,259,273,302,319,310,253,211},new int[]{25,133,155,58,102,183,292,386,363,219});
            fill(g,0x718071,new int[]{228,259,271,269,284,279,254,244,220},new int[]{176,58,101,158,188,233,273,322,277});
            fill(g,0x4E6456,new int[]{302,333,356,381,397,422,410,370,340,319},new int[]{183,115,191,228,302,355,441,415,334,292});
            fill(g,0x7F866E,new int[]{319,333,338,352,353,374,368,344,337},new int[]{235,115,180,205,249,289,320,303,271});
            // Broad front faces carry warm rock terraces surrounded by highland vegetation.
            fill(g,0x67715B,new int[]{42,95,148,197,222,252,280,315,364,417,475,493,469,395,307,212,117,56},
                    new int[]{461,362,307,283,299,274,289,331,352,372,458,556,615,636,633,631,622,599});
            fill(g,0x829078,new int[]{52,106,148,187,220,199,165,132,109,65},new int[]{483,389,335,323,351,391,427,467,531,565});
            fill(g,0x394E43,new int[]{278,310,350,387,430,462,483,461,413,369,336,310},new int[]{318,321,357,371,400,470,550,603,589,522,461,391});
            texture(image,random);
            // Repeated broken strata form ledges rather than regular brickwork.
            for(int y=230;y<603;y+=28){
                int left=36+random.nextInt(46),right=460-random.nextInt(34);
                int[] xs={left,left+34,left+77,left+118,left+161,left+207,left+252,left+298,right};
                int[] ys=new int[xs.length];for(int i=0;i<ys.length;i++)ys[i]=y+random.nextInt(14)-7;
                color(g,0x374A3D);for(int i=1;i<xs.length;i++)g.drawLine(xs[i-1],ys[i-1]+4,xs[i],ys[i]+4);
                color(g,0xA2A084);for(int i=1;i<xs.length;i++)g.drawLine(xs[i-1],ys[i-1],xs[i],ys[i]);
                for(int i=1;i<xs.length;i+=2){color(g,0x50713E);g.fillRect(xs[i]-15,ys[i]-2,22+random.nextInt(18),3);}
            }
            // The tall central escarpment frames the real waterfall overlay.
            fill(g,0x49564B,new int[]{183,206,232,267,302,327,331,322,291,247,205,179},
                    new int[]{347,334,342,335,344,365,434,495,555,591,550,452});
            fill(g,0x7C816B,new int[]{183,207,225,222,234,231,215,218,204,179},
                    new int[]{347,336,343,394,434,475,510,563,552,452});
            fill(g,0x8D8B73,new int[]{276,302,327,320,326,312,309,288,276},
                    new int[]{340,344,365,412,448,495,534,559,504});
            for(int side=0;side<2;side++)for(int row=0;row<9;row++){
                int x=(side==0?186:283)+random.nextInt(15),y=362+row*23;
                color(g,row%2==0?0xA9A58A:0x6B705C);g.fillRect(x,y,22+random.nextInt(15),2);
                color(g,0x3A4B40);g.drawLine(x+random.nextInt(18),y+4,x+random.nextInt(18),y+14);
                moss(g,x+random.nextInt(16),y-3,12+random.nextInt(12),random);
            }
            // Visible upper feeder crosses the stone shelf and spills from a dark notch at y352.
            fill(g,0x2C4D4C,new int[]{307,290,278,266,246,238,250,270,286,302},
                    new int[]{305,319,327,335,342,354,361,352,340,326});
            fill(g,0x4CAAA8,new int[]{302,288,276,264,246,246,265,282,297},
                    new int[]{309,323,331,341,349,355,346,337,321});
            color(g,0xB4E6D5);g.drawLine(297,318,279,333);g.drawLine(273,337,254,348);
            color(g,0x344D42);g.fillOval(221,342,73,17);color(g,0x47726C);g.fillOval(227,345,59,12);
            color(g,0x76C8BB);g.fillRect(237,349,40,5);color(g,0xD5EEE0);g.fillRect(240,351,32,2);
            color(g,0xB0AA83);g.fillRect(220,341,14,4);g.fillRect(280,345,16,4);
            // Vegetation follows ledges, leaving the mouth and vertical cascade unobstructed.
            for(int i=0;i<240;i++){
                int x=25+random.nextInt(462),y=200+random.nextInt(420);
                if(outline.contains(x,y)&&!(x>219&&x<286&&y>328))moss(g,x,y,4+random.nextInt(11),random);
            }
            for(int i=0;i<110;i++){
                int x=24+random.nextInt(464),y=435+random.nextInt(189);
                if(outline.contains(x,y)&&!(x>218&&x<293&&y>520))shrub(g,x,y,7+random.nextInt(10),random);
            }
            // Foothill shelf and its irregular moss edge visually join the lower forest floor.
            fill(g,0x3A5735,new int[]{7,40,83,141,191,244,288,337,391,445,487,506,485,448,373,301,220,153,87,38},
                    new int[]{592,574,594,580,604,592,603,588,601,588,581,599,619,631,637,635,640,635,627,619});
            for(int i=0;i<160;i++){
                int x=12+random.nextInt(488),y=595+random.nextInt(40);
                if(outline.contains(x,y)&&!(x>207&&x<305&&y<624))moss(g,x,y,5+random.nextInt(13),random);
            }
            for(int i=0;i<28;i++){
                int x=35+i*17+random.nextInt(9),y=586+random.nextInt(38);
                if(x<213||x>306)shrub(g,x,y,12+random.nextInt(8),random);
            }
            g.setClip(null);
            // Leave the downstream basin and outlet transparent so the real river joins the waterfall foot.
            g.setComposite(java.awt.AlphaComposite.Clear);
            g.fillPolygon(polygon(new int[]{232,280,294,313,313,207,207,221},
                    new int[]{573,573,592,615,640,640,610,591}));
        }finally{g.dispose();}return enlarge(image);
    }
    // [BLOCK: SEEDED ROCK DETAIL] MountainArt.texture - Add muted mineral chips and small fissures inside the opaque mountain silhouette.
    private static void texture(BufferedImage image,Random random){
        for(int y=32;y<632;y++)for(int x=6;x<506;x++){
            int pixel=image.getRGB(x,y);if((pixel>>>24)==0)continue;
            int adjustment=random.nextInt(13)-6;
            if(Math.floorMod(x*31+y*17,67)==0)adjustment-=13;
            if((x+y*3)%89==0)adjustment+=13;
            int r=clamp(((pixel>>>16)&255)+adjustment),g=clamp(((pixel>>>8)&255)+adjustment),b=clamp((pixel&255)+adjustment);
            image.setRGB(x,y,0xFF000000|(r<<16)|(g<<8)|b);
        }
    }
    // [BLOCK: CACHED ANIMATION ART] MountainArt.buildFall - Draw a continuous luminous drop with four displaced streak and splash phases.
    private static BufferedImage buildFall(int phase){
        BufferedImage image=new BufferedImage(80,224,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            for(int y=0;y<218;y++){
                int sway=(int)Math.round(Math.sin(y*.08)*1.6),spread=y/48;
                g.setColor(new Color(59,146,153,112));g.fillRect(15+sway-spread,y,50+spread*2,1);
                color(g,0x337F91);g.fillRect(22+sway-spread/2,y,38+spread,1);
                color(g,0x55B7C5);g.fillRect(24+sway-spread/2,y,32+spread,1);
                color(g,0xA4E6E7);g.fillRect(25+sway,y,9,1);
                color(g,0xD6F6ED);g.fillRect(26+sway,y,4,1);
                color(g,0x7AD4DB);g.fillRect(38+sway,y,13,1);
                if(y%13<5){color(g,0xBCEEE9);g.fillRect(42+sway,y,3,1);}
                color(g,0x30778A);g.fillRect(56+sway+spread/2,y,2,1);
            }
            for(int i=0;i<25;i++){
                int x=27+(i*11)%27,y=Math.floorMod(i*29+phase*13,218),length=8+(i*7)%23;
                color(g,i%3==0?0xF0FFF6:0xB0ECE9);g.fillRect(x,y,i%4==0?2:1,Math.min(length,219-y));
            }
            g.setColor(new Color(193,246,236,165));g.fillOval(11,207,61,15);
            color(g,0x8BDCD7);g.fillOval(5,214,71,9);color(g,0xE7FAEC);g.fillOval(20,213,44,7);
            for(int i=0;i<18;i++){
                int x=6+(i*23+phase*3)%68,y=201+(i*7+phase*4)%21;
                g.setColor(new Color(219,251,238,100+(i%3)*45));g.fillRect(x,y,2+(i%3),1+(i%2));
            }
        }finally{g.dispose();}return enlarge(image);
    }
    // [BLOCK: CACHED POOL MIST] MountainArt.buildMist - Cache layered translucent splash clouds and foam ripples beneath the fall.
    private static BufferedImage buildMist(int phase){
        BufferedImage image=new BufferedImage(128,48,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            for(int layer=0;layer<6;layer++){
                int width=112-layer*13,y=22-layer*3;
                g.setColor(new Color(192+layer*8,238+layer*2,221+layer*4,17+layer*9));
                g.fillOval(64-width/2+(phase+layer)%3-1,y,width,15+layer*2);
            }
            for(int i=0;i<20;i++){
                int x=13+(i*19+phase*2)%103,y=8+(i*11+phase*3)%25;
                g.setColor(new Color(224,255,241,42+(i%4)*18));g.fillRect(x,y,2+i%4,1+i%2);
            }
            g.setColor(new Color(170,234,220,150));g.drawArc(20,25,88,16,190,160);
            g.setColor(new Color(221,251,234,185));g.drawArc(37-phase,29,54+phase*2,11,185,170);
        }finally{g.dispose();}return enlarge(image);
    }
    // [BLOCK: PIXEL FOLIAGE] MountainArt.moss - Place a broken moss clump with ochre lichen and a shaded lower edge.
    private static void moss(Graphics2D g,int x,int y,int width,Random random){
        color(g,0x34513A);g.fillRect(x-width/2,y,width,3);
        color(g,0x527443);g.fillRect(x-width/2+1,y-2,Math.max(2,width-2),3);
        color(g,0x7D9454);g.fillRect(x-width/3,y-3,Math.max(2,width/2),2);
        if(random.nextBoolean()){color(g,0xA0A567);g.fillRect(x+width/4,y-2,2,1);}
    }
    // [BLOCK: PIXEL FOLIAGE] MountainArt.shrub - Add a small wooded ledge silhouette with layered highland foliage.
    private static void shrub(Graphics2D g,int x,int y,int size,Random random){
        color(g,0x5A4B35);g.fillRect(x-1,y-6,2,9);
        color(g,0x233D31);g.fillOval(x-size/2,y-size,size,size-2);
        color(g,0x3B5D38);g.fillOval(x-size/2-1,y-size-2,size-1,size-5);
        color(g,0x607A43);g.fillOval(x-size/3,y-size-2,Math.max(3,size*2/3),Math.max(3,size/3));
        color(g,0x859452);g.fillRect(x-size/4,y-size,2+random.nextInt(3),1);
    }
    // [BLOCK: NEAREST PIXEL SCALE] MountainArt.enlarge - Expand a prepared half-resolution cache without smoothing or interpolation blur.
    private static BufferedImage enlarge(BufferedImage source){
        BufferedImage image=new BufferedImage(source.getWidth()*2,source.getHeight()*2,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(source,0,0,image.getWidth(),image.getHeight(),null);}finally{g.dispose();}
        return image;
    }
    // [BLOCK: PIXEL PALETTE] MountainArt.clamp - Keep textured rock channels in the legal byte range.
    private static int clamp(int value){return Math.max(0,Math.min(255,value));}
    // [BLOCK: PIXEL GEOMETRY] MountainArt.polygon - Build one integer-pixel silhouette from paired coordinate arrays.
    private static Polygon polygon(int[] xs,int[] ys){return new Polygon(xs,ys,xs.length);}
    // [BLOCK: PIXEL GEOMETRY] MountainArt.fill - Fill a flat facet before applying seeded chips and ledge detail.
    private static void fill(Graphics2D g,int rgb,int[] xs,int[] ys){color(g,rgb);g.fillPolygon(polygon(xs,ys));}
    // [BLOCK: PIXEL PALETTE] MountainArt.color - Select an opaque cached-art palette color.
    private static void color(Graphics2D g,int rgb){g.setColor(new Color(rgb));}
}
