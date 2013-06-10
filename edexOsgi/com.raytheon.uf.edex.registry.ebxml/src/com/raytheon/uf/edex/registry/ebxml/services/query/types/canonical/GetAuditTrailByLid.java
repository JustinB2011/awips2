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
import oasis.names.tc.ebxml.regrep.xsd.rim.v4.QueryType;

import com.raytheon.uf.common.registry.constants.CanonicalQueryTypes;
import com.raytheon.uf.edex.registry.ebxml.dao.AuditableEventTypeDao;
import com.raytheon.uf.edex.registry.ebxml.exception.EbxmlRegistryException;
import com.raytheon.uf.edex.registry.ebxml.services.query.QueryConstants;
import com.raytheon.uf.edex.registry.ebxml.services.query.QueryParameters;
import com.raytheon.uf.edex.registry.ebxml.services.query.types.CanonicalEbxmlQuery;

/**
 * TODO Add Description
 * 
 * <p>
 * <b>Parameter Summary:</b> <br>
 * · <b><i>endTime</i></b> -- Specifies the end of the time interval (inclusive)
 * for rim:/RegistryObject[@xsi:type="rim:AuditableEventType"]/@timestamp value
 * <p>
 * · <b><i>id</i></b> -- Matches rim:/RegistryObject/@id.
 * <p>
 * <b><i>startTime</i></b> -- Specifies the end of the time interval (inclusive)
 * for rim:/RegistryObject[@xsi:type="rim:AuditableEventType"]/@timestamp value
 * <p>
 * 
 * SOFTWARE HISTORY
 * 
 * Date Ticket# Engineer Description ------------ ---------- -----------
 * -------------------------- Jan 18, 2012 bphillip Initial creation 3/18/2013
 * 1802 bphillip Modified to use transaction boundaries and spring dao injection
 * 4/9/2013 1802 bphillip Changed abstract method signature, modified return
 * processing, and changed static variables
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */

public class GetAuditTrailByLid extends CanonicalEbxmlQuery {

    /** The list of valid parameters for this query */
    private static final List<String> QUERY_PARAMETERS = new ArrayList<String>();

    /* Initializes the list of parameters */
    static {
        QUERY_PARAMETERS.add(QueryConstants.END_TIME);
        QUERY_PARAMETERS.add(QueryConstants.LID);
        QUERY_PARAMETERS.add(QueryConstants.START_TIME);
    }

    private AuditableEventTypeDao auditableEventDao;

    @SuppressWarnings("unchecked")
    @Override
    protected void query(QueryType queryType, QueryResponse queryResponse,
            String client) throws EbxmlRegistryException {
        QueryParameters parameters = getParameterMap(queryType.getSlot(),
                queryResponse);

        String lid = parameters.getFirstParameter(QueryConstants.LID);
        String endTime = parameters.getFirstParameter(QueryConstants.END_TIME);
        String startTime = parameters
                .getFirstParameter(QueryConstants.START_TIME);
        if (lid == null) {
            throw new EbxmlRegistryException("Canonical query ["
                    + this.getQueryDefinition()
                    + "] is missing required parameter [" + QueryConstants.LID
                    + "]");
        }

        String query = "select obj from AuditableEventType obj inner join obj.action as Action "
                + "inner join Action.affectedObjects as AffectedObjects "
                + "inner join AffectedObjects.registryObject as regObj "
                + "where regObj.lid=':lid' :endTimeClause :startTimeClause order by obj.timestamp desc";
        query = query.replace(":lid", lid);
        if (startTime == null) {
            query = query.replace(":startTimeClause", "");
        } else {
            query = query.replace(":startTimeClause", " and obj.timestamp >= '"
                    + startTime + "'");
        }
        if (endTime == null) {
            query = query.replace(":endTimeClause", "");
        } else {
            query = query.replace(":endTimeClause", " and obj.timestamp <= '"
                    + endTime + "'");
        }
        setResponsePayload(queryResponse,
                auditableEventDao.executeHQLQuery(query));

    }

    @Override
    protected List<String> getValidParameters() {
        return QUERY_PARAMETERS;
    }

    @Override
    public String getQueryDefinition() {
        return CanonicalQueryTypes.GET_AUDIT_TRAIL_BY_LID;
    }

    public void setAuditableEventDao(AuditableEventTypeDao auditableEventDao) {
        this.auditableEventDao = auditableEventDao;
    }

}
