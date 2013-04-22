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
package com.raytheon.uf.edex.registry.ebxml.services.query.types.canonical;

import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.ebxml.regrep.xsd.query.v4.QueryResponse;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.ObjectRefListType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.ObjectRefType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.QueryType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectListType;
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import com.raytheon.uf.common.registry.constants.CanonicalQueryTypes;
import com.raytheon.uf.edex.registry.ebxml.exception.EbxmlRegistryException;
import com.raytheon.uf.edex.registry.ebxml.services.query.QueryConstants;
import com.raytheon.uf.edex.registry.ebxml.services.query.QueryManagerImpl.RETURN_TYPE;
import com.raytheon.uf.edex.registry.ebxml.services.query.QueryParameters;
import com.raytheon.uf.edex.registry.ebxml.services.query.types.CanonicalEbxmlQuery;

/**
 * The canonical query GetObjectById allows clients to find RegistryObjects
 * based upon the value of their id attribute.
 * 
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 18, 2012            bphillip     Initial creation
 * 3/18/2013    1802       bphillip    Modified to use transaction boundaries and spring dao injection
 * 4/9/2013     1802       bphillip     Changed abstract method signature, modified return processing, and changed static variables
 * 4/19/2013    1931       bphillip    Fixed null pointer issue
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */

public class GetObjectById extends CanonicalEbxmlQuery {

    /** The list of valid parameters for this query */
    private static final List<String> QUERY_PARAMETERS = new ArrayList<String>();

    /* Initializes the list of parameters */
    static {
        QUERY_PARAMETERS.add(QueryConstants.ID);
    }

    @Override
    protected void query(QueryType queryType, QueryResponse queryResponse)
            throws EbxmlRegistryException {
        QueryParameters parameters = getParameterMap(queryType.getSlot(),
                queryResponse);
        // The client did not specify the required parameter
        if (parameters.isEmpty()
                || !parameters.containsParameter(QueryConstants.ID)) {
            throw new EbxmlRegistryException("Canonical query ["
                    + this.getQueryDefinition()
                    + "] is missing required parameter ["
                    + QUERY_PARAMETERS.get(0) + "]");
        }

        String id = parameters.getFirstParameter(QueryConstants.ID);
        List<String> ids = new ArrayList<String>();
        if (id.contains("_") || id.contains("%")) {
            List<String> matchingIds = registryObjectDao.getMatchingIds(id);
            if (matchingIds.isEmpty()) {
                return;
            }
            ids.addAll(matchingIds);
        } else {
            ids.add(id);
        }

        if (returnType.equals(RETURN_TYPE.ObjectRef)) {
            ObjectRefListType objectRefList = new ObjectRefListType();
            for (String idValue : ids) {
                ObjectRefType objectRef = new ObjectRefType();
                objectRef.setId(idValue);
                objectRefList.getObjectRef().add(objectRef);
            }
            queryResponse.setObjectRefList(objectRefList);
        } else {
            List<RegistryObjectType> results = new ArrayList<RegistryObjectType>();
            if (ids.size() == 1) {
                results.add(registryObjectDao.getById(ids.get(0)));
            } else {
                results.addAll(registryObjectDao.getById(ids));
            }
            RegistryObjectListType objList = new RegistryObjectListType();
            objList.getRegistryObject().addAll(results);
            queryResponse.setRegistryObjectList(objList);
        }
    }

    @Override
    protected List<String> getValidParameters() {
        return QUERY_PARAMETERS;
    }

    @Override
    public String getQueryDefinition() {
        return CanonicalQueryTypes.GET_OBJECT_BY_ID;
    }
}
