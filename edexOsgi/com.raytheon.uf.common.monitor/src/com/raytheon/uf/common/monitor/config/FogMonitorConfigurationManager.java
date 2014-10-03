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
package com.raytheon.uf.common.monitor.config;

import java.io.File;

import com.raytheon.uf.common.monitor.xml.MonAreaConfigXML;

/**
 * Fog Monitor Configuration XML File Manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 28, 2009            mpduff     Initial creation
 * Feb 21, 2012 14413      zhao       added code handling "adjacent areas"
 * Sep 24, 2014 2757       skorolev   Added save for Adjacent Area config.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class FogMonitorConfigurationManager extends MonitorConfigurationManager {
    /** Path to Monitoring Area Configuration XML. */
    private static final String CONFIG_FILE_NAME = "fog" + File.separatorChar
            + "monitoringArea" + File.separatorChar + "monitorAreaConfig.xml";

    /** Path to Adjacent Area Configuration XML. */
    private static final String ADJ_AREA_CONFIG_FILE_NAME = "fog"
            + File.separatorChar + "monitoringArea" + File.separatorChar
            + "adjacentAreaConfig.xml";

    /** Singleton instance of this class */
    private static MonitorConfigurationManager instance = null;

    /* Private Constructor */
    private FogMonitorConfigurationManager() {
        configXml = new MonAreaConfigXML();
        adjAreaConfigXml = new MonAreaConfigXML();
    }

    /**
     * Get an instance of this singleton.
     * 
     * @return Instance of this class
     */
    public static synchronized FogMonitorConfigurationManager getInstance() {
        if (instance == null) {
            instance = new FogMonitorConfigurationManager();
        }

        return (FogMonitorConfigurationManager) instance;
    }

    /**
     * Save the XML configuration data to the current XML file name.
     */
    public void saveConfigData() {
        super.saveConfigXml(CONFIG_FILE_NAME);
        super.saveAdjacentAreaConfigXml(ADJ_AREA_CONFIG_FILE_NAME);
    }

    /**
     * Read the XML configuration data for the current XML file name.
     */
    public void readConfigXml(String currentSite) {
        super.readConfigXml(currentSite, CONFIG_FILE_NAME,
                ADJ_AREA_CONFIG_FILE_NAME);
    }

    /**
     * @return the shipDistance
     */
    public int getShipDistance() {
        return configXml.getShipDistance();
    }

    /**
     * @param shipDistance
     *            the shipDistance to set
     */
    public void setShipDistance(int shipDistance) {
        configXml.setShipDistance(shipDistance);
    }

    /**
     * @return the useAlgorithms
     */
    public boolean isUseAlgorithms() {
        return configXml.isUseAlgorithms();
    }

    /**
     * @param useAlgorithms
     *            the useAlgorithms to set
     */
    public void setUseAlgorithms(boolean useAlgorithms) {
        configXml.setUseAlgorithms(useAlgorithms);
    }
}
