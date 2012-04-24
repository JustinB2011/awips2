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
package com.raytheon.uf.edex.maintenance.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.persist.DefaultPathProvider;
import com.raytheon.uf.common.dataplugin.persist.IHDFFilePathProvider;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.plugin.PluginDao;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 20, 2012            dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DefaultPluginArchiveFileNameFormatter implements
        IPluginArchiveFileNameFormatter {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.maintenance.archive.IPluginArchiveFileNameFormatter
     * #getPdosByFile(java.lang.String,
     * com.raytheon.uf.edex.database.plugin.PluginDao, java.util.Map,
     * java.util.Calendar, java.util.Calendar)
     */
    @Override
    public Map<String, List<PersistableDataObject>> getPdosByFile(
            String pluginName, PluginDao dao,
            Map<String, List<PersistableDataObject>> pdoMap,
            Calendar startTime, Calendar endTime)
            throws DataAccessLayerException {
        List<PersistableDataObject> pdos = dao.getRecordsToArchive(startTime,
                endTime);

        Set<String> newFileEntries = new HashSet<String>();
        if (pdos != null && !pdos.isEmpty()) {
            if (pdos.get(0) instanceof IPersistable) {
                IHDFFilePathProvider pathProvider = dao.pathProvider;

                for (PersistableDataObject pdo : pdos) {
                    IPersistable persistable = (IPersistable) pdo;
                    String path = pathProvider.getHDFPath(pluginName,
                            persistable)
                            + File.separator
                            + pathProvider.getHDFFileName(pluginName,
                                    persistable);
                    newFileEntries.add(path);
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

                        synchronized (DefaultPathProvider.fileNameFormat) {
                            timeString = DefaultPathProvider.fileNameFormat
                                    .format(time);
                        }
                    } else {
                        // no refTime to use bounded insert query bounds
                        Date time = startTime.getTime();

                        synchronized (DefaultPathProvider.fileNameFormat) {
                            timeString = DefaultPathProvider.fileNameFormat
                                    .format(time);
                        }
                    }

                    String path = pluginName + timeString;
                    newFileEntries.add(path);
                    List<PersistableDataObject> list = pdoMap.get(path);
                    if (list == null) {
                        list = new ArrayList<PersistableDataObject>(pdos.size());
                        pdoMap.put(path, list);
                    }
                    list.add(pdo);
                }

            }
        }

        Iterator<String> iter = pdoMap.keySet().iterator();
        Map<String, List<PersistableDataObject>> pdosToSave = new HashMap<String, List<PersistableDataObject>>(
                pdoMap.size() - newFileEntries.size());

        while (iter.hasNext()) {
            String key = iter.next();
            if (!newFileEntries.contains(key)) {
                pdosToSave.put(key, pdoMap.get(key));
                iter.remove();
            }
        }

        return pdosToSave;
    }

}
