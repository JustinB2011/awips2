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

/**
 * FFTIRatioDiff
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 23, 2011            dhladky     Initial creation
 * 
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */

public class FFTIRatioDiff {

    private ArrayList<Float> qpes;

    private ArrayList<Float> guids;

    private Double gap = 0.0;

    public FFTIRatioDiff() {
        
    }
    
    public FFTIRatioDiff(ArrayList<Float> qpes, ArrayList<Float> guids,
            Double gap) {
        setQpes(qpes);
        setGuids(guids);
        setGap(gap);
    }

    public ArrayList<Float> getQpes() {
        return qpes;
    }

    public void setQpes(ArrayList<Float> qpes) {
        this.qpes = qpes;
    }

    public ArrayList<Float> getGuids() {
        return guids;
    }

    public void setGuids(ArrayList<Float> guids) {
        this.guids = guids;
    }

    public Double getGap() {
        return gap;
    }

    public void setGap(Double gap) {
        this.gap = gap;
    }

}
