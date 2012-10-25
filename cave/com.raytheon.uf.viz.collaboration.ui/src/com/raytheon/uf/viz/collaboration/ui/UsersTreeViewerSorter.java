package com.raytheon.uf.viz.collaboration.ui;

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

import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.raytheon.uf.viz.collaboration.comm.identity.IVenueSession;
import com.raytheon.uf.viz.collaboration.comm.provider.user.LocalGroups.LocalGroup;
import com.raytheon.uf.viz.collaboration.comm.provider.user.UserId;
import com.raytheon.uf.viz.collaboration.ui.data.SessionGroupContainer;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 1, 2012            rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class UsersTreeViewerSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 == e2) {
            return 0;
        }

        // Make login user top node
        if (e1 instanceof UserId) {
            return -1;
        }

        if (e2 instanceof UserId) {
            return 1;
        }

        // session group before all other types but login user.
        if (e1 instanceof SessionGroupContainer) {
            if ((e2 instanceof SessionGroupContainer) == false) {
                return -1;
            }
        } else if (e2 instanceof SessionGroupContainer) {
            return 1;
        }

        // Groups before users.
        if (e1 instanceof IRosterGroup) {
            if (!(e2 instanceof IRosterGroup)) {
                return -1;
            }
        } else if (e1 instanceof IRosterGroup) {
            return 1;
        }
        if (e1 instanceof IRosterItem && e2 instanceof IRosterItem) {
            // Either both are groups or both are users.
            if (e1 instanceof IRosterGroup && e2 instanceof IRosterGroup) {
                return ((IRosterGroup) e1).getName().compareTo(
                        ((IRosterGroup) e2).getName());
            } else if (e1 instanceof IRosterEntry && e2 instanceof IRosterEntry) {
                String name;
                String otherName;
                IRosterEntry entry = (IRosterEntry) e1;
                IRosterEntry otherEntry = (IRosterEntry) e2;
                if (entry.getUser().getName() != null
                        && !entry.getUser().getName().isEmpty()) {
                    name = entry.getUser().getName();
                } else {
                    name = entry.getName();
                }

                if (otherEntry.getUser().getName() != null
                        && !otherEntry.getUser().getName().isEmpty()) {
                    otherName = otherEntry.getUser().getName();
                } else {
                    otherName = otherEntry.getName();
                }
                return name.compareTo(otherName);
            }
        } else if (e1 instanceof IVenueSession && e2 instanceof IVenueSession) {
            if (((IVenueSession) e1).getVenue() == null
                    || ((IVenueSession) e1).getVenue() == null) {
                return 0;
            }
            return ((IVenueSession) e1).getVenue().toString()
                    .compareTo(((IVenueSession) e2).getVenue().toString());
        }
        if (e1 instanceof LocalGroup) {
            if (!(e2 instanceof LocalGroup)) {
                return -1;
            }
        } else if (e1 instanceof LocalGroup) {
            return 1;
        }
        if (e1 instanceof LocalGroup && e2 instanceof LocalGroup) {
            return ((LocalGroup) e1).getName().compareTo(
                    ((LocalGroup) e2).getName());
        }
        return 0;
    }
}
