package com.raytheon.uf.common.time.util;

import net.jcip.annotations.NotThreadSafe;

/**
 * 
 * A default {@link ITimer} implementation that will use
 * {@link TimeUtil#currentTimeMillis()} to keep track of time. It is good for
 * use inside both production code and tests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 16, 2012 0743       djohnson     Initial creation
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */
@NotThreadSafe
class TimerImpl extends AbstractTimer {

    /**
     * {@inheritDoc}
     */
    @Override
    protected long getCurrentTime() {
        return TimeUtil.currentTimeMillis();
    }
}
