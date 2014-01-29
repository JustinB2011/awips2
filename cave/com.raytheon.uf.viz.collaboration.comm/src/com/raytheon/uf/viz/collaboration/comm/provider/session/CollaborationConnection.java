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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.google.common.eventbus.EventBus;
import com.google.common.net.HostAndPort;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.collaboration.comm.identity.CollaborationException;
import com.raytheon.uf.viz.collaboration.comm.identity.IAccountManager;
import com.raytheon.uf.viz.collaboration.comm.identity.ISession;
import com.raytheon.uf.viz.collaboration.comm.identity.ISharedDisplaySession;
import com.raytheon.uf.viz.collaboration.comm.identity.IVenueSession;
import com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher;
import com.raytheon.uf.viz.collaboration.comm.identity.event.IRosterChangeEvent;
import com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent;
import com.raytheon.uf.viz.collaboration.comm.identity.event.RosterChangeType;
import com.raytheon.uf.viz.collaboration.comm.identity.invite.SharedDisplayVenueInvite;
import com.raytheon.uf.viz.collaboration.comm.identity.invite.VenueInvite;
import com.raytheon.uf.viz.collaboration.comm.identity.user.IQualifiedID;
import com.raytheon.uf.viz.collaboration.comm.provider.SessionPayload;
import com.raytheon.uf.viz.collaboration.comm.provider.SessionPayload.PayloadType;
import com.raytheon.uf.viz.collaboration.comm.provider.SessionPayloadProvider;
import com.raytheon.uf.viz.collaboration.comm.provider.Tools;
import com.raytheon.uf.viz.collaboration.comm.provider.event.RosterChangeEvent;
import com.raytheon.uf.viz.collaboration.comm.provider.event.ServerDisconnectEvent;
import com.raytheon.uf.viz.collaboration.comm.provider.event.VenueInvitationEvent;
import com.raytheon.uf.viz.collaboration.comm.provider.user.ContactsManager;
import com.raytheon.uf.viz.collaboration.comm.provider.user.IDConverter;
import com.raytheon.uf.viz.collaboration.comm.provider.user.UserId;
import com.raytheon.uf.viz.collaboration.comm.provider.user.UserSearch;
import com.raytheon.uf.viz.collaboration.comm.provider.user.VenueId;

/**
 * 
 * <ul>
 * <li>EventBus subscription events.</li>
 * <ul>
 * <li><strong>IVenueInvitationEvent</strong> : This event is posted when the
 * SessionManager receives a venue invitation requesting that the user join some
 * particular collaboration session.</li>
 * <li><strong>IConnectionStatusEvent</strong> : This event is posted when the
 * state of the underlying connection changes, reconnecting, connecting,
 * disconnected, for example.</li>
 * <li><strong>IRosterChangeEvent</strong> : This event is posted when roster
 * changes have occurred.</li>
 * <li><strong>---------------</strong> : ---------------.</li>
 * </ul>
 * </ul>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 24, 2012            jkorman     Initial creation
 * Apr 18, 2012            njensen      Major cleanup
 * Dec  6, 2013 2561       bclement    removed ECF
 * Dec 18, 2013 2562       bclement    added smack compression, fixed invite parsing
 * Dec 19, 2013 2563       bclement    added connection listener, 
 *                                     added better error message on failed connection
 * Jan 07, 2013 2563       bclement    use getServiceName instead of getHost when creating room id
 * Jan 08, 2014 2563       bclement    fixed custom port and service name in user id
 * Jan 15, 2014 2630       bclement    connection data stores status as Mode object
 * Jan 24, 2014 2701       bclement    removed roster manager
 * Jan 28, 2014 2698       bclement    fixed compression default
 *                                     cleaned up createCollaborationVenue, removed getVenueInfo
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent
 */

public class CollaborationConnection implements IEventPublisher {

    static {
        ProviderManager.getInstance().addExtensionProvider(
                SessionPayload.ELEMENT_NAME, SessionPayload.XMLNS,
                new SessionPayloadProvider());
    }

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(CollaborationConnection.class);

    private static CollaborationConnection instance = null;

    private static Map<CollaborationConnectionData, CollaborationConnection> instanceMap = new HashMap<CollaborationConnectionData, CollaborationConnection>();

    private Map<String, ISession> sessions;

    private UserId user;

    private Presence userPresence;

    private PeerToPeerChat chatInstance = null;

    private IAccountManager accountManager = null;

    private EventBus eventBus;

    private ContactsManager contactsMgr;

    private CollaborationConnectionData connectionData;

    private XMPPConnection connection;

    public static boolean COMPRESS = true;

    static {
        try {
            final String compressionProperty = "collaboration.compression";
            if (System.getProperty(compressionProperty) != null) {
                COMPRESS = Boolean.getBoolean(compressionProperty);
            }
        } catch (Exception e) {
            // must not have permission to access system properties. ignore and
            // use default.
        }
    }

    private CollaborationConnection(CollaborationConnectionData connectionData)
            throws CollaborationException {
        this.connectionData = connectionData;
        String password = connectionData.getPassword();
        Mode mode = connectionData.getStatus();
        if (mode == null) {
            mode = Mode.available;
        }
        Presence initialPresence = new Presence(Type.available,
                connectionData.getMessage(), 0, mode);
        Tools.setProperties(initialPresence, connectionData.getAttributes());

        eventBus = new EventBus();
        sessions = new HashMap<String, ISession>();

        HostAndPort hnp = HostAndPort.fromString(connectionData.getServer());
        ConnectionConfiguration conConfig;
        if (hnp.hasPort()) {
            conConfig = new ConnectionConfiguration(hnp.getHostText(),
                    hnp.getPort());
        } else {
            conConfig = new ConnectionConfiguration(hnp.getHostText());
        }

        conConfig.setCompressionEnabled(COMPRESS);

        connection = new XMPPConnection(conConfig);
        
        connectInternal(connectionData.getUserName(), password);

        this.user = new UserId(connectionData.getUserName(),
                connection.getServiceName());

        setupConnectionListener();
        setupAccountManager();
        setupInternalConnectionListeners();
        setupInternalVenueInvitationListener();
        setupP2PComm();
        getPeerToPeerSession();

        userPresence = initialPresence;
        if (accountManager != null && initialPresence != null) {
            accountManager.sendPresence(initialPresence);
        }

        contactsMgr = new ContactsManager(this, connection);
        this.registerEventHandler(contactsMgr);

        instanceMap.put(connectionData, this);
        if (instance == null) {
            instance = this;
        }
    }

    /**
     * connect to XMPP server and login
     * 
     * @param username
     * @param password
     * @throws CollaborationException
     */
    private void connectInternal(String username, String password)
            throws CollaborationException {
        try {
            connection.connect();
            connection.login(username, password);
        } catch (XMPPException e) {
            closeInternals();
            // get a nice reason for the user
            String msg;
            XMPPError xmppErr = e.getXMPPError();
            if (xmppErr != null) {
                switch (xmppErr.getCode()) {
                case 401:
                    msg = "Bad username or password";
                    break;
                case 403:
                    msg = "User not allowed to connect to server";
                    break;
                case 409:
                    msg = "User account already in use by another client";
                    break;
                default:
                    msg = e.getLocalizedMessage();
                }
            } else {
                msg = e.getLocalizedMessage();
            }
            throw new CollaborationException("Login failed: " + msg, e);
        }
    }

    public CollaborationConnectionData getConnectionData() {
        return connectionData;
    }

    /**
     * @return
     * @see com.raytheon.uf.viz.collaboration.comm.identity.roster.IRoster#getUser()
     */
    public UserId getUser() {
        return user;
    }

    /**
     * 
     * @return
     */
    public Presence getPresence() {
        return userPresence;
    }

    /**
     * 
     * @return
     */
    public void setPresence(Presence presence) {
        userPresence = presence;
    }

    /**
     * 
     */
    private void setupAccountManager() {
        if (accountManager == null) {
            if (isConnected()) {
                accountManager = new AccountManager(this);
            }
        }
    }

    /**
     * Get the account manager for this connection.
     * 
     * @return The account manager for this connection.
     */
    public IAccountManager getAccountManager() {
        if (accountManager == null) {
            setupAccountManager();
        }
        return accountManager;
    }

    /**
     * Is this SessionManager currently connected?
     * 
     * @return Is this SessionManager currently connected?
     */
    public boolean isConnected() {
        return ((connection != null) && (connection.getConnectionID() != null));
    }

    private void closeInternals() {
        if (connection != null) {

            chatInstance = null;
            // Get rid of the account and roster managers
            connection.disconnect();
            connection = null;
        }
        PeerToPeerCommHelper.reset();
        instanceMap.remove(connectionData);
        if (this == instance) {
            instance = null;
        }
    }

    /**
     *  
     */
    public void close() {
        if (connection != null) {
            // Close any created sessions.
            Collection<ISession> toRemove = sessions.values();
            sessions.clear();
            for (ISession session : toRemove) {
                if ((chatInstance != null) && chatInstance.equals(session)) {
                    chatInstance.close();
                    chatInstance = null;
                } else {
                    session.close();
                }
            }
            chatInstance = null;
        }
        closeInternals();
    }

    /**
     * Get the PeerToPeerChat session instance.
     * 
     * @return
     */
    public ISession getPeerToPeerSession() throws CollaborationException {
        if (chatInstance == null) {
            chatInstance = new PeerToPeerChat(eventBus, this);
            sessions.put(chatInstance.getSessionId(), chatInstance);
            postEvent(chatInstance);
        }
        return chatInstance;
    }

    public ISharedDisplaySession joinCollaborationVenue(
            IVenueInvitationEvent invitation) throws CollaborationException {
        String venueName = invitation.getRoomId().getName();
        String sessionId = invitation.getInvite().getSessionId();
        SharedDisplaySession session = new SharedDisplaySession(eventBus, this,
                sessionId);
        session.configureVenue(venueName);

        if (invitation.getInvite() instanceof SharedDisplayVenueInvite) {
            SharedDisplayVenueInvite invite = (SharedDisplayVenueInvite) invitation
                    .getInvite();
            session.setCurrentDataProvider(invite.getDataProvider());
            session.setCurrentSessionLeader(invite.getSessionLeader());
        }

        sessions.put(session.getSessionId(), session);
        postEvent(session);
        return session;
    }

    /**
     * 
     * @param venueName
     * @return
     * @throws CollaborationException
     */
    public ISharedDisplaySession createCollaborationVenue(String venueName,
            String subject) throws CollaborationException {
        SharedDisplaySession session = null;
        session = new SharedDisplaySession(eventBus, this);

        session.createVenue(venueName, subject);
        session.setCurrentSessionLeader(user);
        session.setCurrentDataProvider(user);

        sessions.put(session.getSessionId(), session);
        postEvent(session);
        return session;
    }

    /**
     * 
     * @param venueName
     * @return
     * @throws CollaborationException
     */
    public IVenueSession joinTextOnlyVenue(String venueName)
            throws CollaborationException {
        try {
            VenueSession session = new VenueSession(eventBus, this);
            session.configureVenue(venueName);
            sessions.put(session.getSessionId(), session);
            postEvent(session);
            return session;
        } catch (Exception e) {
            throw new CollaborationException(
                    "Error joining venue " + venueName, e);
        }
    }

    /**
     * Check if venue exists on server
     * 
     * @param venueName
     * @return false on error
     */
    public boolean venueExistsOnServer(String venueName) {
        String roomId = VenueSession.getRoomId(connection.getServiceName(),
                venueName);
        try {
            return VenueSession.roomExistsOnServer(connection, roomId);
        } catch (XMPPException e) {
            statusHandler.error("Unable to check for room on server", e);
            return false;
        }
    }

    /**
     * 
     * @param venueName
     * @return
     * @throws CollaborationException
     */
    public IVenueSession createTextOnlyVenue(String venueName, String subject)
            throws CollaborationException {
        VenueSession session = new VenueSession(eventBus, this);
        session.createVenue(venueName, subject);
        sessions.put(session.getSessionId(), session);
        postEvent(session);
        return session;
    }

    /**
     * 
     * @param session
     */
    protected void removeSession(ISession session) {
        sessions.remove(session.getSessionId());
        postEvent(session);
    }

    // ***************************
    // Connection listener
    // ***************************

    /**
     * 
     */
    private void setupInternalConnectionListeners() {
        final Roster roster = connection.getRoster();
        roster.addRosterListener(new RosterListener() {
            
            @Override
            public void presenceChanged(Presence presence) {
                String fromId = presence.getFrom();
                if (contactsMgr != null) {
                    UserId u = IDConverter.convertFrom(fromId);
                    if (u != null) {
                        RosterEntry entry = contactsMgr
                                .getRosterEntry(u);
                        eventBus.post(entry);
                        IRosterChangeEvent event = new RosterChangeEvent(
                                RosterChangeType.MODIFY, entry);
                        eventBus.post(event);
                    }
                }
            }
            
            @Override
            public void entriesUpdated(Collection<String> addresses) {
                send(addresses, RosterChangeType.MODIFY);
            }
            
            @Override
            public void entriesDeleted(Collection<String> addresses) {
                send(addresses, RosterChangeType.DELETE);
            }
            
            @Override
            public void entriesAdded(Collection<String> addresses) {
                send(addresses, RosterChangeType.ADD);
            }

            /**
             * Send event bus notification for roster
             * 
             * @param addresses
             * @param type
             */
            private void send(Collection<String> addresses,
                    RosterChangeType type) {
                for (String addy : addresses) {
                    RosterEntry entry = roster.getEntry(addy);
                    if (entry != null) {
                        IRosterChangeEvent event = new RosterChangeEvent(type,
                                entry);
                        eventBus.post(event);
                    }
                }
            }
        });
    }

    public ISession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private void setupP2PComm() {
        if (isConnected()) {
            PeerToPeerCommHelper helper = new PeerToPeerCommHelper(this);
            connection.addPacketListener(helper, new PacketTypeFilter(
                    Message.class));
        }
    }

    private void setupConnectionListener(){
        if (isConnected()){
            connection.addConnectionListener(new ConnectionListener() {
                
                @Override
                public void reconnectionSuccessful() {
                    statusHandler.debug("Client successfully reconnected to server");
                }
                
                @Override
                public void reconnectionFailed(Exception e) {
                    String reason = getErrorReason(e);
                    statusHandler.error("Client can't reconnect to server: "
                            + reason, e);
                    sendDisconnectNotice(reason);
                }
                
                @Override
                public void reconnectingIn(int seconds) {
                    statusHandler.debug("Client reconnecting to server in " + seconds + " seconds" );
                }
                
                @Override
                public void connectionClosedOnError(Exception e) {
                    String reason = getErrorReason(e);
                    statusHandler.error("Server closed on error: " + reason, e);
                    sendDisconnectNotice(reason);
                }

                private String getErrorReason(Exception e) {
                    String msg = null;
                    if (e instanceof XMPPException) {
                        StreamError streamError = ((XMPPException) e)
                                .getStreamError();
                        if (streamError != null) {
                            if ("conflict".equalsIgnoreCase(streamError
                                    .getCode())) {
                                msg = "User account in use on another client";
                            }
                        }
                    }
                    return msg == null ? e.getLocalizedMessage() : msg;
                }
                
                @Override
                public void connectionClosed() {
                    statusHandler.info("Server closed connection");
                    sendDisconnectNotice("Normal termination");
                }

                private void sendDisconnectNotice(String reason) {
                    ServerDisconnectEvent event = new ServerDisconnectEvent(
                            reason);
                    eventBus.post(event);
                }
            });
        }
    }

    // ***************************
    // Venue invitation listener management
    // ***************************

    /**
     * 
     */
    private void setupInternalVenueInvitationListener() {
        if (isConnected()) {
            MultiUserChat.addInvitationListener(connection,
                    new InvitationListener() {
                        @Override
                        public void invitationReceived(Connection conn,
                                String room, String inviter, String reason,
                                String password, Message message) {
                            // TODO handle password protected rooms
                            IQualifiedID venueId = new VenueId();
                            venueId.setName(Tools.parseName(room));
                            UserId invitor = IDConverter.convertFrom(inviter);

                            if (message != null) {
                                SessionPayload payload = (SessionPayload) message
                                        .getExtension(SessionPayload.XMLNS);
                                if (payload != null) {
                                    handleCollabInvite(venueId, invitor,
                                            payload);
                                    return;
                                }
                            }
                            if (reason != null
                                    && reason.startsWith(Tools.CMD_PREAMBLE)) {
                                reason = "Shared display invitation from incompatible version of CAVE. "
                                        + "Session will be chat-only if invitation is accepted";
                            }
                            handleChatRoomInvite(venueId, invitor, reason,
                                    message);
                        }
                    });
        }
    }

    private void handleChatRoomInvite(IQualifiedID venueId, UserId invitor,
            String reason, Message message) {
        VenueInvite invite = new VenueInvite();
        if (!StringUtils.isBlank(reason)) {
            invite.setMessage(reason);
        } else if (!StringUtils.isBlank(message.getBody())) {
            invite.setMessage(message.getBody());
        } else {
            invite.setMessage("");
        }
        invite.setSubject(message.getSubject());
        IVenueInvitationEvent event = new VenueInvitationEvent(venueId,
                invitor, invite);
        eventBus.post(event);
    }

    private void handleCollabInvite(IQualifiedID venueId, UserId invitor,
            SessionPayload payload) {
        Object obj = payload.getData();
        if (obj == null
                || !payload.getPayloadType().equals(PayloadType.Invitation)
                || !(obj instanceof VenueInvite)) {
            statusHandler.warn("Received unsupported invite payload");
            return;
        }
        VenueInvite invite = (VenueInvite) obj;
        IVenueInvitationEvent event = new VenueInvitationEvent(venueId,
                invitor, invite);
        eventBus.post(event);
    }

    /**
     * Register an event handler with this
     * 
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher#registerEventHandler(java.lang.Object)
     */
    @Override
    public void registerEventHandler(Object handler) {
        eventBus.register(handler);
    }

    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IEventPublisher#unregisterEventHandler(java.lang.Object)
     */
    @Override
    public void unregisterEventHandler(Object handler) {
        eventBus.unregister(handler);
    }

    @Override
    public void postEvent(Object event) {
        if (event != null) {
            eventBus.post(event);
        }
    }

    public ContactsManager getContactsManager() {
        return contactsMgr;
    }

    public Collection<ISession> getSessions() {
        return sessions.values();
    }

    /**
     * Returns the currently connected connection or null if it's not connected
     * 
     * @return
     */
    public static CollaborationConnection getConnection() {
        return instance;
    }

    /**
     * Create a {@link CollaborationConnection} given the
     * {@link CollaborationConnectionData}
     * 
     * @param userData
     * @return
     * @throws CollaborationException
     */
    public static CollaborationConnection connect(
            CollaborationConnectionData userData) throws CollaborationException {
        if (instance != null) {
            throw new CollaborationException("Already connected");
        } else {
            instance = new CollaborationConnection(userData);
            return getConnection();
        }
    }

    protected XMPPConnection getXmppConnection() {
        return connection;
    }

    public UserSearch createSearch() {
        return new UserSearch(connection);
    }

}
