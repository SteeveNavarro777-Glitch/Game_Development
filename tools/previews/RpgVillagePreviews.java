/* FILE PURPOSE: PREVIEW TOOL | Export actual-camera work sites, NPC conversations, river animation and village overview.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageOutputStream;

/** Real game camera previews for the market, river, NPC interaction and ambient motion. */
public final class RpgVillagePreviews {
    // [BLOCK: ART TOOL] RpgVillagePreviews.RpgVillagePreviews - Prevent instances of this static helper class.
    private RpgVillagePreviews(){}
    // [BLOCK: ART TOOL] RpgVillagePreviews.get - Handle get for RpgVillagePreviews.
    private static Object get(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    // [BLOCK: ART TOOL] RpgVillagePreviews.set - Handle set for RpgVillagePreviews.
    private static void set(Object o,String name,double value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.setDouble(o,value);}
    // [BLOCK: ART TOOL] RpgVillagePreviews.main - Entry point for this developer tool.
    @SuppressWarnings("unchecked")
    public static void main(String[] args)throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);
        panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED));panel.update(1.0/60);panel.keys.clear();
        ((List<?>)get(panel,"enemies")).clear();List<Npc> people=(List<Npc>)get(panel,"npcs");Player player=(Player)get(panel,"player");
        for(String id:new String[]{"smith","vendor","fisher","weaver"}){
            Npc focus=people.stream().filter(n->n.id().equals(id)).findFirst().orElseThrow();
            place(panel,player,focus);
            for(int i=0;i<3;i++)panel.update(1.0/60);
            ImageIO.write(render(panel),"png",new File("build/preview-rpg-"+id+".png"));
            if(id.equals("weaver")){
                focus.talk(player);ImageIO.write(render(panel),"png",new File("build/preview-rpg-conversation.png"));
            }
        }
        Npc fisher=people.stream().filter(n->n.id().equals("fisher")).findFirst().orElseThrow();place(panel,player,fisher);
        Iterator<ImageWriter> writers=ImageIO.getImageWritersByFormatName("gif");
        ImageWriter writer=writers.next();
        try(ImageOutputStream out=ImageIO.createImageOutputStream(new File("build/preview-rpg-river.gif"))){
            writer.setOutput(out);writer.prepareWriteSequence(null);
            for(int frame=0;frame<48;frame++){
                for(int tick=0;tick<3;tick++)panel.update(1.0/60);
                BufferedImage image=render(panel);
                IIOMetadata metadata=writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
                String format=metadata.getNativeMetadataFormatName();IIOMetadataNode root=(IIOMetadataNode)metadata.getAsTree(format);
                IIOMetadataNode control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("delayTime","5");control.setAttribute("disposalMethod","none");
                if(frame==0){
                    IIOMetadataNode apps=new IIOMetadataNode("ApplicationExtensions"),loop=new IIOMetadataNode("ApplicationExtension");
                    loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");loop.setUserObject(new byte[]{1,0,0});apps.appendChild(loop);root.appendChild(apps);
                }
                metadata.setFromTree(format,root);writer.writeToSequence(new IIOImage(image,null,metadata),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        }finally{writer.dispose();}
        World world=(World)get(panel,"world");
        BufferedImage overview=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);
        Graphics2D og=overview.createGraphics();og.scale(.25,.25);
        world.drawGround(og,0,0,world.cols*World.T,world.rows*World.T,((Number)get(panel,"time")).doubleValue());
        java.util.List<Drawable> scene=new java.util.ArrayList<>();
        scene.addAll(world.trees);scene.addAll(world.buildings);scene.addAll(world.props);world.collectLandscape(scene,0,0,world.cols*World.T,world.rows*World.T);scene.addAll(people);
        scene.sort(java.util.Comparator.comparingDouble(Drawable::sortY));
        for(Drawable object:scene)object.draw(og,0,0,1);
        og.dispose();ImageIO.write(overview,"png",new File("build/preview-rpg-overview.png"));
        System.out.println("Previewed "+people.size()+" NPCs, "+world.buildings.size()+" homes and "+world.props.size()+" craft/ambient props.");
    }
    // [BLOCK: ART TOOL] RpgVillagePreviews.place - Handle place for RpgVillagePreviews.
    private static void place(GamePanel panel,Player player,Npc npc)throws Exception{
        World world=(World)get(panel,"world");
        for(int[] offset:new int[][]{{28,12},{0,28},{-28,12},{0,-28},{-28,0},{28,0}}){
            player.x=player.px=npc.x()+offset[0];player.y=player.py=npc.y()+offset[1];
            if(!world.blocked(player.feetBox()))break;
        }
        player.vx=player.vy=0;player.state=Player.State.IDLE;player.dir=Player.Dir.LEFT;
        double cx=Math.max(0,Math.min(player.x-240,world.cols*World.T-GamePanel.VIEW_W)),cy=Math.max(0,Math.min(player.y-200,world.rows*World.T-GamePanel.VIEW_H));
        set(panel,"camX",cx);set(panel,"prevCamX",cx);set(panel,"camY",cy);set(panel,"prevCamY",cy);set(panel,"alpha",1);
    }
    // [BLOCK: ART TOOL] RpgVillagePreviews.render - Handle render for RpgVillagePreviews.
    private static BufferedImage render(GamePanel panel){
        BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();panel.paint(g);g.dispose();return image;
    }
}
