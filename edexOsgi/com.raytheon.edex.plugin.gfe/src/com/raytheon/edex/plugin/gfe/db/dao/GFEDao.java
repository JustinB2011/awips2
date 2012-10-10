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

package com.raytheon.edex.plugin.gfe.db.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.edex.db.dao.DefaultPluginDao;
import com.raytheon.edex.plugin.gfe.config.GFESiteActivation;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfig;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfigManager;
import com.raytheon.edex.plugin.gfe.exception.GfeConfigurationException;
import com.raytheon.edex.plugin.gfe.server.GridParmManager;
import com.raytheon.edex.plugin.gfe.server.database.D2DGridDatabase;
import com.raytheon.edex.plugin.gfe.server.database.GridDatabase;
import com.raytheon.edex.plugin.gfe.util.GridTranslator;
import com.raytheon.edex.plugin.gfe.util.SendNotifications;
import com.raytheon.edex.plugin.grib.util.DataFieldTableLookup;
import com.raytheon.uf.common.comm.CommunicationException;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.DatabaseID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.DatabaseID.DataType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.GridUpdateNotification;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.LockNotification;
import com.raytheon.uf.common.dataplugin.gfe.util.GfeUtil;
import com.raytheon.uf.common.dataplugin.grid.GridConstants;
import com.raytheon.uf.common.dataplugin.grid.GridInfoConstants;
import com.raytheon.uf.common.dataplugin.grid.GridInfoRecord;
import com.raytheon.uf.common.dataplugin.grid.GridRecord;
import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataplugin.level.LevelFactory;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.dataquery.db.QueryResult;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.purge.PurgeLogger;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * Data access object for manipulating GFE Records
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 04/08/08     #875       bphillip    Initial Creation
 * 05/16/08     #875       bphillip    Added D2D grib querying methods
 * 06/17/08     #940       bphillip    Implemented GFE Locking
 * 06/17/09     #2380      randerso    Removed purging of grid history.
 *                                     Should cascade when record deleted.
 * 08/07/09     #2763     njensen   Refactored queryByD2DParmId
 * 09/10/12     DR15137   ryu       Changed for MOSGuide D2D mxt/mnt grids for consistency
 *                                  with A1.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class GFEDao extends DefaultPluginDao {

    public GFEDao() throws PluginException {
        super("gfe");
    }

    /**
     * Creates a new GFE Dao
     * 
     * @throws PluginException
     */
    public GFEDao(String pluginName) throws PluginException {
        super(pluginName);
    }

    @Override
    public void purgeExpiredData() throws PluginException {
        Set<String> sites = GFESiteActivation.getInstance().getActiveSites();
        for (String siteID : sites) {
            List<GridUpdateNotification> gridNotifcations = new ArrayList<GridUpdateNotification>();
            List<LockNotification> lockNotifications = new ArrayList<LockNotification>();

            try {
                GridParmManager.versionPurge(siteID);
                GridParmManager.gridsPurge(gridNotifcations, lockNotifications,
                        siteID);
                PurgeLogger.logInfo(
                        "Purging Expired pending isc send requests...", "gfe");
                int requestsPurged = new IscSendRecordDao()
                        .purgeExpiredPending();
                PurgeLogger.logInfo("Purged " + requestsPurged
                        + " expired pending isc send requests.", "gfe");
            } catch (DataAccessLayerException e) {
                throw new PluginException(
                        "Error purging expired send ISC records!", e);
            } finally {
                SendNotifications.send(gridNotifcations);
                SendNotifications.send(lockNotifications);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public int purgeDatabaseForSite(final String siteID)
            throws DataAccessLayerException {
        return (Integer) txTemplate.execute(new TransactionCallback() {
            @Override
            public Integer doInTransaction(TransactionStatus status) {
                List<DatabaseID> dbs = getDatabaseInventoryForSite(siteID);
                if (dbs.isEmpty()) {
                    return 0;
                } else {
                    DetachedCriteria criteria = DetachedCriteria.forClass(
                            GFERecord.class).add(
                            Property.forName("dbId").in(dbs));
                    List<GFERecord> list = getHibernateTemplate()
                            .findByCriteria(criteria);
                    if (!list.isEmpty()) {
                        getHibernateTemplate().deleteAll(list);
                    }
                    return list.size();
                }
            }
        });
    }

    private List<DatabaseID> getDatabaseInventoryForSite(String siteID) {
        List<DatabaseID> dbInventory = new ArrayList<DatabaseID>();
        Object[] dbIds = executeSQLQuery("select distinct dbId from awips.gfe where dbId like '"
                + siteID.toUpperCase() + "%'");
        for (Object id : dbIds) {
            dbInventory.add(new DatabaseID((String) id));
        }
        return dbInventory;
    }

    /**
     * Retrieves a GFE Record
     * 
     * @param record
     *            the record
     * @return The record, populated
     */
    public GFERecord getRecord(PluginDataObject record) {
        return (GFERecord) this.queryById(record);
    }

    public GFERecord[] saveOrUpdate(final GFERecord[] records) {
        List<GFERecord> failedToSave = new ArrayList<GFERecord>();
        for (GFERecord rec : records) {
            if (rec.getIdentifier() == null) {
                try {
                    rec.constructDataURI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (rec.getInsertTime() == null) {
                rec.setInsertTime(Calendar.getInstance());
            }
        }

        final int batchSize = 100;

        // First, try committing all of the records in batches of size
        // batchSize. If a failure occurs on a batch, add that batch to be
        // retried individually. If the whole commit fails, try saving them all
        // individually.
        Session sess = null;
        Transaction tx = null;
        int commitPoint = 0;
        int index = 0;
        boolean notDone = index < records.length;
        try {
            sess = getHibernateTemplate().getSessionFactory().openSession();
            tx = sess.beginTransaction();
            boolean persistIndividually = false;
            String sql = "select id from awips." + pluginName
                    + " where dataURI=:dataURI";
            Query q = sess.createSQLQuery(sql);

            while (notDone) {
                GFERecord rec = records[index++];
                notDone = index < records.length;
                try {
                    q.setString("dataURI", rec.getDataURI());
                    List<?> list = q.list();
                    if (list == null || list.size() == 0) {
                        sess.save(rec);
                    } else {
                        rec.setId(((Number) list.get(0)).intValue());
                        sess.update(rec);
                    }
                    if (index % batchSize == 0 || persistIndividually
                            || !notDone) {
                        sess.flush();
                        sess.clear();
                        tx.commit();
                        tx = null;
                        commitPoint = index;
                        if (persistIndividually && index % batchSize == 0) {
                            // batch persisted individually switch back to batch
                            persistIndividually = false;
                        }
                        if (notDone) {
                            tx = sess.beginTransaction();
                            q = sess.createSQLQuery(sql);
                        }
                    }
                } catch (Exception e) {
                    if (tx != null) {
                        try {
                            tx.rollback();
                        } catch (Exception e1) {
                            logger.error(
                                    "Error occurred rolling back transaction",
                                    e1);
                        }
                    }

                    if (persistIndividually) {
                        // log it and continue
                        logger.error(
                                "Error occurred persisting gfe record individually",
                                e);
                        failedToSave.add(rec);
                    } else {
                        // change to persistIndividually and retry from last
                        // commit
                        persistIndividually = true;
                        index = commitPoint;
                        notDone = true;
                    }

                    tx = sess.beginTransaction();
                    q = sess.createSQLQuery(sql);
                }
            }
        } finally {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    logger.error("Error occurred rolling back transaction", e1);
                }
            }
            if (sess != null) {
                sess.close();
            }
        }

        return failedToSave.toArray(new GFERecord[failedToSave.size()]);

    }

    @Override
    public void delete(final PersistableDataObject obj) {
        GFERecord rec = (GFERecord) obj;
        if (rec.getDataURI() == null) {
            try {
                rec.constructDataURI();
            } catch (PluginException e) {
                logger.error("Unable to construct dataURI for GFE record", e);
            }
        }
        rec = this.getRecord(rec);
        super.delete(rec);
    }

    /**
     * Gets list of all database IDs currently being stored in the database
     * 
     * @return The list of all database IDs currently being stored in the
     *         database
     */
    public List<DatabaseID> getDatabaseInventory() {
        List<DatabaseID> dbInventory = new ArrayList<DatabaseID>();

        Object[] dbIds = executeSQLQuery("select distinct dbId from awips.gfe");
        for (Object id : dbIds) {
            dbInventory.add(new DatabaseID((String) id));
        }
        return dbInventory;
    }

    /**
     * Gets all GFE Records with the specified ParmID
     * 
     * @param parmId
     *            The parmID to query for
     * @return All GFE Records with the specified ParmID
     * @throws DataAccessLayerException
     *             If errors occur during the query
     */
    @SuppressWarnings("unchecked")
    public ArrayList<GFERecord> queryByParmID(ParmID parmId)
            throws DataAccessLayerException {
        return (ArrayList<GFERecord>) this.queryBySingleCriteria("parmId",
                parmId.getParmId());
    }

    public GFERecord getRecord(final ParmID parmId, final TimeRange tr)
            throws DataAccessLayerException {
        GFERecord retVal = (GFERecord) txTemplate
                .execute(new TransactionCallback() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public GFERecord doInTransaction(TransactionStatus status) {
                        DetachedCriteria criteria = DetachedCriteria
                                .forClass(GFERecord.class)
                                .add(Property.forName("parmId").eq(parmId))
                                .add(Property.forName("dataTime").eq(
                                        new DataTime(tr.getStart().getTime(),
                                                tr)));
                        return ((List<GFERecord>) getHibernateTemplate()
                                .findByCriteria(criteria)).get(0);
                    }
                });
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public List<GFERecord> getRecords(final ParmID parmId,
            final List<TimeRange> times) {
        if (times.isEmpty()) {
            return Collections.emptyList();
        }
        List<GFERecord> retVal = (List<GFERecord>) txTemplate
                .execute(new TransactionCallback() {
                    @Override
                    public List<GFERecord> doInTransaction(
                            TransactionStatus status) {
                        List<DataTime> dataTimes = new ArrayList<DataTime>();
                        for (TimeRange tr : times) {
                            dataTimes.add(new DataTime(tr.getStart().getTime(),
                                    tr));
                        }

                        DetachedCriteria criteria = DetachedCriteria
                                .forClass(GFERecord.class)
                                .add(Property.forName("parmId").eq(parmId))
                                .add(Property.forName("dataTime").in(dataTimes));
                        List<GFERecord> list = getHibernateTemplate()
                                .findByCriteria(criteria);
                        return list;
                    }
                });
        return retVal;
    }

    public void deleteRecords(final ParmID parmId, final List<TimeRange> times) {
        if (times.isEmpty()) {
            return;
        }
        final List<GFERecord> recordsToDelete = getRecords(parmId, times);
        final List<GridDataHistory> histories = new ArrayList<GridDataHistory>();
        for (GFERecord rec : recordsToDelete) {
            histories.addAll(rec.getGridHistory());
        }
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                getHibernateTemplate().deleteAll(histories);
                getHibernateTemplate().deleteAll(recordsToDelete);
                statusHandler.info("Deleted " + recordsToDelete.size()
                        + " records from the database.");
            }
        });

        File hdf5File = GfeUtil.getHDF5File(GridDatabase.gfeBaseDataDir,
                parmId.getDbId());
        IDataStore dataStore = DataStoreFactory.getDataStore(hdf5File);
        String[] groupsToDelete = new String[times.size()];
        for (int i = 0; i < times.size(); i++) {
            groupsToDelete[i] = GfeUtil.getHDF5Group(parmId, times.get(i));
        }
        try {
            for (String grp : groupsToDelete) {
                dataStore.delete(grp);
            }
            statusHandler.handle(Priority.DEBUG,
                    "Deleted: " + Arrays.toString(groupsToDelete) + " from "
                            + hdf5File.getName());

        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error deleting hdf5 records", e);
        }

    }

    @SuppressWarnings("unchecked")
    public void updateGridHistories(ParmID parmId,
            Map<TimeRange, List<GridDataHistory>> history)
            throws DataAccessLayerException {
        for (TimeRange range : history.keySet()) {
            DatabaseQuery recordQuery = new DatabaseQuery(GFERecord.class);
            recordQuery.addQueryParam("parmId", parmId);
            recordQuery.addQueryParam("dataTime.validPeriod", range);
            List<GFERecord> result = (List<GFERecord>) this
                    .queryByCriteria(recordQuery);
            if (result.size() == 0) {
                logger.error("No histories were updated for: " + parmId + "::"
                        + range);
            } else if (result.size() == 1) {
                GFERecord returnedRecord = result.get(0);

                List<GridDataHistory> existHist = returnedRecord
                        .getGridHistory();
                List<GridDataHistory> newHist = history.get(range);
                consolidateHistories(existHist, newHist);

                this.update(returnedRecord);
            } else {
                logger.error("MORE THAN 1 RESULT WAS RETURNED: "
                        + result.size());
            }
        }
    }

    public void consolidateHistories(List<GridDataHistory> existHist,
            List<GridDataHistory> newHist) {
        for (int i = 0; i < newHist.size(); i++) {
            if (i < existHist.size()) {
                existHist.get(i).replaceValues(newHist.get(i));
            } else {
                existHist.add(newHist.get(i));
            }
        }

        if (existHist.size() > newHist.size()) {
            // not sure if we will ever have a case where the updated
            // record has fewer history records than existing record

            // log a message as this has the potential to cause orphaned
            // history records
            statusHandler.handle(Priority.WARN,
                    "Updated record has fewer history records.");
            for (int i = newHist.size(); i < existHist.size(); i++) {
                existHist.remove(i);
            }
        }
    }

    /**
     * Gets all GFE Records with the specified DatabaseID
     * 
     * @param dbId
     *            The DatabaseID to query for
     * @return All GFE Records with the specified DatabaseID
     * @throws DataAccessLayerException
     *             If errors occur during the query
     */
    @SuppressWarnings("unchecked")
    public ArrayList<GFERecord> queryByDatabaseID(DatabaseID dbId)
            throws DataAccessLayerException {
        return (ArrayList<GFERecord>) this.queryBySingleCriteria("dbId",
                dbId.toString());
    }

    /**
     * Gets the list of times for a given parmId
     * 
     * @param parmId
     *            The id of the parm
     * @return The list of times for a given parm name and level
     * @throws DataAccessLayerException
     */
    public List<TimeRange> getTimes(ParmID parmId)
            throws DataAccessLayerException {
        List<TimeRange> times = new ArrayList<TimeRange>();
        String timeQuery = "SELECT rangestart,rangeend from awips.gfe where parmid='"
                + parmId + "' ORDER BY rangestart";
        QueryResult result = (QueryResult) executeNativeSql(timeQuery);
        for (int i = 0; i < result.getResultCount(); i++) {
            times.add(new TimeRange((Date) result.getRowColumnValue(i, 0),
                    (Date) result.getRowColumnValue(i, 1)));
        }
        return times;
    }

    /**
     * Retrieves the grid history for the specified parm and time ranges
     * 
     * @param id
     *            The parm id
     * @param trs
     *            The time ranges to search for
     * @return The grid histories
     * @throws DataAccessLayerException
     *             If problems during database interaction occur
     */
    public Map<TimeRange, List<GridDataHistory>> getGridHistory(ParmID id,
            List<TimeRange> trs) throws DataAccessLayerException {

        Map<TimeRange, List<GridDataHistory>> history = new HashMap<TimeRange, List<GridDataHistory>>();
        if (trs.isEmpty()) {
            return history;
        }
        List<GFERecord> records = this.getRecords(id, trs);
        for (GFERecord rec : records) {
            TimeRange tr = rec.getTimeRange();
            history.put(tr, rec.getGridHistory());
        }
        return history;
    }

    /**
     * Retrieves a list of valid times for a specified ParmID from the grib
     * metadata database. The valid time is constructed by adding the forecast
     * time to the reference time.
     * 
     * @param id
     *            The parmID to get the times for
     * @return The list of times associated with the specified ParmID
     * @throws DataAccessLayerException
     *             If errors occur while querying the metadata database
     */
    public List<TimeRange> getD2DTimes(ParmID id)
            throws DataAccessLayerException {
        return queryTimeByD2DParmId(id);
    }

    /**
     * Retrieves a list of available forecast times
     * 
     * @param dbId
     *            The database ID to get the times for
     * @return The list of forecast times associated with the specified
     *         DatabaseID
     * @throws DataAccessLayerException
     *             If errors occur while querying the metadata database
     */
    @SuppressWarnings("unchecked")
    public List<Integer> getD2DForecastTimes(DatabaseID dbId)
            throws DataAccessLayerException {
        DatabaseQuery query = new DatabaseQuery(GridRecord.class.getName());
        query.addDistinctParameter("dataTime.fcstTime");
        try {
            IFPServerConfig config = IFPServerConfigManager
                    .getServerConfig(dbId.getSiteId());
            query.addQueryParam(GridConstants.DATASET_ID,
                    config.d2dModelNameMapping(dbId.getModelName()));
        } catch (GfeConfigurationException e) {
            throw new DataAccessLayerException(
                    "Error occurred looking up model name mapping", e);
        }
        query.addQueryParam("dataTime.refTime", dbId.getModelTimeAsDate());
        query.addOrder("dataTime.fcstTime", true);
        List<?> vals = this.queryByCriteria(query);
        return (List<Integer>) vals;
    }

    /**
     * Retrieves a GridRecord from the grib metadata database based on a ParmID
     * and a TimeRange
     * 
     * @param id
     *            The parmID of the desired GridRecord
     * @param timeRange
     *            The timeRange of the desired GridRecord
     * @return The GridRecord from the grib metadata database
     * @throws DataAccessLayerException
     *             If errors occur while querying the metadata database
     */
    public GridRecord getD2DGrid(ParmID id, TimeRange timeRange,
            GridParmInfo info) throws DataAccessLayerException {
        List<GridRecord> records = queryByD2DParmId(id);
        List<TimeRange> gribTimes = new ArrayList<TimeRange>();
        for (GridRecord record : records) {
            gribTimes.add(record.getDataTime().getValidPeriod());
        }

        for (int i = 0; i < records.size(); i++) {
            TimeRange gribTime = records.get(i).getDataTime().getValidPeriod();
            try {
                if (isMos(id)) {
                    TimeRange time = info.getTimeConstraints().constraintTime(
                            gribTime.getEnd());
                    if (timeRange.getEnd().equals(time.getEnd())
                            || !info.getTimeConstraints().anyConstraints()) {
                        GridRecord retVal = records.get(i);
                        retVal.setPluginName(GridConstants.GRID);
                        return retVal;
                    }
                } else if (D2DGridDatabase.isNonAccumDuration(id, gribTimes)) {
                    if (timeRange.getStart().equals(gribTime.getEnd())
                            || timeRange.equals(gribTime)) {
                        GridRecord retVal = records.get(i);
                        retVal.setPluginName(GridConstants.GRID);
                        return retVal;
                    }

                } else {
                    TimeRange time = info.getTimeConstraints().constraintTime(
                            gribTime.getStart());
                    if ((timeRange.getStart().equals(time.getStart()) || !info
                            .getTimeConstraints().anyConstraints())) {
                        GridRecord retVal = records.get(i);
                        retVal.setPluginName(GridConstants.GRID);
                        return retVal;
                    }
                }
            } catch (GfeConfigurationException e) {
                throw new DataAccessLayerException(
                        "Error getting configuration for "
                                + id.getDbId().getSiteId(), e);
            }
        }

        return null;
    }

    /**
     * Gets a list of GridRecords from the grib metadata database which match
     * the given ParmID
     * 
     * @param id
     *            The ParmID to search with
     * @return The list of GridRecords from the grib metadata database which
     *         match the given ParmID
     * @throws DataAccessLayerException
     *             If errors occur while querying the metadata database
     */
    @SuppressWarnings("unchecked")
    public List<GridRecord> queryByD2DParmId(ParmID id)
            throws DataAccessLayerException {
        Session s = null;
        try {
            String levelName = GridTranslator.getLevelName(id.getParmLevel());

            double[] levelValues = GridTranslator.getLevelValue(id
                    .getParmLevel());
            boolean levelOnePresent = (levelValues[0] != Level
                    .getInvalidLevelValue());
            boolean levelTwoPresent = (levelValues[1] != Level
                    .getInvalidLevelValue());
            Level level = null;

            // to have a level 2, must have a level one
            try {
                if (levelOnePresent && levelTwoPresent) {
                    level = LevelFactory.getInstance().getLevel(levelName,
                            levelValues[0], levelValues[1]);
                } else if (levelOnePresent) {
                    level = LevelFactory.getInstance().getLevel(levelName,
                            levelValues[0]);
                } else {
                    level = LevelFactory.getInstance().getLevel(levelName, 0.0);
                }
            } catch (CommunicationException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            if (level == null) {
                logger.warn("Unable to query D2D parms, ParmID " + id
                        + " does not map to a level");
                return new ArrayList<GridRecord>();
            }

            s = getHibernateTemplate().getSessionFactory().openSession();

            Criteria modelCrit = s.createCriteria(GridInfoRecord.class);
            Criterion baseCrit = Restrictions
                    .eq(GridInfoConstants.LEVEL, level);

            DatabaseID dbId = id.getDbId();
            try {
                IFPServerConfig config = IFPServerConfigManager
                        .getServerConfig(dbId.getSiteId());
                baseCrit = Restrictions.and(baseCrit, Restrictions.eq(
                        GridInfoConstants.DATASET_ID,
                        config.d2dModelNameMapping(dbId.getModelName())));
            } catch (GfeConfigurationException e) {
                throw new DataAccessLayerException(
                        "Error occurred looking up model name mapping", e);
            }
            String abbreviation = DataFieldTableLookup.getInstance()
                    .lookupDataName(id.getParmName());
            if (abbreviation == null) {
                abbreviation = id.getParmName();
            }
            abbreviation = abbreviation.toLowerCase();
            Criterion abbrevCrit = Restrictions
                    .and(baseCrit,
                            Restrictions.or(
                                    Restrictions
                                            .sqlRestriction("lower(parameter_abbreviation) = '"
                                                    + abbreviation + "'"),
                                    Restrictions
                                            .sqlRestriction("lower(parameter_abbreviation) like '"
                                                    + abbreviation + "%hr'")));

            modelCrit.add(abbrevCrit);
            List<?> results = modelCrit.list();
            GridInfoRecord model = null;
            if (results.size() == 0) {
                return new ArrayList<GridRecord>(0);
            } else if (results.size() > 1) {
                // hours matched, take hour with least number that matches exact
                // param
                Pattern p = Pattern.compile("^" + abbreviation + "(\\d+)hr$");
                int lowestHr = -1;
                for (GridInfoRecord m : (List<GridInfoRecord>) results) {
                    String param = m.getParameter().getAbbreviation()
                            .toLowerCase();
                    if (param.equals(abbreviation) && lowestHr < 0) {
                        model = m;
                    } else {
                        Matcher matcher = p.matcher(param);
                        if (matcher.matches()) {
                            int hr = Integer.parseInt(matcher.group(1));
                            if (lowestHr < 0 || hr < lowestHr) {
                                model = m;
                                lowestHr = hr;
                            }
                        }
                    }
                }

            } else {
                model = (GridInfoRecord) results.get(0);
            }

            Criteria recordCrit = s.createCriteria(GridRecord.class);
            baseCrit = Restrictions.eq("info", model);
            baseCrit = Restrictions.and(
                    baseCrit,
                    Restrictions.eq("dataTime.refTime",
                            dbId.getModelTimeAsDate()));
            recordCrit.add(baseCrit);
            recordCrit.addOrder(Order.asc("dataTime.fcstTime"));
            return recordCrit.list();
        } finally {
            if (s != null) {
                s.flush();
                s.close();
            }
        }
    }

    public List<TimeRange> queryTimeByD2DParmId(ParmID id)
            throws DataAccessLayerException {
        List<TimeRange> timeList = new ArrayList<TimeRange>();
        if (id.getParmName().equalsIgnoreCase("wind")) {
            ParmID uWindId = new ParmID(id.toString().replace("wind", "uW"));
            List<TimeRange> uTimeList = new ArrayList<TimeRange>();
            List<DataTime> results = executeD2DParmQuery(uWindId);
            for (DataTime o : results) {
                uTimeList.add(new TimeRange(o.getValidPeriod().getStart(),
                        3600 * 1000));
            }

            ParmID vWindId = new ParmID(id.toString().replace("wind", "vW"));
            List<TimeRange> vTimeList = new ArrayList<TimeRange>();
            results = executeD2DParmQuery(vWindId);
            for (DataTime o : results) {
                vTimeList.add(new TimeRange(o.getValidPeriod().getStart(),
                        3600 * 1000));
            }

            if ((!uTimeList.isEmpty()) && (!vTimeList.isEmpty())
                    & (uTimeList.size() == vTimeList.size())) {
                for (TimeRange tr : uTimeList) {
                    if (vTimeList.contains(tr)) {
                        timeList.add(new TimeRange(tr.getStart(), tr.getStart()));
                    }
                }

                return timeList;
            }

            ParmID sWindId = new ParmID(id.toString().replace("wind", "ws"));
            List<TimeRange> sTimeList = new ArrayList<TimeRange>();
            results = executeD2DParmQuery(sWindId);
            for (DataTime o : results) {
                sTimeList.add(new TimeRange(o.getValidPeriod().getStart(),
                        3600 * 1000));
            }

            ParmID dWindId = new ParmID(id.toString().replace("wind", "wd"));
            List<TimeRange> dTimeList = new ArrayList<TimeRange>();
            results = executeD2DParmQuery(dWindId);
            for (DataTime o : results) {
                dTimeList.add(new TimeRange(o.getValidPeriod().getStart(),
                        3600 * 1000));
            }

            if ((!sTimeList.isEmpty()) && (!dTimeList.isEmpty())
                    & (sTimeList.size() == dTimeList.size())) {
                for (TimeRange tr : sTimeList) {
                    if (dTimeList.contains(tr)) {
                        timeList.add(new TimeRange(tr.getStart(), tr.getStart()));
                    }
                }

                return timeList;
            }
        } else {
            List<DataTime> results = executeD2DParmQuery(id);
            for (DataTime o : results) {
                if (isMos(id)) {
                    timeList.add(new TimeRange(o.getValidPeriod().getEnd(), o
                            .getValidPeriod().getDuration()));
                } else {
                    timeList.add(o.getValidPeriod());
                }

            }
        }

        return timeList;
    }

    private List<DataTime> executeD2DParmQuery(ParmID id)
            throws DataAccessLayerException {
        List<DataTime> times = new ArrayList<DataTime>();
        List<GridRecord> records = queryByD2DParmId(id);
        for (GridRecord record : records) {
            times.add(record.getDataTime());
        }
        return times;
    }

    public void purgeGFEGrids(final DatabaseID dbId) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            @SuppressWarnings("unchecked")
            public void doInTransactionWithoutResult(TransactionStatus status) {
                DetachedCriteria criteria = DetachedCriteria.forClass(
                        GFERecord.class).add(Property.forName("dbId").eq(dbId));
                List<GFERecord> list = getHibernateTemplate().findByCriteria(
                        criteria);
                if (!list.isEmpty()) {
                    List<GridDataHistory> histories = new ArrayList<GridDataHistory>();
                    for (GFERecord rec : list) {
                        histories.addAll(rec.getGridHistory());
                    }
                    getHibernateTemplate().deleteAll(histories);
                    getHibernateTemplate().deleteAll(list);
                }
            }
        });
    }

    public List<DatabaseID> getD2DDatabaseIdsFromDb(String d2dModelName,
            String gfeModel, String siteID) throws DataAccessLayerException {
        return getD2DDatabaseIdsFromDb(d2dModelName, gfeModel, siteID, -1);
    }

    public List<DatabaseID> getD2DDatabaseIdsFromDb(String d2dModelName,
            String gfeModel, String siteID, int maxRecords)
            throws DataAccessLayerException {
        List<DatabaseID> dbInventory = new ArrayList<DatabaseID>();

        DatabaseQuery query = new DatabaseQuery(GridRecord.class.getName());
        query.addDistinctParameter("dataTime.refTime");
        query.addQueryParam(GridConstants.DATASET_ID, d2dModelName);
        query.addOrder("dataTime.refTime", false);

        List<?> result = this.queryByCriteria(query);

        for (Object obj : result) {
            DatabaseID dbId = null;
            dbId = new DatabaseID(siteID, DataType.GRID, "D2D", gfeModel,
                    (Date) obj);
            if (!dbInventory.contains(dbId)) {
                dbInventory.add(dbId);
            }

        }
        return dbInventory;
    }

    /**
     * Retrieves the latest (or newest) model run for the given site and model
     * name.
     * 
     * @param d2dModel
     *            A GridModel object that contains the D2D model name.
     * @param gfeModel
     *            The GFE model name that corresponds to d2dModel.
     * @param siteID
     *            The site to retrieve the data for.
     * @return The DatabaseID of the newest D2D model, or null if no models can
     *         be found.
     * @throws DataAccessLayerException
     */
    public DatabaseID getLatestD2DDatabaseIdsFromDb(String d2dModelName,
            String gfeModel, String siteID) throws DataAccessLayerException {
        List<DatabaseID> dbIds = getD2DDatabaseIdsFromDb(d2dModelName,
                gfeModel, siteID, 1);
        if (!dbIds.isEmpty()) {
            return dbIds.get(0);
        } else {
            return null;
        }
    }

    public Set<ParmID> getD2DParmIdsFromDb(String d2dModelName, DatabaseID dbId)
            throws DataAccessLayerException {

        Set<ParmID> parmIds = new HashSet<ParmID>();

        DatabaseQuery query = new DatabaseQuery(GridRecord.class.getName());
        query.addDistinctParameter(GridConstants.PARAMETER_ABBREVIATION);
        query.addDistinctParameter(GridConstants.MASTER_LEVEL_NAME);
        query.addDistinctParameter(GridConstants.LEVEL_ONE);
        query.addDistinctParameter(GridConstants.LEVEL_TWO);
        query.addQueryParam(GridConstants.DATASET_ID, d2dModelName);
        query.addQueryParam(
                "dataTime.refTime",
                TimeUtil.formatDate(dbId.getModelTimeAsDate()).replaceAll("_",
                        " "));

        List<?> result = this.queryByCriteria(query);

        for (Object obj : result) {
            Object[] objArr = (Object[]) obj;
            String levelName = GridTranslator.getShortLevelName(
                    (String) objArr[1], (Double) objArr[2], (Double) objArr[3]);
            if (!levelName.equals(LevelFactory.UNKNOWN_LEVEL)) {
                String abbrev = (String) objArr[0];
                abbrev = DataFieldTableLookup.getInstance().lookupCdlName(
                        abbrev);
                ParmID newParmId = new ParmID(abbrev, dbId, levelName);
                parmIds.add(newParmId);
            }

        }
        return parmIds;
    }

    /**
     * Removes GridParmInfo from the HDF5 file and any data associated with that
     * info
     * 
     * @param parmAndLevel
     *            The parm and level to delete
     * @param dbId
     *            The database to delete from
     * @param ds
     *            The data store file
     * @throws DataAccessLayerException
     *             If errors occur
     */
    public void removeOldParm(String parmAndLevel, DatabaseID dbId,
            IDataStore ds) throws DataAccessLayerException {

        ParmID pid = new ParmID(parmAndLevel + ":" + dbId.toString());

        try {
            ds.delete("/GridParmInfo/" + parmAndLevel);
        } catch (Exception e1) {
            throw new DataAccessLayerException("Error deleting data from HDF5",
                    e1);
        }
        List<TimeRange> trs = this.getTimes(pid);
        this.deleteRecords(pid, trs);
    }

    @Override
    protected IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception {
        return null;
    }

    public Date getLatestDbIdInsertTime(DatabaseID dbId)
            throws DataAccessLayerException {
        QueryResult result = (QueryResult) this
                .executeNativeSql("select max(inserttime) as maxtime from awips.gfe where dbid='"
                        + dbId.toString() + "';");
        if (result.getResultCount() == 0) {
            return null;
        } else {
            return (Date) result.getRowColumnValue(0, "maxtime");
        }
    }

    /**
     * Retrieves the latest database ID for a given a model name and site
     * identifier.
     * 
     * @param siteId
     *            The site's identifier (e.g., "OAX")
     * @param modelName
     *            The name of the model run (e.g., "GFS40" or "RUC13")
     * @return The DatabaseID of the latest model run for the given parameters
     *         or null if no copies of the given model have been ingested for
     *         the given site.
     * @throws DataAccessLayerException
     */
    public DatabaseID getLatestModelDbId(String siteId, String modelName)
            throws DataAccessLayerException {
        QueryResult result = (QueryResult) this
                .executeNativeSql("select max(dbid) as maxdbid from awips.gfe where dbid like '"
                        + siteId + "!_GRID!_!_" + modelName + "!_%' escape '!'");
        if (result.getResultCount() == 0) {
            return null;
        } else {
            String db = (String) result.getRowColumnValue(0, "maxdbid");
            if (db == null) {
                return null;
            }
            return new DatabaseID(db);
        }
    }

    public static boolean isMos(ParmID id) {
        return id.getDbId().getModelName().equals("MOSGuide")
                && (id.getParmName().startsWith("mxt") || id.getParmName()
                        .startsWith("mnt"));
    }
}
