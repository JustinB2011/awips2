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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.geotools.referencing.GeodeticCalculator;

import com.raytheon.viz.warngen.suppress.SuppressMap;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * GisUtil - Utilities for Warngen GIS
 * 
 * <pre>
 * 
 *    SOFTWARE HISTORY
 *   
 *    Date         Ticket#     Engineer    Description
 *    ------------ ----------  ----------- --------------------------
 *    Dec 7, 2007  #601        chammack    Initial Creation.
 *    Oct 31, 2011 #11077      Qinglu Lin  Added convertAlaskaLons(), which
 *                                         convert longitudes for Alaska when
 *                                         they're in Eastern Hemisphere; invoke
 *                                         it in convertCoords().
 *    Feb 29, 2012 #13596      Qinglu Lin  Added restoreAlaskaLon().
 * </pre>
 * 
 * @author chammack
 * @version 1
 */
public class GisUtil {

    private static final float PORTION_OF_CENTER = 0.1f;

    private static final float DIRECTION_DELTA = 15;

    private static final float EXTREME_DELTA = 0.10f;

    private static final double CONTAINS_PERCENTAGE = 0.1;

    public static enum Direction {
        CENTRAL, NORTH, SOUTH, EAST, WEST, EXTREME
    };

    public static boolean contains(Geometry filter, Geometry geom) {
        if (filter != null) {
            if (filter.contains(geom)) {
                return true;
            }

            double containsPercent = geom.buffer(0.00001).difference(filter)
                    .getArea()
                    / geom.getArea();
            if (containsPercent < CONTAINS_PERCENTAGE) {
                return true;
            }
        }

        return false;
    }

    public static Geometry intersect(Geometry shadedArea, Geometry filter) {
        GeometryFactory gf = new GeometryFactory();
        Geometry rval = shadedArea;
        if (filter instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) filter;
            List<Geometry> unionGeometries = new ArrayList<Geometry>();
            for (int k = 0; k < gc.getNumGeometries(); k++) {
                Geometry g = gc.getGeometryN(k);
                if (g.intersects(rval)) {
                    Geometry subIntersection = GisUtil.intersect(g, rval);
                    unionGeometries.add(subIntersection);
                }
            }

            rval = gf.createGeometryCollection(unionGeometries
                    .toArray(new Geometry[unionGeometries.size()]));
            rval = rval.buffer(0);

        } else {
            rval = rval.intersection(filter).buffer(0);
        }
        return rval;
    }

    public static EnumSet<Direction> calculatePortion(Geometry geom,
            Coordinate point) {
        return calculatePortion(geom, point, SuppressMap.NONE);
    }

    public static EnumSet<Direction> calculatePortion(Geometry geom,
            Coordinate point, String suppressType) {
        Direction xDirection = null;
        Direction yDirection = null;

        Coordinate centroid = geom.getCentroid().getCoordinate();
        Envelope envelope = geom.getEnvelopeInternal();
        double approximateWidth = envelope.getWidth();
        double approximateHeight = envelope.getHeight();

        double distanceX = Math.abs(centroid.x - point.x);
        double distanceY = Math.abs(centroid.y - point.y);

        double centerThresholdX = (approximateWidth * PORTION_OF_CENTER);
        double centerThresholdY = (approximateHeight * PORTION_OF_CENTER);
        double extremaThresholdX = (approximateWidth * EXTREME_DELTA);
        double extremaThresholdY = (approximateHeight * EXTREME_DELTA);

        if ((distanceX < centerThresholdX)) {
            xDirection = Direction.CENTRAL;
        }
        if ((distanceY < centerThresholdY)) {
            yDirection = Direction.CENTRAL;
        }

        if (xDirection != null && yDirection != null)
            return EnumSet.of(xDirection, yDirection);

        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(centroid.x, centroid.y);
        gc.setDestinationGeographicPoint(point.x, point.y);
        double azimuth = gc.getAzimuth();

        if (xDirection == null) {
            if (azimuth < (180 - DIRECTION_DELTA) && azimuth > DIRECTION_DELTA)
                xDirection = Direction.EAST;
            else if (azimuth < 0 - DIRECTION_DELTA)
                xDirection = Direction.WEST;
        }

        if (yDirection == null) {
            if (azimuth < (90 - DIRECTION_DELTA)
                    && azimuth > (-90 + DIRECTION_DELTA))
                yDirection = Direction.NORTH;
            else if (azimuth > (90 + DIRECTION_DELTA)
                    || azimuth < (-90 - DIRECTION_DELTA))
                yDirection = Direction.SOUTH;
        }

        boolean isExtreme = false;
        Coordinate[] coords;
        if (geom instanceof Polygon) {
            LineString lineString = ((Polygon) geom).getExteriorRing();
            coords = lineString.getCoordinates();
            isExtreme = isExtreme(coords, point,
                    (extremaThresholdX + extremaThresholdY) / 2.0);
        } else if (geom instanceof MultiPolygon) {
            int geoms = ((MultiPolygon) geom).getNumGeometries();

            for (int i = 0; i < geoms; i++) {
                LineString lineString = ((Polygon) ((MultiPolygon) geom)
                        .getGeometryN(i)).getExteriorRing();
                coords = lineString.getCoordinates();
                if (isExtreme(coords, point,
                        (extremaThresholdX + extremaThresholdY) / 2.0)) {
                    isExtreme = true;
                    break;
                }
            }

        }
        EnumSet<Direction> retVal = EnumSet.noneOf(Direction.class);

        if (xDirection != null && !suppressType.equals(SuppressMap.EAST_WEST)
                && !suppressType.equals(SuppressMap.ALL))
            retVal.add(xDirection);

        if (yDirection != null && !suppressType.equals(SuppressMap.NORTH_SOUTH)
                && !suppressType.equals(SuppressMap.ALL))
            retVal.add(yDirection);

        if (isExtreme && !suppressType.equals(SuppressMap.ALL))
            retVal.add(Direction.EXTREME);

        return retVal;
    }

    private static boolean isExtreme(Coordinate[] coords, Coordinate c,
            double threshold) {
        for (int i = 1; i < coords.length; i++) {
            double distance = CGAlgorithms.distancePointLine(c, coords[i - 1],
                    coords[i]);
            if (distance < threshold)
                return true;
        }

        return false;
    }

    public static List<String> asStringList(EnumSet<Direction> set) {
        List<String> list = new ArrayList<String>();
        Iterator<Direction> dirIter = set.iterator();
        while (dirIter.hasNext()) {
            list.add(dirIter.next().toString());
        }
        return list;
    }

    public static Coordinate[] convertAlaskaLons(Coordinate[] oldCoords) {
        int length = oldCoords.length;
        Coordinate[] coords = new Coordinate[length];
        for (int i = 0; i < length; i++) {
            coords[i] = new Coordinate();
            if (oldCoords[i].x > 150. && oldCoords[i].y > 45.) {
                coords[i].x = oldCoords[i].x - 360.;
            } else {
                coords[i].x = oldCoords[i].x;
            }
            coords[i].y = oldCoords[i].y;
        }
        return coords;
    }

    public static Point2D[] convertCoords(Coordinate[] oldCoords) {
        Coordinate[] coords = convertAlaskaLons(oldCoords);
        ArrayList<Point2D> pts = new ArrayList<Point2D>();
        int firstX = (int) (coords[0].x * 100);
        int firstY = (int) (coords[0].y * 100);
        pts.add(new Point2D.Double(coords[0].x, coords[0].y));
        for (int i = 0; i < coords.length - 1; i++) {
            if (!((int) (coords[i].x * 100) == firstX && (int) (coords[i].y * 100) == firstY)) {
                pts.add(new Point2D.Double(coords[i].x, coords[i].y));
            }
        }
        Point2D[] rval = new Point2D.Double[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            rval[i] = pts.get(i);
        }
        return rval;
    }

    public static boolean equivalent(Geometry oldGeom, Geometry newGeom) {
        Point2D[] old = GisUtil.convertCoords(oldGeom.getCoordinates());
        Point2D[] neww = GisUtil.convertCoords(newGeom.getCoordinates());
        return Arrays.equals(old, neww);
    }

    /**
     * restoreAlaskaLon()
     * 
     * Feb 28, 2012  DR13596  Qinglu Lin Created.
     * 
     * If the longitude of a Coordinate is less than -180 and corresponding 
     * latitude is larger than 45 degree North, convert it to a value 
     * equivalent to (360 + the longitude).
     */
    public static Coordinate restoreAlaskaLon(Coordinate oldCoords) {
        Coordinate coord = new Coordinate();
    	if (oldCoords.x < -180. && oldCoords.y > 45.) {
            coord.x = 360. + oldCoords.x;
        } else {
            coord.x = oldCoords.x;
        }
        coord.y = oldCoords.y;
        return coord;
    }

}
