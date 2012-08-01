package com.raytheon.uf.edex.plugin.ffmp.common;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.ffmp.FFMPBasin;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPBasinData;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPDataContainer;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPGuidanceBasin;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPRecord;
import com.raytheon.uf.common.monitor.xml.SourceXML;
import com.raytheon.uf.edex.plugin.ffmp.FFMPGenerator;

/**
 * Interpolated Guidance delay
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 July, 2011 6772          dhladky     Initial creation
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */

public class FFMPInterpolatedGuidanceDelay {

    private SourceXML qpeSource = null;

    private SourceXML ffgSource = null;

    private FFMPBasinData previousGuidanceData = null;

    private FFMPRecord currentRecord = null;

    private Date backDate = null;

    private Date newDate = null;

    private FFMPGenerator generator = null;

    private String siteKey = null;

    private long guidFrequency = 0l;

    public FFMPInterpolatedGuidanceDelay(String siteKey, long guidFrequency,
            SourceXML ffgSource, SourceXML qpeSource, Date backDate,
            Date newDate, FFMPGenerator generator,
            FFMPBasinData previousGuidanceData, FFMPRecord currentRecord) {
        this.ffgSource = ffgSource;
        this.qpeSource = qpeSource;
        this.previousGuidanceData = previousGuidanceData;
        this.currentRecord = currentRecord;
        this.backDate = backDate;
        this.newDate = newDate;
        this.siteKey = siteKey;
        this.guidFrequency = guidFrequency;
    }

    /**
     * Calculates the interpolated guidance delay record values
     * 
     * @return
     */
    public FFMPRecord calculateDelayedGuidance() {

        FFMPDataContainer qpeContainer = null;

        ArrayList<String> hucs = new ArrayList<String>();
		hucs.add("ALL");

		// if (qpeSource.isMosaic()) {
		// qpeContainer = generator.getFFMPDataContainer(qpeSource
		// .getSourceName(), hucs, backDate);
		// } else {
		qpeContainer = generator.getFFMPDataContainer(qpeSource.getSourceName()
				+ "-" + siteKey + "-" + siteKey, hucs, backDate);
		// }

		long expirationTime = qpeSource.getExpirationMinutes(siteKey) * 60 * 1000;
		// determine lag_time
        long lagTime = (currentRecord.getDataTime().getRefTime().getTime())
                + (long) (ffgSource.getDurationHour() * 60 * 1000);
        // Determine hour fraction.
        int fraction_Hr = (int) (((float) (currentRecord.getDataTime()
                .getRefTime().getTime() - (lagTime - guidFrequency))) / (float) guidFrequency);
        // Gets the ordered times for QPE
        ArrayList<Date> orderedTimes = qpeContainer
                .getOrderedTimes(currentRecord.getDataTime().getRefTime());

        // EQUATION: Guid = GuidOld + R i/d (GuidNew - GuidOld)
        for (Entry<Long, FFMPBasin> entry : currentRecord.getBasinsMap()
                .get("ALL").getBasins().entrySet()) {
            FFMPBasin currBasin = entry.getValue();
            FFMPGuidanceBasin oldBasin = (FFMPGuidanceBasin) previousGuidanceData
                    .get(entry.getKey());
            // comparison for increase / decrease
            if (oldBasin != null && currBasin != null) {

                float fraction = 0.0f;
                float delta = currBasin.getValue()
                        - oldBasin
                                .getValue(backDate, ffgSource.getSourceName());

                if (delta > 0.0) {
                    // increasing vals
                    fraction = fraction_Hr;
                } else if (delta < 0.0) {
                    // decreasing vals, use num/denom from qpe
                    // this is essentially a ratio of the first accumulation
                    // step increment
                    // to the total amount over this time window.
                    FFMPBasin qpeBasin = qpeContainer.getBasinData("ALL").get(
                            entry.getKey());

                    if (qpeBasin != null) {
                        float intervalAccum = qpeBasin.getAccumValue(backDate,
                                currentRecord.getDataTime().getRefTime(),
                                expirationTime, qpeSource.isRate());
                        // grab first time after initial for step
                        float stepAccum = qpeBasin.getAccumValue(
                                orderedTimes.get(1), orderedTimes.get(0),
                                expirationTime, qpeSource.isRate());
                        fraction = stepAccum / intervalAccum;
                    }

                } else {
                    // the same
                    fraction = 0.0f;
                }

                float val = currBasin.getValue() + (fraction * delta);
                currBasin.setValue(newDate, val);
            }
        }

        return currentRecord;
    }
}
