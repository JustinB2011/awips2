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
package com.raytheon.uf.edex.archive.purge;

import java.util.Collection;

import com.raytheon.uf.common.archive.config.ArchiveConfig;
import com.raytheon.uf.common.archive.config.ArchiveConfigManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Purge task to purge archived data based on configured expiration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May  6, 2013 1965       bgonzale    Initial creation
 *                                     Added info logging for purge counts.
 * Aug 28, 2013 2299       rferrel     manager.purgeExpiredFromArchive now returns
 *                                      number of files purged.
 * 
 * </pre>
 * 
 * @author bgonzale
 * @version 1.0
 */

public class ArchivePurger {
    private final static IUFStatusHandler statusHandler = UFStatus
            .getHandler(ArchiveConfigManager.class);

    /**
     * Purge expired elements from the archives.
     */
    public static void purge() {
        ArchiveConfigManager manager = ArchiveConfigManager.getInstance();
        Collection<ArchiveConfig> archives = manager.getArchives();
        for (ArchiveConfig archive : archives) {
            int purgeCount = manager.purgeExpiredFromArchive(archive);
            if (statusHandler.isPriorityEnabled(Priority.INFO)) {
                StringBuilder sb = new StringBuilder(archive.getName());
                sb.append("::Archive Purged ");
                sb.append(purgeCount);
                sb.append(" file");
                if (purgeCount != 1) {
                    sb.append("s");
                }
                sb.append(".");
                statusHandler.info(sb.toString());
            }
        }
    }
}
