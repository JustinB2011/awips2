package gov.noaa.nws.ncep.viz.resourceManager.ui.loadRbd;

import gov.noaa.nws.ncep.viz.common.ui.NmapCommon;
import gov.noaa.nws.ncep.viz.resourceManager.timeline.TimelineControl;
import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsRequestableResourceData;
import gov.noaa.nws.ncep.viz.resources.INatlCntrsResourceData;
import gov.noaa.nws.ncep.viz.resources.manager.NmapResourceUtils;
import gov.noaa.nws.ncep.viz.resources.manager.RbdBundle;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceBndlLoader;
import gov.noaa.nws.ncep.viz.resources.time_match.NCTimeMatcher;
import gov.noaa.nws.ncep.viz.ui.display.NCMapEditor;
import gov.noaa.nws.ncep.viz.ui.display.NCMapRenderableDisplay;
import gov.noaa.nws.ncep.viz.ui.display.NCPaneManager.PaneLayout;
import gov.noaa.nws.ncep.viz.ui.display.NmapUiUtils;
import gov.noaa.nws.ncep.viz.ui.display.PaneID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.drawables.AbstractDescriptor;
import com.raytheon.uf.viz.core.drawables.AbstractRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.ResourceList;

/**
 * An SWT Composite control to load Rbds
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 10/20/08			#7		Greg Hull		Initial creation
 * 12/15/08        #43      Greg Hull    	Call NmapCommon methods
 * 04/22/09        #99      Greg Hull       Select/New Editor option
 * 07/17/09       #139      Greg Hull       Add double click.
 * 08/28/09       #148      Greg Hull       SWTDialog, Sash, Timeline
 * 09/22/09       #169      Greg Hull       Add Display Cntrls & Multi-Pane
 * 10/16/09       #169      Greg Hull       Animation Mode & Timeline fixes
 * 10/18/09       #169      Greg Hull       for NEW_PANE, show times from Display
 * 10/21/09       #180      Greg Hull       Rework for RBD's as Displays 
 * 01/26/10       #226      Greg Hull       refactored for new resourceManager Dialog
 * 09/02/10       #307      Greg Hull       implement Auto Update
 * 01/23/11                 Greg Hull       copy RBDs before loading.  
 * 01/24/11                 Greg Hull       added rsc_lviewer, prompt if user wants to 
 *                                          reload into exising editor of same name
 * 01/27/11       #408      Greg Hull       set Timeline gui with values from timeMatcher 
 *                                          instead of dominant resource defaults
 * 02/11/11       #408      Greg Hull       Add Load&Close btn  
 * 02/15/11       #408      Greg Hull       Add Edit RBD button.                               
 * 06/07/11       #445       Xilin Guo      Data Manager Performance Improvements
 * 07/11/11                 Greg Hull       Back out changes for #416.
 * 
 * </pre>
 * 
 * @author ghull
 * @version 1
 */
public class LoadRbdControl extends Composite {
    private Shell shell;

    private ResourceBndlLoader rbdLoader = null;

    private SashForm sash_form = null;

    private Group sel_rbds_grp = null;

    private Group load_opts_grp = null;

    private Combo spf_group_combo = null;

    private ListViewer spf_name_lviewer = null;

    private ListViewer rbd_lviewer = null;

    private ListViewer rsc_lviewer = null;

    private Button edit_rbd_btn = null;

    private Button sel_all_rbd_btn = null;

    private TimelineControl timelineControl = null;

    private Button load_btn = null;

    private Button load_and_close_btn = null;

    private Button auto_update_btn = null;

    private Button geo_sync_panes = null;

    private Button time_sync_panes = null;

    // this is the input for the rbd_lviewr content provider. It is
    // initially set with the rbds in an spf and is updated with edited rbds.
    //
    private ArrayList<RbdBundle> availRbdsList = null;

    private ArrayList<RbdBundle> seldRbdsList = null;

    // This is set each time an RBD is edited and cleared when a new SPF is
    // selected.
    // The map if from the name of the originally selected RBD to the edited
    // one.

    // private HashMap<String, RbdBundle> editedRbdMap = null;

    private Point initDlgSize = new Point(750, 860);

    private EditRbdDialog editRbdDlg = null;

    public LoadRbdControl(Composite parent) throws VizException {
        super(parent, SWT.NONE);
        shell = parent.getShell();

        rbdLoader = new ResourceBndlLoader("SPF Loader");

        seldRbdsList = new ArrayList<RbdBundle>();
        availRbdsList = new ArrayList<RbdBundle>();

        Composite top_form = this;
        top_form.setLayout(new GridLayout(1, true));

        top_form.setSize(750, 400);

        // top_form.setLayout( new FormLayout() );

        sash_form = new SashForm(top_form, SWT.VERTICAL);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;

        sash_form.setLayoutData(gd);
        sash_form.setSashWidth(10);

        FormData fd = new FormData();

        Composite top_sash_form = new Composite(sash_form, SWT.NONE);
        top_sash_form.setLayout(new FormLayout());

        fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.left = new FormAttachment(0, 5);
        fd.right = new FormAttachment(100, -5);
        fd.bottom = new FormAttachment(100, -5);
        top_sash_form.setLayoutData(fd);

        sel_rbds_grp = new Group(top_sash_form, SWT.SHADOW_NONE);
        sel_rbds_grp.setText("Select Display");
        sel_rbds_grp.setLayout(new FormLayout());

        fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.left = new FormAttachment(0, 5);
        fd.right = new FormAttachment(100, -5);
        fd.bottom = new FormAttachment(100, -5);
        sel_rbds_grp.setLayoutData(fd);

        spf_group_combo = new Combo(sel_rbds_grp, SWT.DROP_DOWN | SWT.READ_ONLY);
        fd = new FormData();
        // fd.width = 190;
        fd.top = new FormAttachment(0, 35);
        fd.left = new FormAttachment(0, 20);
        fd.right = new FormAttachment(30, -7 - 10);
        spf_group_combo.setLayoutData(fd);

        Label spf_grp_lbl = new Label(sel_rbds_grp, SWT.NONE);
        spf_grp_lbl.setText("SPF Group");

        fd = new FormData();
        fd.bottom = new FormAttachment(spf_group_combo, -3, SWT.TOP);
        fd.left = new FormAttachment(spf_group_combo, 0, SWT.LEFT);
        spf_grp_lbl.setLayoutData(fd);

        spf_name_lviewer = new ListViewer(sel_rbds_grp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);

        fd = new FormData();
        // fd.width = 130;
        fd.top = new FormAttachment(spf_group_combo, 40, SWT.BOTTOM);
        fd.left = new FormAttachment(spf_group_combo, 0, SWT.LEFT);
        // fd.left = new FormAttachment( 0, 10 );
        fd.bottom = new FormAttachment(100, -15);
        fd.right = new FormAttachment(30, -7);

        spf_name_lviewer.getList().setLayoutData(fd);

        Label spf_name_lbl = new Label(sel_rbds_grp, SWT.NONE);
        spf_name_lbl.setText("SPF Name");
        fd = new FormData();
        fd.bottom = new FormAttachment(spf_name_lviewer.getList(), -3, SWT.TOP);
        fd.left = new FormAttachment(spf_name_lviewer.getList(), 0, SWT.LEFT);
        spf_name_lbl.setLayoutData(fd);

        rbd_lviewer = new ListViewer(sel_rbds_grp, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        fd = new FormData();
        fd.width = 222;
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(30, 7); // spf_name_lviewer.getList(), 15,
                                             // SWT.RIGHT );
        // fd.bottom = new FormAttachment( spf_name_lviewer.getList(), 0,
        // SWT.BOTTOM );
        fd.bottom = new FormAttachment(100, -45);
        fd.right = new FormAttachment(66, -7);
        rbd_lviewer.getList().setLayoutData(fd);

        Label rbd_lbl = new Label(sel_rbds_grp, SWT.NONE);
        rbd_lbl.setText("RBDs");
        fd = new FormData();
        fd.bottom = new FormAttachment(rbd_lviewer.getList(), -3, SWT.TOP);
        fd.left = new FormAttachment(rbd_lviewer.getList(), 0, SWT.LEFT);
        rbd_lbl.setLayoutData(fd);

        edit_rbd_btn = new Button(sel_rbds_grp, SWT.PUSH);
        fd = new FormData();// 80,25 );
        edit_rbd_btn.setText("Edit RBD");
        fd.top = new FormAttachment(rbd_lviewer.getList(), 10, SWT.BOTTOM);
        fd.left = new FormAttachment(rbd_lviewer.getList(), 35, SWT.LEFT);
        edit_rbd_btn.setLayoutData(fd);
        edit_rbd_btn.setEnabled(false); // Not Implemented

        sel_all_rbd_btn = new Button(sel_rbds_grp, SWT.PUSH);
        fd = new FormData();// 80,25 );
        sel_all_rbd_btn.setText("Select All");
        fd.top = new FormAttachment(rbd_lviewer.getList(), 10, SWT.BOTTOM);
        fd.right = new FormAttachment(rbd_lviewer.getList(), -35, SWT.RIGHT);
        sel_all_rbd_btn.setLayoutData(fd);

        rsc_lviewer = new ListViewer(sel_rbds_grp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);

        fd = new FormData();
        fd.top = new FormAttachment(rbd_lviewer.getList(), 0, SWT.TOP);
        fd.left = new FormAttachment(66, 7);
        fd.right = new FormAttachment(100, -5);
        fd.bottom = new FormAttachment(100, -120);
        rsc_lviewer.getList().setLayoutData(fd);

        // do this as an indication that the list is view-only
        rsc_lviewer.getList().setBackground(sel_rbds_grp.getBackground());

        Label rsc_lbl = new Label(sel_rbds_grp, SWT.NONE);
        rsc_lbl.setText("Resources && Overlays");
        fd = new FormData();
        fd.bottom = new FormAttachment(rsc_lviewer.getList(), -3, SWT.TOP);
        fd.left = new FormAttachment(rsc_lviewer.getList(), 0, SWT.LEFT);
        rsc_lbl.setLayoutData(fd);

        load_opts_grp = new Group(sel_rbds_grp, SWT.SHADOW_NONE);
        load_opts_grp.setText("Display Options");
        load_opts_grp.setLayout(new FormLayout());

        fd = new FormData();
        fd.top = new FormAttachment(rsc_lviewer.getList(), 20, SWT.BOTTOM);
        fd.left = new FormAttachment(rsc_lviewer.getList(), 0, SWT.LEFT);
        fd.right = new FormAttachment(rsc_lviewer.getList(), 0, SWT.RIGHT);
        fd.bottom = new FormAttachment(100, -5);
        load_opts_grp.setLayoutData(fd);

        auto_update_btn = new Button(load_opts_grp, SWT.CHECK);
        fd = new FormData();
        auto_update_btn.setText("Auto Update");
        fd.top = new FormAttachment(0, 15);
        fd.left = new FormAttachment(0, 20);
        auto_update_btn.setLayoutData(fd);

        // TODO : implement and define the behaviours such as enforcing that
        // all RBDs have the same Geographic area (and if not then change?)
        // TODO : should this also control whether the frame times are synced?
        geo_sync_panes = new Button(load_opts_grp, SWT.CHECK);
        fd = new FormData();
        geo_sync_panes.setText("Geo-Syncronize Panes");
        fd.top = new FormAttachment(auto_update_btn, 7, SWT.BOTTOM);
        fd.left = new FormAttachment(auto_update_btn, 0, SWT.LEFT);
        geo_sync_panes.setLayoutData(fd);

        Group timeline_grp = new Group(sash_form, SWT.SHADOW_NONE);
        timeline_grp.setText("Select Timeline");
        fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.left = new FormAttachment(0, 5);
        fd.right = new FormAttachment(100, -5);
        fd.bottom = new FormAttachment(100, -5);
        timeline_grp.setLayoutData(fd);

        timeline_grp.setLayout(new GridLayout());

        timelineControl = new TimelineControl(timeline_grp);

        sash_form.setWeights(new int[] { 3, 2 });

        Composite load_form = new Composite(top_form, SWT.NONE);
        load_form.setLayout(new FormLayout());
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = false;
        gd.horizontalAlignment = SWT.FILL;
        // gd.verticalAlignment = SWT.FILL;

        load_form.setLayoutData(gd);

        load_and_close_btn = new Button(load_form, SWT.PUSH);
        load_and_close_btn.setText(" Load And Close ");
        fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.right = new FormAttachment(100, -10);

        load_and_close_btn.setLayoutData(fd);
        load_btn = new Button(load_form, SWT.PUSH);
        load_btn.setText("  Load RBDs  ");
        fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.right = new FormAttachment(load_and_close_btn, -20, SWT.LEFT);
        load_btn.setLayoutData(fd);

        addListeners();

        initWidgets();
    }

    // add the listeners and content providers
    //
    private void addListeners() {

        spf_group_combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                File spf_group_dir = new File(NmapResourceUtils
                        .getSpfGroupsDir(), spf_group_combo.getText());
                spf_name_lviewer.setInput(spf_group_dir);
                // 06/02/11. xguo--no pre-select
                // spf_name_lviewer.getList().select(0);
                setSeldSpfName();
            }
        });

        spf_name_lviewer.setContentProvider(NmapCommon
                .createSubDirContentProvider());
        spf_name_lviewer.setLabelProvider(NmapCommon.createFileLabelProvider());

        spf_name_lviewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        setSeldSpfName();
                    }
                });

        // spf_name_lviewer.getList().addListener( SWT.MouseDoubleClick, new
        // Listener() {
        // public void handleEvent(Event event) {
        // setSeldSpfName();
        // loadRBD();
        // // double click will leave the dialog up while "Load" will dispose it
        // // shell.dispose();
        // }
        // });

        // the input is the availRbdsList
        rbd_lviewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return availRbdsList.toArray();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        rbd_lviewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof RbdBundle) {
                    RbdBundle rbd = (RbdBundle) element;
                    if (rbd.isEdited()) {
                        return rbd.getRbdName() + "(E)";
                    } else {
                        return rbd.getRbdName();
                    }
                } else
                    return "Error: bad RBD element";
            }
        });

        rbd_lviewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        setSelectedRBDs();
                    }
                });

        // Input is an RbdBundle and return is a list of the resource names. For
        // multi-pane RBDs the paneLayout is given and each pane is labeled
        //
        rsc_lviewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                RbdBundle selRbd = (RbdBundle) inputElement;
                if (selRbd == null) {
                    return new String[0];
                }

                ArrayList<String> rscNames = new ArrayList<String>();
                boolean isMultiPane = (selRbd.getPaneLayout()
                        .getNumberOfPanes() > 1);

                // loop thru all of the resources in all of the panes and create
                // the
                // list of items for the viewer. These will depend on whether we
                // are
                // importing a single pane (in which case only the selected
                // pane's
                // resources are displayed) and whole RBDs (in which case all
                // pane's
                // resources are displayed) For multi-pane RBDs the format will
                // include
                // the name of the pane.
                for (int r = 0; r < selRbd.getPaneLayout().getRows(); r++) {
                    for (int c = 0; c < selRbd.getPaneLayout().getColumns(); c++) {
                        PaneID paneId = new PaneID(r, c);

                        NCMapRenderableDisplay disp = selRbd
                                .getDisplayPane(paneId);
                        AbstractDescriptor mapDescr = disp.getDescriptor();

                        if (isMultiPane) {
                            rscNames.add("Pane (" + paneId.toString() + ") : "
                                    + disp.getPredefinedAreaName());
                        } else {
                            rscNames.add("Single Pane : "
                                    + disp.getPredefinedAreaName());
                        }

                        for (ResourcePair rp : mapDescr.getResourceList()) {
                            if (rp.getResourceData() instanceof INatlCntrsResourceData) {
                                INatlCntrsResourceData ncRsc = (INatlCntrsResourceData) rp
                                        .getResourceData();
                                String rscName = ncRsc.getResourceName()
                                        .toString();

                                if (ncRsc.getIsEdited()) {
                                    rscName = rscName + "(E)";
                                }

                                if (isMultiPane) {
                                    rscName = "   " + rscName;
                                }

                                rscNames.add(rscName);
                            }
                        }

                    }
                }
                return rscNames.toArray();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        // double click is same as OK (except of course is can't work for
        // multiple selections)
        rbd_lviewer.getList().addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                editRbd();
            }
        });

        edit_rbd_btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                editRbd();
            }
        });

        sel_all_rbd_btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                rbd_lviewer.getList().selectAll();
                setSelectedRBDs();
            }
        });

        // if this button is enabled then only one rbd can be selected.
        // get the selected rbd and set the auto update flag
        auto_update_btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StructuredSelection sel_rbds = (StructuredSelection) rbd_lviewer
                        .getSelection();
                RbdBundle rbdSel = (RbdBundle) sel_rbds.getFirstElement();

                if (rbdSel != null) {// sanity check
                    rbdSel.setAutoUpdate(auto_update_btn.getSelection());
                }
            }
        });

        geo_sync_panes.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StructuredSelection sel_rbds = (StructuredSelection) rbd_lviewer
                        .getSelection();
                RbdBundle rbdSel = (RbdBundle) sel_rbds.getFirstElement();

                if (rbdSel != null) {// sanity check
                    rbdSel.setGeoSyncedPanes(geo_sync_panes.getSelection());
                }

                // TODO ; if enabling then loop thru the panes and check if
                // any of the panes have a different Area and if so prompt the
                // user to confirm they want to change them to the currently
                // selected Area. Then change all of the panes' areas.
                if (geo_sync_panes.getSelection()) {
                    String geoSyncArea = null;
                    AbstractRenderableDisplay panes[] = rbdSel.getDisplays();

                    for (AbstractRenderableDisplay pane : panes) {
                        if (pane instanceof AbstractRenderableDisplay) {
                            String area = ((NCMapRenderableDisplay) pane)
                                    .getPredefinedAreaName();
                            if (geoSyncArea == null) {
                                geoSyncArea = area;
                            }
                            if (!geoSyncArea.equals(area)) {
                                MessageBox mb = new MessageBox(NmapUiUtils
                                        .getCaveShell(), SWT.OK);
                                mb.setText("Info");
                                mb.setMessage("The panes in the RBD have different Predefined Areas,\n"
                                        + "but you may change the area after loading.");
                                mb.open();
                                break;
                            }
                        }
                    }
                }
            }
        });

        load_btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                loadRBD(false);
            }
        });

        load_and_close_btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                loadRBD(true);
            }
        });
    }

    private void initWidgets() throws VizException {

        spf_group_combo.setItems(NmapResourceUtils.getAvailSPFGroups());
        spf_group_combo.select(0);

        File spf_group_dir = new File(NmapResourceUtils.getSpfGroupsDir(),
                spf_group_combo.getText());

        if (!spf_group_dir.exists()) {
            System.out.println("sanity check: SPF group doesn't exist: "
                    + spf_group_dir.getName());
        }

        spf_name_lviewer.setInput(spf_group_dir);
        spf_name_lviewer.refresh();

        // this was not causing the rbd list to be loaded with rbd names so for
        // now just don't select
        // anything. TODO-set based on previously selected group.
        // 06/02/11, xguo. no pre-select
        // if( spf_name_lviewer.getList().getItemCount() > 0 ) {
        // spf_name_lviewer.getList().select(0);
        // setSeldSpfName();
        // setSelectedRBDs();
        // }

        geo_sync_panes.setSelection(true);
        // geo_sync_panes.setVisible(false);
        geo_sync_panes.setEnabled(false);

    }

    // the callback for the SPF list
    private void setSeldSpfName() {
        StructuredSelection seldSpfs = (StructuredSelection) spf_name_lviewer
                .getSelection();

        // the directory that has all the RBDs for this SPF
        File spfDir = (File) seldSpfs.getFirstElement();

        if (!spfDir.exists() || !spfDir.isDirectory()) { // sanity check
            System.out.println("Error; SPF Dir doesn't exist: "
                    + spfDir.getAbsolutePath());
            return;
        }

        // TODO: check to see if any of the RBDs were edited and prompt for
        // confirmation?
        availRbdsList.clear();

        for (File rbdFile : spfDir.listFiles(NmapCommon
                .createFileFilter(new String[] { ".xml" }))) {

            try {
                RbdBundle rbd = RbdBundle.unmarshalRBD(rbdFile, null);
                if (rbd != null) {

                    // This will initialize the timeMatcher by setting the
                    // dominantResourceData
                    // for the dominantResourceName and then calling
                    // setDominantResource to
                    // initialize the times.
                    // xguo,06/02/11. Not initialize time-line until the user
                    // selects it
                    // rbd.initTimeline();

                    availRbdsList.add(rbd);
                }
            } catch (VizException e) {
                System.out.println("Error unmarshalling rbd : "
                        + rbdFile.getAbsolutePath());
                System.out.println("   Error is : " + e.getMessage());
            }
        }

        rbd_lviewer.setInput(availRbdsList);
        rbd_lviewer.refresh();
        // 06/02/11, xguo. refresh Resource & Overlays window.
        rsc_lviewer.setInput(null);
        timelineControl.clearTimeline();

        // pre-select all the resource bundles
        // 06/02/11. No pre-select
        // rbd_lviewer.getList().selectAll();

        // setSelectedRBDs();
    }

    // called from the groups and rbd viewers after
    // one or more rbds have been selected. This will update the GUI and the
    // rbdLoader
    //
    private void setSelectedRBDs() {
        StructuredSelection sel_rbds = (StructuredSelection) rbd_lviewer
                .getSelection();
        Iterator sel_iter = sel_rbds.iterator();
        seldRbdsList.clear();
        RbdBundle rbdSel = null;

        while (sel_iter.hasNext()) {
            rbdSel = (RbdBundle) sel_iter.next();
            // if( rbdSel.getTimeMatcher().getDominantResource() == null ) {
            // System.out.println("Dominant Resource is null?");
            // }
            // else {
            // rbdSel.getTimeMatcher().loadTimes();
            // }
            rbdSel.initTimeline();
            seldRbdsList.add(rbdSel);
        }

        int numSeldRbds = seldRbdsList.size();

        // if one multi-pane RBD is selected then load the Panes viewer.
        //
        edit_rbd_btn.setEnabled((numSeldRbds == 1));
        load_btn.setEnabled((numSeldRbds > 0));
        load_and_close_btn.setEnabled((numSeldRbds > 0));

        timelineControl.setEnabled((numSeldRbds == 1));

        boolean widgetsVisible = false;

        if (numSeldRbds == 1) {

            rbdSel = seldRbdsList.get(0);

            NCTimeMatcher timeMatcher = rbdSel.getTimeMatcher();

            timelineControl.clearTimeline(); // removeAllAvailDomResources();

            // add all of the resources in the RBD as available
            // to be the dominant resource
            AbstractRenderableDisplay panes[] = seldRbdsList.get(0)
                    .getDisplays();

            for (AbstractRenderableDisplay pane : panes) {
                ResourceList rscList = pane.getDescriptor().getResourceList();
                for (ResourcePair rp : rscList) {
                    if (rp.getResourceData() instanceof AbstractNatlCntrsRequestableResourceData) {
                        timelineControl
                                .addAvailDomResource((AbstractNatlCntrsRequestableResourceData) rp
                                        .getResourceData());
                    }
                }
            }

            timelineControl.setTimeMatcher(timeMatcher);

            // determine if auto update is applicable. If the dominant
            // resource is a satellite or radar
            if (timeMatcher.getDominantResource() != null
                    && timeMatcher.getDominantResource().isAutoUpdateable()) {

                auto_update_btn.setEnabled(true);
                auto_update_btn.setSelection(rbdSel.isAutoUpdate());
            } else {
                auto_update_btn.setEnabled(false);
                auto_update_btn.setSelection(false);
            }

            rsc_lviewer.setInput(rbdSel);

            widgetsVisible = (rbdSel.getPaneLayout().getNumberOfPanes() > 1);

        } else {
            auto_update_btn.setEnabled(false);
            auto_update_btn.setSelection(false);

            rsc_lviewer.setInput(null);

            timelineControl.clearTimeline();
        }

        geo_sync_panes.setEnabled(widgetsVisible);
    }

    // update the avail spf groups, names and rbds after
    // the user saves an rbd
    public void updateDialog() {
        // reset the size of the dialog
        shell.setSize(initDlgSize);

        String saveSeldGroup = spf_group_combo.getText();
        spf_group_combo.setItems(NmapResourceUtils.getAvailSPFGroups());

        for (int i = 0; i < spf_group_combo.getItemCount(); i++) {
            if (saveSeldGroup.equals(spf_group_combo.getItem(i))) {
                spf_group_combo.select(i);
                // setSeldSpfName();
                return;
            }
        }
        // 06/02/11. No pre-select
        // spf_group_combo.select(0);
    }

    private boolean loadRBD(boolean close) {
        // sanity check. this shouldn't happen.
        if (seldRbdsList.size() == 0) {
            MessageBox mb = new MessageBox(NmapUiUtils.getCaveShell(), SWT.OK);
            mb.setText("No SPFs are Selected.");
            mb.setMessage("No SPFs are Selected.");
            mb.open();
            return false;
        }

        // Since the rbdLoader is not a UI thread it can not create an editor so
        // we need to
        // create the editors here and pass them to the rbdLoader

        rbdLoader.removeAllSeldRBDs();

        // timelineControl.updateTimeMatcher();

        for (RbdBundle rbdBndl : seldRbdsList) {
            String rbdName = rbdBndl.getRbdName();
            NCTimeMatcher timeMatcher = rbdBndl.getTimeMatcher();

            // Since rbdLoader uses the same resources in the rbdBundle to load
            // in the editor,
            // we will need to make a copy here so that future edits are not
            // immediately reflected in
            // the loaded display. The easiest way to do this is to marshal and
            // then unmarshal the rbd.
            try {
                File tempRbdFile;

                try {
                    tempRbdFile = File.createTempFile("tempRBD-", ".xml");

                    SerializationUtil.jaxbMarshalToXmlFile(rbdBndl,
                            tempRbdFile.getAbsolutePath());
                } catch (SerializationException e) {
                    throw new VizException(e);
                } catch (IOException e) {
                    throw new VizException(e);
                }

                rbdBndl = RbdBundle.unmarshalRBD(tempRbdFile, null);
                tempRbdFile.delete();

                // timeMatcher currently isn't marshalling completely.
                rbdBndl.setTimeMatcher(new NCTimeMatcher(timeMatcher));

                AbstractRenderableDisplay panes[] = rbdBndl.getDisplays();

                if (panes == null || panes.length == 0) {
                    throw new VizException(
                            "No Panes are defined for this RBD???.");
                }

                Integer paneCount = panes.length;
                PaneLayout paneLayout = rbdBndl.getPaneLayout();

                // if this is a multi-pane display
                // if( panes.length > 1 ) {
                // // Check that the user didn't change the pane layout and make
                // it so
                // // there aren't enough panes.
                // if( paneLayout.getPaneCount() < paneCount ) {
                // MessageDialog confirmDlg = new MessageDialog(
                // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                // "Confirm", null,
                // "A "+ paneLayout.toString() +
                // " Display doesn't have enough panes\n"+
                // "for the "+paneCount+" defined in this RBD.\n\n"+
                // "Do you wish to continue and create the necessary new panes?",
                // MessageDialog.QUESTION, new String[]{"Continue", "Cancel"},
                // 0);
                // confirmDlg.open();
                // if( confirmDlg.getReturnCode() == MessageDialog.CANCEL ) {
                // return false;
                // }
                // }
                //
                // // if there are 'extra' panes then create default RBDs to
                // load into these panes
                // // if( numRows*numCols > panes.length ) {
                // // while(
                // rbdLoader.getNumPaneRows()*rbdLoader.getNumPaneCols() >
                // rbdBndls.length ) {
                // // rbdLoader.addRBD( NmapCommon.getDefaultRBDFile() );
                // // rbdBndls = rbdLoader.getSelectedRBDs();
                // // }
                // // }
                // }

                NCMapEditor newEditor = (NCMapEditor) NmapUiUtils
                        .findDisplayByName(rbdName);

                // if there is already a display with this RBD name then prompt
                // if the user wants to replace it
                if (newEditor != null
                        && newEditor.getPaneLayout().getNumberOfPanes() == paneCount) {

                    MessageDialog confirmDlg = new MessageDialog(
                            PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow().getShell(),
                            "Reload?",
                            null,
                            "A Display already exists for RBD  "
                                    + rbdName
                                    + ".\n\n"
                                    + "Do you want to re-load this RBD in to Display '"
                                    + newEditor.getDisplayName()
                                    + "', or create a new Display?",
                            MessageDialog.QUESTION, new String[] { "Reload",
                                    "New Display" }, 0);
                    confirmDlg.open();

                    if (confirmDlg.getReturnCode() == MessageDialog.CANCEL) {
                        newEditor = null;
                    }
                }

                if (newEditor == null) {
                    newEditor = NmapUiUtils.createNatlCntrsEditor(rbdName,
                            paneLayout);
                }

                if (newEditor == null) {
                    throw new VizException(
                            "Unable to create an Editor for RBD " + rbdName);
                }

                if (panes.length > 1) {
                    newEditor
                            .setGeoSyncPanesEnabled(rbdBndl.isGeoSyncedPanes());
                    // newEditor.setTimeSyncPanesEnabled(
                    // rbdLoader.arePanesTimeSynced() );
                    // newEditor.selectPane( newEditor.getDisplayPanes()[0],
                    // true );
                }

                rbdBndl.setNcEditor(newEditor);

                // in the case where all rbds are selected initially and the
                // user loads without
                // selecting the timeline, we need to load the default timeline
                // based on the dflt
                // number of frames and times from the database.

                // rbdBndl.getTimeMatcher().loadTimes();

                rbdLoader.addRBD(rbdBndl);

            } catch (VizException e) {
                MessageBox mb = new MessageBox(NmapUiUtils.getCaveShell(),
                        SWT.OK);
                mb.setText("Error Loading RBD " + rbdName);
                mb.setMessage("Error Loading RBD " + rbdName + ".\n\n"
                        + e.getMessage());
                mb.open();
            }
        }

        // "loading rbds message box..."
        /*
         * VizApp.runAsync( new Runnable() { public void run() { MessageBox mb =
         * new MessageBox( PlatformUI.getWorkbench()
         * .getActiveWorkbenchWindow().getShell(), SWT.OK);
         * mb.setText("Loading Selected RBDs.....");
         * mb.setMessage("Loading Selected RBDs....."); mb.open(); } });
         */
        VizApp.runSync(rbdLoader);

        // They aren't going to like this if there is an error loading....
        if (close) {
            // if( Thread.currentThread().wait() rbdLoader.
            shell.dispose();
        }

        // rbdLoader.loadRBDs();
        // rbdLoader.schedule();
        return true;
    }

    private void editRbd() {
        int seldRbdIndx = rbd_lviewer.getList().getSelectionIndex();

        if (seldRbdIndx < 0) {
            // sanity check
            return;
        }

        RbdBundle rbdSel = availRbdsList.get(seldRbdIndx);

        if (rbdSel == null) { // sanity check
            return;
        }

        // make a copy of this RBD so that edits made and then canceled are not
        // saved to this object
        //
        try {
            try {
                NCTimeMatcher saveTimeMatcher = rbdSel.getTimeMatcher();
                File tempRbdFile = File.createTempFile("tempRBD-", ".xml");

                SerializationUtil.jaxbMarshalToXmlFile(rbdSel,
                        tempRbdFile.getAbsolutePath());
                rbdSel = RbdBundle.unmarshalRBD(tempRbdFile, null);

                rbdSel.setTimeMatcher(saveTimeMatcher);
                tempRbdFile.delete();

            } catch (SerializationException e) {
                throw new VizException(e);
            } catch (IOException e) {
                throw new VizException(e);
            }

            if (editRbdDlg == null) {
                editRbdDlg = new EditRbdDialog(shell, rbdSel);
            }
            if (editRbdDlg.isOpen()) {
            } else {
                RbdBundle editedRbd = editRbdDlg.open();
                if (editedRbd != null) {
                    availRbdsList.add(seldRbdIndx, editedRbd);
                    availRbdsList.remove(seldRbdIndx + 1);

                    rbd_lviewer.refresh(true);
                    rbd_lviewer.getList().select(seldRbdIndx);

                    setSelectedRBDs();

                    rsc_lviewer.refresh(true);
                }
            }

        } catch (VizException e1) {
            MessageDialog errDlg = new MessageDialog(
                    NmapUiUtils.getCaveShell(), "Error", null,
                    "Error Editing RBD", MessageDialog.ERROR,
                    new String[] { "Ok" }, 0);
            errDlg.open();
        }

        editRbdDlg = null;
    }
}