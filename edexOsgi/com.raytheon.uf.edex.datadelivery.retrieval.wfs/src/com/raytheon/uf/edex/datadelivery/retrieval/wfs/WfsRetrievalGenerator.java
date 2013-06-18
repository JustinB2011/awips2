package com.raytheon.uf.edex.datadelivery.retrieval.wfs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.datadelivery.registry.Coverage;
import com.raytheon.uf.common.datadelivery.registry.DataType;
import com.raytheon.uf.common.datadelivery.registry.Parameter;
import com.raytheon.uf.common.datadelivery.registry.PointTime;
import com.raytheon.uf.common.datadelivery.registry.Provider;
import com.raytheon.uf.common.datadelivery.registry.Provider.ServiceType;
import com.raytheon.uf.common.datadelivery.registry.ProviderType;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.datadelivery.registry.SubscriptionBundle;
import com.raytheon.uf.common.datadelivery.retrieval.xml.Retrieval;
import com.raytheon.uf.common.datadelivery.retrieval.xml.RetrievalAttribute;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.datadelivery.retrieval.RetrievalGenerator;
import com.raytheon.uf.edex.datadelivery.retrieval.adapters.RetrievalAdapter;

/**
 * 
 * WFS Retrieval Generator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2012 955        djohnson     Moved to wfs specific package.
 * Nov 19, 2012 1166       djohnson     Clean up JAXB representation of registry objects.
 * May 12, 2013 753        dhladky      Implemented
 * May 31, 2013 2038       djohnson     Move to correct repo.
 * Jun 04, 2013 1763       dhladky      Readied for WFS Retrievals.
 * Jun 18, 2013 2120       dhladky      Fixed times.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */
class WfsRetrievalGenerator extends RetrievalGenerator {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WfsRetrievalGenerator.class);


    WfsRetrievalGenerator(Provider provider) {
        super(ServiceType.WFS);
    }

    @Override
    public List<Retrieval> buildRetrieval(SubscriptionBundle bundle) {

        List<Retrieval> retrievals = Collections.emptyList();
        switch (bundle.getDataType()) {
        case POINT:
            retrievals = getPointRetrievals(bundle);
            break;
        default:
            statusHandler.error("Grid DATA WFS NOT YET IMPLEMENTED");
            throw new IllegalArgumentException(
                    "Grid DATA WFS NOT YET IMPLEMENTED");
        }

        return retrievals;
    }

    /**
     * create the grid type retrievals
     * 
     * @param bundle
     * @return
     */
    private List<Retrieval> getPointRetrievals(SubscriptionBundle bundle) {

        List<Retrieval> retrievals = new ArrayList<Retrieval>();
        Subscription sub = bundle.getSubscription();

        if (sub != null) {

            PointTime subTime = (PointTime) sub.getTime();

            if (sub.getUrl() == null) {
                statusHandler
                        .info("Skipping subscription that is unfulfillable with the current metadata.");
                return Collections.emptyList();
            }

            // with point data they all have the same data
            Parameter param = null;
            
            if (sub.getParameter() != null) {
                param = sub.getParameter().get(0);
            }
                
            Retrieval retrieval = getRetrieval(sub, bundle, param, subTime);
            retrievals.add(retrieval);
        }

        return retrievals;
    }

    /**
     * Get the retrieval
     * 
     * @param sub
     * @param bundle
     * @param param
     * @param level
     * @param Time
     * @return
     */
    private Retrieval getRetrieval(Subscription sub, SubscriptionBundle bundle,
            Parameter param, PointTime time) {

        Retrieval retrieval = new Retrieval();
        retrieval.setSubscriptionName(sub.getName());
        retrieval.setServiceType(getServiceType());
        retrieval.setConnection(bundle.getConnection());
        retrieval.getConnection().setUrl(sub.getUrl());
        retrieval.setOwner(sub.getOwner());
        retrieval.setSubscriptionType(getSubscriptionType(sub));
        retrieval.setNetwork(sub.getRoute());

        // Coverage and type processing
        Coverage cov = sub.getCoverage();

        if (cov instanceof Coverage) {
            retrieval.setDataType(DataType.POINT);
        } else {
            throw new UnsupportedOperationException(
                    "WFS retrieval does not support coverages/types other than Point. ");
        }

        final ProviderType providerType = bundle.getProvider().getProviderType(
                bundle.getDataType());
        final String plugin = providerType.getPlugin();

        // Attribute processing
        RetrievalAttribute att = new RetrievalAttribute();
        att.setCoverage(cov);
        
        if (param != null) {
            
            Parameter lparam = processParameter(param);
            lparam.setLevels(param.getLevels());
            att.setParameter(lparam);
        }
    
        att.setTime(time);
        att.setSubName(retrieval.getSubscriptionName());
        att.setPlugin(plugin);
        att.setProvider(sub.getProvider());
        retrieval.addAttribute(att);

        return retrieval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RetrievalAdapter getServiceRetrievalAdapter() {
        return new WfsRetrievalAdapter();
    }

    @Override
    protected Subscription removeDuplicates(Subscription sub) {
        throw new UnsupportedOperationException("Not implemented for WFS");
    }
   

}
