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
package com.raytheon.viz.hydro.gagedisplay;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;

/**
 * Action for clicking on the contextual menu for color legend.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date			Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * Jul 2, 2008	1194     	mpduff	Initial creation
 * 
 * </pre>
 *
 * @author mpduff
 * @version 1.0	
 */

public class GageColorLegendAction extends AbstractRightClickAction {
  
    /** 
     * Returns the text for the action.
     * 
     * @see org.eclipse.jface.action.Action#getText()
     */
    @Override
    public String getText() {
        // TODO Auto-generated method stub
        final String displayText = "Display Gage Color Legend";
        return displayText;
    }

    /** 
     * Launches the Gage Color Legend Dialog.
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        super.run();
        // Display the swt dialog with the legend in it
        GageLegend gl = new GageLegend(new Shell());
        gl.open();
        
    }

}
