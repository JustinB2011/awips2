/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simulated time clock with offset and scale capabilities.
 * 
 * Provides a simulated time which can be offset from real time, frozen, and/or
 * accelerated/decelerated.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date			Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * Jul 16, 2008				randerso	Initial creation
 * 
 * </pre>
 * 
 * @author randerso
 * @version 1.0
 */

public class SimulatedTime {
    /**
     * The system global simulated time instance
     */
    private static final SimulatedTime systemTime = new SimulatedTime();

    /**
     * Retrieve the system global simulate time instance
     * 
     * @return
     */
    public static SimulatedTime getSystemTime() {
        return systemTime;
    }

    /**
     * Base time for simulation
     */
    private long baseTime;

    /**
     * Offset between real time and simulated time
     */
    private long offset;

    /**
     * Simulated time scale
     */
    private double scale;

    /**
     * True if simulated time is frozen
     */
    private boolean isFrozen;

    /**
     * Time when simulated time was last frozen
     */
    private long frozenTime;

    /**
     * Convenience method for getting current time
     * 
     * @return
     */
    private long now() {
        return System.currentTimeMillis();
    }

    /**
     * Creates a simulated time that matches real time.
     * 
     */
    public SimulatedTime() {
        setRealTime();
    }

    /**
     * Creates a simulated time starting at the specified time with
     * acceleration/deceleration, optionally frozen.
     * 
     * @param date
     *            starting time
     * @param scale
     *            1.0 for normal time rate, >1.0 for accelerated time < 1.0 for
     *            decelerated time. If negative time will run backward.
     * @param isFrozen
     *            true to freeze time
     */
    public SimulatedTime(Date date, double scale, boolean isFrozen) {
        setTime(date);
        this.scale = scale;
        this.isFrozen = isFrozen;
    }

    /**
     * Determine is simulated time = real time
     * 
     * @return true if simulated time = real time
     */
    public boolean isRealTime() {
        return offset == 0 && scale == 1.0 && !isFrozen;
    }

    /**
     * Set the simulated time to real time
     */
    public void setRealTime() {
        baseTime = now();
        offset = 0;
        scale = 1.0;
        isFrozen = false;
    }

    /**
     * Get the current simulated time
     * 
     * @return current simulated time
     */
    public Date getTime() {
        if (isFrozen) {
            return new Date(frozenTime);
        } else {
            return new Date(Math.round((now() - baseTime) * scale) + baseTime
                    + offset);
        }
    }

    /**
     * Set the current simulated time
     * 
     * @param date
     */
    public void setTime(Date date) {
        if (isFrozen) {
            frozenTime = date.getTime();
        } else {
            baseTime = now();
            offset = date.getTime() - baseTime;
        }
    }

    /**
     * Get the simulated time scale (acceleration/deceleration)
     * 
     * @return 1.0 for normal time rate, >1.0 for accelerated time < 1.0 for
     *         decelerated time. If negative time will run backward.
     */
    public double getScale() {
        return scale;
    }

    /**
     * Set the simulated time scale (acceleration/deceleration)
     * 
     * @param scale
     *            1.0 for normal time rate, >1.0 for accelerated time < 1.0 for
     *            decelerated time. If negative time will run backward.
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Get the simulated time frozen state
     * 
     * @return true if time is frozen
     */
    public boolean isFrozen() {
        return isFrozen;
    }

    /**
     * Freeze/unfreeze simulated time
     * 
     * @param isFrozen
     *            true to freeze, false to unfreeze
     */
    public void setFrozen(boolean isFrozen) {
        if (this.isFrozen == isFrozen) {
            return;
        }

        if (isFrozen) {
            frozenTime = getTime().getTime();
        } else {
            baseTime = now();
            offset = frozenTime - baseTime;
        }
        this.isFrozen = isFrozen;
    }

    /**
     * Test routine
     * 
     * @param args
     *            N/A
     */
    public static void main(String[] args) {
        String timeFormat = "HH:mm:ss z dd-MMM-yyyy";
        TimeZone GMT = TimeZone.getTimeZone("GMT");

        SimpleDateFormat gmtFormatter = new SimpleDateFormat(timeFormat);
        gmtFormatter.setTimeZone(GMT);

        SimpleDateFormat local = new SimpleDateFormat(timeFormat);

        Calendar cal = Calendar.getInstance(GMT);
        cal.clear();
        cal.set(1960, 5, 5, 0, 0, 0);

        SimulatedTime simTime = new SimulatedTime(cal.getTime(), 2.0, false);
        for (int i = 0; i < 15; i++) {
            Date date = simTime.getTime();
            System.out.println(gmtFormatter.format(date) + ", "
                    + local.format(date)
                    + (simTime.isFrozen() ? " frozen" : ""));
            simTime.setFrozen(i >= 5 && i < 10);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
