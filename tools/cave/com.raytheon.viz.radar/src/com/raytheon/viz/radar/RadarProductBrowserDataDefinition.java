package com.raytheon.viz.radar;

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

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.dataplugin.radar.util.RadarInfoDict;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.rsc.DisplayType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceType;
import com.raytheon.uf.viz.core.rsc.capabilities.ImagingCapability;
import com.raytheon.uf.viz.productbrowser.AbstractRequestableProductBrowserDataDefinition;
import com.raytheon.uf.viz.productbrowser.ProductBrowserLabel;
import com.raytheon.uf.viz.productbrowser.ProductBrowserPreference;
import com.raytheon.viz.radar.rsc.RadarResourceData;
import com.raytheon.viz.radar.ui.xy.RadarGraphDisplay;
import com.raytheon.viz.radar.ui.xy.RadarXYDisplay;
import com.raytheon.viz.ui.editor.EditorInput;

/**
 * Product browser implementation for radar
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2010            mnash     Initial creation
 * Jun 30, 2010           mnash     Used ProductBrowserLabel instead of String[]
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RadarProductBrowserDataDefinition extends
        AbstractRequestableProductBrowserDataDefinition<RadarResourceData> {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RadarProductBrowserDataDefinition.class);

    private static RadarInfoDict infoDict = null;

    public static LinkedHashMap<String, ResourceType> prodTypes = null;

    public static LinkedHashMap<ResourceType, LinkedHashMap<String, DisplayType>> subTypes = null;

    /**
     * Constructor - must set productName, displayName, and order for data to
     * show up in product browser
     */
    public RadarProductBrowserDataDefinition() {
        if (infoDict == null) {
            loadInfoDict();
        }
        productName = "radar";
        displayName = "Radar";
        order = new String[] { "pluginName", "icao", "productCode",
                "primaryElevationAngle" };
        order = getOrder();
        loadProperties = new LoadProperties();
        loadProperties.getCapabilities().addCapability(ImagingCapability.class);
        loadProperties.setResourceType(getResourceType());
    }

    // load the info dictionary
    private void loadInfoDict() {
        File radarInfo = PathManagerFactory.getPathManager().getStaticFile(
                "radarInfo.txt");
        if (radarInfo != null) {
            infoDict = RadarInfoDict.getInstance(radarInfo.getParent());
        } else {
            infoDict = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#
     * formatData()
     */
    @Override
    public List<ProductBrowserLabel> formatData(String param,
            String[] parameters) {
        List<ProductBrowserLabel> labels;
        try {
            labels = new ArrayList<ProductBrowserLabel>();
            String parm = "";
            if ("productCode".equals(param)) {
                for (int i = 0; i < parameters.length; i++) {
                    int tmp = Integer.parseInt(parameters[i]);
                    if (infoDict.getInfo(tmp).getResolution() != 0) {
                        parm = (int) (Math.log(infoDict.getInfo(tmp)
                                .getNumLevels()) / Math.log(2))
                                + "-bit"
                                + " "
                                + +((double) infoDict.getInfo(tmp)
                                        .getResolution() / 1000) + " km ";
                    }
                    labels.add(new ProductBrowserLabel(infoDict.getInfo(tmp)
                            .getName() + "::" + parm, parameters[i]));
                }
                Collections.sort(labels);
                for (int i = 0; i < parameters.length; i++) {
                    String[] tmp = labels.get(i).getName().split("::");
                    if (tmp.length > 1) {
                        labels.get(i).setName(tmp[1] + tmp[0]);
                    } else {
                        labels.get(i).setName(tmp[0]);
                    }
                }
            } else if ("primaryElevationAngle".equals(param)) {
                for (int i = 0; i < parameters.length; i++) {
                    DecimalFormat format = new DecimalFormat("#.##");
                    String tmp = format.format(Double
                            .parseDouble(parameters[i]));
                    parameters[i] = tmp;
                    labels.add(new ProductBrowserLabel(parameters[i] + "°",
                            parameters[i]));
                }
            } else {
                for (int i = 0; i < parameters.length; i++) {
                    labels.add(new ProductBrowserLabel(parameters[i], null));
                }
            }
        } catch (Exception e) {
            labels = super.formatData(param, parameters);
        }
        return labels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition
     * #getResourceData()
     */
    @Override
    public RadarResourceData getResourceData() {
        return new RadarResourceData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition
     * #getEditor()
     */
    @Override
    public void getEditor() {
        int prodCode = Integer.parseInt((resourceData).getMetadataMap()
                .get("productCode").getConstraintValue());
        String format = infoDict.getInfo(prodCode).getFormat();
        if ("XY".equals(format)) {
            String editor = "com.raytheon.viz.radar.ui.xy.RadarXYEditor";
            EditorInput cont = new EditorInput(
                    (IRenderableDisplay) new RadarXYDisplay());
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().openEditor(cont, editor, true);
            } catch (PartInitException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Unable to open editor : " + editor, e);
            }
        } else if ("Graph".equals(format)) {
            String editor = "com.raytheon.viz.radar.ui.xy.RadarGraphEditor";
            EditorInput cont = new EditorInput(
                    (IRenderableDisplay) new RadarGraphDisplay());
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().openEditor(cont, editor, true);
            } catch (PartInitException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Unable to open editor : " + editor, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.productbrowser.xml.IProductBrowserPreferences#
     * configurePreferences()
     */
    @Override
    public List<ProductBrowserPreference> configurePreferences() {
        return super.configurePreferences();
    }
}
