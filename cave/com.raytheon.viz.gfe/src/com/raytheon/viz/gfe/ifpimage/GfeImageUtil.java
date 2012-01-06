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
package com.raytheon.viz.gfe.ifpimage;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.vividsolutions.jts.geom.Point;

/**
 * Static methods to support IFP Image or generating GFE images off screen.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 12, 2009            njensen     Initial creation
 * 21May2009          6309 garmendariz Modified path for Geotools 2.6.4
 * 
 * </pre>
 * 
 * @author njensen
 * @version 1.0
 */

public class GfeImageUtil {

    public static GridGeometry2D getLocationGeometry(GridLocation gloc,
            com.vividsolutions.jts.geom.Envelope env, float width,
            float height, float percentLeft, float percentRight,
            float percentTop, float percentBottom) {

        Envelope envelope = null;
        GridGeometry2D newGridGeometry = null;

        try {
            if (env != null) {
                Point[] points = new Point[2];
                points[0] = MapUtil.getPoint(env.getMinY(), env.getMinX());
                points[1] = MapUtil.getPoint(env.getMaxY(), env.getMaxX());
                envelope = MapUtil.extractProjectedEnvelope(gloc.getCrs(),
                        points, MapUtil.getTransformFromLatLon(gloc.getCrs()));
            }
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (envelope == null) {
                GridGeometry2D gridGeometry = MapUtil.getGridGeometry(gloc);
                envelope = gridGeometry.getEnvelope();
            }
        }

        double dLeft = envelope.getSpan(0) * percentLeft;
        double dRight = envelope.getSpan(0) * percentRight;
        double dTop = envelope.getSpan(1) * percentTop;
        double dBottom = envelope.getSpan(1) * percentBottom;
        GeneralEnvelope newEnvelope = new GeneralEnvelope(
                envelope.getCoordinateReferenceSystem());
        newEnvelope.setRange(0, envelope.getMinimum(0) - dLeft,
                envelope.getMaximum(0) + dRight);
        newEnvelope.setRange(1, envelope.getMinimum(1) - dBottom,
                envelope.getMaximum(1) + dTop);
        newGridGeometry = new GridGeometry2D(new GeneralGridEnvelope(new int[] {
                0, 0 }, new int[] { (int) width, (int) height }), newEnvelope);

        return newGridGeometry;
    }
}
