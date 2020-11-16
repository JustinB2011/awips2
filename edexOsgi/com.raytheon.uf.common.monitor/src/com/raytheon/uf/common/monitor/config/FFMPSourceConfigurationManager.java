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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.monitor.events.MonitorConfigEvent;
import com.raytheon.uf.common.monitor.events.MonitorConfigListener;
import com.raytheon.uf.common.monitor.xml.FFMPSourceConfigXML;
import com.raytheon.uf.common.monitor.xml.ProductXML;
import com.raytheon.uf.common.monitor.xml.SourceXML;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * FFMPSourceConfigurationManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * Sep 04, 2012  14404    gzhang    Fixing ConcurrentModificationException
 * Apr 26, 2013  1954     bsteffen  Minor code cleanup throughout FFMP.
 * Aug 18, 2013  1742     dhladky   Concurrent mod exception on update fixed
 * Oct 02, 2013  2361     njensen   Use JAXBManager for XML
 * Aug 15, 2015  4722     dhladky   Added new types to be used for new Guidance
 *                                  sources, etc
 * Sep 17, 2015  4756     dhladky   Fixed bugs for multiple guidance sources.
 * Feb 15, 2016  5244     nabowle   Replace deprecated LocalizationFile methods.
 * Aug 08, 2016  5728     mapeters  Added getConfigFileName()
 * Aug 11, 2016  5819     mapeters  Save config file to CONFIGURED instead of
 *                                  SITE
 * 
 * </pre>
 * 
 */

public class FFMPSourceConfigurationManager
        implements ILocalizationPathObserver {

    /** Path to FFMP Source config. */
    private static final String CONFIG_FILE_NAME = "ffmp"
            + IPathManager.SEPARATOR + "FFMPSourceConfig.xml";

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(FFMPSourceConfigurationManager.class);

    /*
     * This needs to initialize before the instance since the constructor will
     * makes use of JAXB. JVM spec 12.4.2 step 9 indicates this will initialize
     * ahead of the instance since it is earlier in in the text source.
     */
    private static final SingleTypeJAXBManager<FFMPSourceConfigXML> jaxb = SingleTypeJAXBManager
            .createWithoutException(FFMPSourceConfigXML.class);

    /**
     * FFMP Source Configuration XML object.
     */
    protected FFMPSourceConfigXML configXml;

    /** Singleton instance of this class */
    private static FFMPSourceConfigurationManager instance = new FFMPSourceConfigurationManager();

    private List<String> virtuals = null;

    private List<String> rates = null;

    private ArrayList<String> guidances = null;

    private ArrayList<String> forecasts = null;

    private List<String> accumulators = null;

    private List<MonitorConfigListener> listeners = new CopyOnWriteArrayList<>();

    /* Private Constructor */
    private FFMPSourceConfigurationManager() {
        configXml = new FFMPSourceConfigXML();
        IPathManager pm = PathManagerFactory.getPathManager();
        pm.addLocalizationPathObserver(CONFIG_FILE_NAME, this);
        readConfigXml();
    }

    /**
     * Get an instance of this singleton.
     * 
     * @return Instance of this class
     */
    public static FFMPSourceConfigurationManager getInstance() {
        return instance;
    }

    public void addListener(MonitorConfigListener fl) {
        listeners.add(fl);
    }

    public void removeListener(MonitorConfigListener fl) {
        listeners.remove(fl);
    }

    /**
     * Read the XML configuration data for the current XML file name.
     */
    public synchronized void readConfigXml() {
        IPathManager pm = PathManagerFactory.getPathManager();
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, CONFIG_FILE_NAME);
        try (InputStream is = lf.openInputStream()) {
            FFMPSourceConfigXML configXmltmp = jaxb
                    .unmarshalFromInputStream(is);

            configXml = configXmltmp;
        } catch (SerializationException | LocalizationException
                | IOException e) {
            statusHandler.error("Error reading file: " + lf.getPath(), e);
        }

        // If only base file exists, save a copy of it to configured
        if (lf.getContext().getLocalizationLevel()
                .equals(LocalizationLevel.BASE)) {
            saveConfigXml();
        }
    }

    /**
     * Save the XML configuration data to the current XML file name.
     */
    private void saveConfigXml() {
        // Save the xml object to disk
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.CONFIGURED);
        ILocalizationFile newXmlFile = pm.getLocalizationFile(lc,
                CONFIG_FILE_NAME);

        try (SaveableOutputStream sos = newXmlFile.openOutputStream()) {
            jaxb.marshalToStream(configXml, sos);
            sos.save();
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, "Couldn't save config file.",
                    e);
        }

    }

    /**
     * Get the sources for this product
     * 
     * @return
     */
    public ArrayList<SourceXML> getSources() {
        return configXml.getSource();
    }

    /**
     * Get the rate sourceXML by product
     * 
     * @return
     */
    public SourceXML getSource(String source) {
        return configXml.getSource(source);
    }

    /**
     * source by display name
     * 
     * @param displayName
     * @return
     */
    public SourceXML getSourceByDisplayName(String displayName) {
        return configXml.getSourceByDisplayName(displayName);
    }

    /**
     * Get the virtual gage basin implementations
     * 
     * @return
     */
    public List<String> getVirtuals() {
        if (virtuals == null) {
            virtuals = new ArrayList<>();

            for (SourceXML xml : configXml.getSource()) {
                if (xml.getSourceType()
                        .equals(SOURCE_TYPE.GAGE.getSourceType())) {
                    virtuals.add(xml.getSourceName());
                }
            }
        }
        return virtuals;
    }

    /**
     * Get the Guidance sources
     * 
     * @return
     */
    public ArrayList<String> getGuidances() {
        if (guidances == null) {
            guidances = new ArrayList<>();

            for (SourceXML xml : configXml.getSource()) {
                if (xml.getSourceType()
                        .equals(SOURCE_TYPE.GUIDANCE.getSourceType())) {
                    guidances.add(xml.getSourceName());
                }
            }
        }
        return guidances;
    }

    /**
     * Get the Guidance Display Names
     * 
     * @return List of display names
     */
    public ArrayList<String> getGuidanceDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();
        for (SourceXML xml : configXml.getSource()) {
            if (xml.getSourceType()
                    .equals(SOURCE_TYPE.GUIDANCE.getSourceType())) {
                if (displayNames.contains(xml.getDisplayName())) {
                    continue;
                }
                displayNames.add(xml.getDisplayName());
            }
        }

        return displayNames;
    }

    /**
     * Get the QPE sources
     * 
     * @return
     */
    public List<String> getQPESources() {
        if (accumulators == null) {
            accumulators = new ArrayList<>();

            for (SourceXML xml : configXml.getSource()) {
                if (xml.getSourceType()
                        .equals(SOURCE_TYPE.QPE.getSourceType())) {
                    accumulators.add(xml.getSourceName());
                }
            }
        }
        return accumulators;
    }

    /**
     * Get the Rate sources
     * 
     * @return
     */
    public List<String> getRates() {
        if (rates == null) {
            rates = new ArrayList<>();

            for (SourceXML xml : configXml.getSource()) {
                if (xml.getSourceType()
                        .equals(SOURCE_TYPE.RATE.getSourceType())) {
                    rates.add(xml.getSourceName());
                }
            }
        }
        return rates;
    }

    /**
     * Get the QPF sources
     * 
     * @return
     */
    public ArrayList<String> getQPFSources() {
        if (forecasts == null) {
            forecasts = new ArrayList<>();

            for (SourceXML xml : configXml.getSource()) {
                if (xml.getSourceType()
                        .equals(SOURCE_TYPE.QPF.getSourceType())) {
                    forecasts.add(xml.getSourceName());
                }
            }
        }
        return forecasts;
    }

    /**
     * Get sources with the same family, used for source bins in FFMPGenerator
     * 
     * @return
     */
    public ArrayList<String> getFamilySources(String family) {

        ArrayList<String> familySources = new ArrayList<>();
        for (SourceXML xml : configXml.getSource()) {
            if (xml.getSourceFamily() != null
                    && xml.getSourceFamily().equals(family)) {
                familySources.add(xml.getSourceName());
            }
        }

        return familySources;
    }

    /**
     * 
     * Enumeration of the data types FFMP can process
     * 
     * @author dhladky
     */
    public enum DATA_TYPE {

        RADAR("RADAR"),
        XMRG("XMRG"),
        GRID("GRID"),
        PDO("PDO"),
        DB("DB"),
        NETCDF("NETCDF");

        private final String dataType;

        private DATA_TYPE(String name) {
            dataType = name;
        }

        public String getDataType() {
            return dataType;
        }
    }

    /**
     * 
     * Enumeration of the source types
     * 
     * @author dhladky
     */
    public enum SOURCE_TYPE {

        RATE("RATE"),
        QPE("QPE"),
        QPF("QPF"),
        GUIDANCE("GUIDANCE"),
        GAGE("GAGE");

        private final String sourceType;

        private SOURCE_TYPE(String name) {
            sourceType = name;
        }

        public String getSourceType() {
            return sourceType;
        }
    }

    /**
     * 
     * Enumeration indicating whether a source contains rate or accumulation
     * values
     * 
     * @author dhladky
     */
    public enum RATEORACCCUM {

        RATE("RATE"), ACCUM("ACCUM");

        private final String rateOrAccum;

        private RATEORACCCUM(String name) {
            rateOrAccum = name;
        }

        public String getRateOrAccum() {
            return rateOrAccum;
        }
    }

    /**
     * 
     * Enumeration of Guidance data types
     * 
     * @author dhladky
     */
    public enum GUIDANCE_TYPE {

        RFC("RFC"), ARCHIVE("ARCHIVE");

        private final String gtype;

        private GUIDANCE_TYPE(String name) {
            gtype = name;
        }

        public String getGuidanceType() {
            return gtype;
        }
    }

    public DATA_TYPE getDataType(String type) {

        if (type.equals(DATA_TYPE.RADAR.getDataType())) {
            return DATA_TYPE.RADAR;
        } else if (type.equals(DATA_TYPE.XMRG.getDataType())) {
            return DATA_TYPE.XMRG;
        } else if (type.equals(DATA_TYPE.GRID.getDataType())) {
            return DATA_TYPE.GRID;
        } else if (type.equals(DATA_TYPE.PDO.getDataType())) {
            return DATA_TYPE.PDO;
        } else if (type.equals(DATA_TYPE.DB.getDataType())) {
            return DATA_TYPE.DB;
        } else if (type.equals(DATA_TYPE.NETCDF.getDataType())) {
            return DATA_TYPE.NETCDF;
        }
        return null;
    }

    public SOURCE_TYPE getSourceType(String sourceName) {
        SourceXML sourceXml = getSource(sourceName);
        if (sourceXml != null) {
            return SOURCE_TYPE.valueOf(sourceXml.getSourceType());
        }
        return null;
    }

    /**
     * Gets the product XML
     * 
     * @param primarySourceName
     * @return
     */
    public ProductXML getProduct(String primarySourceName) {
        for (ProductXML product : getProducts()) {
            if (product.getPrimarySource().equals(primarySourceName)) {
                return product;
            }
        }
        return null;
    }

    public void setProducts(ArrayList<ProductXML> products) {
        configXml.setProducts(products);
    }

    public ArrayList<ProductXML> getProducts() {
        return configXml.getProducts();
    }

    /**
     * Finds the primary source this source is within
     * 
     * @param source
     * @return
     */
    public String getPrimarySource(SourceXML source) {
        for (ProductXML product : getProducts()) {
            if (product.containsSource(source.getSourceName())) {
                return product.getPrimarySource();
            }
        }
        return null;
    }

    @Override
    public void fileChanged(ILocalizationFile file) {
        if (file.getPath().equals(CONFIG_FILE_NAME)) {
            try {
                readConfigXml();
                // inform listeners
                for (MonitorConfigListener fl : listeners) {
                    fl.configChanged(new MonitorConfigEvent(this));
                }
            } catch (Exception e) {
                statusHandler.handle(Priority.WARN,
                        "FFMPSourceConfigurationManager: " + file.getPath()
                                + " couldn't be updated.",
                        e);
            }
        }
    }

    public String getConfigFileName() {
        return CONFIG_FILE_NAME;
    }
}
