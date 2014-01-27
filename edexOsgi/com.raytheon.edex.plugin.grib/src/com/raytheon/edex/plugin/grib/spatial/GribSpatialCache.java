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

package com.raytheon.edex.plugin.grib.spatial;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opengis.metadata.spatial.PixelOrientation;

import com.raytheon.edex.plugin.grib.exception.GribException;
import com.raytheon.edex.plugin.grib.util.GribModelLookup;
import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.awipstools.GetWfoCenterPoint;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.util.GridGeometryWrapChecker;
import com.raytheon.uf.common.gridcoverage.Corner;
import com.raytheon.uf.common.gridcoverage.GridCoverage;
import com.raytheon.uf.common.gridcoverage.exception.GridCoverageException;
import com.raytheon.uf.common.gridcoverage.lookup.GridCoverageLookup;
import com.raytheon.uf.common.gridcoverage.subgrid.SubGrid;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.awipstools.GetWfoCenterHandler;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Cache used for holding GridCoverage objects. This cache is responsible for
 * managing the grids and subgrids stored on the filesystem for grib data. The
 * functions in this class have some overlap with the GridCoverageLookup,
 * methods in this class have some extra functionality for trying to resolve
 * grids off the filesystem.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer     Description
 * ------------- -------- ------------ --------------------------
 * Apr 07, 2009  1994     bphillip     Initial Creation
 * Mar 07, 2013  1771     bsteffen     make subgridding deterministic.
 * Jan 04, 2013  15653    M.Porricelli Shift subgrid domain westward like
 *                                     AWIPSI
 * Oct 15, 2013  2473     bsteffen     Rewrite deprecated code.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class GribSpatialCache {

    /** The logger */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GribSpatialCache.class);

    private static final SingleTypeJAXBManager<GridCoverage> GRID_COVERAGE_JAXB = SingleTypeJAXBManager
            .createWithoutException(GridCoverage.class);

    private static final SingleTypeJAXBManager<SubGridDef> SUB_GRID_DEF_JAXB = SingleTypeJAXBManager
            .createWithoutException(SubGridDef.class);

    private static final SingleTypeJAXBManager<DefaultSubGridCenterPoint> SUB_GRID_CENTER_JAXB = SingleTypeJAXBManager
            .createWithoutException(DefaultSubGridCenterPoint.class);

    /** The singleton instance */
    private static GribSpatialCache instance;

    /**
     * Map containing the GridCoverages<br>
     * The key for this map is the name field of the GridCoverage object stored
     * as the value of the map. This is only used internally for lookup of a
     * coverage by name aka gridId.
     */
    private Map<String, GridCoverage> spatialNameMap;

    /**
     * Maps a GridCoverage id to a set of grid names. The id should come from a
     * coverage in the database but the names are not necessarily in the
     * database. If a grid file spatially matches a grid in the database it will
     * not be added to the db to avoid confusion, this map can be used to look
     * up those names.
     */
    private Map<Integer, Set<String>> gridNameMap;

    /**
     * Map containing the subGrid coverage based on a subGridKey
     */
    private Map<String, GridCoverage> subGridCoverageMap;

    /**
     * Map containing the subGrid based on a the subGridKey
     */
    private Map<String, SubGrid> definedSubGridMap;

    /**
     * Map containing the subGrid definition based on a model name and the base
     * coverage name
     */
    private Map<String, SubGridDef> subGridDefMap;

    /**
     * Map of coverage id to the number of columns in world wrap or -1 for no
     * wrapping.
     */
    private Map<Integer, Integer> worldWrapMap;

    private FileDataList fileDataList;

    private long fileScanTime = 0;
    
    boolean shiftSubGridWest = false;

    /**
     * Gets the singleton instance of GribSpatialCache
     * 
     * @return The singleton instance of the GribSpatialCache
     */
    public static synchronized GribSpatialCache getInstance() {
        if (instance == null) {
            instance = new GribSpatialCache();
        }
        return instance;
    }

    /**
     * Creates a new GribSpatialCache
     */
    private GribSpatialCache() {
        gridNameMap = new HashMap<Integer, Set<String>>();
        spatialNameMap = new HashMap<String, GridCoverage>();
        definedSubGridMap = new HashMap<String, SubGrid>();
        subGridCoverageMap = new HashMap<String, GridCoverage>();
        subGridDefMap = new HashMap<String, SubGridDef>();
        worldWrapMap = new HashMap<Integer, Integer>();
        scanFiles();
    }

    /**
     * Retrieves a grid from the map. If the grid does not exist, the
     * localization files are scanned for newly defined grids, if it still
     * doesn't exist null is returned
     * 
     * @param coverage
     *            a grid coverage
     * @return The GridCoverage object, null if not present
     * @throws GribException
     * @throws DataAccessLayerException
     */
    public GridCoverage getGrid(GridCoverage coverage) {
        GridCoverage rval = GridCoverageLookup.getInstance().getCoverage(
                coverage, false);
        if (rval == null) {
            scanFiles();
            rval = GridCoverageLookup.getInstance()
                    .getCoverage(coverage, false);
        }
        return rval;
    }

    /**
     * Get a grib by name, first all grid files are searched to find one with a
     * matching name, if none is found the database is checked. The returned
     * coverage may not have the name you are looking for but it will be
     * spatially equivalent to that named grid.
     * 
     * @param name
     * @return
     */
    public GridCoverage getGridByName(String name) {
        GridCoverage coverage = spatialNameMap.get(name);
        if (coverage == null) {
            // rescan and hopefully the coverage shows up
            scanFiles();
            coverage = spatialNameMap.get(name);
        }
        return coverage;
    }

    /**
     * This method provides a way to get the names from the definiton files for
     * looking up a grib model. It will return all the names of any coverages
     * defined in the grid definition files that are spatially equivalent to the
     * passed in coverage. This is useful when there are multiple grid
     * definition files with the same spatial attributes but different names or
     * for cases where the name in the definition file does not match what is
     * currently in the db.
     * 
     * @param coverage
     * @return
     */
    public Set<String> getGribCoverageNames(GridCoverage coverage) {
        Set<String> rval = gridNameMap.get(coverage.getId());
        if (rval == null) {
            scanFiles();
            rval = gridNameMap.get(coverage.getId());
            if (rval == null) {
                rval = Collections.emptySet();
            }
        }
        return rval;
    }

    /**
     * For a given modelName and coverage this will return the SubGrid used for
     * slicing data if there is a subGrid file for this model. If this model
     * does not require subgridding this method will return null.
     * 
     * @param modelName
     * @param coverage
     * @return
     */
    public SubGrid getSubGrid(String modelName, GridCoverage coverage) {
        SubGrid subGrid = definedSubGridMap
                .get(subGridKey(modelName, coverage));
        if (subGrid == null) {
            if (loadSubGrid(modelName, coverage)) {
                subGrid = definedSubGridMap
                        .get(subGridKey(modelName, coverage));
            }
        }
        return subGrid;
    }

    /**
     * For a given modelName and coverage this will return the sub-GridCoverage
     * which should be used for this data. If this model does not require
     * subgridding this method will return null.
     * 
     * @param modelName
     * @param coverage
     * @return
     */
    public GridCoverage getSubGridCoverage(String modelName,
            GridCoverage coverage) {
        GridCoverage subGrid = subGridCoverageMap.get(subGridKey(modelName,
                coverage));
        if (subGrid == null) {
            if (loadSubGrid(modelName, coverage)) {
                subGrid = subGridCoverageMap
                        .get(subGridKey(modelName, coverage));
            }
        }
        return subGrid;
    }

    /**
     * If a sub grid area is defined for this model than this will process that
     * defintion and piopulate the subGridCoverageMap and definedSubGridMap.
     * 
     * @param modelName
     * @param coverage
     * @return true if this model is subgridded, false otherwise
     */
    private boolean loadSubGrid(String modelName, GridCoverage coverage) {
        SubGridDef subGridDef = subGridDefMap.get(modelName);
        if (subGridDef != null) {
            String referenceGrid = subGridDef.getReferenceGrid();
            if (referenceGrid == null) {
                referenceGrid = GribModelLookup.getInstance()
                        .getModelByName(subGridDef.getReferenceModel())
                        .getGrid();
                if (referenceGrid == null) {
                    statusHandler
                            .error("Failed to generate sub grid, Unable to determine coverage for referenceModel ["
                            + subGridDef.getReferenceModel() + "]");
                    return false;
                }
            }

            GridCoverage referenceCoverage = getGridByName(referenceGrid
                    .toString());
            if (referenceCoverage == null) {
                statusHandler
                        .error("Failed to generate sub grid, Unable to determine coverage for referenceGrid ["
                        + referenceGrid + "]");
                return false;
            }

            Coordinate subGridCenterLatLon = new Coordinate(
                    subGridDef.getCenterLongitude(),
                    subGridDef.getCenterLatitude());

            Coordinate subGridCenterGridCoord = MapUtil.latLonToGridCoordinate(
                    subGridCenterLatLon, PixelOrientation.CENTER,
                    referenceCoverage);
            
            double shiftX = 0;
            
            // Check whether 'shiftWest' flag is set in subgrid definition xml file
            boolean shiftThisSubGridWest = this.shiftSubGridWest;
            if (subGridDef.getShiftWest() != null) {
                shiftThisSubGridWest = subGridDef.getShiftWest();
            }
            
            if (shiftThisSubGridWest == true) {
            	shiftX = subGridDef.getNx() / 5;
            }
  
            double xCenterPoint = subGridCenterGridCoord.x - shiftX;
            double yCenterPoint = subGridCenterGridCoord.y;

            double xDistance = subGridDef.getNx() / 2;
            double yDistance = subGridDef.getNy() / 2;
            Coordinate lowerLeftPosition = new Coordinate(xCenterPoint
                    - xDistance, yCenterPoint + yDistance);
            Coordinate upperRightPosition = new Coordinate(xCenterPoint
                    + xDistance, yCenterPoint - yDistance);
            
            // If the western edge of the subgrid is placed west of the full grid boundary
            // (possibly when westward shifting above was done) it will be shifted back 
            // to within the boundary, but the eastern edge should be shifted back also, by 
            // a proportional amount
            if (lowerLeftPosition.x < 0) upperRightPosition.x -= lowerLeftPosition.x;

            lowerLeftPosition = MapUtil.gridCoordinateToLatLon(
                    lowerLeftPosition, PixelOrientation.CENTER,
                    referenceCoverage);
            upperRightPosition = MapUtil.gridCoordinateToLatLon(
                    upperRightPosition, PixelOrientation.CENTER,
                    referenceCoverage);

            return trim(modelName, coverage, lowerLeftPosition, upperRightPosition);
        } else {
            Integer wrapCount = worldWrapMap.get(coverage.getId());
            if (wrapCount == null) {
                wrapCount = GridGeometryWrapChecker.checkForWrapping(coverage
                        .getGridGeometry());
                worldWrapMap.put(coverage.getId(), wrapCount);
            }
            if(wrapCount > 0 && wrapCount < coverage.getNx()){
                // make sure that there is data going around the world only
                // once, if the data starts another iteration around the world,
                // subgrid it to cut off the extra data. This mostly hits to
                // remove one redundant column.
                Coordinate upperRightPosition = new Coordinate(wrapCount - 1, 0);
                upperRightPosition = MapUtil.gridCoordinateToLatLon(upperRightPosition,
                        PixelOrientation.CENTER, coverage);
                try {
                    Coordinate lowerLeftPosition = new Coordinate(
                            coverage.getLowerLeftLon(),
                            coverage.getLowerLeftLat());
                    return trim(modelName, coverage, lowerLeftPosition,
                            upperRightPosition);
                } catch (GridCoverageException e) {
                    statusHandler.error(
                            "Failed to generate sub grid for world wide grid: "
                                    + modelName, e);
                    return false;
                }
            }else{
                return false;
            }
        }
    }

    /**
     * Final step of subgrid generation, based off the two Coordinates generate
     * a subgrid coverage, insert it into the db and add it to caches.
     * 
     * @param modelName
     * @param coverage
     * @param lowerLeft
     * @param upperRight
     * @return true on success, false if something went wrong, so no subgrid is
     *         available. This method will log errors and return false
     */
    private boolean trim(String modelName, GridCoverage coverage,
            Coordinate lowerLeft, Coordinate upperRight) {
        SubGrid subGrid = new SubGrid();
        subGrid.setLowerLeftLon(lowerLeft.x);
        subGrid.setLowerLeftLat(lowerLeft.y);
        subGrid.setUpperRightLon(upperRight.x);
        subGrid.setUpperRightLat(upperRight.y);

        // verify numbers in -180 -> 180 range
        subGrid.setLowerLeftLon(MapUtil.correctLon(subGrid.getLowerLeftLon()));
        subGrid.setUpperRightLon(MapUtil.correctLon(subGrid.getUpperRightLon()));

        GridCoverage subGridCoverage = coverage.trim(subGrid);

        if (subGridCoverage != null) {
            try {
                subGridCoverage = insert(subGridCoverage);
            } catch (Exception e) {
                statusHandler.error(e.getLocalizedMessage(), e);
                return false;
            }
            subGridCoverageMap.put(subGridKey(modelName, coverage),
                    subGridCoverage);
            definedSubGridMap.put(subGridKey(modelName, coverage), subGrid);
        }
        return true;
    }

    /**
     * Loads and validates subGridDef pointed to by filePath. If definition
     * empty/invalid returns null.
     * 
     * @param filePath
     * @return
     */
    private SubGridDef loadSubGridDef(final String filePath,
            final Coordinate defaultCenter) {
        SubGridDef rval = null;
        File f = new File(filePath);

        if (f.length() > 0) {
            try {
                rval = SUB_GRID_DEF_JAXB.unmarshalFromXmlFile(f);
                if ((rval.getReferenceModel() == null && rval
                        .getReferenceGrid() == null)
                        || (rval.getModelNames() == null)
                        || (rval.getModelNames().size() == 0)) {
                    // sub grid didn't have required definitions
                    rval = null;
                } else {
                    if ((rval.getCenterLatitude() == null)
                            || (rval.getCenterLongitude() == null)) {
                        if (defaultCenter == null) {
                            rval = null;
                        } else {
                            rval.setCenterLatitude(defaultCenter.y);
                            rval.setCenterLongitude(defaultCenter.x);
                        }
                    }
                }
            } catch (SerializationException e) {
                statusHandler.error(
                        "Failed reading sub grid file: " + filePath, e);
            }
        }

        return rval;
    }

    private String subGridKey(String modelName, GridCoverage coverage) {
        return modelName + "&" + coverage.getId();
    }

    /**
     * scan the grib grid definition for changes, when force is false this will
     * only scan if we have not scanne din the last 60 seconds.
     * 
     * @param force
     * @return
     */
    private synchronized void scanFiles() {
        if (fileScanTime + 60000 > System.currentTimeMillis()) {
            return;
        }
        FileDataList currentFDL = generateFileDataList();
        fileScanTime = System.currentTimeMillis();
        if (!currentFDL.equals(this.fileDataList)) {
            initializeGrids(currentFDL);
            return;
        } else {
            return;
        }
    }

    private void initializeGrids(FileDataList fdl) {
        statusHandler.info("Initializing grib grid coverages");
        long startTime = System.currentTimeMillis();
        ClusterTask ct = null;
        Map<Integer, Set<String>> gridNameMap = new HashMap<Integer, Set<String>>();
        Map<String, GridCoverage> spatialNameMap = new HashMap<String, GridCoverage>();
        Map<String, SubGridDef> subGridDefMap = new HashMap<String, SubGridDef>();
        do {
            ct = ClusterLockUtils.lock("grib", "spatialCache", 120000, true);
        } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));

        try {
            for (FileData fd : fdl.getCoverageFileList()) {
                try {
                    GridCoverage grid = GRID_COVERAGE_JAXB
                            .unmarshalFromXmlFile(fd.getFilePath());
                    String name = grid.getName();
                    grid = insert(grid);
                    spatialNameMap.put(name, grid);
                    Set<String> names = gridNameMap.get(grid.getId());
                    if (names == null) {
                        names = new HashSet<String>();
                        gridNameMap.put(grid.getId(), names);
                    }
                    names.add(name);
                } catch (Exception e) {
                    // Log error but do not throw exception
                    statusHandler.error(
                            "Unable to read default grids file: "
                                    + fd.getFilePath(), e);
                }
            }
            Coordinate defaultCenterPoint = null;

            try {
                defaultCenterPoint = getDefaultSubGridCenterPoint();
            } catch (Exception e) {
                statusHandler
                        .error(
                        "Failed to generate sub grid definitions.  Unable to lookup WFO Center Point",
                        e);
            }
            for (FileData fd : fdl.getSubGridFileList()) {
                try {
                    SubGridDef subGridDef = loadSubGridDef(fd.getFilePath(),
                            defaultCenterPoint);
                    if (subGridDef == null) {
                        continue;
                    }
                    for (String modelName : subGridDef.getModelNames()) {
                        subGridDefMap.put(modelName, subGridDef);
                    }
                } catch (Exception e) {
                    // Log error but do not throw exception
                    statusHandler.error(
                            "Unable to read default grids file: "
                                    + fd.getFilePath(), e);
                }
            }
            this.gridNameMap = gridNameMap;
            this.spatialNameMap = spatialNameMap;
            this.subGridDefMap = subGridDefMap;
            this.subGridCoverageMap.clear();
            this.definedSubGridMap.clear();
            this.fileDataList = fdl;
        } finally {
            ClusterLockUtils.unlock(ct, false);
        }
        long endTime = System.currentTimeMillis();
        statusHandler.info("Grib grid coverages initialized: "
                + (endTime - startTime)
                + "ms");
    }

    private GridCoverage insert(GridCoverage coverage)
            throws GridCoverageException {
        coverage.initialize();
        GridCoverageLookup gcl = GridCoverageLookup.getInstance();
        GridCoverage dbCoverage = gcl.getCoverage(coverage, true);
        return dbCoverage;
    }

    private FileDataList generateFileDataList() {
        /*
         * Retrieve the list of files from the localization service
         */
        IPathManager pm = PathManagerFactory.getPathManager();
        FileDataList fileList = new FileDataList();
        LocalizationContext[] contexts = pm
                .getLocalSearchHierarchy(LocalizationType.EDEX_STATIC);
        fileList.addCoverageFiles(pm.listFiles(contexts, "/grib/grids",
                new String[] { "xml" }, true, true));
        fileList.addSubGridFiles(pm.listFiles(contexts, "/grib/subgrids",
                new String[] { "xml" }, true, true));

        return fileList;
    }

    public static void reinitialize() {
        GribSpatialCache newInstance = new GribSpatialCache();
        instance = newInstance;
    }

    private Coordinate getDefaultSubGridCenterPoint() throws Exception {
        Coordinate defaultCenterPoint = null;
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile defaultSubGridLocationFile = pm
                .getStaticLocalizationFile("/grib/defaultSubGridCenterPoint.xml");
        if ((defaultSubGridLocationFile != null)
                && defaultSubGridLocationFile.exists()) {
            try {
                DefaultSubGridCenterPoint defaultSubGridLocation = defaultSubGridLocationFile
                        .jaxbUnmarshal(DefaultSubGridCenterPoint.class,
                                SUB_GRID_CENTER_JAXB);
                if ((defaultSubGridLocation != null)
                        && (defaultSubGridLocation.getCenterLatitude() != null)
                        && (defaultSubGridLocation.getCenterLongitude() != null)) {
                    defaultCenterPoint = new Coordinate(
                            defaultSubGridLocation.getCenterLongitude(),
                            defaultSubGridLocation.getCenterLatitude());
                    statusHandler
                            .info("Default sub grid location is overriden as ["
                            + defaultCenterPoint.y + "/" + defaultCenterPoint.x
                            + "]");
                }
            } catch (Exception e) {
                statusHandler.error(
                        "Unable to load default sub grid location from file: "
                                + defaultSubGridLocationFile.getFile()
                                        .getAbsolutePath(),
                        e);
            }
        }

        if (defaultCenterPoint == null) {
            // use wfo center point
            String wfo = SiteUtil.getSite();
            GetWfoCenterPoint centerPointRequest = new GetWfoCenterPoint(wfo);
            defaultCenterPoint = new GetWfoCenterHandler()
                    .handleRequest(centerPointRequest);
            statusHandler
                    .info("Default sub grid location is wfo center point ["
                    + defaultCenterPoint.y + "/" + defaultCenterPoint.x + "]");
            /* If we are getting the WFO center as the center point, it means that
            // the site has not defined its own center in the site file
            // defaultSubGridCenterPoint.xml (see previous If block).  
            // Therefore, we will be shifting the domain westward to be similar to 
            // AWIPSI default behavior, so set a flag here.  
            // If the site *has* defined a center in defaultSubGridCenterPoint.xml, 
            // we will use that as the true, intended center and will not shift the 
            // domain further.
            */
            shiftSubGridWest = true;
        }
        else shiftSubGridWest = false;

        return defaultCenterPoint;
    }

    public static Corner determineFirstGridPointCorner(int scanMode) {
        if ((scanMode & 128) > 0) {
            // -i
            if ((scanMode & 64) > 0) {
                // +j
                return Corner.LowerRight;
            } else {
                // -j
                return Corner.UpperRight;
            }
        } else {
            // +i
            if ((scanMode & 64) > 0) {
                // +j
                return Corner.LowerLeft;
            } else {
                // -j
                return Corner.UpperLeft;
            }
        }
    }

}
