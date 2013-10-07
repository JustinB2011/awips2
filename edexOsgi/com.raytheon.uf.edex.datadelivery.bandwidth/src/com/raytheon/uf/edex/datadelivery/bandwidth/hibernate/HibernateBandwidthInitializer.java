package com.raytheon.uf.edex.datadelivery.bandwidth.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.edex.site.SiteUtil;
import com.raytheon.uf.common.datadelivery.registry.SharedSubscription;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.datadelivery.bandwidth.IBandwidthManager;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.BandwidthAllocation;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.IBandwidthDbInit;
import com.raytheon.uf.edex.datadelivery.bandwidth.interfaces.BandwidthInitializer;
import com.raytheon.uf.edex.datadelivery.bandwidth.retrieval.RetrievalManager;

/**
 * 
 * {@link BandwidthInitializer} that uses Hibernate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2013 1543       djohnson     Add SW history, separate how to find subscriptions.
 * Apr 16, 2013 1906       djohnson     Implements RegistryInitializedListener.
 * Apr 30, 2013 1960       djohnson     just call init rather than drop/create tables explicitly.
 * Jun 25, 2013 2106       djohnson     init() now takes a {@link RetrievalManager} as well.
 * Sep 05, 2013 2330       bgonzale     On WFO registry init, only subscribe to local site subscriptions.
 * Sep 06, 2013 2344       bgonzale     Removed attempt to add to immutable empty set.
 * Oct 07, 2013 2267       bgonzale     in executeAfterRegistryInit NCF schedules shared subs.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */
public class HibernateBandwidthInitializer implements BandwidthInitializer {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HibernateBandwidthInitializer.class);

    private final IFindSubscriptionsForScheduling findSubscriptionsStrategy;

    private IBandwidthManager instance;

    /**
     * @param strategy
     */
    public HibernateBandwidthInitializer(
            IFindSubscriptionsForScheduling findSubscriptionsStrategy) {
        this.findSubscriptionsStrategy = findSubscriptionsStrategy;
    }

    @Override
    public boolean init(IBandwidthManager instance, IBandwidthDbInit dbInit,
            RetrievalManager retrievalManager) {

        this.instance = instance;

        // TODO: Need to resolve how to load Subscriptions that SHOULD have been
        // fulfilled. In the case were DD has been down for a while
        // BEFORE removing the tables...

        try {
            dbInit.init();
        } catch (Exception e1) {
            throw new RuntimeException(
                    "Error generating bandwidth manager tables", e1);
        }

        retrievalManager.initRetrievalPlans();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeAfterRegistryInit() {
        Set<Subscription> activeSubscriptions = new HashSet<Subscription>();
        try {
            final String localOffice = SiteUtil.getSite();
            final boolean isCentralRegistry = System.getProperty(
                    "edex.run.mode").equals("centralRegistry");

            // Load active subscriptions
            for (Subscription sub : findSubscriptionsStrategy
                    .findSubscriptionsToSchedule()) {
                boolean isShared = (sub instanceof SharedSubscription);
                boolean isLocalOffice = sub.getOfficeIDs()
                        .contains(localOffice);

                if ((isCentralRegistry && isShared)
                        || (!isShared && isLocalOffice)) {
                    activeSubscriptions.add(sub);
                    statusHandler.info("Scheduling Subscription: " + sub);
                } else {
                    statusHandler.info("Not Scheduling Subscription: " + sub);
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to query for subscriptions to schedule", e);
        }

        List<BandwidthAllocation> unscheduled = new ArrayList<BandwidthAllocation>();

        for (Subscription subscription : activeSubscriptions) {
            // Make sure the Id is set properly..
            subscription.setId(RegistryUtil.getRegistryObjectKey(subscription));
            statusHandler.info("init() - Loading subscription ["
                    + subscription.getName() + "]");
            unscheduled.addAll(instance.schedule(subscription));

            for (BandwidthAllocation allocation : unscheduled) {
                statusHandler.handle(Priority.PROBLEM,
                        "The following bandwidth allocation is in an unscheduled state:\n   "
                                + allocation);
            }
        }
    }
}
