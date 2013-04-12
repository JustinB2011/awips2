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

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import com.raytheon.uf.edex.registry.ebxml.exception.EbxmlRegistryException;

/**
 * Data Access object for interacting with registry object types in the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 3/13/2013    1082       bphillip    Initial creation
 * 4/9/2013     1802       bphillip    Removed exception catching
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class RegistryObjectDao extends
        RegistryObjectTypeDao<RegistryObjectType> {

    public RegistryObjectDao() {
    }

    /**
     * Retrieves all registry objects from the registry
     * 
     * @param <T>
     *            A class type extending RegistryObjectType
     * @return All the registry objects contained in the registry
     * @throws EbxmlRegistryException
     *             If the HQL query fails
     */
    public List<RegistryObjectType> getAllRegistryObjects() {
        return getAll();
    }

    @Override
    protected Class<RegistryObjectType> getEntityClass() {
        return RegistryObjectType.class;
    }

}
