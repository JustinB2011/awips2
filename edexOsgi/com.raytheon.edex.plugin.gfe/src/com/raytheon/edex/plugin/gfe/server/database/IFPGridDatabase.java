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

package com.raytheon.edex.plugin.gfe.server.database;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.springframework.dao.DataAccessException;

import com.raytheon.edex.plugin.gfe.cache.ifpparms.IFPParmIdCache;
import com.raytheon.edex.plugin.gfe.config.GridDbConfig;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfig;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfigManager;
import com.raytheon.edex.plugin.gfe.config.ParmStorageInfo;
import com.raytheon.edex.plugin.gfe.db.dao.GFEDao;
import com.raytheon.edex.plugin.gfe.exception.GfeConfigurationException;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.RemapGrid;
import com.raytheon.uf.common.dataplugin.gfe.config.ProjectionData;
import com.raytheon.uf.common.dataplugin.gfe.config.ProjectionData.ProjectionType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.DatabaseID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord.GridType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.exception.GfeException;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DByte;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DFloat;
import com.raytheon.uf.common.dataplugin.gfe.server.message.ServerResponse;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.GridUpdateNotification;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.IGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.ScalarGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.VectorGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.WeatherGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.util.GfeUtil;
import com.raytheon.uf.common.dataplugin.gfe.weather.WeatherKey;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.IDataStore.StoreOp;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.datastorage.StorageStatus;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.plugin.PluginFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTReader;

/**
 * GFE Grid database containing IFP Grid data.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 04/08/08     #875       bphillip    Initial Creation
 * 06/17/08     #940       bphillip    Implemented GFE Locking
 * 06/18/08                njensen     Added discrete/wx to getGridData()
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */

public class IFPGridDatabase extends GridDatabase {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(IFPGridDatabase.class);

    protected static final String GRID_PARM_INFO = "GridParmInfo";

    protected static final String GRID_PARM_STORAGE_INFO = "GridParmStorageInfo";

    protected static final String GRID_PARM_INFO_GRP = File.separator
            + GRID_PARM_INFO + File.separator;

    protected static final String GRID_PARM_STORAGE_INFO_GRP = File.separator
            + GRID_PARM_STORAGE_INFO + File.separator;

    private boolean parmInfoInitialized = false;

    private static final float VECTOR_DIR_DATA_MULTIPLIER = 0.5f;

    private static final float VECTOR_DIR_DATA_OFFSET = 0.0f;

    private Map<String, GridParmInfo> parmInfo = new HashMap<String, GridParmInfo>();

    private Map<String, ParmStorageInfo> parmStorageInfo = new HashMap<String, ParmStorageInfo>();

    /** The grid configuration for this database */
    protected GridDbConfig gridConfig;

    /**
     * Creates a new IFPGridDatabase
     */
    public IFPGridDatabase() {
        super();
        valid = false;
    }

    /**
     * Creates a new IFPGridDatabase
     * 
     * @param dbId
     *            The database ID for this database
     */
    public IFPGridDatabase(DatabaseID dbId) throws GfeException {
        super(dbId);
        try {
            this.gridConfig = IFPServerConfigManager.getServerConfig(
                    dbId.getSiteId()).gridDbConfig(
                    new DatabaseID(dbId.toString().replace("D2D", "")));
            if (this.gridConfig == null) {
                throw new GfeException(
                        "Server config contains no gridDbConfig for database "
                                + dbId.toString());
            }
        } catch (GfeException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to get gridConfig for: " + dbId, e);
            throw e;
        }
        valid = true;
    }

    /**
     * Upon site activation, this method is called to calculate any changes to
     * parm info or grid locations
     */
    @Override
    public void updateDbs() {
        Map<String, GridParmInfo> parmInfoUser = new HashMap<String, GridParmInfo>();
        Map<String, ParmStorageInfo> parmStorageInfoUser = new HashMap<String, ParmStorageInfo>();

        try {
            getDBConfiguration();
            applyProjectionAndDomainChanges();
        } catch (GfeException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error getting db configuration for " + this.dbId, e);
        }

        if (!this.getUserConfiguration(parmInfoUser, parmStorageInfoUser)) {
            return;
        }

        List<String> toBeAddedParms = new ArrayList<String>();
        List<String> toBeRemovedParms = new ArrayList<String>();
        List<String> toBeChangedParms = new ArrayList<String>();

        // Compare the variables in the database with config
        compareParmInfoWithDB(parmInfoUser, parmStorageInfoUser,
                toBeAddedParms, toBeRemovedParms, toBeChangedParms);

        // Add in new weather elements
        addNewParms(toBeAddedParms, parmInfoUser, parmStorageInfoUser);

        // Remove all old parms
        removeOldParms(toBeRemovedParms);

        // accommodate changes
        changeParmCharacteristics(toBeChangedParms, parmInfoUser,
                parmStorageInfoUser);
    }

    private void addNewParms(List<String> newParms,
            Map<String, GridParmInfo> parmInfoUser,
            Map<String, ParmStorageInfo> parmStorageUser) {
        if (newParms.isEmpty()) {
            return;
        }
        statusHandler.handle(Priority.INFO, "Creating new weather elements...");
        try {
            List<GridParmInfo> gpis = new ArrayList<GridParmInfo>();
            List<ParmStorageInfo> psis = new ArrayList<ParmStorageInfo>();
            for (int i = 0; i < newParms.size(); i++) {
                String item = newParms.get(i);
                statusHandler.handle(Priority.INFO, "Adding: " + item
                        + " to the " + this.dbId + " database.");
                gpis.add(parmInfoUser.get(item));
                psis.add(parmStorageUser.get(item));
            }
            storeGridParmInfo(gpis, psis, StoreOp.STORE_ONLY);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Error adding new parms", e);
        }

    }

    /**
     * Changes the parm characteristics
     * 
     * @param toBeChangedParms
     *            The parms whose info has changed
     * @param parmInfoUser
     *            The GridParmInfo to update to
     */
    private void changeParmCharacteristics(List<String> toBeChangedParms,
            Map<String, GridParmInfo> parmInfoUser,
            Map<String, ParmStorageInfo> parmStorageUser) {

        for (int i = 0; i < toBeChangedParms.size(); i++) {

            // Denotes whether the units were changed
            boolean unitsChanged = false;
            String compositeName = toBeChangedParms.get(i);

            // get the current database information
            GridParmInfo dbGPI = this.parmInfo.get(compositeName);
            ParmStorageInfo dbPSI = this.parmStorageInfo.get(compositeName);

            // get the desired configuration
            GridParmInfo userGPI = parmInfoUser.get(compositeName);
            ParmStorageInfo userPSI = parmStorageUser.get(compositeName);

            // data type change
            if (!userGPI.getGridType().equals(dbGPI.getGridType())) {
                statusHandler.handle(Priority.INFO, "Changing Data Type: "
                        + dbGPI.getParmID() + " from " + dbGPI.getGridType()
                        + " to " + userGPI.getGridType());

                changeDataType(compositeName, userGPI, userPSI);
            }

            // unit change
            if (!userGPI.getUnitString().equals(dbGPI.getUnitString())) {
                statusHandler.handle(
                        Priority.INFO,
                        "Changing Units: " + dbGPI.getParmID() + " from "
                                + dbGPI.getUnitString() + " to "
                                + userGPI.getUnitString());
                changeUnits(compositeName, userGPI);
                unitsChanged = true;
            }

            // rate parm change
            if (userGPI.isRateParm() != dbGPI.isRateParm()) {
                statusHandler.handle(Priority.INFO, "Changing RateParm: "
                        + dbGPI.getParmID() + " from " + dbGPI.isRateParm()
                        + " to " + userGPI.isRateParm());
                // No special handling necessary
            }

            // time constraint changes
            if (!userGPI.getTimeConstraints()
                    .equals(dbGPI.getTimeConstraints())) {
                statusHandler.handle(Priority.INFO,
                        "Changing TimeConstraints: " + dbGPI.getParmID()
                                + " from " + dbGPI.getTimeConstraints()
                                + " to " + userGPI.getTimeConstraints());
                adjustTimeConstraints(compositeName,
                        userGPI.getTimeConstraints());
            }

            // descriptive name change
            if (!userGPI.getDescriptiveName()
                    .equals(dbGPI.getDescriptiveName())) {
                statusHandler.handle(Priority.INFO,
                        "Changing DescriptiveName: " + dbGPI.getParmID()
                                + " from " + dbGPI.getDescriptiveName()
                                + " to " + userGPI.getDescriptiveName());
                // No special handling necessary
            }

            // precision
            if (userGPI.getPrecision() != dbGPI.getPrecision()) {
                statusHandler.handle(Priority.INFO, "Changing Precision: "
                        + dbGPI.getParmID() + " from " + dbGPI.getPrecision()
                        + " to " + userGPI.getPrecision());
                // No special handling necessary
            }

            // max/min changes
            if (userGPI.getMaxValue() != dbGPI.getMaxValue()
                    || userGPI.getMinValue() != dbGPI.getMinValue()
                    || unitsChanged || !userPSI.equals(dbPSI)) {
                // If units were changed, the values need to be clamped to the
                // min and max values
                if (userGPI.getMaxValue() != dbGPI.getMaxValue()
                        || userGPI.getMinValue() != dbGPI.getMinValue()) {
                    statusHandler.handle(
                            Priority.INFO,
                            "Changing Max/Min: " + dbGPI.getParmID()
                                    + " from (" + dbGPI.getMinValue() + ","
                                    + dbGPI.getMaxValue() + ") to ("
                                    + userGPI.getMinValue() + ","
                                    + userGPI.getMaxValue() + ")");
                }
                if (userPSI.dataOffset() != dbPSI.dataOffset()) {
                    statusHandler.handle(Priority.INFO, "Changing DataOffset: "
                            + dbGPI.getParmID() + " from " + dbPSI.dataOffset()
                            + " to " + userPSI.dataOffset());
                }
                if (userPSI.dataMultiplier() != dbPSI.dataMultiplier()) {
                    statusHandler.handle(Priority.INFO,
                            "Changing DataMultiplier: " + dbGPI.getParmID()
                                    + " from " + dbPSI.dataMultiplier()
                                    + " to " + userPSI.dataMultiplier());
                }
                if (!userPSI.storageType().equals(dbPSI.storageType())) {
                    statusHandler.handle(Priority.INFO,
                            "Changing StorageType: " + dbGPI.getParmID()
                                    + " from " + dbPSI.storageType() + " to "
                                    + userPSI.storageType());
                }
                changeMinMaxValues(compositeName, userGPI, userPSI);
            }

            // store the updated GridParmInfo
            dbGPI = new GridParmInfo(dbGPI.getParmID(), dbGPI.getGridLoc(),
                    userGPI.getGridType(), userGPI.getUnitString(),
                    userGPI.getDescriptiveName(), userGPI.getMinValue(),
                    userGPI.getMaxValue(), userGPI.getPrecision(),
                    userGPI.isTimeIndependentParm(),
                    userGPI.getTimeConstraints(), userGPI.isRateParm());
            dbPSI = new ParmStorageInfo(userPSI.dataType(), userGPI
                    .getGridLoc().gridSize(), dbPSI.parmName(), dbPSI.level(),
                    userPSI.dataOffset(), userPSI.dataMultiplier(),
                    userPSI.storageType());
            updateParmAttributes(compositeName, userGPI, userPSI);
        }

    }

    /**
     * Changes the min/max values for a parm. This method also updates any
     * information in the HDF5 to reflect the changed values
     * 
     * @param compositeName
     *            The parm name for which the min/max values have changed
     * @param newGPI
     *            The GridParmInfo
     */
    private void changeMinMaxValues(String compositeName, GridParmInfo newGPI,
            ParmStorageInfo newPSI) {
        GFEDao dao = null;
        // Make sure the specified parm is of type Scalar or Vector
        GridType gridType = newGPI.getGridType();
        if (!(gridType.equals(GridType.SCALAR) || gridType
                .equals(GridType.VECTOR))) {
            return;
        }
        ParmID parmId = new ParmID(this.splitNameAndLevel(compositeName)[0],
                this.dbId);
        try {
            dao = new GFEDao();
        } catch (PluginException e) {
            statusHandler
                    .handle(Priority.PROBLEM,
                            "Error changing min/max values.  Unable to instantiate GFE dao",
                            e);
            return;
        }
        List<GFERecord> records = null;
        try {
            records = dao.queryByParmID(parmId);
        } catch (DataAccessLayerException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error changing min/max values.  Error querying GFE table",
                    e);
            return;
        }
        List<GFERecord> updatedRecords = new ArrayList<GFERecord>();
        Set<String> locationsToDelete = new HashSet<String>();
        for (GFERecord rec : records) {
            switch (gridType) {
            case SCALAR:
                FloatDataRecord scalarRecord = null;
                List<TimeRange> scalarTimes = new ArrayList<TimeRange>();
                scalarTimes.add(rec.getTimeRange());
                ServerResponse<List<IGridSlice>> scalarResult = this
                        .getGridData(parmId, scalarTimes);
                if (!scalarResult.isOkay()) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error changing min/max values.  Unable to retrieve data from HDF5: "
                                    + scalarResult.toString());
                    continue;
                }
                try {
                    scalarRecord = this.retrieveFromHDF5(parmId,
                            rec.getTimeRange());
                } catch (GfeException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error changing min/max values.  Unable to retrieve data from HDF5 "
                                    + e);
                    continue;
                }
                this.applyNewMinMax(scalarRecord.getFloatData(),
                        newGPI.getMinValue(), newGPI.getMaxValue());
                rec.setMessageData(scalarRecord);
                updatedRecords.add(rec);
                locationsToDelete.add(scalarRecord.getGroup());
                break;
            case VECTOR:
                List<TimeRange> vectorTimes = new ArrayList<TimeRange>();
                vectorTimes.add(rec.getTimeRange());
                FloatDataRecord[] vectorRecord = null;
                VectorGridSlice vSlice = null;

                ServerResponse<List<IGridSlice>> result = this.getGridData(
                        parmId, vectorTimes);
                if (!result.isOkay()) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error changing min/max values.  Unable to retrieve data from HDF5: "
                                    + result.toString());
                    continue;
                }
                vSlice = (VectorGridSlice) result.getPayload().get(0);
                try {
                    vectorRecord = this.retrieveVectorFromHDF5(parmId,
                            rec.getTimeRange());
                } catch (GfeException e) {
                    statusHandler
                            .handle(Priority.PROBLEM,
                                    "Error changing min/max values.  Unable to retrieve data from HDF5",
                                    e);
                    continue;
                }
                this.applyNewMinMax(vectorRecord[0].getFloatData(),
                        newGPI.getMinValue(), newGPI.getMaxValue());
                Grid2DFloat rawData = new Grid2DFloat(newGPI.getGridLoc()
                        .getNx(), newGPI.getGridLoc().getNy(),
                        vectorRecord[0].getFloatData());
                Grid2DFloat rawData2 = new Grid2DFloat(newGPI.getGridLoc()
                        .getNx(), newGPI.getGridLoc().getNy(),
                        vectorRecord[1].getFloatData());
                vSlice.setMagGrid(rawData);
                vSlice.setDirGrid(rawData2);
                rec.setMessageData(vSlice);
                updatedRecords.add(rec);
                locationsToDelete.add(vectorRecord[0].getGroup());
                break;
            }
        }
        if (!updatedRecords.isEmpty()) {
            File file = GfeUtil.getHDF5File(gfeBaseDataDir, parmId.getDbId());
            try {
                DataStoreFactory.getDataStore(file).delete(
                        locationsToDelete.toArray(new String[locationsToDelete
                                .size()]));
                this.saveGridsToHdf5(updatedRecords, newPSI);
            } catch (Exception e) {
                statusHandler
                        .handle(Priority.PROBLEM,
                                "Error changing min/max values.  Unable to update data.",
                                e);
            }
        }

    }

    /**
     * Adjusts the time constraints for a given parm. This method simply deletes
     * any grids that are in the db for the modified ParmID
     * 
     * @param compositeName
     *            The parm name that needs to be modified
     * @param timeConstraints
     *            The new time constraints
     */
    private void adjustTimeConstraints(String compositeName,
            TimeConstraints timeConstraints) {
        List<String> puntList = new ArrayList<String>();
        puntList.add(compositeName);
        this.removeOldParms(puntList);
    }

    /**
     * Clamps the data values in a float array to the given min and max values
     * 
     * @param data
     *            The data to clamp
     * @param newMin
     *            The minimum allowed value
     * @param newMax
     *            The maximum allowed value
     */
    private void applyNewMinMax(float[] data, float newMin, float newMax) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] < newMin) {
                data[i] = newMin;
            } else if (data[i] > newMax) {
                data[i] = newMax;
            }
        }
    }

    /**
     * Changes the units for a parm. This method also executes a unit conversion
     * on the data in the HDF5 repository
     * 
     * @param compositeName
     *            The parm to be modified
     * @param gpi
     *            The grid parm info
     */
    private void changeUnits(String compositeName, GridParmInfo gpi) {
        GFEDao dao = null;
        try {
            GridType gridType = gpi.getGridType();
            ParmID parmId = new ParmID(
                    this.splitNameAndLevel(compositeName)[0], this.dbId);
            dao = new GFEDao();
            List<GFERecord> records = dao.queryByParmID(parmId);
            List<GFERecord> updatedRecords = new ArrayList<GFERecord>();
            for (GFERecord rec : records) {
                switch (gridType) {
                case SCALAR:
                    FloatDataRecord scalarRecord = this.retrieveFromHDF5(
                            parmId, rec.getTimeRange());
                    float[] convertedScalarData = this.convertData(
                            this.parmInfo.get(compositeName).getUnitObject(),
                            gpi.getUnitObject(), scalarRecord.getFloatData());
                    scalarRecord.setFloatData(convertedScalarData);
                    rec.setMessageData(scalarRecord);
                    updatedRecords.add(rec);
                    break;
                case VECTOR:
                    FloatDataRecord[] vectorRecord = this
                            .retrieveVectorFromHDF5(parmId, rec.getTimeRange());
                    float[] convertedVectorData = this
                            .convertData(this.parmInfo.get(compositeName)
                                    .getUnitObject(), gpi.getUnitObject(),
                                    vectorRecord[0].getFloatData());
                    vectorRecord[0].setFloatData(convertedVectorData);
                    rec.setMessageData(vectorRecord);
                    updatedRecords.add(rec);
                    break;
                }
            }
            if (!updatedRecords.isEmpty()) {
                this.saveGridsToHdf5(updatedRecords);
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Error changing units", e);
        }

    }

    /**
     * Converts data in the given array to the desired unit
     * 
     * @param from
     *            The unit to convert from
     * @param to
     *            The unit to convert to
     * @param dataToConvert
     *            The data to convert
     * @return The converted array of data
     * @throws GfeException
     *             If the data cannot be converted
     */
    private float[] convertData(Unit<?> from, Unit<?> to, float[] dataToConvert)
            throws GfeException {

        UnitConverter converter = null;

        if ((from != null) && (to != null)) {
            if (to.isCompatible(from)) {
                converter = from.getConverterTo(to);
            } else {
                throw new GfeException(from + " is not compatible with " + to);
            }
        } else {
            throw new GfeException("Units cannot be null!");
        }

        float[] convertedData = new float[dataToConvert.length];

        for (int i = 0; i < dataToConvert.length; i++) {
            convertedData[i] = (float) converter.convert(dataToConvert[i]);
        }

        return convertedData;
    }

    /**
     * Changes the data type in the database for a given parm
     * 
     * @param compositeName
     *            The name of the parm
     * @param newGPI
     *            The GridParmInfo associated with the parm
     */
    private void changeDataType(String compositeName, GridParmInfo newGPI,
            ParmStorageInfo newPSI) {
        List<String> entries = new ArrayList<String>();
        entries.add(compositeName);
        removeOldParms(entries);
        updateParmAttributes(compositeName, newGPI, newPSI);
        this.parmInfo.put(compositeName, newGPI);
        this.parmStorageInfo.put(compositeName, newPSI);
    }

    /**
     * Stores the new GridParmInfo to the HDF5 repository
     * 
     * @param compositeName
     *            The parm to update the GridParmInfo for
     * @param newGPI
     *            The new GridParmInfo
     */
    private void updateParmAttributes(String compositeName,
            GridParmInfo newGPI, ParmStorageInfo newPSI) {
        try {
            storeGridParmInfo(newGPI, newPSI, StoreOp.REPLACE);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error updating GridParmInfo in the HDF5 repository", e);
        }
    }

    /**
     * Removes the data in the database and HDF5 repository for the list of
     * parms
     * 
     * @param puntList
     *            The list of parms to delete
     */
    private void removeOldParms(List<String> puntList) {

        try {
            GFEDao dao = new GFEDao();
            // Remove the grids
            String item = null;
            for (int i = 0; i < puntList.size(); i++) {
                item = puntList.get(i);
                statusHandler.handle(Priority.INFO, "Removing: " + item
                        + " from the " + this.dbId + " database.");
                try {
                    dao.removeOldParm(item, this.dbId, DataStoreFactory
                            .getDataStore(GfeUtil.getHDF5File(gfeBaseDataDir,
                                    this.dbId)));
                    this.parmInfo.remove(item);
                    this.parmStorageInfo.remove(item);
                } catch (DataAccessLayerException e) {
                    statusHandler.handle(Priority.PROBLEM, "Error removing: "
                            + item + " from the database");
                }
            }
        } catch (PluginException e) {
            statusHandler.handle(Priority.PROBLEM, "Error removing old parms!",
                    e);
        }

    }

    @Override
    public ServerResponse<List<ParmID>> getParmList() {
        List<ParmID> parmIds = new ArrayList<ParmID>();
        ServerResponse<List<ParmID>> sr = new ServerResponse<List<ParmID>>();

        // Construct ParmIDs for each entry in the grid info dictionary
        if (gridConfig != null) {
            parmIds = IFPParmIdCache.getInstance().getParmIds(gridConfig, dbId);
        }
        sr.setPayload(parmIds);
        return sr;
    }

    @Override
    public ServerResponse<List<TimeRange>> getGridInventory(ParmID id) {

        List<TimeRange> trs = new ArrayList<TimeRange>();
        ServerResponse<List<TimeRange>> sr = new ServerResponse<List<TimeRange>>();
        GFEDao dao = null;
        try {
            dao = (GFEDao) PluginFactory.getInstance().getPluginDao("gfe");
        } catch (PluginException e1) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get gfe dao", e1);
        }
        try {
            trs.addAll(dao.getTimes(id));
        } catch (DataAccessLayerException e) {
            sr.addMessage("Unable to get times for: " + id.getParmName() + "_"
                    + id.getParmLevel());
        }
        sr.setPayload(trs);
        return sr;
    }

    @Override
    public ServerResponse<Map<TimeRange, List<GridDataHistory>>> getGridHistory(
            ParmID id, List<TimeRange> trs) {
        ServerResponse<Map<TimeRange, List<GridDataHistory>>> sr = new ServerResponse<Map<TimeRange, List<GridDataHistory>>>();
        GFEDao dao = null;
        try {
            dao = (GFEDao) PluginFactory.getInstance().getPluginDao("gfe");
        } catch (PluginException e1) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get gfe dao", e1);
        }

        try {
            Map<TimeRange, List<GridDataHistory>> history = dao.getGridHistory(
                    id, trs);
            sr.setPayload(history);
        } catch (DataAccessLayerException e) {
            sr.addMessage("Error getting grid history for: " + id + "\n"
                    + e.getLocalizedMessage());
            statusHandler.handle(Priority.PROBLEM,
                    "Error getting grid history for: " + id, e);
        }

        return sr;
    }

    /**
     * Save the specified gridSlices over the time period specified by
     * originalTimeRange in the grid database.
     * 
     * Make sure that the database is valid and that the specified parm is known
     * to this database. If not return an error ServerResponse. Call calculate
     * overlap to see where the new grids fit amongst the old grids. Make sure
     * that the gridTimes for each grid is valid and that there is enough room
     * in the database to store the grids. If not, return an error
     * ServerResponse. Next remove any grids that need to be removed by setting
     * Their useTable entries to false. Then call storeGridSlices to put the
     * grids in the database. The SeqOf<TimeInfo> must then be inserted into the
     * timeInfo array. Write the new timeInfo array to the database and return.
     * 
     * @param id
     *            The parm ID to save
     * @param originalTimeRange
     *            The time range to save
     * @param records
     *            The records to save
     * @param requesterId
     *            who requested to save the grids
     * @return The server response
     */
    @Override
    public ServerResponse<?> saveGridData(ParmID id,
            TimeRange originalTimeRange, List<GFERecord> records,
            WsId requesterId) {
        return this.saveGridData(id, originalTimeRange, records, requesterId,
                null);
    }

    /**
     * Save the specified gridSlices over the time period specified by
     * originalTimeRange in the grid database.
     * 
     * Make sure that the database is valid and that the specified parm is known
     * to this database. If not return an error ServerResponse. Call calculate
     * overlap to see where the new grids fit amongst the old grids. Make sure
     * that the gridTimes for each grid is valid and that there is enough room
     * in the database to store the grids. If not, return an error
     * ServerResponse. Next remove any grids that need to be removed by setting
     * Their useTable entries to false. Then call storeGridSlices to put the
     * grids in the database. The SeqOf<TimeInfo> must then be inserted into the
     * timeInfo array. Write the new timeInfo array to the database and return.
     * 
     * @param id
     *            The parm ID to save
     * @param originalTimeRange
     *            The time range to save
     * @param records
     *            The records to save
     * @param requesterId
     *            who requested to save the grids
     * @param skipDelete
     *            time ranges to not delete
     * @return The server response
     */
    public ServerResponse<?> saveGridData(ParmID id,
            TimeRange originalTimeRange, List<GFERecord> records,
            WsId requesterId, List<TimeRange> skipDelete) {

        ServerResponse<?> sr = dbIsValid();

        if (!sr.isOkay()) {
            return sr;
        }

        sr.addMessages(validGridTimes(records, originalTimeRange));
        if (!sr.isOkay()) {
            return sr;
        }

        records = consolidateWithExisting(id, records);
        // Figure out where the grids fit into the current inventory
        List<TimeRange> timeInfo = getGridInventory(id).getPayload();
        List<TimeRange> newTimes = new ArrayList<TimeRange>(records.size());
        Map<TimeRange, List<GridDataHistory>> histories = new HashMap<TimeRange, List<GridDataHistory>>(
                (int) (records.size() * 1.25) + 1);
        for (GFERecord rec : records) {
            newTimes.add(rec.getTimeRange());
            histories.put(rec.getTimeRange(), rec.getGridHistory());
        }

        // don't delete if record already exists
        timeInfo.removeAll(newTimes);
        if (skipDelete != null) {
            timeInfo.removeAll(skipDelete);
        }
        List<GFERecord> gridsToRemove = new ArrayList<GFERecord>();
        for (TimeRange time : timeInfo) {
            if (time.overlaps(originalTimeRange)) {
                gridsToRemove.add(new GFERecord(id, time));
            }
        }

        if (!gridsToRemove.isEmpty()) {
            for (GFERecord toRemove : gridsToRemove) {
                removeFromDb(toRemove);
                removeFromHDF5(toRemove);
            }
        }

        boolean hdf5SaveSuccess = false;
        GFERecord[] failedGrids = null;

        try {
            failedGrids = saveGridsToHdf5(records);
            hdf5SaveSuccess = true;
        } catch (GfeException e) {
            statusHandler.handle(Priority.PROBLEM, "Error saving to hdf5", e);
            sr.addMessage("Error accessing HDF5.  Data not saved.");
        }

        // Save off the individual failures (if any), and then save what we can
        if (failedGrids != null && failedGrids.length > 0) {
            for (GFERecord gfeRecord : failedGrids) {
                sr.addMessage("Failed to save grid to HDF5: " + gfeRecord);
            }
        }

        if (hdf5SaveSuccess) {

            GFERecord[] gridsToStore = records.toArray(new GFERecord[records
                    .size()]);
            if (failedGrids != null && failedGrids.length > 0) {
                Set<GFERecord> workingSet = new HashSet<GFERecord>(records);
                workingSet.removeAll(Arrays.asList(failedGrids));
                gridsToStore = workingSet.toArray(new GFERecord[workingSet
                        .size()]);
            }
            try {
                failedGrids = saveGridToDb(gridsToStore);
                for (GFERecord rec : failedGrids) {
                    // already logged at a lower level
                    String msg = "Error saving grid " + rec.toString();
                    sr.addMessage(msg);
                    newTimes.remove(rec.getTimeRange());
                    histories.remove(rec.getTimeRange());
                    removeFromHDF5(rec);
                }
            } catch (DataAccessLayerException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error persisting grids", e);
                String msg = "General grid persistence error: "
                        + e.getMessage();
                sr.addMessage(msg);
            }
        }

        sr.addNotifications(new GridUpdateNotification(id, originalTimeRange,
                histories, requesterId, id.getDbId().getSiteId()));

        return sr;
    }

    @Override
    public ServerResponse<?> saveGridSlices(ParmID parmId,
            TimeRange originalTimeRange, List<IGridSlice> slices,
            WsId requesterId, List<TimeRange> skipDelete) {
        ServerResponse<?> sr = new ServerResponse<String>();

        List<GFERecord> records = new ArrayList<GFERecord>();
        for (IGridSlice slice : slices) {
            GFERecord rec = new GFERecord(parmId, slice.getValidTime());
            GridDataHistory[] newHistArray = new GridDataHistory[slice
                    .getHistory().length];
            for (int i = 0; i < slice.getHistory().length; i++) {
                GridDataHistory newHist = null;
                try {
                    newHist = slice.getHistory()[i].clone();
                    newHistArray[i] = newHist;
                } catch (CloneNotSupportedException e) {
                    statusHandler
                            .handle(Priority.PROBLEM,
                                    "Unable to clone GridDataHistory for saveGridSlices operation",
                                    e);
                }
            }

            rec.setGridHistory(newHistArray);
            rec.setMessageData(slice);
            records.add(rec);
        }
        sr.addMessages(this.saveGridData(parmId, originalTimeRange, records,
                requesterId, skipDelete));
        return sr;
    }

    /**
     * Determines if a record already exists for the corresponding time range.
     * If so, copies the data from the new record into the existing one so the
     * existing row can be used in the database.
     * 
     * @param parmId
     * @param records
     * @return the consolidated records
     */
    private List<GFERecord> consolidateWithExisting(ParmID parmId,
            List<GFERecord> records) {
        List<GFERecord> consolidated = new ArrayList<GFERecord>(records.size());
        List<TimeRange> times = new ArrayList<TimeRange>();
        for (GFERecord rec : records) {
            times.add(rec.getTimeRange());
        }

        GFEDao dao = null;
        try {
            dao = new GFEDao();
        } catch (PluginException e1) {
            statusHandler
                    .handle(Priority.PROBLEM, e1.getLocalizedMessage(), e1);
        }

        List<GFERecord> existingList = dao.getRecords(parmId, times);
        Map<TimeRange, GFERecord> map = new HashMap<TimeRange, GFERecord>(
                existingList.size());
        for (GFERecord rec : existingList) {
            map.put(rec.getTimeRange(), rec);
        }

        // don't make a new record if record already exists with matching time
        // range
        for (GFERecord rec : records) {
            GFERecord existing = map.get(rec.getTimeRange());
            if (existing == null) {
                consolidated.add(rec);
            } else {
                existing.setMessageData(rec.getMessageData());

                List<GridDataHistory> existHist = existing.getGridHistory();
                List<GridDataHistory> newHist = rec.getGridHistory();
                dao.consolidateHistories(existHist, newHist);

                consolidated.add(existing);
            }

        }

        return consolidated;
    }

    @Override
    public ServerResponse<GridParmInfo> getGridParmInfo(ParmID id) {
        ServerResponse<GridParmInfo> sr = new ServerResponse<GridParmInfo>();
        String compositeName = id.getCompositeName();
        GridParmInfo gpi = parmInfo.get(compositeName);
        if (gpi == null) {
            sr.addMessage("GridParmInfoNotFound - " + id.getCompositeName()
                    + " was not found in the database");
        }
        sr.setPayload(gpi);
        return sr;
    }

    private ServerResponse<List<IGridSlice>> getGridData(ParmID parmId,
            List<TimeRange> timeRanges, GridLocation location) {
        List<IGridSlice> data = new ArrayList<IGridSlice>(timeRanges.size());
        ServerResponse<List<IGridSlice>> sr = new ServerResponse<List<IGridSlice>>();
        ServerResponse<?> ssr = this.dbIsValid();
        sr.addMessages(ssr);

        ServerResponse<GridParmInfo> gpiSsr = getGridParmInfo(parmId);
        sr.addMessages(gpiSsr);
        GridParmInfo gpi = gpiSsr.getPayload().clone();
        gpi.setGridLoc(location);
        ServerResponse<Map<TimeRange, List<GridDataHistory>>> ssr2 = getGridHistory(
                parmId, timeRanges);
        if (!ssr2.isOkay()) {
            sr.addMessages(ssr2);
            return sr;
        }
        Map<TimeRange, List<GridDataHistory>> historyMap = ssr2.getPayload();

        String siteId = parmId.getDbId().getSiteId();
        IGridSlice slice = null;
        Grid2DFloat rawData = null;
        Grid2DFloat rawData2 = null;
        int i = 0;
        switch (gpi.getGridType()) {
        case SCALAR:
            FloatDataRecord[] records = null;
            try {
                records = this.retrieveFromHDF5(parmId, timeRanges);
            } catch (GfeException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error retrieving SCALAR data from HDF5", e);
                sr.addMessage("Error retrieving SCALAR data from HDF5");
                return sr;
            }
            i = 0;
            for (TimeRange time : timeRanges) {
                List<GridDataHistory> history = historyMap.get(time);
                rawData = new Grid2DFloat(gpi.getGridLoc().getNx(), gpi
                        .getGridLoc().getNy(), records[i++].getFloatData());
                slice = new ScalarGridSlice(time, gpi,
                        history.toArray(new GridDataHistory[history.size()]),
                        rawData);
                data.add(slice);
            }
            break;
        case VECTOR:
            FloatDataRecord[][] vecRecords = null;
            try {
                vecRecords = this.retrieveVectorFromHDF5(parmId, timeRanges);
            } catch (GfeException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error retrieving VECTOR data from HDF5", e);
                sr.addMessage("Error retrieving VECTOR data from HDF5");
                return sr;
            }
            i = 0;
            for (TimeRange time : timeRanges) {
                List<GridDataHistory> history = historyMap.get(time);
                rawData = new Grid2DFloat(gpi.getGridLoc().getNx(), gpi
                        .getGridLoc().getNy(), vecRecords[i][0].getFloatData());
                rawData2 = new Grid2DFloat(gpi.getGridLoc().getNx(), gpi
                        .getGridLoc().getNy(), vecRecords[i][1].getFloatData());
                slice = new VectorGridSlice(time, gpi,
                        history.toArray(new GridDataHistory[history.size()]),
                        rawData, rawData2);
                data.add(slice);
                i++;
            }
            break;
        case DISCRETE:
            ByteDataRecord[][] discreteRecords = null;
            try {
                discreteRecords = this.retrieveDiscreteFromHDF5(parmId,
                        timeRanges);
            } catch (GfeException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error retrieving DISCRETE data from HDF5", e);
                sr.addMessage("Error retrieving DISCRETE data from HDF5");
                return sr;
            }
            i = 0;
            for (TimeRange time : timeRanges) {
                List<GridDataHistory> history = historyMap.get(time);
                Grid2DByte rawDiscrete = new Grid2DByte(gpi.getGridLoc()
                        .getNx(), gpi.getGridLoc().getNy(),
                        discreteRecords[i][0].getByteData());
                byte[] discreteKeyData = discreteRecords[i][1].getByteData();
                String discreteKeysString = new String(discreteKeyData);
                String[] discreteSplit = discreteKeysString.split(Pattern
                        .quote(GfeUtil.KEY_SEPARATOR));

                DiscreteKey[] discreteKeyArray = new DiscreteKey[discreteSplit.length];
                for (int j = 0; j < discreteSplit.length; j++) {
                    discreteKeyArray[j] = new DiscreteKey(siteId,
                            discreteSplit[j], parmId);
                }
                slice = new DiscreteGridSlice(time, gpi,
                        history.toArray(new GridDataHistory[history.size()]),
                        rawDiscrete, discreteKeyArray);
                data.add(slice);
                i++;
            }
            break;
        case WEATHER:
            ByteDataRecord[][] wxRecords = null;
            try {
                wxRecords = this.retrieveDiscreteFromHDF5(parmId, timeRanges);
            } catch (GfeException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error retrieving WEATHER data from HDF5", e);
                sr.addMessage("Error retrieving WEATHER data from HDF5");
                return sr;
            }
            i = 0;
            for (TimeRange time : timeRanges) {
                List<GridDataHistory> history = historyMap.get(time);
                Grid2DByte rawWx = new Grid2DByte(gpi.getGridLoc().getNx(), gpi
                        .getGridLoc().getNy(), wxRecords[i][0].getByteData());
                byte[] wxKeyData = wxRecords[i][1].getByteData();
                String wxKeysString = new String(wxKeyData);
                String[] wxSplit = wxKeysString.split(Pattern
                        .quote(GfeUtil.KEY_SEPARATOR));
                WeatherKey[] keyArray = new WeatherKey[wxSplit.length];
                for (int j = 0; j < wxSplit.length; j++) {
                    keyArray[j] = new WeatherKey(siteId, wxSplit[j]);
                }
                slice = new WeatherGridSlice(time, gpi,
                        history.toArray(new GridDataHistory[history.size()]),
                        rawWx, keyArray);
                data.add(slice);
                i++;
            }
            break;
        default:
            break;
        }
        sr.setPayload(data);
        return sr;
    }

    @Override
    public ServerResponse<List<IGridSlice>> getGridData(ParmID id,
            List<TimeRange> timeRanges) {
        return this.getGridData(id, timeRanges, getGridParmInfo(id)
                .getPayload().getGridLoc());

    }

    @Override
    public void dbSizeStats() {

    }

    public void deleteDb() {
        // TODO Auto-generated method stub

    }

    public void dumpStatistics() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getProjectionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServerResponse<?> updateGridHistory(ParmID parmId,
            Map<TimeRange, List<GridDataHistory>> history) {
        ServerResponse<?> sr = new ServerResponse<String>();
        GFEDao dao = null;
        try {
            dao = (GFEDao) PluginFactory.getInstance().getPluginDao("gfe");
            dao.updateGridHistories(parmId, history);
        } catch (PluginException e1) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get gfe dao", e1);
        } catch (DataAccessLayerException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to update grid history!!", e);
        }
        return sr;

    }

    /**
     * Examines the user vs. the database projection and domains, and determines
     * if the database needs to be remapped to be in line with the desired
     * projection and domain. Performs the remapping of all data as needed.
     */
    private void applyProjectionAndDomainChanges() {

        try {
            IFPServerConfig serverConfig = IFPServerConfigManager
                    .getServerConfig(this.dbId.getSiteId());
            GridLocation currentGridLocation = serverConfig.dbDomain();
            if (currentGridLocation.getSiteId().equals("")) {
                currentGridLocation.setSiteId(this.getDbId().getSiteId());
            }

            for (String parmNameAndLevel : parmInfo.keySet()) {
                GridLocation storedGridLocation = parmInfo
                        .get(parmNameAndLevel).getGridLoc();
                String parmName = parmNameAndLevel.split("_")[0];
                String parmLevel = parmNameAndLevel.split("_")[1];
                ParmID currentParm = new ParmID(parmName, dbId, parmLevel);

                if (!storedGridLocation.equals(currentGridLocation)) {
                    GridParmInfo gpi = gridConfig.getGridParmInfo(parmName,
                            parmLevel);
                    if (gpi == null) {
                        continue;
                    }
                    ParmStorageInfo psi = gridConfig.getParmStorageInfo(
                            parmName, parmLevel);
                    this.storeGridParmInfo(gpi, psi, StoreOp.REPLACE);

                    GFEDao gfeDao = new GFEDao();

                    RemapGrid remap = new RemapGrid(storedGridLocation,
                            currentGridLocation, true);
                    ArrayList<GFERecord> records = gfeDao
                            .queryByParmID(currentParm);
                    if (!records.isEmpty()) {
                        statusHandler.handle(
                                Priority.WARN,
                                "Resolution has been changed for site: "
                                        + dbId.getSiteId()
                                        + ". Resampling parm ["
                                        + currentParm.toString() + "] ("
                                        + records.size()
                                        + " grids) to new resolution...");
                    }

                    for (GFERecord rec : records) {
                        List<TimeRange> times = new ArrayList<TimeRange>();
                        times.add(rec.getTimeRange());
                        IGridSlice slice = this
                                .getGridData(rec.getParmId(), times,
                                        storedGridLocation).getPayload().get(0);
                        IGridSlice newSlice = null;
                        try {
                            switch (slice.getGridInfo().getGridType()) {
                            case NONE:
                                break;
                            case SCALAR:
                                ScalarGridSlice scalarSlice = (ScalarGridSlice) slice;
                                Grid2DFloat newGrid = remap
                                        .remap(scalarSlice.getScalarGrid(),
                                                scalarSlice.getGridInfo()
                                                        .getMinValue(),
                                                scalarSlice.getGridInfo()
                                                        .getMaxValue(),
                                                scalarSlice.getGridInfo()
                                                        .getMinValue(),
                                                scalarSlice.getGridInfo()
                                                        .getMinValue());
                                scalarSlice.setGridParmInfo(this
                                        .getGridParmInfo(rec.getParmId())
                                        .getPayload());
                                scalarSlice.setScalarGrid(newGrid);
                                newSlice = scalarSlice;
                                break;
                            case VECTOR:
                                VectorGridSlice vectorSlice = (VectorGridSlice) slice;
                                Grid2DFloat magOutput = new Grid2DFloat(
                                        currentGridLocation.getNx(),
                                        currentGridLocation.getNy());
                                Grid2DFloat dirOutput = new Grid2DFloat(
                                        currentGridLocation.getNx(),
                                        currentGridLocation.getNy());
                                remap.remap(
                                        vectorSlice.getMagGrid(),
                                        vectorSlice.getDirGrid(),
                                        vectorSlice.getGridInfo().getMinValue(),
                                        vectorSlice.getGridInfo().getMaxValue(),
                                        vectorSlice.getGridInfo().getMinValue(),
                                        vectorSlice.getGridInfo().getMinValue(),
                                        magOutput, dirOutput);
                                vectorSlice.setGridParmInfo(this
                                        .getGridParmInfo(rec.getParmId())
                                        .getPayload());
                                vectorSlice.setDirGrid(dirOutput);
                                vectorSlice.setMagGrid(magOutput);
                                newSlice = vectorSlice;
                                break;
                            case WEATHER:
                                WeatherGridSlice weatherSlice = (WeatherGridSlice) slice;
                                Grid2DByte newWeatherGrid = remap.remap(
                                        weatherSlice.getWeatherGrid(), 0, 0);
                                weatherSlice.setGridParmInfo(this
                                        .getGridParmInfo(rec.getParmId())
                                        .getPayload());
                                weatherSlice.setWeatherGrid(newWeatherGrid);
                                newSlice = weatherSlice;
                                break;
                            case DISCRETE:
                                DiscreteGridSlice discreteSlice = (DiscreteGridSlice) slice;
                                Grid2DByte newDiscreteGrid = remap.remap(
                                        discreteSlice.getDiscreteGrid(), 0, 0);
                                discreteSlice.setGridParmInfo(this
                                        .getGridParmInfo(rec.getParmId())
                                        .getPayload());
                                discreteSlice.setDiscreteGrid(newDiscreteGrid);
                                newSlice = discreteSlice;
                                break;
                            }
                            rec.setMessageData(newSlice);
                            this.removeFromHDF5(rec);
                            this.saveGridsToHdf5(Arrays
                                    .asList(new GFERecord[] { rec }));
                        } catch (Exception e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Error remapping data for record [" + rec
                                            + "]", e);
                        }
                    }
                }
            }
        } catch (DataAccessLayerException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to query for grid location!", e);
        } catch (PluginException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to instantiate GFEDao!", e);
        } catch (GfeConfigurationException e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get config for "
                    + this.dbId.getSiteId(), e);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get config for "
                    + this.dbId.getSiteId(), e);
        }
    }

    private void getDBConfiguration() throws GfeException {
        this.parmInfo.clear();
        this.parmStorageInfo.clear();
        if (!parmInfoInitialized) {
            initGridParmInfo();
        }
        try {
            IDataStore ds = DataStoreFactory.getDataStore(GfeUtil.getHDF5File(
                    gfeBaseDataDir, this.dbId));

            IDataRecord[] parmInfoRecords = ds.retrieve(GRID_PARM_INFO_GRP);
            for (IDataRecord gpiRecord : parmInfoRecords) {
                GridParmInfo gpi = populateGpi(gpiRecord.getDataAttributes());
                parmInfo.put(gpiRecord.getName(), gpi);
            }
            IDataRecord[] parmStorageRecords = ds
                    .retrieve(GRID_PARM_STORAGE_INFO_GRP);
            for (IDataRecord psiRecord : parmStorageRecords) {
                ParmStorageInfo psi = populateStorageInfo(psiRecord
                        .getDataAttributes());
                parmStorageInfo.put(psiRecord.getName(), psi);
            }
        } catch (Exception e) {
            throw new GfeException("Error getting db configuration", e);
        }
    }

    private void compareParmInfoWithDB(Map<String, GridParmInfo> parmInfoUser,
            Map<String, ParmStorageInfo> parmStorageInfoUser,
            List<String> toBeAdded, List<String> toBeDeleted,
            List<String> toBeChanged) {

        // reset the returned list
        toBeAdded.clear();
        toBeDeleted.clear();
        toBeChanged.clear();

        // Check for entries to be added
        for (String key : parmInfoUser.keySet()) {
            if (!this.parmInfo.containsKey(key)) {
                toBeAdded.add(key);
            }
        }

        // check for entries to be deleted
        for (String key : this.parmInfo.keySet()) {
            if (!parmInfoUser.containsKey(key)) {
                toBeDeleted.add(key);
            }
        }

        // Check for entries to modified
        for (String key : parmInfoUser.keySet()) {
            GridParmInfo dbGpi, userGpi = null;
            if (this.parmInfo.containsKey(key)) {
                dbGpi = this.parmInfo.get(key);
                userGpi = parmInfoUser.get(key);
                if (!dbGpi.equals(userGpi)) {
                    toBeChanged.add(key);
                }
            }
            ParmStorageInfo dbPsi, userPsi = null;
            if (this.parmStorageInfo.containsKey(key)
                    && !toBeChanged.contains(key)) {
                dbPsi = this.parmStorageInfo.get(key);
                userPsi = parmStorageInfoUser.get(key);
                if (!dbPsi.equals(userPsi)) {
                    toBeChanged.add(key);
                }
            }
        }
    }

    /**
     * IFPGridDatabase::getUserConfiguration() This function, which is called at
     * construction time, reads all the information from the supplied
     * GridDbConfig class and creates information in the calling arguments
     * parmInfo, parmStorageInfo, and areaStorageInfo. These dictionaries are
     * used later by other functions in this class.
     * 
     * @param parmInfo
     *            Map of GridParmInfo
     * @param parmStorageInfo
     *            Map of ParmStorageInfo
     * @return True if the operation is successful, false
     */
    private boolean getUserConfiguration(Map<String, GridParmInfo> parmInfo,
            Map<String, ParmStorageInfo> parmStorageInfo) {
        parmInfo.clear();
        parmStorageInfo.clear();

        if (this.gridConfig == null) {
            return false;
        }
        List<String> nameLevel = this.gridConfig.parmAndLevelList();

        // For each variable get the info and stuff it in the correct object
        for (int i = 0; i < nameLevel.size(); i++) {

            Object[] gpiPsi = new Object[2];

            if (getUserConfigForWE(nameLevel.get(i), gpiPsi)) {
                parmInfo.put(nameLevel.get(i), (GridParmInfo) gpiPsi[0]);
                parmStorageInfo.put(nameLevel.get(i),
                        (ParmStorageInfo) gpiPsi[1]);
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "GPI/PSI attributes not found in configuration for "
                                + nameLevel.get(i) + ".");
                return false;
            }
        }
        return true;
    }

    /**
     * This function reads the elements of a GridParmInfo and ParmStorageInfo
     * from the GridDBConfig for a single weather element (compositeName).
     */
    private boolean getUserConfigForWE(String compositeName, Object[] gpiPsi) {

        String[] nameLevel = splitNameAndLevel(compositeName);
        GridParmInfo gpi = null;

        if (gridConfig == null) {
            statusHandler.handle(Priority.DEBUG, toString()
                    + " has no GridParmInfo config");
            return false;
        } else {
            gpi = this.gridConfig.getGridParmInfo(nameLevel[0], nameLevel[1]);
        }

        if (gpi == null) {
            statusHandler.handle(Priority.DEBUG, compositeName
                    + " not found in GridParmInfo config");
            return false;
        }

        gpi.resetParmID(new ParmID(nameLevel[0], this.dbId, nameLevel[1]));

        // get the ParmStorageInfo
        ParmStorageInfo psi = this.gridConfig.getParmStorageInfo(nameLevel[0],
                nameLevel[1]);
        if (psi == null) {
            statusHandler.handle(Priority.DEBUG, compositeName
                    + " not found in ParmStorageInfo config");
            return false;
        }

        gpiPsi[0] = gpi;
        gpiPsi[1] = psi;
        return true;
    }

    public String[] splitNameAndLevel(String parmAndLevel) {

        String[] retValue = parmAndLevel.split("_");

        if (retValue.length == 1) {
            return new String[] { retValue[0], "SFC" };
        } else {
            return retValue;
        }
    }

    private ServerResponse<?> dbIsValid() {
        ServerResponse<?> sr = new ServerResponse<String>();
        if (dbId == null || !dbId.isValid()) {
            sr.addMessage("DBInvalid - The database is not valid.");
        }
        return sr;
    }

    private ServerResponse<?> validGridTimes(final List<GFERecord> grids,
            final TimeRange validTR) {
        ServerResponse<?> sr = new ServerResponse<String>();

        // Make sure every grid's TR falls within the validTR
        for (int j = 0; j < grids.size(); j++) {
            if (!validTR.contains(grids.get(j).getTimeRange())) {
                sr.addMessage("Grid times not valid");
                return sr;
            }
        }
        return sr;
    }

    /**
     * Gets the HDF5 file containing the grid parm info. Initializes the info if
     * necessary
     * 
     * @return The HDF5 file
     */
    protected void initGridParmInfo() {
        IDataStore ds = DataStoreFactory.getDataStore(GfeUtil.getHDF5File(
                gfeBaseDataDir, this.dbId));

        try {
            if (gridConfig != null && gridConfig.parmAndLevelList().size() > 0) {
                ds.getDatasets(GRID_PARM_INFO_GRP);
                parmInfoInitialized = true;
            }
        } catch (Exception e1) {
            if (gridConfig != null) {
                try {
                    List<String> parmsAndLevels = gridConfig.parmAndLevelList();
                    String[] parmAndLevel = null;
                    List<GridParmInfo> gpis = new ArrayList<GridParmInfo>();
                    List<ParmStorageInfo> psis = new ArrayList<ParmStorageInfo>();
                    for (String parm : parmsAndLevels) {
                        parmAndLevel = this.splitNameAndLevel(parm);
                        gpis.add(gridConfig.getGridParmInfo(parmAndLevel[0],
                                parmAndLevel[1]));
                        psis.add(gridConfig.getParmStorageInfo(parmAndLevel[0],
                                parmAndLevel[1]));
                    }
                    storeGridParmInfo(gpis, psis, StoreOp.STORE_ONLY);
                    parmInfoInitialized = true;
                } catch (Exception e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error getting GridParmInfo file", e);
                    parmInfoInitialized = false;
                }
            }
        }
    }

    private ParmStorageInfo populateStorageInfo(
            Map<String, Object> dataAttributes) {

        String storageType = (String) dataAttributes.get("storageType");
        Point gridSize = new Point((Integer) dataAttributes.get("gridSize.x"),
                (Integer) dataAttributes.get("gridSize.y"));

        String parmName = (String) dataAttributes.get("parmName");
        String level = (String) dataAttributes.get("level");
        Float dataOffset = (Float) dataAttributes.get("dataOffset");
        Float dataMultiplier = (Float) dataAttributes.get("dataMultiplier");
        String dataType = (String) dataAttributes.get("dataType");

        ParmStorageInfo storageInfo = new ParmStorageInfo(dataType, gridSize,
                parmName, level, dataOffset, dataMultiplier, storageType);
        return storageInfo;

    }

    protected GridParmInfo populateGpi(Map<String, Object> dataAttributes)
            throws Exception {

        GridParmInfo gpi = new GridParmInfo();
        TimeConstraints tc = new TimeConstraints();
        GridLocation location = new GridLocation();
        ProjectionData pd = new ProjectionData();

        pd.setProjectionID((String) dataAttributes
                .get("gridLoc.projection.projectionID"));
        pd.setProjectionType(ProjectionType.valueOf((String) dataAttributes
                .get("gridLoc.projection.projectionType")));
        pd.setLatLonLL(new Coordinate((Float) dataAttributes
                .get("gridLoc.projection.latLonLL.x"), (Float) dataAttributes
                .get("gridLoc.projection.latLonLL.y")));
        pd.setLatLonUR(new Coordinate((Float) dataAttributes
                .get("gridLoc.projection.latLonUR.x"), (Float) dataAttributes
                .get("gridLoc.projection.latLonUR.y")));
        pd.setLatLonOrigin(new Coordinate((Float) dataAttributes
                .get("gridLoc.projection.latLonOrigin.x"),
                (Float) dataAttributes.get("gridLoc.projection.latLonOrigin.y")));
        pd.setStdParallelOne((Float) dataAttributes
                .get("gridLoc.projection.stdParallelOne"));
        pd.setStdParallelTwo((Float) dataAttributes
                .get("gridLoc.projection.stdParallelTwo"));
        pd.setGridPointLL(new Point((Integer) dataAttributes
                .get("gridLoc.projection.gridPointLL.x"),
                (Integer) dataAttributes
                        .get("gridLoc.projection.gridPointLL.y")));
        pd.setGridPointUR(new Point((Integer) dataAttributes
                .get("gridLoc.projection.gridPointUR.x"),
                (Integer) dataAttributes
                        .get("gridLoc.projection.gridPointUR.y")));
        pd.setLatIntersect((Float) dataAttributes
                .get("gridLoc.projection.latIntersect"));
        pd.setLonCenter((Float) dataAttributes
                .get("gridLoc.projection.lonCenter"));
        pd.setLonOrigin((Float) dataAttributes
                .get("gridLoc.projection.lonOrigin"));

        location.setSiteId((String) dataAttributes.get("gridLoc.siteID"));
        location.setNx((Integer) dataAttributes.get("gridLoc.nx"));
        location.setNy((Integer) dataAttributes.get("gridLoc.ny"));
        location.setTimeZone((String) dataAttributes.get("gridLoc.timeZone"));
        location.setOrigin(new Coordinate((Float) dataAttributes
                .get("gridLoc.origin.x"), (Float) dataAttributes
                .get("gridLoc.origin.y")));
        location.setExtent(new Coordinate((Float) dataAttributes
                .get("gridLoc.extent.x"), (Float) dataAttributes
                .get("gridLoc.extent.y")));
        location.setGeometry(new WKTReader().read((String) dataAttributes
                .get("gridLoc.geometry")));
        location.setCrsWKT((String) dataAttributes.get("gridLoc.crs"));
        location.setProjection(pd);

        tc.setDuration((Integer) dataAttributes.get("timeConstraints.duration"));
        tc.setRepeatInterval((Integer) dataAttributes
                .get("timeConstraints.repeatInterval"));
        tc.setStartTime((Integer) dataAttributes
                .get("timeConstraints.startTime"));

        gpi.setParmID(new ParmID((String) dataAttributes.get("parmID")));
        gpi.setGridType(GridType.valueOf((String) dataAttributes
                .get("gridType")));
        gpi.setDescriptiveName((String) dataAttributes.get("descriptiveName"));
        gpi.setUnitString((String) dataAttributes.get("unitString"));
        gpi.setMaxValue((Float) dataAttributes.get("maxValue"));
        gpi.setMinValue((Float) dataAttributes.get("minValue"));
        gpi.setPrecision((Integer) dataAttributes.get("precision"));
        gpi.setRateParm((Boolean) dataAttributes.get("rateParm"));
        gpi.setTimeIndependentParm((Boolean) dataAttributes
                .get("timeIndependentParm"));
        gpi.setTimeConstraints(tc);
        gpi.setGridLoc(location);

        return gpi;
    }

    private Map<String, Object> getStorageInfoAsMap(ParmStorageInfo info) {
        Map<String, Object> dataAttributes = new HashMap<String, Object>();
        dataAttributes.put("storageType", info.storageType());
        dataAttributes.put("gridSize.x", info.gridSize().x);
        dataAttributes.put("gridSize.y", info.gridSize().y);
        dataAttributes.put("storageAreaName", info.storageAreaName());
        dataAttributes.put("parmName", info.parmName());
        dataAttributes.put("level", info.level());
        dataAttributes.put("dataOffset", info.dataOffset());
        dataAttributes.put("dataMultiplier", info.dataMultiplier());
        dataAttributes.put("dataType", info.dataType());
        return dataAttributes;
    }

    protected Map<String, Object> getGpiAsMap(GridParmInfo gpi)
            throws Exception {

        Map<String, Object> dataAttributes = new HashMap<String, Object>();

        dataAttributes.put("parmID", gpi.getParmID().toString());
        dataAttributes.put("gridType", gpi.getGridType().toString());
        dataAttributes.put("descriptiveName", gpi.getDescriptiveName());
        dataAttributes.put("unitString", gpi.getUnitString());
        dataAttributes.put("minValue", gpi.getMinValue());
        dataAttributes.put("maxValue", gpi.getMaxValue());
        dataAttributes.put("precision", gpi.getPrecision());
        dataAttributes.put("rateParm", gpi.isRateParm());
        dataAttributes.put("timeIndependentParm", gpi.isTimeIndependentParm());

        TimeConstraints tc = gpi.getTimeConstraints();
        dataAttributes.put("timeConstraints.duration", tc.getDuration());
        dataAttributes.put("timeConstraints.repeatInterval",
                tc.getRepeatInterval());
        dataAttributes.put("timeConstraints.startTime", tc.getStartTime());

        GridLocation location = gpi.getGridLoc();
        dataAttributes.put("gridLoc.siteID", location.getSiteId());
        dataAttributes.put("gridLoc.nx", location.getNx());
        dataAttributes.put("gridLoc.ny", location.getNy());
        dataAttributes.put("gridLoc.timeZone", location.getTimeZone());
        dataAttributes.put("gridLoc.origin.x", location.getOrigin().x);
        dataAttributes.put("gridLoc.origin.y", location.getOrigin().y);
        dataAttributes.put("gridLoc.extent.x", location.getExtent().x);
        dataAttributes.put("gridLoc.extent.y", location.getExtent().y);
        dataAttributes.put("gridLoc.geometry", location.getGeometry().toText());
        dataAttributes.put("gridLoc.crs", location.getCrsWKT());

        ProjectionData proj = location.getProjection();
        dataAttributes.put("gridLoc.projection.projectionID",
                proj.getProjectionID());
        dataAttributes.put("gridLoc.projection.projectionType", proj
                .getProjectionType().toString());
        dataAttributes.put("gridLoc.projection.latLonLL.x",
                proj.getLatLonLL().x);
        dataAttributes.put("gridLoc.projection.latLonLL.y",
                proj.getLatLonLL().y);
        dataAttributes.put("gridLoc.projection.latLonUR.x",
                proj.getLatLonUR().x);
        dataAttributes.put("gridLoc.projection.latLonUR.y",
                proj.getLatLonUR().y);
        dataAttributes.put("gridLoc.projection.latLonOrigin.x",
                proj.getLatLonOrigin().x);
        dataAttributes.put("gridLoc.projection.latLonOrigin.y",
                proj.getLatLonOrigin().y);
        dataAttributes.put("gridLoc.projection.stdParallelOne",
                proj.getStdParallelOne());
        dataAttributes.put("gridLoc.projection.stdParallelTwo",
                proj.getStdParallelTwo());
        dataAttributes.put("gridLoc.projection.gridPointLL.x",
                proj.getGridPointLL().x);
        dataAttributes.put("gridLoc.projection.gridPointLL.y",
                proj.getGridPointLL().y);
        dataAttributes.put("gridLoc.projection.gridPointUR.x",
                proj.getGridPointUR().x);
        dataAttributes.put("gridLoc.projection.gridPointUR.y",
                proj.getGridPointUR().y);
        dataAttributes.put("gridLoc.projection.latIntersect",
                proj.getLatIntersect());
        dataAttributes.put("gridLoc.projection.lonCenter", proj.getLonCenter());
        dataAttributes.put("gridLoc.projection.lonOrigin", proj.getLonOrigin());
        return dataAttributes;
    }

    public GFERecord[] saveGridsToHdf5(List<GFERecord> dataObjects)
            throws GfeException {
        return saveGridsToHdf5(dataObjects, null);
    }

    /**
     * Saves GFERecords to the HDF5 repository
     * 
     * @param rec
     *            The GFERecords to be saved
     * @return Returns records that failed to store
     * @throws GfeException
     *             If errors occur during the interaction with the HDF5
     *             repository
     */
    public GFERecord[] saveGridsToHdf5(List<GFERecord> dataObjects,
            ParmStorageInfo parmStorageInfo) throws GfeException {
        List<GFERecord> failedGrids = new ArrayList<GFERecord>();
        try {
            Map<File, List<GFERecord>> recordMap = new HashMap<File, List<GFERecord>>();

            for (GFERecord rec : dataObjects) {
                File file = GfeUtil.getHDF5File(gfeBaseDataDir, rec.getParmId()
                        .getDbId());
                List<GFERecord> recList = recordMap.get(file);
                if (recList == null) {
                    recList = new ArrayList<GFERecord>();
                    recordMap.put(file, recList);
                }
                recList.add(rec);
            }

            for (Entry<File, List<GFERecord>> entry : recordMap.entrySet()) {
                IDataStore dataStore = DataStoreFactory.getDataStore(entry
                        .getKey());
                // The correlation map is used to detect which gferecord had
                // a problem if an error occurs
                Map<IDataRecord, GFERecord> correlationMap = new HashMap<IDataRecord, GFERecord>();
                for (GFERecord rec : entry.getValue()) {
                    Object data = rec.getMessageData();
                    String groupName = GfeUtil.getHDF5Group(rec.getParmId(),
                            rec.getDataTime().getValidPeriod());

                    if (parmStorageInfo == null) {
                        parmStorageInfo = findStorageInfo(rec.getParmId());
                    }
                    // Get storage info (for float and vector data)
                    String storageType = parmStorageInfo.storageType();

                    if (data instanceof FloatDataRecord
                            && !"float".equals(storageType)) {
                        storeConvertedFloatRecord((FloatDataRecord) data,
                                dataStore, groupName, parmStorageInfo,
                                correlationMap, rec);
                    } else if (data instanceof IDataRecord) {
                        // store without conversion
                        ((IDataRecord) data).setGroup(groupName);
                        dataStore.addDataRecord((IDataRecord) data);
                        correlationMap.put(((IDataRecord) data), rec);
                    } else if (data instanceof VectorGridSlice) {
                        storeVectorGridSlice(data, dataStore, groupName,
                                parmStorageInfo, correlationMap, rec);
                    } else if (data instanceof ScalarGridSlice) {
                        storeScalarGridSlice(data, dataStore, groupName,
                                parmStorageInfo, correlationMap, rec);
                    } else if (data instanceof DiscreteGridSlice) {
                        storeDiscreteGridSlice(data, dataStore, groupName,
                                parmStorageInfo, correlationMap, rec);
                    } else if (data instanceof WeatherGridSlice) {
                        WeatherGridSlice slice = (WeatherGridSlice) data;
                        if (slice.getWeatherGrid() != null) {
                            byte[] rawData = slice.getWeatherGrid().getBuffer()
                                    .array();
                            ByteDataRecord rawRecord = new ByteDataRecord(
                                    "Data", groupName, rawData, 2, new long[] {
                                            slice.getGridInfo().getGridLoc()
                                                    .getNx(),
                                            slice.getGridInfo().getGridLoc()
                                                    .getNy() });
                            dataStore.addDataRecord(rawRecord);

                            StringBuffer sb = new StringBuffer();
                            boolean first = true;
                            for (WeatherKey key : slice.getKeys()) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(GfeUtil.KEY_SEPARATOR);
                                }
                                sb.append(key.toString());
                            }
                            byte[] keyBytes = sb.toString().getBytes();
                            ByteDataRecord keyRecord = new ByteDataRecord(
                                    "Keys", groupName, keyBytes, 1,
                                    new long[] { keyBytes.length });
                            dataStore.addDataRecord(keyRecord);
                            correlationMap.put(rawRecord, rec);
                            correlationMap.put(keyRecord, rec);
                        }
                    }
                }

                StorageStatus ss = dataStore.store(StoreOp.REPLACE);
                StorageException[] exceptions = ss.getExceptions();
                if (exceptions != null && exceptions.length > 0) {
                    // Describe the errors, then
                    // only log the first one, don't flood the log with
                    // duplicates.
                    statusHandler
                            .handle(Priority.PROBLEM,
                                    "Storage exceptions occurred during hdf5 save.  "
                                            + exceptions.length
                                            + " errors occurred.  The first failure will be logged.");
                    boolean first = true;
                    for (StorageException se : exceptions) {
                        IDataRecord recordWithException = se.getRecord();
                        GFERecord rec = correlationMap.get(recordWithException);
                        failedGrids.add(rec);
                        if (first) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Failed to save grid: ", se);
                            first = false;
                        }
                    }
                }

            }

        } catch (StorageException e) {
            throw new GfeException("Error storing to HDF5", e);
        }
        return failedGrids.toArray(new GFERecord[failedGrids.size()]);
    }

    /**
     * Store a grid slice of scalar data.
     * 
     * @param data
     *            The scalar grid slice to store.
     * @param dataStore
     *            The data store in which to put the slice.
     * @param groupName
     *            The group name under which to store the slice.
     * @param parmStorageInfo
     *            A structure that describes conversions on the data.
     * @param correlationMap
     *            Used for failed storage diagnostics.
     * @param rec
     *            The GFERecord the slice is associated with.
     * @throws StorageException
     */
    protected void storeScalarGridSlice(Object data, IDataStore dataStore,
            String groupName, ParmStorageInfo parmStorageInfo,
            Map<IDataRecord, GFERecord> correlationMap, GFERecord rec)
            throws StorageException {
        ScalarGridSlice slice = (ScalarGridSlice) data;
        if (slice.getScalarGrid() != null) {
            float[] rawData = slice.getScalarGrid().getFloats();
            FloatDataRecord rawRecord = new FloatDataRecord("Data", groupName,
                    rawData, 2, new long[] {
                            slice.getGridInfo().getGridLoc().getNx(),
                            slice.getGridInfo().getGridLoc().getNy() });
            this.storeConvertedFloatRecord(rawRecord, dataStore, groupName,
                    parmStorageInfo, correlationMap, rec);
        }
    }

    /**
     * Store a grid slice of vector data.
     * 
     * @param data
     *            The vector grid slice
     * @param dataStore
     *            The data store in which to save the slice
     * @param groupName
     *            The group name under which to save the slice
     * @param parmStorageInfo
     *            A structure which describes conversions to be done at
     *            storage/retrieval time
     * @param correlationMap
     *            Used for failed save diagnostics
     * @param rec
     *            The GFERecord associated with the slice
     * @throws StorageException
     */
    protected void storeVectorGridSlice(Object data, IDataStore dataStore,
            String groupName, ParmStorageInfo parmStorageInfo,
            Map<IDataRecord, GFERecord> correlationMap, GFERecord rec)
            throws StorageException {
        VectorGridSlice slice = (VectorGridSlice) data;
        if (slice.getMagGrid() != null || slice.getDirGrid() != null) {
            float[] rawMagData = slice.getMagGrid().getFloats();
            float[] rawDirData = slice.getDirGrid().getFloats();
            FloatDataRecord rawMagRecord = new FloatDataRecord("Mag",
                    groupName, rawMagData, 2, new long[] {
                            slice.getGridInfo().getGridLoc().getNx(),
                            slice.getGridInfo().getGridLoc().getNy() });

            FloatDataRecord rawDirRecord = new FloatDataRecord("Dir",
                    groupName, rawDirData, 2, new long[] {
                            slice.getGridInfo().getGridLoc().getNx(),
                            slice.getGridInfo().getGridLoc().getNy() });

            // Direction grids use the same storage type as the mag grid,
            // but offset is always 0 and multiplier is always 0.5.
            ParmStorageInfo dirStorageInfo = new ParmStorageInfo(
                    parmStorageInfo.dataType(), parmStorageInfo.gridSize(),
                    parmStorageInfo.parmName(), parmStorageInfo.level(),
                    VECTOR_DIR_DATA_OFFSET, VECTOR_DIR_DATA_MULTIPLIER,
                    parmStorageInfo.storageType());
            this.storeConvertedFloatRecord(rawMagRecord, dataStore, groupName,
                    parmStorageInfo, correlationMap, rec);
            this.storeConvertedFloatRecord(rawDirRecord, dataStore, groupName,
                    dirStorageInfo, correlationMap, rec);
        }
    }

    /**
     * Store a grid slice of discrete data.
     * 
     * @param data
     *            The discrete grid slice
     * @param dataStore
     *            The data store in which to save the slice
     * @param groupName
     *            The group name under which to save the slice
     * @param parmStorageInfo
     *            A structure which describes conversions to be done at
     *            storage/retrieval time
     * @param correlationMap
     *            Used for failed save diagnostics
     * @param rec
     *            The GFERecord associated with the slice
     * @throws StorageException
     */
    protected void storeDiscreteGridSlice(Object data, IDataStore dataStore,
            String groupName, ParmStorageInfo parmStorageInfo,
            Map<IDataRecord, GFERecord> correlationMap, GFERecord rec)
            throws StorageException {
        DiscreteGridSlice slice = (DiscreteGridSlice) data;
        if (slice.getDiscreteGrid() != null) {
            byte[] rawData = slice.getDiscreteGrid().getBuffer().array();
            ByteDataRecord rawRecord = new ByteDataRecord("Data", groupName,
                    rawData, 2, new long[] {
                            slice.getGridInfo().getGridLoc().getNx(),
                            slice.getGridInfo().getGridLoc().getNy() });
            dataStore.addDataRecord(rawRecord);

            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (DiscreteKey key : slice.getKey()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(GfeUtil.KEY_SEPARATOR);
                }
                sb.append(key.toString());
            }
            byte[] keyBytes = sb.toString().getBytes();
            ByteDataRecord keyRecord = new ByteDataRecord("Keys", groupName,
                    keyBytes, 1, new long[] { keyBytes.length });
            dataStore.addDataRecord(keyRecord);
            correlationMap.put(rawRecord, rec);
            correlationMap.put(keyRecord, rec);
        }
    }

    /**
     * Store a grid slice of weather data.
     * 
     * @param data
     *            The weather grid slice
     * @param dataStore
     *            The data store in which to save the slice
     * @param groupName
     *            The group name under which to save the slice
     * @param parmStorageInfo
     *            A structure which describes conversions to be done at
     *            storage/retrieval time
     * @param correlationMap
     *            Used for failed save diagnostics
     * @param rec
     *            The GFERecord associated with the slice
     * @throws StorageException
     */
    protected void storeWeatherGridSlice(Object data, IDataStore dataStore,
            String groupName, ParmStorageInfo parmStorageInfo,
            Map<IDataRecord, GFERecord> correlationMap, GFERecord rec)
            throws StorageException {

    }

    /**
     * Write a FloatDataRecord to the data store, converting it to another
     * format if parmStorageInfo indicates that it should be.
     * 
     * @param data
     *            the FloatDataRecord to convert
     * @param dataStore
     *            The data store in which to put the data
     * @param groupName
     *            The group name to assign to the stored record
     * @param parmStorageInfo
     *            Information on how the data should be converted
     * @param correlationMap
     *            Used to look up failed saves
     * @param rec
     *            The GFE record being stored
     */
    protected void storeConvertedFloatRecord(FloatDataRecord data,
            IDataStore dataStore, String groupName,
            ParmStorageInfo parmStorageInfo,
            Map<IDataRecord, GFERecord> correlationMap, GFERecord rec)
            throws StorageException {

        float[] fdata = data.getFloatData();
        String storageType = parmStorageInfo.storageType();
        float offset = parmStorageInfo.dataOffset();
        float multiplier = parmStorageInfo.dataMultiplier();
        float fcvt;
        IDataRecord storeDataRec = null;
        if ("short".equals(storageType) && multiplier != 0.0f) {
            short[] converted = new short[fdata.length];
            for (int i = 0; i < fdata.length; i++) {
                fcvt = (fdata[i] - offset) * multiplier;
                converted[i] = (short) ((fcvt >= 0.0f) ? fcvt + 0.5
                        : fcvt - 0.5);
            }
            storeDataRec = new ShortDataRecord(data.getName(), data.getGroup(),
                    converted, data.getDimension(), data.getSizes().clone());
        } else if ("byte".equals(storageType) && multiplier != 0.0f) {
            byte[] converted = new byte[fdata.length];
            for (int i = 0; i < fdata.length; i++) {
                fcvt = (fdata[i] - offset) * multiplier;
                converted[i] = (byte) ((fcvt >= 0.0f) ? fcvt + 0.5 : fcvt - 0.5);
            }
            storeDataRec = new ByteDataRecord(data.getName(), data.getGroup(),
                    converted, data.getDimension(), data.getSizes().clone());
        } else {
            storeDataRec = data;
        }

        storeDataRec.setGroup(groupName);
        dataStore.addDataRecord(storeDataRec);
        correlationMap.put(storeDataRec, rec);
    }

    /**
     * Saves a single GFERecord to the database
     * 
     * @param record
     *            The GFERecord(s) to be saved
     * @param requestorId
     *            The workstationID of the requestor
     * @return failed grids
     * @throws DataAccessLayerException
     */
    public GFERecord[] saveGridToDb(GFERecord... record)
            throws DataAccessLayerException {
        GFEDao dao = null;
        try {
            dao = (GFEDao) PluginFactory.getInstance().getPluginDao("gfe");
        } catch (PluginException e1) {
            throw new DataAccessLayerException("Unable to get gfe dao", e1);
        }
        try {
            return dao.saveOrUpdate(record);
        } catch (DataAccessException e) {
            throw new DataAccessLayerException(
                    "Error saving GFE grid to database", e);
        }
    }

    @Override
    public FloatDataRecord[] retrieveFromHDF5(ParmID parmId,
            List<TimeRange> times) throws GfeException {
        FloatDataRecord[] scalarData = new FloatDataRecord[times.size()];
        IDataStore dataStore = getDataStore(parmId);
        String groups[] = GfeUtil.getHDF5Groups(parmId, times);

        try {
            IDataRecord[] rawData = dataStore.retrieveGroups(groups,
                    Request.ALL);
            if (rawData.length != times.size()) {
                throw new IllegalArgumentException(
                        "Invalid number of dataSets returned expected 1 per group, received: "
                                + (rawData.length / times.size()));
            }

            for (int i = 0; i < rawData.length; i++) {
                IDataRecord rec = rawData[i];
                if (rec instanceof FloatDataRecord) {
                    scalarData[i] = (FloatDataRecord) rec;
                } else if (gridConfig == null) {
                    throw new IllegalArgumentException("Data array for "
                            + parmId.getParmName() + " "
                            + parmId.getParmLevel()
                            + " is not a float array, but database "
                            + toString()
                            + " does not contain a grid configuration.");
                } else {
                    // Convert to a FloatDataRecord for internal use
                    ParmStorageInfo psi = parmStorageInfo.get(parmId
                            .getCompositeName());
                    scalarData[i] = storageToFloat(rec, psi);
                }
            }
        } catch (Exception e) {
            throw new GfeException("Unable to get data from HDF5 for ParmID: "
                    + parmId + " TimeRange: " + times, e);
        }

        return scalarData;
    }

    @Override
    public FloatDataRecord[][] retrieveVectorFromHDF5(ParmID parmId,
            List<TimeRange> times) throws GfeException {
        FloatDataRecord[][] vectorData = new FloatDataRecord[times.size()][2];
        IDataStore dataStore = getDataStore(parmId);
        String groups[] = GfeUtil.getHDF5Groups(parmId, times);
        try {
            IDataRecord[] rawData = dataStore.retrieveGroups(groups,
                    Request.ALL);
            if (rawData.length / 2 != times.size()) {
                throw new IllegalArgumentException(
                        "Invalid number of dataSets returned expected 2 per group, received: "
                                + (rawData.length / times.size()));
            }

            for (int i = 0; i < rawData.length; i += 2) {
                IDataRecord magRec = null;
                IDataRecord dirRec = null;
                for (int j = 0; j < 2; j++) {
                    IDataRecord rec = rawData[i + j];
                    if ("Mag".equals(rec.getName())) {
                        magRec = rec;
                    } else if ("Dir".equals(rec.getName())) {
                        dirRec = rec;
                    } else {
                        throw new IllegalArgumentException(
                                "Unknown dataset retrieved for vector data.  Valid values: Mag, Dir  Received: "
                                        + rec.getName());
                    }
                }

                if (magRec.getClass() == dirRec.getClass()) {
                    if (magRec instanceof FloatDataRecord) {
                        vectorData[i / 2][0] = (FloatDataRecord) magRec;
                        vectorData[i / 2][1] = (FloatDataRecord) dirRec;
                    } else if (gridConfig == null) {
                        throw new IllegalArgumentException("Data array for "
                                + parmId.getParmName() + " "
                                + parmId.getParmLevel()
                                + " is not a float array, but database "
                                + toString()
                                + " does not contain a grid configuration.");
                    } else {
                        ParmStorageInfo magStorageInfo = parmStorageInfo
                                .get(parmId.getCompositeName());
                        ParmStorageInfo dirStorageInfo = new ParmStorageInfo(
                                magStorageInfo.dataType(),
                                magStorageInfo.gridSize(),
                                magStorageInfo.parmName(),
                                magStorageInfo.level(), VECTOR_DIR_DATA_OFFSET,
                                VECTOR_DIR_DATA_MULTIPLIER,
                                magStorageInfo.storageType());
                        vectorData[i / 2][0] = storageToFloat(magRec,
                                magStorageInfo);
                        vectorData[i / 2][1] = storageToFloat(dirRec,
                                dirStorageInfo);
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Magnitude and direction grids are not of the same type.");
                }
            }
        } catch (Exception e) {
            throw new GfeException("Unable to get data from HDF5 for ParmID: "
                    + parmId + " TimeRange: " + times, e);
        }

        return vectorData;
    }

    /**
     * Convert a data record to an equivalent FloatDataRecord. Assumes that
     * rawData and parmStorageInfo are non-null, rawData is not already a
     * FloatDataRecord, and parmStorageInfo.dataMultiplier() is nonzero.
     * 
     * @param rawData
     *            The data as stored in the HDF5 file.
     * @param parmStorageInfo
     *            Describes how data is converted in storage.
     * @return rawData, converted to a FloatDataRecord according to the
     *         conversion in parmStorageInfo.
     */
    protected FloatDataRecord storageToFloat(IDataRecord rawData,
            ParmStorageInfo parmStorageInfo) {
        FloatDataRecord data;
        String storageType = parmStorageInfo.storageType();
        float multiplier = parmStorageInfo.dataMultiplier();
        float offset = parmStorageInfo.dataOffset();
        float[] floats = null;
        if ("byte".equals(storageType)) {
            byte[] rawBytes = ((ByteDataRecord) rawData).getByteData();
            floats = new float[rawBytes.length];
            for (int idx = 0; idx < rawBytes.length; idx++) {
                // hex mask to treat bytes as unsigned
                floats[idx] = (rawBytes[idx] & 0xff) / multiplier + offset;
            }
        } else if ("short".equals(storageType)) {
            short[] rawShorts = ((ShortDataRecord) rawData).getShortData();
            floats = new float[rawShorts.length];
            for (int idx = 0; idx < rawShorts.length; idx++) {
                // shorts are stored as signed, no masking!
                floats[idx] = rawShorts[idx] / multiplier + offset;
            }
        } else if ("float".equals(storageType)) {
            throw new IllegalArgumentException(
                    "Expected FloatDataRecord, but got "
                            + rawData.getClass().getName());
        } else {
            throw new IllegalArgumentException("Unknown data type '"
                    + storageType + "'.");
        }
        data = new FloatDataRecord(rawData.getName(), rawData.getGroup(),
                floats, rawData.getDimension(), rawData.getSizes());
        return data;
    }

    /**
     * @param rec
     * @return
     */
    protected ParmStorageInfo findStorageInfo(ParmID parmID) {
        ParmStorageInfo parmStorageInfo = null;

        if (gridConfig == null) {
            // Default to a no-conversion float storage
            GridLocation gridLoc = getGridParmInfo(parmID).getPayload()
                    .getGridLoc();
            parmStorageInfo = new ParmStorageInfo("float", gridLoc.gridSize(),
                    parmID.getParmName(), parmID.getParmLevel(), 0.0f, 1.0f,
                    "float");
        } else {
            // look up the parm storage info
            parmStorageInfo = gridConfig.getParmStorageInfo(
                    parmID.getParmName(), parmID.getParmLevel());
        }
        return parmStorageInfo;
    }

    private void storeGridParmInfo(List<GridParmInfo> gridParmInfo,
            List<ParmStorageInfo> parmStorageInfoList, StoreOp storeOp)
            throws Exception {
        IDataStore ds = DataStoreFactory.getDataStore(GfeUtil.getHDF5File(
                gfeBaseDataDir, this.dbId));
        String parmNameAndLevel = null;
        for (GridParmInfo gpi : gridParmInfo) {
            parmNameAndLevel = gpi.getParmID().getParmName() + "_"
                    + gpi.getParmID().getParmLevel();
            ByteDataRecord br = new ByteDataRecord(parmNameAndLevel,
                    GRID_PARM_INFO_GRP, new byte[1]);
            br.setDataAttributes(getGpiAsMap(gpi));
            ds.addDataRecord(br);
            parmInfo.put(parmNameAndLevel, gpi);
        }

        for (ParmStorageInfo psi : parmStorageInfoList) {
            parmNameAndLevel = psi.parmName() + "_" + psi.level();
            ByteDataRecord br2 = new ByteDataRecord(parmNameAndLevel,
                    GRID_PARM_STORAGE_INFO_GRP, new byte[1]);
            br2.setDataAttributes(getStorageInfoAsMap(psi));
            ds.addDataRecord(br2);
            parmStorageInfo.put(parmNameAndLevel, psi);
        }
        ds.store(storeOp);
    }

    private void storeGridParmInfo(GridParmInfo gpi, ParmStorageInfo psi,
            StoreOp storeOp) throws Exception {
        List<GridParmInfo> gpis = new ArrayList<GridParmInfo>();
        gpis.add(gpi);
        List<ParmStorageInfo> psis = new ArrayList<ParmStorageInfo>();
        psis.add(psi);
        storeGridParmInfo(gpis, psis, storeOp);
    }
}
