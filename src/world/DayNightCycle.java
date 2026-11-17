/* FILE PURPOSE: WORLD CLOCK | Three-minute day/night phases, numbered cycles and continuous dusk/dawn lighting.
 * Course examples: enum states, encapsulation and deterministic simulation; docs/CODE_GUIDE.md.
 */
import java.util.Locale;

/** Advances only with active world simulation; render queries never change the clock. */
final class DayNightCycle {
    enum Phase { DAY,NIGHT }
    static final double PHASE_SECONDS=180,TRANSITION_SECONDS=12;
    private static final double EPSILON=1e-8;
    private long phaseIndex;
    private double seconds;
    // [BLOCK: SIMULATION CLOCK] DayNightCycle.update - Preserve remainder and all phase crossings, including coarse elapsed-time steps.
    void update(double dt){
        if(!Double.isFinite(dt)||dt<=0)return;
        double total=seconds+dt;long crossings=(long)Math.floor((total+EPSILON)/PHASE_SECONDS);
        seconds=Math.max(0,total-crossings*PHASE_SECONDS);phaseIndex+=crossings;
    }
    // [BLOCK: ENUM STATE] DayNightCycle.phase - Derive the alternating phase from the transition count.
    Phase phase(){return (phaseIndex&1)==0?Phase.DAY:Phase.NIGHT;}
    // [BLOCK: ENCAPSULATION] DayNightCycle.night - Identify the gameplay night phase independently of the visual transition.
    boolean night(){return phase()==Phase.NIGHT;}
    // [BLOCK: NUMBERED CYCLE] DayNightCycle.number - Return Day 1/Night 1, Day 2/Night 2 and subsequent cycle numbers.
    long number(){return phaseIndex/2+1;}
    // [BLOCK: ENCAPSULATION] DayNightCycle.phaseSeconds - Read active seconds elapsed in the current three-minute phase.
    double phaseSeconds(){return seconds;}
    // [BLOCK: COUNTDOWN] DayNightCycle.remainingSeconds - Round the remaining phase duration for a stable whole-second HUD.
    int remainingSeconds(){return Math.max(1,(int)Math.ceil(PHASE_SECONDS-seconds-EPSILON));}
    // [BLOCK: CLOCK LABEL] DayNightCycle.label - Provide the numbered phase without exposing writable clock state.
    String label(){return phase()+" "+number();}
    // [BLOCK: CLOCK LABEL] DayNightCycle.countdown - Format the remaining interval as minutes and seconds.
    String countdown(){int remaining=remainingSeconds();return String.format(Locale.ROOT,"%02d:%02d",remaining/60,remaining%60);}
    // [BLOCK: CONTINUOUS LIGHTING] DayNightCycle.darkness - Fade through the final twelve seconds of each phase with no boundary jump.
    double darkness(){
        double u=Math.max(0,Math.min(1,(seconds-(PHASE_SECONDS-TRANSITION_SECONDS))/TRANSITION_SECONDS));
        double blend=u*u*(3-2*u);return night()?1-blend:blend;
    }
}
