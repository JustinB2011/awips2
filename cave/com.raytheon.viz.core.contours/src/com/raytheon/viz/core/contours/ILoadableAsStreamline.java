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
package com.raytheon.viz.core.contours;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;

/**
 * ILoadableAsStreamline
 * 
 * Interface that specifies a streamline resource can be created from an
 * existing resource
 * 
 * 
 * <pre>
 * 
 *    SOFTWARE HISTORY
 *   
 *    Date         Ticket#     Engineer    Description
 *    ------------ ----------  ----------- --------------------------
 *    Aug 04, 2008             brockwoo    Initial Creation.
 * 
 * </pre>
 * 
 * @author brockwoo
 * @version 1
 */
public interface ILoadableAsStreamline {

    /**
     * Return the corresponding imagery resource
     * 
     * @return an imagery resource
     * @throws VizException
     */
    public abstract AbstractVizResource<?, ?> getStreamlineResource()
            throws VizException;

    public abstract boolean isStreamlineVector();

}
