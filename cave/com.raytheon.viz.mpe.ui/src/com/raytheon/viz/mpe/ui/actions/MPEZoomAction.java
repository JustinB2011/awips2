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
package com.raytheon.viz.mpe.ui.actions;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;
import com.raytheon.viz.ui.tools.AbstractModalTool;
import com.raytheon.viz.ui.tools.ModalToolManager;

/**
 * Activate zoom box support in the mpe editor.
 * 
 * <pre>
 * 
 *   SOFTWARE HISTORY
 *  
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 11Apr2011     8738        jpiatt      Initial Creation.
 * 
 * </pre>
 * 
 * @author jpiatt
 * @version 1
 */
public class MPEZoomAction extends AbstractHandler {

	private IExtent unZoomedExtent;
	
	private IDisplayPaneContainer container = null;
	
	private IDisplayPane pane = null;
	
	private IRenderableDisplay display = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final String ZOOM_ID = "com.raytheon.viz.ui.tools.nav.ZoomTool";
		final String PAN_ID = "com.raytheon.viz.ui.tools.nav.PanTool";

		AbstractVizPerspectiveManager perspMgr = VizPerspectiveListener
		.getCurrentPerspectiveManager();
		if (perspMgr != null) {
			ModalToolManager mgr = perspMgr.getToolManager();

			Collection<AbstractModalTool> toolList = mgr
			.getSelectedModalTools();

			try {

				for (AbstractModalTool abstractModalTool : toolList) {

					if ((PAN_ID).equals(abstractModalTool.getClass().getName())) {
						
						if (display == null) {
							container = EditorUtil.getActiveVizContainer();
							if (container != null) {
								pane = container.getActiveDisplayPane();
								if (pane != null) {
									unZoomedExtent = pane.getRenderableDisplay().getExtent();
								}

							}
							
						}

						mgr.activateToolSet(ZOOM_ID);
						break;

					} else {
					
						pane.getRenderableDisplay().setExtent(unZoomedExtent);
						pane.getDescriptor().getRenderableDisplay().refresh();
						break;
					}
				}
			} catch (VizException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

}
