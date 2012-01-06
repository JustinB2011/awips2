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
package com.raytheon.edex.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Class for managing the protected file list
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 25, 2010            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class ProtectedFiles {
    private static transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProtectedFiles.class);

    private static final String COMMENT = "#";

    private static final String PROTECTED_FILE = "protectedFiles.txt";

    private static final String BASE_HEADER = "# The layout of this file is a line that starts with a # is treated as a\n"
            + "# comment and any other line is treated as a protected base file in the \n"
            + "# format: localization_type/subpath/to/file\n"
            + "# A simple example would be: BASE:common_static/textdb/textCategoryClass.txt\n"
            + "# Any entries for SITE level will apply to all sites\n";

    private static final String LEVEL_HEADER = "# The layout of this file is a line that starts with a # is treated as a\n"
            + "# comment and any other line is treated as a protected base file in the \n"
            + "# format: localization_type/subpath/to/file\n"
            + "# A simple example would be: %s:cave_static/gfe/userPython/textProducts/AFD.py\n"
            + "# Any entries in this file will only apply for users localized to the site\n";

    private static ProtectedFiles base = new ProtectedFiles(PathManagerFactory
            .getPathManager().getContext(LocalizationType.EDEX_STATIC,
                    LocalizationLevel.BASE));

    private static Map<String, ProtectedFiles> sites = new HashMap<String, ProtectedFiles>();

    private Set<String> protectedFiles;

    private LocalizationLevel level;

    private File file;

    private long lastModifiedTime = 0;

    private ProtectedFiles(LocalizationContext context) {
        level = context.getLocalizationLevel();
        file = PathManagerFactory.getPathManager()
                .getLocalizationFile(context, PROTECTED_FILE).getFile();
        protectedFiles = Collections
                .synchronizedSortedSet(new TreeSet<String>());
        reloadFile();
    }

    /**
     * Add the list of protected files into protectedFiles.txt for the site with
     * siteId
     * 
     * @param siteId
     *            The site to add the protected files to
     * @param protectedFileList
     *            The new list of protected files
     */
    public static void protect(String siteId, List<String> protectedFileList) {
        ProtectedFiles site = getSiteLevelFiles(siteId);
        site.protectedFiles.addAll(protectedFileList);
        site.writeProtectedFile();
    }

    /**
     * Given the localized site, type and path to check, determine the level at
     * which the file is protected at
     * 
     * @param localizedSite
     * @param type
     * @param path
     * @return level at which protected at or null if not protected path
     */
    public static LocalizationLevel getProtectedLevel(String localizedSite,
            LocalizationType type, String path) {
        // Check base first
        if (base == null) {
            return null;
        }
        LocalizationLevel level = base.getProtectedLevelInternal(type, path);

        // If not protected in base file, check the site
        if (level == null && localizedSite != null) {
            ProtectedFiles files = getSiteLevelFiles(localizedSite);
            level = files.getProtectedLevelInternal(type, path);
        }

        return level;
    }

    /**
     * @param localizedSite
     * @return
     */
    private static ProtectedFiles getSiteLevelFiles(String siteId) {
        ProtectedFiles site = sites.get(siteId);
        if (site == null) {
            LocalizationContext siteContext = PathManagerFactory
                    .getPathManager().getContextForSite(
                            LocalizationType.EDEX_STATIC, siteId);
            site = new ProtectedFiles(siteContext);
            sites.put(siteId, site);
        }
        return site;
    }

    /**
     * Write the protectedFiles.txt file.
     */
    private synchronized void writeProtectedFile() {
        String str;
        try {
            file.createNewFile();

            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(level == LocalizationLevel.BASE ? BASE_HEADER : String
                    .format(LEVEL_HEADER, level.toString()));
            Iterator<String> iter = protectedFiles.iterator();
            while (iter.hasNext()) {
                str = iter.next();
                out.write(str + "\n");
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the protected file list into memory
     */
    private void reloadFile() {
        if (file.lastModified() > lastModifiedTime) {
            if (file.exists() == false) {
                writeProtectedFile();
            }
            protectedFiles.clear();
            lastModifiedTime = file.lastModified();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(COMMENT) && !line.trim().equals("")) {
                        line.replace("//", "/");

                        protectedFiles.add(line);
                    }
                }
            } catch (IOException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error reading protected file list", e);
            }
        }
    }

    /**
     * Gets the level of protection for this LocalizationType.
     * 
     * @param type
     *            The LocalizationType
     * @param subPath
     *            The subPath
     * @return The LocalizationLevel of protection or null if not protected
     */
    private synchronized LocalizationLevel getProtectedLevelInternal(
            LocalizationType type, String subPath) {
        LocalizationLevel protectionLevel = null;
        reloadFile();

        String path = type.toString().toLowerCase() + File.separator + subPath;
        if (path.contains("//")) {
            path.replace("//", "/");
        }

        LocalizationLevel[] levels = PathManagerFactory.getPathManager()
                .getAvailableLevels();

        for (LocalizationLevel level : levels) {
            String levelPath = level.toString().toUpperCase() + ":" + path;
            boolean isProtected = protectedFiles.contains(levelPath);

            if (isProtected) {
                protectionLevel = level;
                break;
            }
        }

        return protectionLevel;
    }

}
