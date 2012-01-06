package gov.noaa.nws.ncep.viz.rsc.satellite.rsc;

import gov.noaa.nws.ncep.viz.common.ColorMapUtil;
import gov.noaa.nws.ncep.viz.common.ui.NmapCommon;
import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsResource;
import gov.noaa.nws.ncep.viz.resources.INatlCntrsResource;
import gov.noaa.nws.ncep.viz.resources.colorBar.ColorBarResource;
import gov.noaa.nws.ncep.viz.resources.colorBar.ColorBarResourceData;
import gov.noaa.nws.ncep.viz.rsc.satellite.units.NcSatelliteUnits;
import gov.noaa.nws.ncep.viz.ui.display.ColorBarFromColormap;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.quantity.Temperature;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridGeometry2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;

import com.raytheon.uf.common.colormap.ColorMap;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.satellite.units.counts.DerivedWVPixel;
import com.raytheon.uf.common.dataplugin.satellite.units.generic.GenericPixel;
import com.raytheon.uf.common.dataplugin.satellite.units.goes.PolarPrecipWaterPixel;
import com.raytheon.uf.common.dataplugin.satellite.units.water.BlendedTPWPixel;
import com.raytheon.uf.common.geospatial.ISpatialEnabled;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.ColorMapParameters;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.style.level.Level;
import com.raytheon.uf.viz.core.style.level.SingleLevel;
import com.raytheon.uf.viz.derivparam.library.DerivedParameterRequest;
import com.raytheon.viz.core.rsc.hdf5.AbstractTileSet;
import com.raytheon.viz.core.rsc.hdf5.FileBasedTileSet;
import com.raytheon.viz.satellite.SatelliteConstants;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Provides satellite raster rendering support
 * 
 * <pre>
 * 
 *  SOFTWARE HISTORY
 * 
 *  Date         Ticket#     Engineer    Description
 *  ------------ ----------  ----------- --------------------------
 *  Mar 1, 2007              chammack    Initial Creation.
 *  02/17/2009               njensen     Refactored to new rsc architecture.
 *  03/02/2009		2032	 jsanchez	 Added check for displayedDate if no data.
 *  03/25/2009      2086     jsanchez    Mapped correct converter to parameter type.
 *                                        Updated the call to ColormapParametersFactory.build
 *  03/30/2009      2169     jsanchez    Updated numLevels handling.
 *  08/26/2009               ghull       Integrate with AbstractNatlCntrsResource
 *  04/15/2010      #259     ghull       Added ColorBar
 *  05/24/2010      #281     ghull       Incorporate changes to Raytheon's SatResource.
 *  05/25/2010      #281     ghull       created from SatResource and McidasResource
 *  03/10/2011      #393     archana   added the method getTileSet() to the resource
 * </pre>
 * 
 * @author chammack
 * @version 1
 */

public abstract class AbstractSatelliteResource extends
        AbstractNatlCntrsResource<SatelliteResourceData, MapDescriptor>
        implements INatlCntrsResource {

    protected SatelliteResourceData satRscData;

    protected FileBasedTileSet baseTile;

    protected GridGeometry2D baseGeom;

    // private Map<SatelliteRecord, ByteDataRecord> fullDataSetMap = new
    // HashMap<SatelliteRecord, ByteDataRecord>();
    //
    protected String legendStr = "Satellite"; // init so not-null

    // ? Why a list? Why would there be different image types?
    protected List<String> imageTypes; // ie physicalElements;

    protected String baseFileName;

    protected IGraphicsTarget grphTarget;

    protected int numLevels;

    protected String viewType;

    protected ColorBarResource cbarResource;

    protected ResourcePair cbarRscPair;

    public FileBasedTileSet getTileSet() {
        FrameData fd = (FrameData) this.getCurrentFrame();
        return fd.getTileSet();
    }

    protected class FrameData extends AbstractFrameData {
        FileBasedTileSet tileSet;

        /**
         * @return the tileSet
         */
        public FileBasedTileSet getTileSet() {
            return tileSet;
        }

        // PluginDataObject satRec;
        GridGeometry2D gridGeom;

        DataTime tileTime; // the time of the data used to create the tileset
                           // used to determine if we need to replace the tile
                           // with a better timematch.

        protected FrameData(DataTime time, int interval) {
            super(time, interval);
            tileTime = null;
        }

        @Override
        public boolean updateFrameData(IRscDataObject rscDataObj) {
            PluginDataObject satRec = ((DfltRecordRscDataObj) rscDataObj)
                    .getPDO();
            if (!(satRec instanceof ISpatialEnabled)) {
                System.out
                        .println("AbstractSatelliteResource.updateFrameData: PDO "
                                + satRec.getClass().toString()
                                + " doesn't implement ISpatialEnabled");
                return false;
            }

            synchronized (this) {
                try {
                    // TODO : need to decide if this image is a better timeMatch
                    // for this frame than the existing one.

                    // if this is the first record to be added then we will
                    // need to create the baseTile. We can't do this in init()
                    // since we need a record to create baseTile
                    if (baseTile == null) {
                        initializeFirstFrame(satRec);
                    }

                    if (baseTile == null) {

                        if (getProjectionFromRecord(satRec).equalsIgnoreCase(
                                "STR")
                                || getProjectionFromRecord(satRec)
                                        .equalsIgnoreCase("MER")
                                || getProjectionFromRecord(satRec)
                                        .equalsIgnoreCase("LCC")) {
                            /*
                             * for remapped projections such as MER, LCC, STR
                             */
                            gridGeom = baseGeom = MapUtil
                                    .getGridGeometry(((ISpatialEnabled) satRec)
                                            .getSpatialObject());
                            tileSet = baseTile = new McidasFileBasedTileSet(
                                    satRec, "Data", numLevels, 256, gridGeom,
                                    AbstractSatelliteResource.this,
                                    PixelInCell.CELL_CORNER, viewType);
                            tileTime = satRec.getDataTime();
                        } else {
                            /*
                             * for native Satellite projections. Note native
                             * projections can vary with each image. cannot
                             * specify a baseTile or baseGeom.
                             */
                            gridGeom = createNativeGeometry(satRec);
                            tileSet = new McidasFileBasedTileSet(satRec,
                                    "Data", numLevels, 256, gridGeom,
                                    AbstractSatelliteResource.this,
                                    PixelInCell.CELL_CORNER, viewType);
                            tileTime = satRec.getDataTime();
                        }

                    } else {
                        // if the tileset is already set, and the new record is
                        // is not a better match then return
                        if (tileSet != null && tileTime != null) {
                            if (timeMatch(satRec.getDataTime()) >= timeMatch(tileTime)) {
                                return false;
                            } else { // if this is a better match, we need to
                                     // create a new tile.
                                if (tileSet == baseTile) {
                                    tileSet = null;
                                } else {
                                    tileSet.dispose();
                                    tileSet = null;
                                }
                            }
                        }

                        tileSet = new McidasFileBasedTileSet(satRec, "Data",
                                baseTile);
                        tileTime = satRec.getDataTime();
                        gridGeom = baseGeom;
                    }

                    tileSet.setMapDescriptor(AbstractSatelliteResource.this.descriptor);

                    if (grphTarget != null)
                        tileSet.init(grphTarget);

                    // tileSet.put(record.getDataTime(), tile);
                    // dataTimes.add( satRecord.getDataTime() );

                    imageTypes.add(getImageTypeFromRecord(satRec));

                    Collections.sort(AbstractSatelliteResource.this.dataTimes);
                } catch (VizException e) {
                    System.out.println("Error processing SatelliteRecord. "
                            + e.getMessage());
                    return false;
                }
            }

            return true;
        }

        @Override
        public void dispose() { // not tested yet...
            if (tileSet != baseTile && tileSet != null) {
                tileSet.dispose();
                tileSet = null;
            }
        }
    }

    // abstract methods used to get Record dependent info from the PDO
    // This represents the only differences between Mcidas and Gini Satellite
    // Resources.
    //
    abstract String getImageTypeFromRecord(PluginDataObject pdo);

    abstract String getDataUnitsFromRecord(PluginDataObject pdo);

    abstract String getCreatingEntityFromRecord(PluginDataObject pdo);

    abstract String getProjectionFromRecord(PluginDataObject pdo);

    abstract GridGeometry2D createNativeGeometry(PluginDataObject pdo);

    /**
     * Constructor
     * 
     * @throws VizException
     */
    public AbstractSatelliteResource(SatelliteResourceData data,
            LoadProperties props) {
        super(data, props);
        satRscData = data;
        // this.tileSet = new HashMap<DataTime, FileBasedTileSet>();
        // this.dataTimes = new ArrayList<DataTime>();

        this.imageTypes = new ArrayList<String>(); // physicalElements
        this.baseFileName = VizApp.getDataDir();
        grphTarget = null;
        numLevels = 0;

        // SatelliteRecord[] records = data.getRecords();
        // Arrays.sort(records, new SatelliteRecordComparator());
        //
        // for (SatelliteRecord record : records) {
        // try {
        // addRecord(record);
        // } catch (VizException e) {
        // UFStatus.handle(Priority.PROBLEM, Activator.PLUGIN_ID,
        // StatusConstants.CATEGORY_WORKSTATION, "satellite",
        // "Error adding satellite record", e);
        // }
        // }
        //
        /*
         * This handles if there is no data for East & West CONUS simultaneously
         */
        // if (dataTimes.size() > 1) {
        // currFrameTime = dataTimes.get(dataTimes.size() - 1);
        // }
    }

    @Override
    public String getName() {
        String legendString = super.getName();
        FrameData fd = (FrameData) getCurrentFrame();
        if (fd == null || fd.tileTime == null) {
            return legendString;
        }
        return legendString + "("
                + NmapCommon.getTimeStringFromDataTime(fd.tileTime, "/");
    }

    @Override
    protected AbstractFrameData createNewFrame(DataTime frameTime,
            int frameInterval) {
        return new FrameData(frameTime, frameInterval);
    }

    // need a record to create the baseTile. used to get the numLevels, .
    private void initializeFirstFrame(PluginDataObject record)
            throws VizException {
        // File file = HDF5Util.findHDF5Location(record);
        //
        // if (!file.exists()) {
        // throw new VizException("File does not exist: " + file.toString());
        // }
        //
        SingleLevel level = new SingleLevel(Level.LevelType.SURFACE);

        NcSatelliteUnits.register();

        // getLegend(record);

        // NOTE : This will replace Raytheons IRPixel (and
        // IRPixelToTempConverter) with ours
        // even for D2D's GiniSatResource.
        // UnitFormat.getUCUMInstance(). label( , arg1)

        Unit<?> dataUnit = null;
        String dataUnitStr = getDataUnitsFromRecord(record);
        String imgType = null;
        DerivedParameterRequest request = (DerivedParameterRequest) record
                .getMessageData();

        if (request == null) {
            imgType = getImageTypeFromRecord(record);
        } else {
            imgType = request.getParameterAbbreviation();
        }

        if (dataUnitStr != null && !dataUnitStr.isEmpty() && request == null) {
            try {
                dataUnit = UnitFormat.getUCUMInstance().parseSingleUnit(
                        dataUnitStr, new ParsePosition(0));
            } catch (ParseException e) {
                throw new VizException("Unable parse units : " + dataUnitStr, e);
            }
        } else if (request != null) {
            if (imgType.equals("satDivWVIR")) {
                dataUnit = new DerivedWVPixel();
            } else {
                dataUnit = new GenericPixel();
            }
        } else
            dataUnit = new GenericPixel();

        // } else {
        // // units not in satellite_units table
        // if (physicalElement.equals("Gridded Cloud Amount")) {
        // unit = new SounderCloudAmountPixel();
        // } else if (physicalElement
        // .equals("Gridded Cloud Top Pressure or Height")) {
        // unit = new SounderCloudTopHeightPixel();
        // }
        // }

        // ? This logic came from Raytheon's SatResource. Does it apply to
        // McIdas?
        String creatingEntity = null;

        if (imgType.equals(SatelliteConstants.PRECIP)) {

            creatingEntity = getCreatingEntityFromRecord(record); // .getCreatingEntity();

            if (creatingEntity.equals(SatelliteConstants.DMSP)
                    || creatingEntity.equals(SatelliteConstants.POES)) {

                dataUnit = new PolarPrecipWaterPixel();
            } else if (creatingEntity.equals(SatelliteConstants.MISC)) {

                dataUnit = new BlendedTPWPixel();
            }
        }

        // create the colorMap and set it in the colorMapParameters and init the
        // colorBar
        ColorMapParameters colorMapParameters = new ColorMapParameters();

        ColorMap colorMap;
        try {
            colorMap = (ColorMap) ColorMapUtil.loadColorMap(satRscData
                    .getResourceName().getRscCategory(), satRscData
                    .getColorMapName());
        } catch (VizException e) {
            throw new VizException("Error loading colormap: "
                    + satRscData.getColorMapName());
        }

        ColorBarFromColormap colorBar = satRscData.getColorBar();

        (colorBar).setColorMap(colorMap);

        colorMapParameters.setColorMap(colorMap);
        colorMapParameters.setDisplayUnit(satRscData.getDisplayUnit());
        colorMapParameters.setDataUnit(dataUnit);

        // set real color and data max/min based on ... TODO
        colorMapParameters.setDataMin(0.0f);
        colorMapParameters.setDataMax(255.0f);
        colorMapParameters.setColorMapMin(0.0f);
        colorMapParameters.setColorMapMax(255.0f);

        getCapability(ColorMapCapability.class).setColorMapParameters(
                colorMapParameters);

        numLevels = 1;
        int newSzX = ((ISpatialEnabled) record).getSpatialObject().getNx();
        int newSzY = ((ISpatialEnabled) record).getSpatialObject().getNy();

        while ((newSzX > 512 && newSzY > 512)) {
            newSzX /= 2;
            newSzY /= 2;
            numLevels++;
        }

    }

    // @Override
    // public String getName() {
    // if(currFrameTime == null )
    // return "Satellite - No Data Available";
    // RequestConstraint rc =
    // satRscData.getMetadataMap().get("physicalElement");
    //
    // // TODO : for to11dr11-determine creating entity
    // return (rc == null ? "Satellite : " :
    // SatelliteConstants.getLegend( rc.getConstraintValue(),
    // "" )+" " )
    // + " : " + currFrameTime.getLegendString();
    // }

    @Override
    public void disposeInternal() {
        super.disposeInternal(); // dispose of the frameData

        if (baseTile != null) {
            baseTile.dispose();
            baseTile = null;
        }

        getDescriptor().getResourceList().remove(cbarRscPair);
    }

    @Override
    public void initResource(IGraphicsTarget target) throws VizException {
        synchronized (this) {
            this.viewType = target.getViewType();
            this.grphTarget = target;

            grphTarget.setUseBuiltinColorbar(false);

            // create the colorBar Resource and add it to the resourceList for
            // this descriptor.
            cbarRscPair = ResourcePair
                    .constructSystemResourcePair(new ColorBarResourceData(
                            satRscData.getColorBar()));

            getDescriptor().getResourceList().add(cbarRscPair);
            getDescriptor().getResourceList().instantiateResources(
                    getDescriptor(), true);

            cbarResource = (ColorBarResource) cbarRscPair.getResource();

            queryRecords();

            if (this.baseTile != null) {
                this.baseTile.init(target);
            }
            for (AbstractFrameData frm : frameDataMap.values()) {
                AbstractTileSet ts = ((FrameData) frm).tileSet;
                if (ts != null)
                    ts.init(target);
            }
        }
    }

    @Override
    public void paintFrame(AbstractFrameData frmData, IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        // this.displayedDate = paintProps.getDataTime();
        // this.target = target;

        FrameData currFrame = (FrameData) frmData;

        FileBasedTileSet tileSet = currFrame.tileSet;

        if (tileSet != null) {
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
            if (params.getColorMap() == null) {
                String colorMapName = params.getColorMapName();
                if (colorMapName == null)
                    colorMapName = "Sat/VIS/ZA (Vis Default)";

                params.setColorMap(target.buildColorMap(colorMapName));
            }

            tileSet.paint(target, paintProps);
        }
    }

    public void setDescriptor(MapDescriptor descriptor) {
        if (this.baseTile != null) {
            this.baseTile.setMapDescriptor(descriptor);
        }
        for (AbstractFrameData frm : frameDataMap.values()) {
            AbstractTileSet ts = ((FrameData) frm).tileSet;

            if (ts != null) {
                ts.setMapDescriptor(descriptor);
            }
        }

        this.descriptor = descriptor;
    }

    @Override
    public void project(CoordinateReferenceSystem mapData) throws VizException {
        if (this.baseTile != null)
            this.baseTile.reproject();

        for (AbstractFrameData frm : frameDataMap.values()) {
            AbstractTileSet ts = ((FrameData) frm).tileSet;
            if (ts != null)
                ts.reproject();
        }
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        Double value = inspectValue(coord);
        if (value == null) {
            return "NO DATA";
        }
        ColorMapParameters cmp = getCapability(ColorMapCapability.class)
                .getColorMapParameters();
        Unit<?> unit = cmp.getDisplayUnit();
        float[] intervals = cmp.getColorBarIntervals();
        if (intervals != null) {
            float f1 = intervals[0];
            float f2 = intervals[intervals.length - 1];
            if (value > f1 && value > f2) {
                return String.format(">%.1f%s", Math.max(f1, f2),
                        unit == null ? "" : unit.toString());
            }
            if (value < f1 && value < f2) {
                return String.format("<%.1f%s", Math.min(f1, f2),
                        unit == null ? "" : unit.toString());
            }
        }
        return String.format("%.1f%s", value,
                unit == null ? "" : unit.toString());
    }

    public Double inspectValue(ReferencedCoordinate coord) throws VizException {
        FrameData currFrame = (FrameData) getCurrentFrame();

        if (currFrame == null) {
            return null;
        }

        Coordinate latlon = null;

        try {
            latlon = coord.asLatLon();
        } catch (Exception e) {
            throw new VizException("Error transforming coordinate to lat/lon",
                    e);
        }
        if (latlon == null) {
            return null;
        }

        return currFrame.tileSet.interrogate(latlon, false);
    }

    protected List<String> getImageTypes() {
        return imageTypes;
    }

    /*
     * implement ICloudHeightCapable methods
     */

    public abstract boolean isCloudHeightCompatible();

    public Double getRawIRImageValue(Coordinate latlon) {
        if (!isCloudHeightCompatible()) {
            return null;
        }

        FrameData currFrame = (FrameData) getCurrentFrame();

        if (currFrame == null) {
            return null;
        }
        try {
            return currFrame.tileSet.interrogate(latlon, true);
        } catch (VizException e) {
            return null;
        }
    }

    // This will return the temp in the units set as the display units in the
    // colormap parameters.
    //
    public Double getSatIRTemperature(Coordinate latlon) {
        if (!isCloudHeightCompatible()) {
            return null;
        }

        FrameData currFrame = (FrameData) getCurrentFrame();

        if (currFrame == null) {
            return null;
        }
        try {
            return currFrame.tileSet.interrogate(latlon, false);
        } catch (VizException e) {
            return null;
        }

    }

    public Unit<Temperature> getTemperatureUnits() {
        if (isCloudHeightCompatible()) {
            if (satRscData.getDisplayUnit() == null) {
                return SI.CELSIUS;
            } else {
                return (Unit<Temperature>) satRscData.getDisplayUnit();
            }
        } else {
            return null;
        }
    }

    // the colorBar and/or the colormap may have changed so update the
    // colorBarPainter and the colorMapParametersCapability which holds
    // the instance of the colorMap that Raytheon's code needs
    @Override
    public void resourceAttrsModified() {
        // update the colorbarPainter with a possibly new colorbar
        ColorBarFromColormap colorBar = satRscData.getColorBar();

        cbarResource.setColorBar(colorBar);

        ColorMapParameters cmapParams = getCapability(ColorMapCapability.class)
                .getColorMapParameters();
        cmapParams.setColorMap(colorBar.getColorMap());
        cmapParams.setColorMapName(satRscData.getColorMapName());
        // not currently an attribute but could be.
        cmapParams.setDisplayUnit(satRscData.getDisplayUnit());

        getCapability(ColorMapCapability.class).setColorMapParameters(
                cmapParams);

        // TODO to11dr11 determine how to migrate this
        if (baseTile != null) {
            // baseTile.resourceChanged(ChangeType.CAPABILITY,
            // this.getCapability( ColorMapCapability.class));
        }
    }

    public String getLegendString() {
        return legendStr;
    }

    public GeneralGridGeometry getImageGeometry() {
        FrameData currFrame = (FrameData) getCurrentFrame();
        return currFrame.gridGeom;
    }

    // private String getLegend( PluginDataObject record ) {
    // if( legendStr == null ) {
    // String productName = null;
    // DerivedParameterRequest request =
    // (DerivedParameterRequest) record.getMessageData();
    //
    // if( request == null ) {
    // productName = getImageTypeFromRecord( record );
    // } else {
    // productName = request.getParameterAbbreviation();
    // }
    // legendStr = SatelliteConstants.getLegend(
    // productName, getCreatingEntityFromRecord( record ) );
    // // ((SatelliteRecord) record).getCreatingEntity());
    // }
    //
    // return legendStr;
    // }
    //
    // @Override
    // public void addContextMenuItems(IMenuManager menuManager) {
    // if (descriptor != null && getName().contains("IR")) {
    // CloudHeightRightClickAction.addInstance(menuManager, descriptor);
    // }
    // }
}
