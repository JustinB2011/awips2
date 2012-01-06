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
package com.raytheon.viz.warngen.gis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.raytheon.uf.common.dataplugin.warning.config.AreaConfiguration;
import com.raytheon.uf.common.dataplugin.warning.config.AreaSourceConfiguration;
import com.raytheon.uf.common.dataplugin.warning.config.AreaSourceConfiguration.AreaType;
import com.raytheon.uf.common.dataplugin.warning.config.GeospatialConfiguration;
import com.raytheon.uf.common.dataplugin.warning.config.WarngenConfiguration;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialData;
import com.raytheon.uf.common.dataplugin.warning.util.CountyUserData;
import com.raytheon.uf.common.dataplugin.warning.util.FileUtil;
import com.raytheon.uf.common.dataplugin.warning.util.GeometryUtil;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.geospatial.ISpatialQuery.SearchMode;
import com.raytheon.uf.common.geospatial.SpatialException;
import com.raytheon.uf.common.geospatial.SpatialQueryFactory;
import com.raytheon.uf.common.geospatial.SpatialQueryResult;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.warngen.gui.WarngenLayer;
import com.raytheon.viz.warngen.suppress.SuppressMap;
import com.raytheon.viz.warngen.util.Abbreviation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Area
 * 
 * Finds areas affected by area warnings
 * 
 * <pre>
 * 
 *    SOFTWARE HISTORY
 *   
 *    Date         Ticket#     Engineer    Description
 *    ------------ ----------  ----------- --------------------------
 *    Nov 15, 2007 #601        chammack    Initial Creation.
 * 
 * </pre>
 * 
 * @author chammack
 * @version 1
 */
public class Area {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(Area.class);

    /**
     * If an area greater than this percentage of the area is covered, no
     * direction is included
     */
    public static final double DEFAULT_PORTION_TOLERANCE = 0.25;

    private Area() {

    }

    public static AffectedAreas[] findAffectedAreas(
            WarngenConfiguration config, Geometry polygon,
            Geometry warningArea, String localizedSite) throws VizException {

        // --- Begin argument checking ---
        Validate.notNull(config.getGeospatialConfig().getAreaSource(),
                "Area source must be provided for findAffectedAreas to operate");
        Validate.notNull(polygon, "Area geometry must not be null");

        // Get spatial query result for entries in our area from existing data;
        List<Geometry> geoms = new ArrayList<Geometry>();
        GeometryUtil.buildGeometryList(geoms, warningArea);

        GeospatialConfiguration geospatialConfig = config.getGeospatialConfig();
        AreaConfiguration areaConfig = config.getAreaConfig();

        return findAffectedAreas(areaConfig, geospatialConfig, polygon,
                localizedSite, geoms);
    }

    private static AffectedAreas[] findAffectedAreas(
            AreaConfiguration areaConfig,
            GeospatialConfiguration geospatialConfig, Geometry polygon,
            String localizedSite, List<Geometry> geoms) throws VizException {
        String areaSource = areaConfig.getAreaSource();
        String areaField = areaConfig.getAreaField();
        String fipsField = areaConfig.getFipsField();
        String areaNotationField = areaConfig.getAreaNotationField();
        String pointField = areaConfig.getPointField();
        String pointSource = geospatialConfig.getPointSource();
        Map<String, RequestConstraint> pointFilter = areaConfig
                .getPointFilter();
        String parentAreaField = areaConfig.getParentAreaField();
        String timezonePathcastField = geospatialConfig.getTimezoneField();
        ArrayList<String> fields = new ArrayList<String>();
        /* fields is not used in querying to the database */
        if (areaConfig.getSortBy() != null) {
            for (String field : areaConfig.getSortBy()) {
                fields.add(field);
            }
        }

        Map<String, GeospatialData> countyMap = new HashMap<String, GeospatialData>();
        for (Geometry g : geoms) {
            CountyUserData data = (CountyUserData) g.getUserData();
            if (data != null) {
                String gid = GeometryUtil.getPrefix(data);
                if (countyMap.containsKey(gid) == false) {
                    countyMap.put(gid, data.entry);
                }
            }
        }

        // Query for points within polygon
        SpatialQueryResult[] ptFeatures = null;
        if (pointField != null) {
            try {
                ptFeatures = SpatialQueryFactory.create().query(pointSource,
                        new String[] { pointField }, polygon, pointFilter,
                        SearchMode.INTERSECTS);
            } catch (SpatialException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }

        Abbreviation abbreviation = null;

        if (areaConfig.getAreaNotationTranslationFile() != null) {
            try {
                abbreviation = new Abbreviation(FileUtil.getFile(
                        areaConfig.getAreaNotationTranslationFile(),
                        localizedSite));
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.ERROR, "Unable to find "
                        + areaConfig.getAreaNotationTranslationFile() + "", e);
            }
        }

        List<String> uniqueFips = new ArrayList<String>();
        List<AffectedAreas> areas = new ArrayList<AffectedAreas>();
        for (GeospatialData regionFeature : countyMap.values()) {
            Geometry regionGeom = regionFeature.geometry;
            Geometry parentGeom = regionFeature.parent.geometry;
            AffectedAreas area = new AffectedAreas();
            area.name = regionFeature.attributes.get(areaField).toString();
            area.fips = regionFeature.attributes.get(fipsField).toString();
            area.stateabbr = regionFeature.attributes.get(areaNotationField)
                    .toString();
            area.size = regionGeom.getArea();
            area.suppress = suppressType(areaSource, area.stateabbr, area.fips);

            Object tzData = regionFeature.attributes.get(timezonePathcastField);

            if (tzData != null) {
                area.timezone = String.valueOf(tzData);
            } else {
                area.timezone = "P";
            }

            if (abbreviation != null) {
                area.areaNotation = abbreviation.translate(String
                        .valueOf(regionFeature.attributes
                                .get(areaNotationField)));
                area.areasNotation = abbreviation.translatePlural(String
                        .valueOf(regionFeature.attributes
                                .get(areaNotationField)));
            }
            String gid = String.valueOf(regionFeature.attributes
                    .get(WarngenLayer.GID));
            List<Geometry> intersections = new ArrayList<Geometry>();
            for (Geometry g : geoms) {
                if (GeometryUtil.getPrefix(g.getUserData()).equalsIgnoreCase(
                        gid)) {
                    intersections.add(g);
                }
            }
            Geometry intersection = regionGeom.getFactory()
                    .createGeometryCollection(
                            intersections.toArray(new Geometry[intersections
                                    .size()]));
            double areaIntersection = intersection.getArea();

            double tolerCheck = regionGeom.getArea()
                    * DEFAULT_PORTION_TOLERANCE;
            if (areaIntersection < tolerCheck) {
                Coordinate centroidOfIntersection = intersection.getCentroid()
                        .getCoordinate();
                area.partOfArea = GisUtil.asStringList(GisUtil
                        .calculatePortion(regionGeom, centroidOfIntersection,
                                area.suppress));
            }

            // Search the parent region
            GeospatialData parentRegion = regionFeature.parent;
            if (parentRegion != null) {
                area.parentRegion = String.valueOf(parentRegion.attributes
                        .get(parentAreaField));

                area.partOfParentRegion = GisUtil.asStringList(GisUtil
                        .calculatePortion(parentGeom, regionGeom.getCentroid()
                                .getCoordinate()));
            }

            // Search against point matches
            if (ptFeatures != null) {
                List<String> pointList = new ArrayList<String>();
                for (SpatialQueryResult ptRslt : ptFeatures) {
                    if (regionGeom.contains(ptRslt.geometry)) {
                        pointList.add(String.valueOf(ptRslt.attributes
                                .get(pointField)));
                    }
                }

                area.points = pointList.toArray(new String[pointList.size()]);
            }
            if (uniqueFips.contains(area.fips) == false) {
                uniqueFips.add(area.fips);
                areas.add(area);
            }
        }

        // Perform Sort
        if (fields.size() > 0) {
            AffectedAreasComparator comparator = new AffectedAreasComparator(
                    fields);
            Collections.sort(areas, comparator);
        }
        return areas.toArray(new AffectedAreas[areas.size()]);
    }

    public static Map<String, Object> findInsectingAreas(
            WarngenConfiguration config, Geometry warnPolygon,
            Geometry warnArea, String localizedSite, WarngenLayer warngenLayer)
            throws VizException {
        Map<String, Object> areasMap = new HashMap<String, Object>();

        for (AreaSourceConfiguration asc : config.getAreaSources()) {
            if (asc.getAreaType() == AreaType.INTERSECT) {
                String areaSource = asc.getAreaSource();
                String key = areaSource + "." + localizedSite;

                List<Geometry> geoms = new ArrayList<Geometry>();

                for (GeospatialData f : warngenLayer.getGeodataFeatures(key)) {
                    for (int i = 0; i < warnArea.getNumGeometries(); i++) {
                        Geometry geom = warnArea.getGeometryN(i);
                        if (f.geometry.intersects(geom)) {
                            GeometryUtil.buildGeometryList(geoms, f.geometry);
                            break;
                        }
                    }
                }

                AffectedAreas[] affectedAreas = findAffectedAreas(
                        asc.getAreaConfig(), config.getGeospatialConfig(),
                        warnPolygon, localizedSite, geoms);

                areasMap.put(asc.getVariable(), affectedAreas);
            }
        }

        return areasMap;

    }

    private static String suppressType(String areaSource, String state,
            String fips) {
        String retVal = SuppressMap.NONE;
        String type = areaSource.equalsIgnoreCase("zone") ? "Z" : "C";

        if (state != null && fips != null) {
            String key = state + type + fips.substring(2);
            retVal = SuppressMap.getInstance().getType(key);
        }
        return retVal;
    }

}
