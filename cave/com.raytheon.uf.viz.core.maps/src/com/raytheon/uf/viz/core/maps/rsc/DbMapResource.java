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
package com.raytheon.uf.viz.core.maps.rsc;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataquery.db.QueryResult;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery.QueryLanguage;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.maps.rsc.AbstractDbMapResourceData.ColumnDefinition;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.DensityCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.LabelableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.MagnificationCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.OutlineCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ShadeableCapability;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.PointStyle;
import com.raytheon.viz.core.spatial.GeometryCache;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBReader;

/**
 * Databased map resource for line and polygon data
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2009            randerso     Initial creation
 * 
 * </pre>
 * 
 * @author randerso
 * @version 1.0
 */
public class DbMapResource extends
        AbstractDbMapResource<DbMapResourceData, MapDescriptor> {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(DbMapResource.class);

    private static final String GID = "gid";

    /**
     * at time of writing this is the density multiplier used to determine if a
     * label should be drawn in ZoneSelectorResource
     */
    private static final int BASE_DENSITY_MULT = 50;

    protected class LabelNode {
        private final Rectangle2D rect;

        private final String label;

        private final double[] location;

        public LabelNode(String label, Point c, IGraphicsTarget target) {
            this.label = label;
            this.location = descriptor.worldToPixel(new double[] {
                    c.getCoordinate().x, c.getCoordinate().y });
            DrawableString ds = new DrawableString(label, null);
            ds.font = font;
            rect = target.getStringsBounds(ds);
        }

        /**
         * @return the rect
         */
        public Rectangle2D getRect() {
            return rect;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return the location
         */
        public double[] getLocation() {
            return location;
        }
    }

    private class MapQueryJob extends Job {

        private static final int QUEUE_LIMIT = 1;

        public class Request {
            Random rand = new Random(System.currentTimeMillis());

            IGraphicsTarget target;

            IMapDescriptor descriptor;

            DbMapResource rsc;

            String geomField;

            String labelField;

            String shadingField;

            String query;

            Map<Object, RGB> colorMap;

            Request(IGraphicsTarget target, IMapDescriptor descriptor,
                    DbMapResource rsc, String query, String geomField,
                    String labelField, String shadingField,
                    Map<Object, RGB> colorMap) {
                this.target = target;
                this.descriptor = descriptor;
                this.rsc = rsc;
                this.query = query;
                this.geomField = geomField;
                this.labelField = labelField;
                this.shadingField = shadingField;
                this.colorMap = colorMap;
            }

            RGB getColor(Object key) {
                if (colorMap == null) {
                    colorMap = new HashMap<Object, RGB>();
                }
                RGB color = colorMap.get(key);
                if (color == null) {
                    color = new RGB(rand.nextInt(206) + 50,
                            rand.nextInt(206) + 50, rand.nextInt(206) + 50);
                    colorMap.put(key, color);
                }

                return color;
            }
        }

        public class Result {
            public IWireframeShape outlineShape;

            public List<LabelNode> labels;

            public IShadedShape shadedShape;

            public Map<Object, RGB> colorMap;

            public boolean failed;

            public Throwable cause;

            public String query;

            private Result(String query) {
                this.query = query;
                failed = true;
            }
        }

        private ArrayBlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(
                QUEUE_LIMIT);

        private ArrayBlockingQueue<Result> resultQueue = new ArrayBlockingQueue<Result>(
                QUEUE_LIMIT);

        private boolean canceled;

        public MapQueryJob() {
            super("Retrieving map...");
        }

        public void request(IGraphicsTarget target, IMapDescriptor descriptor,
                DbMapResource rsc, String query, String geomField,
                String labelField, String shadingField,
                Map<Object, RGB> colorMap) {
            if (requestQueue.size() == QUEUE_LIMIT) {
                requestQueue.poll();
            }
            requestQueue.add(new Request(target, descriptor, rsc, query,
                    geomField, labelField, shadingField, colorMap));

            this.cancel();
            this.schedule();
        }

        public Result getLatestResult() {
            return resultQueue.poll();
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Request req = requestQueue.poll();
            while (req != null) {
                Result result = new Result(req.query);
                try {
                    String table = resourceData.getTable();
                    if (canceled) {
                        canceled = false;
                        result = null;
                        return Status.CANCEL_STATUS;
                    }
                    QueryResult mappedResult = MapQueryCache
                            .executeMappedQuery(req.query, "maps",
                                    QueryLanguage.SQL);

                    Map<Integer, Geometry> gidMap = new HashMap<Integer, Geometry>(
                            mappedResult.getResultCount() * 2);
                    List<Integer> toRequest = new ArrayList<Integer>(
                            mappedResult.getResultCount());
                    for (int i = 0; i < mappedResult.getResultCount(); ++i) {
                        if (canceled) {
                            canceled = false;
                            result = null;
                            return Status.CANCEL_STATUS;
                        }
                        int gid = ((Number) mappedResult.getRowColumnValue(i,
                                GID)).intValue();
                        Geometry geom = GeometryCache.getGeometry(table, ""
                                + gid, req.geomField);
                        if (geom != null) {
                            gidMap.put(gid, (Geometry) geom.clone());
                        } else {
                            toRequest.add(gid);
                        }
                    }

                    if (toRequest.size() > 0) {
                        WKBReader wkbReader = new WKBReader();
                        StringBuilder geomQuery = new StringBuilder();
                        geomQuery.append("SELECT ").append(GID)
                                .append(", AsBinary(").append(req.geomField)
                                .append(") as ").append(req.geomField)
                                .append(" FROM ").append(table)
                                .append(" WHERE ").append(GID).append(" IN (");
                        Integer first = toRequest.get(0);
                        geomQuery.append('\'').append(first).append('\'');
                        for (int i = 1; i < toRequest.size(); ++i) {
                            Integer gid = toRequest.get(i);
                            geomQuery.append(",'").append(gid).append('\'');
                        }
                        geomQuery.append(");");

                        if (canceled) {
                            canceled = false;
                            result = null;
                            return Status.CANCEL_STATUS;
                        }
                        QueryResult geomResults = MapQueryCache
                                .executeMappedQuery(geomQuery.toString(),
                                        "maps", QueryLanguage.SQL);
                        for (int i = 0; i < geomResults.getResultCount(); ++i) {
                            if (canceled) {
                                canceled = false;
                                result = null;
                                return Status.CANCEL_STATUS;
                            }
                            int gid = ((Number) geomResults.getRowColumnValue(
                                    i, 0)).intValue();
                            Geometry g = null;
                            Object obj = geomResults.getRowColumnValue(i, 1);
                            if (obj instanceof byte[]) {
                                byte[] wkb = (byte[]) obj;
                                g = wkbReader.read(wkb);
                            } else {
                                statusHandler.handle(Priority.ERROR,
                                        "Expected byte[] received "
                                                + obj.getClass().getName()
                                                + ": " + obj.toString()
                                                + "\n  query=\"" + req.query
                                                + "\"");
                            }
                            gidMap.put(gid, g);
                            GeometryCache.putGeometry(table, "" + gid,
                                    req.geomField, (Geometry) g.clone());
                        }
                    }

                    IWireframeShape newOutlineShape = req.target
                            .createWireframeShape(false, req.descriptor, 0.0f);

                    List<LabelNode> newLabels = new ArrayList<LabelNode>();

                    IShadedShape newShadedShape = null;
                    if (req.shadingField != null) {
                        newShadedShape = req.target.createShadedShape(false,
                                req.descriptor, true);
                    }

                    JTSCompiler jtsCompiler = new JTSCompiler(newShadedShape,
                            newOutlineShape, req.descriptor, PointStyle.CROSS);

                    List<Geometry> resultingGeoms = new ArrayList<Geometry>(
                            mappedResult.getResultCount());
                    int numPoints = 0;
                    for (int i = 0; i < mappedResult.getResultCount(); ++i) {
                        if (canceled) {
                            canceled = false;
                            result = null;
                            return Status.CANCEL_STATUS;
                        }
                        int gid = ((Number) mappedResult.getRowColumnValue(i,
                                GID)).intValue();
                        Geometry g = gidMap.get(gid);
                        Object obj = null;

                        if (req.labelField != null) {
                            obj = mappedResult.getRowColumnValue(i,
                                    req.labelField.toLowerCase());
                        }

                        if (obj != null && g != null) {
                            String label;
                            if (obj instanceof BigDecimal) {
                                label = Double.toString(((Number) obj)
                                        .doubleValue());
                            } else {
                                label = obj.toString();
                            }
                            int numGeometries = g.getNumGeometries();
                            List<Geometry> gList = new ArrayList<Geometry>(
                                    numGeometries);
                            for (int polyNum = 0; polyNum < numGeometries; polyNum++) {
                                Geometry poly = g.getGeometryN(polyNum);
                                gList.add(poly);
                            }
                            // Sort polygons in g so biggest comes first.
                            Collections.sort(gList, new Comparator<Geometry>() {
                                @Override
                                public int compare(Geometry g1, Geometry g2) {
                                    return (int) Math.signum(g2.getEnvelope()
                                            .getArea()
                                            - g1.getEnvelope().getArea());
                                }
                            });

                            for (Geometry poly : gList) {
                                Point point = poly.getInteriorPoint();
                                if (point.getCoordinate() != null) {
                                    LabelNode node = new LabelNode(label,
                                            point, req.target);
                                    newLabels.add(node);
                                }
                            }
                        }

                        if (g != null) {
                            numPoints += g.getNumPoints();
                            resultingGeoms.add(g);
                            if (req.shadingField != null) {
                                g.setUserData(mappedResult.getRowColumnValue(i,
                                        req.shadingField.toLowerCase()));
                            }
                        }
                    }

                    newOutlineShape.allocate(numPoints);

                    for (Geometry g : resultingGeoms) {
                        RGB color = null;
                        Object shadedField = g.getUserData();
                        if (shadedField != null) {
                            color = req.getColor(shadedField);
                        }

                        try {
                            jtsCompiler.handle(g, color, true);
                        } catch (VizException e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Error reprojecting map outline", e);
                        }
                    }

                    newOutlineShape.compile();

                    if (req.shadingField != null) {
                        newShadedShape.compile();
                    }

                    result.outlineShape = newOutlineShape;
                    result.labels = newLabels;
                    result.shadedShape = newShadedShape;
                    result.colorMap = req.colorMap;
                    result.failed = false;
                } catch (Throwable e) {
                    result.cause = e;
                } finally {
                    if (result != null) {
                        if (resultQueue.size() == QUEUE_LIMIT) {
                            resultQueue.poll();
                        }
                        resultQueue.add(result);
                        req.rsc.issueRefresh();
                    }
                }

                req = requestQueue.poll();
            }

            return Status.OK_STATUS;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#canceling()
         */
        @Override
        protected void canceling() {
            super.canceling();
            this.canceled = true;
        }
    }

    protected IWireframeShape outlineShape;

    protected List<LabelNode> labels;

    protected IShadedShape shadedShape;

    protected Map<Object, RGB> colorMap;

    protected double[] levels;

    protected double lastSimpLev;

    protected String lastLabelField;

    protected String lastShadingField;

    private MapQueryJob queryJob;

    protected String geometryType;

    public DbMapResource(DbMapResourceData data, LoadProperties loadProperties) {
        super(data, loadProperties);
        queryJob = new MapQueryJob();

        // Prepopulate fields
        getLabelFields();
        getGeometryType();
        getLevels();
    }

    @Override
    protected void disposeInternal() {
        if (outlineShape != null) {
            outlineShape.dispose();
        }

        if (shadedShape != null) {
            shadedShape.dispose();
        }

        super.disposeInternal();
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        getCapability(ShadeableCapability.class).setAvailableShadingFields(
                getLabelFields().toArray(new String[0]));
    }

    private String buildQuery(PixelExtent extent) throws VizException {
        Envelope env = null;
        try {
            Envelope e = descriptor.pixelToWorld(extent, descriptor.getCRS());
            ReferencedEnvelope ref = new ReferencedEnvelope(e,
                    descriptor.getCRS());
            env = ref.transform(MapUtil.LATLON_PROJECTION, true);
        } catch (Exception e) {
            throw new VizException("Error transforming extent", e);
        }

        double[] levels = getLevels();
        String geometryField = getGeomField(levels[levels.length - 1]);

        // get the geometry field
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(GID);

        // add any additional columns
        List<String> additionalColumns = new ArrayList<String>();
        if (resourceData.getColumns() != null) {
            for (ColumnDefinition column : resourceData.getColumns()) {
                query.append(", ");
                query.append(column);

                additionalColumns.add(column.getName());
            }
        }

        // add the label field
        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        if (labelField != null && !additionalColumns.contains(labelField)) {
            query.append(", ");
            query.append(labelField);
        }

        // add the shading field
        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        if (shadingField != null && !additionalColumns.contains(shadingField)) {
            query.append(", ");
            query.append(shadingField);
        }

        // add the geometry table
        query.append(" FROM ");
        query.append(resourceData.getTable());

        // add the geospatial constraint
        query.append(" WHERE ");
        query.append(getGeospatialConstraint(geometryField, env));

        // add any additional constraints
        if (resourceData.getConstraints() != null) {
            for (String constraint : resourceData.getConstraints()) {
                query.append(" AND ");
                query.append(constraint);
            }
        }

        query.append(';');

        return query.toString();
    }

    protected String getGeomField(double simpLev) {
        DecimalFormat df = new DecimalFormat("0.######");
        String suffix = "_"
                + StringUtils.replaceChars(df.format(simpLev), '.', '_');

        return resourceData.getGeomField() + suffix;
    }

    /**
     * @return
     */
    protected Object getGeospatialConstraint(String geometryField, Envelope env) {
        // create the geospatial constraint from the envelope
        String geoConstraint = String.format(
                "%s && ST_SetSrid('BOX3D(%f %f, %f %f)'::box3d,4326)",
                geometryField, env.getMinX(), env.getMinY(), env.getMaxX(),
                env.getMaxY());
        return geoConstraint;
    }

    /**
     * @param dpp
     * @return
     */
    protected double getSimpLev(double dpp) {
        double[] levels = getLevels();
        double simpLev = levels[0];
        for (double level : getLevels()) {
            if (dpp < level) {
                break;
            }
            simpLev = level;
        }
        // System.out.println("dpp: " + dpp + ", simpLev: " + simpLev);
        return simpLev;
    }

    @Override
    protected void paintInternal(IGraphicsTarget aTarget,
            PaintProperties paintProps) throws VizException {
        PixelExtent screenExtent = (PixelExtent) paintProps.getView()
                .getExtent();

        // compute an estimate of degrees per pixel
        double yc = screenExtent.getCenter()[1];
        double x1 = screenExtent.getMinX();
        double x2 = screenExtent.getMaxX();
        double[] c1 = descriptor.pixelToWorld(new double[] { x1, yc });
        double[] c2 = descriptor.pixelToWorld(new double[] { x2, yc });
        Rectangle canvasBounds = paintProps.getCanvasBounds();
        int screenWidth = canvasBounds.width;
        double dppX = Math.abs(c2[0] - c1[0]) / screenWidth;
        // System.out.println("c1:" + Arrays.toString(c1) + "  c2:"
        // + Arrays.toString(c2) + "  dpp:" + dppX);

        double simpLev = getSimpLev(dppX);

        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        boolean isLabeled = labelField != null;

        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        // System.out.println("shadingField: " + shadingField);
        boolean isShaded = isPolygonal() && shadingField != null;

        if (simpLev < lastSimpLev
                || (isLabeled && !labelField.equals(lastLabelField))
                || (isShaded && !shadingField.equals(lastShadingField))
                || lastExtent == null
                || !lastExtent.getEnvelope().contains(
                        clipToProjExtent(screenExtent).getEnvelope())) {
            if (!paintProps.isZooming()) {
                PixelExtent expandedExtent = getExpandedExtent(screenExtent);
                String query = buildQuery(expandedExtent);
                queryJob.request(aTarget, descriptor, this, query,
                        getGeomField(simpLev), labelField, shadingField,
                        colorMap);
                lastExtent = expandedExtent;
                lastSimpLev = simpLev;
                lastLabelField = labelField;
                lastShadingField = shadingField;
            }
        }

        MapQueryJob.Result result = queryJob.getLatestResult();
        if (result != null) {
            if (result.failed) {
                lastExtent = null; // force to re-query when re-enabled
                throw new VizException("Error processing map query request: "
                        + result.query, result.cause);
            }
            if (outlineShape != null) {
                outlineShape.dispose();
            }

            if (shadedShape != null) {
                shadedShape.dispose();
            }
            outlineShape = result.outlineShape;
            labels = result.labels;
            shadedShape = result.shadedShape;
            colorMap = result.colorMap;
        }

        float alpha = paintProps.getAlpha();

        if (shadedShape != null && shadedShape.isDrawable() && isShaded) {
            aTarget.drawShadedShape(shadedShape, alpha);
        }

        if (outlineShape != null && outlineShape.isDrawable()
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            aTarget.drawWireframeShape(outlineShape,
                    getCapability(ColorableCapability.class).getColor(),
                    getCapability(OutlineCapability.class).getOutlineWidth(),
                    getCapability(OutlineCapability.class).getLineStyle());
        } else if (outlineShape == null
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            issueRefresh();
        }

        double labelMagnification = getCapability(MagnificationCapability.class)
                .getMagnification();

        if (labels != null && isLabeled && labelMagnification != 0) {
            if (font == null) {
                font = aTarget
                        .initializeFont(aTarget.getDefaultFont().getFontName(),
                                (float) (10 * labelMagnification), null);
                font.setSmoothing(false);
            }
            double screenToWorldRatio = paintProps.getView().getExtent()
                    .getWidth()
                    / paintProps.getCanvasBounds().width;

            double offsetX = getCapability(LabelableCapability.class)
                    .getxOffset() * screenToWorldRatio;
            double offsetY = getCapability(LabelableCapability.class)
                    .getyOffset() * screenToWorldRatio;
            RGB color = getCapability(ColorableCapability.class).getColor();
            IExtent extent = paintProps.getView().getExtent();
            List<DrawableString> strings = new ArrayList<DrawableString>(
                    labels.size());
            List<LabelNode> selectedNodes = new ArrayList<LabelNode>(
                    labels.size());
            List<IExtent> extents = new ArrayList<IExtent>();
            String lastLabel = null;
            // get min distance
            double density = this.getCapability(DensityCapability.class)
                    .getDensity();
            double minScreenDistance = Double.MAX_VALUE;
            if (density > 0) {
                minScreenDistance = screenToWorldRatio * BASE_DENSITY_MULT
                        / density;
            }

            // find which nodes to draw
            for (LabelNode node : labels) {
                if (extent.contains(node.location)) {
                    if (shouldDraw(node, selectedNodes, minScreenDistance)) {
                        selectedNodes.add(node);
                    }
                }
            }

            // create drawable strings for selected nodes
            for (LabelNode node : selectedNodes) {
                DrawableString string = new DrawableString(node.label, color);
                string.setCoordinates(node.location[0] + offsetX,
                        node.location[1] - offsetY);
                string.font = font;
                string.horizontalAlignment = HorizontalAlignment.CENTER;
                string.verticallAlignment = VerticalAlignment.MIDDLE;
                boolean add = true;

                IExtent strExtent = new PixelExtent(
                        node.location[0],
                        node.location[0]
                                + (node.rect.getWidth() * screenToWorldRatio),
                        node.location[1],
                        node.location[1]
                                + ((node.rect.getHeight() - node.rect.getY()) * screenToWorldRatio));

                if (lastLabel != null && lastLabel.equals(node.label)) {
                    // check intersection of extents
                    for (IExtent ext : extents) {
                        if (ext.intersects(strExtent)) {
                            add = false;
                            break;
                        }
                    }
                } else {
                    extents.clear();
                }
                lastLabel = node.label;
                extents.add(strExtent);

                if (add) {
                    strings.add(string);
                }
            }

            aTarget.drawStrings(strings);
        }
    }

    /**
     * checks if the potentialNode has the same text AND is to close to an
     * already selected node
     * 
     * @param potentialNode
     * @param selectedDrawList
     * @param minScreenDistance
     * @return
     */
    protected boolean shouldDraw(LabelNode potentialNode,
            List<LabelNode> selectedDrawList, double minScreenDistance) {
        boolean rval = false;

        // String label = potentialNode.getLabel();
        double x = potentialNode.getLocation()[0];
        double y = potentialNode.getLocation()[1];
        double minDistance = Double.MAX_VALUE;

        // check already selected labels
        for (LabelNode node : selectedDrawList) {
            // if (!node.getLabel().equals(label)) {
            // continue;
            // }
            double distance = Math.abs(node.getLocation()[0] - x)
                    + Math.abs(node.getLocation()[1] - y);
            minDistance = Math.min(distance, minDistance);
        }

        if (minDistance >= minScreenDistance) {
            rval = true;
        } else {
            rval = false;
        }

        return rval;
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        super.project(crs);

        if (this.outlineShape != null) {
            outlineShape.dispose();
            this.outlineShape = null;
        }

        if (this.shadedShape != null) {
            shadedShape.dispose();
            this.shadedShape = null;
        }
    }

    /**
     * @return the levels
     */
    protected double[] getLevels() {
        if (levels == null) {
            try {
                int p = resourceData.getTable().indexOf('.');
                String schema = resourceData.getTable().substring(0, p);
                String table = resourceData.getTable().substring(p + 1);
                StringBuilder query = new StringBuilder(
                        "SELECT f_geometry_column FROM public.geometry_columns WHERE f_table_schema='");
                query.append(schema);
                query.append("' AND f_table_name='");
                query.append(table);
                query.append("' AND f_geometry_column LIKE '");
                query.append(resourceData.getGeomField());
                query.append("_%';");
                List<Object[]> results = MapQueryCache.executeQuery(
                        query.toString(), "maps", QueryLanguage.SQL);

                levels = new double[results.size()];
                int i = 0;
                for (Object[] objs : results) {
                    String s = ((String) objs[0]).substring(
                            resourceData.getGeomField().length() + 1).replace(
                            '_', '.');
                    levels[i++] = Double.parseDouble(s);
                }
                Arrays.sort(levels);
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying available levels", e);
            }
        }

        return levels;
    }

    protected String getGeometryType() {
        if (geometryType == null) {
            try {
                int p = resourceData.getTable().indexOf('.');
                String schema = resourceData.getTable().substring(0, p);
                String table = resourceData.getTable().substring(p + 1);
                StringBuilder query = new StringBuilder(
                        "SELECT type FROM geometry_columns WHERE f_table_schema='");
                query.append(schema);
                query.append("' AND f_table_name='");
                query.append(table);
                query.append("' LIMIT 1;");
                List<Object[]> results = MapQueryCache.executeQuery(
                        query.toString(), "maps", QueryLanguage.SQL);

                geometryType = (String) results.get(0)[0];
            } catch (Throwable e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying geometry type", e);
            }
        }

        return geometryType;
    }

    protected boolean isLineal() {
        return getGeometryType().endsWith("LINESTRING");
    }

    protected boolean isPolygonal() {
        return getGeometryType().endsWith("POLYGON");
    }
}
