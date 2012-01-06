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
package com.raytheon.uf.edex.esb.camel.directvm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.Endpoint;
import org.apache.camel.component.direct.DirectComponent;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ServiceHelper;

/**
 * Provides a cross-context synchronous component
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2008            chammack     Initial creation
 * 
 * </pre>
 * 
 * @author chammack
 * @version 1.0
 */

@SuppressWarnings("unchecked")
public class DirectVMComponent extends DirectComponent {

    private static Map<String, CopyOnWriteArrayList<DefaultConsumer>> CONSUMERS = new HashMap<String, CopyOnWriteArrayList<DefaultConsumer>>();

    @Override
    protected Endpoint createEndpoint(String uri, String remaining,
            Map parameters) throws Exception {

        synchronized (CONSUMERS) {

            CopyOnWriteArrayList<DefaultConsumer> consumers = CONSUMERS
                    .get(uri);

            if (consumers == null) {
                consumers = new CopyOnWriteArrayList<DefaultConsumer>();
                CONSUMERS.put(uri, consumers);

            }

            Endpoint endpoint = new DirectVMEndpoint(uri, this, consumers);
            setProperties(endpoint, parameters);
            return endpoint;
        }

    }

    @Override
    protected void doStop() throws Exception {
        Collection<CopyOnWriteArrayList<DefaultConsumer>> set = CONSUMERS
                .values();
        for (CopyOnWriteArrayList<DefaultConsumer> consumerList : set) {
            ServiceHelper.stopService(consumerList);
        }

        CONSUMERS.clear();
        super.doStop();
    }
}
