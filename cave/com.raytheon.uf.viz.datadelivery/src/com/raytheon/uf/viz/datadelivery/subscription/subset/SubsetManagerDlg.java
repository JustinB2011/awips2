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
package com.raytheon.uf.viz.datadelivery.subscription.subset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;

import com.google.common.base.Preconditions;
import com.raytheon.uf.common.datadelivery.registry.AdhocSubscription;
import com.raytheon.uf.common.datadelivery.registry.Coverage;
import com.raytheon.uf.common.datadelivery.registry.DataSet;
import com.raytheon.uf.common.datadelivery.registry.DataType;
import com.raytheon.uf.common.datadelivery.registry.GriddedDataSet;
import com.raytheon.uf.common.datadelivery.registry.Network;
import com.raytheon.uf.common.datadelivery.registry.PointDataSet;
import com.raytheon.uf.common.datadelivery.registry.SiteSubscription;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.datadelivery.registry.Time;
import com.raytheon.uf.common.datadelivery.request.DataDeliveryPermission;
import com.raytheon.uf.common.datadelivery.retrieval.util.DataSizeUtils;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;
import com.raytheon.uf.common.registry.handler.RegistryHandlerException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizAppTaskExecutor;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.datadelivery.common.xml.AreaXML;
import com.raytheon.uf.viz.datadelivery.filter.MetaDataManager;
import com.raytheon.uf.viz.datadelivery.services.DataDeliveryServices;
import com.raytheon.uf.viz.datadelivery.subscription.CreateSubscriptionDlg;
import com.raytheon.uf.viz.datadelivery.subscription.ISubscriptionService;
import com.raytheon.uf.viz.datadelivery.subscription.ISubscriptionService.ISubscriptionServiceResult;
import com.raytheon.uf.viz.datadelivery.subscription.SubscriptionService.ForceApplyPromptResponse;
import com.raytheon.uf.viz.datadelivery.subscription.SubscriptionService.IForceApplyPromptDisplayText;
import com.raytheon.uf.viz.datadelivery.subscription.presenter.CreateSubscriptionDlgPresenter;
import com.raytheon.uf.viz.datadelivery.subscription.subset.presenter.DataTimingSubsetPresenter;
import com.raytheon.uf.viz.datadelivery.subscription.subset.xml.PointTimeXML;
import com.raytheon.uf.viz.datadelivery.subscription.subset.xml.SpecificDateTimeXML;
import com.raytheon.uf.viz.datadelivery.subscription.subset.xml.SubsetXML;
import com.raytheon.uf.viz.datadelivery.subscription.subset.xml.TimeXML;
import com.raytheon.uf.viz.datadelivery.subscription.subset.xml.VerticalXML;
import com.raytheon.uf.viz.datadelivery.utils.DataDeliveryGUIUtils;
import com.raytheon.uf.viz.datadelivery.utils.DataDeliveryUtils;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.presenter.IDisplay;

/**
 * Data Delivery Subset Manager Dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 17, 2012            mpduff       Initial creation.
 * Jun 04, 2012   645      jpiatt       Added tooltips & code clean up.
 * Jun 08, 2012   700      djohnson     Always use subset name for subscribed coverage.
 * Jun 21, 2012   736      djohnson     Change OPERATION_STATUS to OperationStatus.
 * Aug 02, 2012   955      djohnson     Type-safe registry query/responses.
 * Aug 08, 2012 863        jpiatt       Added clean & dirty checks.
 * Aug 10, 2012 1002       mpduff       Implementing dataset size estimation.
 * Aug 10, 2012 1022       djohnson     {@link SubsetXML} requires provider name,  use {@link GriddedDataSet}.
 * Aug 22, 2012 0743       djohnson     Subclass for data type specific operations.
 * Aug 29, 2012 0223       mpduff       Set cycle times in new sub object if not in create mode.
 * Sep 06, 2012 1121       mpduff       Use the DataLevelType.getKey() method.
 * Sep 07, 2012 1102       djohnson     Move setting subscription group name from old subscription into null check.
 * Oct 03, 2012 1241       djohnson     Use {@link DataDeliveryPermission} and registry handlers.
 * Oct  4, 2012 1245       jpiatt       Modify to reference util class & code clean up.
 * Oct 11, 2012 1221       mpduff       Set subscription's fulldataset flag.
 * Oct 31, 2012 1278       mpduff       Integrate SpatialUtils class.
 * Nov 09, 2012 1286       djohnson     Consolidate duplicate subscription handling.
 * Nov 26, 2012 1342       mpduff       Fix the closing of the dialog.
 * Nov 26, 2012 1286       djohnson     Always set registry ID on a subscription.
 * Dec 11, 2012 1405       mpduff       Move close confirmation dialog after event.doit = false.
 * Dec 10, 2012 1259       bsteffen     Switch Data Delivery from LatLon to referenced envelopes.
 * Dec 11, 2012 1264       mpduff       Changes to AreaComp.
 * Dec 12, 2012 1391       bgonzale     Mark this dialog as busy when the create subscription
 *                                      dialog is opened. Changed handleOK() to use create subscription
 *                                      dialog status in return result.
 * Dec 17, 2012 1434       mpduff       Don't allow underscores in name.
 * Dec 18, 2012 1439       mpduff       Redo subscription name validation.
 * Jan 02, 2012 1345       djohnson     Use gui thread task executor.
 * Jan 04, 2012 1420       mpduff       Pass the subscription in to the GriddedTimingSelectionDlg.
 * Jan 10, 2013 1444       mpduff       Fix the loading of saved subsets from the saved subset tab.
 * Jan 28, 2013 1530       djohnson     Break out long method chaining into local variables for debugging.
 * Jan 30, 2013 1543       djohnson     Use List instead of ArrayList.
 * Mar 21, 2013 1794       djohnson     Add option to create a shared subscription, if phase3 code is available.
 * Mar 29, 2013 1841       djohnson     Subscription is now UserSubscription.
 * Apr 08, 2013 1826       djohnson     Remove delivery options.
 * May 15, 2013 1040       mpduff       Implement shared subscriptions.
 * May 21, 2013 2020       mpduff       Rename UserSubscription to SiteSubscription.
 * May 28, 2013 1650       djohnson     More information when failing to schedule subscriptions.
 * Jun 04, 2013  223       mpduff       Moved data type specific code to sub classes.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
public abstract class SubsetManagerDlg<DATASET extends DataSet, PRESENTER extends DataTimingSubsetPresenter<DATASET, ?, ?, TIMEXML, ?>, TIMEXML extends TimeXML>
        extends CaveSWTDialog implements ITabAction, IDataSize, IDisplay,
        IForceApplyPromptDisplayText {
    /** constant */
    private final static String DATASETS_NOT_SUPPORTED = "Datasets of type [%s] are currently not supported!";

    /** Status Handler */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SubsetManagerDlg.class);

    /** Subset Name text box */
    private Text nameText;

    /** Estimated size informational label */
    protected Label sizeLbl;

    /** TabFolder object */
    private TabFolder tabFolder;

    /** DataSet object */
    protected DATASET dataSet;

    /** Create a subscription dialog */
    private CreateSubscriptionDlgPresenter subDlg;

    /** Saved subset tab */
    private SavedSubsetTab subsetTab;

    /** Vertical subset tab */
    protected VerticalSubsetTab vTab;

    /**
     * The presenter class separates as much of the logic from the SWT code as
     * possible. It also allows for unit testing of the logic since SWT is
     * typically not ran headless.
     **/
    protected PRESENTER timingTabControls;

    /** Spatial subset tab */
    protected SpatialSubsetTab spatialTabControls;

    /** Subset XML file object */
    private SubsetXML<TIMEXML> subsetXml;

    /** Load dataset flag */
    private boolean loadDataSet = false;

    /** Edit flag */
    private boolean create = true;

    /** Subscription object */
    protected Subscription subscription;

    /** Utility for calculating bandwidth */
    protected DataSizeUtils dataSize = null;

    /** Dialog initialized flag */
    protected boolean initialized = false;

    /** Subset manager constant */
    private final String DD_SUBSET_MANAGER = "Data Delivery Subset Manager - ";

    protected final String VERTICAL_TAB = "Vertical Levels/Parameters";

    private final String SPATIAL_TAB = "Spatial";

    private final ISubscriptionService subscriptionService = DataDeliveryServices
            .getSubscriptionService();

    /**
     * Constructor
     * 
     * @param shell
     *            The parent Shell
     * @param dataSet
     *            The DataSetMetaData
     * @param loadDataSet
     *            Populate the dialog if true
     * @param subsetXml
     *            The SubsetXML object to load
     */
    public SubsetManagerDlg(Shell shell, DATASET dataSet, boolean loadDataSet,
            SubsetXML<TIMEXML> subsetXml) {
        super(shell, SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN,
                CAVE.INDEPENDENT_SHELL);
        if (dataSet != null) {
            setText(DD_SUBSET_MANAGER + dataSet.getDataSetName());
        } else {
            setText(DD_SUBSET_MANAGER);
        }

        this.dataSet = dataSet;
        this.subsetXml = subsetXml;
        this.loadDataSet = loadDataSet;

        this.dataSize = new DataSizeUtils(dataSet);
    }

    /**
     * Constructor
     * 
     * @param shell
     *            The parent Shell
     * 
     * @param dataSet
     *            The DataSetMetaData
     */
    public SubsetManagerDlg(Shell shell, DATASET dataSet) {
        this(shell, dataSet, false, null);
    }

    /**
     * Constructor for editing a subscription.
     * 
     * @param shell
     *            The parent Shell
     * @param loadDataSet
     *            Populate the dialog if true
     * @param subscription
     *            The subscription to edit
     */
    @SuppressWarnings("unchecked")
    public SubsetManagerDlg(Shell shell, boolean loadDataSet,
            Subscription subscription) {
        super(shell, SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN,
                CAVE.INDEPENDENT_SHELL);
        this.create = false;
        this.loadDataSet = true;
        // TODO: Is there a cleaner way to do this other than casting?
        this.dataSet = (DATASET) MetaDataManager.getInstance().getDataSet(
                subscription.getDataSetName(), subscription.getProvider());
        this.subscription = subscription;
        setText(DD_SUBSET_MANAGER + "Edit: " + subscription.getName());

        this.dataSize = new DataSizeUtils(dataSet);
    }

    abstract void createTabs(TabFolder tabFolder);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {

        createTabFolder();
        createInfoComp();
        createButtons();

        if (loadDataSet) {
            if (subsetXml != null) {
                loadFromSubsetXML(subsetXml);
            } else if (subscription != null) {
                loadFromSubscription(subscription);
            }
        }

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent event) {
                event.doit = false;
                displayMessage();
            }
        });

        initialized = true;
        updateDataSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.DEFAULT, true, false);
    }

    /** Create the TabFolder */
    private void createTabFolder() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

        tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setLayoutData(gd);
        createCommonTabs(tabFolder);
        tabFolder.pack();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#opened()
     */
    @Override
    protected void opened() {
        // Set the min size of the shell to the current
        // shell size since it has been packed before this call
        // shell.setMinimumSize(shell.getSize());
    }

    /** Create the tabs */
    private void createCommonTabs(TabFolder tabFolder) {
        createTabs(tabFolder);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);

        TabItem spatialTab = new TabItem(tabFolder, SWT.NONE);
        spatialTab.setText(SPATIAL_TAB);
        Composite spatialComp = new Composite(tabFolder, SWT.NONE);
        spatialComp.setLayout(gl);
        spatialComp.setLayoutData(gd);
        spatialTab.setControl(spatialComp);
        spatialTabControls = new SpatialSubsetTab(spatialComp, dataSet, this);

        TabItem savedSetsTab = new TabItem(tabFolder, SWT.NONE);
        savedSetsTab.setText("Saved Subsets");
        Composite savedSetsComp = new Composite(tabFolder, SWT.NONE);
        savedSetsTab.setControl(savedSetsComp);
        subsetTab = new SavedSubsetTab(savedSetsComp, this);
    }

    /**
     * Construct and return the presenter class.
     * 
     * @param shell
     * @param subsetManagerDlg
     * @param dataSet2
     * @param timingComp
     * 
     * @return the presenter instance
     */
    protected abstract PRESENTER getDataTimingSubsetPresenter(
            Composite parentComp, DATASET dataSet, IDataSize callback,
            Shell shell);

    protected abstract <T extends Subscription> T populateSubscription(T sub,
            boolean create);

    /** Create the information composite */
    private void createInfoComp() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);

        Composite subsetComp = new Composite(shell, SWT.NONE);
        subsetComp.setLayout(gl);
        subsetComp.setLayoutData(gd);

        Label sizeLabelLbl = new Label(subsetComp, SWT.NONE);
        sizeLabelLbl.setText("Est. File Size: ");

        sizeLbl = new Label(subsetComp, SWT.BORDER);
        sizeLbl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label nameLbl = new Label(subsetComp, SWT.NONE);
        nameLbl.setText("Subset Name: ");

        nameText = new Text(subsetComp, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        nameText.setToolTipText("Enter a subset name");
        nameText.setEnabled(create);
    }

    /** Create the buttons */
    private void createButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        final int numColumns = 4;
        GridLayout gl = new GridLayout(numColumns, false);

        Composite bottomComp = new Composite(shell, SWT.NONE);
        bottomComp.setLayout(gl);
        bottomComp.setLayoutData(gd);

        int buttonWidth = 87;
        GridData btnData = new GridData(buttonWidth, SWT.DEFAULT);

        Button subscribeBtn = new Button(bottomComp, SWT.PUSH);
        if (!create) {
            subscribeBtn.setText("Continue...");
            subscribeBtn.setToolTipText("Click to continue editing");
        } else {
            subscribeBtn.setText("Subscribe...");
            subscribeBtn
                    .setToolTipText("Click to create a subscription to a subset");
        }
        subscribeBtn.setLayoutData(btnData);
        subscribeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (subscription == null) {
                    launchCreateSubscriptionGui(createSubscription(
                            new SiteSubscription(), Network.OPSNET));
                } else {
                    launchCreateSubscriptionGui(subscription);
                }
            }
        });

        btnData = new GridData(buttonWidth, SWT.DEFAULT);
        Button queryBtn = new Button(bottomComp, SWT.PUSH);
        queryBtn.setText("Query");
        queryBtn.setLayoutData(btnData);
        queryBtn.setToolTipText("Click to query subset data");
        queryBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleQuery();
            }
        });

        btnData = new GridData(buttonWidth, SWT.DEFAULT);
        Button cancelBtn = new Button(bottomComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(btnData);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                displayMessage();
            }
        });
    }

    /**
     * Launch the Create Subscription GUI.
     * 
     * @param sub
     *            The subscription object
     */
    public void launchCreateSubscriptionGui(Subscription sub) {
        DataDeliveryGUIUtils.markBusyInUIThread(shell);
        try {
            if (handleOK(sub)) {
                close();
            }
        } finally {
            DataDeliveryGUIUtils.markNotBusyInUIThread(shell);
        }
    }

    /**
     * Launch the Create Subscription GUI
     */
    private boolean handleOK(Subscription sub) {
        if (this.validated(true)) {
            if (subDlg != null && !subDlg.isDisposed()) {
                subDlg.bringToTop();
            } else {
                subDlg = new CreateSubscriptionDlgPresenter(
                        new CreateSubscriptionDlg(shell, create), dataSet,
                        create, new VizAppTaskExecutor());
                subDlg.setSubscriptionData(sub);
                subDlg.open();
            }
            return subDlg.getStatus() == Status.OK;
        }

        return false;
    }

    /**
     * Query button action handler.
     */
    private void handleQuery() {
        boolean valid = this.validated(false);

        if (valid) {

            AdhocSubscription as = createSubscription(new AdhocSubscription(),
                    Network.OPSNET);
            // null means the user hit cancel on the date/cycle selection dialog
            if (as == null) {
                return;
            }
            try {
                ISubscriptionServiceResult result = subscriptionService.store(
                        as, this);

                if (result.hasMessageToDisplay()) {
                    DataDeliveryUtils.showMessage(getShell(), SWT.OK,
                            "Query Scheduled", result.getMessageToDisplay());
                }
            } catch (RegistryHandlerException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error requesting adhoc data.", e);
            }
        }
    }

    /**
     * Create the user subscription.
     * 
     * @param <T>
     *            The subscription object reference type
     * @param sub
     *            The subscription to populate
     * @param the
     *            route for the subscription
     * 
     * @return the populated subscription
     */
    protected <T extends SiteSubscription> T createSubscription(T sub,
            Network defaultRoute) {

        Preconditions.checkNotNull(sub, "A subscription must be provided.");
        Preconditions.checkNotNull(defaultRoute,
                "A defaultRoute must be provided.");

        sub.setOwner((create) ? LocalizationManager.getInstance()
                .getCurrentUser() : this.subscription.getOwner());

        return setupCommonSubscriptionAttributes(sub, defaultRoute);
    }

    /**
     * Sets up common subscription attributes.
     * 
     * @param <T>
     *            The subscription object reference type
     * @param sub
     *            The subscription to populate
     * @param the
     *            route for the subscription
     * 
     * @return the populated subscription
     */
    private <T extends Subscription> T setupCommonSubscriptionAttributes(T sub,
            Network defaultRoute) {

        Preconditions.checkNotNull(sub, "A subscription must be provided.");
        Preconditions.checkNotNull(defaultRoute,
                "A defaultRoute must be provided.");

        sub.setRoute(defaultRoute);
        sub.setName(nameText.getText());
        if (subscription == null || subscription.getOfficeIDs() == null) {
            sub.addOfficeID(LocalizationManager.getInstance().getCurrentSite());
        } else {
            sub.setOfficeIDs(subscription.getOfficeIDs());
        }

        if (!create) {
            sub.setGroupName(this.subscription.getGroupName());
            sub.setSubscriptionEnd(this.subscription.getSubscriptionEnd());
            sub.setSubscriptionStart(this.subscription.getSubscriptionStart());
            sub.setActivePeriodEnd(this.subscription.getActivePeriodEnd());
            sub.setActivePeriodStart(this.subscription.getActivePeriodStart());
            sub.setActive(this.subscription.isActive());
            sub.setPriority(this.subscription.getPriority());
        }
        sub.setProvider(dataSet.getProviderName());
        sub.setDataSetName(dataSet.getDataSetName());
        sub.setDataSetSize(dataSize.getDataSetSize());
        sub.setDataSetType(dataSet.getDataSetType());

        // Catch the case where the user closes this dialog.
        if (this.isDisposed()) {
            return null;
        }

        sub.setDataSetName(dataSet.getDataSetName());
        sub.setSubscriptionId("AdHocID");
        if (this.subscription != null) {
            if (this.subscription.getDescription() != null) {
                sub.setDescription(subscription.getDescription());
            }
            sub.setGroupName(subscription.getGroupName());
        }

        sub.setId(RegistryUtil.getRegistryObjectKey(sub));

        return populateSubscription(sub, create);
    }

    /**
     * Setup the timing information specific to the data type.
     */
    protected abstract Time setupDataSpecificTime(Time newTime, Subscription sub);

    /**
     * Display cancel changes message.
     */
    private void displayMessage() {
        if (!isDirty()) {
            close();
        } else {
            int choice = DataDeliveryUtils
                    .showMessage(shell, SWT.YES | SWT.NO, "Cancel Changes?",
                            "Are you sure you wish to close without saving your subset?");
            if (choice == SWT.YES) {
                close();
            }
        }
    }

    /**
     * Validate user has selected the necessary items.
     * 
     * @param subscription
     *            true for subscription, false for adhoc query
     * @return true if filled out correctly
     */
    private boolean validated(boolean subscription) {
        String name = nameText.getText();

        // Is Subset Name entered
        if ((name == null) || (name.isEmpty())) {
            DataDeliveryUtils.showMessage(getShell(), SWT.OK,
                    DataDeliveryGUIUtils.NAME_REQUIRED_TITLE,
                    DataDeliveryGUIUtils.NAME_REQUIRED_MESSAGE);
            return false;
        }

        if (DataDeliveryGUIUtils.INVALID_CHAR_PATTERN.matcher(name.trim())
                .find()) {
            DataDeliveryUtils.showMessage(getShell(), SWT.ERROR,
                    DataDeliveryGUIUtils.INVALID_CHARS_TITLE,
                    DataDeliveryGUIUtils.INVALID_CHARS_MESSAGE);

            return false;
        }

        if (!validateArea()) {
            return false;
        }

        Collection<String> invalidTabs = getInvalidTabs();

        if (!invalidTabs.isEmpty()) {
            StringBuilder message = new StringBuilder(
                    "The following tabs do not have valid entries:\n\n");
            for (String tab : invalidTabs) {
                message.append(tab + "\n");
            }
            DataDeliveryUtils.showMessage(shell, getStyle(), "Invalid Entries",
                    message.toString());
            return false;
        }

        return true;
    }

    protected Collection<String> getInvalidTabs() {
        Collection<String> invalidTabs = new ArrayList<String>(3);

        if (!spatialTabControls.isValid()) {
            invalidTabs.add(SPATIAL_TAB);
        }

        return invalidTabs;
    }

    /**
     * Validate the area.
     */
    private boolean validateArea() {
        ReferencedEnvelope envelope = spatialTabControls.getEnvelope();

        if (envelope == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("The defined area is invalid.\n\n");
            sb.append("Adjust the selected area and try again.");

            DataDeliveryUtils.showMessage(getShell(), SWT.ICON_ERROR,
                    "Validation Error", sb.toString());

            return false;
        }
        boolean valid = false;
        try {
            Envelope intersection = MapUtil.reprojectAndIntersect(envelope,
                    dataSet.getCoverage().getEnvelope());
            if (intersection != null && intersection.getSpan(0) > 0
                    && intersection.getSpan(1) > 0) {
                valid = true;
            }
        } catch (TransformException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }

        if (!valid) {
            StringBuilder sb = new StringBuilder();
            sb.append("The defined area is invalid for ");
            sb.append(dataSet.getDataSetName());
            sb.append(".\nThe areas do not intersect.\n\n");

            sb.append("Adjust the selected area and try again.");

            int answer = DataDeliveryUtils.showMessage(getShell(), SWT.OK
                    | SWT.CANCEL, "Validation Error", sb.toString());

            return answer == SWT.OK ? true : false;
        }

        return true;
    }

    @Override
    public void handleSaveSubset() {

        if (!DataDeliveryGUIUtils.hasText(this.nameText)) {
            DataDeliveryUtils.showMessage(getShell(), SWT.OK, "Name Required",
                    "Name requred. A subset name must be entered.");
            return;
        }

        if (DataDeliveryGUIUtils.INVALID_CHAR_PATTERN.matcher(
                nameText.getText().trim()).find()) {
            DataDeliveryUtils
                    .showMessage(getShell(), SWT.ERROR, "Invalid Characters",
                            "Invalid characters. The Subset Name may only contain letters/numbers/dashes.");
            return;
        }

        SubsetXML<TIMEXML> subset = new SubsetXML<TIMEXML>();
        populateSubsetXML(subset);

        // Have all the info, now save the file
        SubsetFileManager.getInstance().saveSubset(subset, this.shell);
        setClean();
        subsetTab.enableButtons(nameText);
    }

    protected void populateSubsetXML(SubsetXML<TIMEXML> subset) {
        subset.setBaseSubsetName(nameText.getText());
        subset.setDatasetName(dataSet.getDataSetName());
        subset.setProviderName(dataSet.getProviderName());

        // Save area info
        AreaXML area = spatialTabControls.getSaveInfo();
        subset.setArea(area);

        // TODO Only save this for grid. Once Obs have parameters then this will
        // need to be saved for obs
        if (dataSet.getDataSetType() == DataType.GRID) {
            // next save vertical layer/parameter info
            ArrayList<VerticalXML> vertList = vTab.getSaveInfo();
            subset.setVerticalList(vertList);
        }

        // finally the date/cycle/forecast data
        TIMEXML time = timingTabControls.getSaveInfo();
        subset.setTime(time);
    }

    /**
     * Load saved subset button action handler. This action takes the settings
     * from the saved subset and applies them to the current data set.
     * 
     * @param subsetName
     *            Name of the subset to load
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleLoadSubset(String subsetName) {

        // TODO: How else to do this other than casting?
        SubsetXML<TIMEXML> loadedSubsetXml = (SubsetXML<TIMEXML>) SubsetFileManager
                .getInstance().loadSubset(subsetName);

        loadFromSubsetXML(loadedSubsetXml);
    }

    protected void loadFromSubsetXML(SubsetXML<TIMEXML> subsetXml) {
        if (this.subsetXml == subsetXml) {
            // only populate area and name if subsetXml is loading from initial
            // load, not from the saved subsets tab.
            AreaXML area = subsetXml.getArea();
            spatialTabControls.setDataSet(this.dataSet);
            spatialTabControls.populate(area);

            this.nameText.setText(subsetXml.getBaseSubsetName());
        }
    }

    protected void loadFromSubscription(Subscription subscription) {
        this.nameText.setText(this.subscription.getName());

        // Area
        AreaXML area = new AreaXML();

        ReferencedEnvelope envelope = this.subscription.getCoverage()
                .getEnvelope();
        ReferencedEnvelope requestEnvelope = this.subscription.getCoverage()
                .getRequestEnvelope();

        if (requestEnvelope != null && !requestEnvelope.isEmpty()) {
            area.setEnvelope(requestEnvelope);
        } else {
            area.setEnvelope(envelope);
        }

        spatialTabControls.setDataSet(this.dataSet);
        spatialTabControls.populate(area);

    }

    /**
     * Get the Time object.
     * 
     * @return The time object
     */
    protected abstract TIMEXML getTimeXmlFromSubscription();

    /**
     * If any mods have been made to the composite selections, set dirty true.
     */
    protected boolean isDirty() {

        if (vTab != null && vTab.isDirty()) {
            return true;
        }

        if (timingTabControls.isDirty()) {
            return true;
        }

        if (spatialTabControls.isSpatialDirty()) {
            return true;
        }

        return false;
    }

    /**
     * Reset the dirty flags.
     */
    protected void setClean() {
        if (vTab != null) {
            vTab.setClean();
        }
        timingTabControls.setDirty(false);
        spatialTabControls.setSpatialDirty(false);
    }

    /**
     * Constructs the appropriate subset dialog based upon the {@link DataSet}
     * type.
     * 
     * @param shell
     *            the current dialog shell
     * @param data
     *            the data set
     * @return the dialog
     */
    public static SubsetManagerDlg<?, ?, ?> fromDataSet(Shell shell,
            DataSet data) {
        if (data.getDataSetType() == DataType.GRID) {
            return new GriddedSubsetManagerDlg(shell, (GriddedDataSet) data);
        } else if (data.getDataSetType() == DataType.POINT) {
            return new PointSubsetManagerDlg(shell, (PointDataSet) data);
        }
        throw new IllegalArgumentException(String.format(
                DATASETS_NOT_SUPPORTED, data.getClass().getName()));
    }

    /**
     * Constructs the appropriate subset dialog based upon the
     * {@link Subscription#getDataSetType()}.
     * 
     * @param shell
     *            the current dialog shell
     * @param loadDataSet
     *            true if load the data set
     * @param subscription
     *            the subscription object
     * @return SubsetManagerDlg
     */
    public static SubsetManagerDlg<?, ?, ?> fromSubscription(Shell shell,
            boolean loadDataSet, Subscription subscription) {
        if (DataType.GRID == subscription.getDataSetType()) {
            return new GriddedSubsetManagerDlg(shell, loadDataSet, subscription);
        } else if (DataType.POINT == subscription.getDataSetType()) {
            return new PointSubsetManagerDlg(shell, loadDataSet, subscription);
        }

        throw new IllegalArgumentException(String.format(
                DATASETS_NOT_SUPPORTED, subscription.getDataSetType()));
    }

    /**
     * Constructs the appropriate subset dialog based upon the {@link DataSet}
     * type.
     * 
     * @param shell
     *            the current dialog shell
     * @param data
     *            the DataSet obj
     * @param loadDataSet
     *            true if load the data set
     * @param subset
     *            Subset XML
     * @return SubsetManagerDlg
     */
    @SuppressWarnings("unchecked")
    public static SubsetManagerDlg<?, ?, ?> fromSubsetXML(Shell shell,
            DataSet data, boolean loadDataSet, SubsetXML<?> subset) {
        if (data instanceof GriddedDataSet) {
            return new GriddedSubsetManagerDlg(shell, (GriddedDataSet) data,
                    loadDataSet, (SubsetXML<SpecificDateTimeXML>) subset);
        } else if (data instanceof PointDataSet) {
            return new PointSubsetManagerDlg(shell, (PointDataSet) data, true,
                    (SubsetXML<PointTimeXML>) subset);
        }
        throw new IllegalArgumentException(String.format(
                DATASETS_NOT_SUPPORTED, data.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean displayYesNoPopup(String title, String message) {
        return DataDeliveryUtils.showYesNoMessage(getShell(), title, message) == SWT.YES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOptionDisplayText(ForceApplyPromptResponse option,
            int requiredLatency, Subscription subscription,
            Set<String> wouldBeUnscheduledSubscriptions) {
        final boolean singleSubscription = wouldBeUnscheduledSubscriptions
                .size() == 1;
        switch (option) {
        case CANCEL:
            return "Do not store the adhoc query";
        case FORCE_APPLY:
            if (singleSubscription
                    && wouldBeUnscheduledSubscriptions.contains(subscription
                            .getName())) {
                // Can't force apply a query that won't ever be processed
                return null;
            }
            return "Store the adhoc query and unschedule the subscriptions";
        case INCREASE_LATENCY:
            return "Increase the latency on the adhoc query to "
                    + requiredLatency + " minutes";
        case EDIT_SUBSCRIPTIONS:
            return "Edit the "
                    + ((singleSubscription) ? "subscription" : "subscriptions");
        default:
            throw new IllegalArgumentException(
                    "Don't know how to handle option [" + option + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getNameText() {
        return nameText.getText();
    }

    /**
     * Set the coverage into the subscription.
     * 
     * @param sub
     *            The subscription needing the coverage
     */
    protected void setCoverage(Subscription sub, Coverage cov) {
        if (spatialTabControls.useDataSetSize()) {
            cov.setRequestEnvelope(cov.getEnvelope());
            sub.setFullDataSet(true);
        } else {
            cov.setRequestEnvelope(spatialTabControls.getEnvelope());
            sub.setFullDataSet(false);
        }
        sub.setCoverage(cov);
    }
}
