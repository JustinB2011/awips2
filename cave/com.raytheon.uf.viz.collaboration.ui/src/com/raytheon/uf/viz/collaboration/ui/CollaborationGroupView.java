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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.collaboration.comm.identity.CollaborationException;
import com.raytheon.uf.viz.collaboration.comm.identity.IPresence;
import com.raytheon.uf.viz.collaboration.comm.identity.IPresence.Type;
import com.raytheon.uf.viz.collaboration.comm.identity.ISession;
import com.raytheon.uf.viz.collaboration.comm.identity.IVenueSession;
import com.raytheon.uf.viz.collaboration.comm.identity.event.IRosterChangeEvent;
import com.raytheon.uf.viz.collaboration.comm.identity.info.IVenueInfo;
import com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterEntry;
import com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterGroup;
import com.raytheon.uf.viz.collaboration.comm.identity.roster.IRosterManager;
import com.raytheon.uf.viz.collaboration.comm.provider.Tools;
import com.raytheon.uf.viz.collaboration.comm.provider.session.SessionManager;
import com.raytheon.uf.viz.collaboration.data.CollaborationDataManager;
import com.raytheon.uf.viz.collaboration.data.CollaborationGroup;
import com.raytheon.uf.viz.collaboration.data.CollaborationNode;
import com.raytheon.uf.viz.collaboration.data.CollaborationUser;
import com.raytheon.uf.viz.collaboration.data.LoginUser;
import com.raytheon.uf.viz.collaboration.data.OrphanGroup;
import com.raytheon.uf.viz.collaboration.data.SessionGroup;
import com.raytheon.uf.viz.collaboration.ui.editor.CollaborationEditor;
import com.raytheon.uf.viz.collaboration.ui.login.ChangeStatusDialog;
import com.raytheon.uf.viz.collaboration.ui.login.LoginData;
import com.raytheon.uf.viz.collaboration.ui.login.LoginDialog;
import com.raytheon.uf.viz.collaboration.ui.session.AbstractSessionView;
import com.raytheon.uf.viz.collaboration.ui.session.CollaborationSessionView;
import com.raytheon.uf.viz.collaboration.ui.session.PeerToPeerView;
import com.raytheon.uf.viz.collaboration.ui.session.SessionView;
import com.raytheon.uf.viz.core.icon.IconUtil;
import com.raytheon.uf.viz.drawing.PathToolbar;

/**
 * This class is the main view to display the user's information and allow the
 * user to create sessions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 1, 2012             rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class CollaborationGroupView extends ViewPart implements IPartListener {
    public static final String ID = "com.raytheon.uf.viz.collaboration.ui.CollaborationGroupView";

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(CollaborationGroupView.class);

    private static String ORPHAN_GROUP_ID = "Orphan Users";

    private SessionGroup activeSessionGroup;

    private TreeViewer usersTreeViewer;

    CollaborationGroup topLevel;

    Map<String, String[]> groupMap;

    private Action createSessionAction;

    private Action linkToEditorAction;

    private Action inviteAction;

    private Action joinAction;

    private Action joinCollaborationAction;

    private Action peerToPeerChatAction;

    private Action logonAction;

    private Action logoutAction;

    private Action aliasAction;

    private Action renameAction;

    private Action addGroupAction;

    private Action addUserAction;

    private Action selectGroups;

    private Action removeGroupAction;

    private Action removeUserAction;

    private Action changeStatusMessageAction;

    private Action changeStatusAction;

    private Action changePasswordAction;

    private Action drawToolbarAction;

    // private Action refreshActiveSessionsAction;

    private Action collapseAllAction;

    private TreeEditor treeEditor;

    /**
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        createActions();
        createToolbar();
        createMenubar();

        getViewSite().getWorkbenchWindow().getPartService()
                .addPartListener(this);

        createUsersTree(parent);
        addDoubleClickListeners();
        createContextMenu();
        if (CollaborationDataManager.getInstance().isConnected() == false) {
            usersTreeViewer.getTree().setEnabled(false);
        }
    }

    /**
     * 
     */
    private void createActions() {
        Bundle bundle = Activator.getDefault().getBundle();

        createSessionAction = new Action("Create Session...") {
            @Override
            public void run() {
                createSession();
            }

        };
        createSessionAction.setImageDescriptor(IconUtil.getImageDescriptor(
                bundle, "add_collaborate.gif"));
        createSessionAction.setEnabled(CollaborationDataManager.getInstance()
                .isConnected());

        linkToEditorAction = new Action("Link Editor to Chat Session",
                Action.AS_CHECK_BOX) {
            @Override
            public void run() {
                CollaborationDataManager.getInstance().setLinkCollaboration(
                        isChecked());
            }
        };
        linkToEditorAction.setImageDescriptor(IconUtil.getImageDescriptor(
                bundle, "link_to_editor.gif"));
        linkToEditorAction.setChecked(CollaborationDataManager.getInstance()
                .getLinkCollaboration());

        inviteAction = new Action("Invite...") {
            @Override
            public void run() {
                System.out.println("Invite... to join session" + getId());
                String sessionId = getId();
                IVenueSession session = CollaborationDataManager.getInstance()
                        .getSession(sessionId);
                String roomName = session.getVenue().getInfo()
                        .getVenueDescription();
                List<String> ids = new ArrayList<String>();

                for (CollaborationUser user : getSelectedUsers()) {
                    String id = user.getId();
                    System.out.println("Add Selected User: " + id);
                    ids.add(id);
                }
                IVenueInfo info = session.getVenue().getInfo();
                System.out.println("room: " + info.getVenueName());
                System.out.println("subject: "
                        + session.getVenue().getInfo().getVenueSubject());
                try {
                    session.sendInvitation(ids, session.getVenue().getInfo()
                            .getVenueSubject());
                } catch (CollaborationException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error sending invitiation", e);
                }
            };
        };
        inviteAction.setImageDescriptor(IconUtil.getImageDescriptor(bundle,
                "invite.gif"));
        inviteAction
                .setToolTipText("Invite selected user(s) to join a session.");

        joinAction = new Action("Join Session") {
            @Override
            public void run() {
                createJoinCollaboration();
            }
        };

        peerToPeerChatAction = new Action("Chat") {
            @Override
            public void run() {
                createP2PChat(getId());
            }
        };
        peerToPeerChatAction.setImageDescriptor(IconUtil.getImageDescriptor(
                bundle, "chats.gif"));

        logonAction = new Action("Login...") {
            @Override
            public void run() {
                populateTree();
            }
        };
        logonAction.setImageDescriptor(IconUtil.getImageDescriptor(bundle,
                "login.png"));

        logoutAction = new Action("Logout") {
            @Override
            public void run() {
                performLogout();
            }
        };

        logoutAction.setImageDescriptor(IconUtil.getImageDescriptor(bundle,
                "logout.gif"));

        aliasAction = new Action("Alias") {
            @Override
            public void run() {
                aliasItem();
                System.out.println("Alias");
            };
        };

        renameAction = new Action("Rename") {
            @Override
            public void run() {
                System.out.println("Rename action");
                nyiFeature("Rename");
            }
        };

        addUserAction = new Action("Add User") {
            public void run() {
                // addUsersToGroup();
                nyiFeature("Add User to a Group");
            };
        };
        addUserAction.setImageDescriptor(IconUtil.getImageDescriptor(bundle,
                "add_contact.gif"));
        addUserAction.setEnabled(false);

        addGroupAction = new Action("Create Group") {
            public void run() {
                System.out.println("Create Group here");
                nyiFeature("Create Group");
            };
        };
        addGroupAction.setImageDescriptor(IconUtil.getImageDescriptor(bundle,
                "add_group.gif"));
        addGroupAction.setEnabled(false);

        selectGroups = new Action("Select System Groups...") {
            public void run() {
                System.out.println("Select System Groups to Display...");
                nyiFeature("Select System Groups.");
            }
        };
        selectGroups.setEnabled(false);

        changeStatusMessageAction = new Action("Change Status Message...") {
            public void run() {
                changeStatusMessage();
            };
        };
        changeStatusMessageAction.setEnabled(false);

        changePasswordAction = new Action("Change password...") {
            public void run() {
                changePassword();
            };
        };
        changePasswordAction.setEnabled(false);

        changeStatusAction = new Action("Change Status",
                Action.AS_DROP_DOWN_MENU) {
            public void run() {
                changeStatus(getId());
            };
        };
        changeStatusAction.setEnabled(false);

        // refreshActiveSessionsAction = new Action("Refresh") {
        // public void run() {
        // System.out.println("Refresh Active Sessions");
        // refreshActiveSessions();
        // }
        // };
        // refreshActiveSessionsAction.setImageDescriptor(IconUtil
        // .getImageDescriptor(bundle, "refresh.gif"));
        // refreshActiveSessionsAction
        // .setToolTipText("Refresh the Active Sessions Entries.");

        collapseAllAction = new Action("Collapse All") {
            public void run() {
                usersTreeViewer.collapseAll();
            }
        };
        collapseAllAction.setImageDescriptor(IconUtil.getImageDescriptor(
                bundle, "collapseall.gif"));

        IMenuCreator creator = new IMenuCreator() {

            Menu menu;

            @Override
            public Menu getMenu(Menu parent) {
                menu = new Menu(parent);
                fillStatusMenu(menu);
                return menu;
            }

            @Override
            public Menu getMenu(Control parent) {
                menu = new Menu(parent);
                fillStatusMenu(menu);
                return menu;
            }

            @Override
            public void dispose() {
                menu.dispose();
            }
        };
        changeStatusAction.setMenuCreator(creator);

        removeGroupAction = new Action("Remove Group") {
            public void run() {
            };
        };
        removeUserAction = new Action("Remove User") {
            public void run() {
            };
        };

        drawToolbarAction = new Action("Drawing Toolbar") {
            @Override
            public void run() {
                PathToolbar.getToolbar().open();
            }
        };
        drawToolbarAction.setImageDescriptor(IconUtil.getImageDescriptor(
                com.raytheon.uf.viz.drawing.Activator.getDefault().getBundle(),
                "draw.gif"));
    }

    private void changePassword() {
        System.out.println("Change password here");
        ChangePasswordDialog dialog = new ChangePasswordDialog(Display
                .getCurrent().getActiveShell());
        dialog.open();

        Object result = dialog.getReturnValue();
        if (result != null) {
            char[] password = result.toString().toCharArray();
            SessionManager sessionManager = CollaborationDataManager
                    .getInstance().getSessionManager();
            try {
                sessionManager.getAccountManager().changePassword(password);
            } catch (CollaborationException e) {
                // TODO Auto-generated catch block. Please revise as
                // appropriate.
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
    }

    private void changeStatusMessage() {
        ChangeStatusDialog dialog = new ChangeStatusDialog(Display.getCurrent()
                .getActiveShell());
        dialog.open();

        LoginData loginData = (LoginData) dialog.getReturnValue();
        if (loginData != null) {
            CollaborationDataManager.getInstance().fireModifiedPresence();
        }
    }

    private void changeStatus(String status) {
        CollaborationDataManager manager = CollaborationDataManager
                .getInstance();
        LoginData loginData = manager.getLoginData();
        int index = Integer.parseInt(status);
        IPresence.Mode mode = CollaborationUtils.statusModes[index];
        if (mode != loginData.getMode()) {
            loginData.setMode(mode);
            manager.fireModifiedPresence();
            LoginDialog.saveUserLoginData(loginData);
        }
    }

    /**
     * This displays a warning dialog then closes all collaboration views and
     * disconnects from the server.
     */
    private void performLogout() {
        MessageBox messageBox = new MessageBox(Display.getCurrent()
                .getActiveShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
        messageBox.setText("Log Out of Collaboration");
        messageBox.setMessage("Logging out will sever your\n"
                + "connection to the server and\n"
                + "close all collaboration views\n" + "and editors.");
        int result = messageBox.open();
        if (result == SWT.OK) {
            // Close all Session Views
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().hideView(this);
            for (IViewReference ref : PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getViewReferences()) {
                IViewPart view = ref.getView(false);
                if (view instanceof AbstractSessionView) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().hideView(view);
                }
            }

            // Close all Collaboration Editors.
            for (IEditorReference ref : PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .getEditorReferences()) {
                IEditorPart editor = ref.getEditor(false);
                if (editor instanceof CollaborationEditor) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().hideEditor(ref);
                }
            }
            CollaborationDataManager.getInstance().closeManager();
        }
    }

    /**
     * 
     */
    protected void aliasItem() {
        Control oldEditor = treeEditor.getEditor();
        if (oldEditor != null) {
            oldEditor.dispose();
        }
        TreeSelection selection = (TreeSelection) usersTreeViewer
                .getSelection();
        selection.getFirstElement();

        Text newEditor = new Text(usersTreeViewer.getTree(), SWT.NONE);
        newEditor.setText(((CollaborationNode) selection.getFirstElement())
                .getId());
        newEditor.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text = (Text) treeEditor.getEditor();
                treeEditor.getItem().setText(text.getText());
            }
        });
        newEditor.selectAll();
        newEditor.setFocus();
        treeEditor.setEditor(newEditor, usersTreeViewer.getTree()
                .getSelection()[0]);

    }

    private void fillStatusMenu(Menu menu) {
        System.out.println("fillStatusMenu");
        for (int index = 0; index < CollaborationUtils.statusModes.length; ++index) {
            IPresence.Mode mode = CollaborationUtils.statusModes[index];
            Action action = new Action(mode.getMode()) {
                public void run() {
                    changeStatusAction.setId(getId());
                    changeStatusAction.run();
                };
            };
            action.setId(Integer.toString(index));
            ActionContributionItem item = new ActionContributionItem(action);
            action.setImageDescriptor(IconUtil.getImageDescriptor(Activator
                    .getDefault().getBundle(), mode.name().toLowerCase()
                    + ".gif"));
            item.fill(menu, -1);
        }
    }

    private void createToolbar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(createSessionAction);
        mgr.add(collapseAllAction);
        mgr.add(linkToEditorAction);
    }

    private void createMenubar() {
        IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
        createMenu(mgr);
        mgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.removeAll();
                createMenu(manager);
            }
        });
    }

    private void createMenu(IMenuManager mgr) {
        mgr.add(addGroupAction);
        mgr.add(addUserAction);
        mgr.add(selectGroups);
        mgr.add(new Separator());
        mgr.add(changeStatusAction);
        mgr.add(changeStatusMessageAction);
        mgr.add(changePasswordAction);
        mgr.add(new Separator());

        mgr.add(drawToolbarAction);

        mgr.add(new Separator());
        if (CollaborationDataManager.getInstance().isConnected()) {
            mgr.add(logoutAction);
        } else {
            mgr.add(logonAction);
        }
    }

    private void createSession() {
        CollaborationDataManager manager = CollaborationDataManager
                .getInstance();
        SessionManager sessionManager = manager.getSessionManager();
        if (sessionManager == null) {
            System.err.println("Unable to get session manager");
            return;
        }

        // TODO determine invite based on if any users/groups selected.

        CreateSessionDialog dialog = new CreateSessionDialog(Display
                .getCurrent().getActiveShell(), usersSelected());
        dialog.open();

        CreateSessionData result = (CreateSessionData) dialog.getReturnValue();

        if (result != null) {
            if (result.isCollaborationSession()) {
                createCollaborationView(result);
            } else {
                createTextOnlyView(result);
            }
        }

        try {
            if (result.isInviteUsers()) {
                IVenueSession session = CollaborationDataManager.getInstance()
                        .getSession(result.getSessionId());
                List<String> usersList = new ArrayList<String>();
                for (CollaborationUser user : getSelectedUsers()) {
                    usersList.add(user.getId());
                }
                String b = result.getInviteMessage();
                session.sendInvitation(usersList, b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCollaborationView(CreateSessionData result) {
        String sessionId = result.getSessionId();
        try {
            PlatformUI
                    .getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .showView(CollaborationSessionView.ID, sessionId,
                            IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to open collaboration sesson", e);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, "Unexpected excepton", e);
        }
    }

    /**
     * Generate a view for text only session.
     * 
     * @param result
     */
    private void createTextOnlyView(CreateSessionData result) {
        String sessionId = result.getSessionId();
        try {
            PlatformUI
                    .getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .showView(SessionView.ID, sessionId,
                            IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to open text  only chat session", e);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, "Unexpected exception", e);
        }
    }

    private void createJoinCollaboration() {
        IStructuredSelection selection = (IStructuredSelection) usersTreeViewer
                .getSelection();
        Object[] nodes = selection.toArray();
        for (Object node : nodes) {
            if (node instanceof SessionGroup) {
                SessionGroup sg = (SessionGroup) node;
                // System.out.println("Join: " + sg.getId());
                CollaborationDataManager manager = CollaborationDataManager
                        .getInstance();
                String sessionId = manager.joinCollaborationSession(
                        sg.getText(), sg.getId());
                sg.setId(sessionId);
                try {
                    IViewPart part = PlatformUI
                            .getWorkbench()
                            .getActiveWorkbenchWindow()
                            .getActivePage()
                            .showView(CollaborationSessionView.ID, sessionId,
                                    IWorkbenchPage.VIEW_ACTIVATE);
                } catch (PartInitException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Unable to open collaboation sesson", e);
                } catch (Exception e) {
                    statusHandler.handle(Priority.ERROR, "Unexpected excepton",
                            e);
                }
            }
        }
    }

    /**
     * Generate a view to for messages to/from a user via a Peer to Peer
     * connection.
     * 
     * @param user
     */
    private void createP2PChat(String user) {
        System.err.println("createPrivateChat with " + user);
        try {
            PlatformUI
                    .getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .showView(PeerToPeerView.ID, user,
                            IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to open chat", e);
        }
    }

    /**
     * Generate the Tree View component and add tooltip tracking.
     * 
     * @param parent
     */
    private void createUsersTree(Composite parent) {
        Composite child = new Composite(parent, SWT.NONE);
        child.setLayout(new GridLayout(1, false));
        child.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        usersTreeViewer = new TreeViewer(child);
        usersTreeViewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        TreeColumn column = new TreeColumn(usersTreeViewer.getTree(), SWT.NONE);
        column.setWidth(200); // any width would work

        usersTreeViewer.setContentProvider(new UsersTreeContentProvider());
        usersTreeViewer.setLabelProvider(new UsersTreeLabelProvider());
        usersTreeViewer.setSorter(new UsersTreeViewerSorter());
        topLevel = new CollaborationGroup("kickstart");
        usersTreeViewer.setInput(topLevel);

        treeEditor = new TreeEditor(usersTreeViewer.getTree());
        usersTreeViewer.getTree().addMouseTrackListener(
                new MouseTrackAdapter() {
                    @Override
                    public void mouseHover(MouseEvent e) {
                        TreeItem item = usersTreeViewer.getTree().getItem(
                                new Point(e.x, e.y));
                        if (item != null) {
                            CollaborationNode node = (CollaborationNode) item
                                    .getData();
                            StringBuilder builder = new StringBuilder();
                            if (node instanceof CollaborationUser) {
                                builder.append("ID: ").append(node.getId());
                                CollaborationUser user = (CollaborationUser) node;
                                builder.append("\nStatus: ");
                                if (user.getType() == Type.UNAVAILABLE) {
                                    builder.append("Offline");
                                } else {
                                    builder.append(user.getMode().getMode());

                                    // builder.append("Type: ").append(user.getType())
                                    // .append("\n");
                                    String message = user.getStatusMessage();
                                    if (message != null && message.length() > 0) {
                                        builder.append("\n");
                                        builder.append("Message: \"").append(
                                                user.getStatusMessage() + "\"");
                                    }
                                }
                            } else if (node instanceof SessionGroup
                                    && ((SessionGroup) node).isSessionRoot() == false) {
                                builder.append("ID: ").append(node.getId());
                                IVenueInfo info = CollaborationDataManager
                                        .getInstance().getSession(node.getId())
                                        .getVenue().getInfo();
                                builder.append("\nVenueName: ")
                                        .append(info.getVenueName())
                                        .append("\n");
                                // builder.append("VenueID: ")
                                // .append(info.getVenueID()).append("\n");
                                builder.append("Subject: ")
                                        .append(info.getVenueSubject())
                                        .append("\n");
                                builder.append("ParticipantCount: ").append(
                                        info.getParticipantCount());
                            }
                            usersTreeViewer.getTree().setToolTipText(
                                    builder.toString());
                        } else {
                            usersTreeViewer.getTree().setToolTipText("");
                        }
                    }
                });
    }

    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(usersTreeViewer.getControl());
        usersTreeViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, usersTreeViewer);
    }

    /**
     * Filling the context menu for the tree depending on whether the item is a
     * group or a user
     * 
     * @paramfillContextMenu manager
     */
    private void fillContextMenu(IMenuManager manager) {
        IStructuredSelection selection = (IStructuredSelection) usersTreeViewer
                .getSelection();
        Object o = selection.getFirstElement();
        if (o instanceof LoginUser) {
            createMenu(manager);
            return;
        }

        if (o instanceof SessionGroup) {
            SessionGroup sessionGroup = (SessionGroup) o;
            if (sessionGroup.isSessionRoot()) {
                manager.add(createSessionAction);
                // manager.add(refreshActiveSessionsAction);
            } else {
                manager.add(joinAction);
            }
            return;
        }

        if (o instanceof CollaborationUser) {
            CollaborationUser user = (CollaborationUser) o;
            if (user.getType() == Type.AVAILABLE) {
                MenuManager inviteManager = new MenuManager("Invite to...");
                // get current open chats
                Map<String, IVenueSession> sessions = CollaborationDataManager
                        .getInstance().getSessions();
                for (String name : sessions.keySet()) {
                    final ISession session = sessions.get(name);
                    if (session != null) {
                        final IVenueInfo info = sessions.get(name).getVenue()
                                .getInfo();
                        if (info != null) {
                            System.out.println("Add to Invite To menu: "
                                    + info.getVenueDescription());
                            Action action = new Action(
                                    info.getVenueDescription()) {
                                /*
                                 * (non-Javadoc)
                                 * 
                                 * @see org.eclipse.jface.action.Action#run()
                                 */
                                @Override
                                public void run() {
                                    inviteAction.setId(session.getSessionId());
                                    inviteAction.run();
                                }
                            };
                            action.setId(info.getVenueID());
                            inviteManager.add(action);
                        }
                    }
                }
                manager.add(inviteManager);
                manager.add(peerToPeerChatAction);
                peerToPeerChatAction.setId(user.getId());
                if (user.isLocal()) {
                    manager.add(addUserAction);
                    manager.add(addGroupAction);
                    manager.add(removeUserAction);
                    manager.add(removeGroupAction);
                }
                manager.add(new Separator());
                manager.add(createSessionAction);
            }
        } else if (o instanceof CollaborationGroup) {
            CollaborationGroup group = (CollaborationGroup) o;
            manager.add(createSessionAction);
            if (group.isLocal()) {
                manager.add(addUserAction);
                manager.add(addGroupAction);
                manager.add(removeGroupAction);
                manager.add(renameAction);
            }
        }
    }

    /**
     * Get entries for all part of the Tree Viewer and enable actions.
     */
    protected void populateTree() {
        CollaborationDataManager manager = CollaborationDataManager
                .getInstance();
        SessionManager sessionManager = manager.getSessionManager();
        topLevel.removeChildren();
        if (sessionManager == null) {
            usersTreeViewer.getTree().setEnabled(false);
            addGroupAction.setEnabled(false);
            addUserAction.setEnabled(false);
            selectGroups.setEnabled(false);
            changeStatusAction.setEnabled(false);
            drawToolbarAction.setEnabled(false);
            changeStatusMessageAction.setEnabled(false);
            changePasswordAction.setEnabled(false);
            return;
        }
        addGroupAction.setEnabled(true);
        addUserAction.setEnabled(true);
        selectGroups.setEnabled(true);
        changeStatusAction.setEnabled(true);
        drawToolbarAction.setEnabled(true);
        changeStatusMessageAction.setEnabled(true);
        changePasswordAction.setEnabled(true);

        LoginUser user = new LoginUser(manager.getLoginId());
        topLevel.addChild(user);
        activeSessionGroup = new SessionGroup("Active Sessions");
        activeSessionGroup.setSessionRoot(true);
        topLevel.addChild(activeSessionGroup);

        populateActiveSessions();

        populateGroups();

        usersTreeViewer.getTree().setEnabled(true);
        usersTreeViewer.refresh(topLevel, true);
        createSessionAction.setEnabled(true);
    }

    /**
     * Clears and populates the Tree Viewer's active session node.
     */
    private void populateActiveSessions() {
        activeSessionGroup.removeChildren();
        try {
            CollaborationDataManager manager = CollaborationDataManager
                    .getInstance();
            for (IViewReference ref : getViewSite().getWorkbenchWindow()
                    .getActivePage().getViewReferences()) {
                IViewPart viewPart = ref.getView(false);
                if (viewPart instanceof SessionView) {
                    String sessionId = viewPart.getViewSite().getSecondaryId();
                    SessionGroup child = new SessionGroup(sessionId);
                    child.setText(manager.getSession(sessionId).getVenue()
                            .getInfo().getVenueDescription());
                    activeSessionGroup.addChild(child);
                }
            }
        } catch (NullPointerException e) {
            // Ignore happens when creating view when starting CAVE.
        }
    }

    /**
     * Clear and populate the groups from the roster manager entries.
     */
    private void populateGroups() {
        CollaborationDataManager manager = CollaborationDataManager
                .getInstance();
        for (CollaborationNode node : topLevel.getChildren()) {
            if (!(node instanceof LoginUser || node instanceof SessionGroup)) {
                topLevel.removeChild(node);
            }
        }

        for (String groupId : manager.getGroups(false)) {
            CollaborationGroup child = new CollaborationGroup(groupId);
            for (String userId : manager.getUsersInGroup(groupId)) {
                CollaborationUser user = new CollaborationUser(userId);
                child.addChild(user);
            }
            topLevel.addChild(child);
        }
        List<String> orphanList = manager.getOrphanUsers();
        if (orphanList.size() > 0) {
            OrphanGroup orphanGroup = new OrphanGroup(ORPHAN_GROUP_ID);
            for (String userId : orphanList) {
                orphanGroup.addChild(new CollaborationUser(userId));
            }
            topLevel.addChild(orphanGroup);
        }
    }

    /**
     * @return
     */
    private boolean usersSelected() {
        return getSelectedUsers().size() > 0;
    }

    /**
     * Get a unique set of selected users that have a Type of AVAILABLE. This
     * does a recursive search so will work even when groups contain groups.
     * 
     * @return
     */
    private Set<CollaborationUser> getSelectedUsers() {
        Set<CollaborationUser> selectedUsers = new HashSet<CollaborationUser>();
        IStructuredSelection selection = (IStructuredSelection) usersTreeViewer
                .getSelection();
        Object[] nodes = selection.toArray();

        for (Object node : nodes) {
            if (node instanceof CollaborationUser) {
                CollaborationUser user = (CollaborationUser) node;
                if ((user instanceof LoginUser) == false
                        && user.getType() == Type.AVAILABLE) {
                    selectedUsers.add((CollaborationUser) node);
                }
            } else if ((node instanceof SessionGroup) == false) {
                selectedUsers
                        .addAll(getSelectedUsers((CollaborationGroup) node));
            }
        }

        return selectedUsers;
    }

    /**
     * This recursively searches group Nodes and returns all users with Type
     * AVAILABLE.
     * 
     * @param groupNode
     * @return users
     */
    private Set<CollaborationUser> getSelectedUsers(CollaborationGroup groupNode) {
        CollaborationDataManager manger = CollaborationDataManager
                .getInstance();
        Set<CollaborationUser> selectedUsers = new HashSet<CollaborationUser>();
        for (CollaborationNode node : groupNode.getChildren()) {
            if (node instanceof CollaborationUser) {
                CollaborationNode user = (CollaborationUser) node;
                if (manger.getUser(user.getId()).getType() == Type.AVAILABLE) {
                    selectedUsers.add((CollaborationUser) node);
                }
            } else if (node instanceof CollaborationGroup) {
                selectedUsers
                        .addAll(getSelectedUsers((CollaborationGroup) node));
            }
        }
        return selectedUsers;
    }

    private void addUsersToGroup() {
        // TODO, one add user is implemented, remove this
        if (true) {
            nyiFeature("Add user is not yet implemented");
            return;
        }
        Set<CollaborationUser> users = getSelectedUsers();
        IStructuredSelection selection = (IStructuredSelection) usersTreeViewer
                .getSelection();
        Object[] nodes = selection.toArray();
        System.out.println("Add User: " + users.size());
        IRosterManager rosterManager = CollaborationDataManager.getInstance()
                .getSessionManager().getRosterManager();
        for (Object node : nodes) {
            if (node instanceof CollaborationUser) {
                CollaborationUser user = (CollaborationUser) node;
                try {
                    String account = user.getId();
                    String nickname = Tools.parseName(account);
                    // String[] groups = new String[] { "rogerTestGroup" };
                    String[] groups = null;
                    rosterManager.sendRosterAdd(account, nickname, groups);
                    // rosterManager.sendRosterRemove(userId)
                } catch (CollaborationException e) {
                    // TODO Auto-generated catch block. Please revise as
                    // appropriate.
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            }
            // for (CollaborationUser user : users) {
            // try {
            // String account = user.getId();
            // String nickname = account.substring(0, account.indexOf('@'));
            // rosterManager.sendRosterAdd(account, nickname,
            // new String[] { "rogertestgroup" });
            // } catch (CollaborationException e) {
            // // TODO Auto-generated catch block. Please revise as
            // // appropriate.
            // statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
            // e);
            // }
        }
    }

    /**
     * This should go away as all actions are implemented.
     * 
     * @param feature
     */
    private void nyiFeature(String feature) {
        MessageBox messageBox = new MessageBox(Display.getCurrent()
                .getActiveShell(), SWT.ICON_INFORMATION);
        messageBox.setText("Not Yet Implemented");
        messageBox.setMessage(feature);
        messageBox.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    private void addDoubleClickListeners() {
        usersTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                System.out.println("Double click");
                TreeSelection selection = (TreeSelection) event.getSelection();
                CollaborationNode node = (CollaborationNode) selection
                        .getFirstElement();
                if (node instanceof SessionGroup) {
                    // SessionGroup group = (SessionGroup) node;
                    // if (!group.isSessionRoot()) {
                    // createJoinCollaboration();
                    // }
                } else if (node instanceof CollaborationUser) {
                    CollaborationUser user = (CollaborationUser) node;
                    if (user.getType() == Type.AVAILABLE) {
                        String loginUserId = CollaborationDataManager
                                .getInstance().getLoginId();
                        if (loginUserId.equals(node.getId()) == false) {
                            createP2PChat(node.getId());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        System.out.println("Disposing: " + getClass().getName());
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partActivated(IWorkbenchPart part) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart
     * )
     */
    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partClosed(IWorkbenchPart part) {
        if (part instanceof SessionView) {
            SessionView sessionView = (SessionView) part;
            String sessionId = sessionView.getViewSite().getSecondaryId();
            System.out.println("partClosed remove sessionId: " + sessionId);
            for (CollaborationNode node : activeSessionGroup.getChildren()) {
                if (sessionId.equals(node.getId())) {
                    activeSessionGroup.removeChild(node);
                    usersTreeViewer.refresh(activeSessionGroup);
                    break;
                }
            }
        } else if (part == this) {
            CollaborationDataManager.getInstance().unRegisterEventHandler(this);
            getViewSite().getWorkbenchWindow().getPartService()
                    .removePartListener(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart
     * )
     */
    @Override
    public void partDeactivated(IWorkbenchPart part) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partOpened(IWorkbenchPart part) {
        if (part instanceof SessionView) {
            SessionView sessionView = (SessionView) part;
            String sessionId = sessionView.getViewSite().getSecondaryId();
            System.out.println("partOpen add sessionId: " + sessionId);
            SessionGroup child = new SessionGroup(sessionId);
            child.setText(CollaborationDataManager.getInstance()
                    .getSession(sessionId).getVenue().getInfo()
                    .getVenueDescription());
            activeSessionGroup.addChild(child);
            usersTreeViewer.refresh(activeSessionGroup);
        } else if (part == this) {
            CollaborationDataManager.getInstance().registerEventHandler(this);
            populateTree();
            usersTreeViewer.refresh();
        }
    }

    /**
     * Refresh the labels on the View Tree to reflect presence change.
     * 
     * @param rosterEntry
     */
    @Subscribe
    public void handleModifiedPresence(IRosterEntry rosterEntry) {
        // Only need to update the usersTreeViewer.
        System.out.println("group view roster entry for:"
                + rosterEntry.getUser().getName() + "@"
                + rosterEntry.getUser().getHost() + " "
                + rosterEntry.getPresence().getMode() + "/"
                + rosterEntry.getPresence().getType());
        refreshUser(CollaborationUtils.makeUserId(rosterEntry), topLevel);
        usersTreeViewer.refresh(topLevel, true);
    }

    private void refreshUser(String userId, CollaborationGroup groupNode) {
        for (CollaborationNode child : groupNode.getChildren()) {
            if (child instanceof CollaborationUser) {
                if (userId.equals(child.getId())) {
                    usersTreeViewer.refresh(child, true);
                }
            } else if (child instanceof CollaborationGroup) {
                refreshUser(userId, (CollaborationGroup) child);
            }
        }
    }

    @Subscribe
    public void handleRosterChangeEvent(IRosterChangeEvent rosterChangeEvent) {
        // Assume the CollaborationDataManager has updated the back end.
        // Only need to update the usersTreeView.
        System.out.println("CollaborationGroupView rosterChangeEvent<"
                + rosterChangeEvent.getType() + ">: "
                + rosterChangeEvent.getEntry().getUser().getFQName());
        IRosterEntry rosterEntry = rosterChangeEvent.getEntry();
        String userId = CollaborationUtils.makeUserId(rosterEntry);
        List<String> groupIds = new ArrayList<String>();
        for (IRosterGroup rosterGroup : rosterEntry.getGroups()) {
            groupIds.add(rosterGroup.getName());
        }
        switch (rosterChangeEvent.getType()) {
        case ADD:
            // Should be a rare event after initial population.
            OrphanGroup orphanGroup = null;
            if (groupIds.size() == 0) {
                // remove from all groups and add to Orphans.
                for (CollaborationNode node : topLevel.getChildren()) {
                    if (node instanceof OrphanGroup) {
                        CollaborationUser user = null;
                        orphanGroup = (OrphanGroup) node;
                        for (CollaborationNode child : orphanGroup
                                .getChildren()) {
                            if (userId.equals(child.getId())) {
                                user = (CollaborationUser) child;
                                break;
                            }
                        }
                        if (user == null) {
                            orphanGroup.addChild(new CollaborationUser(userId));
                            usersTreeViewer.refresh(orphanGroup);
                        }
                    } else if (node instanceof CollaborationGroup
                            && !(node instanceof SessionGroup)) {
                        CollaborationGroup groupNode = (CollaborationGroup) node;
                        for (CollaborationNode child : groupNode.getChildren()) {
                            if (userId.equals(child.getId())) {
                                if (!groupIds.contains(groupNode.getId())) {
                                    groupNode.removeChild(child);
                                    usersTreeViewer.refresh(groupNode);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (orphanGroup == null) {
                    orphanGroup = new OrphanGroup(ORPHAN_GROUP_ID);
                    orphanGroup.addChild(new CollaborationUser(userId));
                    topLevel.addChild(orphanGroup);
                    usersTreeViewer.refresh();
                }
            } else {
                // TODO add user to groups and remove from others including
                // orphan group.
                for (CollaborationNode node : topLevel.getChildren()) {
                    if (node instanceof CollaborationGroup
                            && !(node instanceof SessionGroup)) {
                        CollaborationGroup group = (CollaborationGroup) node;
                        if (group instanceof OrphanGroup) {
                            orphanGroup = (OrphanGroup) group;
                        }
                        boolean addUser = groupIds.contains(group.getId());
                        for (CollaborationNode child : group.getChildren()) {
                            if (userId.equals(child.getId())) {
                                if (addUser) {
                                    // User already in the group no need to
                                    // add.
                                    groupIds.remove(group.getId());
                                    addUser = false;
                                } else {
                                    // User no longer in this group.
                                    group.removeChild(child);
                                    usersTreeViewer.refresh(group);
                                }
                                break;
                            }
                        }
                        if (addUser) {
                            group.addChild(new CollaborationUser(userId));
                            groupIds.remove(group.getId());
                        }
                    }
                }

                boolean refreshTopLevel = false;
                if (orphanGroup != null
                        && orphanGroup.getChildren().size() == 0) {
                    topLevel.removeChild(orphanGroup);
                    refreshTopLevel = true;
                }

                // groups now contains new groups. See if they are on the
                // display list.
                if (groupIds.size() > 0) {
                    CollaborationDataManager manager = CollaborationDataManager
                            .getInstance();
                    for (String groupId : groupIds) {
                        if (manager.displayGroup(groupId)) {
                            CollaborationGroup groupNode = new CollaborationGroup(
                                    groupId);
                            topLevel.addChild(groupNode);
                            groupNode.addChild(new CollaborationUser(userId));
                        }
                    }
                    refreshTopLevel = true;
                }
                if (refreshTopLevel) {
                    usersTreeViewer.refresh(topLevel);
                }
            }
            break;
        case DELETE:
            // Should be a rare event.
            for (CollaborationNode node : topLevel.getChildren()) {
                if (node instanceof CollaborationGroup
                        && !(node instanceof SessionGroup)) {
                    CollaborationGroup groupNode = (CollaborationGroup) node;
                    for (CollaborationNode child : groupNode.getChildren()) {
                        if (userId.equals(child.getId())) {
                            groupNode.removeChild(child);
                            usersTreeViewer.refresh(groupNode);
                            break;
                        }
                    }
                }
            }
            break;
        case MODIFY:
            // Assume this only changes the presence of a user in the
            // desired
            // groups.
            // Since this is handled by the handleModifiedPresence nothing
            // needs
            // to be done.
            // for (String groupId : groups) {
            // for (CollaborationNode node : topLevel.getChildren()) {
            // if (node instanceof CollaborationGroup
            // && !(node instanceof SessionGroup)
            // && groupId.equals(node.getId())) {
            // CollaborationGroup groupNode = (CollaborationGroup) node;
            // for (CollaborationNode child : groupNode.getChildren()) {
            // if (userId.equals(child.getId())) {
            // usersTreeViewer.refresh(child, true);
            // break;
            // }
            // }
            // }
            // }
            // }
            break;
        default:
            statusHandler.handle(Priority.PROBLEM, "Unknown type: "
                    + rosterChangeEvent.getType());
            break;
        }
    }
}
