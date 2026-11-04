import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/** Whole-body combat poses and their authored spear-grip anchors, cached at startup. */
final class CombatBody {
    static final int PHASES=4;
    record Frame(BufferedImage image,int handX,int handY){}
    private static Frame[][][] frames;
    private CombatBody(){}
    static boolean ready(){return frames!=null;}
    static int phase(int stage,double time){
        double start=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage);
        if(time<start)return 0;
        if(time<(start+end)/2)return 1;
        if(time<end)return 2;
        return 3;
    }
    static Frame at(Player.Dir dir,int stage,int phase){
        if(!ready())throw new IllegalStateException("Combat bodies not loaded");
        if(stage<0||stage>=SpearAttack.STAGES||phase<0||phase>=PHASES)
            throw new IllegalArgumentException("Invalid combat body phase");
        return frames[dir.ordinal()][stage][phase];
    }
    static Frame frame(Player.Dir dir,int stage,double time){
        return at(SpearAttack.bodyDirection(dir,time,stage),stage,phase(stage,time));
    }
    static void load()throws IOException{
        Frame[][][] loaded=new Frame[4][SpearAttack.STAGES][PHASES];
        String root="res/agyu-combat/";
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(open(root+"frames.csv"),StandardCharsets.UTF_8))){
            String header=reader.readLine();
            if(header==null)throw new IOException("Empty combat frame manifest");
            Map<String,Integer> columns=new HashMap<>();
            String[] labels=header.split(",",-1);
            for(int i=0;i<labels.length;i++)columns.put(labels[i].trim(),i);
            String line;int count=0;
            while((line=reader.readLine())!=null){
                if(line.isBlank())continue;
                String[] cells=line.split(",",-1);
                try{
                    Player.Dir dir=Player.Dir.valueOf(value(cells,columns,"direction").toUpperCase(java.util.Locale.ROOT));
                    if(dir==Player.Dir.LEFT)throw new IOException("Left combat poses are mirrored at load time");
                    int stage=Integer.parseInt(value(cells,columns,"stage"));
                    int phase=Integer.parseInt(value(cells,columns,"phase"));
                    int hx=Integer.parseInt(value(cells,columns,"hand_x"));
                    int hy=Integer.parseInt(value(cells,columns,"hand_y"));
                    if(stage<0||stage>=SpearAttack.STAGES||phase<0||phase>=PHASES||
                            Math.abs(hx)>30||hy>-3||hy<-48)throw new IOException("Invalid combat pose coordinates");
                    String file=value(cells,columns,"file");
                    if(!file.matches("(right|down|up)_[0-5]_[0-3]\\.png"))
                        throw new IOException("Invalid combat frame filename");
                    BufferedImage image;
                    try(InputStream input=open(root+file)){image=ImageIO.read(input);}
                    if(image==null||image.getWidth()!=64||image.getHeight()!=64||!image.getColorModel().hasAlpha())
                        throw new IOException("Combat body must be a transparent64px frame: "+file);
                    if(loaded[dir.ordinal()][stage][phase]!=null)throw new IOException("Duplicate combat pose");
                    loaded[dir.ordinal()][stage][phase]=new Frame(image,hx,hy);count++;
                }catch(IllegalArgumentException|IndexOutOfBoundsException ex){
                    throw new IOException("Malformed combat frame row: "+line,ex);
                }
            }
            if(count!=3*SpearAttack.STAGES*PHASES)throw new IOException("Expected72 authored combat body frames");
        }
        for(Player.Dir dir:new Player.Dir[]{Player.Dir.DOWN,Player.Dir.UP,Player.Dir.RIGHT})
            for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int phase=0;phase<PHASES;phase++)
                if(loaded[dir.ordinal()][stage][phase]==null)throw new IOException("Missing combat body pose");
        for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int phase=0;phase<PHASES;phase++){
            Frame right=loaded[Player.Dir.RIGHT.ordinal()][stage][phase];
            loaded[Player.Dir.LEFT.ordinal()][stage][phase]=
                    new Frame(Assets.flip(right.image),-1-right.handX,right.handY);
        }
        // Publish the complete cache together; rendering never loads or rebuilds images.
        frames=loaded;
    }
    private static String value(String[] cells,Map<String,Integer> columns,String name)throws IOException{
        Integer column=columns.get(name);
        if(column==null||column>=cells.length)throw new IOException("Missing combat manifest column: "+name);
        return cells[column].trim();
    }
    private static InputStream open(String file)throws IOException{
        InputStream resource=CombatBody.class.getResourceAsStream("/"+file);
        return resource!=null?resource:Files.newInputStream(Path.of(file));
    }
}
