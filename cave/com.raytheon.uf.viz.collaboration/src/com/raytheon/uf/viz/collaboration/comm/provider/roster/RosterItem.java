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
package com.raytheon.uf.viz.collaboration.comm.provider.roster;

import com.raytheon.uf.viz.collaboration.comm.identity.roster.IRoster;
import com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterItem;

/**
 * TODO Add Description
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2012            jkorman     Initial creation
 *
 * </pre>
 *
 * @author jkorman
 * @version 1.0	
 */

/**
 * TODO Add Description
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 29, 2012            jkorman     Initial creation
 *
 * </pre>
 *
 * @author jkorman
 * @version 1.0	
 */
public class RosterItem implements IRosterItem {

    //
    private String name = null;
    
    //
    private IRosterItem parent = null;
    
    //
    private IRoster roster = null;

    /**
     * 
     * @param name
     * @param parent
     * @param roster
     */
    public RosterItem(String name, IRosterItem parent, IRoster roster) {
        this.name = name;
        this.parent = parent;
        this.roster = roster;
    }

    /**
     * 
     */
    public RosterItem() {
        this(null,null,null);
    }
    
    /**
     * 
     * @param name
     * @param item
     */
    public RosterItem(String name, IRosterItem item) {
        this(name, item, null);
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @see com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterItem#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 
     * @param item
     */
    public void setParent(IRosterItem item) {
        parent = item;
    }
    
    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterItem#getParent()
     */
    @Override
    public IRosterItem getParent() {
        return parent;
    }

    /**
     * 
     * @param roster
     */
    public void setRoster(IRoster roster) {
        this.roster = roster;
    }
    
    /**
     * 
     * @see com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterItem#getRoster()
     */
    @Override
    public IRoster getRoster() {
        return null;
    }

}
