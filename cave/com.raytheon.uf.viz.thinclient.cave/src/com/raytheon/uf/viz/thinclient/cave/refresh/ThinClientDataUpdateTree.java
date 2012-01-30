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
package com.raytheon.uf.viz.thinclient.cave.refresh;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.RecordFactory;
import com.raytheon.uf.viz.core.alerts.AlertMessage;
import com.raytheon.uf.viz.core.catalog.LayerProperty;
import com.raytheon.uf.viz.core.datastructure.DataCubeContainer;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.updater.DataUpdateTree;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2011            bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ThinClientDataUpdateTree extends DataUpdateTree {
    private IUFStatusHandler statusHandler = UFStatus
            .getHandler(ThinClientDataUpdateTree.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");

    private long lastQuery = 0l;

    public static synchronized ThinClientDataUpdateTree getInstance() {
        DataUpdateTree instance = DataUpdateTree.getInstance();
        if (!(instance instanceof ThinClientDataUpdateTree)) {
            instance = new ThinClientDataUpdateTree();
            setCustomInstance(instance);
        }
        return (ThinClientDataUpdateTree) instance;
    }

    private ThinClientDataUpdateTree() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        lastQuery = System.currentTimeMillis();
    }

    public Collection<AlertMessage> updateAllData() {
        String time = DATE_FORMAT.format(new Date(lastQuery));
        // put in a 1 second overlap in case insert time is a bit off.
        lastQuery = System.currentTimeMillis() - 1000;
        Set<AlertMessage> messages = new HashSet<AlertMessage>();
        for (DataPair pair : getDataPairs()) {
            AbstractResourceData resourceData = pair.data.getResourceData();
            if (!(resourceData instanceof AbstractRequestableResourceData)
                    || resourceData.isFrozen())
                continue;
            Map<String, RequestConstraint> metadata = pair.metadata;
            metadata = new HashMap<String, RequestConstraint>(metadata);
            metadata.put("insertTime", new RequestConstraint(time,
                    ConstraintType.GREATER_THAN));
            LayerProperty property = new LayerProperty();
            try {
                property.setEntryQueryParameters(metadata, false);
                List<Object> records = DataCubeContainer.getData(property,
                        60000);
                if (records != null && !records.isEmpty()) {
                    for (Object record : records) {
                        if (record instanceof PluginDataObject) {
                            PluginDataObject pdo = (PluginDataObject) record;
                            AlertMessage am = new AlertMessage();
                            am.dataURI = pdo.getDataURI();
                            am.decodedAlert = RecordFactory.getInstance()
                                    .loadMapFromUri(am.dataURI);
                            messages.add(am);
                        }
                    }
                }
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
        return messages;
    }

}
