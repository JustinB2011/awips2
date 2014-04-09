package com.raytheon.viz.grid;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.raytheon.uf.common.comm.CommunicationException;
import com.raytheon.uf.common.dataplugin.grid.GridConstants;
import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataplugin.level.LevelFactory;
import com.raytheon.uf.common.dataplugin.level.mapping.LevelMappingFactory;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.DisplayType;
import com.raytheon.uf.viz.core.rsc.ResourceProperties;
import com.raytheon.uf.viz.core.rsc.ResourceType;
import com.raytheon.uf.viz.datacube.DataCubeContainer;
import com.raytheon.uf.viz.productbrowser.AbstractRequestableProductBrowserDataDefinition;
import com.raytheon.uf.viz.productbrowser.ProductBrowserLabel;
import com.raytheon.uf.viz.productbrowser.ProductBrowserPreference;
import com.raytheon.uf.viz.productbrowser.ProductBrowserPreference.PreferenceType;
import com.raytheon.viz.grid.inv.GridInventory;
import com.raytheon.viz.grid.rsc.GridLoadProperties;
import com.raytheon.viz.grid.rsc.GridResourceData;

/**
 * Product browser implementation for grid
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 21, 2010           bsteffen    Initial creation
 * May 26, 2010           mnash       Used ProductBrowserLabel implementation
 *                                    instead of requery
 * May 02, 2013  1949     bsteffen    Switch Product Browser from uengine to
 *                                    DbQueryRequest.
 * Sep 19, 2013  2391     mpduff      refactored some methods to common class.
 * Jan 23, 2014  2711     bsteffen    Get all levels from LevelFactory.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class GridProductBrowserDataDefinition extends
        AbstractRequestableProductBrowserDataDefinition<GridResourceData> {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GridProductBrowserDataDefinition.class);

    private final String SHOW_DERIVED_PARAMS = "Show Derived Parameters";

    /**
     * Constructor.
     */
    public GridProductBrowserDataDefinition() {
        productName = GridInventory.PLUGIN_NAME;
        displayName = "Grid";
        order = new String[] { GridInventory.MODEL_NAME_QUERY,
                GridInventory.PARAMETER_QUERY,
                GridInventory.MASTER_LEVEL_QUERY, GridInventory.LEVEL_ID_QUERY };
        order = getOrder();
        loadProperties = new GridLoadProperties();
        loadProperties.setResourceType(getResourceType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition
     * #getResourceData()
     */
    @Override
    public GridResourceData getResourceData() {
        return new GridResourceData();
    }

    @Override
    public void constructResource(String[] selection, ResourceType type) {
        GridInventory inventory = getInventory();
        if (inventory == null) {
            super.constructResource(selection, type);
            return;
        }
        if (type != null) {
            loadProperties.setResourceType(type);
        }
        HashMap<String, RequestConstraint> parameters = getProductParameters(
                selection, order);
        List<String> ensembles = null;
        try {
            ensembles = inventory.getEnsembles(parameters);
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        if (ensembles != null && ensembles.size() > 1) {
            Collections.sort(ensembles);
            List<ResourcePair> pairs = new ArrayList<ResourcePair>();
            for (String ensemble : ensembles) {
                ResourcePair pair = new ResourcePair();
                resourceData = getResourceData();
                HashMap<String, RequestConstraint> newParameters = new HashMap<String, RequestConstraint>(
                        parameters);
                newParameters.put(GridConstants.ENSEMBLE_ID,
                        new RequestConstraint(ensemble));
                resourceData.setMetadataMap(newParameters);
                pair.setResourceData(resourceData);
                pair.setLoadProperties(loadProperties);
                pair.setProperties(new ResourceProperties());
                pairs.add(pair);
            }
            constructResource(pairs);
        } else {
            resourceData = getResourceData();
            resourceData.setMetadataMap(parameters);
            constructResource();
        }
    }

    @Override
    protected String[] queryData(String param,
            Map<String, RequestConstraint> queryList) {
        try {
            if (getInventory() == null) {
                return super.queryData(param, queryList);
            } else {
                Collection<String> sources = null;
                Collection<String> params = null;
                Collection<Level> levels = null;
                BlockingQueue<String> returnQueue = new LinkedBlockingQueue<String>();
                for (Entry<String, RequestConstraint> queryParam : queryList
                        .entrySet()) {
                    String key = queryParam.getKey();
                    String value = queryParam.getValue().getConstraintValue();
                    if (key.equals(GridInventory.MODEL_NAME_QUERY)) {
                        sources = Arrays.asList(value);
                    } else if (key.equals(GridInventory.PARAMETER_QUERY)) {
                        params = Arrays.asList(value);
                    } else if (key.equals(GridInventory.MASTER_LEVEL_QUERY)) {
                        if (levels == null) {
                            levels = new ArrayList<Level>(LevelFactory
                                    .getInstance().getAllLevels());
                        }
                        Iterator<Level> iter = levels.iterator();
                        while (iter.hasNext()) {
                            if (!iter.next().getMasterLevel().getName()
                                    .equals(value)) {
                                iter.remove();
                            }
                        }

                    } else if (key.equals(GridInventory.LEVEL_ONE_QUERY)) {
                        double doubleValue = Double.parseDouble(value);
                        if (levels == null) {
                            levels = new ArrayList<Level>(
                                    LevelMappingFactory
                                            .getInstance(
                                                    LevelMappingFactory.VOLUMEBROWSER_LEVEL_MAPPING_FILE)
                                            .getAllLevels());
                        }
                        Iterator<Level> iter = levels.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().getLevelonevalue() != doubleValue) {
                                iter.remove();
                            }
                        }
                    } else if (key.equals(GridInventory.LEVEL_TWO_QUERY)) {
                        double doubleValue = Double.parseDouble(value);
                        if (levels == null) {
                            levels = new ArrayList<Level>(
                                    LevelMappingFactory
                                            .getInstance(
                                                    LevelMappingFactory.VOLUMEBROWSER_LEVEL_MAPPING_FILE)
                                            .getAllLevels());
                        }
                        Iterator<Level> iter = levels.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().getLeveltwovalue() != doubleValue) {
                                iter.remove();
                            }
                        }
                    } else if (key.equals(GridInventory.LEVEL_ID_QUERY)) {
                        levels = Arrays.asList(LevelFactory.getInstance()
                                .getLevel(value));
                    }
                }

                if (param.equals(GridInventory.MODEL_NAME_QUERY)) {
                    try {
                        getInventory().checkSources(sources, params, levels,
                                returnQueue);
                    } catch (InterruptedException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                    return returnQueue.toArray(new String[0]);
                } else if (param.equals(GridInventory.PARAMETER_QUERY)) {
                    try {
                        getInventory().checkParameters(sources, params, levels,
                                false, returnQueue);
                    } catch (InterruptedException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                    return returnQueue.toArray(new String[0]);
                } else if (param.equals(GridInventory.MASTER_LEVEL_QUERY)) {
                    try {
                        getInventory().checkLevels(sources, params, levels,
                                returnQueue);
                    } catch (InterruptedException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                    Set<String> masterlevels = new HashSet<String>();
                    LevelFactory lf = LevelFactory.getInstance();
                    for (String levelid : returnQueue) {
                        Level level = lf.getLevel(levelid);
                        masterlevels.add(level.getMasterLevel().getName());
                    }
                    return masterlevels.toArray(new String[0]);
                } else if (param.equals(GridInventory.LEVEL_ID_QUERY)) {
                    try {
                        getInventory().checkLevels(sources, params, levels,
                                returnQueue);
                    } catch (InterruptedException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                    return returnQueue.toArray(new String[0]);
                }
            }
        } catch (CommunicationException e) {
            statusHandler.handle(Priority.ERROR, "Unable to query data for "
                    + productName, e);
        }
        return new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.productbrowser.
     * AbstractRequestableProductBrowserDataDefinition
     * #formatData(java.lang.String, java.lang.String[])
     */
    @Override
    public List<ProductBrowserLabel> formatData(String param,
            String[] parameters) {
        try {
            List<ProductBrowserLabel> labels = GridProductBrowserDataFormatter
                    .formatGridData(param, parameters);
            if (labels != null && !labels.isEmpty()) {
                return labels;
            }
        } catch (CommunicationException e) {
            statusHandler.handle(Priority.ERROR, "Unable to format data for "
                    + productName, e);
        }

        return super.formatData(param, parameters);
    }

    @Override
    public HashMap<String, RequestConstraint> getProductParameters(
            String[] selection, String[] order) {
        if (getInventory() == null) {
            return super.getProductParameters(selection, order);
        }
        HashMap<String, RequestConstraint> queryList = super
                .getProductParameters(selection, order);
        if (queryList.containsKey(GridInventory.LEVEL_ID_QUERY)) {
            RequestConstraint levelRC = queryList
                    .remove(GridInventory.LEVEL_ID_QUERY);
            // Convert Level id to level one and level two values.
            try {
                Level level = LevelFactory.getInstance().getLevel(
                        levelRC.getConstraintValue());
                queryList
                        .put(GridInventory.LEVEL_ONE_QUERY,
                                new RequestConstraint(level
                                        .getLevelOneValueAsString()));
                queryList
                        .put(GridInventory.LEVEL_TWO_QUERY,
                                new RequestConstraint(level
                                        .getLevelTwoValueAsString()));
                queryList
                        .put(GridInventory.MASTER_LEVEL_QUERY,
                                new RequestConstraint(level.getMasterLevel()
                                        .getName()));
            } catch (CommunicationException e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to get product parameters for " + productName,
                        e);
            }
        }
        return queryList;
    }

    private GridInventory getInventory() {
        if ((Boolean) getPreference(SHOW_DERIVED_PARAMS).getValue()) {
            return (GridInventory) DataCubeContainer
                    .getInventory(GridConstants.GRID);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition
     * #getDisplayTypes()
     */
    @Override
    public Map<ResourceType, List<DisplayType>> getDisplayTypes() {
        Map<ResourceType, List<DisplayType>> type = new HashMap<ResourceType, List<DisplayType>>();
        List<DisplayType> types = new ArrayList<DisplayType>();
        types.add(DisplayType.CONTOUR);
        types.add(DisplayType.IMAGE);
        type.put(ResourceType.PLAN_VIEW, types);
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.productbrowser.xml.IProductBrowserPreferences#
     * configurePreferences()
     */
    @Override
    public List<ProductBrowserPreference> configurePreferences() {
        List<ProductBrowserPreference> widgets = super.configurePreferences();
        ProductBrowserPreference derivedParameterPref = new ProductBrowserPreference();
        derivedParameterPref.setLabel(SHOW_DERIVED_PARAMS);
        derivedParameterPref.setPreferenceType(PreferenceType.BOOLEAN);
        derivedParameterPref
                .setTooltip("Show derived parameters in the Product Browser");
        derivedParameterPref.setValue(true);
        widgets.add(derivedParameterPref);
        return widgets;
    }
}
