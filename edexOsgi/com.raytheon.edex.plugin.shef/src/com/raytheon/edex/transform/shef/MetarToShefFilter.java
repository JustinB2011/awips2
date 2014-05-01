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
package com.raytheon.edex.transform.shef;

import static com.raytheon.uf.common.localization.LocalizationContext.LocalizationType.EDEX_STATIC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.ohd.AppsDefaults;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.edex.decodertools.core.filterimpl.AbstractFilterElement;
import com.raytheon.uf.edex.decodertools.core.filterimpl.AbstractObsFilter;
import com.raytheon.uf.edex.decodertools.core.filterimpl.PluginDataObjectFilter;

/**
 * Use information in metarToShefFilter.xml, MetarToShefFilter filters out the
 * metar messages before send the message to MetarToShefTransformer to encode to
 * a SHEF message.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date       Ticket# Engineer Description
 * ---------- ------- -------- --------------------------
 * 1/10/2013  15497   wkwock   Initial creation
 * 2/13/2013   1584   mpduff   Fix creation of "dummy" config.
 * 08/08/2013 16408   wkwock   Use different metar.cfg file and options
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class MetarToShefFilter {
    @XmlElement
    @DynamicSerializeElement
    protected List<MetarToShefRun> metarToShefRun = new ArrayList<MetarToShefRun>();

    private static final String ERROR_1_FMT = "Could not create {%s} context for file \"%s\"";

    private static final String ERROR_2_FMT = "File %s does not exist";

    private static final String METAR_CFG = "metar.cfg";

    public static final String FILTERS_DIR = "plugin-filters";

    private final String metarToShefOptions = AppsDefaults.getInstance()
            .getToken("metar2shef_options");

    private final Log logger = LogFactory.getLog(getClass());

    private String filterConfigFile = null;

    public MetarToShefFilter() {
    }

    public MetarToShefFilter(String configFile, String localContext) {
        filterConfigFile = configFile;
        try {
            File filterDir = null;
            IPathManager manager = PathManagerFactory.getPathManager();
            if (manager != null) {
                LocalizationContext context = manager.getContext(EDEX_STATIC,
                        LocalizationLevel.valueOf(localContext));
                if (context != null) {
                    filterDir = manager.getFile(context, FILTERS_DIR);
                    if (filterDir.exists()) {
                        File srcFile = new File(filterDir, filterConfigFile);

                        byte[] data = new byte[(int) srcFile.length()];

                        InputStream stream = getInputStream(srcFile);
                        try {
                            stream.read(data);
                            stream.close();
                            Object obj = SerializationUtil
                                    .unmarshalFromXml(new String(data));
                            if (obj instanceof PluginDataObjectFilter) {
                                logger.info("Found " + filterConfigFile
                                        + " is PluginDataObjectFilter");
                                PluginDataObjectFilter pdof = (PluginDataObjectFilter) obj;
                                MetarToShefRun mtsr = new MetarToShefRun();
                                mtsr.setConfigFileName(METAR_CFG);
                                mtsr.setMetarToShefOptions(metarToShefOptions);
                                mtsr.setFilterElements(pdof.getFilterElements());
                                mtsr.setFilterName(pdof.getFilterName());
                                this.metarToShefRun.add(mtsr);
                            } else if (obj instanceof MetarToShefFilter) {
                                MetarToShefFilter filter = (MetarToShefFilter) obj;
                                this.metarToShefRun = filter.metarToShefRun;
                                logger.info("Found " + filterConfigFile
                                        + " is MetarToShefFilter");
                            } else {
                                logger.error("Found " + filterConfigFile
                                        + " is "
                                        + obj.getClass().getCanonicalName());
                                createDummyFilter();
                            }
                        } catch (IOException e) {
                            logger.error("Unable to read filter config", e);
                        } catch (JAXBException e) {
                            logger.error("Unable to unmarshall filter config",
                                    e);
                        }
                    } else {
                        logger.error(String.format(ERROR_2_FMT,
                                filterDir.getPath()));
                        createDummyFilter();
                    }
                } else {
                    logger.error(String.format(ERROR_1_FMT, localContext,
                            configFile));
                    createDummyFilter();
                }
            } else {
                // Could not create PathManager
            }
        } catch (Exception e) {
            logger.error("Error creating filter.", e);
            createDummyFilter();
        }

        for (MetarToShefRun mtsr : metarToShefRun) {
            logger.info("Filter name = " + mtsr.getFilterName()
                    + " with config file: " + mtsr.getConfigFileName());
        }
    }

    private PluginDataObject filterARun(PluginDataObject report,
            List<AbstractFilterElement> filterElements) {
        if (report != null) {
            PluginDataObject r = null;
            boolean keep = true;
            for (AbstractFilterElement element : filterElements) {
                r = element.filter(report);

                    // Only allow keep to be set to true. Once true it stays
                    // that way.
                    if (AbstractObsFilter.INCLUDE_TYPE.equals(element
                            .getFilterType())) {
                        // Did the filter pass?
                        if (r == null) {
                            // If we fail an element, exit now.
                            keep = false;
                            break;
                        }
                    } else if (AbstractObsFilter.EXCLUDE_TYPE.equals(element
                            .getFilterType())) {
                        if (r != null) {
                            // There was a match, so we want to remove this
                            // item.
                            keep = false;
                            // And there's no reason for further checks.
                            break;
                        }
                    }
                }
                if (keep) {
                    report = r;
                } else {
                    report = null;
                }
        }
        return report;
    }

    /**
     * Apply the list of filters against given input data.
     * 
     */
    // @Override
    public PluginDataObject[] filter(PluginDataObject[] reports) {
        HashMap<String,MetarToShefRun> matchList = new HashMap<String,MetarToShefRun>();
    	ArrayList<PluginDataObject> reportList = new ArrayList<PluginDataObject> ();
        for (PluginDataObject report : reports) {
            for (MetarToShefRun mtsr : metarToShefRun) {
                PluginDataObject resultRpt = filterARun(report, mtsr.getFilterElements());
                if (resultRpt != null ) {
            	    reportList.add(resultRpt);
            	    matchList.put(resultRpt.getDataURI(), mtsr);
                    MetarToShefTransformer.setMatchList (matchList);
                    break;
                }
            }
        }
        return (PluginDataObject[])reportList.toArray(new PluginDataObject[reportList.size()]);
    }

    private void createDummyFilter() {
        MetarToShefRun mtsr = new MetarToShefRun();
        mtsr.setConfigFileName(METAR_CFG);
        mtsr.setMetarToShefOptions(metarToShefOptions);

        // Add a dummy element.
        AbstractFilterElement dummy = new AbstractFilterElement() {
            @Override
            public PluginDataObject filter(PluginDataObject report) {
                return report;
            }
        };
        dummy.setFilterType(AbstractObsFilter.INCLUDE_TYPE);
        mtsr.getFilterElements().add(dummy);
        mtsr.setFilterName("Created Pass-All filter");
        this.metarToShefRun.add(mtsr);
    }

    /**
     * 
     * @param file
     * @return
     */
    private static FileInputStream getInputStream(File file) {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fis;
    }

    public void addMetarToShefRun(MetarToShefRun element) {
        metarToShefRun.add(element);
    }

    /**
     * 
     * @return
     */
    public List<MetarToShefRun> getMetarToShefRun() {
        return metarToShefRun;
    }

    /**
     * 
     * @param elements
     */
    public void setMetarToShefRun(List<MetarToShefRun> elements) {
        metarToShefRun = elements;
    }

}
