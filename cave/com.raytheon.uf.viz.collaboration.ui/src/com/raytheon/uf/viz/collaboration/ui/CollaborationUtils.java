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

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.collaboration.comm.identity.IPresence;
import com.raytheon.uf.viz.collaboration.comm.identity.IPresence.Mode;
import com.raytheon.uf.viz.collaboration.data.CollaborationGroup;
import com.raytheon.uf.viz.collaboration.data.CollaborationNode;
import com.raytheon.uf.viz.collaboration.data.CollaborationUser;
import com.raytheon.uf.viz.collaboration.data.SessionGroup;

/**
 * Methods for sending, receiving messages
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 24, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class CollaborationUtils {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(CollaborationUtils.class);

    public static final IPresence.Mode[] statusModes = { Mode.AVAILABLE,
            Mode.DND, Mode.AWAY };

    /**
     * Get the statusModes' index for desired mode.
     * 
     * @param mode
     * @return index - the mode's index or -1 if not in statusModes
     */
    public static int statusModesIndex(IPresence.Mode mode) {
        for (int index = 0; index < statusModes.length; ++index) {
            if (mode.equals(statusModes[index])) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Get an descriptor for the file in the icon directory. DEPRECATED: Use
     * IconUtil instead
     * 
     * @param name
     *            - file name
     * @return imageDescriptor
     */
    @Deprecated
    public static ImageDescriptor getImageDescriptor(String name) {
        String iconPath = "icons" + File.separator;
        URL url = FileLocator.find(Activator.getDefault().getBundle(),
                new Path(iconPath + name), null);
        if (url != null && url.getFile() == null) {
            url = FileLocator.find(Activator.getDefault().getBundle(),
                    new Path(".." + File.separator + iconPath + name), null);
        }
        return ImageDescriptor.createFromURL(url);
    }

    /**
     * Get an image associated with the node.
     * 
     * @param node
     * @return image
     */
    public static Image getNodeImage(CollaborationNode node) {
        Image nodeImage = null;
        if (node instanceof CollaborationUser) {
            CollaborationUser user = (CollaborationUser) node;
            if (user.getMode() == IPresence.Mode.AVAILABLE) {
                nodeImage = getImageDescriptor("available.gif").createImage();
            } else if (user.getMode() == IPresence.Mode.AWAY) {
                nodeImage = getImageDescriptor("away.gif").createImage();
            } else if (user.getMode() == IPresence.Mode.DND) {
                nodeImage = getImageDescriptor("do_not_disturb.gif")
                        .createImage();
            } else {
                nodeImage = getImageDescriptor("available.gif").createImage();
            }
        } else if (node instanceof SessionGroup) {
            if (!((SessionGroup) node).isSessionRoot()) {
                nodeImage = getImageDescriptor("session_group.gif")
                        .createImage();
            } else {
                // nodeImage = getImageDescriptor("").createImage();

            }
        } else if (node instanceof CollaborationGroup) {
            nodeImage = getImageDescriptor("group.gif").createImage();
        }
        return nodeImage;
    }

    public static void sendChatMessage(List<String> ids, String message) {
        // TODO transform Strings to IDS
        System.err.println("sendChatMessage: " + message);
        // if (CollaborationData.getInstance().hasChat(null)) {
        // IChat chat = CollaborationData.getInstance().getOpenChat(null);
        // try {
        // chat.sendChatMessage(message);
        // } catch (ECFException e) {
        // statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
        // e);
        // }
        // }
    }

    public static void createChat(List<ID> users) {
        System.err.println("createChat: " + users);
        // IPresenceContainerAdapter presence = CollaborationData.getInstance()
        // .getPresence();
        // try {
        // presence.getChatManager().createChat(users.get(0),
        // new IIMMessageListener() {
        // @Override
        // public void handleMessageEvent(
        // IIMMessageEvent messageEvent) {
        // if (messageEvent instanceof ChatMessageEvent) {
        // ChatMessageEvent event = (ChatMessageEvent) messageEvent;
        // System.out.println(event.getChatMessage()
        // .getBody());
        // }
        // }
        // });
        // } catch (ECFException e) {
        // statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        // }
    }

    public static void changeStatus(IPresence.Mode statusMode) {
        System.out.println("Changing mode...");
        // IPresenceContainerAdapter presence = CollaborationData.getInstance()
        // .getPresence();
        //
        // IPresence pres = new Presence(IPresence.Type.AVAILABLE, "AVAILABLE",
        // IPresence.Mode.fromString(type.toString()));
        // try {
        // presence.getRosterManager()
        // .getPresenceSender()
        // .sendPresenceUpdate(
        // CollaborationData.getInstance().getClient().getID(),
        // pres);
        // } catch (ECFException e) {
        // statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        // }
        // presence.getAccountManager();
    }
}
