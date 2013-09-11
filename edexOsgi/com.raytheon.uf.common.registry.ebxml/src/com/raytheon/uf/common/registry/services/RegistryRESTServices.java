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

import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;
import com.raytheon.uf.common.registry.RegistryJaxbManager;
import com.raytheon.uf.common.registry.RegistryNamespaceMapper;
import com.raytheon.uf.common.registry.constants.RegistryAvailability;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.registry.services.rest.IRegistryAvailableRestService;
import com.raytheon.uf.common.registry.services.rest.IRegistryDataAccessService;
import com.raytheon.uf.common.registry.services.rest.IRegistryObjectsRestService;
import com.raytheon.uf.common.registry.services.rest.IRepositoryItemsRestService;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Class used to access REST services provided by the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 5/21/2013    2022        bphillip    Initial implementation
 * 7/29/2013    2191        bphillip    Implemented registry data access service
 * 8/1/2013     1693        bphillip    Modified getregistry objects method to correctly handle response
 * 9/5/2013     1538        bphillip    Changed cache expiration timeout and added http header
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class RegistryRESTServices {

    /** Map of known registry object request services */
    private static LoadingCache<String, IRegistryObjectsRestService> registryObjectServiceMap = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, IRegistryObjectsRestService>() {
                public IRegistryObjectsRestService load(String url) {
                    return getPort(url, IRegistryObjectsRestService.class);
                }
            });

    /** Map of known repository item request services */
    private static LoadingCache<String, IRepositoryItemsRestService> repositoryItemServiceMap = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, IRepositoryItemsRestService>() {
                public IRepositoryItemsRestService load(String url) {
                    return getPort(url, IRepositoryItemsRestService.class);
                }
            });

    /** Map of known registry availability services */
    private static LoadingCache<String, IRegistryAvailableRestService> registryAvailabilityServiceMap = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, IRegistryAvailableRestService>() {
                public IRegistryAvailableRestService load(String url) {
                    return getPort(url, IRegistryAvailableRestService.class);
                }
            });

    /** Map of known registry data access services */
    private static LoadingCache<String, IRegistryDataAccessService> registryDataAccessServiceMap = CacheBuilder
            .newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, IRegistryDataAccessService>() {
                public IRegistryDataAccessService load(String url) {
                    return getPort(url, IRegistryDataAccessService.class);
                }
            });

    /**
     * The logger
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RegistryRESTServices.class);

    private static RegistryJaxbManager jaxbManager;

    static {
        try {
            jaxbManager = new RegistryJaxbManager(new RegistryNamespaceMapper());
        } catch (JAXBException e) {
            statusHandler.error("Error creating Registry jaxb manager!", e);
        }
    }

    /**
     * Gets the registry object rest service implementation
     * 
     * @param baseURL
     *            The base URL of the registry
     * @return The service implementation
     */
    public static IRegistryObjectsRestService getRegistryObjectService(
            String baseURL) {
        try {
            return registryObjectServiceMap.get(baseURL);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting Registry Object Rest Service", e);
        }
    }

    /**
     * Gets a registry object via the rest service
     * 
     * @param <T>
     *            Type of object extending RegistryObjectType
     * @param baseURL
     *            The base URL of the registry
     * @param objectId
     *            The id of the object to retrieve
     * @return The object
     * @throws JAXBException
     *             If errors occur while serializing the object
     */
    @SuppressWarnings("unchecked")
    public static <T extends RegistryObjectType> T getRegistryObject(
            Class<T> expectedType, String baseURL, String objectId)
            throws JAXBException, RegistryServiceException {
        String objStr = getRegistryObjectService(baseURL).getRegistryObject(
                objectId);
        Object retVal = jaxbManager.unmarshalFromXml(objStr);
        if (retVal instanceof JAXBElement<?>) {
            return (T) ((JAXBElement<?>) retVal).getValue();
        }
        return (T) jaxbManager.unmarshalFromXml(objStr);
    }

    /**
     * Gets the repository item rest service implementation
     * 
     * @param baseURL
     *            The base URL of the registry
     * @return The service implementation
     */
    public static IRepositoryItemsRestService getRepositoryItemService(
            String baseURL) {
        try {
            return repositoryItemServiceMap.get(baseURL);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting Repository Item Rest Service", e);
        }
    }

    /**
     * Gets a repository item via the rest service
     * 
     * @param baseURL
     *            The base URL of the registry
     * @param repositoryItemId
     *            The id of the object
     * @return The repository item
     */
    public static byte[] getRepositoryItem(String baseURL,
            String repositoryItemId) {
        return getRepositoryItemService(baseURL).getRepositoryItem(
                repositoryItemId);
    }

    /**
     * Gets the registry available service implementation
     * 
     * @param baseURL
     *            The base URL of the registry
     * @return THe registry available service implementation
     */
    public static IRegistryAvailableRestService getRegistryAvailableService(
            String baseURL) {
        try {
            return registryAvailabilityServiceMap.get(baseURL);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting Registry Availability Rest Service", e);
        }
    }

    /**
     * Check if the registry at the given URL is available
     * 
     * @param baseURL
     *            The base URL of the registry
     * @return True if the registry services are available
     */
    public static boolean isRegistryAvailable(String baseURL) {
        String response = null;
        try {
            response = getRegistryAvailableService(baseURL)
                    .isRegistryAvailable();
            if (RegistryAvailability.AVAILABLE.equals(response)) {
                return true;
            } else {
                statusHandler.info("Registry at [" + baseURL
                        + "] not available: " + response);
            }
            return RegistryAvailability.AVAILABLE.equals(response);
        } catch (Throwable t) {
            if (response == null) {
                response = ExceptionUtils.getRootCauseMessage(t);
            }
            statusHandler.error("Registry at [" + baseURL + "] not available: "
                    + response);
            return false;
        }
    }

    /**
     * Gets the data access service for the specified registry URL
     * 
     * @param baseURL
     *            The baseURL of the registry
     * @return The data access service for the specified registry URL
     */
    public static IRegistryDataAccessService getRegistryDataAccessService(
            String baseURL) {
        try {
            return registryDataAccessServiceMap.get(baseURL);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "Error getting Registry Availability Rest Service", e);
        }
    }

    /**
     * Accesses a rest service at the provided URL. This method is primarily
     * used for resolving remote object references which use a REST service
     * 
     * @param url
     *            The URL of the rest service
     * @return
     */
    public static Object accessXMLRestService(String url) {
        String response = null;
        try {
            response = Resources
                    .toString(new URL(url), Charset.forName("UTF8"));
        } catch (Exception e) {
            throw new RegistryServiceException(
                    "Error accessing REST service at URL: [" + url + "]", e);
        }
        try {
            return jaxbManager.unmarshalFromXml(response);
        } catch (JAXBException e) {
            throw new RegistryServiceException(
                    "Error unmarshalling xml response from REST Service at URL: ["
                            + url + "]");
        }
    }

    private static <T extends Object> T getPort(String url,
            Class<T> serviceClass) {
        T service = JAXRSClientFactory.create(url, serviceClass);
        Client client = (Client) Proxy.getInvocationHandler((Proxy) service);
        // Create HTTP header containing the calling registry
        client.header(RegistryUtil.CALLING_REGISTRY_SOAP_HEADER_NAME,
                RegistryUtil.LOCAL_REGISTRY_ADDRESS);
        return service;
    }
}
