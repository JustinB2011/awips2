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
package com.raytheon.viz.volumebrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.grib.util.GribModelLookup;
import com.raytheon.uf.common.dataplugin.grib.util.GridModel;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.drawables.AbstractRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.procedures.Bundle;
import com.raytheon.uf.viz.core.procedures.IAlterBundleContributor;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.d2d.nsharp.rsc.D2DNSharpResourceData;
import com.raytheon.uf.viz.d2d.nsharp.rsc.GribNSharpResourceData;
import com.raytheon.uf.viz.xy.crosssection.rsc.CrossSectionResourceData;
import com.raytheon.uf.viz.xy.timeseries.rsc.TimeSeriesResourceData;
import com.raytheon.uf.viz.xy.varheight.rsc.VarHeightResourceData;
import com.raytheon.viz.core.rsc.BlendedResourceData;
import com.raytheon.viz.volumebrowser.xml.VbSource;
import com.raytheon.viz.volumebrowser.xml.VbSourceList;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 4, 2010            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class GridAlterBundleContributor implements IAlterBundleContributor {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GridAlterBundleContributor.class);

    private static final String GRID_KEY = "Grid";

    private static Map<String, String> modelTitleToNameMap = null;

    private Map<String, String> getModelTitleToNameMap() {

        if (modelTitleToNameMap == null) {
            modelTitleToNameMap = new HashMap<String, String>();
            try {
                for (VbSource source : VbSourceList.getInstance().getEntries()) {
                    if (source.getName() != null) {
                        modelTitleToNameMap.put(source.getName(),
                                source.getKey());
                    } else {
                        GridModel model = GribModelLookup.getInstance()
                                .getModelByName(source.getKey());
                        if (model == null) {
                            modelTitleToNameMap.put(source.getKey(),
                                    source.getKey());
                        } else {
                            modelTitleToNameMap.put(model.getTitle(),
                                    source.getKey());
                        }
                    }
                }
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Cannot load grid sources.", e);
            }
        }
        return modelTitleToNameMap;
    }

    private void alterResourceList(ResourceList list, String selectedString) {
        for (ResourcePair rp : list) {
            AbstractResourceData rData = rp.getResourceData();
            if (rData instanceof BlendedResourceData) {
                alterResourceList(
                        ((BlendedResourceData) rData).getResourceList(),
                        selectedString);
            } else if (rData instanceof AbstractRequestableResourceData) {
                alterResource((AbstractRequestableResourceData) rData,
                        selectedString);
            }
        }
    }

    private void alterResource(AbstractRequestableResourceData data,
            String selectedString) {
        Map<String, RequestConstraint> reqMap = data.getMetadataMap();
        if (selectedString != null) {
            reqMap.put("modelInfo.modelName", new RequestConstraint(
                    selectedString));

            // next, need to modify for other displays (not plan view)
            if (data instanceof VarHeightResourceData) {
                GribModelLookup lookup = GribModelLookup.getInstance();
                ((VarHeightResourceData) data).setSource(lookup.getModelByName(
                        selectedString).getTitle());
            } else if (data instanceof TimeSeriesResourceData) {
                GribModelLookup lookup = GribModelLookup.getInstance();
                ((TimeSeriesResourceData) data).setSource(lookup
                        .getModelByName(selectedString).getTitle());
            } else if (data instanceof GribNSharpResourceData) {
                ((D2DNSharpResourceData) data).setSoundingType(selectedString);
            } else if (data instanceof CrossSectionResourceData) {
                GribModelLookup lookup = GribModelLookup.getInstance();
                ((CrossSectionResourceData) data).setSource(lookup
                        .getModelByName(selectedString).getTitle());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.procedures.IAlterBundleContributor#getAlterables
     * ()
     */
    @Override
    public Map<String, String[]> getAlterables() {
        Map<String, String[]> alterables = new HashMap<String, String[]>();
        List<String> models = new ArrayList<String>(getModelTitleToNameMap()
                .keySet());
        Collections.sort(models);
        alterables.put(GRID_KEY, models.toArray(new String[models.size()]));
        return alterables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.procedures.IAlterBundleContributor#alterBundle
     * (com.raytheon.uf.viz.core.procedures.Bundle, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void alterBundle(Bundle bundleToAlter, String alterKey,
            String alterValue) {
        if (GRID_KEY.equals(alterKey)) {
            for (AbstractRenderableDisplay display : bundleToAlter
                    .getDisplays()) {
                alterResourceList(display.getDescriptor().getResourceList(),
                        getModelTitleToNameMap().get(alterValue));
            }
        }
    }
}
