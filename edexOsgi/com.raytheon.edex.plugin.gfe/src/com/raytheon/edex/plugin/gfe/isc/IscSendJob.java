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

package com.raytheon.edex.plugin.gfe.isc;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jep.JepException;

import com.raytheon.edex.plugin.gfe.config.GFESiteActivation;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfig;
import com.raytheon.edex.plugin.gfe.config.IFPServerConfigManager;
import com.raytheon.edex.plugin.gfe.db.dao.GFEDao;
import com.raytheon.edex.plugin.gfe.exception.GfeConfigurationException;
import com.raytheon.edex.plugin.gfe.server.GridParmManager;
import com.raytheon.edex.plugin.gfe.util.SendNotifications;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.server.message.ServerResponse;
import com.raytheon.uf.common.dataplugin.gfe.server.notify.GridUpdateNotification;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.database.plugin.PluginFactory;

/**
 * Class to for running the isc scripts
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 07/06/09      1995       bphillip    Initial release
 * 04/06/12      #457       dgilling    Move call to delete records
 *                                      from queue into run().
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class IscSendJob implements Runnable {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(IscSendJob.class);

    /** Date format for formatting dates for use with iscExtract script */
    private static final SimpleDateFormat ISC_EXTRACT_DATE = new SimpleDateFormat(
            "yyyyMMdd_HHmm");

    private Map<String, IscSendScript> scripts;

    private int runningTimeOutMillis;

    private int threadSleepInterval;

    private int initialDelay;

    /**
     * Constructs a new IscSendJob
     * 
     * @param siteID
     *            the site ID
     * 
     * @param cmdQueue
     */
    public IscSendJob() {
        scripts = new HashMap<String, IscSendScript>();
        runningTimeOutMillis = 300000;
        threadSleepInterval = 30000;
        initialDelay = 120000;
    }

    @Override
    public void run() {
        long curTime = System.currentTimeMillis();
        while ((!EDEXUtil.isRunning())
                || ((System.currentTimeMillis() - curTime) < initialDelay)) {
            try {
                Thread.sleep(threadSleepInterval);
            } catch (Throwable t) {
                // ignore
            }
        }

        try {
            // run forever
            while (true) {
                IscSendRecord record = SendIscTransactions
                        .getNextSendJob(runningTimeOutMillis);
                if (record != null) {
                    runIscSend(record);
                    SendIscTransactions.removeSendJob(record);
                } else {
                    try {
                        Thread.sleep(threadSleepInterval);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        } finally {
            // Make sure OS resources are released at thread death
            for (IscSendScript script : scripts.values()) {
                script.dispose();
            }
        }
    }

    private void runIscSend(IscSendRecord request) {
        try {
            ParmID id = request.getParmID();
            TimeRange tr = request.getTimeRange();
            String xmlDest = request.getXmlDest();
            String siteId = id.getDbId().getSiteId();

            if (!GFESiteActivation.getInstance().getActiveSites()
                    .contains(siteId)) {
                statusHandler.warn("Attempted to send " + id
                        + " for deactivated site " + siteId + ".");
                return;
            }

            statusHandler.info("Starting isc for " + id.toString() + " "
                    + tr.toString() + " " + xmlDest);

            Map<String, Object> cmd = new HashMap<String, Object>();
            cmd.put("parmNames", Arrays.asList(id.getParmName()));
            cmd.put("databaseName", id.getDbId().getModelId());
            cmd.put("startTime", ISC_EXTRACT_DATE.format(tr.getStart()));
            cmd.put("endTime", ISC_EXTRACT_DATE.format(tr.getEnd()));

            // destination server XML, might be empty
            // the -D switch is a file location containing the xml
            // information
            if (!xmlDest.isEmpty()) {
                cmd.put("destinations", xmlDest);
            }

            try {
                IFPServerConfig config = IFPServerConfigManager
                        .getServerConfig(siteId);
                // IRT table address
                cmd.put("irtTableAddressA", config.iscRoutingTableAddress()
                        .get("ANCF"));
                cmd.put("irtTableAddressB", config.iscRoutingTableAddress()
                        .get("BNCF"));
                // xmt script
                cmd.put("transmitScript", config.transmitScript());
                // our server host, port, protocol, our mhs id, and our site id
                cmd.put("ourServerHost", config.getServerHost());
                cmd.put("ourServerPort", String.valueOf(config.getRpcPort()));
                cmd.put("ourServerProtocol",
                        String.valueOf(config.getProtocolVersion()));
                cmd.put("ourMHSid", config.getMhsid());
                cmd.put("ourSiteID", config.getSiteID().get(0));
            } catch (GfeConfigurationException e) {
                statusHandler.error(
                        "Unable to retrieve site configuration for site "
                                + siteId, e);
                return;
            }

            try {
                IscSendScript script = scripts.get(siteId);
                if (script == null) {
                    script = IscSendScriptFactory
                            .constructIscSendScript(siteId);
                    scripts.put(siteId, script);
                }
                script.execute(cmd);
            } catch (JepException e) {
                statusHandler.error("Error executing iscExtract.", e);
                return;
            }

            try {
                GFEDao dao = (GFEDao) PluginFactory.getInstance().getPluginDao(
                        "gfe");

                ServerResponse<List<TimeRange>> sr = GridParmManager
                        .getGridInventory(id);
                if (!sr.isOkay()) {
                    statusHandler.error("Error getting inventory for " + id);
                    return;
                }

                WsId wsId = new WsId(InetAddress.getLocalHost(), "ISC", "ISC");
                List<TimeRange> inventory = sr.getPayload();
                List<TimeRange> overlapTimes = new ArrayList<TimeRange>();
                for (TimeRange range : inventory) {
                    if (tr.contains(range)) {
                        overlapTimes.add(range);
                    }
                }

                List<GridUpdateNotification> notifications = new ArrayList<GridUpdateNotification>();
                List<GFERecord> records = dao.getRecords(id, overlapTimes);
                for (GFERecord record : records) {
                    List<GridDataHistory> history = record.getGridHistory();
                    Map<TimeRange, List<GridDataHistory>> historyMap = new HashMap<TimeRange, List<GridDataHistory>>();
                    Date now = new Date();
                    for (GridDataHistory hist : history) {
                        hist.setLastSentTime(now);
                    }
                    historyMap.put(record.getTimeRange(), history);
                    dao.saveOrUpdate(record);
                    notifications.add(new GridUpdateNotification(id, record
                            .getTimeRange(), historyMap, wsId, siteId));
                }

                SendNotifications.send(notifications);

            } catch (PluginException e) {
                statusHandler.error("Error creating GFE dao!", e);
            } catch (Exception e) {
                statusHandler.error(
                        "Error updating last sent times in GFERecords.", e);
            }

        } catch (Throwable t) {
            statusHandler.error("Exception in ISCSendJob: ", t);
        }
    }

    public int getRunningTimeOutMillis() {
        return runningTimeOutMillis;
    }

    public void setRunningTimeOutMillis(int runningTimeOutMillis) {
        this.runningTimeOutMillis = runningTimeOutMillis;
    }

    public int getThreadSleepInterval() {
        return threadSleepInterval;
    }

    public void setThreadSleepInterval(int threadSleepInterval) {
        this.threadSleepInterval = threadSleepInterval;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }
}