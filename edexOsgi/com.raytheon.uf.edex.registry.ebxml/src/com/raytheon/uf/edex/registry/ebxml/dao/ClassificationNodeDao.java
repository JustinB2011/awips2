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
package com.raytheon.uf.edex.registry.ebxml.dao;

import java.util.List;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.ClassificationNodeType;

/**
 * Data access object for retrieving ClassificationNodeTypes
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2/21/2012    184        bphillip     Initial creation
 * 8/3/2012     724        bphillip    Added more methods for getting classification nodes
 * 3/18/2013    1802       bphillip    Modified to use transaction boundaries and spring injection
 * 4/9/2013     1802       bphillip    Removed exception catching
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class ClassificationNodeDao extends
        RegistryObjectTypeDao<ClassificationNodeType> {

    /** Regex to use when querying for telephone types */
    public static final String TELEPHONE_TYPE_REGEX = "urn:oasis:names:tc:ebxml-regrep:PhoneType:%";

    /** Regex to use when querying for postal address types */
    public static final String ADDRESS_TYPE_REGEX = "urn:oasis:names:tc:ebxml-regrep:PostalAddressType%";

    /** Regex to use when querying for email types */
    public static final String EMAIL_TYPE_REGEX = "urn:oasis:names:tc:ebxml-regrep:EmailType:%";

    public ClassificationNodeDao() {

    }

    public boolean isValidNode(String id) {
        long result = (Long) this
                .executeHQLQuery(
                        "select count(node) from ClassificationNodeType node where node.id=:id",
                        "id", id).get(0);
        return result != 0;
    }

    /**
     * Retrieves ClassificationNode objects based on the path
     * 
     * @param path
     *            The path to get the classification node type for
     * @return The ClassificationNode object with the specified path
     */
    public ClassificationNodeType getByPath(String path) {
        List<ClassificationNodeType> result = this
                .executeHQLQuery("select obj from ClassificationNodeType obj where obj.path='"
                        + path + "'");
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Gets the ID of the classification node given the code
     * 
     * @param code
     *            The code of the classification node
     * @return The ID of the classification node with the given code
     */
    public String getNodeFromCode(String code) {
        List<String> results = this
                .executeHQLQuery("select obj.id from ClassificationNodeType obj where obj.code='"
                        + code + "'");

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    /**
     * Gets the Code of the classification node given the object ID
     * 
     * @param id
     *            The object ID of the classification node
     * @return The code of the classification node with the given ID
     */
    public String getCodeFromNode(String id) {
        List<String> results = this
                .executeHQLQuery("select obj.code from ClassificationNodeType obj where obj.id='"
                        + id + "'");

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    /**
     * Gets the codes of the telephone types in the registry
     * 
     * @return The codes of the telephone types in the registry
     */
    public List<String> getTelephoneTypes() {

        return this
                .executeHQLQuery("select obj.code from ClassificationNodeType obj where obj.lid like '"
                        + TELEPHONE_TYPE_REGEX + "'");

    }

    /**
     * Gets the codes of the address types in the registry
     * 
     * @return The codes of the address types in the registry
     */
    public List<String> getAddressTypes() {

        return this
                .executeHQLQuery("select obj.code from ClassificationNodeType obj where obj.lid like '"
                        + ADDRESS_TYPE_REGEX + "'");

    }

    /**
     * Gets the codes of the email types in the registry
     * 
     * @return The codes dmail types in the registry
     */
    public List<String> getEmailTypes() {
        return this
                .executeHQLQuery("select obj.code from ClassificationNodeType obj where obj.lid like '"
                        + EMAIL_TYPE_REGEX + "'");
    }

    @Override
    protected Class<ClassificationNodeType> getEntityClass() {
        return ClassificationNodeType.class;
    }
}
