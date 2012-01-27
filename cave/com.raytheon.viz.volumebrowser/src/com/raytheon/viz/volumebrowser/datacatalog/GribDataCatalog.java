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
package com.raytheon.viz.volumebrowser.datacatalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.operation.MathTransform;

import com.raytheon.uf.common.dataplugin.grib.spatial.projections.GridCoverage;
import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.level.LevelMappingFactory;
import com.raytheon.uf.viz.core.level.LevelUtilities;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.DisplayType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceType;
import com.raytheon.uf.viz.d2d.nsharp.rsc.D2DNSharpResourceData;
import com.raytheon.uf.viz.d2d.nsharp.rsc.GribNSharpResourceData;
import com.raytheon.viz.awipstools.ToolsDataManager;
import com.raytheon.viz.grid.inv.GridInventory;
import com.raytheon.viz.grid.rsc.GribSkewTLoadProperties;
import com.raytheon.viz.grid.rsc.GridNameGenerator;
import com.raytheon.viz.grid.rsc.GridResourceData;
import com.raytheon.viz.grid.util.CoverageUtils;
import com.raytheon.viz.volumebrowser.util.FieldDisplayTypesFactory;
import com.raytheon.viz.volumebrowser.vbui.MenuItemManager;
import com.raytheon.viz.volumebrowser.vbui.SelectedData;
import com.raytheon.viz.volumebrowser.vbui.VBMenuBarItemsMgr.SpaceTimeMenu;
import com.raytheon.viz.volumebrowser.vbui.VBMenuBarItemsMgr.ViewMenu;
import com.raytheon.viz.volumebrowser.vbui.VolumeBrowserAction;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Implements the IDataCatalog interface for grib data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 27, 2009 #2161      lvenable     Initial creation
 * 10-21-09     #1711      bsteffen    Updated Baseline and Points to use new ToolsDataManager
 * 11/17/2009   #3120      rjpeter     Updated to use LevelMappingFactory.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class GribDataCatalog extends AbstractInventoryDataCatalog {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GribDataCatalog.class);

    /**
     * Create the product entry in the grid data catalog.
     * 
     * Uses the gribModelDataMap to determine the integer representations of the
     * selData.
     * 
     * @param selData
     *            The selected data.
     */
    @Override
    public IDataCatalogEntry getCatalogEntry(SelectedData selData) {
        if (!isValidSelection(selData)) {
            return null;
        }
        GribDataCatalogEntry catalogEntry = new GribDataCatalogEntry(selData);

        catalogEntry.modelName = selData.getSourcesKey();
        catalogEntry.paramAbbreviation = selData.getFieldsKey();
        catalogEntry.selectedPlanesKey = selData.getPlanesKey();

        return catalogEntry;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.raytheon.viz.volumebrowser.datacatalog.AbstractDataCatalog#
     * addProductParameters
     * (com.raytheon.viz.volumebrowser.datacatalog.IDataCatalogEntry,
     * java.util.HashMap)
     */
    @Override
    public void addProductParameters(IDataCatalogEntry entry,
            HashMap<String, RequestConstraint> parameters) {

        GribDataCatalogEntry catalogEntry = (GribDataCatalogEntry) entry;

        ViewMenu viewSelection = catalogEntry.getDialogSettings()
                .getViewSelection();

        // sounding isn't restricted to a specific parameter or level
        switch (viewSelection) {
        case SOUNDING:
            RequestConstraint params = new RequestConstraint();
            params.setConstraintType(ConstraintType.IN);
            params.setConstraintValueList(soundingParams);
            parameters.put(GridInventory.MODEL_NAME_QUERY,
                    new RequestConstraint(catalogEntry.modelName));
            parameters.put(GridInventory.PARAMETER_QUERY, params);
            parameters.put(GridInventory.MASTER_LEVEL_QUERY,
                    new RequestConstraint("MB"));
            parameters
                    .put(GridInventory.LEVEL_TWO_QUERY, new RequestConstraint(
                            Level.getInvalidLevelValueAsString()));
            break;
        case VARVSHGT:
        case TIMEHEIGHT:
        case CROSSSECTION:
            parameters.put(GridInventory.MODEL_NAME_QUERY,
                    new RequestConstraint(catalogEntry.modelName));
            parameters.put(GridInventory.PARAMETER_QUERY,
                    new RequestConstraint(catalogEntry.paramAbbreviation));
            parameters.put(
                    GridInventory.MASTER_LEVEL_QUERY,
                    new RequestConstraint(FieldDisplayTypesFactory
                            .getInstance().getVolumePlaneType(
                                    catalogEntry.paramAbbreviation)));
            parameters
                    .put(GridInventory.LEVEL_TWO_QUERY, new RequestConstraint(
                            Level.getInvalidLevelValueAsString()));
            break;
        case PLANVIEW:
        case TIMESERIES:
        default:
            parameters.put(GridInventory.MODEL_NAME_QUERY,
                    new RequestConstraint(catalogEntry.modelName));
            parameters.put(GridInventory.PARAMETER_QUERY,
                    new RequestConstraint(catalogEntry.paramAbbreviation));
            if (catalogEntry.getDialogSettings().getSpaceTimeSelection() == SpaceTimeMenu.SPACE) {
                parameters.put(
                        GridInventory.MASTER_LEVEL_QUERY,
                        new RequestConstraint(catalogEntry.selectedPlanesKey
                                .replace("spatial-", "")));
                parameters.put(
                        GridInventory.LEVEL_TWO_QUERY,
                        new RequestConstraint(Level
                                .getInvalidLevelValueAsString()));
            } else {
                // Get all possible levels for the selected levels
                LevelMappingFactory lmf = LevelMappingFactory.getInstance();
                List<Level> selectedLevels = new ArrayList<Level>(lmf
                        .getLevelMappingForKey(catalogEntry.selectedPlanesKey)
                        .getLevels());

                RequestConstraint masterRC = new RequestConstraint(null,
                        ConstraintType.IN);
                RequestConstraint oneRC = new RequestConstraint(null,
                        ConstraintType.IN);
                RequestConstraint twoRC = new RequestConstraint(null,
                        ConstraintType.IN);
                for (Level level : selectedLevels) {
                    masterRC.addToConstraintValueList(level.getMasterLevel()
                            .getName());
                    oneRC.addToConstraintValueList(Double.toString(level
                            .getLevelonevalue()));
                    twoRC.addToConstraintValueList(Double.toString(level
                            .getLeveltwovalue()));
                }
                parameters.put(GridInventory.MASTER_LEVEL_QUERY, masterRC);
                parameters.put(GridInventory.LEVEL_ONE_QUERY, oneRC);
                parameters.put(GridInventory.LEVEL_TWO_QUERY, twoRC);
                // }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.volumebrowser.datacatalog.AbstractDataCatalog#getDisplayUnit
     * (com.raytheon.viz.volumebrowser.datacatalog.IDataCatalogEntry,
     * com.raytheon.uf.viz.core.rsc.DisplayType)
     */
    @Override
    protected String getDisplayUnit(IDataCatalogEntry catalogEntry,
            DisplayType displayType) {

        String displayUnit = super.getDisplayUnit(catalogEntry, displayType);

        // if we fail to get a unit from the style rules use the parameter unit
        if (displayUnit == null) {

            GribDataCatalogEntry gribDataCatalogEntry = ((GribDataCatalogEntry) catalogEntry);
            return getInventory().getParameterUnit(
                    gribDataCatalogEntry.modelName,
                    gribDataCatalogEntry.paramAbbreviation);
        }

        return displayUnit;

    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.raytheon.viz.volumebrowser.datacatalog.AbstractDataCatalog#
     * getLoadProperties
     * (com.raytheon.viz.volumebrowser.datacatalog.IDataCatalogEntry,
     * com.raytheon.uf.viz.core.rsc.ResourceType,
     * com.raytheon.uf.viz.core.rsc.DisplayType)
     */
    @Override
    protected LoadProperties getLoadProperties(
            IDataCatalogEntry dataCatalogEntry, ResourceType resourceType,
            DisplayType displayType) {

        if (resourceType == ResourceType.SOUNDING) {
            GribSkewTLoadProperties loadProperties = new GribSkewTLoadProperties();
            loadProperties.setPoint(dataCatalogEntry.getSelectedData()
                    .getPlanesKey().replace("Point", ""));
            return loadProperties;
        } else {
            return super.getLoadProperties(dataCatalogEntry, resourceType,
                    displayType);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.volumebrowser.datacatalog.AbstractDataCatalog#getPlugins
     * ()
     */
    @Override
    protected String[] getPlugins(ViewMenu setting) {
        return new String[] { "grib" };
    }

    @Override
    protected AbstractRequestableResourceData getResourceData(
            IDataCatalogEntry catalogEntry, ResourceType resourceType) {

        AbstractRequestableResourceData rscData = null;

        switch (resourceType) {
        case PLAN_VIEW:
            GridResourceData gRscData = new GridResourceData();

            if (catalogEntry.getDialogSettings().getSpaceTimeSelection() == SpaceTimeMenu.SPACE) {
                gRscData.setSpatial(true);
            } else {
                gRscData.setNameGenerator(new GridNameGenerator(catalogEntry
                        .getSelectedData().getPlanesText()));
            }
            rscData = gRscData;

            break;

        case SOUNDING:
            D2DNSharpResourceData tmpData = new GribNSharpResourceData(
                    catalogEntry.getSelectedData().getSourcesKey());
            tmpData.setCoordinate(getPointCoordinate(catalogEntry));
            String pointName = catalogEntry.getSelectedData().getSourcesText()
                    + "-" + catalogEntry.getSelectedData().getPlanesKey();
            tmpData.setPointName(pointName);
            rscData = tmpData;
            break;
        default:
            rscData = super.getResourceData(catalogEntry, resourceType);
        }

        return rscData;
    }

    @Override
    public Collection<ResourcePair> getResourcesToLoad(
            IDataCatalogEntry catalogEntry, ResourceType resourceType,
            DisplayType displayType) {
        Map<String, RequestConstraint> metadataMap = getProductParameters(catalogEntry);

        List<Integer> perts = null;
        try {
            perts = getGridInventory().getPerts(metadataMap);
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error occured during perturbation query.", e);
        }
        if (perts != null && perts.size() > 1) {
            Collection<ResourcePair> requests = new ArrayList<ResourcePair>();
            for (Integer pert : perts) {
                Collection<ResourcePair> origRequests = super
                        .getResourcesToLoad(catalogEntry, resourceType,
                                displayType);
                for (ResourcePair request : origRequests) {
                    ((AbstractRequestableResourceData) request
                            .getResourceData()).getMetadataMap().put(
                            "modelInfo.perturbationNumber",
                            new RequestConstraint(pert.toString()));
                    requests.add(request);
                }
            }
            return requests;
        } else {
            return super.getResourcesToLoad(catalogEntry, resourceType,
                    displayType);
        }
    }

    /**
     * @return
     */
    private GridInventory getGridInventory() {
        return (GridInventory) getInventory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.volumebrowser.datacatalog.AbstractInventoryDataCatalog
     * #get3DLevels()
     */
    @Override
    protected Collection<? extends Level> get3DLevels() {
        ArrayList<Level> all = new ArrayList<Level>();
        NavigableSet<Level> tilts = LevelUtilities
                .getOrderedSetOfStandardLevels("TILT");
        if (tilts != null) {
            all.addAll(tilts);
        }
        NavigableSet<Level> pres = LevelUtilities
                .getOrderedSetOfStandardLevels("MB");
        if (pres != null) {
            all.addAll(pres);
        }
        NavigableSet<Level> theta = LevelUtilities
                .getOrderedSetOfStandardLevels("K");
        if (theta != null) {
            all.addAll(theta);
        }
        return all;
    }

    @Override
    protected Collection<String> get3DSources(String[] planes) {
        List<String> lineLetters = new ArrayList<String>();
        List<Double> lats = new ArrayList<Double>();
        List<Double> lons = new ArrayList<Double>();
        List<String> pointLetters = new ArrayList<String>();

        ViewMenu viewSelection = VolumeBrowserAction.getVolumeBrowserDlg()
                .getDialogSettings().getViewSelection();
        if (viewSelection == ViewMenu.PLANVIEW) {
            ;//
        } else if (viewSelection == ViewMenu.TIMESERIES) {
            pointLetters.add(VolumeBrowserAction.getVolumeBrowserDlg()
                    .getDialogSettings().getPointsSelection().toString()
                    .replace("POINT_", ""));
        } else {
            if (planes == null) {
                return null;
            }
            for (String plane : planes) {
                if (plane.equals("LATS") || plane.equals("LONS")) {
                    return null;
                } else if (plane.startsWith("Lat")) {
                    lats.add(Double.parseDouble(plane.replace("Lat", "")));
                } else if (plane.startsWith("Lon")) {
                    lons.add(Double.parseDouble(plane.replace("Lon", "")));
                } else if (plane.startsWith("Line")) {
                    lineLetters.add(plane.replace("Line", ""));
                } else if (plane.startsWith("Point")) {
                    pointLetters.add(plane.replace("Point", ""));
                }
            }
        }
        if (pointLetters.isEmpty() && lineLetters.isEmpty() && lats.isEmpty()
                && lons.isEmpty()) {
            return null;
        }
        List<String> sources = getSupportedSourcesInternal();
        Map<String, GridCoverage> coverages = new HashMap<String, GridCoverage>();
        try {
            coverages = CoverageUtils.getInstance().getCoverages(sources);
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        Set<String> fileredSources = new HashSet<String>();
        for (String source : sources) {
            try {
                GridCoverage coverage = coverages.get(source);
                if (coverage == null) {
                    coverage = CoverageUtils.getInstance().getCoverage(source);
                }
                if (coverage == null) {
                    fileredSources.add(source);
                    continue;
                }
                GridGeometry2D gridGeom = coverage.getGridGeometry();
                MathTransform llToCRS = MapUtil.getTransformFromLatLon(gridGeom
                        .getCoordinateReferenceSystem());
                Envelope2D env = gridGeom.getEnvelope2D();
                for (String letter : pointLetters) {
                    Coordinate c = ToolsDataManager.getInstance().getPoint(
                            letter);
                    DirectPosition2D dp = new DirectPosition2D(c.x, c.y);
                    llToCRS.transform(dp, dp);
                    if (env.contains(dp.x, dp.y)) {
                        fileredSources.add(source);
                        break;
                    }
                }
                for (String letter : lineLetters) {
                    LineString ls = ToolsDataManager.getInstance().getBaseline(
                            letter);
                    Envelope2D lineEnv = null;
                    for (Coordinate c : ls.getCoordinates()) {
                        DirectPosition2D dp = new DirectPosition2D(c.x, c.y);
                        llToCRS.transform(dp, dp);
                        if (lineEnv == null) {
                            lineEnv = new Envelope2D(
                                    gridGeom.getCoordinateReferenceSystem(),
                                    dp.x, dp.y, 1, 1);
                        } else {
                            lineEnv.add(dp.x, dp.y);
                        }
                    }
                    if (lineEnv.intersects(env)) {
                        fileredSources.add(source);
                        break;
                    }
                }
                ReferencedEnvelope rEnv = new ReferencedEnvelope(env,
                        gridGeom.getCoordinateReferenceSystem());
                rEnv = rEnv.transform(MapUtil.getLatLonProjection(), true);
                for (Double lat : lats) {
                    if (rEnv.getMinY() < lat && rEnv.getMaxY() > lat) {
                        fileredSources.add(source);
                        break;
                    }
                }
                for (Double lon : lons) {
                    if (rEnv.getMinX() < lon && rEnv.getMaxX() > lon) {
                        fileredSources.add(source);
                        break;
                    }
                }
            } catch (Exception e) {
                fileredSources.add(source);
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
        return fileredSources;
    }

    @Override
    protected Collection<String> get3DPlanes(Collection<String> sources) {
        ViewMenu viewSelection = VolumeBrowserAction.getVolumeBrowserDlg()
                .getDialogSettings().getViewSelection();
        if (sources == null || sources.isEmpty()
                || viewSelection == ViewMenu.PLANVIEW
                || viewSelection == ViewMenu.TIMESERIES) {
            Set<String> results = new HashSet<String>();
            results.addAll(MenuItemManager.getInstance().getLatLonKeys());
            results.addAll(getPointLineKeys());
            return results;
        }
        ToolsDataManager tdm = ToolsDataManager.getInstance();
        Map<String, GridCoverage> coverages = new HashMap<String, GridCoverage>();
        try {
            coverages = CoverageUtils.getInstance().getCoverages(sources);
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        Set<String> validPlanes = new HashSet<String>(sources.size());
        for (String source : sources) {
            try {
                GridCoverage coverage = coverages.get(source);
                if (coverage == null) {
                    coverage = CoverageUtils.getInstance().getCoverage(source);
                }
                if (coverage == null) {
                    Set<String> results = new HashSet<String>();
                    results.addAll(MenuItemManager.getInstance()
                            .getLatLonKeys());
                    results.addAll(getPointLineKeys());
                    return results;
                }
                GridGeometry2D gridGeom = coverage.getGridGeometry();
                MathTransform llToCRS = MapUtil.getTransformFromLatLon(gridGeom
                        .getCoordinateReferenceSystem());
                Envelope2D env = gridGeom.getEnvelope2D();
                for (String letter : tdm.getPointNames()) {
                    Coordinate c = tdm.getPoint(letter);
                    DirectPosition2D dp = new DirectPosition2D(c.x, c.y);
                    llToCRS.transform(dp, dp);
                    if (env.contains(dp.x, dp.y)) {
                        validPlanes.add("Point" + letter);
                    }
                }
                for (String letter : tdm.getBaselineNames()) {
                    LineString ls = tdm.getBaseline(letter);
                    Envelope2D lineEnv = null;
                    for (Coordinate c : ls.getCoordinates()) {
                        DirectPosition2D dp = new DirectPosition2D(c.x, c.y);
                        llToCRS.transform(dp, dp);
                        if (lineEnv == null) {
                            lineEnv = new Envelope2D(
                                    gridGeom.getCoordinateReferenceSystem(),
                                    dp.x, dp.y, 1, 1);
                        } else {
                            lineEnv.add(dp.x, dp.y);
                        }
                    }
                    if (lineEnv.intersects(env)) {
                        validPlanes.add("Line" + letter);
                    }
                }
                ReferencedEnvelope rEnv = new ReferencedEnvelope(env,
                        gridGeom.getCoordinateReferenceSystem());
                rEnv = rEnv.transform(MapUtil.getLatLonProjection(), true);
                for (String llKey : MenuItemManager.getInstance()
                        .getLatLonKeys()) {
                    if (llKey.startsWith("Lat")) {
                        double lat = Double.parseDouble(llKey
                                .replace("Lat", ""));
                        if (rEnv.getMinY() < lat && rEnv.getMaxY() > lat) {
                            validPlanes.add(llKey);
                        }
                    } else if (llKey.startsWith("Lon")) {
                        double lon = Double.parseDouble(llKey
                                .replace("Lon", ""));
                        if (rEnv.getMinX() < lon && rEnv.getMaxX() > lon) {
                            validPlanes.add(llKey);
                        }
                    } else {
                        validPlanes.add(llKey);
                    }
                }
            } catch (Exception e) {
                validPlanes.add(source);
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
        return validPlanes;
    }
}
