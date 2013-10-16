package com.raytheon.uf.edex.datadelivery.retrieval.adhoc;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.datadelivery.registry.AdhocSubscription;
import com.raytheon.uf.common.datadelivery.registry.Provider;
import com.raytheon.uf.common.datadelivery.registry.ProviderType;
import com.raytheon.uf.common.datadelivery.registry.handlers.DataDeliveryHandlers;
import com.raytheon.uf.common.datadelivery.registry.handlers.IAdhocSubscriptionHandler;
import com.raytheon.uf.common.datadelivery.registry.handlers.IProviderHandler;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.purge.PurgeRule;
import com.raytheon.uf.edex.database.purge.PurgeRuleSet;

/**
 * Dumps expired Adhoc Subscriptions based on diff between end and expiration
 * time in purge rules.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 11, 2013  2460      dhladky     Initial creation
 * 
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */

public class AdhocSubscriptionCleaner {
    
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AdhocSubscriptionCleaner.class);
        
    public AdhocSubscriptionCleaner() {

    }
    
    /**
     * Cleans old adhoc subscriptions from the WFO registry.
     * Compares the end() time on the sub to the expiration period in 
     * associated purge rules derived from the plugin each subs data 
     * is stored too. Runs on every 20 minutes.
     */
    public void processSubscriptions() {
       
        statusHandler.handle(Priority.INFO, "Processing Adhoc Subscriptions for expiration...");
        List<AdhocSubscription> adhocs = null;
        final IAdhocSubscriptionHandler adhocSubHandler = DataDeliveryHandlers.getAdhocSubscriptionHandler();
        
        try {
            adhocs = adhocSubHandler.getAll();
            
        } catch (RegistryHandlerException e) {
            statusHandler.handle(Priority.ERROR, "Can't load list of adhoc subscriptions from registry!", e);
        }
        
        if (adhocs != null && !adhocs.isEmpty()) {
            
            Map<String, Provider> providers = new HashMap<String, Provider>();
            Map<String, PurgeRuleSet> purgeRules = new HashMap<String, PurgeRuleSet>();
            List<AdhocSubscription> subsToDelete = new ArrayList<AdhocSubscription>();
            
            // loop over the adhoc subscriptions we have
            for (AdhocSubscription adhoc: adhocs) {
                
                String providerName = adhoc.getProvider();
                Provider provider = null;
                
                // speed, only has to lookup each provider once
                if (!providers.containsKey(providerName)) {
                
                    final IProviderHandler providerHandler = DataDeliveryHandlers.getProviderHandler();

                    try {
                        provider = providerHandler.getByName(providerName);
                        providers.put(providerName, provider);
                        
                    } catch (RegistryHandlerException e) {
                        statusHandler.handle(Priority.ERROR, "Can't load provider from registry! "+providerName, e);
                    }
                } else {
                    // pull a cached provider
                    if (providerName != null) {
                        provider = providers.get(providerName);
                    }
                }
                
                List<String> plugins = new ArrayList<String>();

                if (provider != null) {
                    // find the actual plugin
                    for (ProviderType type : provider.getProviderType()) {
                        plugins.add(type.getPlugin());
                    }
                }
                
                // extract the purge rules from localization, place in map for speed
                PurgeRuleSet purgeRuleSet = null;

                if (plugins != null && !plugins.isEmpty()) {
                    // multiple plugins for providers on occasion
                    for (String plugin : plugins) {
                        
                        if (!purgeRules.containsKey(plugin)) {

                            purgeRuleSet = PluginDao
                                    .getPurgeRulesForPlugin(plugin);
                            purgeRules.put(plugin, purgeRuleSet);
                            
                        } else {
                            // pull a cached purgeRuleSet
                            if (plugin != null) {
                                purgeRuleSet = purgeRules.get(plugin);
                            }
                        }

                        if (purgeRuleSet != null) {
                            
                            List<PurgeRule> rules = purgeRuleSet.getRules();

                            // Go over all of the purge rules for each sub
                            for (PurgeRule rule: rules) {
                                // use current system date/time
                                long timeCheck = TimeUtil.newDate().getTime();
                                Date expireDate = new Date(timeCheck + rule.getPeriodInMillis());
                                
                                // check rule against creation time of this sub
                                if (adhoc.getTime().getEnd().before(expireDate)) {
                                    if (!subsToDelete.contains(adhoc)) {
                                        subsToDelete.add(adhoc);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // get rid of old subscriptions
            if (subsToDelete != null && !subsToDelete.isEmpty()) {

                try {
                    adhocSubHandler.delete(subsToDelete);
                    statusHandler.handle(Priority.INFO, "Deleted "
                            + subsToDelete.size()
                            + " expired adhocSubscriptions.");
                } catch (RegistryHandlerException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Failed to delete expired adhoc subscriptions.", e);
                }
            }
        }
    }

}
