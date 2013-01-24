package com.raytheon.uf.common.time.util;

/**
 * 
 * Defines a timer that can be started and stopped.
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
public interface ITimer {

    /**
     * Start the time on the timer.
     * 
     * @throws IllegalStateException
     *             if the timer is already running, or has been stopped and not
     *             reset
     */
    void start() throws IllegalStateException;

    /**
     * Stop the time on the timer.
     * 
     * @throws IllegalStateException
     *             if the timer hasn't been started
     */
    void stop() throws IllegalStateException;

    /**
     * Get the elapsed time, in milliseconds.
     * 
     * @return the elapsed time in milliseconds
     */
    long getElapsedTime();

    /**
     * Reset the timer.
     */
    void reset();
}
