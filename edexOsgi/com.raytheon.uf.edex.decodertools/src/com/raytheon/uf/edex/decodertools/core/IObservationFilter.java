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
package com.raytheon.uf.edex.decodertools.core;

import com.raytheon.uf.common.dataplugin.PluginDataObject;

/**
 * 
 * 
 * 
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 23, 2009            jkorman     Initial creation
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */
public interface IObservationFilter {

    public static final String INCLUDE_TYPE = "INCLUDE";
    
    public static final String EXCLUDE_TYPE = "EXCLUDE";
    
    /**
     * 
     * @param element
     */
    void addFilterElement(IObsFilterElement element);

    /**
     * Executes this filter against the supplied report data. The supplied
     * report is returned if it passes all filter elements. A null report
     * reference is returned if the report fails.
     * @param report
     * @return
     */
    PluginDataObject [] filter(PluginDataObject [] report);
    
}
