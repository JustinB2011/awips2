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
package com.raytheon.uf.common.registry.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.Cataloger;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.LifecycleManager;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.MsgRegistryException;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.NotificationListener;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.QueryManager;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.Validator;
import oasis.names.tc.ebxml.regrep.xsd.lcm.v4.SubmitObjectsRequest;
import oasis.names.tc.ebxml.regrep.xsd.rs.v4.RegistryExceptionType;
import oasis.names.tc.ebxml.regrep.xsd.rs.v4.RegistryResponseStatus;
import oasis.names.tc.ebxml.regrep.xsd.rs.v4.RegistryResponseType;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.raytheon.uf.common.comm.ProxyConfiguration;
import com.raytheon.uf.common.comm.ProxyUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Convenience class used for accessing the registry soap services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 4/9/2013     1802        bphillip    Initial implementation
 * Apr 24, 2013 1910        djohnson    RegistryResponseStatus is now an enum.
 * 8/28/2013    1538        bphillip    Removed caches, add http client preferences
 * 9/5/2013     1538        bphillip    Add HTTP header information
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class RegistrySOAPServices {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RegistrySOAPServices.class);

    /** Path separator */
    private static final String PATH_SEPARATOR = "/";

    /** WSDL suffix */
    private static final String WSDL = "?wsdl";

    /** The name of the notification listener service */
    private static final String NOTIFICATION_SERVICE_NAME = "notificationListener";

    /** The name of the lifecycle manager service */
    private static final String LIFECYCLE_MANAGER_SERVICE_NAME = "lifecycleManager";

    /** The name of the cataloger service */
    private static final String CATALOGER_SERVICE_NAME = "cataloger";

    /** The name of the query service */
    private static final String QUERY_SERVICE_NAME = "queryManager";

    /** The name of the validator service */
    private static final String VALIDATOR_SERVICE_NAME = "validator";

    private static final ProxyConfiguration proxyConfig;

    private static final HTTPClientPolicy httpClientPolicy;
    static {
        proxyConfig = getProxyConfiguration();
        httpClientPolicy = new HTTPClientPolicy();

        try {
            httpClientPolicy.setReceiveTimeout(Long.parseLong(System
                    .getProperty("ebxml-http-receive-timeout")));
        } catch (NumberFormatException e) {
            statusHandler
                    .error("ebxml-http-receive-timeout not specified.  Using default value of 1 minute",
                            e);
            httpClientPolicy.setReceiveTimeout(60000);
        }
        try {
            httpClientPolicy.setConnectionTimeout(Long.parseLong(System
                    .getProperty("ebxml-http-connection-timeout")));
        } catch (NumberFormatException e) {
            statusHandler
                    .error("ebxml-http-connection-timeout not specified.  Using default value of 10 seconds",
                            e);
            httpClientPolicy.setReceiveTimeout(10000);
        }
        httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        httpClientPolicy.setMaxRetransmits(5);
        if (proxyConfig != null) {
            httpClientPolicy.setProxyServer(proxyConfig.getHost());
            httpClientPolicy.setProxyServerPort(proxyConfig.getPort());
            httpClientPolicy.setNonProxyHosts(proxyConfig.getNonProxyHosts());
        }
    }

    /** Cache of known notification services */
    private static LoadingCache<String, NotificationListener> notificationManagerServices = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, NotificationListener>() {
                public NotificationListener load(String key) {
                    return getPort(key, NotificationListener.class);
                }
            });

    /** Cache of known lifecycle manager services */
    private static LoadingCache<String, LifecycleManager> lifecycleManagerServices = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, LifecycleManager>() {
                public LifecycleManager load(String key) {
                    return getPort(key, LifecycleManager.class);
                }
            });

    /** Cache of known cataloger services */
    private static LoadingCache<String, Cataloger> catalogerServices = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Cataloger>() {
                public Cataloger load(String key) {
                    return getPort(key, Cataloger.class);
                }
            });

    /** Cache of known query services */
    private static LoadingCache<String, QueryManager> queryServices = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, QueryManager>() {
                public QueryManager load(String key) {
                    return getPort(key, QueryManager.class);
                }
            });

    /** Cache of known validator services */
    private static LoadingCache<String, Validator> validatorServices = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Validator>() {
                public Validator load(String key) {
                    return getPort(key, Validator.class);
                }
            });

    /**
     * Gets the notification listener service URL for the given host
     * 
     * @param host
     *            The host
     * @return The notification listener service URL for the given host
     * @throws MalformedURLException
     *             If errors occur creating the URL object
     */
    public static String getNotificationListenerServiceUrl(final String baseURL)
            throws MalformedURLException {
        return new URL(baseURL + PATH_SEPARATOR + NOTIFICATION_SERVICE_NAME)
                .toString();
    }

    /**
     * Gets the notification listener service for the given host
     * 
     * @param host
     *            The host to get the notification listener service for
     * @return The notification listener service for the given host
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static NotificationListener getNotificationListenerServiceForHost(
            final String host) throws RegistryServiceException {
        return getNotificationListenerServiceForUrl(host + PATH_SEPARATOR
                + NOTIFICATION_SERVICE_NAME);
    }

    /**
     * Gets the notification listener service at the given URL string
     * 
     * @param url
     *            The url
     * @return The notification listener service at the given URL
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static NotificationListener getNotificationListenerServiceForUrl(
            final String url) throws RegistryServiceException {
        try {
            return notificationManagerServices.get(url);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting notification service!", e);
        }
    }

    /**
     * Gets the lifecycle manager service for the given host
     * 
     * @param host
     *            The host to get the lifecycle manager service for
     * @return The lifecycle manager service for the given host
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static LifecycleManager getLifecycleManagerServiceForHost(
            final String host) throws RegistryServiceException {
        return getLifecycleManagerServiceForUrl(host + PATH_SEPARATOR
                + LIFECYCLE_MANAGER_SERVICE_NAME);
    }

    /**
     * Gets the lifecycle manager service for the given URL string
     * 
     * @param url
     *            The service URL
     * @return The lifecycle manager service at the given URL string
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static LifecycleManager getLifecycleManagerServiceForUrl(
            final String url) throws RegistryServiceException {
        try {
            return lifecycleManagerServices.get(url);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting lifecycleManager service", e);
        }
    }

    /**
     * Gets the cataloger service for the given host
     * 
     * @param host
     *            The host to get the cataloger service for
     * @return The cataloger service at the given host
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static Cataloger getCatalogerServiceForHost(final String host)
            throws RegistryServiceException {
        return getCatalogerServiceForUrl(host + PATH_SEPARATOR
                + CATALOGER_SERVICE_NAME);
    }

    /**
     * Gets the cataloger service for the given url string
     * 
     * @param url
     *            the url string
     * @return The cataloger service
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static Cataloger getCatalogerServiceForUrl(final String url)
            throws RegistryServiceException {
        try {
            return catalogerServices.get(url);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting cataloger service!", e);
        }
    }

    /**
     * Gets the query manager service at the given host
     * 
     * @param host
     *            The host name
     * @return The query manager service
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static QueryManager getQueryServiceForHost(final String host)
            throws RegistryServiceException {
        return getQueryServiceForUrl(host + PATH_SEPARATOR + QUERY_SERVICE_NAME);
    }

    /**
     * Gets the query manager service at the given url string
     * 
     * @param serviceUrl
     *            The url string
     * @return The query manager service at the given url string
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static QueryManager getQueryServiceForUrl(final String url)
            throws RegistryServiceException {
        try {
            return queryServices.get(url);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error gett queryManager service!", e);
        }
    }

    /**
     * Gets the validator service for the given host
     * 
     * @param host
     *            The host
     * @return The validator service for the given host
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static Validator getValidatorServiceForHost(final String host)
            throws RegistryServiceException {
        return getValidatorServiceForUrl(host + PATH_SEPARATOR
                + VALIDATOR_SERVICE_NAME);
    }

    /**
     * Gets the validator service for the given url string
     * 
     * @param serviceUrl
     *            The url string
     * @return The validator service for the given url string
     * @throws RegistryServiceException
     *             If errors occur creating the URL object
     */
    public static Validator getValidatorServiceForUrl(final String url)
            throws RegistryServiceException {
        try {
            return validatorServices.get(url);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting validator service!", e);
        }
    }

    /**
     * 
     * Sends a SubmitObjects request to the lifecycle manager service at the
     * given host
     * 
     * @param request
     *            The request
     * @param host
     *            The host to send the request to
     * @throws RegistryServiceException
     *             If errors occur during request submission
     */
    public static void sendSubmitObjectsRequest(SubmitObjectsRequest request,
            String host) throws RegistryServiceException {

        LifecycleManager lcm;
        try {
            lcm = getLifecycleManagerServiceForHost(host);
        } catch (RegistryServiceException e) {
            throw new RegistryServiceException(
                    "Error getting lifecyclemanager for host at [" + host + "]",
                    e);
        }
        RegistryResponseType response;
        try {
            response = lcm.submitObjects(request);
        } catch (MsgRegistryException e) {
            throw new RegistryServiceException(
                    "Error executing submitObjects!", e);
        }
        RegistryResponseStatus status = response.getStatus();
        if (status.equals(RegistryResponseStatus.SUCCESS)) {
            statusHandler.info("Submit Objects request ["
                    + response.getRequestId() + "] successful");
        } else if (status.equals(RegistryResponseStatus.PARTIAL_SUCCESS)) {
            statusHandler.warn("Submit Objects request ["
                    + response.getRequestId() + "] partially successful");
        } else if (status.equals(RegistryResponseStatus.FAILURE)) {
            statusHandler.error("Submit Objects request ["
                    + response.getRequestId() + "] failed!");
            StringBuilder exceptionMessage = new StringBuilder();
            for (RegistryExceptionType exc : response.getException()) {
                exceptionMessage.append(exc).append("\n");
            }
            throw new RegistryServiceException("Submit Objects Request ["
                    + response.getRequestId()
                    + "] failed with the following exceptions:"
                    + exceptionMessage.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Object> T getPort(String serviceUrl,
            Class<?> serviceInterface) throws RegistryServiceException {
        W3CEndpointReferenceBuilder endpointBuilder = new W3CEndpointReferenceBuilder();
        endpointBuilder.wsdlDocumentLocation(serviceUrl.toString() + WSDL);
        endpointBuilder.address(serviceUrl.toString());
        W3CEndpointReference ref = endpointBuilder.build();
        T port = (T) ref.getPort(serviceInterface);

        Client client = ClientProxy.getClient(port);
        ((HTTPConduit) client.getConduit()).setClient(httpClientPolicy);
        // Create HTTP header containing the calling registry
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put(RegistryUtil.CALLING_REGISTRY_SOAP_HEADER_NAME,
                Arrays.asList(RegistryUtil.LOCAL_REGISTRY_ADDRESS));
        client.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
        return port;
    }

    /**
     * Gets the proxy configuration
     * 
     * @return The proxy configuration
     */
    private static ProxyConfiguration getProxyConfiguration() {
        ProxyConfiguration proxyConfig = null;
        File proxyFile = PathManagerFactory.getPathManager().getStaticFile(
                "datadelivery" + File.separator + "proxy.properties");
        if (proxyFile != null) {
            try {
                proxyConfig = ProxyUtil.getProxySettings(proxyFile);
            } catch (IOException e) {
                throw new RegistryServiceException(
                        "Error reading proxy properties", e);
            }
        }
        return proxyConfig;
    }
}
