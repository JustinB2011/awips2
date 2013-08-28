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
package com.raytheon.uf.edex.registry.ebxml.services.lifecycle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.MsgRegistryException;
import oasis.names.tc.ebxml.regrep.wsdl.registry.services.v4.QueryManager;
import oasis.names.tc.ebxml.regrep.xsd.query.v4.QueryRequest;
import oasis.names.tc.ebxml.regrep.xsd.query.v4.QueryResponse;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.DynamicObjectRefType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.QueryType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.UrlValidator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.raytheon.uf.common.registry.schemas.ebxml.util.annotations.RegistryObjectReference;
import com.raytheon.uf.common.registry.services.RegistryRESTServices;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.registry.ebxml.dao.DynamicObjectRefDao;
import com.raytheon.uf.edex.registry.ebxml.dao.RegistryObjectDao;
import com.raytheon.uf.edex.registry.ebxml.exception.EbxmlRegistryException;
import com.raytheon.uf.edex.registry.ebxml.util.EbxmlExceptionUtil;

/**
 * 
 * Utility class used to resolve references contained in registry objects
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 8/5/2013    2191        bphillip    Initial implementation
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class ObjectReferenceResolver {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ObjectReferenceResolver.class);

    /** Data access object for registry objects */
    private RegistryObjectDao registryObjectDao;

    /** Data access object for dynamic reference objects */
    private DynamicObjectRefDao dynamicRefDao;

    /** Query Manager service */
    private QueryManager queryManager;

    /** Validator for validating REST endpoint addresses */
    private UrlValidator urlValidator = new UrlValidator();

    /** Cache holding the fields in each class that are object references */
    private static LoadingCache<Class<?>, List<String>> OBJECT_REFERENCE_FIELD_CACHE = CacheBuilder
            .newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, List<String>>() {
                @Override
                public List<String> load(Class<?> clazz) throws Exception {
                    List<String> fields = new ArrayList<String>();
                    while (!clazz.equals(Object.class)) {
                        for (Field f : clazz.getDeclaredFields()) {
                            if (f.isAnnotationPresent(RegistryObjectReference.class)) {
                                fields.add(f.getName());
                            }
                        }
                        clazz = clazz.getSuperclass();
                    }
                    return fields;
                }
            });

    /**
     * Validates the list of objects and makes sure all fields referencing
     * objects can be resolved.
     * 
     * @param regObjs
     *            The list of objects to check references for
     * @throws MsgRegistryException
     *             If any or all of the objects in the list contain unresolved
     *             references
     */
    public String allReferencesDoNotResolve(List<RegistryObjectType> regObjs)
            throws MsgRegistryException {
        StringBuilder returnMessage = new StringBuilder();
        for (RegistryObjectType regObj : regObjs) {
            statusHandler.info("Checking references for object: "
                    + regObj.getId() + "...");
            try {
                List<String> fields = OBJECT_REFERENCE_FIELD_CACHE.get(regObj
                        .getClass());
                for (String field : fields) {
                    String propertyValue = (String) PropertyUtils.getProperty(
                            regObj, field);
                    if (propertyValue == null) {
                        continue;
                    }

                    // Check for each reference type
                    if (!isStaticReference(propertyValue)
                            && !isDynamicReference(propertyValue)
                            && !isRESTReference(propertyValue)) {
                        continue;
                    }
                    returnMessage.append("Object [").append(regObj.getId())
                            .append("] references object [")
                            .append(propertyValue).append("]\n");
                }
            } catch (Exception e) {
                throw EbxmlExceptionUtil.createMsgRegistryException(
                        "Error checking references", e);
            }
        }
        return returnMessage.toString();
    }

    /**
     * Validates the list of objects and makes sure all fields referencing
     * objects can be resolved.
     * 
     * @param regObjs
     *            The list of objects to check references for
     * @throws MsgRegistryException
     *             If any or all of the objects in the list contain unresolved
     *             references
     */
    public String allReferencesResolve(List<RegistryObjectType> regObjs)
            throws MsgRegistryException {
        StringBuilder returnMessage = new StringBuilder();
        for (RegistryObjectType regObj : regObjs) {
            statusHandler.info("Checking references for object: "
                    + regObj.getId() + "...");
            try {
                List<String> fields = OBJECT_REFERENCE_FIELD_CACHE.get(regObj
                        .getClass());
                for (String field : fields) {
                    String propertyValue = (String) PropertyUtils.getProperty(
                            regObj, field);
                    if (propertyValue == null) {
                        continue;
                    }

                    // Check for each reference type
                    if (isStaticReference(propertyValue)
                            || isDynamicReference(propertyValue)
                            || isRESTReference(propertyValue)) {
                        continue;
                    }
                    returnMessage.append("Object [").append(regObj.getId())
                            .append("] references unresolvable object [")
                            .append(propertyValue).append("]\n");
                }
            } catch (Exception e) {
                throw EbxmlExceptionUtil.createMsgRegistryException(
                        "Error checking references", e);
            }
        }
        return returnMessage.toString();
    }

    /**
     * Checks if the object specified by ref on the object obj is a static
     * reference
     * 
     * @param ref
     *            The string to check if it is a static reference
     * @return true if the item is a static reference and the object exists.
     *         False if the object is not a static reference
     */
    private boolean isStaticReference(String ref) {
        return getStaticReferencedObject(ref) != null;
    }

    public RegistryObjectType getStaticReferencedObject(String ref) {
        return registryObjectDao.getById(ref);
    }

    /**
     * Checks if the property is a dynamic reference
     * 
     * @param obj
     *            The object being checked
     * @param propertyName
     *            The property on the object being checked
     * @param ref
     *            The value of the property on the object being checked
     * @return True if this is a dynamic reference and the referenced object can
     *         be resolved
     * @throws MsgRegistryException
     *             If issues occur while executing the query via the query
     *             manager
     * @throws EbxmlRegistryException
     *             If too many results were returned by the dynamic reference
     *             query
     */
    private boolean isDynamicReference(String ref) throws MsgRegistryException,
            EbxmlRegistryException {
        return getDynamicReferencedObject(ref) != null;
    }

    public RegistryObjectType getDynamicReferencedObject(String ref)
            throws EbxmlRegistryException, MsgRegistryException {
        DynamicObjectRefType dynamicRef = dynamicRefDao.getById(ref);
        if (dynamicRef != null) {
            QueryType refQuery = dynamicRef.getQuery();
            if (refQuery != null) {
                QueryRequest queryRequest = new QueryRequest();
                queryRequest.setId("Resolving reference [" + ref + "]");
                queryRequest.setQuery(refQuery);
                QueryResponse queryResponse = queryManager
                        .executeQuery(queryRequest);
                if (responseOk(queryResponse)) {
                    return queryResponse.getRegistryObjects().get(0);
                }
            }
        }
        return null;
    }

    /**
     * Checks if this is a object reference which references a REST service
     * 
     * @param ref
     *            The value being checked
     * @return True if this is a REST reference and the object being referenced
     *         can be resolved, else false
     * @throws EbxmlRegistryException
     *             If too many results were returned by the REST call or an
     *             unexpected type was returned by the REST call
     */
    private boolean isRESTReference(String ref) throws EbxmlRegistryException {
        return getRESTReferencedObject(ref) != null;
    }

    public RegistryObjectType getRESTReferencedObject(String ref)
            throws EbxmlRegistryException {

        RegistryObjectType retVal = null;
        if (urlValidator.isValid(ref)) {
            Object restResponse = RegistryRESTServices
                    .accessXMLRestService(ref);
            if (restResponse instanceof QueryResponse) {
                QueryResponse queryResponse = (QueryResponse) restResponse;
                if (responseOk(queryResponse)) {
                    retVal = queryResponse.getRegistryObjects().get(0);
                }
            } else if (restResponse instanceof RegistryObjectType) {
                retVal = (RegistryObjectType) restResponse;
            } else if (restResponse instanceof JAXBElement<?>) {
                return (RegistryObjectType) ((JAXBElement<?>) restResponse)
                        .getValue();
            }

            else {
                throw new EbxmlRegistryException("Unexpected response from "
                        + ref + ". Received response of type: "
                        + restResponse.getClass());
            }
        }
        return retVal;
    }

    public boolean isValidURL(String url) {
        return urlValidator.isValid(url);
    }

    /**
     * Checks if a query response is ok. The response is ok if the query result
     * is of size 1
     * 
     * @param queryResponse
     *            The query response to check
     * @return True if the query returned a single result
     * @throws EbxmlRegistryException
     *             If the query returned too many results
     */
    private boolean responseOk(QueryResponse queryResponse)
            throws EbxmlRegistryException {
        if (queryResponse.isOk()) {
            List<RegistryObjectType> responseObjects = queryResponse
                    .getRegistryObjects();
            if (responseObjects.isEmpty()) {
                return false;
            } else if (responseObjects.size() > 1) {
                throw new EbxmlRegistryException(
                        "DynamicObjReference query returned too many results. Expected 1. Got "
                                + responseObjects.size());
            }
            return true;
        }
        return false;
    }

    public void setRegistryObjectDao(RegistryObjectDao registryObjectDao) {
        this.registryObjectDao = registryObjectDao;
    }

    public void setDynamicRefDao(DynamicObjectRefDao dynamicRefDao) {
        this.dynamicRefDao = dynamicRefDao;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

}
