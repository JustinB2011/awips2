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
package com.raytheon.viz.awipstools.ui.action;

import com.raytheon.uf.viz.core.rsc.tools.AwipsToolsResourceData;
import com.raytheon.uf.viz.core.rsc.tools.action.AbstractMapToolAction;
import com.raytheon.viz.awipstools.ui.layer.InteractiveBaselinesLayer;

/**
 * Handles the Baseline Tools Action.
 * 
 * <pre>
 * 
 *  SOFTWARE HISTORY
 * 
 *  Date         Ticket#     Engineer    Description
 *  ------------ ----------  ----------- --------------------------
 *  Sep192007    #447        ebabin      Initial Creation.
 *  20Dec2007    #645        ebabin      Updated to fix sampling.  
 *  12May2008    #1031       ebabin      Fix for baselines editing.
 *  14Oct2009    #683        bsteffen    Fix for grabbing points.
 *  10-21-09     #1711       bsteffen    Refactor to common MovableTool model
 * 
 * </pre>
 * 
 * @author ebabin
 * @version 1
 */
public class BaselinesToolAction extends
        AbstractMapToolAction<InteractiveBaselinesLayer> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.awipstools.ui.action.MapToolAction#getResourceData()
     */
    @Override
    protected AwipsToolsResourceData<InteractiveBaselinesLayer> getResourceData() {
        return new AwipsToolsResourceData<InteractiveBaselinesLayer>(
                "Interactive Baselines", InteractiveBaselinesLayer.class);

    }
}
