package com.raytheon.uf.edex.datadelivery.bandwidth.notification;

import java.util.ServiceLoader;

import com.raytheon.uf.common.datadelivery.registry.DataSetMetaData;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.registry.event.RemoveRegistryEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.SubscriptionRetrieval;
import com.raytheon.uf.edex.datadelivery.bandwidth.retrieval.SubscriptionRetrievalFulfilled;

/**
 * Class encapsulating the notification system used by BandwidthManager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 02, 2012 726        jspinks     Initial creation
 * Oct 10, 2012 0726       djohnson    Make buses final.
 * Dec 11, 2012 1286       djohnson    Create a factory to hold Google event buses.
 * Feb 07, 2013 1543       djohnson    Changed to behave similarly to EventBus.
 * Apr 29, 2013 1910       djohnson    Watch for NPEs and errors unregistering.
 * 
 * </pre>
 * 
 * @version 1.0
 */
public class BandwidthEventBus {

    private static final BandwidthEventBusFactory eventBusFactory;
    static {
        eventBusFactory = ServiceLoader
                .<BandwidthEventBusFactory> load(BandwidthEventBusFactory.class)
                .iterator().next();
    }

    private static final com.google.common.eventbus.EventBus dataSetBus = eventBusFactory
            .getDataSetBus();

    private static final com.google.common.eventbus.EventBus subscriptionBus = eventBusFactory
            .getSubscriptionBus();

    private static final com.google.common.eventbus.EventBus retrievalBus = eventBusFactory
            .getRetrievalBus();

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BandwidthEventBus.class);

    private static final String NULL_SUBSCRIBER = "Ignoring a null subscriber.";

    /**
     * Registers an object with the event bus.
     * 
     * @param subscriber
     */
    public static void register(Object subscriber) {
        if (subscriber != null) {
            BandwidthEventBus.retrievalBus.register(subscriber);
            BandwidthEventBus.subscriptionBus.register(subscriber);
            BandwidthEventBus.dataSetBus.register(subscriber);
        } else {
            statusHandler.handle(Priority.WARN, NULL_SUBSCRIBER,
                    new IllegalArgumentException(NULL_SUBSCRIBER));
        }
    }

    /**
     * Unregister an Object with the event bus.
     * 
     * @param subscriber
     */
    public static void unregister(Object subscriber) {
        if (subscriber != null) {
            try {
                BandwidthEventBus.retrievalBus.unregister(subscriber);
            } catch (Throwable t) {
                statusHandler.handle(Priority.WARN,
                        "Unable to unregister subscriber of type ["
                                + subscriber.getClass().getName()
                                + "] from the retrieval event bus!", t);
            }
            try {
                BandwidthEventBus.subscriptionBus.unregister(subscriber);
            } catch (Throwable t) {
                statusHandler.handle(Priority.WARN,
                        "Unable to unregister subscriber of type ["
                                + subscriber.getClass().getName()
                                + "] from the subscription event bus!", t);
            }
            try {
                BandwidthEventBus.dataSetBus.unregister(subscriber);
            } catch (Throwable t) {
                statusHandler.handle(Priority.WARN,
                        "Unable to unregister subscriber of type ["
                                + subscriber.getClass().getName()
                                + "] from the dataSet event bus!", t);
            }
        } else {
            statusHandler.handle(Priority.WARN, NULL_SUBSCRIBER,
                    new IllegalArgumentException(NULL_SUBSCRIBER));
        }
    }

    /**
     * Publishes events for all subscribers to receive
     * 
     * @param event
     */
    public static void publish(Object object) {
        if (object instanceof SubscriptionRetrieval) {
            BandwidthEventBus.retrievalBus.post(object);
        } else if (object instanceof SubscriptionRetrievalFulfilled) {
            BandwidthEventBus.subscriptionBus.post(object);
        } else if (object instanceof DataSetMetaData) {
            BandwidthEventBus.dataSetBus.post(object);
        } else if (object instanceof Subscription) {
            BandwidthEventBus.subscriptionBus.post(object);
        } else if (object instanceof RemoveRegistryEvent) {
            BandwidthEventBus.subscriptionBus.post(object);
        } else {
            throw new IllegalArgumentException("Object type ["
                    + object.getClass().getName()
                    + "] not supported in BandwidthEventBus");
        }
    }

}
