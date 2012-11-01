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

package com.raytheon.uf.edex.database.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.persist.DefaultPathProvider;
import com.raytheon.uf.common.dataplugin.persist.IHDFFilePathProvider;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.dataquery.db.QueryParam.QueryOperand;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.IDataStore.StoreOp;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.datastorage.StorageStatus;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;
import com.raytheon.uf.common.geospatial.ISpatialEnabled;
import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.core.props.PropertiesFactory;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.database.purge.PurgeLogger;
import com.raytheon.uf.edex.database.purge.PurgeRule;
import com.raytheon.uf.edex.database.purge.PurgeRuleSet;
import com.raytheon.uf.edex.database.query.DatabaseQuery;
import com.raytheon.uf.edex.database.spatial.DataReprojector;
import com.raytheon.uf.edex.database.spatial.ReferencedDataRecord;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Abstract implementation of a Plugin data access object
 * <p>
 * Plugins wishing to store data in the HDF5 repository may extend this class
 * directly. If HDF5 persistance is not necessary, plugins may use or extend
 * DefaultPluginDao
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2/6/09       1990       bphillip    Initial creation
 * 6/29/12      #828       dgilling    Force getPurgeRulesForPlugin()
 *                                     to search only COMMON_STATIC.
 * Oct 10, 2012 1261       djohnson    Add some generics wildcarding.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public abstract class PluginDao extends CoreDao {

    /** The default retention time to retain data */
    public static final int DEFAULT_RETENTION_TIME = 24;

    /** The default tile to retrieve. Currently unused */
    public static final int DEFAULT_TILE = -1;

    /** The hdf5 file system suffix */
    public static final String HDF5_SUFFIX = ".h5";

    /** The base path of the hdf5 data store */
    public static final String HDF5_DIR = PropertiesFactory.getInstance()
            .getEnvProperties().getEnvValue("HDF5DIR");

    /** The base path of the folder containing HDF5 data for the owning plugin */
    public final String PLUGIN_HDF5_DIR;

    /** The path provider for determining paths to hdf5 data */
    public IHDFFilePathProvider pathProvider;

    /** The owning plugin name */
    protected String pluginName;

    /** The sql string used to check for duplicates in the database */
    protected String dupCheckSql = "select id from awips.:tableName where dataURI=':dataURI'";

    protected static final String PURGE_VERSION_FIELD = "dataTime.refTime";

    /**
     * Constructs a new PluginDao for the given plugin
     * 
     * @param pluginName
     *            The name of the plugin to create the data access object for
     * @throws PluginException
     *             If problems occur while gathering data about the data type
     *             plugin
     */
    public PluginDao(String pluginName) throws PluginException {
        super(DaoConfig.forDatabase(PluginFactory.getInstance().getDatabase(
                pluginName)));
        Class<PluginDataObject> clazz = PluginFactory.getInstance()
                .getPluginRecordClass(pluginName);
        if (clazz != null) {
            this.setDaoClass(clazz);
        }

        this.pluginName = pluginName;
        PLUGIN_HDF5_DIR = HDF5_DIR + File.separator + pluginName
                + File.separator;
        dupCheckSql = dupCheckSql.replace(":tableName", PluginFactory
                .getInstance().getPrimaryTable(pluginName));
        pathProvider = PluginFactory.getInstance().getPathProvider(pluginName);
    }

    /**
     * Defines the behavior for storing data to the HDF5 data store
     * 
     * @param dataStore
     *            The datastore to save the data to
     * @param obj
     *            The object containing the data to be stored
     * @return The updated data store
     * @throws Exception
     *             if problems occur while interacting with the HDF5 data store
     */
    protected abstract IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception;

    /**
     * Persists a group of records to the data stores
     * 
     * @param records
     *            The records to persist
     * @throws PluginException
     *             If problems occur while interacting with the HDF5 data stores
     */
    public void persistRecords(PluginDataObject... records)
            throws PluginException {
        persistToHDF5(records);
        persistToDatabase(records);
    }

    @SuppressWarnings("unchecked")
    public PluginDataObject[] persistToDatabase(PluginDataObject... records) {
        List<PersistableDataObject<Object>> toPersist = new ArrayList<PersistableDataObject<Object>>();
        for (PluginDataObject record : records) {
            toPersist.add(record);
        }
        List<PersistableDataObject<Object>> duplicates = mergeAll(toPersist);
        for (PersistableDataObject<Object> pdo : duplicates) {
            logger.info("Discarding duplicate: "
                    + ((PluginDataObject) (pdo)).getDataURI());
            toPersist.remove(pdo);
        }
        return toPersist.toArray(new PluginDataObject[toPersist.size()]);
    }

    /**
     * Persists the HDF5 component of the records to the HDF5 repository
     * 
     * @param records
     *            The records to persist
     * @return The status of the storage operation
     * @throws PluginException
     *             If problems occur while interacting with the HDF5 data
     *             storesr
     */
    public StorageStatus persistToHDF5(PluginDataObject... records)
            throws PluginException {
        // Step 1: sort the objects by the file they belong to
        Map<File, List<IPersistable>> persistableMap = new HashMap<File, List<IPersistable>>(
                records.length);

        for (PluginDataObject pdo : records) {
            if (pdo instanceof IPersistable) {
                IPersistable persistable = (IPersistable) pdo;

                // get the directory
                String directory = HDF5_DIR
                        + File.separator
                        + pdo.getPluginName()
                        + File.separator
                        + pathProvider.getHDFPath(pdo.getPluginName(),
                                persistable);
                File dataStoreFile = new File(directory
                        + File.separator
                        + pathProvider.getHDFFileName(pdo.getPluginName(),
                                persistable));

                List<IPersistable> pdoList = persistableMap.get(dataStoreFile);
                if (pdoList == null) {
                    pdoList = new ArrayList<IPersistable>();
                    persistableMap.put(dataStoreFile, pdoList);
                }

                pdoList.add(persistable);
            }
        }

        // Step 2: Iterate through all the files, and persist all records that
        // belong to each file in bulk

        Iterator<File> fileIterator = persistableMap.keySet().iterator();
        List<StorageException> exceptions = new ArrayList<StorageException>();
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            List<IPersistable> persistables = persistableMap.get(file);

            // create the directories necessary for this file, if they do not
            // exist
            // File directory = file.getParentFile();
            // if (!directory.exists()) {
            // directory.mkdirs();
            // }

            IDataStore dataStore = DataStoreFactory.getDataStore(file);
            IDataStore replaceDataStore = null;

            for (IPersistable persistable : persistables) {
                try {
                    if (((PersistableDataObject) persistable)
                            .isOverwriteAllowed()) {
                        if (replaceDataStore == null) {
                            replaceDataStore = DataStoreFactory
                                    .getDataStore(file);
                        }

                        populateDataStore(replaceDataStore, persistable);
                    } else {
                        populateDataStore(dataStore, persistable);
                    }
                } catch (Exception e) {
                    throw new PluginException("Error populating data store", e);
                }
            }

            try {
                StorageStatus s = dataStore.store();
                // add exceptions to a list for aggregation
                exceptions.addAll(Arrays.asList(s.getExceptions()));
            } catch (StorageException e) {
                logger.error("Error persisting to HDF5", e);
            }

            if (replaceDataStore != null) {
                try {
                    StorageStatus s = replaceDataStore.store(StoreOp.REPLACE);
                    // add exceptions to a list for aggregation
                    exceptions.addAll(Arrays.asList(s.getExceptions()));
                } catch (StorageException e) {
                    logger.error("Error persisting replace records to HDF5", e);
                }
            }
        }

        // Create an aggregated status object
        StorageStatus status = new StorageStatus();
        status.setExceptions(exceptions.toArray(new StorageException[exceptions
                .size()]));
        return status;
    }

    /**
     * Retrieves metadata from the database according to the provided query
     * 
     * @param query
     *            The query
     * @return The query results
     * @throws DataAccessLayerException
     *             If problems occur while interacting with the database
     */
    @SuppressWarnings("unchecked")
    public PluginDataObject[] getMetadata(DatabaseQuery query)
            throws PluginException {
        try {
            return ((List<PluginDataObject>) super.queryByCriteria(query))
                    .toArray(new PluginDataObject[] {});
        } catch (DataAccessLayerException e) {
            throw new PluginException("Error getting metadata", e);
        }
    }

    /**
     * Retrieves the complete set of metadata from the database for the record
     * with the provided dataURI
     * 
     * @param dataURI
     *            The dataURI of the record for which to retrieve metadata
     * @return The record populated with a complete set of metadata
     * @throws DataAccessLayerException
     *             If problems occur while interacting with the database
     */
    @SuppressWarnings("unchecked")
    public PluginDataObject getMetadata(String dataURI) throws PluginException {
        List<PluginDataObject> result;
        try {
            result = ((List<PluginDataObject>) queryBySingleCriteria("dataURI",
                    dataURI));
        } catch (DataAccessLayerException e) {
            throw new PluginException("Error getting metadata", e);
        }
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * Retrieves the HDF5 component of the records provided
     * 
     * @param objects
     *            The objects to retrieve the HDF5 component for
     * @param tileSet
     *            The tile set to retrieve. Any value less than or equal to zero
     *            returns the "base" data only.
     * @return The HDF5 data records
     * @throws StorageException
     *             If problems occur while interacting with HDF5 data stores
     */
    public List<IDataRecord[]> getHDF5Data(List<PluginDataObject> objects,
            int tileSet) throws PluginException {

        List<IDataRecord[]> retVal = new ArrayList<IDataRecord[]>();

        for (PluginDataObject obj : objects) {

            if (obj instanceof IPersistable) {
                /* connect to the data store and retrieve the data */
                IDataStore dataStore = getDataStore((IPersistable) obj);
                boolean interpolated = DataStoreFactory.isInterpolated(tileSet);
                if (!interpolated) {
                    tileSet = 0;
                }
                IDataRecord[] record = new IDataRecord[tileSet + 1];
                try {
                    String group = DataStoreFactory.createGroupName(
                            obj.getDataURI(),
                            DataStoreFactory.DEF_DATASET_NAME, interpolated);
                    // Retrieve the base record.
                    record[0] = dataStore.retrieve(obj.getDataURI(),
                            DataStoreFactory.DEF_DATASET_NAME, Request.ALL);
                    // Now get the interpolated data, if any!
                    for (int tile = 1; tile < record.length; tile++) {
                        record[tile] = dataStore.retrieve(group,
                                String.valueOf(tile), Request.ALL);
                    }
                } catch (Exception e) {
                    throw new PluginException("Error getting HDF5 data", e);
                }
                retVal.add(record);
            }
        }

        return retVal;
    }

    /**
     * Retrieves the HDF5 component of the record provided
     * 
     * @param object
     *            The objects to retrieve the HDF5 component for
     * @param tileSet
     *            The tile set to retrieve. Currently unimplemented
     * @return The HDF5 data records
     * @throws StorageException
     *             If problems occur while interacting with HDF5 data stores
     */
    public IDataRecord[] getHDF5Data(PluginDataObject object, int tile)
            throws PluginException {
        return getHDF5Data(Arrays.asList(new PluginDataObject[] { object }),
                tile).get(0);
    }

    /**
     * Retrieves the fully populated object from the data stores according to
     * the provided query
     * 
     * @param query
     *            The query to execute
     * @param tileSet
     *            The tile set to retrieve. Currently unimplemented
     * @return The results of the query
     * @throws DataAccessLayerException
     *             If problems occur while interacting with the metadata
     *             database
     * @throws StorageException
     *             If probolems occur while interacting with the metadata
     *             database
     */
    public PluginDataObject[] getFullRecord(DatabaseQuery query, int tile)
            throws PluginException {
        PluginDataObject[] queryResults = getMetadata(query);
        for (PluginDataObject obj : queryResults) {
            obj.setPluginName(pluginName);
            obj.setMessageData(getHDF5Data(obj, tile));
        }
        return queryResults;
    }

    public void initializePlugin() throws PluginException {

    }

    /**
     * Purges all data associated with the owning plugin based on criteria
     * specified by the owning plugin
     * 
     * @throws PluginException
     *             If problems occur while interacting with the data stores
     */
    public void purgeAllData() throws PluginException {
        try {
            List<Date> allRefTimes = getRefTimes();
            Set<Date> roundedRefTimes = new HashSet<Date>();
            for (Date d : allRefTimes) {
                this.purgeDataByRefTime(d, null);
                roundedRefTimes.add(roundDateToHour(d));
            }
            for (Date d : roundedRefTimes) {
                this.purgeHDF5DataByRefTime(d, null);
            }
        } catch (Exception e) {
            throw new PluginException("Error purging all data for "
                    + pluginName + " plugin.", e);
        }
    }

    /**
     * Purges data according to purge criteria specified by the owning plugin
     * 
     * @throws PluginException
     *             If problems occur while interacting with data stores
     */
    public void purgeExpiredData() throws PluginException {
        try {
            PluginDao pluginDao = PluginFactory.getInstance().getPluginDao(
                    pluginName);
            PurgeRuleSet ruleSet = getPurgeRulesForPlugin(pluginName);

            if (ruleSet == null) {
                PurgeLogger.logInfo(
                        "No valid purge rules found. Skipping purge.",
                        pluginName);
                return;
            }

            // Query the database to get all possible product keys for this data
            List<String> ruleKeys = ruleSet.getKeys();
            int totalItems = 0;

            if ((ruleKeys != null) && !ruleKeys.isEmpty()) {
                // Iterate through keys, fully purge each key set
                String[][] distinctKeys = getDistinctProductKeys(ruleSet
                        .getKeys());
                for (String[] key : distinctKeys) {
                    totalItems += purgeExpiredKey(pluginDao, ruleSet, key);
                }
            } else {
                // no rule keys defined, can only apply default rule
                totalItems += purgeExpiredKey(pluginDao, ruleSet, null);
            }

            StringBuilder messageBuffer = new StringBuilder();
            messageBuffer.append("Purged ").append(totalItems).append(" item");
            if (totalItems != 1) {
                messageBuffer.append("s");
            }
            messageBuffer.append(" total.");

            PurgeLogger.logInfo(messageBuffer.toString(), pluginName);
        } catch (EdexException e) {
            throw new PluginException("Error applying purge rule!!", e);
        }
    }

    /**
     * Takes the purgeKeys, looks up the associated purge rule, and applies it
     * to the data matched by purgeKeys.
     * 
     * @param pluginDao
     * @param ruleSet
     * @param purgeKeys
     * @return Number of records purged
     * @throws DataAccessLayerException
     */
    protected int purgeExpiredKey(PluginDao pluginDao, PurgeRuleSet ruleSet,
            String[] purgeKeys) throws DataAccessLayerException {
        PurgeRule rule = ruleSet.getRuleForKeys(purgeKeys);

        if (rule == null) {
            PurgeLogger.logWarn(
                    "No rule found for purgeKeys: "
                            + Arrays.toString(purgeKeys), pluginName);
            return 0;
        }

        // Holds the times kept by this rule
        List<Date> timesKeptByRule = new ArrayList<Date>();

        Set<Date> roundedTimes = new HashSet<Date>();

        // Holds the times to be purged by this rule
        List<Date> timesPurgedByRule = new ArrayList<Date>();

        /*
         * This section applies the purge rule
         */
        String[][] productKeys = null;
        if (purgeKeys != null) {
            productKeys = new String[purgeKeys.length][];
            Iterator<String> iter = ruleSet.getKeys().iterator();
            int index = 0;
            for (String purgeKey : purgeKeys) {
                productKeys[index] = new String[2];
                productKeys[index][0] = iter.next();
                productKeys[index++][1] = purgeKey;
            }
        }

        List<Date> refTimesForKey = pluginDao
                .getRefTimesForCriteria(productKeys);
        String productKeyString = null;
        if (productKeys != null) {
            StringBuilder productKeyBuilder = new StringBuilder();
            for (String[] pKey : productKeys) {
                productKeyBuilder.append(Arrays.toString(pKey));
            }
            productKeyString = productKeyBuilder.toString();
        }

        if (rule.isModTimeToWaitSpecified()) {
            Date maxInsertTime = pluginDao.getMaxInsertTime(productKeys);
            if (maxInsertTime != null) {
                long lastInsertTime = maxInsertTime.getTime();
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastInsertTime) < rule
                        .getModTimeToWaitInMillis()) {
                    PurgeLogger
                            .logInfo(
                                    "For procuct key, "
                                            + productKeyString
                                            + ", the most recent version is less than "
                                            + rule.getModTimeToWaitDescription()
                                            + " old. Increasing versions to keep for this key.",
                                    pluginName);
                    rule.setVersionsToKeep(rule.getVersionsToKeep() + 1);
                }
            }
        }

        // Calculate the period cutoff time if necessary
        Date periodCutoffTime = new Date();
        if (rule.isPeriodSpecified()) {
            if (rule.isPeriodBasedOnLatestTime()) {
                Date maxRefTime = pluginDao.getMaxRefTime(productKeys);
                if (maxRefTime == null) {
                    PurgeLogger.logInfo("No data available to purge",
                            pluginName);
                    return 0;
                } else {
                    periodCutoffTime = new Date(maxRefTime.getTime()
                            - rule.getPeriodInMillis());
                }
            } else {
                periodCutoffTime = new Date(System.currentTimeMillis()
                        - rule.getPeriodInMillis());
            }
        }

        // Filter the keepers by the delta time specified
        if (rule.isDeltaSpecified()) {
            for (Date refTime : refTimesForKey) {
                Date timeToCompare = rule.getRoundedDate(refTime)[1];
                long delta = rule.getDeltaTimeInMillis();
                long dateTimeAsLong = timeToCompare.getTime();

                if (rule.isDeltaTimeMultiple()) {
                    if (dateTimeAsLong % delta == 0) {
                        // If the versions to keep is zero we keep it if
                        // it does not exceed the period specified, if
                        // any
                        if (rule.getVersionsToKeep() == 0) {
                            if (rule.isPeriodSpecified()
                                    && refTime.before(periodCutoffTime)) {
                                timesPurgedByRule.add(refTime);

                            } else {
                                timesKeptByRule.add(refTime);
                            }
                        }

                        // If the versions to keep is not zero and
                        // adding this will not exceed the specified
                        // number of versions to keep and it does not
                        // exceed the period specified, the time is kept
                        else if (rule.getVersionsToKeep() > 0) {
                            if (rule.isRoundSpecified()) {
                                if (roundedTimes.size() < rule
                                        .getVersionsToKeep()) {
                                    roundedTimes.add(timeToCompare);
                                    timesKeptByRule.add(refTime);
                                } else {
                                    timesPurgedByRule.add(refTime);
                                }
                            } else {
                                if (timesKeptByRule.size() < rule
                                        .getVersionsToKeep()) {
                                    if (rule.isPeriodSpecified()
                                            && refTime.before(periodCutoffTime)) {
                                        timesPurgedByRule.add(refTime);
                                    } else {
                                        timesKeptByRule.add(refTime);
                                    }
                                }
                            }

                        }
                    } else {
                        timesPurgedByRule.add(refTime);
                    }
                }
            }
        }

        /*
         * If a versions to keep is specified, determine the versions to keep.
         * If a delta is specified for this rule, then the versions have already
         * been calculated based on the delta time. This section is used only if
         * a delta time is not used
         */
        else if (!rule.isDeltaSpecified() && rule.isVersionsToKeepSpecified()) {
            Date currentRefTime = null;
            for (int i = 0; i < refTimesForKey.size(); i++) {
                currentRefTime = refTimesForKey.get(i);
                if (i < rule.getVersionsToKeep()) {
                    if (rule.isPeriodSpecified()
                            && currentRefTime.before(periodCutoffTime)) {
                        timesPurgedByRule.add(currentRefTime);
                    } else {
                        timesKeptByRule.add(currentRefTime);
                    }
                    timesKeptByRule.add(currentRefTime);
                } else {
                    timesPurgedByRule.add(currentRefTime);
                }

            }
            /*
             * This rule only specifies a time cutoff
             */
        } else if (!rule.isDeltaSpecified()
                && !rule.isVersionsToKeepSpecified()
                && rule.isPeriodSpecified()) {
            for (Date currentRefTime : refTimesForKey) {
                if (currentRefTime.before(periodCutoffTime)) {
                    timesPurgedByRule.add(currentRefTime);
                } else {
                    timesKeptByRule.add(currentRefTime);
                }
            }
            /*
             * This rule has been so poorly written that it does nothing
             */
        } else {
            PurgeLogger
                    .logInfo(
                            "Purge rule does not specify a delta, period, or versions to keep.",
                            pluginName);
        }

        /*
         * If log only is specified, log the results but purge nothing
         */
        if (rule.isLogOnly()) {
            PurgeLogger.logInfo("Rule is configured to log only", pluginName);
            PurgeLogger.logInfo("These version would be removed by the rule:",
                    pluginName);
            Collections.sort(timesPurgedByRule);
            Collections.sort(timesKeptByRule);
            for (Date d : timesPurgedByRule) {
                PurgeLogger.logInfo(d.toString(), pluginName);
            }
            PurgeLogger.logInfo(
                    "These versions would have been retained by the rule:",
                    pluginName);
            for (Date d : timesKeptByRule) {
                PurgeLogger.logInfo(d.toString(), pluginName);
            }

            return 0;
        }

        // We must remove the keep times from the purge list. This
        // ensures that if the time passes at least one time constraint,
        // then it will be retained
        timesPurgedByRule.removeAll(timesKeptByRule);

        int itemsDeletedForKey = 0;
        for (Date deleteDate : timesPurgedByRule) {

            // Delete the data in the database
            int itemsDeletedForTime = pluginDao.purgeDataByRefTime(deleteDate,
                    productKeys);

            itemsDeletedForKey += itemsDeletedForTime;
        }
        if (itemsDeletedForKey > 0) {
            StringBuilder messageBuffer = new StringBuilder();
            messageBuffer.append("Purged ").append(itemsDeletedForKey)
                    .append(" item");
            if (itemsDeletedForKey != 1) {
                messageBuffer.append("s");
            }
            messageBuffer.append(" for key ").append(productKeyString);
            PurgeLogger.logInfo(messageBuffer.toString(), pluginName);
        }

        // Debug output to see which times were retained
        if (PurgeLogger.isDebugEnabled()) {
            if (!timesKeptByRule.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                Collections.sort(timesKeptByRule);
                builder.append("The following times were retained");
                builder.append(" for key ").append(productKeyString)
                        .append(":");

                for (Date keepDate : timesKeptByRule) {
                    builder.append("[").append(keepDate).append("]")
                            .append(" ");
                }
                PurgeLogger.logDebug(builder.toString(), pluginName);
            }
        }

        return itemsDeletedForKey;
    }

    /**
     * Gets the data store for the given object
     * 
     * @param obj
     *            The object for which to get the data store
     * @return The data store
     */
    public IDataStore getDataStore(IPersistable obj) {
        String persistDir = PLUGIN_HDF5_DIR
                + pathProvider.getHDFPath(this.pluginName, obj)
                + File.separator;
        String archive = pathProvider.getHDFFileName(this.pluginName, obj);

        File persistFile = new File(persistDir, archive);
        /* connect to the data store and retrieve the data */
        return DataStoreFactory.getDataStore(persistFile);
    }

    /**
     * Takes a list of IPersistable objects and return a map of IDataStore
     * objects and a list of IPersistable objects that are stored in that data
     * store.
     * 
     * @param objs
     *            A list of IPersistable objects to get their respsective data
     *            stores.
     * @return
     */
    public Map<IDataStore, List<IPersistable>> getDataStoreMap(
            List<IPersistable> objs) {
        StringBuilder tmp = new StringBuilder(120);

        Map<String, List<IPersistable>> fileMap = new HashMap<String, List<IPersistable>>();

        // group objects by file
        for (IPersistable obj : objs) {
            tmp.setLength(0);
            tmp.append(pathProvider.getHDFPath(this.pluginName, obj));
            tmp.append(File.separatorChar);
            tmp.append(pathProvider.getHDFFileName(this.pluginName, obj));
            String path = tmp.toString();
            List<IPersistable> objsInFile = fileMap.get(path);
            if (objsInFile == null) {
                objsInFile = new ArrayList<IPersistable>();
                fileMap.put(path, objsInFile);
            }
            objsInFile.add(obj);
        }

        Map<IDataStore, List<IPersistable>> dataStoreMap = new HashMap<IDataStore, List<IPersistable>>(
                (int) (fileMap.size() * 1.25) + 1);
        for (Map.Entry<String, List<IPersistable>> entry : fileMap.entrySet()) {
            dataStoreMap.put(
                    DataStoreFactory.getDataStore(new File(PLUGIN_HDF5_DIR
                            + entry.getKey())), entry.getValue());
        }

        return dataStoreMap;
    }

    /**
     * Gets a list of the distinct product keys for this plugin
     * 
     * @return The list of distinct product keys for this plugin
     * @throws DataAccessLayerException
     *             If errors occur while querying for the data
     */
    @SuppressWarnings("unchecked")
    public String[][] getDistinctProductKeys(List<String> keys)
            throws DataAccessLayerException {
        String[][] distinctKeys = null;
        if ((keys != null) && !keys.isEmpty()) {
            DatabaseQuery query = new DatabaseQuery(this.daoClass);
            for (int i = 0; i < keys.size(); i++) {
                if (i == 0) {
                    query.addDistinctParameter(keys.get(i));
                } else {
                    query.addReturnedField(keys.get(i));
                }
            }
            if (keys.size() == 1) {
                List<?> results = this.queryByCriteria(query);
                distinctKeys = new String[results.size()][];
                int index = 0;
                for (Object obj : results) {
                    distinctKeys[index] = new String[1];
                    distinctKeys[index++][0] = String.valueOf(obj);
                }
            } else {
                List<Object[]> results = (List<Object[]>) this
                        .queryByCriteria(query);
                distinctKeys = new String[results.size()][];
                int rIndex = 0;

                for (Object[] result : results) {
                    distinctKeys[rIndex] = new String[result.length];
                    int cIndex = 0;

                    for (Object obj : result) {
                        distinctKeys[rIndex][cIndex++] = String.valueOf(obj);
                    }

                    rIndex++;
                }
            }
        }
        return distinctKeys;
    }

    /**
     * Gets all distinct reference times for this plugin
     * 
     * @param productKey
     *            The product key to get the list of reference times for
     * @return A list of distinct reference times for the given productKey
     * @throws DataAccessLayerException
     */
    @SuppressWarnings("unchecked")
    public List<Date> getRefTimes() throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);
        query.addDistinctParameter(PURGE_VERSION_FIELD);
        query.addOrder(PURGE_VERSION_FIELD, false);
        List<Date> times = (List<Date>) this.queryByCriteria(query);
        return times;
    }

    /**
     * Gets all distinct reference times for the given productKey
     * 
     * @param productKeys
     *            The product keys to get the list of reference times for.
     *            Should be in key value pairs.
     * @return A list of distinct reference times for the given productKey
     * @throws DataAccessLayerException
     */
    @SuppressWarnings("unchecked")
    public List<Date> getRefTimesForCriteria(String[][] productKeys)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);

        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                query.addQueryParam(key[0], key[1]);
            }
        }

        query.addDistinctParameter(PURGE_VERSION_FIELD);
        query.addOrder(PURGE_VERSION_FIELD, false);
        List<Date> times = (List<Date>) this.queryByCriteria(query);
        return times;
    }

    /**
     * Purges data from the database for this plugin with the given reference
     * time matching the given productKeys. If refTime is null, will purge all
     * data associated with the productKeys.
     * 
     * @param refTime
     *            The reftime to delete data for. A null will purge all data for
     *            the productKeys.
     * @param productKeys
     *            The keys to use as a constraint for deletions
     * @return
     * @throws DataAccessLayerException
     */
    @SuppressWarnings("unchecked")
    public int purgeDataByRefTime(Date refTime, String[][] productKeys)
            throws DataAccessLayerException {

        int results = 0;
        DatabaseQuery dataQuery = new DatabaseQuery(this.daoClass);

        if (refTime != null) {
            dataQuery.addQueryParam(PURGE_VERSION_FIELD, refTime);
        }

        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                dataQuery.addQueryParam(key[0], key[1]);
            }
        }

        List<Integer> idList = null;
        DatabaseQuery idQuery = null;

        dataQuery.addReturnedField("id");
        dataQuery.setMaxResults(500);

        boolean purgeHDF5Data = false;
        boolean optimizedPurge = false;

        try {
            // Determine if this plugin uses HDF5 to store data
            purgeHDF5Data = (PluginFactory.getInstance()
                    .getPluginRecordClass(pluginName).newInstance() instanceof IPersistable);
        } catch (Exception e) {
            PurgeLogger.logError(
                    "Unabled to determine if plugin has HDF5 data to purge",
                    this.pluginName, e);
        }

        // determine if hdf5 purge can be optimized
        if (purgeHDF5Data) {
            // check how the path keys line up to purge keys
            List<String> pathKeys = pathProvider.getKeyNames(this.pluginName);
            boolean pathKeysEmpty = (pathKeys == null) || pathKeys.isEmpty();
            boolean productKeysEmpty = (productKeys == null)
                    || (productKeys.length == 0);

            // determine if hdf5 purge can be optimized
            if (!pathKeysEmpty) {
                if (productKeysEmpty) {
                    // Purging on higher magnitude that path, only need to track
                    // file
                    optimizedPurge = true;
                } else if (pathKeys.size() < productKeys.length) {
                    // there are more purge keys than path keys, cannot optimize
                    // hdf5 purge
                    optimizedPurge = false;
                } else {
                    // need to compare each key to check for optimized purge,
                    // all
                    // productKeys must be a pathKey for optimized purge, both
                    // key lists should be small 3 or less, no need to optimize
                    // list
                    // look ups
                    optimizedPurge = true;
                    for (String[] productKey : productKeys) {
                        boolean keyMatch = false;
                        for (String pathKey : pathKeys) {
                            if (pathKey.equals(productKey[0])) {
                                keyMatch = true;
                                break;
                            }
                        }

                        if (!keyMatch) {
                            optimizedPurge = false;
                            break;
                        }
                    }
                }
            } else {
                // if purge is same level as path, optimize
                optimizedPurge = productKeysEmpty;
            }
        }

        // fields for hdf5 purge
        String previousFile = null;
        StringBuilder pathBuilder = new StringBuilder();
        Map<String, List<String>> fileToUriMap = new HashMap<String, List<String>>();

        do {
            idList = (List<Integer>) this.queryByCriteria(dataQuery);
            if (!idList.isEmpty()) {
                idQuery = new DatabaseQuery(this.daoClass);
                idQuery.addQueryParam("id", idList, QueryOperand.IN);
                List<PluginDataObject> pdos = (List<PluginDataObject>) this
                        .queryByCriteria(idQuery);
                this.delete(pdos);

                if (purgeHDF5Data) {
                    for (PluginDataObject pdo : pdos) {
                        pathBuilder.setLength(0);
                        IPersistable persist = (IPersistable) pdo;
                        pathBuilder
                                .append(pathProvider.getHDFPath(
                                        this.pluginName, persist))
                                .append(File.separatorChar)
                                .append(pathProvider.getHDFFileName(
                                        this.pluginName, persist));
                        String file = pathBuilder.toString();

                        if (optimizedPurge) {
                            // only need to track file, tracking last file
                            // instead of constantly indexing hashMap
                            if (!file.equals(previousFile)) {
                                fileToUriMap.put(file, null);
                                previousFile = file;
                            }
                        } else {
                            List<String> uriList = fileToUriMap.get(file);
                            if (uriList == null) {
                                // sizing to 50 as most data types have numerous
                                // entries in a file
                                uriList = new ArrayList<String>(50);
                                fileToUriMap.put(file, uriList);
                            }
                            uriList.add(file);
                        }
                    }
                }

                results += pdos.size();
            }

        } while ((idList != null) && !idList.isEmpty());

        if (purgeHDF5Data) {
            for (Map.Entry<String, List<String>> hdf5Entry : fileToUriMap
                    .entrySet()) {
                try {
                    IDataStore ds = DataStoreFactory.getDataStore(new File(
                            hdf5Entry.getKey()));
                    List<String> uris = hdf5Entry.getValue();
                    if (uris == null) {
                        ds.deleteFiles(null);
                    } else {
                        ds.delete(uris.toArray(new String[uris.size()]));
                    }
                } catch (Exception e) {
                    PurgeLogger.logError("Error occurred purging file: "
                            + hdf5Entry.getKey(), this.pluginName, e);
                }
            }
        }

        return results;
    }

    /**
     * Purges the HDF5 data according to the provided time and key.
     * 
     * @param refTime
     *            The time to delete
     * @param productKey
     *            The key to delete
     */
    protected void purgeHDF5DataByRefTime(Date refTime, String productKey)
            throws DataAccessLayerException {
        IDataStore dataStore = DataStoreFactory.getDataStore(new File(this
                .getHDF5Path(productKey)));
        try {
            dataStore.deleteFiles(new String[] { File.separator
                    + this.pluginName + File.separator
                    + TimeUtil.formatDate(refTime) });
        } catch (Exception e) {
            throw new DataAccessLayerException("Error purging hdf5 data", e);
        }
    }

    /**
     * Gets the maximum reference time contained in the database for the given
     * key. The key corresponds to the productKey field in the data object.
     * 
     * @param productKeys
     *            The product keys to get the maximum reference time for. Should
     *            be in key value pairs.
     * @return Null if this key was not found, else the maximum reference time
     * @throws DataAccessLayerException
     *             If errors occur while querying the database
     */
    @SuppressWarnings("unchecked")
    public Date getMaxRefTime(String[][] productKeys)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);
        query.addDistinctParameter(PURGE_VERSION_FIELD);

        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                query.addQueryParam(key[0], key[1]);
            }
        }

        query.addOrder(PURGE_VERSION_FIELD, false);
        query.setMaxResults(1);
        List<Date> result = (List<Date>) this.queryByCriteria(query);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Gets the maximum insert time contained in the database for the given key.
     * The key corresponds to the productKey field in the data object.
     * 
     * @param productKey
     *            The key for which to get the maximum insert time
     * @return Null if this key was not found, else the maximum insert time
     * @throws DataAccessLayerException
     *             If errors occur while querying the database
     */
    @SuppressWarnings("unchecked")
    public Date getMaxInsertTime(String[][] productKeys)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);
        // doing distinct is wasted with a ordered max
        // query.addDistinctParameter("insertTime");
        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                query.addQueryParam(key[0], key[1]);
            }
        }

        query.addReturnedField("insertTime");
        query.addOrder("insertTime", false);
        query.setMaxResults(1);
        List<Calendar> result = (List<Calendar>) this.queryByCriteria(query);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0).getTime();
        }
    }

    /**
     * Gets the minimum insert time contained in the database for the given
     * keys. The keys corresponds to the productKey fields in the data object.
     * 
     * @param productKeys
     *            The product keys to get the minimum insert time for. Should be
     *            in key value pairs.
     * @return Null if this key was not found, else the minimum insert time
     * @throws DataAccessLayerException
     *             If errors occur while querying the database
     */
    @SuppressWarnings("unchecked")
    public Date getMinInsertTime(String[][] productKeys)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);
        // doing distinct is wasted with a ordered max
        // query.addDistinctParameter("insertTime");

        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                query.addQueryParam(key[0], key[1]);
            }
        }

        query.addReturnedField("insertTime");
        query.addOrder("insertTime", true);
        query.setMaxResults(1);
        List<Calendar> result = (List<Calendar>) this.queryByCriteria(query);
        if ((result == null) || result.isEmpty()) {
            return null;
        } else {
            return result.get(0).getTime();
        }
    }

    /**
     * Gets the minimum reference time contained in the database for the given
     * key. The key corresponds to the productKey field in the data object.
     * 
     * @param productKeys
     *            The product keys to get the minimum reference times for.
     *            Should be in key value pairs.
     * @return Null if this key was not found, else the minimum reference time
     * @throws DataAccessLayerException
     *             If errors occur while querying the database
     */
    @SuppressWarnings("unchecked")
    public Date getMinRefTime(String[][] productKeys)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(this.daoClass);
        query.addDistinctParameter(PURGE_VERSION_FIELD);

        if ((productKeys != null) && (productKeys.length > 0)) {
            for (String[] key : productKeys) {
                query.addQueryParam(key[0], key[1]);
            }
        }

        query.addOrder(PURGE_VERSION_FIELD, true);
        query.setMaxResults(1);
        List<Date> result = (List<Date>) this.queryByCriteria(query);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Breaks the product key into key value pairs.
     * 
     * @param productKey
     *            The product key to break apart into pairs
     * @return The product key/value pairs
     */
    protected List<String[]> getProductKeyParameters(String productKey) {
        List<String[]> params = new ArrayList<String[]>();
        if (productKey.isEmpty()) {
            return params;
        }

        String[] keyTokens = productKey.trim().split(";");
        for (String keyToken : keyTokens) {
            String[] constraintTokens = keyToken.split("=");
            constraintTokens[0] = constraintTokens[0].trim();
            constraintTokens[1] = constraintTokens[1].trim();
            params.add(constraintTokens);
        }
        return params;
    }

    /**
     * Gets the path to the HDF5 file based on the provided product key
     * 
     * @param productKey
     *            The product key for which to generate the path to the hdf5
     *            file
     * @return The path to the hdf5 file based on the provided product key
     */
    protected String getHDF5Path(String productKey) {
        StringBuilder pathBuilder = new StringBuilder();
        List<String[]> params = getProductKeyParameters(productKey);
        pathBuilder.append(this.PLUGIN_HDF5_DIR);
        for (String[] param : params) {
            pathBuilder.append(File.separator + param[1]);
        }
        return pathBuilder.toString();
    }

    private Date roundDateToHour(Date dateToRound) {
        return new Date(dateToRound.getTime() - dateToRound.getTime() % 3600000);
    }

    /**
     * Deletes an object from the database
     * 
     * @param obj
     *            The object to delete
     */
    public void delete(final List<PluginDataObject> objs) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                getHibernateTemplate().deleteAll(objs);
            }
        });
    }

    public static PurgeRuleSet getPurgeRulesForPlugin(String pluginName) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        Map<LocalizationLevel, LocalizationFile> tieredFile = pathMgr
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        "purge/" + pluginName + "PurgeRules.xml");
        LocalizationContext[] levelHierarchy = pathMgr
                .getLocalSearchHierarchy(LocalizationType.COMMON_STATIC);
        File rulesFile = null;
        for (LocalizationContext ctx : levelHierarchy) {
            LocalizationFile lFile = tieredFile.get(ctx.getLocalizationLevel());
            if (lFile != null) {
                rulesFile = lFile.getFile();
                break;
            }
        }

        if (rulesFile != null) {
            // allow zero length file to disable purge for this plugin
            if (rulesFile.length() > 0) {
                try {
                    PurgeRuleSet purgeRules = (PurgeRuleSet) SerializationUtil
                            .jaxbUnmarshalFromXmlFile(rulesFile);

                    // ensure there's a default rule
                    if (purgeRules.getDefaultRule() == null) {
                        purgeRules.setDefaultRule(loadDefaultPurgeRule());
                    }
                    return purgeRules;
                } catch (SerializationException e) {
                    PurgeLogger
                            .logError(
                                    "Error deserializing purge rules! Data will not be purged. Please define rules.",
                                    pluginName, e);
                }
            }
        } else if (!"default".equals(pluginName)) {
            // no purge rule for this plugin, check base purge rule
            return getPurgeRulesForPlugin("default");
        }
        return null;
    }

    public static PurgeRule loadDefaultPurgeRule() {
        File defaultRule = PathManagerFactory.getPathManager().getStaticFile(
                "purge/defaultPurgeRules.xml");
        if (defaultRule == null) {
            PurgeLogger.logError("Default purge rule not defined!!", "EDEX");
            statusHandler
                    .error("Default purge rule not defined!!  Data will not be purged for plugins which do not specify purge rules!");
            return null;
        }
        try {
            PurgeRuleSet purgeRules = (PurgeRuleSet) SerializationUtil
                    .jaxbUnmarshalFromXmlFile(defaultRule);
            System.out.println("Default file has default rule: "
                    + (purgeRules.getDefaultRule() != null));
            return purgeRules.getDefaultRule();
        } catch (SerializationException e) {
            PurgeLogger.logError("Error deserializing default purge rule!",
                    "DEFAULT");
        }
        return null;
    }

    public void archiveData(String archivePath, Calendar insertStartTime,
            Calendar insertEndTime) throws DataAccessLayerException,
            SerializationException, IOException {
        List<PersistableDataObject> pdos = getRecordsToArchive(insertStartTime,
                insertEndTime);
        if ((pdos != null) && (pdos.size() > 0)) {
            // map of file to list of pdo
            Map<String, List<PersistableDataObject>> pdoMap = new HashMap<String, List<PersistableDataObject>>();
            if (pdos.get(0) instanceof IPersistable) {
                IHDFFilePathProvider pathProvider = this.pathProvider;

                for (PersistableDataObject pdo : pdos) {
                    IPersistable persistable = (IPersistable) pdo;
                    String path = pathProvider.getHDFPath(pluginName,
                            persistable)
                            + File.separator
                            + pathProvider.getHDFFileName(pluginName,
                                    persistable);
                    List<PersistableDataObject> list = pdoMap.get(path);
                    if (list == null) {
                        list = new ArrayList<PersistableDataObject>(pdos.size());
                        pdoMap.put(path, list);
                    }
                    list.add(pdo);
                }
            } else {
                // order files by refTime hours
                for (PersistableDataObject pdo : pdos) {
                    String timeString = null;
                    if (pdo instanceof PluginDataObject) {
                        PluginDataObject pluginDataObj = (PluginDataObject) pdo;
                        Date time = pluginDataObj.getDataTime()
                                .getRefTimeAsCalendar().getTime();
                        timeString = DefaultPathProvider.fileNameFormat.get()
                                .format(time);
                    } else {
                        // no refTime to use bounded insert query bounds
                        Date time = insertStartTime.getTime();
                        timeString = DefaultPathProvider.fileNameFormat.get()
                                .format(time);
                    }

                    String path = pluginName + timeString;
                    List<PersistableDataObject> list = pdoMap.get(path);
                    if (list == null) {
                        list = new ArrayList<PersistableDataObject>(pdos.size());
                        pdoMap.put(path, list);
                    }
                    list.add(pdo);
                }

            }

            for (Map.Entry<String, List<PersistableDataObject>> entry : pdoMap
                    .entrySet()) {
                String path = archivePath + File.separator + pluginName
                        + File.separator + entry.getKey();

                // remove .h5
                int index = path.lastIndexOf('.');
                if ((index > 0) && (path.length() - index < 5)) {
                    // ensure its end of string in case extension is
                    // dropped/changed
                    path = path.substring(0, index);
                }

                path += ".bin.gz";

                File file = new File(path);

                if (file.exists()) {
                    // pull the
                }

                // Thrift serialize pdo list
                byte[] data = SerializationUtil.transformToThrift(entry
                        .getValue());

                // debug transform back for object inspection
                Object obj = SerializationUtil.transformFromThrift(data);

                // save list to disk (in gz format?)
                FileUtil.bytes2File(data, file, true);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public List<PersistableDataObject> getRecordsToArchive(
            Calendar insertStartTime, Calendar insertEndTime)
            throws DataAccessLayerException {
        DatabaseQuery dbQuery = new DatabaseQuery(this.getDaoClass());
        dbQuery.addQueryParam("insertTime", insertStartTime,
                QueryOperand.GREATERTHANEQUALS);
        dbQuery.addQueryParam("insertTime", insertEndTime,
                QueryOperand.LESSTHAN);
        dbQuery.addOrder("dataTime.refTime", true);

        return (List<PersistableDataObject>) this.queryByCriteria(dbQuery);
    }

    public double getHDF5Value(PluginDataObject pdo,
            CoordinateReferenceSystem crs, Coordinate coord,
            double defaultReturn) throws Exception {
        IDataStore store = getDataStore((IPersistable) pdo);
        // TODO a cache would probably be good here
        double rval = defaultReturn;
        if (pdo instanceof ISpatialEnabled) {
            ISpatialObject spat = getSpatialObject(pdo);
            DataReprojector reprojector = getDataReprojector(store);
            ReferencedEnvelope nativeEnv = getNativeEnvelope(spat);
            IDataRecord data = reprojector.getProjectedPoints(pdo.getDataURI(),
                    spat, nativeEnv, crs, new Coordinate[] { coord });
            Double res = extractSingle(data);
            if (res != null) {
                rval = res;
            }
        }
        return rval;
    }

    /**
     * @param record
     * @param crs
     *            target crs for projected data
     * @param envelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws Exception
     */
    public ReferencedDataRecord getProjected(PluginDataObject record,
            CoordinateReferenceSystem crs, Envelope envelope) throws Exception {
        ReferencedEnvelope targetEnv = new ReferencedEnvelope(
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(),
                envelope.getMaxY(), crs);
        return getProjected(record, targetEnv);
    }

    /**
     * @param record
     * @param crs
     *            target crs for projected data
     * @param envelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws Exception
     */
    public GridCoverage2D getProjectedCoverage(PluginDataObject record,
            CoordinateReferenceSystem crs, Envelope envelope) throws Exception {
        ReferencedEnvelope targetEnv = new ReferencedEnvelope(
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(),
                envelope.getMaxY(), crs);
        return getProjectedCoverage(record, targetEnv);
    }

    /**
     * @param record
     * @param targetEnvelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws Exception
     */
    public ReferencedDataRecord getProjected(PluginDataObject record,
            ReferencedEnvelope targetEnvelope) throws Exception {
        ISpatialObject spatial = getSpatialObject(record);
        IDataStore store = getDataStore((IPersistable) record);
        DataReprojector reprojector = getDataReprojector(store);
        ReferencedEnvelope nativeEnvelope = getNativeEnvelope(spatial);
        return reprojector.getReprojected(record.getDataURI(), spatial,
                nativeEnvelope, targetEnvelope);
    }

    /**
     * @param record
     * @param targetEnvelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws Exception
     */
    public GridCoverage2D getProjectedCoverage(PluginDataObject record,
            ReferencedEnvelope envelope) throws Exception {
        ISpatialObject spatial = getSpatialObject(record);
        IDataStore store = getDataStore((IPersistable) record);
        DataReprojector reprojector = getDataReprojector(store);
        ReferencedEnvelope nativeEnvelope = getNativeEnvelope(spatial);
        return reprojector.getReprojectedCoverage(record.getDataURI(), spatial,
                nativeEnvelope, envelope);
    }

    protected ISpatialObject getSpatialObject(PluginDataObject record)
            throws Exception {
        if (record instanceof ISpatialEnabled) {
            return ((ISpatialEnabled) record).getSpatialObject();
        } else {
            throw new Exception(record.getClass() + " is not spatially enabled");
        }
    }

    protected DataReprojector getDataReprojector(IDataStore dataStore) {
        return new DataReprojector(dataStore);
    }

    protected ReferencedEnvelope getNativeEnvelope(ISpatialObject spatial)
            throws FactoryException {
        CoordinateReferenceSystem crs = spatial.getCrs();
        Geometry geom = spatial.getGeometry();
        return MapUtil.getBoundingEnvelope(crs, (Polygon) geom);
    }

    public Double extractSingle(IDataRecord record) {
        Double rval = null;
        if (record == null) {
            return rval;
        }
        if (record instanceof ByteDataRecord) {
            byte[] data = ((ByteDataRecord) record).getByteData();
            rval = (double) data[0];
        } else if (record instanceof FloatDataRecord) {
            float[] data = ((FloatDataRecord) record).getFloatData();
            rval = (double) data[0];
        } else if (record instanceof IntegerDataRecord) {
            int[] data = ((IntegerDataRecord) record).getIntData();
            rval = (double) data[0];
        }
        return rval;
    }
}
