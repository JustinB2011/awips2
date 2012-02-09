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
package com.raytheon.uf.viz.core.level;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataplugin.level.MasterLevel;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizCommunicationException;

/**
 * Factory for getting level mappings
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11/16/2009    #3120     rjpeter     Initial version
 * 11/21/2009    #3576     rjpeter     Added group capability
 * 
 * &#064;author rjpeter
 * @version 1.0
 */
public class LevelMappingFactory {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(LevelMappingFactory.class);

    private static LevelMappingFactory instance = null;

    private Map<String, LevelMapping> keyToLevelMappings = new HashMap<String, LevelMapping>();

    private boolean levelToLevelMappingsInitialized = false;

    private Map<Level, LevelMapping> levelToLevelMappings = new HashMap<Level, LevelMapping>();

    private boolean groupToMasterLevelsInitialized = false;

    private Map<String, Map<MasterLevel, Set<Level>>> groupToMasterLevels = new HashMap<String, Map<MasterLevel, Set<Level>>>();

    public synchronized static LevelMappingFactory getInstance() {
        if (instance == null) {
            instance = new LevelMappingFactory();
        }
        return instance;
    }

    private LevelMappingFactory() {
        File path = PathManagerFactory.getPathManager().getStaticFile(
                "volumebrowser/LevelMappingFile.xml");
        LevelMappingFile levelMapFile = null;
        long start = System.currentTimeMillis();
        try {
            levelMapFile = JAXB.unmarshal(path, LevelMappingFile.class);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "An error was encountered while creating the LevelNameMappingFile from "
                            + path.toString(), e);
        }

        List<LevelMapping> levelMappings = levelMapFile.getLevelMappingFile();

        if (levelMappings != null && levelMappings.size() > 0) {
            for (LevelMapping mapping : levelMappings) {
                if (keyToLevelMappings.containsKey(mapping.getKey())) {
                    // handle multiple entries to same key by appending levels
                    LevelMapping priorEntry = keyToLevelMappings.get(mapping
                            .getKey());
                    priorEntry.getDatabaseLevels().addAll(
                            mapping.getDatabaseLevels());
                } else {
                    keyToLevelMappings.put(mapping.getKey(), mapping);
                }
            }

        }
        long finish = System.currentTimeMillis();
        System.out.println("LevelMappingFactory initialization took ["
                + (finish - start) + "] ms");
    }

    public LevelMapping getLevelMappingForKey(String key) {
        return keyToLevelMappings.get(key);
    }

    public LevelMapping getLevelMappingForLevel(Level level)
            throws VizCommunicationException {
        if (!levelToLevelMappingsInitialized) {
            initializeLevelToLevelMappings();
        }
        return levelToLevelMappings.get(level);
    }

    public Collection<LevelMapping> getAllLevelMappings() {
        return keyToLevelMappings.values();
    }

    public Set<Level> getAllLevels() throws VizCommunicationException {
        if (!levelToLevelMappingsInitialized) {
            initializeLevelToLevelMappings();
        }
        return levelToLevelMappings.keySet();
    }

    public Map<MasterLevel, Set<Level>> getLevelMapForGroup(String group)
            throws VizCommunicationException {
        if (!groupToMasterLevelsInitialized) {
            initializeGroupToMasterLevels();
        }
        return groupToMasterLevels.get(group);
    }

    private void initializeLevelToLevelMappings()
            throws VizCommunicationException {
        for (LevelMapping mapping : keyToLevelMappings.values()) {
            String group = mapping.getGroup();

            for (Level l : mapping.getLevels()) {
                if (levelToLevelMappings.containsKey(l)) {
                    LevelMapping oldMapping = levelToLevelMappings.get(l);
                    // Only replace the old level mapping if we have less
                    // levels than the old mapping
                    // This should cause the most specific mapping to be
                    // used
                    if (mapping.getLevels().size() < oldMapping.getLevels()
                            .size()) {
                        levelToLevelMappings.put(l, mapping);
                    }
                } else {
                    levelToLevelMappings.put(l, mapping);
                }
            }
        }
        levelToLevelMappingsInitialized = true;
    }

    private void initializeGroupToMasterLevels()
            throws VizCommunicationException {
        for (LevelMapping mapping : keyToLevelMappings.values()) {
            String group = mapping.getGroup();
            Map<MasterLevel, Set<Level>> masterLevels = null;

            if (group != null) {
                masterLevels = groupToMasterLevels.get(mapping.getGroup());
                if (masterLevels == null) {
                    masterLevels = new HashMap<MasterLevel, Set<Level>>();
                    groupToMasterLevels.put(group, masterLevels);
                }
            }

            for (Level l : mapping.getLevels()) {

                // populate grouping map
                if (masterLevels != null) {
                    MasterLevel ml = l.getMasterLevel();
                    Set<Level> levels = masterLevels.get(ml);

                    if (levels == null) {
                        levels = new HashSet<Level>();
                        masterLevels.put(ml, levels);
                    }

                    levels.add(l);
                }
            }
        }
        groupToMasterLevelsInitialized = true;
    }
}
