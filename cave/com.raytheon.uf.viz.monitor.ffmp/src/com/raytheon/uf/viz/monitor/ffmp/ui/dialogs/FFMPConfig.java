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
package com.raytheon.uf.viz.monitor.ffmp.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.dataplugin.ffmp.FFMPRecord.FIELDS;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.monitor.config.FFMPRunConfigurationManager;
import com.raytheon.uf.common.monitor.config.FFMPSourceConfigurationManager;
import com.raytheon.uf.common.monitor.xml.FFMPRunXML;
import com.raytheon.uf.common.monitor.xml.ProductRunXML;
import com.raytheon.uf.common.monitor.xml.ProductXML;
import com.raytheon.uf.viz.monitor.ffmp.FFMPMonitor;
import com.raytheon.uf.viz.monitor.ffmp.ui.dialogs.FfmpTableConfigData.COLUMN_NAME;
import com.raytheon.uf.viz.monitor.ffmp.xml.FFMPConfigBasinXML;
import com.raytheon.uf.viz.monitor.ffmp.xml.FFMPTableColumnXML;

public class FFMPConfig {
    private static FFMPConfig classInstance = new FFMPConfig();

    public static enum TableCellColor {
        Upper, Mid, Lower, BelowLower, Default, ForcedFFG, VGB
    };

    public static enum ThreshColNames {
        RATE(1), QPE(2), QPF(3), GUID(4), RATIO(5), DIFF(6);

        private int colIndex;

        ThreshColNames(int idx) {
            colIndex = idx;
        }

        public int getColIndex() {
            return colIndex;
        }
    }

    private FFMPConfigBasinXML ffmpCfgBasin;

    private final Color upperColor = new Color(Display.getDefault(), 255, 152,
            116);

    private final Color midColor = new Color(Display.getDefault(), 251, 255,
            121);

    private final Color lowerColor = new Color(Display.getDefault(), 70, 255,
            113);

    private final Color belowLowerColor = new Color(Display.getDefault(), 215,
            215, 215);

    private final Color defaultColor = new Color(Display.getDefault(), 167,
            167, 167);

    private final Color forcedFFGColor = new Color(Display.getDefault(), 255,
            165, 0);

    private final Color vgbColor = new Color(Display.getDefault(), 196, 137,
            250);

    private final String defaultConfigXml = "DefaultFFMPconfig_basin.xml";

    /**
     * Used for looking up data - NOT for editing.
     */
    private HashMap<ThreshColNames, ThresholdManager> threshMgrMap;

    private HashMap<String, ThreshColNames> thresholdLookup;
    
    private AttributesDlgData attrData = null;
    
    private boolean reReadAttrData = false;

    private FFMPConfig() {
        init();
    }

    public static synchronized FFMPConfig getInstance() {
        if (classInstance == null) {
            classInstance = new FFMPConfig();
        }

        return classInstance;
    }

    public void disposeResources() {
        upperColor.dispose();
        midColor.dispose();
        lowerColor.dispose();
        belowLowerColor.dispose();
        defaultColor.dispose();
        forcedFFGColor.dispose();
        vgbColor.dispose();
    }

    public static void unloadConfig() {
        classInstance = null;
    }

    private void init() {
        threshMgrMap = new HashMap<ThreshColNames, ThresholdManager>();

        thresholdLookup = new HashMap<String, ThreshColNames>();

        for (ThreshColNames threshColName : ThreshColNames.values()) {
            thresholdLookup.put(threshColName.name(), threshColName);
        }

        readDefaultFFMPConfigBasin(defaultConfigXml);
    }

    public boolean isThreshold(String colName) {
        return thresholdLookup.containsKey(colName);
    }

    public ThreshColNames getThreshold(String colName) {
        return thresholdLookup.get(colName);
    }

    public Color getCellColor(TableCellColor tblCellColor) {
        switch (tblCellColor) {
        case Upper:
            return upperColor;
        case Mid:
            return midColor;
        case Lower:
            return lowerColor;
        case BelowLower:
            return belowLowerColor;
        case Default:
            return defaultColor;
        case ForcedFFG:
            return forcedFFGColor;
        case VGB:
            return vgbColor;
        }

        return defaultColor;
    }

    public FFMPConfigBasinXML getFFMPConfigData() {
        return ffmpCfgBasin;
    }

    public void loadNewConfig(LocalizationFile newConfigFile) {
        readNewFFMPConfigBasin(newConfigFile);
    }

    public void loadDefaultConfig() {
        readDefaultFFMPConfigBasin(defaultConfigXml);
    }

    private void readNewFFMPConfigBasin(LocalizationFile xmlFileName) {
        ffmpCfgBasin = null;

        try {
            ffmpCfgBasin = JAXB.unmarshal(xmlFileName.getFile(),
                    FFMPConfigBasinXML.class);

            createThresholdManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readDefaultFFMPConfigBasin(String xmlFileName) {
        ffmpCfgBasin = null;
        String fs = String.valueOf(File.separatorChar);

        try {
            IPathManager pm = PathManagerFactory.getPathManager();

            Map<LocalizationLevel, LocalizationFile> fileMap = pm
                    .getTieredLocalizationFile(LocalizationType.CAVE_STATIC,
                            "ffmp" + fs + "guiConfig" + fs + xmlFileName);

            String path = null;
            if (fileMap.get(LocalizationLevel.USER) != null) {
                path = fileMap.get(LocalizationLevel.USER).getFile(true)
                        .getAbsolutePath();
            } else if (fileMap.get(LocalizationLevel.SITE) != null) {
                path = fileMap.get(LocalizationLevel.SITE).getFile(true)
                        .getAbsolutePath();
            } else {
                path = fileMap.get(LocalizationLevel.BASE).getFile(true)
                        .getAbsolutePath();
            }

            System.out.println("Path Config FFMP: " + path);

            ffmpCfgBasin = JAXB.unmarshal(new File(path),
                    FFMPConfigBasinXML.class);

            createThresholdManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFFMPBasinConfig(LocalizationFile xmlFileName) {
        try {
            JAXB.marshal(ffmpCfgBasin, xmlFileName.getFile());

            xmlFileName.save();

            createThresholdManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FFMPTableColumnXML getTableColumnData(ThreshColNames colName) {
        ArrayList<FFMPTableColumnXML> ffmpTableCols = ffmpCfgBasin
                .getTableColumnData();

        for (FFMPTableColumnXML tableColData : ffmpTableCols) {
            if (tableColData.getColumnName().compareTo(colName.name()) == 0) {
                return tableColData;
            }
        }

        return null;
    }

    /**
     * Get the guidance column data. Must have a separate method for this column
     * since it does not have thresholds.
     * 
     * @return the guidance FFMPTableColumnXML object
     */
    public FFMPTableColumnXML getGuidColumnData() {
        ArrayList<FFMPTableColumnXML> ffmpTableCols = ffmpCfgBasin
                .getTableColumnData();

        for (FFMPTableColumnXML tableColData : ffmpTableCols) {
            if (tableColData.getColumnName().compareTo("") == 0) {
                return tableColData;
            }
        }

        return null;
    }

    public void updateThresholdValues(ThreshColNames colName, Double upperVal,
            Double midVal, Double lowerVal, Double filterVal) {
        FFMPTableColumnXML tcXML = getTableColumnData(colName);
        tcXML.setUpper(upperVal);
        tcXML.setMid(midVal);
        tcXML.setLow(lowerVal);
        tcXML.setFilter(filterVal);
        createThresholdManager();
    }

    public String getBasinTrendPlotColorName(String itemName) {
        ArrayList<FFMPTableColumnXML> tcXmlArray = ffmpCfgBasin
                .getTableColumnData();

        for (FFMPTableColumnXML data : tcXmlArray) {
            if (itemName.compareTo(data.getColumnName().trim()) == 0) {
                return data.getBasinTrendPlotColor();
            }
        }

        return "grey";
    }

    /**
     * Get the threshold color.
     * 
     * @param colName
     *            The column name
     * @param val
     *            The value
     * @return The color of the threshold, or default color if configured to not
     *         color based on threshold
     */
    public Color getThresholdColor(String colName, double val) {
        if (thresholdLookup.containsKey(colName) == true) {
            ArrayList<FFMPTableColumnXML> ffmpTableCols = ffmpCfgBasin
                    .getTableColumnData();

            for (FFMPTableColumnXML tableColData : ffmpTableCols) {
                if (tableColData.getColumnName().compareTo(colName) == 0) {
                    if (tableColData.getColorCell()) {
                        return getCellColor(threshMgrMap.get(
                                thresholdLookup.get(colName))
                                .getThresholdColor(val));
                    } else {
                        return defaultColor;
                    }
                }
            }
        }

        return defaultColor;
    }

    public Color getBasinThresholdColor(String colName, double val) {
        if (thresholdLookup.containsKey(colName) == true) {
            return getCellColor(threshMgrMap.get(thresholdLookup.get(colName))
                    .getBasinThresholdColor(val));
        }
        return defaultColor;
    }

    private void createThresholdManager() {
        threshMgrMap.clear();

        ArrayList<FFMPTableColumnXML> tableColData = ffmpCfgBasin
                .getTableColumnData();

        for (ThreshColNames threshName : ThreshColNames.values()) {
            for (FFMPTableColumnXML tcXML : tableColData) {
                if (threshName.name().compareTo(tcXML.getColumnName()) == 0) {
                    ThresholdManager threshMgr = new ThresholdManager(tcXML);
                    threshMgrMap.put(threshName, threshMgr);
                }
            }
        }
    }

    public ThresholdManager getThresholdManager(String threshName) {
        if (thresholdLookup.containsKey(threshName) == true) {
            return threshMgrMap.get(thresholdLookup.get(threshName));
        }

        return null;
    }
    
    public void createAttributesDlgData(String siteKey) {
        ArrayList<FFMPTableColumnXML> columnData = ffmpCfgBasin
                .getTableColumnData();
        FfmpTableConfig tableConfig = FfmpTableConfig.getInstance();
        FfmpTableConfigData tableConfigData = tableConfig
                .getTableConfigData(siteKey);
        String[] columns = tableConfigData.getTableColumnKeys();

        attrData = new AttributesDlgData();

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String displayName = null;
            
            for (FFMPTableColumnXML tcXML : columnData) {
                if (column.contains("_")) {
                    String[] parts = column.split("_");
                    displayName = parts[0];
                    column = parts[1];
                }
                if (column.equalsIgnoreCase(tcXML.getColumnName())) {
                    boolean includedInTable = false;
                    if (column.equalsIgnoreCase(COLUMN_NAME.GUID.getColumnName()) || 
                            column.equalsIgnoreCase(COLUMN_NAME.RATIO.getColumnName()) ||
                            column.equalsIgnoreCase(COLUMN_NAME.DIFF.getColumnName())) {
                        if (ffmpCfgBasin.getIncludedGuids().contains(displayName)) {
                            includedInTable = true;
                            attrData.setGuidColumnIncluded(displayName,
                                    includedInTable);
                        }
                    }

                    attrData.setColumnVisible(column,
                            tcXML.getDisplayedInTable());
                    break;
                }
            }
        }
    }
    
    public AttributesDlgData getVisibleColumns(String siteKey, boolean reReadAttrData) {
        this.reReadAttrData = reReadAttrData;
        return getVisibleColumns(siteKey);
    }

    public AttributesDlgData getVisibleColumns(String siteKey) {
        if ((attrData == null) || reReadAttrData) {
            createAttributesDlgData(siteKey);
        }

        return attrData;
    }

    public void setVisibleColumns(AttributesDlgData attrData) {
        this.attrData = attrData;
        
        ArrayList<FFMPTableColumnXML> columnData = ffmpCfgBasin
                .getTableColumnData();

       
        for (FFMPTableColumnXML tcXML : columnData) {
            if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.RATE.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.RATE.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.NAME.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.NAME.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.QPE.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.QPE.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.QPF.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.QPF.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.GUID.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.GUID.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.RATIO.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.RATIO.getColumnName()));
            } else if (tcXML.getColumnName().equalsIgnoreCase(COLUMN_NAME.DIFF.getColumnName())) {
                tcXML.setDisplayedInTable(attrData.isColumnVisible(COLUMN_NAME.DIFF.getColumnName()));
            }
        }
        
        HashMap<String, Boolean> guidanceMap = attrData.getGuidanceList();
        String list = "";
        boolean first = true;
        for (String key: guidanceMap.keySet()) {
            if (first == false) {
                list.concat(",");
            }
            list = list.concat(key);
            first = false;
        }
        ffmpCfgBasin.setIncludedGuids(list);
        ffmpCfgBasin.setIncludedQPF(attrData.getQpfType());
    }

    public boolean isSplit() {
        ArrayList<FFMPTableColumnXML> columnData = ffmpCfgBasin
                .getTableColumnData();

        for (FFMPTableColumnXML tcXML : columnData) {
            if (tcXML.getColumnName().compareTo(FIELDS.QPF.name()) == 0) {
                return tcXML.getDisplayedInTable();
            }
        }

        return false;
    }

    public double getTimeFrame() {
        return ffmpCfgBasin.getTimeFrame();
    }

    public String getUnderlay() {
        return ffmpCfgBasin.getUnderlay();
    }

    public String[] getActiveBasinTrendPlots() {
        String basinPlots = ffmpCfgBasin.getBasinTrendPlots();

        String[] plotsArray = basinPlots.split(",");

        return plotsArray;
    }

    public boolean columnIsVisible(String colName) {
        ArrayList<FFMPTableColumnXML> columnData = ffmpCfgBasin
                .getTableColumnData();
        for (FFMPTableColumnXML tcXML : columnData) {
            if (tcXML.getColumnName().compareTo(colName) == 0) {
                return tcXML.getDisplayedInTable();
            }
        }

        return false;
    }

    /**
     * Get the index of the column to be sorted when the dialog initially
     * starts. If the column is not visible then default the sort column to be
     * the name column.
     * 
     * @param siteKey The siteKey being used
     * @return Column index.
     */
    public int getStartSortIndex(String siteKey) {
        FFMPMonitor monitor = FFMPMonitor.getInstance();
        FFMPRunConfigurationManager configManager = FFMPRunConfigurationManager
                .getInstance();
        FfmpTableConfig tableCfg = FfmpTableConfig.getInstance();
        FFMPSourceConfigurationManager sourceConfigManager = FFMPSourceConfigurationManager
                .getInstance();
        
        FFMPRunXML runner = configManager.getRunner(monitor.getWfo());
        ProductRunXML prodRunXml = runner.getProduct(siteKey);
        String name = prodRunXml.getProductName();

        ProductXML productXml = sourceConfigManager.getProduct(name);

        ArrayList<String> guidTypes = productXml.getAvailableGuidanceTypes();
        
        String guidRankSource = null;
        if (guidTypes.size() > 1) {
            String colSorted = ffmpCfgBasin.getColumnSorted();
            if (colSorted.contains(",")) {
                String[] parts = colSorted.split(",");
                guidRankSource = parts[1];
            }
        }
        
        FfmpTableConfigData tableCfgData = tableCfg.getTableConfigData(siteKey);
        String[] tableColumns = tableCfgData.getTableColumnKeys();
        String sortedColName = ffmpCfgBasin.getColumnSorted();
        if (sortedColName.contains(",")) {
            String[] parts = sortedColName.split(",");
            sortedColName = parts[0];
        }
        String guidType = null;
        for (int i = 0; i < tableColumns.length; i++) {
            String column = tableColumns[i];
            if (isColumnGuid(column)) {
                String[] parts = column.split("_");
                column = parts[1];
                guidType = parts[0];
            }
            
            if (column.equalsIgnoreCase(sortedColName)) {
                if ((guidType != null) && (guidRankSource != null)) {
                    if (guidType.equalsIgnoreCase(guidRankSource)) {
                        if (columnIsVisible(column)) {
                            return i;
                        }
                    }
                } else {
                    if (columnIsVisible(column)) {
                        return i;
                    }
                }
            }
        }
        
        return 0;
    }

    private boolean isColumnGuid(String columnName) {
        if (columnName.contains("_")) {
            return true;
        }
        
        return false;
    }
    
    public double getFilterValue(ThreshColNames threshColName) {
        ArrayList<FFMPTableColumnXML> columnData = ffmpCfgBasin
                .getTableColumnData();

        FFMPTableColumnXML data = columnData.get(threshColName.getColIndex());

        return data.getFilter();
    }

    /**
     * @return the attrData
     */
    public AttributesDlgData getAttrData() {
        return attrData;
    }

    /**
     * @param attrData the attrData to set
     */
    public void setAttrData(AttributesDlgData attrData) {
        this.attrData = attrData;
    }

    /**
     * @return the reReadAttrData
     */
    public boolean isReReadAttrData() {
        return reReadAttrData;
    }

    /**
     * @param reReadAttrData the reReadAttrData to set
     */
    public void setReReadAttrData(boolean reReadAttrData) {
        this.reReadAttrData = reReadAttrData;
    }
    
    public String getIncludedGuids() {
        return ffmpCfgBasin.getIncludedGuids();
    }
}
