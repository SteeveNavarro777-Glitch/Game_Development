/* FILE PURPOSE: TITLE AUDIO | Menu music and confirmation clicks from the supplied assets. */
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

final class TitleAudio {
    private Clip music, click;
    private boolean started, closed, musicRequested;

    synchronized void start(boolean enabled) {
        if (started || closed || GraphicsEnvironment.isHeadless()) return;
        started = true;
        musicRequested = enabled;
        Thread worker = new Thread(this::load, "Agyu title audio");
        worker.setDaemon(true);
        worker.start();
    }

    private void load() {
        Clip theme = open("menu_music.wav", -11);
        Clip feedback = open("click.wav", -9);
        synchronized (this) {
            if (closed) {
                if (theme != null) theme.close();
                if (feedback != null) feedback.close();
                return;
            }
            music = theme;
            click = feedback;
            setMusic(musicRequested);
        }
    }

    synchronized void setMusic(boolean enabled) {
        musicRequested = enabled;
        if (music == null) return;
        if (enabled && !music.isRunning()) music.loop(Clip.LOOP_CONTINUOUSLY);
        if (!enabled && music.isRunning()) music.stop();
    }

    synchronized void click(boolean enabled) {
        if (!enabled || click == null) return;
        click.stop();
        click.setFramePosition(0);
        click.start();
    }

    synchronized void close() {
        closed = true;
        if (music != null) music.close();
        if (click != null) click.close();
        music = click = null;
    }

    private static Clip open(String name, float gain) {
        Clip clip = null;
        try (BufferedInputStream raw = new BufferedInputStream(Assets.openResource("res/title/" + name));
             AudioInputStream wav = AudioSystem.getAudioInputStream(raw)) {
            clip = AudioSystem.getClip();
            clip.open(wav);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Math.max(volume.getMinimum(), Math.min(gain, volume.getMaximum())));
            }
            return clip;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException
                | IllegalArgumentException | SecurityException unavailable) {
            if (clip != null) clip.close();
            return null;
        }
    }
}
