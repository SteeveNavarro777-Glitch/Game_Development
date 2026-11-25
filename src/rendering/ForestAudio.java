/* FILE PURPOSE: FOREST AUDIO | A short synthesized scare sting with bounded asynchronous playback and no external sound dependency.
 * Course examples: encapsulated shared resources and concurrency guard; docs/CODE_GUIDE.md.
 */
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/** Sound is optional: headless tests and computers without an output device retain the visual encounter. */
final class ForestAudio {
    private static final AtomicBoolean PLAYING=new AtomicBoolean();
    private static volatile boolean enabled=true;
    private static final int RATE=16000;
    private static final byte[] STING=sting();
    // [BLOCK: AUDIO UTILITY] ForestAudio.ForestAudio - Prevent instances of the single bounded sound provider.
    private ForestAudio(){}
    // [BLOCK: SETTINGS PREFERENCE] ForestAudio.setEnabled - Let the title menu silence optional effects without touching combat state.
    static void setEnabled(boolean value){enabled=value;}
    // [BLOCK: CACHED PCM ARRAY] ForestAudio.sting - Prepare a brief low-volume descending growl with a smooth attack and release.
    private static byte[] sting(){
        byte[] result=new byte[RATE*3/10];
        for(int i=0;i<result.length;i++){
            double u=i/(double)result.length,envelope=Math.min(1,u*25)*Math.pow(1-u,2),phase=2*Math.PI*(220*i/(double)RATE-100*i*i/(2d*RATE*result.length));
            double value=(Math.sin(phase)+.32*Math.sin(phase*2.71)+.18*Math.sin(i*1.61))*envelope;
            result[i]=(byte)Math.round(value*27);
        }
        return result;
    }
    // [BLOCK: BOUNDED AUDIO PLAYBACK] ForestAudio.scare - Play at most one sting off the game loop, release its device and tolerate missing audio hardware.
    static void scare(){
        if(!enabled||GraphicsEnvironment.isHeadless()||!PLAYING.compareAndSet(false,true))return;
        Thread worker=new Thread(()->{
            try(Clip clip=AudioSystem.getClip()){
                clip.open(new AudioFormat(RATE,8,1,true,false),STING,0,STING.length);clip.start();Thread.sleep(360);
            }catch(LineUnavailableException|IllegalArgumentException|SecurityException unavailable){
                // The visual scare is complete even when this machine has no usable audio output.
            }catch(InterruptedException interrupted){Thread.currentThread().interrupt();}
            finally{PLAYING.set(false);}
        },"Agyu forest sting");worker.setDaemon(true);worker.start();
    }
}
