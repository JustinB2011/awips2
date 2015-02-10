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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.monitor.MonitorAreaUtils;
import com.raytheon.uf.common.monitor.data.AdjacentWfoMgr;
import com.raytheon.uf.common.monitor.events.MonitorConfigEvent;
import com.raytheon.uf.common.monitor.events.MonitorConfigListener;
import com.raytheon.uf.common.monitor.xml.AreaIdXML;
import com.raytheon.uf.common.monitor.xml.AreaIdXML.ZoneType;
import com.raytheon.uf.common.monitor.xml.MonAreaConfigXML;
import com.raytheon.uf.common.monitor.xml.StationIdXML;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Monitor configuration manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 5, 2010            mpduff     Initial creation
 * Apr 29, 2011 DR#8986   zhao       Read in Counties instead of Forecast Zones
 * Feb 21 2012  14413     zhao       add code handling "adjacent areas"
 * Nov 20 2012  1297      skorolev   Cleaned code
 * Oct 02 2013  2361      njensen    Use JAXBManager for XML
 * Oct 17 2013  16682     zhao       fixed a bug in readConfigXml()
 * Apr 23 2014  3054      skorolev   Removed unnecessary parameter in the addArea method.
 * May 13 2014  3133      njensen    getStationType returns String instead of ObsHistType
 * May 15 2014  3086      skorolev   Renamed from MonitorConfigurationManager. Replaces three separate area configuration managers with one.
 * Sep 04 2014  3220      skorolev   Added fileUpdated method.
 * Nov 21 2014  3841      skorolev   Corrected addArea, addAdjArea and added getAdjAreaConfigXml.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class FSSObsMonitorConfigurationManager implements
        ILocalizationFileObserver {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(FSSObsMonitorConfigurationManager.class);

    /** Maps county table in the PostgreSQL database. */
    public static final String COUNTY_TABLE = "mapdata.county";

    /** Maps forecast zones table in the PostgreSQL database. */
    public static final String FORECAST_ZONE_TABLE = "mapdata.zone";

    /** Maps marine zones table in the PostgreSQL database. */
    public static final String MARINE_ZONE_TABLE = "mapdata.marinezones";

    /** Single Type JAXB Manager */
    private static final SingleTypeJAXBManager<MonAreaConfigXML> jaxb = SingleTypeJAXBManager
            .createWithoutException(MonAreaConfigXML.class);

    /** Monitoring Area Configuration XML object. */
    protected MonAreaConfigXML configXml;

    /** Adjacent Area Configuration XML object. */
    protected MonAreaConfigXML adjAreaConfigXml;

    /** List of newly added zones. */
    protected List<String> addedZones = new ArrayList<String>();

    /** List of newly added stations. */
    protected List<String> addedStations = new ArrayList<String>();

    /** Name of plugin */
    private static String pluginName = null;

    /** Monitor name */
    private static String monitorName = null;

    /** Name of area configuration file */
    private String configFileName = null;

    /** Name of adjacent Area Configuration file */
    private String adjAreaConfigFileName = null;

    /** List of listeners */
    private final List<MonitorConfigListener> listeners = new CopyOnWriteArrayList<MonitorConfigListener>();

    /** Current site */
    private String currentSite;

    /** Monitor Name **/
    public enum MonName {
        ss, fog, snow
    };

    /** Instance of SAFESEAS monitor configuration manager. */
    private static FSSObsMonitorConfigurationManager ssInstance = new FSSObsMonitorConfigurationManager(
            MonName.ss.name());

    /** Instance of Fog monitor configuration manager. */
    private static FSSObsMonitorConfigurationManager fogInstance = new FSSObsMonitorConfigurationManager(
            MonName.fog.name());

    /** Instance of SNOW monitor configuration manager. */
    private static FSSObsMonitorConfigurationManager snowInstance = new FSSObsMonitorConfigurationManager(
            MonName.snow.name());

    /** Localization Area Configuration File. */
    private LocalizationFile lacf = null;

    /**
     * Private Constructor
     * 
     * @param monitorName
     */
    public FSSObsMonitorConfigurationManager(String monitorName) {
        setMonitorName(monitorName);
        // Avoid confusion in file path
        if (monitorName == MonName.ss.name()) {
            pluginName = "safeseas";
        } else {
            pluginName = monitorName;
        }
        /** Path to Monitoring Area Configuration XML. */
        setConfigFileName(pluginName + File.separatorChar + "monitoringArea"
                + File.separatorChar + "monitorAreaConfig.xml");

        /** Path to Adjacent Areas Configuration XML. */
        setAdjAreaConfigFileName(pluginName + File.separatorChar
                + "monitoringArea" + File.separatorChar
                + "adjacentAreaConfig.xml");
        configXml = new MonAreaConfigXML();
        adjAreaConfigXml = new MonAreaConfigXML();
        readConfigXml();
    }

    /**
     * Reads the XML configuration data for the current XML file name. filename:
     * monitor area config file name adjAreaFileName: adjacent areas config file
     * name
     * 
     */
    public void readConfigXml() {
        boolean monitorAreaFileExists = true;
        boolean adjacentAreaFileExists = true;
        try {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationContext lc = pm.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
            this.currentSite = lc.getContextName();
            lacf = pm.getLocalizationFile(lc, configFileName);
            lacf.addFileUpdatedObserver(this);
            String monitorAreaFilePath = lacf.getFile().getAbsolutePath();
            MonAreaConfigXML configXmltmp = jaxb
                    .unmarshalFromXmlFile(monitorAreaFilePath.toString());
            configXml = configXmltmp;
        } catch (Exception e) {
            statusHandler
                    .handle(Priority.WARN,
                            "No "
                                    + pluginName
                                    + " monitor area configuration file found. New configuration file has been generated and saved:"
                                    + "");
            monitorAreaFileExists = false;
        }

        try {
            IPathManager pm = PathManagerFactory.getPathManager();
            String adjacentAreaFilePath = pm.getFile(
                    pm.getContext(LocalizationType.COMMON_STATIC,
                            LocalizationLevel.SITE), adjAreaConfigFileName)
                    .getAbsolutePath();
            MonAreaConfigXML configXmltmp = jaxb
                    .unmarshalFromXmlFile(adjacentAreaFilePath.toString());
            adjAreaConfigXml = configXmltmp;

        } catch (Exception e) {
            statusHandler
                    .handle(Priority.WARN,
                            "No "
                                    + pluginName
                                    + " adjacent area configuration file found. New configuration file has been generated and saved.");
            adjacentAreaFileExists = false;
        }

        try {
            // Check for a monitor area config file, if one does not exist,
            // create and use defaults
            /**
             * Note: Read in "county" for CONUS site, "forecast zone" for OCONUS
             * site [DR#9905]
             */
            if (!monitorAreaFileExists) {
                List<String> zones;
                if (SiteMap.getInstance().getSite4LetterId(currentSite)
                        .charAt(0) == 'K') { // CONUS site
                    zones = MonitorAreaUtils.getUniqueCounties(currentSite);
                } else { // OCONUS site
                    zones = MonitorAreaUtils.getForecastZones(currentSite);
                }
                List<String> marineZones = MonitorAreaUtils
                        .getMarineZones(currentSite);
                if (!zones.isEmpty()) {
                    for (String zone : zones) {
                        AreaIdXML zoneXml = new AreaIdXML();
                        zoneXml.setAreaId(zone);
                        zoneXml.setType(ZoneType.REGULAR);
                        List<StationIdXML> stations = MonitorAreaUtils
                                .getZoneReportingStationXMLs(zone);
                        if (!stations.isEmpty()) {
                            for (StationIdXML station : stations) {
                                zoneXml.addStationIdXml(station);
                            }
                        }
                        configXml.addAreaId(zoneXml);
                    }
                }
                // add marine zones if any exist
                if (!marineZones.isEmpty()) {
                    for (String zone : marineZones) {
                        AreaIdXML zoneXml = new AreaIdXML();
                        zoneXml.setAreaId(zone);
                        zoneXml.setType(ZoneType.MARITIME);
                        List<StationIdXML> stations = MonitorAreaUtils
                                .getZoneReportingStationXMLs(zone);
                        if (!stations.isEmpty()) {
                            for (StationIdXML station : stations) {
                                zoneXml.addStationIdXml(station);
                            }
                        }
                        configXml.addAreaId(zoneXml);
                    }
                }
                // Default value for Timewindow.
                int defaultVal = 2;
                configXml.setTimeWindow(defaultVal);
                saveConfigXml();
            }
            // Check for an adjacent area config file, if one does not exist,
            // create and use defaults
            if (!adjacentAreaFileExists) {
                AdjacentWfoMgr adjMgr = new AdjacentWfoMgr(currentSite);
                List<String> zones = adjMgr.getAdjZones();
                if (!zones.isEmpty()) {
                    for (String zone : zones) {
                        AreaIdXML zoneXml = new AreaIdXML();
                        zoneXml.setAreaId(zone);
                        zoneXml.setType(ZoneType.REGULAR);
                        List<StationIdXML> stations = MonitorAreaUtils
                                .getZoneReportingStationXMLs(zone);
                        if (!stations.isEmpty()) {
                            for (StationIdXML station : stations) {
                                zoneXml.addStationIdXml(station);
                            }
                        }
                        adjAreaConfigXml.addAreaId(zoneXml);
                    }
                }
                saveAdjacentAreaConfigXml();
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, e.getMessage());
        }
    }

    /**
     * Saves the monitor area XML configuration data to the current XML file
     * name.
     */
    public void saveConfigXml() {
        // Save the xml object to disk
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);
        LocalizationFile newXmlFile = pm.getLocalizationFile(lc,
                getConfigFileName());
        if (newXmlFile.getFile().getParentFile().exists() == false) {
            newXmlFile.getFile().getParentFile().mkdirs();
        }
        try {
            jaxb.marshalToXmlFile(configXml, newXmlFile.getFile()
                    .getAbsolutePath());
            newXmlFile.save();
            lacf = newXmlFile;
            lacf.addFileUpdatedObserver(this);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, e.getMessage());
        }
    }

    /**
     * Saves the adjacent area XML configuration data to the current XML file
     * name.
     * 
     * @param filename
     *            adjacentAreaConfig.xml
     */
    public void saveAdjacentAreaConfigXml() {
        // Save the xml object to disk
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);
        LocalizationFile newXmlFile = pm.getLocalizationFile(lc,
                getAdjAreaConfigFileName());
        if (newXmlFile.getFile().getParentFile().exists() == false) {
            newXmlFile.getFile().getParentFile().mkdirs();
        }
        try {
            jaxb.marshalToXmlFile(adjAreaConfigXml, newXmlFile.getFile()
                    .getAbsolutePath());
            newXmlFile.save();
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, e.getMessage());
        }
    }

    /**
     * Adds a new Area to the configuration. This method only adds the area, the
     * other info will need to be added to the area via the other methods in
     * this class.
     * 
     * @param areaId
     * @param type
     *            Type of zone
     */
    public void addArea(AreaIdXML areaXML) {
        List<AreaIdXML> areaXmlList = configXml.getAreaIds();
        boolean areaExists = false;
        String areaId = areaXML.getAreaId();
        for (AreaIdXML area : areaXmlList) {
            if (area.getAreaId().equals(areaId)) {
                areaExists = true;
                break;
            }
        }
        if (areaExists == false) {
            configXml.addAreaId(areaXML);
            if (!addedZones.contains(areaId)) {
                addedZones.add(areaId);
            }
        }
    }

    /**
     * Adds a new Area to the configuration. This method only adds the area, the
     * other info will need to be added to the area via the other methods in
     * this class.
     * 
     * @param areaId
     *            The area id
     * @param lat
     *            The area latitude
     * @param lon
     *            The area longitude
     * @param type
     *            The area type
     */
    public void addArea(String areaId, double lat, double lon, ZoneType type) {
        List<AreaIdXML> areaXmlList = configXml.getAreaIds();
        boolean areaExists = false;
        for (AreaIdXML area : areaXmlList) {
            if (area.getAreaId().equals(areaId)) {
                area.setType(type);
                areaExists = true;
                break;
            }
        }
        if (areaExists == false) {
            AreaIdXML area = new AreaIdXML();
            area.setAreaId(areaId);
            area.setType(type);
            if ((lat > -90.0) && (lat < 90.0)) {
                area.setCLat(lat);
            }
            if ((lon > -180.0) && (lon < 180.0)) {
                area.setCLon(lon);
            }
            configXml.addAreaId(area);
            if (!addedZones.contains(areaId)) {
                addedZones.add(areaId);
            }
        }
    }

    /**
     * Adds a station to the area.
     * 
     * @param areaId
     *            The area id to add the station to
     * @param stationId
     *            The station id
     * @param type
     *            The station type
     * @param existingStation
     *            Does the station already exist
     */
    public void addStation(String areaId, String stationId, String type,
            boolean existingStation) {
        List<AreaIdXML> areaList = configXml.getAreaIds();
        if (stationId.contains("#")) {
            stationId = stationId.substring(0, stationId.indexOf("#"));
        }
        for (AreaIdXML area : areaList) {
            if (area.getAreaId().equals(areaId)) {
                StationIdXML stationXml = new StationIdXML();
                stationXml.setName(stationId);
                stationXml.setType(type);
                area.addStationIdXml(stationXml);
                addedStations.add(stationId + "#" + type);
            }
        }
    }

    /**
     * Gets the areas of a particular type.
     * 
     * @param type
     *            ZoneType of the area
     * @return List of areas of the specified type
     */
    public List<String> getAreasByType(ZoneType type) {
        List<String> results = new ArrayList<String>();
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            if (area.getType().equals(type)) {
                results.add(area.getAreaId());
            }
        }
        return results;
    }

    /**
     * Gets stations associated with an area.
     * 
     * @param areaId
     *            AreaId of associated stations
     * @return List of stations for area
     */
    public List<String> getAreaStationsWithType(String areaId) {
        List<String> results = new ArrayList<String>();
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            if (area.getAreaId().equals(areaId)) {
                List<StationIdXML> stationList = area.getStationIds();
                for (StationIdXML station : stationList) {
                    results.add(station.getName() + "#" + station.getType());
                }
            }
        }
        return results;
    }

    /**
     * Gets stations associated with an adjacent area.
     * 
     * @param areaId
     *            AreaId of associated stations
     * @return List of stations for area
     */
    public List<String> getAdjacentAreaStationsWithType(String areaId) {
        List<String> results = new ArrayList<String>();
        List<AreaIdXML> areaList = adjAreaConfigXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            if (area.getAreaId().equals(areaId)) {
                List<StationIdXML> stationList = area.getStationIds();
                for (StationIdXML station : stationList) {
                    results.add(station.getName() + "#" + station.getType());
                }
            }
        }
        return results;
    }

    /**
     * Gets stations associated with an area.
     * 
     * @param areaId
     *            AreaId of associated stations
     * @return List of stations for area
     */
    public List<String> getAreaStations(String areaId) {
        List<String> results = new ArrayList<String>();
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            if (area.getAreaId().equals(areaId)) {
                List<StationIdXML> stationList = area.getStationIds();
                for (StationIdXML station : stationList) {
                    results.add(station.getName());
                }
            }
        }
        return results;
    }

    /**
     * Gets the zones where station is monitoring
     * 
     * @param stationId
     *            The station to get the zones
     * @return List of zones
     */
    public List<String> getAreaByStationId(String stationId) {
        List<String> results = new ArrayList<String>();
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            List<StationIdXML> stationList = area.getStationIds();
            for (StationIdXML station : stationList) {
                if (station.getName().equals(stationId)) {
                    results.add(area.getAreaId());
                }
            }
        }
        return results;
    }

    /**
     * Gets all the stations associated with the areas.
     * 
     * @return List of stations
     */
    public List<String> getStations() {
        List<AreaIdXML> areaXml = configXml.getAreaIds();
        List<String> stations = new ArrayList<String>();
        for (AreaIdXML area : areaXml) {
            List<StationIdXML> stationList = area.getStationIds();
            for (StationIdXML station : stationList) {
                stations.add(station.getName() + "#" + station.getType() + "#"
                        + area.getAreaId());
            }
        }
        return stations;
    }

    /**
     * Get Station IDs.
     * 
     * @return List of all stations IDs in the area configuration.
     */
    public Set<String> getStationIDs() {
        List<AreaIdXML> areaXml = configXml.getAreaIds();
        Set<String> stations = new HashSet<String>();
        for (AreaIdXML area : areaXml) {
            List<StationIdXML> stationList = area.getStationIds();
            for (StationIdXML station : stationList) {
                stations.add(station.getName());
            }
        }
        return stations;
    }

    /**
     * Gets a list of all monitoring areas.
     * 
     * @return List<String> of monitor area ids
     */
    public List<String> getAreaList() {
        List<AreaIdXML> areaXmlList = configXml.getAreaIds();
        List<String> areaList = new ArrayList<String>();
        for (AreaIdXML area : areaXmlList) {
            areaList.add(area.getAreaId());
        }
        return areaList;
    }

    /**
     * Gets a list of all adjacent areas.
     * 
     * @return ArrayList<String> of adjacent area ids
     */
    public List<String> getAdjacentAreaList() {
        List<AreaIdXML> areaXmlList = adjAreaConfigXml.getAreaIds();
        List<String> areaList = new ArrayList<String>();
        for (AreaIdXML area : areaXmlList) {
            areaList.add(area.getAreaId());
        }
        return areaList;
    }

    /**
     * Removes a station from the area.
     * 
     * @param area
     *            Area to remove the station from
     * @param station
     *            Station to remove from the area
     */
    public void removeStation(String area, String station) {
        station = station.substring(0, station.indexOf("#"));
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML areaXml : areaList) {
            if (areaXml.getAreaId().equals(area)) {
                List<StationIdXML> stationList = areaXml.getStationIds();
                for (int i = 0; i < stationList.size(); i++) {
                    if (stationList.get(i).getName().equals(station)) {
                        stationList.remove(i);
                        areaXml.getStationIds();
                    }
                }
            }
        }
    }

    /**
     * Removes a station from the monitoring area.
     * 
     * @param station
     *            The station to remove
     */
    public void removeStation(String station) {
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML areaXml : areaList) {
            List<StationIdXML> stationList = areaXml.getStationIds();
            for (int i = 0; i < stationList.size(); i++) {
                StationIdXML stationXml = stationList.get(i);
                if (stationXml.getName().equals(station)) {
                    stationList.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Gets an AreaIdXML object.
     * 
     * @param area
     *            The area to get
     * @return The AreaIdXML object
     */
    public AreaIdXML getAreaXml(String area) {
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML areaXml : areaList) {
            if (areaXml.getAreaId().equals(area)) {
                return areaXml;
            }
        }
        return null;
    }

    /**
     * Gets an AdjAreaXml.
     * 
     * @param zone
     *            from additional list
     * @return
     */
    public AreaIdXML getAdjAreaXML(String zone) {
        List<AreaIdXML> areaList = adjAreaConfigXml.getAreaIds();
        for (AreaIdXML adjAreaXml : areaList) {
            if (adjAreaXml.getAreaId().equals(zone)) {
                return adjAreaXml;
            }
        }
        return null;
    }

    /**
     * Removes an area from the monitoring area.
     * 
     * @param area
     *            The area to remove
     */
    public void removeArea(String area) {
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (int i = 0; i < areaList.size(); i++) {
            if (areaList.get(i).getAreaId().equals(area)) {
                areaList.remove(i);
                break;
            }
        }
        for (int i = 0; i < addedZones.size(); i++) {
            if (addedZones.get(i).equals(area)) {
                addedZones.remove(i);
                break;
            }
        }
    }

    /**
     * Removes an added area.
     * 
     * @param area
     *            The area to remove
     */
    public void removeAddedArea(String area) {
        for (int i = 0; i < addedZones.size(); i++) {
            if (addedZones.get(i).equals(area)) {
                addedZones.remove(i);
                break;
            }
        }
    }

    /**
     * Gets TimeWindow
     * 
     * @return the timeWindow
     */
    public double getTimeWindow() {
        return configXml.getTimeWindow();
    }

    /**
     * Sets TimeWindow
     * 
     * @param hours
     *            the timeWindow to set
     */
    public void setTimeWindow(double hours) {
        configXml.setTimeWindow(hours);
    }

    /**
     * Gets Ship Distance
     * 
     * @return the shipDistance
     */
    public int getShipDistance() {
        return configXml.getShipDistance();
    }

    /**
     * Sets Ship Distance
     * 
     * @param shipDistance
     *            the shipDistance to set
     */
    public void setShipDistance(int shipDistance) {
        configXml.setShipDistance(shipDistance);
    }

    /**
     * Flag is true if to use the Fog Monitor overall threat level.
     * 
     * @return the useAlgorithms flag
     */
    public boolean isUseAlgorithms() {
        return configXml.isUseAlgorithms();
    }

    /**
     * Sets flag UseAlgorithms
     * 
     * @param useAlgorithms
     *            the useAlgorithms to set
     */
    public void setUseAlgorithms(boolean useAlgorithms) {
        configXml.setUseAlgorithms(useAlgorithms);
    }

    /**
     * Gets Configuration Xml
     * 
     * @return the configXml
     */
    public MonAreaConfigXML getConfigXml() {
        return configXml;
    }

    /**
     * Gets Adjacent Configuration Xml
     * 
     * @return the adjAreaConfigXml
     */
    public MonAreaConfigXML getAdjAreaConfigXml() {
        return adjAreaConfigXml;
    }

    /**
     * Gets Added Zones
     * 
     * @return the addedZones
     */
    public List<String> getAddedZones() {
        return addedZones;
    }

    /**
     * Sets Added Zones
     * 
     * @param addedZones
     *            the addedZones to set
     */
    public void setAddedZones(ArrayList<String> addedZones) {
        this.addedZones = addedZones;
    }

    /**
     * Gets Added Stations
     * 
     * @return the addedStations
     */
    public List<String> getAddedStations() {
        return addedStations;
    }

    /**
     * Sets Added Stations
     * 
     * @param addedStations
     *            the addedStations to set
     */
    public void setAddedStations(ArrayList<String> addedStations) {
        this.addedStations = addedStations;
    }

    // TODO: Include Mesonet data types.
    /**
     * Gets station type.
     * 
     * @param theZone
     * @param theStation
     * @return type of station
     */
    public String getStationType(String theZone, String theStation) {
        String result = null;
        List<AreaIdXML> areaList = configXml.getAreaIds();
        for (AreaIdXML area : areaList) {
            if (area.getAreaId().equals(theZone)) {
                List<StationIdXML> stationList = area.getStationIds();
                for (StationIdXML station : stationList) {
                    if (station.getName().equals(theStation)) {
                        return station.getType();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets Configuration File Name
     * 
     * @return
     */
    public String getConfigFileName() {
        return configFileName;
    }

    /**
     * Sets Configuration File Name
     * 
     * @param configFileName
     */
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    /**
     * Gets Adjacent Area Configuration File Name
     * 
     * @return
     */
    public String getAdjAreaConfigFileName() {
        return adjAreaConfigFileName;
    }

    /**
     * Sets Adjacent Area Configuration File Name
     * 
     * @param adjAreaConfigFileName
     */
    public void setAdjAreaConfigFileName(String adjAreaConfigFileName) {
        this.adjAreaConfigFileName = adjAreaConfigFileName;
    }

    /**
     * Adds Monitor Configuration Listener
     * 
     * @param ml
     *            Monitor config listener
     */
    public void addListener(MonitorConfigListener ml) {
        listeners.add(ml);
    }

    /**
     * Removes Monitor Configuration Listener
     * 
     * @param ml
     *            Monitor config listener
     */
    public void removeListener(MonitorConfigListener ml) {
        listeners.remove(ml);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.localization.ILocalizationFileObserver#fileUpdated
     * (com.raytheon.uf.common.localization.FileUpdatedMessage)
     */
    @Override
    public void fileUpdated(FileUpdatedMessage message) {
        if (message.getFileName().equals(getConfigFileName())) {
            try {
                readConfigXml();
                // inform listeners
                for (MonitorConfigListener fl : listeners) {
                    fl.configChanged(new MonitorConfigEvent(this));
                }
                statusHandler.handle(
                        Priority.INFO,
                        "FSSObsMonitorConfigurationManager: "
                                + message.getFileName() + " is updated.");
            } catch (Exception e) {
                statusHandler.handle(
                        Priority.WARN,
                        "FSSObsMonitorConfigurationManager: "
                                + message.getFileName()
                                + " couldn't be updated.", e);
            }
        }
    }

    /**
     * Gets Monitor Name.
     * 
     * @return monitor name
     */
    public String getMonitorName() {
        return monitorName;
    }

    /**
     * Sets Monitor Name.
     * 
     * @param monitorName
     */
    public void setMonitorName(String monitorName) {
        FSSObsMonitorConfigurationManager.monitorName = monitorName;
    }

    /**
     * Remove Adjacent Area.
     * 
     * @param zone
     */
    public void removeAdjArea(String zone) {
        List<AreaIdXML> adjAreaList = adjAreaConfigXml.getAreaIds();
        for (int i = 0; i < adjAreaList.size(); i++) {
            if (adjAreaList.get(i).getAreaId().equals(zone)) {
                adjAreaList.remove(i);
                break;
            }
        }
    }

    /**
     * Add Adjacent Area.
     * 
     * @param area
     */
    public void addAdjArea(AreaIdXML areaXML) {
        List<AreaIdXML> adjAreaList = adjAreaConfigXml.getAreaIds();
        boolean areaExists = false;
        for (AreaIdXML area : adjAreaList) {
            if (area.getAreaId().equals(areaXML.getAreaId())) {
                areaExists = true;
                break;
            }
        }
        if (areaExists == false) {
            adjAreaConfigXml.addAreaId(areaXML);
        }
    }

    /**
     * Get Fog monitor area configuration manager.
     * 
     * @return
     */
    public static FSSObsMonitorConfigurationManager getFogObsManager() {
        fogInstance.readConfigXml();
        return fogInstance;
    }

    /**
     * Get SAFESEAS monitor area configuration manager.
     * 
     * @return
     */
    public static FSSObsMonitorConfigurationManager getSsObsManager() {
        ssInstance.readConfigXml();
        return ssInstance;
    }

    /**
     * Get SNOW monitor area configuration manager.
     * 
     * @return
     */
    public static FSSObsMonitorConfigurationManager getSnowObsManager() {
        snowInstance.readConfigXml();
        return snowInstance;
    }
}
