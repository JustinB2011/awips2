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
package com.raytheon.uf.viz.collaboration.comm.provider.session;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Base64;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.viz.collaboration.comm.identity.CollaborationException;
import com.raytheon.uf.viz.collaboration.comm.identity.ISession;
import com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher;
import com.raytheon.uf.viz.collaboration.comm.identity.user.IQualifiedID;

/**
 * TODO Add Description
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 21, 2012            jkorman     Initial creation
 *
 * </pre>
 *
 * @author jkorman
 * @version 1.0	
 */

public abstract class BaseSession implements ISession, IEventPublisher {

    protected final String sessionId;
    
    private EventBus managerEventBus;

    private EventBus eventBus;
    
    private Map<Object, Object> eventSubscribers;

    private IContainer connectionContainer;

    private IPresenceContainerAdapter connectionPresence = null;
    
    private Namespace connectionNamespace = null;

    // The session manager that created this session.
    private SessionManager sessionManager;

    /**
     * 
     * @param container
     * @param externalBus
     * @param manager
     */
    protected BaseSession(IContainer container, EventBus externalBus, SessionManager manager) {
        // Set the session identifier.
        sessionId = UUID.randomUUID().toString();
        managerEventBus = externalBus;
        eventBus = new EventBus();
        connectionContainer = container;
        sessionManager = manager;
        eventSubscribers = new HashMap<Object,Object>();
        
    }
    
    /**
     * 
     * @throws ECFException
     */
    void setup() throws ECFException {
        // Check if the container has been set up previously.
        if (connectionContainer != null) {
            
            connectionNamespace = connectionContainer.getConnectNamespace();
            connectionPresence = (IPresenceContainerAdapter) connectionContainer
                    .getAdapter(IPresenceContainerAdapter.class);
        } else {

        }
    }
    
    /**
     * Get access to the peer to peer session instance.
     * @return The peer to peer chat session instance.
     * @throws CollaborationException
     */
    PeerToPeerChat getP2PSession() throws CollaborationException {
        return (PeerToPeerChat) sessionManager.getPeerToPeerSession();
    }
    
    /**
     * 
     * @return
     */
    IContainer getConnectionContainer() {
        return connectionContainer;
    }

    /**
     * 
     * @return
     */
    Namespace getConnectionNamespace() {
        return connectionNamespace;
    }

    /**
     * 
     * @return
     */
    IPresenceContainerAdapter getConnectionPresenceAdapter() {
        return connectionPresence;
    }

    
    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.ISession#getUserID()
     */
    @Override
    public IQualifiedID getUserID() {
        return null;
    }

    /**
     * 
     * @see com.raytheon.uf.viz.collaboration.comm.identity.ISession#isConnected()
     */
    @Override
    public boolean isConnected() {
        boolean connected = false;
        if(connectionContainer != null) {
            connected = (connectionContainer.getConnectedID() != null);
        }
        return connected;
    }

    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.ISession#close()
     */
    @Override
    public void close() {

        // Unregister any handlers added using this session
        for(Object o : eventSubscribers.values()) {
            managerEventBus.unregister(o);
        }
        sessionManager.removeSession(this);
    }

    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.ISession#getSessionId()
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    //*****************
    // Implement IEventPublisher methods
    //*****************

    /**
     * 
     * @param handler
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher#registerEventHandler(java.lang.Object)
     */
    @Override
    public void registerEventHandler(Object handler) {
        if(!eventSubscribers.containsKey(handler)) {
            eventBus.register(handler);
        }
    }
    
    /**
     * 
     * @param handler
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher#unRegisterEventHandler(java.lang.Object)
     */
    @Override
    public void unRegisterEventHandler(Object handler) {
        eventSubscribers.remove(handler);
        eventBus.unregister(handler);
    }

    /**
     * 
     */
    @Override
    public EventBus getEventPublisher() {
        return eventBus;
    }
    
    /**
     * 
     * @param name
     * @return
     */
    public ID createID(String name) throws IDCreateException {
        ID id = null;
        if(connectionNamespace != null) {
            id = IDFactory.getDefault().createID(connectionNamespace, name);           
        }
        return id;
    }
}
