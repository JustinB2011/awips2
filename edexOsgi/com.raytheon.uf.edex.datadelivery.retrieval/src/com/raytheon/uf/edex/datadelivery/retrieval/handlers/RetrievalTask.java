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
package com.raytheon.uf.edex.datadelivery.retrieval.handlers;

import com.raytheon.uf.common.datadelivery.registry.Network;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.datadelivery.retrieval.db.RetrievalRequestRecord;
import com.raytheon.uf.edex.datadelivery.retrieval.db.RetrievalRequestRecord.State;

/**
 * Inner class to process individual retrievals.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 07, 2011            dhladky      Initial creation
 * Aug 15, 2012 1022       djohnson     Moved from inner to class proper.
 * Aug 22, 2012 0743       djohnson     Continue processing retrievals until there are no more.
 * Nov 19, 2012 1166       djohnson     Clean up JAXB representation of registry objects.
 * Jan 30, 2013 1543       djohnson     Constrain to the network retrievals are pulled for.
 * 
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */
public class RetrievalTask implements Runnable {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RetrievalTask.class);

    private final Network network;

    private final IRetrievalPluginDataObjectsProcessor retrievedDataProcessor;

    private final IRetrievalResponseCompleter retrievalCompleter;

    private final IRetrievalPluginDataObjectsFinder retrievalDataFinder;

    public RetrievalTask(Network network,
            IRetrievalPluginDataObjectsFinder retrievalDataFinder,
            IRetrievalPluginDataObjectsProcessor retrievedDataProcessor,
            IRetrievalResponseCompleter retrievalCompleter) {
        this.network = network;
        this.retrievalDataFinder = retrievalDataFinder;
        this.retrievedDataProcessor = retrievedDataProcessor;
        this.retrievalCompleter = retrievalCompleter;
    }

    @Override
    public void run() {
        try {
            while (true) {

                // process request
                boolean success = false;
                RetrievalRequestRecord request = null;
                try {

                    RetrievalPluginDataObjects retrievalPluginDataObject = retrievalDataFinder
                            .findRetrievalPluginDataObjects();
                    // This forces the return from the while loop once there are
                    // no more retrievals to process
                    if (retrievalPluginDataObject == null) {
                        statusHandler.info("No " + network
                                + " retrievals found.");
                        return;
                    }

                    request = retrievalPluginDataObject.getRequestRecord();
                    success = (request.getState() == State.COMPLETED);
                    retrievedDataProcessor
                            .processRetrievedPluginDataObjects(retrievalPluginDataObject);
                } catch (Exception e) {
                    statusHandler.error(
                            network + " retrieval processing error", e);
                }

                if (request != null) {
                    retrievalCompleter.completeRetrieval(request,
                            new RetrievalResponseStatus(success));
                }
            }
        } catch (Throwable e) {
            // so thread can't die
            statusHandler.error("Error caught in " + network
                    + " retrieval thread", e);
        }
    }
}
