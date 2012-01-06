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

package com.raytheon.edex.plugin.gfe.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.edex.plugin.gfe.db.dao.GFEDao;
import com.raytheon.edex.plugin.gfe.server.database.D2DGridDatabase;
import com.raytheon.edex.plugin.gfe.server.database.GridDatabase;
import com.raytheon.edex.plugin.gfe.server.lock.LockManager;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.server.lock.LockTable;
import com.raytheon.uf.common.dataplugin.gfe.server.lock.LockTable.LockMode;
import com.raytheon.uf.common.dataplugin.gfe.server.message.ServerResponse;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.GridUpdateNotification;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.LockNotification;
import com.raytheon.uf.common.dataplugin.gfe.server.request.GetGridRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.request.LockRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.request.LockTableRequest;
import com.raytheon.uf.common.dataplugin.gfe.server.request.SaveGridRequest;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.IGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.WeatherGridSlice;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.edex.database.plugin.PluginFactory;

/**
 * Server side representation of a parm. A GridParm is tied to a database.
 * Methods in this class generally delegate operations to the database.
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 04/08/08     #875       bphillip    Initial Creation
 * 06/17/08     #940       bphillip    Implemented GFE Locking
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class GridParm {

    /** The parm ID associated with this GridParm */
    private ParmID id;

    /** The grid database associated with this GridParm */
    private GridDatabase db;

    List<TimeRange> badDataTimes = new ArrayList<TimeRange>();

    public List<TimeRange> getBadDataTimes() {
        return badDataTimes;
    }

    /**
     * Creates a new empty GridParm
     */
    public GridParm() {

    }

    /**
     * Creates a new GridParm
     * 
     * @param id
     *            The parm ID associated with this GridParm
     * @param db
     *            The GridDatabase associated with this GridParm
     */
    public GridParm(ParmID id, GridDatabase db) {
        this.id = id;
        this.db = db;

    }

    public boolean isValid() {
        return id.isValid();
    }

    /**
     * Returns the grid inventory for this parameter through "trs"
     * 
     * @return The server response containing the grid inventory
     */
    public ServerResponse<List<TimeRange>> getGridInventory() {
        return db.getGridInventory(id);
    }

    /**
     * Returns the grid history for this parameter and specified grids through
     * history. Returns the status
     * 
     * @param trs
     *            The time ranges to get the history for
     * @return The server response containing the grid history
     */
    public ServerResponse<Map<TimeRange, List<GridDataHistory>>> getGridHistory(
            List<TimeRange> trs) {
        return db.getGridHistory(id, trs);
    }

    public ServerResponse<?> updateGridHistory(
            Map<TimeRange, List<GridDataHistory>> history) {
        ServerResponse<?> sr = new ServerResponse<String>();
        db.updateGridHistory(id, history);
        return sr;
    }

    /**
     * Returns the grid parameter information for this parameter through the
     * calling argument. Returns the status
     * 
     * @return The server status containing the grid info
     */
    public ServerResponse<GridParmInfo> getGridParmInfo() {
        return db.getGridParmInfo(id);

    }

    /**
     * Command to save grid data. The saveRequest defines the data to be saved
     * by the requestorId. Returns the status.
     * 
     * Verifies that the parmID in the saveRequest is for this parm. If not,
     * return false with an internal error. Calls checkTimes() to validate the
     * times in the replacement time range. Calls dataOkay() to check the
     * validity of the data. Calls checkLocks() to verify that the requester has
     * the lock for this save request. On error, return with the appropriate
     * error code. If all of the above checks pass, then call the
     * GridDatabase::saveGridData().
     * 
     * @param saveRequest
     *            The save grid request
     * @param requesterId
     *            The workstation ID of the requester
     * @return The server response
     */
    public ServerResponse<?> saveGridData(SaveGridRequest saveRequest,
            WsId requesterId, String siteID) {

        ServerResponse<?> sr = new ServerResponse<String>();

        // Valid ParmID?
        if (!id.equals(saveRequest.getParmId())) {
            sr.addMessage("Mismatch on parmID  saveRequest="
                    + saveRequest.getParmId().toString() + "  gridParm="
                    + id.toString());
            return sr;
        }

        // collapse the grid key
        for (GFERecord record : saveRequest.getGridSlices()) {
            IGridSlice slice = (IGridSlice) record.getMessageData();
            if (slice instanceof DiscreteGridSlice) {
                DiscreteGridSlice discreteSlice = (DiscreteGridSlice) record
                        .getMessageData();
                discreteSlice.collapse();
            } else if (slice instanceof WeatherGridSlice) {
                WeatherGridSlice weatherSlice = (WeatherGridSlice) record
                        .getMessageData();
                weatherSlice.collapse();
            }
        }

        // validate the times in the replacement time range
        sr.addMessages(checkTimes(saveRequest.getReplacementTimeRange(),
                saveRequest.getGridSliceTimes()));
        if (!sr.isOkay()) {
            return sr;
        }

        // validate the data
        sr.addMessages(recordsOkay(saveRequest.getGridSlices(),
                new ArrayList<TimeRange>()));
        if (!sr.isOkay()) {
            return sr;
        }

        // ensure the locks are okay
        sr.addMessages(checkLocks(saveRequest.getReplacementTimeRange(),
                requesterId, siteID));
        if (!sr.isOkay()) {
            return sr;
        }

        // Finally save the data
        ServerResponse<?> saveResponse = db.saveGridData(id,
                saveRequest.getReplacementTimeRange(),
                saveRequest.getGridSlices(), requesterId);
        sr.addMessages(saveResponse);
        return sr;
    }

    /**
     * Returns the requested grid data through "data". Returns the status.
     * "data" will only contain the GridSlices from this call (it is zero'd
     * first in this call).
     * 
     * Zeros out "data". Validates the parmid in the GetGridRequest to ensure
     * that it is the same as this parms. Calls the getGridInventory() to get
     * the current inventory for this parm. Ensures that all of the requested
     * time ranges are contained in the grid inventory. Requests the data from
     * the GridDatabase using getGridData(). Calls dataOkay() to check the
     * validity of the data. On error, resets "data" to length zero and returns.
     * If any of the retrieved grid slices are invalid (grid is there, but data
     * is invalid), then the badDataTimes entry is filled with the time of that
     * bad grid.
     * 
     * @param getRequest
     *            The get data request
     * @param badDataTimes
     *            The bad data times
     * @return The server response
     */
    public ServerResponse<List<IGridSlice>> getGridData(
            GetGridRequest getRequest, List<TimeRange> badDataTimes) {

        ServerResponse<List<IGridSlice>> sr = new ServerResponse<List<IGridSlice>>();

        // Valid parmid?
        if (!id.equals(getRequest.getParmId())) {
            sr.addMessage("Mismatch on parmID getRequest="
                    + getRequest.getParmId().toString() + " gridParm="
                    + id.getParmId().toString());
            return sr;
        }

        // Get current inventory
        List<TimeRange> reqTimes = getRequest.getTimes();

        // TODO do we really need this time range check? it's not worth much
        // njensen made it only work on non-D2D databases since
        // it was slowing down smart init
        if (!id.getDbId().getDbType().equals("D2D")) {
            List<TimeRange> trs = null;
            ServerResponse<List<TimeRange>> ssr = getGridInventory();
            trs = ssr.getPayload();
            sr.addMessages(ssr);
            if (!sr.isOkay()) {
                sr.addMessage("Cannot get grid data with the get inventory failure");
                return sr;
            }
            // Ensure that all requested time ranges are in the inventory
            if (!trs.containsAll(reqTimes)) {
                sr.addMessage("Some of the requested time ranges are not in the inventory."
                        + " Inv: "
                        + trs
                        + " requestTimes: "
                        + getRequest.getTimes());
                return sr;
            }
        }

        // Get the data
        if (getRequest.isConvertUnit() && (db instanceof D2DGridDatabase)) {
            sr = ((D2DGridDatabase) db).getGridData(id, reqTimes,
                    getRequest.isConvertUnit());
        } else {
            sr = db.getGridData(id, reqTimes);
        }
        if (!sr.isOkay()) {
            sr.addMessage("Failure in retrieving grid data from GridDatabase");
            return sr;
        }

        // Validate the data
        sr.addMessages(dataOkay(sr.getPayload(), badDataTimes));
        if (!sr.isOkay()) {
            sr.addMessage("Cannot get grid data - data is not valid");
        }

        return sr;
    }

    /**
     * Commands a time purge on this grid parameter. All grids prior to
     * "purgeTime" will be purged if all required conditions are met (e.g.,
     * locks, grids completely before the purgeTime). Changes made to the grids
     * are assembled as GridUpdateNotifications and LockNotifications and
     * returned.
     * 
     * Zeros out the notifications. Gets the grid inventory. If there are no
     * grids prior to purgeTime, then simply return. If there are grids, then
     * create a LockTableRequest for the requestor and call the LockManager's
     * getLockTables. This routine breaks any locks that are necessary to remove
     * and assembles lock notifications for those. The
     * GridDatabase::saveGridData is called with no grid slices for the purge
     * range to delete the grids. GridUpdateNotification is assembled for the
     * purged grids.
     * 
     * @param purgeTime
     *            The time to be purged
     * @param gridNotifications
     *            The grid update notifications
     * @param lockNotifications
     *            The lock notifications
     * @return The server response
     */
    public ServerResponse<?> timePurge(Date purgeTime,
            List<GridUpdateNotification> gridNotifications,
            List<LockNotification> lockNotifications, String siteID) {

        ServerResponse<?> sr = new ServerResponse<String>();
        lockNotifications.clear();
        gridNotifications.clear();

        // get current inventory
        List<TimeRange> trs = null;
        ServerResponse<List<TimeRange>> ssr = this.getGridInventory();
        sr.addMessages(ssr);
        if (!sr.isOkay()) {
            sr.addMessage("Cannot timePurge since the get inventory failed");
            return sr;
        }
        trs = ssr.getPayload();

        // Get the lock table
        WsId wsId = new WsId(null, "timePurge", "EDEX", 0);
        List<LockTable> lts = new ArrayList<LockTable>();

        LockTableRequest lockreq = new LockTableRequest(this.id);
        ServerResponse<List<LockTable>> ssr2 = LockManager.getInstance()
                .getLockTables(lockreq, wsId, siteID);
        sr.addMessages(ssr2);
        lts = ssr2.getPayload();
        if (!sr.isOkay() || lts.size() != 1) {
            sr.addMessage("Cannot timePurge since getting lock table failed");
        }

        List<TimeRange> breakList = new ArrayList<TimeRange>();
        List<TimeRange> noBreak = new ArrayList<TimeRange>();
        for (int i = 0; i < lts.get(0).getLocks().size(); i++) {
            if (lts.get(0).getLocks().get(i).getTimeRange().getEnd()
                    .before(purgeTime)
                    || lts.get(0).getLocks().get(i).getTimeRange().getEnd()
                            .equals(purgeTime)) {
                breakList.add(lts.get(0).getLocks().get(i).getTimeRange());

            } else {
                noBreak.add(lts.get(0).getLocks().get(i).getTimeRange());
            }
        }

        List<TimeRange> purge = new ArrayList<TimeRange>();
        for (int i = 0; i < trs.size(); i++) {
            if (trs.get(i).getEnd().before(purgeTime)
                    || trs.get(i).getEnd().equals(purgeTime)) {
                boolean found = false;
                for (int j = 0; j < noBreak.size(); j++) {
                    if (noBreak.get(j).contains(trs.get(i))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    purge.add(trs.get(i));
                }
            }
        }

        List<LockRequest> lreqs = new ArrayList<LockRequest>();
        List<LockTable> ltChanged = new ArrayList<LockTable>();
        for (int i = 0; i < breakList.size(); i++) {
            lreqs.add(new LockRequest(id, breakList.get(i), LockMode.BREAK_LOCK));
        }

        ServerResponse<List<LockTable>> lockResponse = LockManager
                .getInstance().requestLockChange(lreqs, wsId, siteID);
        sr.addMessages(lockResponse);
        if (!sr.isOkay()) {
            sr.addMessage("Cannot timePurge since the break lock failed");
        }
        ltChanged = lockResponse.getPayload();

        // assemble the LockChangeNotification
        for (int i = 0; i < ltChanged.size(); i++) {
            lockNotifications
                    .add(new LockNotification(ltChanged.get(i), siteID));
            // gridNotifications.add(new GridUpdateNotification(id, breakList
            // .get(i), Arrays.asList, ""));
        }

        GFEDao dao = null;
        try {
            dao = (GFEDao) PluginFactory.getInstance().getPluginDao("gfe");
        } catch (PluginException e) {
            sr.addMessage("Unable to get gfe dao");
        }
        dao.deleteRecords(id, purge);
        for (int i = 0; i < purge.size(); i++) {
            // assemble the GridUpdateNotification
            Map<TimeRange, List<GridDataHistory>> histories = new HashMap<TimeRange, List<GridDataHistory>>(
                    0);
            gridNotifications.add(new GridUpdateNotification(id, purge.get(i),
                    histories, wsId, siteID));
        }
        return sr;

    }

    /**
     * Routine to output statistical information about this instance
     */
    public void dumpStatistics() {
        // TODO: Do we need this method
    }

    @Override
    public String toString() {
        return "ParmID: " + id;
    }

    private ServerResponse<?> dataOkay(List<IGridSlice> gridSlices,
            List<TimeRange> badDataTimes) {

        ServerResponse<?> sr = new ServerResponse<String>();
        for (IGridSlice slice : gridSlices) {
            ServerResponse<?> sr1 = gridSliceOkay(slice);
            sr.addMessages(sr1);
            if (!sr1.isOkay()) {
                badDataTimes.add(slice.getValidTime());
            }
        }
        return sr;
    }

    /**
     * Checks the data to ensure that it is valid. If there is a bad data slice,
     * then place the valid time of that grid in the badDataTimes entry.
     * 
     * @param records
     * @param badDataTimes
     * @return
     */
    private ServerResponse<?> recordsOkay(List<GFERecord> records,
            List<TimeRange> badDataTimes) {
        ServerResponse<?> sr = new ServerResponse<String>();
        for (GFERecord rec : records) {
            IGridSlice slice = (IGridSlice) rec.getMessageData();
            ServerResponse<?> sr1 = gridSliceOkay(slice);
            sr.addMessages(sr1);
            if (!sr1.isOkay()) {
                badDataTimes.add(slice.getValidTime());
            }
        }
        return sr;
    }

    /**
     * Checks if the data slice is valid. Returns the status.
     * 
     * Calls the GridSlice::isValid(). Then calls checkAttributes() to ensure
     * that the database attributes are the same as the GridParmInfo in the data
     * slice.
     * 
     * @param ds
     *            The grid slice to check
     * @return The server response
     */
    private ServerResponse<?> gridSliceOkay(IGridSlice ds) {
        ServerResponse<?> sr = new ServerResponse<String>();
        String error = ds.isValid();
        if (error != null) {
            sr.addMessage("GridSlice is not valid: " + error);
            return sr;
        }

        sr.addMessages(checkAttributes(ds));
        return sr;
    }

    /**
     * Validates the times of the grid slices to ensure that they fall inside of
     * the replacement time range.
     * 
     * @param replacementTimeRange
     *            The replacement time range
     * @param timeRanges
     *            The time ranges of the data
     * @return The status
     */
    private ServerResponse<String> checkTimes(TimeRange replacementTimeRange,
            List<TimeRange> timeRanges) {

        ServerResponse<String> sr = new ServerResponse<String>();
        for (int i = 0; i < timeRanges.size(); i++) {
            if (!replacementTimeRange.contains(timeRanges.get(i))) {
                sr.addMessage("GridSlice #"
                        + i
                        + "'s valid time is not within the replacement TimeRange. ValidTime="
                        + timeRanges.get(i) + " ReplacementTR="
                        + replacementTimeRange);
                return sr;
            }
        }
        return sr;

    }

    /**
     * Checks the locks to ensure that the requestor has those times locked for
     * the specified time range.
     * 
     * Forms a LockTableRequest for the ParmID. Gets the LockTable for the
     * ParmID using the lock manager. Calls the Lock Tables's checkLock() to
     * ensure that it is LOCKED_BY_ME.
     * 
     * @param replacementTimeRange
     *            The time range to check
     * @param requestor
     *            The workstation ID of the requester
     * @return The server response
     */
    private ServerResponse<?> checkLocks(final TimeRange replacementTimeRange,
            final WsId requestor, String siteID) {

        ServerResponse<?> sr = new ServerResponse<LockTable>();

        // Get the lock table for this parameter
        LockTableRequest req = new LockTableRequest(id);
        List<LockTable> lockTables = new ArrayList<LockTable>();
        ServerResponse<List<LockTable>> ssr = LockManager.getInstance()
                .getLockTables(req, requestor, siteID);
        lockTables = ssr.getPayload();
        sr.addMessages(ssr);
        if (!sr.isOkay() || lockTables.size() != 1) {

            sr.addMessage("Cannot verify locks due to problem with Lock Manager");
            return sr;
        }

        // check to see if the requestor has the time period locked
        if (lockTables.get(0).checkLock(replacementTimeRange, requestor) != LockTable.LockStatus.LOCKED_BY_ME) {
            sr.addMessage("Lock Check failed since " + requestor
                    + " does not have the time period " + replacementTimeRange
                    + " locked. LockTable=" + lockTables.get(0));
        }

        return sr;
    }

    /**
     * Checks the GridParmInfo in the GridSlice and in the GridDatabase to
     * ensure that they are consistent with each other. Returns the status.
     * 
     * Ensures that the GridParmInfos are equal. Checks the time range of the
     * GridSlice and compares it to the TimeConstraints.
     * 
     * @param gridSlice
     *            The grid slice to check
     * @return The server response
     */
    private ServerResponse<?> checkAttributes(IGridSlice gridSlice) {
        ServerResponse<?> sr = new ServerResponse<String>();

        GridParmInfo dbGPI = getGridParmInfo().getPayload();

        // Compare the two GridParmInfos
        if (!gridSlice.getGridInfo().equals(dbGPI)) {
            sr.addMessage("GridParmInfo in GridSlice does not match the Database"
                    + " GridSlice="
                    + gridSlice.getGridInfo()
                    + " Database="
                    + dbGPI);
            return sr;

        }

        // Check the TimeConstraints
        final TimeConstraints tc = dbGPI.getTimeConstraints();

        if (!tc.validTR(gridSlice.getValidTime())) {
            sr.addMessage("ValidTime of GridSlice does not match TimeConstraint requirements. ValidTime="
                    + gridSlice.getValidTime() + " TimeConstraints=" + tc);
            return sr;
        }

        return sr;
    }
}
