package com.raytheon.uf.edex.datadelivery.retrieval.opendap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.datadelivery.registry.Ensemble;
import com.raytheon.uf.common.datadelivery.registry.Provider.ServiceType;
import com.raytheon.uf.common.datadelivery.retrieval.util.HarvesterServiceManager;
import com.raytheon.uf.common.datadelivery.retrieval.util.LookupManager;
import com.raytheon.uf.common.datadelivery.retrieval.xml.Constant;
import com.raytheon.uf.common.datadelivery.retrieval.xml.DataSetConfig;
import com.raytheon.uf.common.datadelivery.retrieval.xml.DataSetNaming;
import com.raytheon.uf.common.datadelivery.retrieval.xml.ServiceConfig;
import com.raytheon.uf.common.datadelivery.retrieval.xml.UnitConfig;
import com.raytheon.uf.common.datadelivery.retrieval.xml.UnitLookup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import dods.dap.AttributeTable;

/**
 * Constants for working with OpenDAP. This class should remain package-private,
 * all access should be limited to classes in the same package.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2011    218      dhladky     Initial creation
 * Jul 24, 2012    955      djohnson    Use {@link Pattern}s, simplify logic.
 * Aug 09, 2012    1022     djohnson    Handle correct parsing of wave model dataset names.
 * Aug 31, 2012    1125     djohnson    Rename getCollectionAndCycle() to getDataSetNameAndCycle(), 
 *                                      gens related datasets prepend collection name.
 * Sep 06, 2012    1125     djohnson    Also prepend naefs collection names.
 * Oct 28, 2012    1163     dhladky     Largely did away with this Class in lieu of configfile.
 * Nov 09, 2012    1163     dhladky     Made pre-load for service config
 * Nov 19, 2012    1166     djohnson    Clean up JAXB representation of registry objects.
 * Jan 08, 2013    1466     dhladky     NCOM dataset name parsing fix.
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */
final class OpenDAPParseUtility {

    private static final Pattern QUOTES_PATTERN = Pattern.compile("\"");

    /** Singleton instance of this class */
    private static OpenDAPParseUtility instance = null;

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(OpenDAPParseUtility.class);
    
    /*
     * Service configuration for OPENDAP
     */
    private final ServiceConfig serviceConfig;

    /* Private Constructor */
    private OpenDAPParseUtility() {
        serviceConfig = HarvesterServiceManager.getInstance().getServiceConfig(
                ServiceType.OPENDAP);
    }
    
    /**
     * call this to get your instance
     * @return
     */
    public static OpenDAPParseUtility getInstance() {
    	if (instance == null) {
    		instance = new OpenDAPParseUtility();
    	}
    	return instance;
    }

    /**
     * Get the dataset name and cycle.
     * 
     * @param linkKey
     *            the linkKey
     * @param collectionName
     *            the collection name
     * @return the dataset name and cycle
     */
    public List<String> getDataSetNameAndCycle(String linkKey,
            String collectionName) throws Exception {
        String datasetName = null;
        String cycle = null;
        String numCycle = null;

        if (serviceConfig.getDataSetConfig() != null) {
            // cycle defaults to none
            cycle = serviceConfig.getConstantValue("NONE");
            DataSetConfig dsc = serviceConfig.getDataSetConfig();
            Map<com.raytheon.uf.common.datadelivery.retrieval.xml.Pattern, Pattern> patterns = dsc
                    .getPatternMap();

            for (Entry<com.raytheon.uf.common.datadelivery.retrieval.xml.Pattern, Pattern> entry : patterns
                    .entrySet()) {

                com.raytheon.uf.common.datadelivery.retrieval.xml.Pattern pat = entry
                        .getKey();

                // special processing
                if (pat.getName().equals(collectionName)) {
                    Pattern innerPattern = Pattern.compile(pat.getRegex());
                    String[] chunks = innerPattern.split(linkKey);
                    // special RTOFS
                    if (pat.getDataSetLocationAt(0) == 0
                            && pat.getCycleLocationAt(0) == 1
                            && chunks.length == 2) {
                        datasetName = chunks[0];
                        cycle = chunks[1];
                        break;
                        // Special for NCOM, no cycle
                    } else if (pat.getDataSetLocationAt(0) == 1
                            && chunks.length == 3) {
                        datasetName = chunks[1];
                        break;
                    } else {
                        // most often used
                        datasetName = linkKey;
                        break;
                        // there is no cycle;
                    }
                } else {
                    // non specific pattern processing
                    Matcher m = entry.getValue().matcher(linkKey);
                    if (m.find()) {
                        if (m.groupCount() > 0) {
                            if (pat.getDataSetLocationAt(0) == 1
                                    && pat.getCycleLocationAt(0) == 3) {
                                datasetName = m.group(1);
                                cycle = m.group(3);
                            }
                            if (pat.getDataSetLocationAt(0) == 1
                                    && pat.getCycleLocationAt(0) == 2
                                    && m.groupCount() == 2) {

                                datasetName = m.group(1);
                                cycle = m.group(2);
                            }
                            if (pat.getDataSetLocationAt(0) == 1
                                    && pat.getCycleLocationAt(0) == 2
                                    && pat.getDataSetLocationAt(1) == 3
                                    && m.groupCount() > 2) {

                                datasetName = m.group(1) + m.group(3);
                                cycle = m.group(2);
                            }

                            if (datasetName != null) {
                                break;
                            }
                        }
                    }
                }
            }

            // Fall back to the default, collectionName
            if (datasetName == null) {
                datasetName = collectionName;
            }

            // the dataset names are the same for the following related
            // collections,
            // so prepend with the collection name.
            if (dsc.getDataSetNamings() != null) {

                DataSetNaming dsn = dsc.getDataSetNamingByName(collectionName);

                if (dsn != null) {

                    Constant constant = serviceConfig
                            .getNamingSchema(dsn.getExpression());

                    if (constant != null) {
                        if (dsn.getExpression()
                                .equals(serviceConfig
                                        .getConstantValue("ALTERNATE_NAMING_SCHEMA1"))) {
                            datasetName = collectionName + dsn.getSeparator()
                                    + datasetName;
                        } else if (dsn
                                .getExpression()
                                .equals(serviceConfig
                                        .getConstantValue("ALTERNATE_NAMING_SCHEMA2"))) {
                            datasetName = collectionName;
                        } else {
                            statusHandler
                                    .handle(Priority.INFO,
                                            dsn.getExpression()
                                                    + "Is not a known OPENDAP Alternate naming schema. "
                                                    + collectionName);
                        }
                    }
                }
            }

            try {
                numCycle = Integer.valueOf(
                        cycle.substring(0, cycle.length() - 1)).toString();
            } catch (NumberFormatException nfe) {
                // Not a problem, just not a numeric cycle
            }
        }

        return Arrays.asList(datasetName, cycle, numCycle);
    }

    public Pattern getTimeStepPattern() {

        String timeStep = serviceConfig
                .getConstantValue("TIME_STEP_PATTERN");
        return Pattern.compile(timeStep);
    }

    public Pattern getUnitPattern() {
        String unitPattern = serviceConfig
                .getConstantValue("UNIT_PATTERN");
        return Pattern.compile(unitPattern);
    }

    public Pattern getZPattern() {

        String z = serviceConfig.getConstantValue("Z_PATTERN");
        return Pattern.compile(z);
    }

    /**
     * Remove the Z from the date
     * 
     * @param date
     * @return
     */
    public String parseDate(String date) {
        return trim(getZPattern().matcher(date).replaceAll(
                serviceConfig.getConstantValue("BLANK")));
    }

    /**
     * parse ensemble model info
     * 
     * @param table
     * @return
     */
    public Ensemble parseEnsemble(AttributeTable table) {

        String stime = serviceConfig.getConstantValue("TIMEINIT");
        String slength = serviceConfig.getConstantValue("LENGTH");
        String sname = serviceConfig.getConstantValue("NAME");
        int size = new Integer(trim(table.getAttribute(
                serviceConfig.getConstantValue("SIZE")).getValueAt(0)))
                .intValue();
        String name = null;
        String length = null;
        String tinit = null;

        if (table.getAttribute(slength) != null) {
            length = table.getAttribute(slength).getValueAt(0);
        }

        if (table.getAttribute(sname) != null) {
            name = table.getAttribute(sname).getValueAt(0);
        }

        if (table.getAttribute(stime) != null) {
            tinit = table.getAttribute(stime).getValueAt(0);
        }

        Ensemble ens = new Ensemble();
        ens.setSize(size);
        ens.setLength(length);
        ens.setName(name);
        ens.setInit(tinit);

        return ens;
    }

    /**
     * Parses the time steps
     * 
     * @param timeStep
     * @return
     */
    public List<String> parseTimeStep(String inStep) {
        List<String> step = new ArrayList<String>();

        Matcher matcher = getTimeStepPattern().matcher(trim(inStep));

        if (matcher.find()) {
            step.add(matcher.group(1));
            step.add(matcher.group(2));
        } else {
            throw new IllegalArgumentException(
                    "Unable to find time step with input [" + inStep + "]");
        }

        return step;
    }

    /**
     * Strip off the annoying brackets on the units Fix any units that are not
     * correct with SI
     * 
     * @param description
     * @return
     */
    public String parseUnits(String description) {
    	
        String runit = serviceConfig.getConstantValue("UNKNOWN");
        UnitLookup ul = LookupManager.getInstance().getUnits();

		if (ul != null) {
			// some require no parsing
			UnitConfig uc = ul.getUnitByProviderName(description);

			if (uc != null) {
				// adjusts to correct units
				runit = uc.getName();
			} else {

				Matcher m = getUnitPattern().matcher(description);

				if (m.find()) {
					runit = m.group(2);
					uc = ul.getUnitByProviderName(runit);
					if (uc != null) {
						// adjusts to correct units
						runit = uc.getName();
					}
				}
			}
		}

        return runit;
    }

    /**
     * Remove silly quotes
     * 
     * @param val
     * @return
     */
    public String trim(String val) {

        return QUOTES_PATTERN.matcher(val).replaceAll(
                serviceConfig.getConstantValue("BLANK"));
    }

    

}
