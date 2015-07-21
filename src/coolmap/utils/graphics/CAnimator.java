package coolmap.utils.graphics;

import java.util.concurrent.TimeUnit;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.interpolators.AccelerationInterpolator;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

/**
 *
 * @author gangsu
 */
public class CAnimator {

    private static final SwingTimerTimingSource _timingSource = new SwingTimerTimingSource();
    private static final AccelerationInterpolator _easing = new AccelerationInterpolator(0.35, 0.35);
    
    public static Animator createInstance(TimingTarget target, int duration){
        _timingSource.init();
        return new Animator.Builder(_timingSource).addTarget(target).setInterpolator(_easing).setDuration(duration, TimeUnit.MILLISECONDS).build();
    }
}
