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
package com.raytheon.uf.common.site;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2010            bfarmer     Initial creation
 * Apr 09, 2012 DR14765    mhuang      Map out correct CCCC site ID for backup
 *                                      sites.
 * 
 * </pre>
 * 
 * @author bfarmer
 * @version 1.0
 */

public class SiteMap {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SiteMap.class);

    private static SiteMap instance = new SiteMap();

    private static final String AFOS_LOOKUP_FILENAME = "textdb/afos_lookup_table.dat";

    private static final String NATIONAL_CATEGORY_TABLE_FILENAME = "textdb/national_category_table.template";

    private static final String SITE_OVERRIDE_FILENAME = "site3LetterTo4LetterOverride.dat";

    private static final String RFC_TABLE_FILENAME = "textdb/rfc_lookup_table.dat";

    private List<String> rfcList = new ArrayList<String>();

    private Map<String, String> siteToSiteMap = new HashMap<String, String>();

    private Map<String, String> nationalCategoryMap = new HashMap<String, String>();

    private Map<String, String> siteTo4LetterSite = new HashMap<String, String>();

    private Map<String, Set<String>> siteTo3LetterSite = new HashMap<String, Set<String>>();

    public static SiteMap getInstance() {
        return instance;
    }

    private SiteMap() {
        readFiles();
    }

    public String getCCCFromXXXCode(String xxx) {
        String retval = null;
        if (xxx != null) {
            if (xxx.length() == 3) {
                retval = siteToSiteMap.get(getSite4LetterId(xxx));
                if (retval == null) {
                    retval = nationalCategoryMap.get(getSite4LetterId(xxx));
                }
            } else if (xxx.length() == 4) {
                retval = siteToSiteMap.get(xxx);
                if (retval == null) {
                    retval = nationalCategoryMap.get(xxx);
                }
            }
        }

        return retval;
    }

    /**
     * Attempt to map an xxxid to a cccid. Use the afos_lookup_table.dat data only.
     * @param xxx An id to map.
     * @return
     */
    public String getAFOSTableMap(String xxx) {
        return siteToSiteMap.get(xxx);
    }
    
    public synchronized String mapICAOToCCC(String icao) {
        return nationalCategoryMap.get(icao);
    }

    public void readFiles() {
        siteToSiteMap.clear();
        nationalCategoryMap.clear();
        siteTo4LetterSite.clear();
        siteTo3LetterSite.clear();

        // load base afos lookup
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext lc = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        File file = pathMgr.getFile(lc, AFOS_LOOKUP_FILENAME);
        loadAfosLookupFile(file, siteToSiteMap);

        file = pathMgr.getFile(lc, RFC_TABLE_FILENAME);
        loadRFCLookupFile(file, rfcList);

        // load site afos lookup
        lc = pathMgr.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);
        file = pathMgr.getFile(lc, AFOS_LOOKUP_FILENAME);
        loadAfosLookupFile(file, siteToSiteMap);
        
        // load national category
        lc = pathMgr.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);
        file = pathMgr.getFile(lc, NATIONAL_CATEGORY_TABLE_FILENAME);
        loadNationalCategoryFile(file, nationalCategoryMap);

        // post-process the nationalCategoryMap to generate the 3 to 4 letter
        // mapping
        for (String icao : nationalCategoryMap.keySet()) {
            if (icao.trim().length() == 4) {
                String threeId = icao.substring(1);
                String prefixCode = icao.substring(0, 1);
                String foundId = siteTo4LetterSite.get(threeId);
                // US contiguous prefix code "K" takes precedence
                if (foundId == null || prefixCode.equals("k")) {
                    siteTo4LetterSite.put(threeId, icao);
                }
                Set<String> reverse = siteTo3LetterSite.get(icao);
                if (reverse == null) {
                    reverse = new TreeSet<String>();
                    siteTo3LetterSite.put(icao, reverse);
                }
                reverse.add(icao.substring(1));
            }
        }

        // load site 3 letter to 4 letter override
        lc = pathMgr.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);
        file = pathMgr.getFile(lc, SITE_OVERRIDE_FILENAME);
        loadSite3LetterTo4LetterOverrideFile(file, siteTo4LetterSite,
                siteTo3LetterSite);
    }

    private void loadAfosLookupFile(File file, Map<String, String> aliasMap) {
        if ((file != null) && file.exists()) {
            try {
                BufferedReader fis = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                String line = null;
                try {
                    while ((line = fis.readLine()) != null) {
                        String dataKey = line.substring(0, 4);
                        String tblData = line.substring(5);
                        aliasMap.put(dataKey, tblData);
                    }
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Could not read AFOS Lookup File "
                                    + AFOS_LOOKUP_FILENAME, e);

                }
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Failed to find AFOS Lookup File "
                                + AFOS_LOOKUP_FILENAME, e);

            }
        }
    }

    private void loadNationalCategoryFile(File file,
            Map<String, String> aliasMap) {
        if ((file != null) && file.exists()) {
            try {
                BufferedReader fis = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                String line = null;
                try {
                    while ((line = fis.readLine()) != null) {
                        if (line.length() == 9) {
                            String dataKey = line.substring(0, 4);
                            String tblData = line.substring(6);
                            aliasMap.put(dataKey, tblData);
                        }
                    }
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Could not read National Category Table "
                                    + NATIONAL_CATEGORY_TABLE_FILENAME, e);

                }
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Could not find National Category Table "
                                + NATIONAL_CATEGORY_TABLE_FILENAME, e);

            }
        }
    }

    private void loadSite3LetterTo4LetterOverrideFile(File file,
            Map<String, String> site3To4LetterMap,
            Map<String, Set<String>> site4To3LetterMap) {
        if ((file != null) && file.exists()) {
            try {
                BufferedReader fis = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                String line = null;
                try {
                    while ((line = fis.readLine()) != null) {
                        line = line.trim();
                        if (line.length() >= 8 && !line.startsWith("#")) {
                            String site3 = line.substring(0, 3);
                            String site4 = line.substring(4);

                            // If this overrides an existing entry,
                            // remove the site3 from the reverse lookup map
                            String old4letter = site3To4LetterMap.get(site3);
                            if (old4letter != null) {
                                Set<String> values3 = site4To3LetterMap
                                        .get(old4letter);
                                values3.remove(site3);
                                if (values3.isEmpty()) {
                                    site4To3LetterMap.remove(old4letter);
                                }
                            }

                            site3To4LetterMap.put(site3, site4);

                            // Add the entry to the reverse lookup map
                            Set<String> site3s = site4To3LetterMap.get(site4);
                            if (site3s == null) {
                                site3s = new TreeSet<String>();
                                site4To3LetterMap.put(site4, site3s);
                            }
                            site3s.add(site3);
                        }
                    }
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Could not read Site 3 Letter To 4 Letter Override "
                                    + SITE_OVERRIDE_FILENAME, e);

                }
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Could not find Site 3 Letter To 4 Letter Override "
                                + SITE_OVERRIDE_FILENAME, e);

            }
        }
    }

    private void loadRFCLookupFile(File file, List<String> aliasList) {
        if ((file != null) && file.exists()) {
            try {
                BufferedReader fis = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                String line = null;
                try {
                    while ((line = fis.readLine()) != null) {
                        aliasList.add(line.trim());
                    }
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Could not read RFC Lookup File "
                                    + RFC_TABLE_FILENAME, e);

                }
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Failed to find RFC Lookup File " + RFC_TABLE_FILENAME,
                        e);

            }
        }
    }

    /**
     * Converts a 3 letter site ID into a 4 letter ID, e.g. OAX to KOAX
     * 
     * @param site3LetterId
     *            the 3 letter site id
     * @return
     */
    public String getSite4LetterId(String site3LetterId) {
        String site = siteTo4LetterSite.get(site3LetterId);

        // if site not found default to K
        if (site == null) {
            site = "K" + site3LetterId;
        } else { 
        	// DR_14765, in case the site hashed out from combined mapping
        	// table from both national_category_table and afo_lookup_table 
        	// does not start with K but not from site3LetterTo4LetterOerride.dat
        	// which are starting with P or T
        	char[] siteChar = site.toCharArray();
        	if (siteChar[0] != 'K') {
        		if (!((siteChar[0] == 'P' && (siteChar[1] == 'A' || siteChar[1] == 'G'
        			|| siteChar[1] == 'H')) ||
        				(siteChar[0] == 'T' && siteChar[1] == 'S'))) {
            		site = "K" + site3LetterId;       			
        		}
        	}
        }

        return site;
    }

    /**
     * Convert a 4 letter site ID into the 3 letter site IDs that convert to it,
     * e.g. KOAX to OAX. Some 3 letter sites convert to the same 4 letter site,
     * so this reverse lookup has to return a collection.
     * 
     * @param site4LetterId
     * @return the 3 letter sites that map to the 4 letter site
     */
    public Set<String> getSite3LetterIds(String site4LetterId) {
        Set<String> site3LetterIds = siteTo3LetterSite.get(site4LetterId);
        if (site3LetterIds == null) {
            site3LetterIds = new TreeSet<String>();
            if (site4LetterId == null) {
                ; // return empty set
            } else if (site4LetterId.length() <= 3) {
                site3LetterIds.add(site4LetterId);
            } else {
                site3LetterIds.add(site4LetterId.substring(1));
            }
        }
        return site3LetterIds;
    }

    public boolean isRFCSite(String site) {
        return rfcList.contains(site);
    }
}
