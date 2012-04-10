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
package com.raytheon.uf.viz.collaboration.comm.provider.event;

import com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent;
import com.raytheon.uf.viz.collaboration.comm.identity.invite.SharedDisplayInvite;
import com.raytheon.uf.viz.collaboration.comm.identity.user.IQualifiedID;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2012            jkorman     Initial creation
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */

public class VenueInvitationEvent implements IVenueInvitationEvent {

    private IQualifiedID venueId;

    private IQualifiedID invitor;

    private String subject;

    private SharedDisplayInvite invite;

    /**
     * 
     * @param roomId
     * @param invitor
     * @param subject
     * @param body
     */
    public VenueInvitationEvent(IQualifiedID venueId, IQualifiedID invitor,
            String subject, SharedDisplayInvite invite) {
        this.venueId = venueId;
        this.invitor = invitor;
        this.subject = subject;
        this.invite = invite;
    }

    /**
     * Get the room identifier
     * 
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent#getRoomId()
     */
    @Override
    public IQualifiedID getRoomId() {
        return venueId;
    }

    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent#getInviter()
     */
    @Override
    public IQualifiedID getInviter() {
        return invitor;
    }

    /**
     * @see com.raytheon.uf.viz.collaboration.comm.identity.event.IVenueInvitationEvent#getSubject()
     */
    @Override
    public String getSubject() {
        return subject;
    }

    public SharedDisplayInvite getInvite() {
        return invite;
    }

}
