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
package com.raytheon.uf.common.time.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * Utilities for time, some extracted from Util.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2009            njensen     Initial creation
 * Sep 11, 2012 1154       djohnson    Add MILLIS constants and isNewerDay().
 * Nov 09, 2012 1322       djohnson    Add SECONDS_PER_MINUTE.
 * 
 * </pre>
 * 
 * @author njensen
 * @version 1.0
 */

public class TimeUtil {

    /**
     * A clock that does not really return the current time. Useful when you
     * only want to keep track of times in a conditional sense, such as if a
     * logging priority is enabled. This is an example of the Null Object
     * pattern.
     * 
     * @see http://en.wikipedia.org/wiki/Null_Object_pattern
     * 
     * @author djohnson
     * 
     */
    private static class NullClock extends AbstractTimer {
        @Override
        protected long getCurrentTime() {
            return 1;
        }
    }

    /**
     * Delegates the retrieval of the current time to the system clock.
     * Production code will always use this.
     * 
     * @author djohnson
     * 
     */
    private static class SystemTimeStrategy implements ITimeStrategy {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }

    public static final String DATE_STRING = "(\\d{4})-(\\d{2})-(\\d{2})[ _](\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{1,3})";

    // Util.java has a few of these constants, but that is located in an EDEX
    // plugin and this is a more appropriate place for them anyways
    public static final long MILLIS_PER_SECOND = 1000;

    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;

    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;

    public static final long MILLIS_PER_YEAR = 3600 * 24 * 1000 * 365;

    public static final int SECONDS_PER_MINUTE = 60;

    // create instance of simple date format on class load, as instantiating it
    // is expensive the SimpleDateFormat class is not thread-safe,
    // so calling methods use synchronized
    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd_HH:mm:ss.S");

    private static SimpleDateFormat sqlSdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.S");

    static {
        sqlSdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    static final ITimeStrategy SYSTEM_TIME_STRATEGY = new SystemTimeStrategy();

    static final ITimer NULL_CLOCK = new NullClock();

    /**
     * The strategy to retrieve the "current time" value from.
     */
    static ITimeStrategy timeStrategy = SYSTEM_TIME_STRATEGY;

    /**
     * Converts a Calendar in the local time zone to a GMT date
     * 
     * @param cal
     *            A Calendar object in the local time zone
     * @return The GMT date
     */
    public static Date calendarToGMT(Calendar cal) {
        Date dt = null;
        synchronized (sdf) {
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String str = formatCalendar(cal);
            sdf.setTimeZone(TimeZone.getDefault());
            try {
                dt = sdf.parse(str);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return dt;
    }

    /**
     * Retrieve the current time in milliseconds. This method should be used
     * instead of {@link System#currentTimeMillis()}. This method DOES NOT use
     * {@link SimulatedTime} and therefore should be isolated to duration
     * checks, and logging type statements. If the desired result is the
     * currently configured system time, e.g. CAVE sessions where the user has
     * configured the system to a specific time. Those purposes are handled by
     * the {@link SimulatedTime} class. The {@link Date} and {@link Calendar}
     * returning methods in this class will delegate to {@link SimulatedTime}.
     * 
     * @see {@link SimulatedTime}
     * @return the current time in milliseconds
     */
    public static long currentTimeMillis() {
        return timeStrategy.currentTimeMillis();
    }

    /**
     * Formats a calendar object into the following format yyyy-MM-dd_HH:mm:ss.S
     * 
     * @param cal
     *            The calendar to format
     * @return The formatted result
     */
    public static String formatCalendar(Calendar cal) {
        String format = null;

        synchronized (sdf) {
            sdf.setTimeZone(cal.getTimeZone());
            format = sdf.format(cal.getTime());
        }
        return format;
    }

    /**
     * Retrieve date as a string in the index standard format: yyyy-MM-dd
     * kk:mm:ss.SSS
     * 
     * @param aCalendar
     *            A Calendar instance
     * @return The formatted date string from the Calendar instance
     */
    public static String formatDate(Calendar aCalendar) {
        return formatDate(aCalendar.getTime());
    }

    /**
     * Retrieve date as a string in the index standard format: yyyy-MM-dd
     * kk:mm:ss.SSS
     * 
     * @param aDate
     *            A Date instance
     * @return The formatted date string from the Date instance
     */
    public static String formatDate(Date aDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(aDate.getTime());
        return formatCalendar(cal);
    }

    public static long formattedDateToLong(String formattedDate) {
        long retVal = 0;
        try {
            retVal = sdf.parse(formattedDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static String formatToSqlTimestamp(Date aDate) {
        synchronized (sqlSdf) {
            return sqlSdf.format(aDate);
        }
    }

    /**
     * Retrieve a {@link ITimer} instance that will only actually keep track of
     * time if the specified priority level is enabled. This allows efficient
     * use of system resources, while calling code need not change.
     * 
     * @param handler
     *            the handler to use to check for a priority level being enabled
     * @param priority
     *            the priority level
     * @return the {@link ITimer} instance
     */
    public static ITimer getPriorityEnabledTimer(IUFStatusHandler handler,
            Priority priority) {
        return handler.isPriorityEnabled(priority) ? getTimer() : NULL_CLOCK;
    }

    /**
     * Retrieve a {@link ITimer} that allows the demarcation of arbitrary start
     * and stop times.
     * 
     * @return a {@link ITimer}
     */
    public static ITimer getTimer() {
        return new TimerImpl();
    }

    /**
     * Check whether the time represented by a {@link Date} is a new day
     * compared to another {@link Date} object.
     * 
     * @param earlierDate
     *            the earlier date
     * @param laterDate
     *            the later date
     * @param timeZone
     *            the timeZone to use when determining what date it is for the
     *            specified time
     * @return true if the laterDate is a new day compared to earlierDate
     */
    public static boolean isNewerDay(Date earlierDate, Date laterDate,
            TimeZone timeZone) {
        Calendar earlierCal = TimeUtil.newCalendar(timeZone);
        earlierCal.setTime(earlierDate);

        Calendar laterCal = TimeUtil.newCalendar(timeZone);
        laterCal.setTime(laterDate);

        return laterCal.get(Calendar.DAY_OF_YEAR) > earlierCal
                .get(Calendar.DAY_OF_YEAR)
                || laterCal.get(Calendar.YEAR) > earlierCal.get(Calendar.YEAR);
    }

    /**
     * Return a new {@link Calendar} instance. This method delegates to the
     * {@link SimulatedTime} class to determine the currently configured system
     * time.
     * 
     * @see {@link SimulatedTime}
     * @return the calendar
     */
    public static Calendar newCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(SimulatedTime.getSystemTime().getMillis());
        return cal;
    }

    /**
     * Return a new {@link Calendar} instance for the specified {@link TimeZone}
     * . This method delegates to the {@link SimulatedTime} class to determine
     * the currently configured system time.
     * 
     * @param timeZone
     *            the time zone
     * @see {@link SimulatedTime}
     * @return the calendar
     */
    public static Calendar newCalendar(TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(SimulatedTime.getSystemTime().getMillis());
        return cal;
    }

    /**
     * Return a new {@link Date} instance. This method delegates to the
     * {@link SimulatedTime} class to determine the currently configured system
     * time.
     * 
     * @see {@link SimulatedTime}
     * @return the current {@link Date}
     */
    public static Date newDate() {
        return SimulatedTime.getSystemTime().getTime();
    }

    /**
     * Return a new ImmutableDate. This method delegates to the
     * {@link SimulatedTime} class to determine the currently configured system
     * time.
     * 
     * @see {@link SimulatedTime}
     * @return an immutable date for the current time
     */
    public static ImmutableDate newImmutableDate() {
        return new ImmutableDate(SimulatedTime.getSystemTime().getMillis());
    }
}
