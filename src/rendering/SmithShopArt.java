import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import javax.imageio.ImageIO;

/** Extracts the four supplied Rare/Legendary spear and shield shop icons. */
final class SmithShopArt {
    private static final String SHEET = "/items/agyu-weapons.png";

    private final BufferedImage rareSpear;
    private final BufferedImage legendarySpear;
    private final BufferedImage rareShield;
    private final BufferedImage legendaryShield;
    private final BufferedImage[][] spearEffects = new BufferedImage[2][8];
    private final BufferedImage[][] shieldEffects = new BufferedImage[2][8];

    SmithShopArt() {
        BufferedImage sheet = load(SHEET);
        rareSpear = icon(sheet, 18, 82, 112, 148, 58);
        legendarySpear = icon(sheet, 116, 82, 148, 148, 58);
        rareShield = icon(sheet, 17, 520, 112, 150, 58);
        legendaryShield = icon(sheet, 132, 520, 132, 150, 58);
        for (int frame = 0; frame < 8; frame++) {
            int x = 448 + frame * 200;
            spearEffects[0][frame] = effect(sheet, x, 0, 200, 112, 92, 52);
            spearEffects[1][frame] = effect(sheet, x, 112, 200, 118, 92, 52);
            shieldEffects[0][frame] = effect(sheet, x, 454, 200, 111, 94, 54);
            shieldEffects[1][frame] = effect(sheet, x, 566, 200, 116, 94, 54);
        }
    }

    BufferedImage icon(EquipmentLoadout.Gear gear, EquipmentLoadout.Level level) {
        if (gear == EquipmentLoadout.Gear.SPEAR) {
            return level == EquipmentLoadout.Level.LEGENDARY ? legendarySpear : rareSpear;
        }
        return level == EquipmentLoadout.Level.LEGENDARY ? legendaryShield : rareShield;
    }

    // [BLOCK: SUPPLIED EFFECT LOOKUP] SmithShopArt.effect - Return one cleaned blue/orange catalog frame for combat.
    BufferedImage effect(EquipmentLoadout.Gear gear, EquipmentLoadout.Level level, int frame) {
        int tier = level == EquipmentLoadout.Level.LEGENDARY ? 1 : 0;
        return (gear == EquipmentLoadout.Gear.SPEAR ? spearEffects : shieldEffects)[tier][Math.floorMod(frame, 8)];
    }

    private static BufferedImage load(String path) {
        try (InputStream in = SmithShopArt.class.getResourceAsStream(path)) {
            if (in != null) {
                return ImageIO.read(in);
            }
            File file = new File("res" + path);
            if (file.isFile()) {
                return ImageIO.read(file);
            }
        } catch (IOException ignored) {
            // A compact fallback icon is returned by icon() below.
        }
        return null;
    }

    private static BufferedImage icon(BufferedImage sheet, int x, int y, int w, int h, int size) {
        if (sheet == null) {
            return fallback(size);
        }
        int sx = Math.max(0, Math.min(x, sheet.getWidth() - 1));
        int sy = Math.max(0, Math.min(y, sheet.getHeight() - 1));
        int sw = Math.max(1, Math.min(w, sheet.getWidth() - sx));
        int sh = Math.max(1, Math.min(h, sheet.getHeight() - sy));
        BufferedImage crop = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D source = crop.createGraphics();
        try {
            source.drawImage(sheet, 0, 0, sw, sh, sx, sy, sx + sw, sy + sh, null);
        } finally {
            source.dispose();
        }
        clearConnectedCheckerboard(crop);
        keepLargestComponent(crop);

        int minX = sw, minY = sh, maxX = -1, maxY = -1;
        for (int py = 0; py < sh; py++) {
            for (int px = 0; px < sw; px++) {
                if ((crop.getRGB(px, py) >>> 24) != 0) {
                    minX = Math.min(minX, px);
                    minY = Math.min(minY, py);
                    maxX = Math.max(maxX, px);
                    maxY = Math.max(maxY, py);
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return fallback(size);
        }
        BufferedImage trimmed = crop.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        double scale = Math.min((double) size / trimmed.getWidth(), (double) size / trimmed.getHeight());
        int dw = Math.max(1, (int) Math.round(trimmed.getWidth() * scale));
        int dh = Math.max(1, (int) Math.round(trimmed.getHeight() * scale));
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(trimmed, (size - dw) / 2, (size - dh) / 2, dw, dh, null);
        g.dispose();
        return out;
    }

    /** Effects contain detached sparks, so unlike icons every non-background component is retained. */
    private static BufferedImage effect(BufferedImage sheet,int x,int y,int w,int h,int maxWidth,int maxHeight){
        if(sheet==null)return fallback(Math.min(maxWidth,maxHeight));
        int sx=Math.max(0,Math.min(x,sheet.getWidth()-1)),sy=Math.max(0,Math.min(y,sheet.getHeight()-1));
        int sw=Math.max(1,Math.min(w,sheet.getWidth()-sx)),sh=Math.max(1,Math.min(h,sheet.getHeight()-sy));
        BufferedImage crop=new BufferedImage(sw,sh,BufferedImage.TYPE_INT_ARGB);Graphics2D source=crop.createGraphics();
        try{source.drawImage(sheet,0,0,sw,sh,sx,sy,sx+sw,sy+sh,null);}finally{source.dispose();}
        clearConnectedCheckerboard(crop);
        int minX=sw,minY=sh,maxX=-1,maxY=-1;
        for(int py=0;py<sh;py++)for(int px=0;px<sw;px++)if((crop.getRGB(px,py)>>>24)>24){
            minX=Math.min(minX,px);minY=Math.min(minY,py);maxX=Math.max(maxX,px);maxY=Math.max(maxY,py);
        }
        if(maxX<minX||maxY<minY)return fallback(Math.min(maxWidth,maxHeight));
        BufferedImage trimmed=crop.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);
        double scale=Math.min((double)maxWidth/trimmed.getWidth(),(double)maxHeight/trimmed.getHeight());
        int dw=Math.max(1,(int)Math.round(trimmed.getWidth()*scale)),dh=Math.max(1,(int)Math.round(trimmed.getHeight()*scale));
        BufferedImage out=new BufferedImage(dw,dh,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(trimmed,0,0,dw,dh,null);}finally{g.dispose();}return out;
    }

    /** Flood-fill only edge-connected light neutral pixels, preserving highlights. */
    private static void clearConnectedCheckerboard(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        boolean[] seen = new boolean[w * h];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int x = 0; x < w; x++) {
            enqueueBackground(image, x, 0, seen, queue);
            enqueueBackground(image, x, h - 1, seen, queue);
        }
        for (int y = 1; y < h - 1; y++) {
            enqueueBackground(image, 0, y, seen, queue);
            enqueueBackground(image, w - 1, y, seen, queue);
        }
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            int packed = queue.removeFirst();
            int x = packed % w;
            int y = packed / w;
            image.setRGB(x, y, 0);
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                if (nx >= 0 && ny >= 0 && nx < w && ny < h) {
                    enqueueBackground(image, nx, ny, seen, queue);
                }
            }
        }
    }

    private static void enqueueBackground(BufferedImage image, int x, int y,
            boolean[] seen, ArrayDeque<Integer> queue) {
        int index = y * image.getWidth() + x;
        if (seen[index]) {
            return;
        }
        seen[index] = true;
        int argb = image.getRGB(x, y);
        int a = argb >>> 24;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        boolean transparent = a < 32;
        boolean checker = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) < 12
                && r > 215 && g > 215 && b > 215;
        if (transparent || checker) {
            queue.addLast(index);
        }
    }

    /** Neighboring catalog variants can overlap a rectangular crop; retain only the selected connected sprite. */
    private static void keepLargestComponent(BufferedImage image) {
        int w=image.getWidth(),h=image.getHeight();
        int[] component=new int[w*h];java.util.Arrays.fill(component,-1);
        int largest=-1,largestSize=0,next=0;
        int[] dx={-1,0,1,-1,1,-1,0,1},dy={-1,-1,-1,0,0,1,1,1};
        ArrayDeque<Integer> queue=new ArrayDeque<>();
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int start=y*w+x;if(component[start]>=0||(image.getRGB(x,y)>>>24)==0)continue;
            int id=next++,size=0;component[start]=id;queue.add(start);
            while(!queue.isEmpty()){
                int p=queue.removeFirst(),px=p%w,py=p/w;size++;
                for(int i=0;i<8;i++){
                    int nx=px+dx[i],ny=py+dy[i];if(nx<0||ny<0||nx>=w||ny>=h)continue;
                    int n=ny*w+nx;if(component[n]>=0||(image.getRGB(nx,ny)>>>24)==0)continue;
                    component[n]=id;queue.addLast(n);
                }
            }
            if(size>largestSize){largestSize=size;largest=id;}
        }
        if(largest<0)return;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(component[y*w+x]!=largest)image.setRGB(x,y,0);
    }

    private static BufferedImage fallback(int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setColor(new java.awt.Color(42, 180, 238));
        g.fillOval(size / 4, size / 4, size / 2, size / 2);
        g.dispose();
        return out;
    }
}
