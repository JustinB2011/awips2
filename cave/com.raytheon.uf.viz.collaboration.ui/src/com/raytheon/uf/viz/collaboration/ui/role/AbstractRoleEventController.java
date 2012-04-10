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
package com.raytheon.uf.viz.collaboration.ui.role;

import com.raytheon.uf.viz.collaboration.comm.identity.ISharedDisplaySession;
import com.raytheon.uf.viz.collaboration.ui.telestrator.CollaborationPathDrawingTool;
import com.raytheon.uf.viz.collaboration.ui.telestrator.CollaborationPathToolbar;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.drawing.PathToolbar;
import com.raytheon.uf.viz.drawing.tools.PathDrawingTool;

/**
 * Abstract role event controller that shares fields and methods that are common
 * to other event controllers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 26, 2012            njensen     Initial creation
 * 
 * </pre>
 * 
 * @author njensen
 * @version 1.0
 */

public abstract class AbstractRoleEventController implements
        IRoleEventController {

    protected ISharedDisplaySession session;

    protected AbstractRoleEventController(ISharedDisplaySession session) {
        this.session = session;
    }

    @Override
    public void startup() {
        session.registerEventHandler(this);
    }

    @Override
    public void shutdown() {
        session.unRegisterEventHandler(this);
    }

    protected void activateTelestrator() {
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                // activate the drawing tool by default for the session leader
                PathDrawingTool tool = new CollaborationPathDrawingTool();
                tool.activate();

                // open the path drawing toolbar
                PathToolbar toolbar = CollaborationPathToolbar.getToolbar();
                toolbar.open();
            }
        });
    }

    protected void deactivateTelestrator() {
        // TODO this must be handled better
        PathToolbar.getToolbar().close();
    }

}
