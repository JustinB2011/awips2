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
package com.raytheon.uf.viz.remote.graphics.events;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.viz.remote.graphics.AbstractRemoteGraphicsEvent;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 8, 2012            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */
@DynamicSerialize
public class BeginFrameEvent extends AbstractRemoteGraphicsEvent implements
        IRenderEvent {

    @DynamicSerializeElement
    private double extentFactor;

    @DynamicSerializeElement
    private double[] extentCenter;

    /**
     * @return the extentFactor
     */
    public double getExtentFactor() {
        return extentFactor;
    }

    /**
     * @param extentFactor
     *            the extentFactor to set
     */
    public void setExtentFactor(double extentFactor) {
        this.extentFactor = extentFactor;
    }

    /**
     * @return the extentCenter
     */
    public double[] getExtentCenter() {
        return extentCenter;
    }

    /**
     * @param extentCenter
     *            the extentCenter to set
     */
    public void setExtentCenter(double[] extentCenter) {
        this.extentCenter = extentCenter;
    }

}
