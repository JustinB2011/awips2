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
package com.raytheon.uf.viz.monitor.ffmp.ui.rsc;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;

import com.raytheon.uf.common.dataplugin.ffmp.FFMPAggregateRecord;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPRecord;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPUtils;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.monitor.config.FFMPRunConfigurationManager;
import com.raytheon.uf.common.monitor.config.FFMPSourceConfigurationManager;
import com.raytheon.uf.common.monitor.xml.FFMPRunXML;
import com.raytheon.uf.common.monitor.xml.ProductRunXML;
import com.raytheon.uf.common.monitor.xml.ProductXML;
import com.raytheon.uf.common.monitor.xml.SourceXML;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.monitor.ffmp.FFMPMonitor;
import com.raytheon.uf.viz.monitor.ffmp.ui.dialogs.FFMPConfig;
import com.raytheon.uf.viz.monitor.ffmp.ui.listeners.FFMPLoadListener;
import com.raytheon.uf.viz.monitor.ffmp.ui.listeners.FFMPLoaderEvent;

/**
 * Place holder more or less for a ResourceData Object This dosen't do anything
 * currently.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 Feb, 2011   7587    dhladky     Initial creation
 * 25 Jan, 2012   DR13839 gzhang      Handle Uris and Huc processing
 * 01/27/13     1478      D. Hladky   revamped the cache file format to help NAS overloading
 * 02/01/13      1569    D. Hladky   Changed to reading aggregate records from pypies
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */
public class FFMPDataLoader extends Thread {

    private static final IUFStatusHandler statusHandler = UFStatus.getHandler(FFMPDataLoader.class);

    private ProductXML product = null;

    private FFMPRunXML runner = null;

    private Date timeBack = null;

    private Date mostRecentTime = null;

    public boolean isDone = false;

    public LOADER_TYPE loadType = null;

    private String siteKey = null;

    private String dataKey = null;

    private ArrayList<String> hucsToLoad = null;

    private String wfo = null;

    private FFMPResourceData resourceData = null;

    private FFMPConfig config = null;

    private ArrayList<FFMPLoadListener> loadListeners = new ArrayList<FFMPLoadListener>();

    public FFMPDataLoader(FFMPResourceData resourceData, Date timeBack,
            Date mostRecentTime, LOADER_TYPE loadType,
            ArrayList<String> hucsToLoad) {

        this.product = resourceData.getProduct();
        this.siteKey = resourceData.siteKey;
        this.dataKey = resourceData.dataKey;
        this.timeBack = timeBack;
        this.mostRecentTime = mostRecentTime;
        this.loadType = loadType;
        this.hucsToLoad = hucsToLoad;
        this.wfo = resourceData.wfo;
        this.resourceData = resourceData;
        this.runner = FFMPRunConfigurationManager.getInstance().getRunner(wfo);
        this.config = FFMPConfig.getInstance();

        if ((loadType == LOADER_TYPE.INITIAL)
                || (loadType == LOADER_TYPE.GENERAL)) {
            this.setPriority(MAX_PRIORITY);
        } else {
            this.setPriority(MIN_PRIORITY);
        }
    }

    /**
     * Add listener
     * 
     * @param fl
     */
    public synchronized void addListener(FFMPLoadListener fl) {
        loadListeners.add(fl);
    }

    /**
     * Remove listener
     * 
     * @param fl
     */
    public synchronized void removeListener(FFMPLoadListener fl) {
        loadListeners.remove(fl);
    }

    // kills the loader
    public void kill() {
        isDone = true;
    }

    @Override
    public void run() {
        
        long time = System.currentTimeMillis();

        try {
            resourceData.setLoader(loadType);
            FFMPMonitor monitor = getMonitor(); 
            FFMPSourceConfigurationManager sourceConfig = monitor.getSourceConfig();

            ProductRunXML productRun = runner.getProduct(siteKey);
            ArrayList<String> qpfSources = new ArrayList<String>();
            String layer = config.getFFMPConfigData().getLayer();
            boolean isProductLoad = true;
            String rateURI = null;

            if ((loadType == LOADER_TYPE.INITIAL)
                    || (loadType == LOADER_TYPE.GENERAL)) {
                rateURI = monitor.getAvailableUri(siteKey, dataKey,
                        product.getRate(), mostRecentTime);
            }

            NavigableMap<Date, List<String>> qpeURIs = monitor
                    .getAvailableUris(siteKey, dataKey, product.getQpe(),
                            timeBack);

            ArrayList<NavigableMap<Date, List<String>>> qpfs = new ArrayList<NavigableMap<Date, List<String>>>();

            for (String qpfType : productRun.getQpfTypes(product)) {
                for (SourceXML qpfSource : productRun.getQpfSources(product,
                        qpfType)) {

                    NavigableMap<Date, List<String>> qpfURIs = null;
                    Date qpfTime = timeBack;

                    if (loadType == LOADER_TYPE.GENERAL) {
                        qpfTime = monitor.getPreviousQueryTime(siteKey,
                                qpfSource.getSourceName());
                    }

                    qpfURIs = monitor.getAvailableUris(siteKey, dataKey,
                            qpfSource.getSourceName(), qpfTime);

                    if (qpfURIs != null && !qpfURIs.isEmpty()) {
                        qpfs.add(qpfURIs);
                        qpfSources.add(qpfSource.getSourceName());
                    }
                }
            }

            NavigableMap<Date, List<String>> virtualURIs = monitor
                    .getAvailableUris(siteKey, dataKey, product.getVirtual(),
                            timeBack);

            HashMap<String, NavigableMap<Date, List<String>>> guids = new HashMap<String, NavigableMap<Date, List<String>>>();

            for (String type : productRun.getGuidanceTypes(product)) {
                for (SourceXML guidSource : productRun.getGuidanceSources(
                        product, type)) {

                    NavigableMap<Date, List<String>> iguidURIs = null;
                    Date guidTime = timeBack;

                    if (loadType == LOADER_TYPE.GENERAL) {
                        guidTime = monitor.getPreviousQueryTime(siteKey,
                                guidSource.getSourceName());
                    }

                    iguidURIs = monitor.getAvailableUris(siteKey, dataKey,
                            guidSource.getSourceName(), guidTime);

                    if (iguidURIs != null && !iguidURIs.isEmpty()) {
                        guids.put(guidSource.getSourceName(), iguidURIs);
                    }
                }
            }
            // We only load all for long range data, all + layer for medium
            // range
            if (loadType == LOADER_TYPE.TERTIARY) {
                hucsToLoad.clear();
                hucsToLoad.add(FFMPRecord.ALL);
            }

            if (isDone) {
                return;
            }

            // rate
            if (rateURI != null) {
                fireLoaderEvent(loadType, "Processing " + product.getRate(),
                        isDone);
                for (String phuc : hucsToLoad) {
                    monitor.processUri(isProductLoad, rateURI, siteKey,
                            product.getRate(), timeBack, phuc);
                }
                fireLoaderEvent(loadType, product.getRate(), isDone);
            }

            // qpes
            fireLoaderEvent(loadType, "Processing " + product.getQpe(), isDone);
            FFMPAggregateRecord qpeCache = null;

            if (loadType == LOADER_TYPE.INITIAL) {

                SourceXML source = sourceConfig.getSource(
                        product.getQpe());

                qpeCache = readAggregateRecord(source, dataKey, wfo);

                if (qpeCache != null) {
                    monitor.insertFFMPData(qpeCache, siteKey,
                            product.getQpe());
                }
            }

            // Use this method of QPE data retrieval if you don't have cache files
            if (!qpeURIs.isEmpty() && qpeCache == null) {
                for (String phuc : hucsToLoad) {
                    if (phuc.equals(layer)
                            || phuc.equals(FFMPRecord.ALL)) {
                        monitor.processUris(qpeURIs, isProductLoad,
                                siteKey, product.getQpe(), timeBack, phuc);
                    }
                }
            }

            fireLoaderEvent(loadType, product.getQpe(), isDone);

            int i = 0;
            for (NavigableMap<Date, List<String>> qpfURIs : qpfs) {
                // qpf
                fireLoaderEvent(loadType, "Processing " + product.getQpf(i),
                        isDone);
                FFMPAggregateRecord qpfCache = null;

                if (loadType == LOADER_TYPE.INITIAL) {

                    
                    SourceXML source = sourceConfig
                            .getSource(qpfSources.get(i));

                    String pdataKey = findQPFHomeDataKey(source);
                    qpfCache = readAggregateRecord(source, pdataKey, wfo);

                    if (qpfCache != null) {
                        for (String phuc : hucsToLoad) {
                            if ((phuc.equals(layer) || phuc.equals(FFMPRecord.ALL))
                                    && loadType == LOADER_TYPE.INITIAL
                                    && source.getSourceName().equals(
                                            config.getFFMPConfigData()
                        .getIncludedQPF())) {
                                if (!qpfURIs.isEmpty()) {

                                    monitor.processUris(qpfURIs,
                                            isProductLoad, siteKey,
                                            source.getSourceName(), timeBack,
                                            phuc);
                                }
                            }
                        }

                        monitor.insertFFMPData(qpfCache, siteKey,
                                source.getSourceName());
                    }
                }
                // if (isUrisProcessNeeded(qpfData,qpfURIs))
                // {/*DR13839*/
                // Use this method of QPF data retrieval if you don't have cache files
                if ((qpfCache == null) && !qpfURIs.isEmpty()) {
                    for (String phuc : hucsToLoad) {
                        if (phuc.equals(layer)
                                || phuc.equals(FFMPRecord.ALL)) { // old
                                                         // code:
                                                         // keep
                                                         // for
                                                         // reference*/
                            // if (isHucProcessNeeded(phuc)) {/*DR13839*/
                            monitor.processUris(qpfURIs, isProductLoad,
                                    siteKey, product.getQpf(i), timeBack, phuc);
                        }
                    }
                }

                fireLoaderEvent(loadType, product.getQpf(i), isDone);

                i++;
            }

            fireLoaderEvent(loadType, "Processing " + product.getVirtual(),
                    isDone);
            FFMPAggregateRecord vgbCache = null;

            if (loadType == LOADER_TYPE.INITIAL) {

                SourceXML source = sourceConfig.getSource(
                        product.getVirtual());

                vgbCache = readAggregateRecord(source, dataKey, wfo);

                if (vgbCache != null) {

                    monitor.insertFFMPData(vgbCache, siteKey,
                            product.getVirtual());
                }
            }

            // Use this method of Virtual data retrieval if you don't have cache files
            if ((vgbCache == null) && !virtualURIs.isEmpty()) {
                monitor.processUris(virtualURIs, isProductLoad, siteKey,
                        product.getVirtual(), timeBack, FFMPRecord.ALL);
            }

            fireLoaderEvent(loadType, product.getVirtual(), isDone);

            // process guidance all for all only, never uses cache files
            for (String type : productRun.getGuidanceTypes(product)) {

                ArrayList<SourceXML> guidSources = productRun
                        .getGuidanceSources(product, type);
                for (SourceXML guidSource : guidSources) {

                    NavigableMap<Date, List<String>> iguidURIs = guids
                            .get(guidSource.getSourceName());

                    fireLoaderEvent(loadType,
                            "Processing " + guidSource.getSourceName(), isDone);

                    monitor.processUris(iguidURIs, isProductLoad, siteKey,
                            guidSource.getSourceName(), timeBack, FFMPRecord.ALL);

                    fireLoaderEvent(loadType, guidSource.getSourceName(),
                            isDone);

                }
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,"General Problem in Loading FFMP Data", e);
        } finally {
            isDone = true;
        }

        String message = null;
        if (loadType == LOADER_TYPE.INITIAL) {
            message = "Finished Initial Load";
        } else {
            message = "Finished General Data Load";
        }

        long endTime = (System.currentTimeMillis()) - time;
        System.out.println(loadType.loaderType + " Loader took: " + endTime / 1000 + " seconds");

        fireLoaderEvent(loadType, message, isDone);
    }

    /**
     * Fire loader updates to the front end displays
     * 
     * @param FFMPLoaderStatus
     **/
    public void fireLoaderEvent(LOADER_TYPE ltype, String lmessage,
            boolean lstatus) {

        final FFMPLoaderStatus sstatus = new FFMPLoaderStatus(ltype, lmessage,
                lstatus);

        VizApp.runAsync(new Runnable() {
            public void run() {
                FFMPLoaderEvent fle = new FFMPLoaderEvent(sstatus);
                Iterator<FFMPLoadListener> iter = loadListeners.iterator();

                while (iter.hasNext()) {
                    FFMPLoadListener listener = iter.next();
                    listener.loadStatus(fle);
                }
            }
        });
    }

    private FFMPMonitor getMonitor() {
        if (FFMPMonitor.isRunning()) {
            // System.out.println("Monitor is running...");
            return FFMPMonitor.getInstance();
        } else {
            // System.out.println("Monitor is dead...");
            isDone = true;
            return null;
        }
    }

    public enum LOADER_TYPE {

        INITIAL("Initial"), GENERAL("General"), SECONDARY("Secondary"), TERTIARY(
                "Tertiary");

        private final String loaderType;

        private LOADER_TYPE(String name) {
            loaderType = name;
        }

        public String getLoaderType() {
            return loaderType;
        }
    };

    /**
     * Loads the Cache files
     * 
     * @param sourceName
     * @param huc
     * @param wfo
     * @return
     */
    private FFMPAggregateRecord readAggregateRecord(SourceXML source,
            String pdataKey, String wfo) throws Exception {

        FFMPAggregateRecord record = null;
        String sourceSiteDataKey = getSourceSiteDataKey(source, pdataKey);

        try {

            File hdf5File = FFMPUtils.getHdf5File(wfo, sourceSiteDataKey);
            IDataStore dataStore = DataStoreFactory.getDataStore(hdf5File);
            IDataRecord rec = dataStore.retrieve(wfo, sourceSiteDataKey,
                    Request.ALL);
            byte[] bytes = ((ByteDataRecord) rec).getByteData();
            record = SerializationUtil.transformFromThrift(
                    FFMPAggregateRecord.class, bytes);
        } catch (Exception e) {
            statusHandler.handle(Priority.WARN,
                    "Couldn't read Aggregate Record" + sourceSiteDataKey);
        }

        return record;
    }

    /**
     * Finds the home datakey identifier for QPF sources
     * 
     * @param source
     * @return
     */
    private String findQPFHomeDataKey(SourceXML source) {

        FFMPRunConfigurationManager runManager = FFMPRunConfigurationManager
                .getInstance();

        for (ProductRunXML product : runManager.getProducts()) {

            try {
                // we are just checking if it exists or not
                String pdataKey = product.getProductKey();
                String sourceSiteDataKey = getSourceSiteDataKey(source, pdataKey);
                File hdf5File = FFMPUtils.getHdf5File(wfo, sourceSiteDataKey);
                DataStoreFactory.getDataStore(hdf5File);

                return pdataKey;
            } catch (Exception e) {
                // not the right key, doesn't exist
                continue;
            }
        }

        return siteKey;
    }
       
    /**
     * Get the sourceSiteDataKey for this piece of data
     * @param source
     * @param pdataKey
     * @return
     */
    private String getSourceSiteDataKey(SourceXML source, String pdataKey) {
        return source.getSourceName() + "-" + siteKey + "-"
        + pdataKey;
    }

}