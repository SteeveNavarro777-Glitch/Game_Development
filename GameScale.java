import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Scene proportions, applied about ground anchors rather than image corners. */
final class GameScale {
    static final double AGYU=.85,TRADITIONAL_HOME=.80;
    private GameScale(){}
    static Graphics2D agyuGraphics(Graphics2D target,double x,double y){
        Graphics2D g=(Graphics2D)target.create();
        g.translate(x,y);g.scale(AGYU,AGYU);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        return g;
    }
    static void agyuFrame(Graphics2D target,BufferedImage image,double x,double y){
        Graphics2D g=agyuGraphics(target,x,y);
        try{g.drawImage(image,-image.getWidth()/2,-image.getHeight()+1,null);}finally{g.dispose();}
    }
}
