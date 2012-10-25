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
package com.raytheon.uf.viz.collaboration.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresence.Type;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.raytheon.uf.viz.collaboration.comm.identity.info.SiteConfigInformation;
import com.raytheon.uf.viz.collaboration.comm.provider.session.CollaborationConnection;

/**
 * Common code that is used whenever providing labels for users in a tree.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2012            bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public abstract class AbstractUserLabelProvider extends ColumnLabelProvider {

    protected Map<String, Image> imageMap = new HashMap<String, Image>();

    @Override
    public String getText(Object element) {
        if (!(element instanceof IUser)) {
            return null;
        }
        IUser user = (IUser) element;
        StringBuilder name = new StringBuilder();
        name.append(getDisplayName(user));
        IPresence presence = getPresence(user);
        if (presence != null) {
            Object site = presence.getProperties().get(
                    SiteConfigInformation.SITE_NAME);
            if (site != null) {
                name.append(" - ");
                name.append(site);
            }
            Object role = presence.getProperties().get(
                    SiteConfigInformation.ROLE_NAME);
            if (role != null) {
                name.append(" - ");
                name.append(role);
            }
        }
        return name.toString();
    }

    @Override
    public Image getImage(Object element) {
        if (!(element instanceof IUser)) {
            return null;
        }
        IUser user = (IUser) element;
        IPresence presence = getPresence(user);
        String key = "";
        if (presence != null && presence.getType() == Type.AVAILABLE) {
            key = presence.getMode().toString().replaceAll("\\s+", "_");
        } else {
            key = "contact_disabled";
        }
        if (imageMap.get(key) == null && !key.equals("")) {
            imageMap.put(key, CollaborationUtils.getNodeImage(key));
        }
        return imageMap.get(key);
    }

    @Override
    public String getToolTipText(Object element) {
        if (!(element instanceof IUser)) {
            return null;
        }
        IUser user = (IUser) element;
        IPresence presence = getPresence(user);
        StringBuilder text = new StringBuilder();
        text.append("Name: ").append(getDisplayName(user)).append("\n");
        text.append("Status: ");
        if (presence == null || presence.getType() != Type.AVAILABLE) {
            text.append("Offline\n");
        } else {
            text.append(CollaborationUtils.formatMode(presence.getMode()))
                    .append("\n");
            if (presence.getStatus() != null && !presence.getStatus().isEmpty()) {
                text.append("Message : \"").append(presence.getStatus())
                        .append("\"\n");
            }
            for (Object key : presence.getProperties().keySet()) {
                Object value = presence.getProperties().get(key);
                if (value != null && key != null) {
                    text.append(key).append(" : ").append(value).append("\n");
                }
            }
        }
        // delete trailing newline
        text.deleteCharAt(text.length() - 1);
        return text.toString();
    }

    @Override
    public int getToolTipStyle(Object object) {
        return SWT.SHADOW_OUT;
    }

    @Override
    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    @Override
    public void dispose() {
        for (String key : imageMap.keySet()) {
            imageMap.get(key).dispose();
        }
        imageMap.clear();
    }

    protected String getDisplayName(IUser user) {
        CollaborationConnection connection = CollaborationConnection
                .getConnection();
        if (connection == null) {
            String name = user.getNickname();
            if (name == null) {
                name = user.getName();
            }
            return name;
        } else {
            return connection.getContactsManager().getDisplayName(user);
        }
    }

    protected abstract IPresence getPresence(IUser user);

}
