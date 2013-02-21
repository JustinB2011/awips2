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
package com.raytheon.uf.edex.ebxml.registry.memory.matchers;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import com.raytheon.uf.edex.ebxml.query.matcher.IMatcher;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 19, 2011            bphillip     Initial creation
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */

public class ObjectTypeMatcher implements IMatcher {

    private final String value;

    public ObjectTypeMatcher(String value) {
        this.value = value;
    }

    @Override
    public boolean matches(RegistryObjectType obj) {
        String objType = obj.getObjectType();
        if (objType != null && objType.equals(value)) {
            return true;
        }
        return false;
    }

}
