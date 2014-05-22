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
package com.raytheon.uf.viz.gisdatastore.rsc;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.geospatial.util.WorldWrapCorrector;
import com.raytheon.uf.common.status.IPerformanceStatusHandler;
import com.raytheon.uf.common.status.PerformanceStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.TextStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.drawables.IFont;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.DensityCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.LabelableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.MagnificationCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.OutlineCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ShadeableCapability;
import com.raytheon.uf.viz.gisdatastore.Activator;
import com.raytheon.uf.viz.gisdatastore.ui.PreferenceConverter;
import com.raytheon.viz.ui.input.InputAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Resource to render data from a GeoTools DataStore
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2012      #1326 randerso     Initial creation
 * Feb 22, 2013      #1641 randerso     Moved ID_ATTRIBUTE_NAME to package scope
 * Jul 24, 2013      #1907 randerso     Fixed sampling when cropped
 * Jul 24, 2013      #1908 randerso     Update attributes when cropped
 * Feb 18, 2014      #2819 randerso     Removed unnecessary clones of geometries
 * Mar 11, 2014      #2718 randerso     Changes for GeoTools 10.5
 * Mar 25, 2014      #2664 randerso     Added support for non-WGS84 shape files
 * Apr 14, 2014      #2664 randerso     Fix NullPointerException when no .prj file present
 * Apr 21, 2014      #2998 randerso     Stored types of attributes to be used in the AttributeViewer
 * 
 * </pre>
 * 
 * @author randerso
 * @version 1.0
 */

public class DataStoreResource extends
        AbstractVizResource<DataStoreResourceData, MapDescriptor> implements
        IPropertyChangeListener, IResourceDataChanged {
    private static final IPerformanceStatusHandler perfLog = PerformanceStatus
            .getHandler("GIS:");

    static final String ID_ATTRIBUTE_NAME = "Feature.ID";

    /**
     * Non-polygonal geometry within CLICK_TOLERANCE screen pixels of the cursor
     * will be selected when double clicking in the map.
     * 
     * 10 pixels was selected as it felt about right when trying to select
     * individual points in a point based shape file
     */
    private static final int CLICK_TOLERANCE = 10;

    private static final RGB RUBBER_BAND_COLOR = new RGB(0, 255, 0);

    /**
     * The screen will be re-centered about the centroid of the selected feature
     * only if the centroid lies outside the central portion of the screen as
     * defined by this value.
     */
    private static final double RECENTER_TOLERANCE = 0.8;

    /** Property Key for highlight color */
    public static final String HIGHLIGHT_COLOR_KEY = "HighlightColor";

    /** Default value for highlight color */
    public static final String HIGHLIGHT_COLOR_DEFAULT = "HotPink";

    /** Property Key for highlight style */
    public static final String HIGHLIGHT_STYLE_KEY = "HighlightStyle";

    /** Default value for highlight style */
    public static final String HIGHLIGHT_STYLE_DEFAULT = "SOLID";

    /** Property Key for highlight width */
    public static final String HIGHLIGHT_WIDTH_KEY = "HighlightWidth";

    /** Default value for highlight width */
    public static final String HIGHLIGHT_WIDTH_DEFAULT = "2";

    /** Property Key for product opacity */
    public static final String PRODUCT_OPACITY_KEY = "ProductOpacity";

    /** Default value for product opacity */
    public static final String PRODUCT_OPACITY_DEFAULT = "0.35";

    protected static final double EXPANSION_FACTOR = 0.25;

    /**
     * at time of writing this is the density multiplier used to determine if a
     * label should be drawn in ZoneSelectorResource
     */
    private static final int BASE_DENSITY_MULT = 50;

    protected static class LabelNode {
        private final Rectangle2D rect;

        private final String label;

        private final double[] location;

        public LabelNode(String label, double[] location, Rectangle2D rect) {
            this.label = label;
            this.location = location;
            this.rect = rect;
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

    static class DisplayAttributes {
        private boolean visible;

        private boolean highlighted;

        private Point centroid;

        public DisplayAttributes() {
            visible = true;
            highlighted = false;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isHighlighted() {
            return highlighted;
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

        public Point getCentroid() {
            return centroid;
        }

        public void setCentroid(Point centroid) {
            this.centroid = centroid;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isVisible()) {
                if (isHighlighted()) {
                    sb.append("Highlighted");
                } else {
                    sb.append("Visible");
                }
            } else {
                sb.append("Invisible");
            }

            return sb.toString();
        }

    }

    private class MouseHandler extends InputAdapter {
        private Coordinate start;

        private Coordinate end;

        private boolean inDrag;

        @Override
        public boolean handleMouseMove(int x, int y) {
            if (rubberBandExtent != null) {
                IDisplayPaneContainer container = getResourceContainer();
                Coordinate c = container.translateClick(x, y);
                if (c != null) {
                    try {
                        dragPromptCoord = new ReferencedCoordinate(c)
                                .asPixel(getDescriptor().getGridGeometry());
                    } catch (Exception e) {
                        dragPromptCoord = null;
                    }
                } else {
                    dragPromptCoord = null;
                }
                issueRefresh();
            }
            return false;
        }

        @Override
        public boolean handleMouseDown(int x, int y, int mouseButton) {
            if (rubberBandExtent != null) {
                if (mouseButton == 1) {
                    IDisplayPaneContainer container = getResourceContainer();
                    Coordinate c = container.translateClick(x, y);
                    if (c != null) {
                        try {
                            start = new ReferencedCoordinate(c)
                                    .asPixel(getDescriptor().getGridGeometry());
                            end = start;
                            inDrag = true;
                            return true;
                        } catch (Exception e) {
                            start = null;
                            return false;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (rubberBandExtent != null) {
                if ((mouseButton == 1) && inDrag) {
                    IDisplayPaneContainer container = getResourceContainer();
                    Coordinate c = container.translateClick(x, y);
                    if (c != null) {
                        try {
                            end = new ReferencedCoordinate(c)
                                    .asPixel(getDescriptor().getGridGeometry());
                            updateRubberBandBox(start, end);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {
            if (rubberBandExtent != null) {
                if ((mouseButton == 1) && inDrag) {
                    IDisplayPaneContainer container = getResourceContainer();
                    Coordinate c = container.translateClick(x, y);
                    if (c != null) {
                        try {
                            end = new ReferencedCoordinate(c)
                                    .asPixel(getDescriptor().getGridGeometry());
                        } catch (Exception e) {
                            // use previous end value
                        }
                    }
                    inDrag = false;
                    updateRubberBandBox(start, end);
                    deactivateRubberBandBox();
                    return true;
                }
            }

            return super.handleMouseUp(x, y, mouseButton);
        }

        @Override
        public boolean handleDoubleClick(int x, int y, int button) {
            if (!doubleClickListeners.isEmpty()) {
                IDisplayPaneContainer container = getResourceContainer();
                Coordinate c = container.translateClick(x, y);

                try {
                    List<SimpleFeature> features = findFeatures(new ReferencedCoordinate(
                            c));
                    List<String> featureIds = new ArrayList<String>(
                            features.size());
                    for (SimpleFeature feature : features) {
                        featureIds.add(feature.getID());
                    }
                    for (Object obj : doubleClickListeners.getListeners()) {
                        IDoubleClickSelectionListener listener = (IDoubleClickSelectionListener) obj;
                        listener.selectedFeaturesChanged(featureIds);
                    }

                } catch (VizException e) {
                    // ignore click
                }
            }

            return super.handleDoubleClick(x, y, button);
        }
    }

    /**
     * Interface for listeners to be notified of rubber band selection changes
     */
    public static interface IRubberBandSelectionListener {
        /**
         * Called when rubber band selection changes
         * 
         * @param extent
         *            selected pixel extent
         */
        public void rubberBandSelectionChanged(PixelExtent extent);
    }

    /**
     * Interface for listeners to be notified of double click selection
     */
    public static interface IDoubleClickSelectionListener {
        /**
         * Called when double click selection occurs
         * 
         * @param selectedIds
         *            IDs of selected features
         */
        public void selectedFeaturesChanged(List<String> selectedIds);
    }

    /**
     * Interface for listeners to be notified when attributes are updated
     */
    public static interface IAttributesUpdatedListener {
        /**
         * Called when attributes are updated
         */
        public void attributesUpdated();
    }

    protected DataStore dataStore;

    private String typeName;

    private SimpleFeatureType schema;

    private MathTransform incomingToLatLon;

    private MathTransform latLonToIncoming;

    /**
     * The valid time range for this resource. If null resource is time
     * agnostic.
     */
    private TimeRange timeRange;

    // Intentionally declaring as a LinkedHashMap
    // to ensure insertion order is preserved.
    private LinkedHashMap<String, Class<?>> attrTypeMap;

    private Object[][] attributes;

    protected Map<String, DisplayAttributes> displayAttributes;

    protected IWireframeShape outlineShape;

    protected IWireframeShape highlightShape;

    protected IFont font;

    protected PixelExtent lastExtent;

    protected PixelExtent projExtent;

    protected List<LabelNode> labels;

    protected IShadedShape shadedShape;

    protected Map<Object, RGB> colorMap;

    protected String lastLabelField;

    protected String lastShadingField;

    private ReloadJob reloadJob;

    protected String geometryType;

    private String displayName;

    private RGB highlightColor;

    private LineStyle highlightStyle;

    private int highlightWidth;

    private boolean updateHighlights;

    private PixelExtent rubberBandExtent;

    private PixelExtent cropExtent;

    private MouseHandler mouseHandler;

    private ListenerList rubberBandListeners;

    private ListenerList doubleClickListeners;

    private ListenerList attributesUpdatedListeners;

    private Coordinate dragPromptCoord;

    private WorldWrapCorrector worldWrapCorrector;

    private String sampleAttribute;

    private Geometry boundingGeom;

    /**
     * Constructor for DataStoreResource
     * 
     * @param data
     * @param loadProperties
     * @throws IOException
     */
    public DataStoreResource(DataStoreResourceData data,
            LoadProperties loadProperties) throws IOException {
        super(data, loadProperties);
        reloadJob = new ReloadJob();

        this.dataStore = data.getDataStore();
        this.typeName = data.getTypeName();
        this.displayName = data.getMapName();
        this.rubberBandListeners = new ListenerList();
        this.doubleClickListeners = new ListenerList();
        this.attributesUpdatedListeners = new ListenerList();

        GeneralEnvelope env = new GeneralEnvelope(MapUtil.LATLON_PROJECTION);
        env.setEnvelope(-180.0, -90.0, 180.0, 90.0);

        GridGeometry2D latLonGridGeometry = new GridGeometry2D(
                new GeneralGridEnvelope(new int[] { 0, 0 }, new int[] { 360,
                        180 }, false), env);
        this.worldWrapCorrector = new WorldWrapCorrector(latLonGridGeometry);
        resourceData.addChangeListener(this);
        mouseHandler = new MouseHandler();
    }

    @Override
    protected void disposeInternal() {

        if (font != null) {
            font.dispose();
            font = null;
        }

        if (outlineShape != null) {
            outlineShape.dispose();
        }

        if (shadedShape != null) {
            shadedShape.dispose();
        }

        if (highlightShape != null) {
            highlightShape.dispose();
        }

        // ensure mouseHandler is unregistered
        if (mouseHandler != null) {
            getResourceContainer().unregisterMouseHandler(mouseHandler);
        }

        lastExtent = null;

        this.resourceData.disposeDataStore();
        this.reloadJob.cancel();
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {

        loadDataStore();

        String[] names = this.getAttributeNames();
        getCapability(LabelableCapability.class).setAvailableLabelFields(names);

        getCapability(ShadeableCapability.class).setAvailableShadingFields(
                names);

        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        if (this.timeRange != null) {
            float opacity = PreferenceConverter.getFloat(prefs,
                    PRODUCT_OPACITY_KEY, PRODUCT_OPACITY_DEFAULT);
            getCapability(ShadeableCapability.class).setOpacity(opacity);
        }

        highlightColor = PreferenceConverter.getRGB(prefs, HIGHLIGHT_COLOR_KEY,
                HIGHLIGHT_COLOR_DEFAULT);

        highlightStyle = PreferenceConverter.getLineStyle(prefs,
                HIGHLIGHT_STYLE_KEY, HIGHLIGHT_STYLE_DEFAULT);

        highlightWidth = PreferenceConverter.getInt(prefs, HIGHLIGHT_WIDTH_KEY,
                HIGHLIGHT_WIDTH_DEFAULT);

        prefs.addPropertyChangeListener(this);
        getResourceContainer().registerMouseHandler(mouseHandler);
    }

    protected void loadDataStore() throws VizException {
        try {
            ITimer timer = TimeUtil.getTimer();
            timer.start();

            schema = dataStore.getSchema(typeName);
            CoordinateReferenceSystem incomingCrs = schema
                    .getGeometryDescriptor().getCoordinateReferenceSystem();
            if (incomingCrs == null) {
                statusHandler.warn("No projection information found for "
                        + dataStore.getFeatureSource(typeName).getName()
                                .getURI()
                        + ", assuming WGS84 unprojected lat/lon.");
                incomingCrs = MapUtil.getLatLonProjection();
            }
            incomingToLatLon = MapUtil.getTransformToLatLon(incomingCrs);
            latLonToIncoming = MapUtil.getTransformFromLatLon(incomingCrs);

            List<AttributeDescriptor> attrDesc = schema
                    .getAttributeDescriptors();

            if (attrDesc == null) {
                attrTypeMap = new LinkedHashMap<String, Class<?>>(1);
                attrTypeMap.put(ID_ATTRIBUTE_NAME, Integer.class);
            } else {

                attrTypeMap = new LinkedHashMap<String, Class<?>>(
                        attrDesc.size(), 1.0f);
                attrTypeMap.put(ID_ATTRIBUTE_NAME, Integer.class);
                for (AttributeDescriptor at : attrDesc) {
                    Class<?> atType = at.getType().getBinding();
                    if (!Geometry.class.isAssignableFrom(atType)) {
                        attrTypeMap.put(at.getLocalName(), atType);
                    }
                }
            }

            timer.stop();
            perfLog.logDuration("loadDataStore", timer.getElapsedTime());
        } catch (Exception e) {
            throw new VizException("Error loading shapefile: ", e);
        }
    }

    private void loadAttributes() throws FactoryException,
            MismatchedDimensionException, TransformException {
        ITimer timer = TimeUtil.getTimer();
        timer.start();
        Query query = new Query();
        query.setTypeName(typeName);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
                .getDefaultHints());

        String geomField = schema.getGeometryDescriptor().getLocalName();

        IExtent worldExtent = getDescriptor().getRenderableDisplay().getView()
                .getExtent();
        Rectangle screenBounds = getResourceContainer().getActiveDisplayPane()
                .getBounds();
        double worldToScreenRatio = worldExtent.getWidth() / screenBounds.width;

        List<Geometry> geomList = new ArrayList<Geometry>();
        PixelExtent extent = clipToProjExtent(projExtent);
        Geometry boundingGeom = JTS.transform(
                buildBoundingGeometry(extent, worldToScreenRatio),
                latLonToIncoming);
        flattenGeometry(boundingGeom, geomList);

        List<Filter> filterList = new ArrayList<Filter>(geomList.size());
        for (Geometry g : geomList) {
            Filter filter = ff
                    .intersects(ff.property(geomField), ff.literal(g));
            filterList.add(filter);
        }
        query.setFilter(ff.or(filterList));

        SimpleFeatureCollection featureCollection = null;
        SimpleFeatureIterator featureIterator = null;
        try {
            SimpleFeatureSource featureSource = dataStore
                    .getFeatureSource(typeName);

            featureCollection = featureSource.getFeatures(query);

            int size = featureCollection.size();
            Set<String> attributeNames = attrTypeMap.keySet();
            attributes = new Object[size][attributeNames.size()];

            featureIterator = featureCollection.features();
            int i = 0;
            while (featureIterator.hasNext()) {
                int index = i++;
                SimpleFeature f = featureIterator.next();

                String id = f.getID();
                DisplayAttributes da = getDisplayAttributes(id);
                Geometry g = (Geometry) f.getAttribute(geomField);
                da.setCentroid((Point) JTS.transform(g.getCentroid(),
                        incomingToLatLon));

                int j = 0;
                for (String attrName : attributeNames) {
                    if (attrName.equals(ID_ATTRIBUTE_NAME)) {
                        attributes[index][j++] = id;
                    } else {
                        Object attr = f.getAttribute(attrName);
                        attributes[index][j++] = attr;
                    }
                }
            }
        } catch (Exception e) {
            Activator.statusHandler.handle(Priority.PROBLEM,
                    e.getLocalizedMessage(), e);
        } finally {
            if (featureIterator != null) {
                featureIterator.close();
            }
        }
        timer.stop();
        perfLog.logDuration("loadAttributes", timer.getElapsedTime());
    }

    private void flattenGeometry(Geometry geometry, List<Geometry> geomList) {
        if (geometry instanceof GeometryCollection) {
            GeometryCollection collection = (GeometryCollection) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                flattenGeometry(collection.getGeometryN(i), geomList);
            }
        } else {
            geomList.add(geometry);
        }
    }

    private Geometry buildBoundingGeometry(PixelExtent extent,
            double worldToScreenRatio) {

        double[] northPole = getDescriptor().worldToPixel(
                new double[] { 0, 90 });
        double[] southPole = getDescriptor().worldToPixel(
                new double[] { 0, -90 });

        double[] p = null;
        if ((northPole != null) && extent.contains(northPole)) {
            p = northPole;
        } else if ((southPole != null) && extent.contains(southPole)) {
            p = southPole;
        }

        double delta = 1 / worldToScreenRatio;

        Geometry g;
        if (p == null) {
            g = geometryFromExtent(extent, worldToScreenRatio);
        } else {
            // if pole in the extent split the extent into four quadrants with
            // corners at the pole
            PixelExtent[] quadrant = new PixelExtent[4];
            quadrant[0] = new PixelExtent(extent.getMinX(), p[0] - delta,
                    extent.getMinY(), p[1] - delta);
            quadrant[1] = new PixelExtent(p[0] + delta, extent.getMaxX(),
                    extent.getMinY(), p[1] - delta);
            quadrant[2] = new PixelExtent(p[0] + delta, extent.getMaxX(), p[1]
                    + delta, extent.getMaxY());
            quadrant[3] = new PixelExtent(extent.getMinX(), p[0] - delta, p[1]
                    + delta, extent.getMaxY());

            List<Geometry> geometries = new ArrayList<Geometry>(4);
            for (PixelExtent ext : quadrant) {
                if ((ext.getWidth() > 0) && (ext.getHeight() > 0)) {
                    geometries.add(geometryFromExtent(ext, worldToScreenRatio));
                }
            }

            GeometryFactory gf = new GeometryFactory();
            g = gf.createGeometryCollection(geometries
                    .toArray(new Geometry[geometries.size()]));
        }

        MathTransform gridToCrs = getDescriptor().getGridGeometry()
                .getGridToCRS();
        DefaultMathTransformFactory dmtf = new DefaultMathTransformFactory();
        try {
            MathTransform crsToLatLon = MapUtil
                    .getTransformToLatLon(getDescriptor().getCRS());
            MathTransform gridToLatLon = dmtf.createConcatenatedTransform(
                    gridToCrs, crsToLatLon);
            g = JTS.transform(g, gridToLatLon);

            // correct for world wrap
            g = this.worldWrapCorrector.correct(g);

            return g;
        } catch (Exception e) {
            Activator.statusHandler.handle(Priority.PROBLEM,
                    "Error computing bounding geometry", e);
        }
        return null;
    }

    private Geometry geometryFromExtent(PixelExtent extent,
            double worldToScreenRatio) {
        int nx = (int) Math.ceil(extent.getWidth() / worldToScreenRatio);
        int ny = (int) Math.ceil(extent.getHeight() / worldToScreenRatio);

        double dx = extent.getWidth() / nx;
        double dy = extent.getHeight() / ny;

        Coordinate[] coordinates = new Coordinate[(2 * (nx + ny)) + 1];
        int i = 0;
        for (int x = 0; x < nx; x++) {
            coordinates[i++] = new Coordinate((x * dx) + extent.getMinX(),
                    extent.getMinY());
        }
        for (int y = 0; y < ny; y++) {
            coordinates[i++] = new Coordinate(extent.getMaxX(), (y * dy)
                    + extent.getMinY());
        }
        for (int x = nx; x > 0; x--) {
            coordinates[i++] = new Coordinate((x * dx) + extent.getMinX(),
                    extent.getMaxY());
        }
        for (int y = ny; y > 0; y--) {
            coordinates[i++] = new Coordinate(extent.getMinX(), (y * dy)
                    + extent.getMinY());
        }
        coordinates[i++] = coordinates[0];

        GeometryFactory gf = new GeometryFactory();
        LinearRing shell = gf.createLinearRing(coordinates);
        Geometry g = gf.createPolygon(shell, null);
        return g;
    }

    @Override
    protected void paintInternal(IGraphicsTarget aTarget,
            PaintProperties paintProps) throws VizException {
        PixelExtent screenExtent = (PixelExtent) paintProps.getView()
                .getExtent();

        // determine if we should display in this frame
        DataTime curTime = paintProps.getFramesInfo().getCurrentFrame();
        if ((curTime != null) && (timeRange != null)) {
            if (timeRange.isValid()) { // non-zero duration
                if (!timeRange.contains(curTime.getValidTime().getTime())) {
                    return;
                }
            } else {
                if (!timeRange.getStart().equals(
                        curTime.getValidTime().getTime())) {
                    return;
                }
            }
        }

        double worldToScreenRatio = paintProps.getView().getExtent().getWidth()
                / paintProps.getCanvasBounds().width;

        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        boolean isLabeled = labelField != null;

        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        boolean isShaded = isPolygonal() && (shadingField != null);
        boolean isProduct = this.timeRange != null;

        boolean updateLabels = isLabeled && !labelField.equals(lastLabelField);
        boolean updateShading = false;
        if (isShaded || isProduct) {
            if (shadingField == null) {
                updateShading = lastShadingField != null;
            } else {
                updateShading = !shadingField.equals(lastShadingField);
            }
        }
        boolean updateExtent = (lastExtent == null)
                || !lastExtent.getEnvelope().contains(
                        clipToProjExtent(screenExtent).getEnvelope());

        if (updateHighlights || updateLabels || updateShading || updateExtent) {
            if (!paintProps.isZooming()) {
                PixelExtent expandedExtent = getExpandedExtent(screenExtent);
                try {
                    boundingGeom = JTS.transform(
                            buildBoundingGeometry(expandedExtent,
                                    worldToScreenRatio), latLonToIncoming);
                } catch (Exception e) {
                    throw new VizException(e.getLocalizedMessage(), e);
                }

                String geomField = schema.getGeometryDescriptor()
                        .getLocalName();

                boolean highlightsOnly = updateHighlights && !updateLabels
                        && !updateShading && !updateExtent;
                reloadJob.request(aTarget, this, boundingGeom, geomField,
                        labelField, shadingField, colorMap, isProduct,
                        highlightsOnly);
                lastExtent = expandedExtent;
                lastLabelField = labelField;
                lastShadingField = shadingField;
                updateHighlights = false;
            }
        }

        ReloadJob.Result result = reloadJob.getLatestResult();
        if (result != null) {
            handleResult(result);
        }

        float alpha = paintProps.getAlpha();

        if ((isProduct || isShaded) && (shadedShape != null)
                && shadedShape.isDrawable()) {
            float opacity = getCapability(ShadeableCapability.class)
                    .getOpacity();
            aTarget.drawShadedShape(shadedShape, alpha * opacity);
        }

        if ((outlineShape != null) && outlineShape.isDrawable()
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            aTarget.drawWireframeShape(outlineShape,
                    getCapability(ColorableCapability.class).getColor(),
                    getCapability(OutlineCapability.class).getOutlineWidth(),
                    getCapability(OutlineCapability.class).getLineStyle(),
                    alpha);
        } else if ((outlineShape == null)
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            issueRefresh();
        }

        if ((highlightShape != null) && highlightShape.isDrawable()
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            aTarget.drawWireframeShape(highlightShape, highlightColor,
                    highlightWidth, highlightStyle);
        }

        double labelMagnification = getCapability(MagnificationCapability.class)
                .getMagnification();

        if ((labels != null) && isLabeled && (labelMagnification != 0)) {
            drawLabels(aTarget, paintProps, labelMagnification,
                    worldToScreenRatio);
        }

        if (rubberBandExtent != null) {
            aTarget.drawShadedRect(rubberBandExtent, RUBBER_BAND_COLOR, 0.5,
                    null);
            aTarget.drawRect(rubberBandExtent, RUBBER_BAND_COLOR, 2, 1.0);
        }

        if (dragPromptCoord != null) {
            DrawableString ds = new DrawableString("Drag to select", new RGB(0,
                    0, 0));
            ds.setCoordinates(dragPromptCoord.x, dragPromptCoord.y);
            ds.textStyle = TextStyle.BOXED;
            ds.boxColor = new RGB(255, 255, 255);
            ds.horizontalAlignment = HorizontalAlignment.LEFT;
            ds.verticallAlignment = VerticalAlignment.BOTTOM;
            aTarget.drawStrings(ds);
        }
    }

    private void handleResult(ReloadJob.Result result) throws VizException {
        if (result.failed) {
            lastExtent = null; // force to re-query when re-enabled
            throw new VizException("Error processing map query request: ",
                    result.cause);
        }
        if (!result.highlightsOnly) {
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

        if (highlightShape != null) {
            highlightShape.dispose();
        }
        highlightShape = result.highlightShape;
    }

    private void drawLabels(IGraphicsTarget aTarget,
            PaintProperties paintProps, double labelMagnification,
            double worldToScreenRatio) throws VizException {
        if (font == null) {
            font = aTarget.initializeFont(aTarget.getDefaultFont()
                    .getFontName(), (float) (10 * labelMagnification), null);
            font.setSmoothing(false);
        }

        double offsetX = getCapability(LabelableCapability.class).getxOffset()
                * worldToScreenRatio;
        double offsetY = getCapability(LabelableCapability.class).getyOffset()
                * worldToScreenRatio;
        RGB color = getCapability(ColorableCapability.class).getColor();
        IExtent extent = paintProps.getView().getExtent();
        List<DrawableString> strings = new ArrayList<DrawableString>(
                labels.size());
        List<LabelNode> selectedNodes = new ArrayList<LabelNode>(labels.size());
        List<IExtent> extents = new ArrayList<IExtent>();
        String lastLabel = null;
        // get min distance
        double density = this.getCapability(DensityCapability.class)
                .getDensity();
        double minScreenDistance = Double.MAX_VALUE;
        if (density > 0) {
            minScreenDistance = (worldToScreenRatio * BASE_DENSITY_MULT)
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
            string.setCoordinates(node.location[0] + offsetX, node.location[1]
                    - offsetY);
            string.font = font;
            string.horizontalAlignment = HorizontalAlignment.CENTER;
            string.verticallAlignment = VerticalAlignment.MIDDLE;
            boolean add = true;

            IExtent strExtent = new PixelExtent(
                    node.location[0],
                    node.location[0]
                            + (node.rect.getWidth() * worldToScreenRatio),
                    node.location[1],
                    node.location[1]
                            + ((node.rect.getHeight() - node.rect.getY()) * worldToScreenRatio));

            if ((lastLabel != null) && lastLabel.equals(node.label)) {
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractVizResource#setDescriptor(com.raytheon
     * .uf.viz.core.drawables.IDescriptor)
     */
    @Override
    public void setDescriptor(MapDescriptor descriptor) {
        super.setDescriptor(descriptor);

        projExtent = new PixelExtent(descriptor.getGridGeometry()
                .getGridRange());

        lastExtent = null;
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        super.project(crs);

        projExtent = new PixelExtent(descriptor.getGridGeometry()
                .getGridRange());

        lastExtent = null;

        if (this.outlineShape != null) {
            outlineShape.dispose();
            this.outlineShape = null;
        }

        if (this.shadedShape != null) {
            shadedShape.dispose();
            this.shadedShape = null;
        }

        if (this.highlightShape != null) {
            this.highlightShape.dispose();
            this.highlightShape = null;
        }
    }

    @Override
    public ResourceOrder getResourceOrder() {
        String orderId = (this.getProperties().isMapLayer() ? "MAP_OUTLINE"
                : "IMAGE_LOCAL");
        return RenderingOrderFactory.getRenderingOrder(orderId);
    }

    /**
     * @param screenExtent
     * @return
     */
    protected PixelExtent getExpandedExtent(PixelExtent screenExtent) {
        PixelExtent expandedExtent = screenExtent.clone();
        expandedExtent.getEnvelope().expandBy(
                expandedExtent.getWidth() * EXPANSION_FACTOR,
                expandedExtent.getHeight() * EXPANSION_FACTOR);

        return clipToProjExtent(expandedExtent);
    }

    protected PixelExtent clipToProjExtent(PixelExtent extent) {
        Envelope e = extent.getEnvelope()
                .intersection(projExtent.getEnvelope());

        if (cropExtent != null) {
            e = e.intersection(cropExtent.getEnvelope());
        }

        PixelExtent clipped = new PixelExtent(e.getMinX(), e.getMaxX(),
                e.getMinY(), e.getMaxY());
        return clipped;
    }

    protected String getGeometryType() {
        if (geometryType == null) {
            geometryType = schema.getGeometryDescriptor().getType()
                    .getBinding().getSimpleName();
        }

        return geometryType;
    }

    protected boolean isPuntal() {
        return getGeometryType().endsWith("Point");
    }

    protected boolean isLineal() {
        return getGeometryType().endsWith("LineString");
    }

    protected boolean isPolygonal() {
        return getGeometryType().endsWith("Polygon");
    }

    @Override
    public String getName() {
        if (this.displayName == null) {
            return this.typeName;
        }
        return this.displayName;
    }

    @Override
    public Map<String, Object> interrogate(ReferencedCoordinate coord)
            throws VizException {
        Map<String, Object> data = null;
        return data;
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        String sampleString = null;

        // sampling is only supported when loaded as product
        if (timeRange != null) {
            if (sampleAttribute != null) {
                // TODO get the query off the UI thread
                List<SimpleFeature> features = findFeatures(coord);

                StringBuilder sb = new StringBuilder();
                for (SimpleFeature f : features) {
                    Object attr;
                    if (sampleAttribute.equals(ID_ATTRIBUTE_NAME)) {
                        attr = f.getID();
                    } else {
                        attr = f.getAttribute(sampleAttribute);
                    }
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(String.valueOf(attr));
                }
                sampleString = sb.toString();
            } else {
                sampleString = "No Sample Attribute Selected";
            }
        }
        return sampleString;
    }

    /**
     * Set the displayed resource name
     * 
     * @param name
     */
    public void setName(String name) {
        if ((name != null) && name.isEmpty()) {
            name = null;
        }
        this.displayName = name;
        this.resourceData.setMapName(name);
    }

    /**
     * @return the attribute names
     */
    public String[] getAttributeNames() {
        return attrTypeMap.keySet().toArray(new String[attrTypeMap.size()]);
    }

    /**
     * Get Java type of an attribute
     * 
     * @param attributeName
     *            name of the desired attribute
     * @return the type
     */
    public Class<?> getAttributeType(String attributeName) {
        return attrTypeMap.get(attributeName);
    }

    /**
     * @return the attributes
     */
    public Object[][] getAttributes() {
        if (attributes == null) {
            try {
                loadAttributes();
            } catch (Exception e) {
                statusHandler.error("Error loading attributes: ", e);
                attributes = new Object[0][0];
            }
        }
        return attributes;
    }

    DataStore getDataStore() {
        return dataStore;
    }

    String getTypeName() {
        return typeName;
    }

    SimpleFeatureType getSchema() {
        return schema;
    }

    /**
     * @return the incomingToLatLon
     */
    public MathTransform getIncomingToLatLon() {
        return incomingToLatLon;
    }

    /**
     * @return the latLonToIncoming
     */
    public MathTransform getLatLonToIncoming() {
        return latLonToIncoming;
    }

    /**
     * @param coord
     * @return
     * @throws VizException
     */
    private List<SimpleFeature> findFeatures(ReferencedCoordinate coord)
            throws VizException {
        // ITimer timer = TimeUtil.getTimer();
        // timer.start();

        Query query = new Query();
        query.setTypeName(typeName);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
                .getDefaultHints());

        String geomField = schema.getGeometryDescriptor().getLocalName();

        boolean polygonal = isPolygonal();
        Filter clickFilter;
        try {
            if (polygonal) {
                GeometryFactory gf = new GeometryFactory();
                Point clickPoint = gf.createPoint(JTS.transform(
                        coord.asLatLon(), null, latLonToIncoming));
                clickFilter = ff.contains(ff.property(geomField),
                        ff.literal(clickPoint));
            } else {
                Coordinate pix;
                pix = coord.asPixel(getDescriptor().getGridGeometry());

                IExtent worldExtent = getDescriptor().getRenderableDisplay()
                        .getView().getExtent();
                Rectangle screenBounds = getResourceContainer()
                        .getActiveDisplayPane().getBounds();
                double worldToScreenRatio = worldExtent.getWidth()
                        / screenBounds.width;

                double delta = CLICK_TOLERANCE * worldToScreenRatio;
                PixelExtent bboxExtent = new PixelExtent(pix.x - delta, pix.x
                        + delta, pix.y + delta, pix.y - delta);
                Geometry clickBox = JTS.transform(
                        buildBoundingGeometry(bboxExtent, worldToScreenRatio),
                        latLonToIncoming);
                List<Geometry> clickGeomList = new ArrayList<Geometry>();
                flattenGeometry(clickBox, clickGeomList);
                List<Filter> clickFilterList = new ArrayList<Filter>(
                        clickGeomList.size());
                for (Geometry g : clickGeomList) {
                    Filter filter = ff.intersects(ff.property(geomField),
                            ff.literal(g));
                    clickFilterList.add(filter);
                }
                clickFilter = ff.or(clickFilterList);
            }
        } catch (Exception e) {
            throw new VizException(
                    "Error transforming sample point to lat/lon ", e);
        }

        query.setFilter(clickFilter);

        SimpleFeatureCollection featureCollection = null;
        SimpleFeatureIterator featureIterator = null;
        List<SimpleFeature> features;
        try {
            SimpleFeatureSource featureSource = dataStore
                    .getFeatureSource(typeName);

            featureCollection = featureSource.getFeatures(query);
            features = new ArrayList<SimpleFeature>(featureCollection.size());
            featureIterator = featureCollection.features();

            while (featureIterator.hasNext()) {
                SimpleFeature f = featureIterator.next();
                Geometry g = (Geometry) f.getDefaultGeometry();
                if (g.intersects(boundingGeom)) {
                    features.add(f);
                }
            }
        } catch (Exception e) {
            Activator.statusHandler.handle(Priority.PROBLEM,
                    e.getLocalizedMessage(), e);
            features = Collections.emptyList();
        } finally {
            if (featureIterator != null) {
                featureIterator.close();
            }
        }

        // timer.stop();
        // perfLog.logDuration("findFeatures", timer.getElapsedTime());
        return features;
    }

    DisplayAttributes getDisplayAttributes(String id) {
        DisplayAttributes da = this.displayAttributes.get(id);
        if (da == null) {
            da = new DisplayAttributes();
            this.displayAttributes.put(id, da);
        }
        return da;
    }

    /**
     * Make a feature visible/invisible
     * 
     * @param id
     *            ID of feature to change
     * @param visible
     *            true for visible, false for invisible
     */
    public void setVisible(String id, boolean visible) {
        DisplayAttributes da = getDisplayAttributes(id);
        da.setVisible(visible);
        lastExtent = null;
        issueRefresh();
    }

    /**
     * Determine if a feature is visibile
     * 
     * @param id
     *            ID of feature in question
     * @return true if visible, false if invisible
     */
    public boolean isVisible(String id) {
        DisplayAttributes da = getDisplayAttributes(id);
        return da.isVisible();
    }

    /**
     * Highlight/Unhighlight a feature
     * 
     * @param id
     *            ID of feature to change
     * @param highlighted
     *            true for highlighted, false for unhighlighted
     */
    public void setHighlighted(String id, boolean highlighted) {
        DisplayAttributes da = getDisplayAttributes(id);
        da.setHighlighted(highlighted);
        updateHighlights = true;

        issueRefresh();
    }

    /**
     * Determine if a feature is highlighted
     * 
     * @param id
     *            ID of feature in question
     * @return true if highlighted, false if unhighlighted
     */
    public boolean isHighlighted(String id) {
        DisplayAttributes da = getDisplayAttributes(id);
        return da.isHighlighted();
    }

    /**
     * Recenter the display on a feature if it is off screen
     * 
     * @param id
     *            ID of feature to be recentered about
     */
    public void recenter(String id) {
        DisplayAttributes da = getDisplayAttributes(id);
        Point p = da.getCentroid();
        if (p != null) {
            double[] center = new double[] { p.getCoordinate().x,
                    p.getCoordinate().y };

            double[] pixel = this.getDescriptor().worldToPixel(center);

            IExtent extent = this.getDescriptor().getRenderableDisplay()
                    .getExtent().clone();
            extent.scale(RECENTER_TOLERANCE);
            if (!extent.contains(pixel)) {
                this.getDescriptor().getRenderableDisplay().recenter(center);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        boolean update = false;
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        if (event.getProperty().equals(HIGHLIGHT_COLOR_KEY)) {
            highlightColor = PreferenceConverter.getRGB(prefs,
                    HIGHLIGHT_COLOR_KEY, HIGHLIGHT_COLOR_DEFAULT);
            update = true;
        } else if (event.getProperty().equals(HIGHLIGHT_STYLE_KEY)) {
            highlightStyle = PreferenceConverter.getLineStyle(prefs,
                    HIGHLIGHT_STYLE_KEY, HIGHLIGHT_STYLE_DEFAULT);
            update = true;
        } else if (event.getProperty().equals(HIGHLIGHT_WIDTH_KEY)) {
            highlightWidth = PreferenceConverter.getInt(prefs,
                    HIGHLIGHT_WIDTH_KEY, HIGHLIGHT_WIDTH_DEFAULT);
            update = true;
        } else if (event.getProperty().equals(PRODUCT_OPACITY_KEY)) {
            float opacity = PreferenceConverter.getFloat(prefs,
                    PRODUCT_OPACITY_KEY, PRODUCT_OPACITY_DEFAULT);
            getCapability(ShadeableCapability.class).setOpacity(opacity);
        }

        if (update) {
            lastExtent = null;
            issueRefresh();
        }
    }

    /**
     * Activate rubber band selection box
     */
    public void activateRubberBandBox() {
        rubberBandExtent = new PixelExtent(0, 0, 0, 0);
    }

    /**
     * Update rubber band selection
     * 
     * @param start
     *            starting corner of selection
     * @param end
     *            ending corner of selection
     */
    public void updateRubberBandBox(Coordinate start, Coordinate end) {
        dragPromptCoord = end;
        double minX = Math.min(start.x, end.x);
        double maxX = Math.max(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double maxY = Math.max(start.y, end.y);
        rubberBandExtent = new PixelExtent(minX, maxX, minY, maxY);
        issueRefresh();
    }

    /**
     * Deactivate rubber band selection box
     */
    public void deactivateRubberBandBox() {
        if ((rubberBandExtent.getWidth() > 0)
                && (rubberBandExtent.getHeight() > 0)) {
            for (Object obj : rubberBandListeners.getListeners()) {
                IRubberBandSelectionListener listener = (IRubberBandSelectionListener) obj;
                listener.rubberBandSelectionChanged(rubberBandExtent);
            }
        }

        dragPromptCoord = null;
        rubberBandExtent = null;
        issueRefresh();
    }

    /**
     * Add a rubber band selection listener
     * 
     * @param listener
     */
    public void addRubberBandSelectionListener(
            IRubberBandSelectionListener listener) {
        rubberBandListeners.add(listener);
    }

    /**
     * Remove a rubber band selection listener
     * 
     * @param listener
     */
    public void removeRubberBandSelectionListener(
            IRubberBandSelectionListener listener) {
        rubberBandListeners.remove(listener);
    }

    /**
     * Add a double click selection listener
     * 
     * @param listener
     */
    public void addDoubleClickSelectionListener(
            IDoubleClickSelectionListener listener) {
        doubleClickListeners.add(listener);
    }

    /**
     * Remove a double click selection listener
     * 
     * @param listener
     */
    public void removeDoubleClickSelectionListener(
            IDoubleClickSelectionListener listener) {
        doubleClickListeners.remove(listener);
    }

    /**
     * Add an attributes updated listener
     * 
     * @param listener
     */
    public void addAttributesUpdatedListener(IAttributesUpdatedListener listener) {
        attributesUpdatedListeners.add(listener);
    }

    /**
     * Remove an attributes updated listener
     * 
     * @param listener
     */
    public void removeAttributesUpdatedListener(
            IAttributesUpdatedListener listener) {
        attributesUpdatedListeners.remove(listener);
    }

    private void notifyAttributesUpdatedListeners() {
        for (Object listener : attributesUpdatedListeners.getListeners()) {
            ((IAttributesUpdatedListener) listener).attributesUpdated();
        }
    }

    /**
     * Crop the displayed features to the selected extent
     * 
     * @param extent
     *            the selected extent
     */
    public void crop(PixelExtent extent) {
        cropExtent = extent;
        lastExtent = null;
        attributes = null;
        issueRefresh();
        notifyAttributesUpdatedListeners();
    }

    /**
     * Determine if the resource is cropped
     * 
     * @return true if cropped
     */
    public boolean isCropped() {
        return cropExtent != null;
    }

    /**
     * Uncrop the resource (display all features)
     */
    public void uncrop() {
        cropExtent = null;
        lastExtent = null;
        attributes = null;
        issueRefresh();
        notifyAttributesUpdatedListeners();
    }

    /**
     * Set the time range associated with the resource when displayed as a
     * product
     * 
     * @param timeRange
     */
    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    @Override
    public void resourceChanged(ChangeType type, Object object) {
        if (type.equals(ChangeType.CAPABILITY)) {
            if (object instanceof ColorableCapability) {
                if (this.timeRange != null) {
                    // force rebuild of shaded shape to pick up color change
                    lastExtent = null;
                }
            } else if (object instanceof MagnificationCapability) {
                if (font != null) {
                    font.dispose();
                    font = null;
                }
            }
        }
        issueRefresh();
    }

    /**
     * Get the name of the sample attribute
     * 
     * @return sample attribute name
     */
    public String getSampleAttribute() {
        return sampleAttribute;
    }

    /**
     * Set the sample attribute name
     * 
     * @param sampleAttribute
     */
    public void setSampleAttribute(String sampleAttribute) {
        this.sampleAttribute = sampleAttribute;
    }
}
