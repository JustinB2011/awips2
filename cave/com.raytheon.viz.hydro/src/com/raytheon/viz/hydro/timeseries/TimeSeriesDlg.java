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
package com.raytheon.viz.hydro.timeseries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.ohd.AppsDefaults;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.hydro.CaveHydroSWTDialog;
import com.raytheon.viz.hydro.resource.MultiPointResource;
import com.raytheon.viz.hydro.timeseries.table.SiteInfo;
import com.raytheon.viz.hydro.timeseries.table.TabInfo;
import com.raytheon.viz.hydro.timeseries.util.GraphData;
import com.raytheon.viz.hydro.timeseries.util.GroupInfo;
import com.raytheon.viz.hydro.timeseries.util.LIDData;
import com.raytheon.viz.hydro.timeseries.util.PageInfo;
import com.raytheon.viz.hydro.timeseries.util.TimeSeriesUtil;
import com.raytheon.viz.hydro.timeseries.util.TraceData;
import com.raytheon.viz.hydrocommon.HydroConstants;
import com.raytheon.viz.hydrocommon.HydroDataCache;
import com.raytheon.viz.hydrocommon.HydroDisplayManager;
import com.raytheon.viz.hydrocommon.data.GageData;
import com.raytheon.viz.hydrocommon.util.StnClassSyncUtil;

/**
 * This class displays the Time Series dialog for Hydroview.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 NOV 2007  373        lvenable    Initial creation. 
 * 09 JUN 2008  1194       M. Duff     Implement the Dialog.
 * 21 Oct 2008  1520       M. Duff     Implement the Tabular Portion.
 * 06 NOV 2009  2639       M. Duff     Bug fixes.
 * 15 JUN 2010  5828       M. Duff     Added additional error checking for
 *                                     loading of group_definition.cfg.
 * 02 Jul 2010  5280       lbousaidi   added Page number to the display
 * 09 Sep 2010  5399       lbousaidi   Added a new constructor and code to 
 * 									   highlight PE TS in bottomListData 
 * 									   related to selected Lid
 * 14 sep 2010  5282	   lbousaidi   set default selections and re-uses 
 * 									   the open Time Series Control window
 * 24 JAN 2011  7797       bkowal      updates so that Hydro Time Series can
 *                                     be ran as a standalone application
 *                                     without starting CAVE.
 * 01 June 2011 9499       djingtao    update openGraph() 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 * 
 */
public class TimeSeriesDlg extends CaveHydroSWTDialog {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(TimeSeriesDlg.class);

    /* Constants */
    private static final String PREDEFINED_GROUP = "Predefined Group";

    private static final String STATION_SELECTION = "Station Selection";

    private static final String GRAPH = "Graph";

    private static final String PAGE = "Page";

    private static final String GROUP = "Group";

    private static final String TRACE = "Trace";

    private static final String NAME = "name";

    private static final String DESCRIPT = "descript";

    private static final String GRID = "grid";

    private static final String TRACEMODE = "tracemode";

    private static final String PASTHOURS = "pasthours";

    private static final String FUTUREHOURS = "futurehours";

    private static final String POS = "pos";

    private static final String XSIZE = "xsize";

    private static final String YSIZE = "ysize";

    private static final String YLINEAR = "ylinear";

    private static final String YSCALE = "yscale";

    private static final String SHOWCAT = "showcat";

    private static final String DERIVEPP = "derivepp";

    private static final String SHOWPP = "showpp";

    private static final String LATESTFCSTONLY = "latestfcstonly";

    private static final String STN = "stn";

    private static final String COLOR = "color";

    private static final String[] TS_LIST = { "RG", "RP", "RZ", "FF", "FX",
            "FZ" };

    /**
     * The TimeSeries Display Dialog.
     */
    private TimeSeriesDisplayDlg timeSeriesDisplayDlg = null;

    /**
     * Beginning year button.
     */
    private Button beginYearBtn;

    /**
     * Beginning month button.
     */
    private Button beginMonthBtn;

    /**
     * Beginning day button.
     */
    private Button beginDayBtn;

    /**
     * Beginning hour button.
     */
    private Button beginHourBtn;

    /**
     * Up arrow used to increase the beginning date & time
     */
    private Button upBeginTimeBtn;

    /**
     * Down arrow used to decrease the beginning date & time
     */
    private Button dnBeginTimeBtn;

    /**
     * Ending year button.
     */
    private Button endYearBtn;

    /**
     * Ending month button.
     */
    private Button endMonthBtn;

    /**
     * Ending day button.
     */
    private Button endDayBtn;

    /**
     * Ending hour button.
     */
    private Button endHourBtn;

    /**
     * Up arrow used to increase the ending date & time
     */
    private Button upEndTimeBtn;

    /**
     * Down arrow used to decrease the ending date & time
     */
    private Button dnEndTimeBtn;

    /**
     * Mode combo box.
     */
    private Combo modeCbo;

    /**
     * River check box.
     */
    private Button riverChk;

    /**
     * Precipitation check box.
     */
    private Button precipChk;

    /**
     * Temperature check box.
     */
    private Button tempChk;

    /**
     * Snow check box.
     */
    private Button snowChk;

    /**
     * Other check box.
     */
    private Button otherChk;

    /**
     * Check all check boxes button.
     */
    private Button checkAllBtn;

    /**
     * Search text control.
     */
    private Text searchTF;

    /**
     * ID radio button.
     */
    private Button idRdo;

    /**
     * Name radio button.
     */
    private Button nameRdo;

    /**
     * Type Source order combo box.
     */
    private Combo tsOrderCbo;

    /**
     * Font used for the list controls.
     */
    private Font font;

    /**
     * Top data list box.
     */
    private List topDataList;

    /**
     * Bottom data list box.
     */
    private List bottomDataList;

    /**
     * List of Group Data.
     */
    private List groupDataList;

    /**
     * Beginning time calendar.
     */
    private Calendar beginCal;

    private Date beginDate = null;

    /**
     * Ending time calendar.
     */
    private Calendar endCal;

    private Date endDate = null;

    /**
     * The selected data label.
     */
    private Label selectedDataLbl;

    /**
     * The graph button.
     */
    private Button graphButton = null;

    /**
     * The table button.
     */
    private Button tableButton = null;

    /**
     * The both button.
     */
    private Button bothButton = null;

    /**
     * Beginning time enumeration.
     * 
     * @author lvenable
     * 
     */
    private enum beginTimeKey {
        BEGIN_YEAR, BEGIN_MONTH, BEGIN_DAY, BEGIN_HOUR
    };

    /**
     * Ending time enumeration.
     * 
     * @author lvenable
     * 
     */
    private enum endTimeKey {
        END_YEAR, END_MONTH, END_DAY, END_HOUR
    };

    /**
     * Selected beginning time key.
     */
    private beginTimeKey selectedBeginTime;

    /**
     * Selected ending time key.
     */
    private endTimeKey selectedEndTime;

    /**
     * The Stack Composite.
     */
    private Composite stackComp;

    /**
     * The Stack Layout.
     */
    private final StackLayout stackLayout = new StackLayout();

    /**
     * Set of Location IDs in the upperDataList
     */
    private ArrayList<String> lidList = new ArrayList<String>();

    /**
     * Filtered lid list
     */
    private ArrayList<String> filteredLidList = new ArrayList<String>();

    /**
     * Tree Map holding the Location Id and Name in sorted order.
     */
    private Map<String, String> stationData;// = new TreeMap<String, String>();

    /**
     * Map holding the location id and display class.
     */
    private Map<String, String> stnDisplayMap = null;

    /**
     * The Station Group for the stack layout.
     */
    private Group stnGroup;

    /**
     * The Group Group for the stack layout.
     */
    private Group groupGroup;

    /**
     * Keeps track of the station layout being displayed.initializecomponents
     */
    private boolean stnLayoutDisplayed = true;

    /**
     * Previous LID data.
     */
    private LIDData prevLidData = new LIDData();

    /**
     * Current LID data.
     */
    private LIDData currLidData = new LIDData();

    /** List of GroupInfo objects */
    private ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();

    /** Holds the Group Information */
    private GroupInfo groupInfo;

    /** Holds the page information */
    private PageInfo pageInfo = null;

    /** Holds the graph information */
    private GraphData graphData = null;

    private String groupConfigFilePath = null;

    private ArrayList<SiteInfo> siteInfoList = new ArrayList<SiteInfo>();

    private ArrayList<TabInfo> tabInfoList = new ArrayList<TabInfo>();

    private HydroDisplayManager displayManager;

    private boolean openTimeSeriesDisplays = false;

    private GageData gageData = null;

    private Cursor waitCursor = null;

    private Cursor arrowCursor = null;

    /**
     * Auto open the graph or tabular display. True opens graph, false opens
     * tabular display.
     */
    private boolean displayGraph = false;

    private Rectangle bounds = null;

    private TabularTimeSeriesDlg tabularDlg;

    /**
     * Used to indicate if the Hydro TimeSeries application is being ran in
     * standalone mode - outside of CAVE.
     */
    private boolean standaloneMode = false;
    
    /**
     * Mode ComboBox previous selection.
     */
    private int prevModeIdx;
    
    private String startMode;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     */
    public TimeSeriesDlg(Shell parent) {
        super(parent, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK | CAVE.INDEPENDENT_SHELL);
        setText("Time Series Control");

        displayManager = HydroDisplayManager.getInstance();
        if (displayManager.getCurrentLid() != null) {
            currentLid = displayManager.getCurrentLid();
        }

        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     * @param lid
     *            Preselected Lid to display upon opening the dialog
     */
    public TimeSeriesDlg(Shell parent, String lid) {
        super(parent, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK | CAVE.INDEPENDENT_SHELL);
        setText("Time Series");

        currentLid = lid;
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     * @param lid
     *            Preselected Lid to display upon opening the dialog
     */
    public TimeSeriesDlg(Shell parent, String lid, String PE, String TS,
            boolean displayGraph) {
        super(parent, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK | CAVE.INDEPENDENT_SHELL);
        setText("Time Series");
        currentLid = lid;
        currentTs = TS;
        currentPe = PE;
        this.displayGraph = displayGraph;
        openTimeSeriesDisplays = true;
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     * @param lid
     *            Preselected Lid to display upon opening the dialog
     */
    public TimeSeriesDlg(Shell parent, String lid, boolean displayGraph) {
        super(parent, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK | CAVE.INDEPENDENT_SHELL);
        setText("Time Series");

        currentLid = lid;
        this.displayGraph = displayGraph;
        openTimeSeriesDisplays = true;
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     * @param lid
     *            Preselected Lid to display upon opening the dialog
     */
    public TimeSeriesDlg(Shell parent, GageData gageData, boolean displayGraph) {
        super(parent, SWT.DIALOG_TRIM, CAVE.DO_NOT_BLOCK | CAVE.INDEPENDENT_SHELL);
        setText("Time Series");

        currentLid = gageData.getLid();
        this.displayGraph = displayGraph;
        openTimeSeriesDisplays = true;
        this.gageData = gageData;
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            the Parent shell.
     * 
     * @param groupConfigFile
     *            the user-specified file with group configuration information.
     */
    public TimeSeriesDlg(Shell parent, File groupConfigFile) {
        super(parent, SWT.DIALOG_TRIM, CAVE.INDEPENDENT_SHELL);
        setText("Time Series");

        this.standaloneMode = true;
        // Ensure That The Group Configuration File Exists.
        if (!groupConfigFile.exists()) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to locate group configuration file - "
                            + groupConfigFile.getAbsolutePath(), null);
            this.groupConfigFilePath = null;
        } else {
            this.groupConfigFilePath = groupConfigFile.getAbsolutePath();
        }
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW);
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 1;
        mainLayout.marginWidth = 1;
        return mainLayout;
    }

    public void setFocus() {
        shell.setFocus();
    }

    @Override
    protected void disposed() {
        if (font.isDisposed() == false) {
            font.dispose();
        }
        if (waitCursor.isDisposed() == false) {
            waitCursor.dispose();
        }
        if (arrowCursor.isDisposed() == false) {
            arrowCursor.dispose();
        }
    }

    public void disposeDialogTS() {
        if (!shell.isDisposed())
            shell.dispose();
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setReturnValue(false);

        font = new Font(shell.getDisplay(), "Monospace", 11, SWT.NORMAL);

        // Initialize all of the controls and layoutsendCal
        initializeComponents();
        setCurrentData();
    }

    @Override
    protected void opened() {
        if (openTimeSeriesDisplays) {
            if (displayGraph) {
                openGraph();
            }
            openTimeSeriesDisplays = false;
        }

        if (this.standaloneMode) {
            this.updateInterfaceForStandalone();
        }
        
        AbstractVizResource<?,?> rsc = HydroDisplayManager.getInstance().getDisplayedResource();
        if (rsc instanceof MultiPointResource) {
            ((MultiPointResource) rsc).setTs(this);
        }
    }

    /**
     * Initialize the dialog components.
     */
    private void initializeComponents() {
        // Get the begin and end values from Apps_defaults
        AppsDefaults appsDef = AppsDefaults.getInstance();
        int beginDays = Integer.parseInt(appsDef.getToken(
                "timeseries_begintime", "30"));
        int endDays = Integer.parseInt(appsDef.getToken("timeseries_endtime",
                "5"));
        startMode = appsDef.getToken("timeseries_mode", "STATION");
        Date d = SimulatedTime.getSystemTime().getTime();
        beginCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        beginCal.setTime(d);
        beginCal.add(Calendar.DAY_OF_MONTH, beginDays * -1);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);

        beginDate = beginCal.getTime();

        endCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        endCal.setTime(d);
        endCal.add(Calendar.DAY_OF_MONTH, endDays);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);

        endDate = endCal.getTime();

        selectedBeginTime = beginTimeKey.BEGIN_YEAR;
        selectedEndTime = endTimeKey.END_YEAR;

        createTimeControls();

        createStationControls();
        createGroupControl();
        createBottomButtons();

        /* Add a complete list of stations to the station list */
        try {
            populateStationList();
        } catch (VizException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (startMode.equals("GROUP") && (displayGraph == false)) {
            modeCbo.select(0);
            stackLayout.topControl = groupGroup;
            stackComp.layout();
            stnLayoutDisplayed = false;
            populateGroupList();
            prevModeIdx = 0;
        }
    }

    /**
     * Create the station controls for the Stack Layout.
     */
    private void createStationControls() {
        stackComp = new Composite(shell, SWT.NONE);
        stackComp.setLayout(stackLayout);
        stnGroup = new Group(stackComp, SWT.NONE);
        stnGroup.setLayout(new GridLayout());
        stackLayout.topControl = stnGroup;

        createCheckBoxControls();

        /* Add a separator */
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        Label sepLbl = new Label(stnGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
        sepLbl.setLayoutData(gd);

        createSearchControls();
        createTopDataListControl();
        createBottomDataListLabel();
        createBottomDataListControl();
    }

    /**
     * Create the group controls for the Stack Layout.
     */
    private void createGroupControl() {
        groupGroup = new Group(stackComp, SWT.NONE);
        groupGroup.setLayout(new GridLayout());

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 250;
        gd.heightHint = 600;
        groupDataList = new List(groupGroup, SWT.BORDER | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL);
        groupDataList.setLayoutData(gd);
        groupDataList.setFont(font);
    }

    /**
     * Create the beginning and ending time controls.
     */
    private void createTimeControls() {
        // --------------------------------------
        // Create beginning time label
        // --------------------------------------
        Composite beginningTimeComp = new Composite(shell, SWT.NONE);
        RowLayout topLabelCompRl = new RowLayout();
        beginningTimeComp.setLayout(topLabelCompRl);

        Label beginningTimeLbl = new Label(beginningTimeComp, SWT.NONE);
        beginningTimeLbl.setText("Beginning Time Z:");

        // --------------------------------------
        // Create beginning time controls
        // --------------------------------------
        Composite beginTimeCtrlComp = new Composite(shell, SWT.NONE);
        GridLayout beginTimeCtrlGl = new GridLayout(5, false);
        beginTimeCtrlComp.setLayout(beginTimeCtrlGl);

        GridData gd = new GridData(63, SWT.DEFAULT);
        beginYearBtn = new Button(beginTimeCtrlComp, SWT.PUSH);
        beginYearBtn.setText(String.valueOf(beginCal.get(Calendar.YEAR)));
        beginYearBtn.setLayoutData(gd);
        beginYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedBeginTime = beginTimeKey.BEGIN_YEAR;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        beginMonthBtn = new Button(beginTimeCtrlComp, SWT.PUSH);
        beginMonthBtn.setText(String.valueOf(beginCal.get(Calendar.MONTH) + 1));
        beginMonthBtn.setLayoutData(gd);
        beginMonthBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedBeginTime = beginTimeKey.BEGIN_MONTH;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        beginDayBtn = new Button(beginTimeCtrlComp, SWT.PUSH);
        beginDayBtn
                .setText(String.valueOf(beginCal.get(Calendar.DAY_OF_MONTH)));
        beginDayBtn.setLayoutData(gd);
        beginDayBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedBeginTime = beginTimeKey.BEGIN_DAY;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        beginHourBtn = new Button(beginTimeCtrlComp, SWT.PUSH);
        beginHourBtn
                .setText(String.valueOf(beginCal.get(Calendar.HOUR_OF_DAY)));
        beginHourBtn.setLayoutData(gd);
        beginHourBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedBeginTime = beginTimeKey.BEGIN_HOUR;
            }
        });

        // Add the time arrow buttons
        Composite timeArrowsComp = new Composite(beginTimeCtrlComp, SWT.NONE);
        RowLayout timeArrowRl = new RowLayout(SWT.VERTICAL);
        timeArrowsComp.setLayout(timeArrowRl);

        RowData rd = new RowData(20, 20);
        upBeginTimeBtn = new Button(timeArrowsComp, SWT.ARROW);
        upBeginTimeBtn.setLayoutData(rd);
        upBeginTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateBeginTime(1);
            }
        });

        rd = new RowData(20, 20);
        dnBeginTimeBtn = new Button(timeArrowsComp, SWT.ARROW | SWT.DOWN);
        dnBeginTimeBtn.setLayoutData(rd);
        dnBeginTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateBeginTime(-1);
            }
        });

        // --------------------------------------
        // Create ending time label
        // --------------------------------------
        Composite endingTimeComp = new Composite(shell, SWT.NONE);
        RowLayout endingCompRl = new RowLayout();
        endingTimeComp.setLayout(endingCompRl);

        Label endingTimeLbl = new Label(endingTimeComp, SWT.NONE);
        endingTimeLbl.setText("Ending Time Z:");

        // --------------------------------------
        // Create ending time controls
        // --------------------------------------
        Composite endTimeCtrlComp = new Composite(shell, SWT.NONE);
        GridLayout endTimeCtrlGl = new GridLayout(5, false);
        endTimeCtrlComp.setLayout(endTimeCtrlGl);

        gd = new GridData(63, SWT.DEFAULT);
        endYearBtn = new Button(endTimeCtrlComp, SWT.PUSH);
        endYearBtn.setText(String.valueOf(endCal.get(Calendar.YEAR)));
        endYearBtn.setLayoutData(gd);
        endYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedEndTime = endTimeKey.END_YEAR;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        endMonthBtn = new Button(endTimeCtrlComp, SWT.PUSH);
        endMonthBtn.setText(String.valueOf(endCal.get(Calendar.MONTH) + 1));
        endMonthBtn.setLayoutData(gd);
        endMonthBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedEndTime = endTimeKey.END_MONTH;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        endDayBtn = new Button(endTimeCtrlComp, SWT.PUSH);
        endDayBtn.setText(String.valueOf(endCal.get(Calendar.DAY_OF_MONTH)));
        endDayBtn.setLayoutData(gd);
        endDayBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedEndTime = endTimeKey.END_DAY;
            }
        });

        gd = new GridData(43, SWT.DEFAULT);
        endHourBtn = new Button(endTimeCtrlComp, SWT.PUSH);
        endHourBtn.setText(String.valueOf(endCal.get(Calendar.HOUR_OF_DAY)));
        endHourBtn.setLayoutData(gd);
        endHourBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectedEndTime = endTimeKey.END_HOUR;
            }
        });

        // Add the time arrow buttons
        Composite endTimeArrowsComp = new Composite(endTimeCtrlComp, SWT.NONE);
        RowLayout endTimeArrowRl = new RowLayout(SWT.VERTICAL);
        endTimeArrowsComp.setLayout(endTimeArrowRl);

        rd = new RowData(20, 20);
        upEndTimeBtn = new Button(endTimeArrowsComp, SWT.ARROW);
        upEndTimeBtn.setLayoutData(rd);
        upEndTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateEndTime(1);
            }
        });

        rd = new RowData(20, 20);
        dnEndTimeBtn = new Button(endTimeArrowsComp, SWT.ARROW | SWT.DOWN);
        dnEndTimeBtn.setLayoutData(rd);
        dnEndTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateEndTime(-1);
            }
        });

        // --------------------------------------
        // Create Mode controls
        // --------------------------------------
        Composite modeComp = new Composite(shell, SWT.NONE);
        GridLayout modeGl = new GridLayout(2, false);
        modeComp.setLayout(modeGl);

        Label modeLbl = new Label(modeComp, SWT.NONE);
        modeLbl.setText("Mode: ");

        modeCbo = new Combo(modeComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        modeCbo.add(PREDEFINED_GROUP);
        modeCbo.add(STATION_SELECTION);
        modeCbo.select(1);
        prevModeIdx = 1;

        modeCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (prevModeIdx != modeCbo.getSelectionIndex()) {
                    if (stnLayoutDisplayed) {
                        stackLayout.topControl = groupGroup;
                        stackComp.layout();
                        stnLayoutDisplayed = false;
                        populateGroupList();
                    } else {
                        stackLayout.topControl = stnGroup;
                        stackComp.layout();
                        stnLayoutDisplayed = true;
                    }
                    prevModeIdx = modeCbo.getSelectionIndex();
                }
            }
        });
    }

    /**
     * Create the check box controls.
     */
    private void createCheckBoxControls() {
        Composite checkComp = new Composite(stnGroup, SWT.NONE);
        GridLayout checkGl = new GridLayout(3, false);
        checkComp.setLayout(checkGl);

        int cellWidth = 80;

        GridData gd = new GridData(cellWidth, SWT.DEFAULT);
        riverChk = new Button(checkComp, SWT.CHECK);
        riverChk.setText("River");
        riverChk.setLayoutData(gd);
        riverChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filterList();
            }
        });

        gd = new GridData(cellWidth, SWT.DEFAULT);
        precipChk = new Button(checkComp, SWT.CHECK);
        precipChk.setText("Precip");
        precipChk.setLayoutData(gd);
        precipChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filterList();
            }
        });

        gd = new GridData(cellWidth, SWT.DEFAULT);
        tempChk = new Button(checkComp, SWT.CHECK);
        tempChk.setText("Temp");
        tempChk.setLayoutData(gd);
        tempChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filterList();
            }
        });

        gd = new GridData(cellWidth, SWT.DEFAULT);
        snowChk = new Button(checkComp, SWT.CHECK);
        snowChk.setText("Snow");
        snowChk.setLayoutData(gd);
        snowChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filterList();
            }
        });

        gd = new GridData(cellWidth, SWT.DEFAULT);
        otherChk = new Button(checkComp, SWT.CHECK);
        otherChk.setText("Other");
        otherChk.setLayoutData(gd);
        otherChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filterList();
            }
        });

        gd = new GridData(cellWidth, SWT.DEFAULT);
        checkAllBtn = new Button(checkComp, SWT.PUSH);
        checkAllBtn.setText("All");
        checkAllBtn.setLayoutData(gd);
        checkAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                riverChk.setSelection(true);
                precipChk.setSelection(true);
                tempChk.setSelection(true);
                snowChk.setSelection(true);
                otherChk.setSelection(true);
                filterList();
            }
        });
    }

    /**
     * Create the search controls.
     */
    private void createSearchControls() {
        Composite searchComp = new Composite(stnGroup, SWT.NONE);
        GridLayout searchGl = new GridLayout(4, false);
        searchComp.setLayout(searchGl);

        GridData gd = new GridData(90, SWT.DEFAULT);

        Label searchLbl = new Label(searchComp, SWT.NORMAL);
        searchLbl.setText("Search:   ");

        searchTF = new Text(searchComp, SWT.BORDER);
        searchTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                String search = searchTF.getText();
                if (!search.equals("") && !search.equals(" ")) {

                    /* Iterate over the location Ids in the list */
                    ListIterator<String> iter = lidList.listIterator();
                    while (iter.hasNext()) {
                        if (idRdo.getSelection()) {
                            if (iter.next().startsWith(
                                    searchTF.getText().toUpperCase())) {
                                topDataList.setSelection(iter.previousIndex());
                                break;
                            }
                        } else {
                            String lid = iter.next();
                            if (stationData
                                    .get(lid)
                                    .toUpperCase()
                                    .startsWith(
                                            searchTF.getText().toUpperCase())) {
                                topDataList.setSelection(iter.previousIndex());
                                break;
                            }
                        }
                    }
                } else {
                    // topDataList.setSelection(0); // Commented to more closely
                    // match legacy app
                }
                if (topDataList.getSelectionIndex() > 0) {
                    populateBottomList(
                            lidList.get(topDataList.getSelectionIndex()),
                            tsOrderCbo.getSelectionIndex());
                }
            }
        });

        Label spacer = new Label(searchComp, SWT.NORMAL);
        spacer.setText("   ");

        gd = new GridData(SWT.DEFAULT, SWT.BOTTOM, false, true);
        idRdo = new Button(searchComp, SWT.RADIO);
        idRdo.setLayoutData(gd);
        idRdo.setText("ID");
        idRdo.setSelection(true);
        idRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.setCursor(waitCursor);
                try {
                    populateStationList();
                } catch (VizException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                shell.setCursor(arrowCursor);
            }
        });

        Label tsOrderLbl = new Label(searchComp, SWT.NORMAL);
        tsOrderLbl.setText("TS Order: ");

        tsOrderCbo = new Combo(searchComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        tsOrderCbo.add(" ");
        tsOrderCbo.add("RG");
        tsOrderCbo.add("RP");
        tsOrderCbo.add("RZ");
        tsOrderCbo.add("FF");
        tsOrderCbo.add("FX");
        tsOrderCbo.add("FZ");
        tsOrderCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String line = topDataList.getItem(topDataList
                        .getSelectionIndex());
                String selectedLid = line.substring(0, line.indexOf(" "));
                populateBottomList(selectedLid, tsOrderCbo.getSelectionIndex());
            }
        });
        tsOrderCbo.select(1);
        Label spacer2 = new Label(searchComp, SWT.NORMAL);
        spacer2.setText("   ");

        gd = new GridData(SWT.DEFAULT, SWT.TOP, false, true);
        nameRdo = new Button(searchComp, SWT.RADIO);
        nameRdo.setLayoutData(gd);
        nameRdo.setText("Name");
        nameRdo.setSelection(false);
        nameRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.setCursor(waitCursor);
                try {
                    populateStationList();
                } catch (VizException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                shell.setCursor(arrowCursor);
            }
        });
    }

    /**
     * Create the top list control.
     */
    private void createTopDataListControl() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 250;
        gd.heightHint = 200;
        topDataList = new List(stnGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL);
        topDataList.setLayoutData(gd);

        topDataList.setFont(font);

        topDataList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                populateBottomList(
                        lidList.get(topDataList.getSelectionIndex()),
                        tsOrderCbo.getSelectionIndex());
            }
        });
    }

    /**
     * Create the labels for the bottom list control.
     */
    private void createBottomDataListLabel() {
        Composite labelComp = new Composite(stnGroup, SWT.NONE);
        GridLayout labelGl = new GridLayout(5, false);
        labelComp.setLayout(labelGl);

        Label peLbl = new Label(labelComp, SWT.NONE);
        peLbl.setText("PE");

        Label typSrcLbl = new Label(labelComp, SWT.NONE);
        typSrcLbl.setText("TypSrc");

        Label extLbl = new Label(labelComp, SWT.NONE);
        extLbl.setText("Ext");

        Label durLbl = new Label(labelComp, SWT.NONE);
        durLbl.setText("Dur");

        selectedDataLbl = new Label(labelComp, SWT.NONE);
        selectedDataLbl.setText("AAAAAA");
    }

    /**
     * Create the bottom list control.
     */
    private void createBottomDataListControl() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 250;
        gd.heightHint = 200;
        bottomDataList = new List(stnGroup, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL);
        bottomDataList.setLayoutData(gd);
        bottomDataList.setFont(font);
        bottomDataList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                correctSelections();
            }
        });
    }

    /**
     * Create the buttons at the bottom of the dialog.
     */
    private void createBottomButtons() {
        GridData mainGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout buttonGl = new GridLayout(4, false);
        buttonComp.setLayoutData(mainGD);
        buttonComp.setLayout(buttonGl);

        int buttonWidth = 63;

        GridData gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button graphBtn = new Button(buttonComp, SWT.PUSH);
        graphBtn.setText("Graph");
        graphBtn.setLayoutData(gd);
        this.graphButton = graphBtn;
        graphBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                graphButton.setEnabled(false);
                bothButton.setEnabled(false);
                openGraph();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button tableBtn = new Button(buttonComp, SWT.PUSH);
        tableBtn.setText("Table");
        tableBtn.setLayoutData(gd);
        this.tableButton = tableBtn;
        tableBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                tableButton.setEnabled(false);
                bothButton.setEnabled(false);
                openTabularDisplay();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button bothBtn = new Button(buttonComp, SWT.PUSH);
        bothBtn.setText("Both");
        bothBtn.setLayoutData(gd);
        this.bothButton = bothBtn;
        bothBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                bothAction();
            }
        });

        gd = new GridData(SWT.END, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.dispose();
            }
        });
    }

    /**
     * Update the beginning time buttons with the changed time.
     * 
     * @param adjustValue
     *            Amount to adjust the date/time.
     */
    private void updateBeginTime(int adjustValue) {
        if (selectedBeginTime == beginTimeKey.BEGIN_YEAR) {
            beginCal.roll(Calendar.YEAR, adjustValue);
        } else if (selectedBeginTime == beginTimeKey.BEGIN_MONTH) {
            beginCal.roll(Calendar.MONTH, adjustValue);
        } else if (selectedBeginTime == beginTimeKey.BEGIN_DAY) {
            beginCal.roll(Calendar.DAY_OF_MONTH, adjustValue);
        } else if (selectedBeginTime == beginTimeKey.BEGIN_HOUR) {
            beginCal.roll(Calendar.HOUR_OF_DAY, adjustValue);
        }

        beginDate = beginCal.getTime();
        beginYearBtn.setText(String.valueOf(beginCal.get(Calendar.YEAR)));
        beginMonthBtn.setText(String.valueOf(beginCal.get(Calendar.MONTH) + 1));
        beginDayBtn
                .setText(String.valueOf(beginCal.get(Calendar.DAY_OF_MONTH)));
        beginHourBtn
                .setText(String.valueOf(beginCal.get(Calendar.HOUR_OF_DAY)));
    }

    /**
     * Update the ending time buttons with the changed time.
     * 
     * @param adjustValue
     *            Amount to adjust the date/time.
     */
    private void updateEndTime(int adjustValue) {
        if (selectedEndTime == endTimeKey.END_YEAR) {
            endCal.roll(Calendar.YEAR, adjustValue);
        } else if (selectedEndTime == endTimeKey.END_MONTH) {
            endCal.roll(Calendar.MONTH, adjustValue);
        } else if (selectedEndTime == endTimeKey.END_DAY) {
            endCal.roll(Calendar.DAY_OF_MONTH, adjustValue);
        } else if (selectedEndTime == endTimeKey.END_HOUR) {
            endCal.roll(Calendar.HOUR_OF_DAY, adjustValue);
        }

        endDate = endCal.getTime();
        endYearBtn.setText(String.valueOf(endCal.get(Calendar.YEAR)));
        endMonthBtn.setText(String.valueOf(endCal.get(Calendar.MONTH) + 1));
        endDayBtn.setText(String.valueOf(endCal.get(Calendar.DAY_OF_MONTH)));
        endHourBtn.setText(String.valueOf(endCal.get(Calendar.HOUR_OF_DAY)));
    }

    /**
     * Populates the station list box.
     */
    private void populateStationList() throws VizException {
        TimeSeriesDataManager dataManager = TimeSeriesDataManager.getInstance();
        lidList = new ArrayList<String>();

        /* Check the locview and stnclass table counts */
        if (!dataManager.checkLidCount()) {
            long locCount = dataManager.recordCount("locview", "");
            long stnClassCount = dataManager.recordCount("stnClass", "");
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Error: Number of records in LocView " + locCount
                    + " ");
            errMessage.append("does not equal number of records in StnClass "
                    + stnClassCount + ". ");
            errMessage
                    .append(""
                            + "Click 'OK' to run the set station class program to correct this problem.\n\n");
            errMessage.append("This may take several minutes to execute.");
            boolean response = MessageDialog.openConfirm(shell,
                    "Tables Out of Sync", errMessage.toString());

            if (response) {
                StnClassSyncUtil.setStnClassAll();
            } else {
                throw new VizException(errMessage.toString());
            }
        }

        stationData = dataManager.getTimeSeriesStationData(nameRdo
                .getSelection());
        stnDisplayMap = dataManager.getStationDisplayMap();

        Set<String> lids = stationData.keySet();

        topDataList.removeAll();

        Iterator<String> iter = lids.iterator();
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        String lid = null;
        while (iter.hasNext()) {
            lid = iter.next();
            formatter.format("%-10s %-25s", lid, stationData.get(lid));
            topDataList.add(sb.toString());
            lidList.add(lid);
            sb.setLength(0);
        }

        if (currentLid != null) {
            ListIterator<String> iter2 = lidList.listIterator();
            while (iter2.hasNext()) {
                if (idRdo.getSelection()) {
                    if (iter2.next().equalsIgnoreCase(currentLid)) {
                        topDataList.setSelection(iter2.previousIndex());
                        topDataList.showSelection();

                        /* set to null so we don't enter this block again */
                        populateBottomList(currentLid,
                                tsOrderCbo.getSelectionIndex());
                        currentLid = null;
                        break;
                    }
                }

            }
        }
    }

    /**
     * Populate the details list
     */
    private void populateBottomList(String selectedLid, int tsSelection) {
        TimeSeriesDataManager dataManager = TimeSeriesDataManager.getInstance();
        siteInfoList.clear();
        try {
            /* Returns data ordered by pe asc,dur asc,ts asc,extremum asc. */
            ArrayList<Object[]> data = (ArrayList<Object[]>) dataManager
                    .getSitePEData(selectedLid);

            bottomDataList.removeAll();

            /* Get the lists of PE data */
            LinkedHashMap<String, ArrayList<SiteInfo>> hMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();
            LinkedHashMap<String, ArrayList<SiteInfo>> qMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();
            LinkedHashMap<String, ArrayList<SiteInfo>> pMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();
            LinkedHashMap<String, ArrayList<SiteInfo>> sMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();
            LinkedHashMap<String, ArrayList<SiteInfo>> tMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();
            LinkedHashMap<String, ArrayList<SiteInfo>> otherMap = new LinkedHashMap<String, ArrayList<SiteInfo>>();

            String prevPE = "";
            for (int i = 0; i < data.size(); i++) {

                SiteInfo si = new SiteInfo();
                Object[] row = data.get(i);
                si.setLid(selectedLid);
                si.setPe((String) row[1]);
                si.setTs((String) row[2]);
                si.setExt((String) row[3]);
                si.setDur((Integer) row[4]);
                // siteInfoList.add(si);

                if (si.getPe().startsWith("H")) {
                    if (!si.getPe().equals(prevPE)) {
                        hMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    hMap.get(si.getPe()).add(si);
                } else if (si.getPe().startsWith("Q")) {
                    if (!si.getPe().equals(prevPE)) {
                        qMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    qMap.get(si.getPe()).add(si);
                } else if (si.getPe().startsWith("P")) {
                    if (!si.getPe().equals(prevPE)) {
                        pMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    pMap.get(si.getPe()).add(si);
                } else if (si.getPe().startsWith("S")) {
                    if (!si.getPe().equals(prevPE)) {
                        sMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    sMap.get(si.getPe()).add(si);
                } else if (si.getPe().startsWith("T")) {
                    if (!si.getPe().equals(prevPE)) {
                        tMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    tMap.get(si.getPe()).add(si);
                } else {
                    if (!si.getPe().equals(prevPE)) {
                        otherMap.put(si.getPe(), new ArrayList<SiteInfo>());
                        prevPE = si.getPe();
                    }

                    otherMap.get(si.getPe()).add(si);
                }
            }

            /*
             * <pre> Need to reorder the data by PE in this order: H, Q, P, S,
             * T, then everything else
             * 
             * TS sort in this order: RG, RP, RZ, FF, FX, FZ. If a TS is
             * selected to appear first then the rest will sort in order after
             * that one. </pre>
             */
            boolean tsSelected = true;
            if (tsSelection < 1) {
                tsSelected = false;
            }

            processDataList(hMap, tsSelected);
            processDataList(qMap, tsSelected);
            processDataList(pMap, tsSelected);
            processDataList(sMap, tsSelected);
            processDataList(tMap, tsSelected);
            processDataList(otherMap, tsSelected);

            selectedDataLbl.setText(selectedLid);
            bottomDataList.setSelection(0);
        } catch (VizException e) {
            e.printStackTrace();
        }
    }

    /**
     * A generic function that will call the correct group list population
     * function based on whether the dialog is being ran in standalone mode or
     * not.
     */
    private void populateGroupList() {
        if (!this.standaloneMode) {
            this.populateGroupListForStandard();
        } else {
            this.populateGroupListForStandalone();
        }
    }

    /**
     * Populate the Group List box using the group configuration file that has
     * been identified in the hydro constants file.
     */
    private void populateGroupListForStandard() {
        groupDataList.removeAll();

        IPathManager pm = PathManagerFactory.getPathManager();
        File file = pm.getStaticFile(HydroConstants.GROUP_DEFINITION);

        if (file != null) {
            groupConfigFilePath = file.getAbsolutePath();           
        }
        this.readGroupList();
    }

    /**
     * Populate the Group List box using the group configuration file that the
     * user has manually specified by setting the "whfs_config_dir" to the
     * directory that contains the file.
     */
    private void populateGroupListForStandalone() {
        groupDataList.removeAll();

        if (this.groupConfigFilePath != null) {
        
            this.readGroupList();
        }
    }

    /**
     * Read the Group List file.
     */
    private void readGroupList() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(
                    groupConfigFilePath));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().equals("")) {
                    continue;
                }
                processGroupData(str);
            }
            in.close();
        } catch (IOException e) {
            // TODO add log statement about group_definition.cfg not found
            e.printStackTrace();
        }
    }

    /**
     * Filter the upper list based on the boxes the user checks.
     */
    private void filterList() {
        // riverChk, precipChk, tempChk, snowChk, otherChk,
        try {
            populateStationList();
        } catch (VizException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        filteredLidList = new ArrayList<String>();
        topDataList.removeAll();
        boolean validClass = false;
        StringBuilder searchStr = new StringBuilder();
        if (riverChk.getSelection()) {
            searchStr.append("FDR");
        }
        if (precipChk.getSelection()) {
            searchStr.append("P");
        }
        if (tempChk.getSelection()) {
            searchStr.append("T");
        }
        if (snowChk.getSelection()) {
            searchStr.append("S");
        }
        if (otherChk.getSelection()) {
            searchStr.append("O");
        }
        if (searchStr.length() == 0) {
            try {
                populateStationList();
            } catch (VizException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        } else {
            String lid = null;
            for (int i = 0; i < lidList.size(); i++) {
                for (int j = 0; j < searchStr.length(); j++) {
                    char searchChar = searchStr.charAt(j);
                    lid = lidList.get(i);
                    if (stnDisplayMap.containsKey(lid)) {
                        if (stnDisplayMap.get(lid).indexOf(searchChar) >= 0) {
                            validClass = true;
                            break;
                        }
                    }
                }
                if (validClass) {
                    filteredLidList.add(lid);
                    validClass = false;
                }
            }
        }
        lidList = filteredLidList;

        /* Populate the list */
        String lid = null;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        topDataList.removeAll();
        for (int i = 0; i < lidList.size(); i++) {
            lid = lidList.get(i);
            formatter.format("%-10s %-25s", lid, stationData.get(lid));
            topDataList.add(sb.toString());
            sb.setLength(0);
        }
    }

    /**
     * Change the selections.
     */
    private void correctSelections() {
        int count = bottomDataList.getSelectionCount();
        String[] dataString = bottomDataList.getSelection();

        if (count == 1) {
            prevLidData.setData(dataString[0]);
            currLidData.setData(dataString[0]);
        } else if (count > 1) {
            /*
             * Check for valid combination of PEDTSEPs when there are two
             * different data types
             */
            boolean flag = false;
            boolean check1 = false;
            boolean check2 = false;
            int n = 0;

            /* Search for more than 2 different PEs */
            while ((n < count) && !flag) {
                LIDData tmpData = new LIDData();
                tmpData.setData(dataString[n]);
                check1 = lidCheck(prevLidData, tmpData);
                if ((currLidData != null) && (currLidData.getDur() != null)
                        && (currLidData.getPe() != null)) {
                    check2 = lidCheck(currLidData, tmpData);
                }

                if (check1 && check2) {
                    /*
                     * Save the current PE to currLidData when prevLidData is
                     * the same as currLidData
                     */
                    if (prevLidData.getPe().equalsIgnoreCase(
                            currLidData.getPe())
                            && prevLidData.getDur().equalsIgnoreCase(
                                    currLidData.getDur())) {
                        currLidData.setPe(tmpData.getPe());
                        currLidData.setDur(tmpData.getDur());
                    } else {
                        flag = true;
                    }
                }
                n++;
            }

            if (flag) {
                /*
                 * Both of the prevLidData and currLidData has filled with
                 * different data, need deselect the PE items equal to
                 * prevLidData,
                 */
                int[] indices = bottomDataList.getSelectionIndices();
                for (int i = 0; i < count; i++) {
                    LIDData tmpLidData = new LIDData();
                    tmpLidData.setData(dataString[i]);

                    check1 = lidCheck(prevLidData, tmpLidData);

                    if (!check1) {
                        // deselect this row
                        bottomDataList.deselect(indices[i]);
                    }
                }
            }

            String[] dataString2 = bottomDataList.getSelection();
            for (int i = 0; i < dataString2.length; i++) {
                LIDData tmpLidData = new LIDData();
                tmpLidData.setData(dataString2[i]);

                check1 = lidCheck(prevLidData, tmpLidData);
                check2 = lidCheck(currLidData, tmpLidData);

                if (check1 && check2) {
                    /*
                     * Both of the prevLidData and currLidData has filled with
                     * different data, need: 1) move data from currLidData to
                     * prevLidData, and 2) save the new data to currLidData.
                     */
                    prevLidData = currLidData;
                    currLidData = tmpLidData;
                }
            }
        }

        // Reset the siteInfoList selections
        int selectedIndex = bottomDataList.getSelectionIndex();

        for (int i = 0; i < siteInfoList.size(); i++) {
            SiteInfo si = siteInfoList.get(i);
            if (i == selectedIndex) {
                si.setSelected(true);
            } else {
                si.setSelected(false);
            }
        }

    }

    /**
     * Check if two LIDs' data belong to the same group.
     * 
     * The rules are: 1) if the PEs are different; or 2) if the PEs of both LID
     * are "PP", but the durations are different
     * 
     * @param lidData1
     *            first lid data to compare
     * @param lidData2
     *            second lid data to compare
     * @return true if the lid data matches
     */
    private boolean lidCheck(LIDData lidData1, LIDData lidData2) {
        boolean result = false;
        if (((lidData1 != null) && (lidData1.getDur() != null) && (lidData1
                .getPe() != null))
                && ((lidData2 != null) && (lidData2.getDur() != null) && (lidData2
                        .getPe() != null))) {

            if (!lidData1.getPe().equalsIgnoreCase(lidData2.getPe())) {
                result = true;
            } else if (lidData1.getPe().equalsIgnoreCase("PP")
                    && lidData2.getPe().equalsIgnoreCase("PP")) {
                if (!lidData1.getDur().equalsIgnoreCase(lidData2.getDur())) {
                    result = true;
                }
            }
        }

        return result;
    }
    
    /**
     * Action called when the both button is pressed.
     */
    private void bothAction() {
        tableButton.setEnabled(false);
        graphButton.setEnabled(false);
        bothButton.setEnabled(false);
        openTabularDisplay();
        openGraph();
        tableButton.setEnabled(true);
        graphButton.setEnabled(true);
        bothButton.setEnabled(true);
        tabularDlg.getShell().moveAbove(this.shell);
        timeSeriesDisplayDlg.getShell().moveAbove(this.shell);
    }

    /**
     * Process the lines in the group config file.
     * 
     * @param line
     */
    private void processGroupData(String line) {
    	Boolean showpp_flag = false;

    	// Remove any leading whitespace
        line = line.replaceAll("^\\s+", "");

        if (line.startsWith(GROUP)) {
            groupInfo = new GroupInfo();
            groupInfo.setGroupSelected(true);
            groupList.add(groupInfo);
        }
        String[] parts = line.split(":");
      
        if (parts[0].equals(GROUP)) {    
            String[] pairs = parts[1].split(",");
            for (String s : pairs) {
                String[] values = s.split("=", 2);

                // make sure we have values to go with the key
                if (values.length > 1) {
                    if ((values[0] != null) && values[0].equalsIgnoreCase(NAME)) {
                        if (values[1] != null)
                            groupInfo.setGroupName(values[1]);
                        groupDataList.add(groupInfo.getGroupName());
                    } else if (values[0].equalsIgnoreCase(DESCRIPT)) {
                        groupInfo.setDescription(values[1]);
                    } else if (values[0].equalsIgnoreCase(GRID)) {
                        if (values[1].equalsIgnoreCase("T")) {
                            groupInfo.setGridLines(true);
                        } else {
                            groupInfo.setGridLines(false);
                        }
                    } else if (values[0].equalsIgnoreCase(TRACEMODE)) {
                        groupInfo.setTraceMode(values[1]);                     
                    } else if (values[0].equalsIgnoreCase(PASTHOURS)) {
                        groupInfo.setPastHours(Integer.parseInt(values[1]));
                    } else if (values[0].equalsIgnoreCase(FUTUREHOURS)) {
                        groupInfo.setFutureHours(Integer.parseInt(values[1]));
                    } else {
                        // TODO log a message about invalid key/value pair
                        System.err.println("Invalid key/value pair: " + s);
                    }
                }
            }
        } else if (parts[0].equals(PAGE)) {
            String[] values = parts[1].split("=", 2);
            if (values.length > 1) {
                pageInfo = new PageInfo();
                pageInfo.setTitle(values[1]);
                groupInfo.addPageInfo(pageInfo);
            }
        } else if (parts[0].equals(GRAPH)) {
            graphData = new GraphData();
            
            String[] pairs = parts[1].split(",");
            for (String s : pairs) {
                String[] values = s.split("=", 2);
                if (values.length > 1) {
                    if (values[0].equalsIgnoreCase(POS)) {
                        graphData.setGraph_pos(Integer.parseInt(values[1]));
                    } else if (values[0].equalsIgnoreCase(XSIZE)) {
                        graphData.setXsize(Integer.parseInt(values[1]));
                    } else if (values[0].equalsIgnoreCase(YSIZE)) {
                        graphData.setYsize(Integer.parseInt(values[1]));
                    } else if (values[0].equalsIgnoreCase(YLINEAR)) {
                        graphData.setYlinear(values[1]);
                    } else if (values[0].equalsIgnoreCase(YSCALE)) {
                        graphData.setYscale(values[1]);
                    } else if (values[0].equalsIgnoreCase(SHOWCAT)) {
                        if (values[1].equalsIgnoreCase("T")) {
                            graphData.setShowcat(true);
                        } else {
                            graphData.setShowcat(false);
                        }
                    } else if (values[0].equalsIgnoreCase(DERIVEPP)) {
                        graphData.setDerivepp(values[1]);
                    } else if (values[0].equalsIgnoreCase(SHOWPP)) {
                        if (values[1].equalsIgnoreCase("T")) {                        
                        	showpp_flag = true;
                        } else {                        
                        	showpp_flag = false;
                        }
                    } else if (values[0].equalsIgnoreCase(LATESTFCSTONLY)) {
                        if (values[1].equalsIgnoreCase("T")) {
                            graphData.setLatestfcstonly(true);
                        } else {
                            graphData.setLatestfcstonly(false);
                        }
                    } else {
                        // TODO log a message about invalid key/value pair
                        System.err.println("Invalid key/value pair: " + s);
                    }
                }
            }
            // handle the case where there isn't a page element when
            // there should be
            if (pageInfo == null) {
                pageInfo = new PageInfo();
            }
            pageInfo.addGraphData(graphData);
        } else if (parts[0].equals(TRACE)) {
            TraceData td = new TraceData();
            String[] pairs = parts[1].split(",");
            for (String s : pairs) {
                String[] values = s.split("=", 2);
                if (values.length > 1) {
                    if (values[0].equalsIgnoreCase(STN)) {
                        td.setLid(values[1]);
                    } else if (values[0].equalsIgnoreCase(HydroConstants.PC)) {
                        td.setPc(values[1]);
                        if (showpp_flag == true)
                        	graphData.setShowpp(true);
                        else
                        	graphData.setShowpp(false);
                    } else if (values[0].equalsIgnoreCase(COLOR)) {
                        td.setColorName(values[1]);
                    }
                }
            }
            graphData.addTrace(td);                       
            
            graphData.setBeginDate(beginDate);
            graphData.setEndDate(endDate);

        } else {
            // TODO log error here, invalid value
            System.err.println("Error in Group Definition Config file: " + line);
        }              
    }

    /**
     * Handle Table option when selected on the Time Series Control Dialog
     */
    private void openTabularDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (!validateForm()) {
            this.enableTableButton();
            this.enableBothButton();
            return;
        } else {
            if (modeCbo.getItem(modeCbo.getSelectionIndex()) == PREDEFINED_GROUP) {
                siteInfoList.clear();
            }

            tabInfoList.clear();

            if ((tabularDlg != null) && tabularDlg.isOpen()) {
                tabularDlg.disposeTabularTS();
            }

            tabularDlg = new TabularTimeSeriesDlg(shell, beginCal.getTime(),
                    endCal.getTime(), this);

            TabInfo tabInfo = new TabInfo();
            tabInfo.setBeginTime(beginCal);
            tabInfo.setEndTime(endCal);

            /* Set up the GraphData objects */
            if (modeCbo.getText().equals(STATION_SELECTION)) {
                groupInfo = new GroupInfo();
                groupInfo.setCurrentPage(0);

                /* Get the data from the station list */
                String selection = topDataList.getItem(topDataList
                        .getSelectionIndex());
                String[] pieces = selection.split("\\s+", 2);
                tabularDlg.setLid(pieces[0]);
                tabularDlg.setSiteName(pieces[1].trim());
                tabInfo.addBuffer(pieces[0] + " " + pieces[1].trim());
                tabInfo.setLid(pieces[0]);

                /* Get the selections from the data list */
                int[] indices = bottomDataList.getSelectionIndices();

                /*
                 * Loop through the site info list that are the data for the
                 * bottom list
                 */
                for (int i = 0; i < siteInfoList.size(); i++) {
                    for (int ind : indices) {
                        if (ind == i) {
                            siteInfoList.get(i).setSelected(true);
                            break;
                        }
                    }
                }
                tabInfo.setInfoList(siteInfoList);
                tabInfoList.add(tabInfo);

                tabularDlg.setGroupInfo(groupInfo);
            } else {

                /* Set the group info object */
                groupInfo = groupList.get(groupDataList.getSelectionIndex());
                tabularDlg.setGroupInfo(groupInfo);

                for (PageInfo pi : groupInfo.getPageInfoList()) {
                    for (GraphData gd : pi.getGraphDataList()) {
                        for (TraceData td : gd.getTraces()) {
                            SiteInfo si = new SiteInfo();
                            si.setLid(td.getLid());
                            si.setPe(td.getPe());
                            si.setTs(td.getTs());
                            si.setDur(TimeSeriesUtil.convertDur2Short(td
                                    .getDur().charAt(0)));
                            si.setExt(td.getExtremum());

                            if (td.getBasistime() != null) {
                                si.setBasisTime(sdf.format(td.getBasistime()));
                            }
                            siteInfoList.add(si);
                        }
                    }
                }
                tabInfo.setLid(siteInfoList.get(0).getLid());

                tabInfo.setInfoList(siteInfoList);
                tabInfoList.add(tabInfo);
            }
            tabularDlg.setTabInfoList(tabInfoList);
            tabularDlg.open();
        }
    }

    /**
     * Open the graph.
     */
    private void openGraph() {
        if (!validateForm()) {
            this.enableGraphButton();
            this.enableBothButton();
            return;
        } else {

            if (timeSeriesDisplayDlg != null) {
                bounds = timeSeriesDisplayDlg.getDialogBounds();
                timeSeriesDisplayDlg.disposeDialog();
            }

            timeSeriesDisplayDlg = new TimeSeriesDisplayDlg(shell, bounds, this);

            PageInfo pageInfo = new PageInfo();
            LIDData firstLidData = new LIDData();

            pageInfo.setNumberGraphs(0);

            /* Set up the GraphData objects */
            if (modeCbo.getText().equals(STATION_SELECTION)) {
                int numberGraphs = 1;
                groupInfo = new GroupInfo();
                groupInfo.setCurrentPage(0);

                /* Get the data from the station list */
                String[] sa = topDataList.getSelection();
                String[] pieces = sa[0].split("\\s+", 2);
                timeSeriesDisplayDlg.setLid(pieces[0]);
                timeSeriesDisplayDlg.setSiteName(pieces[1].trim());

                /* Get the data from the data list */
                int[] indices = bottomDataList.getSelectionIndices();

                /* Hold the first value for reference */
                String s = bottomDataList.getItem(indices[0]);
                String[] s2 = s.split("\\s+");
                firstLidData.setPe(s2[0]);
                                
                firstLidData.setDur(TimeSeriesUtil.convertDurNameToValue(s));

                ArrayList<TraceData> dataList = new ArrayList<TraceData>();

                for (int i = 0; i < indices.length; i++) {
                    /*
                     * Check the selections and determine if 1 or 2 graphs are
                     * needed
                     */
                    String selection = bottomDataList.getItem(indices[i]);
                    String[] pieces2 = selection.split("\\s+");
                    LIDData lidData = new LIDData();
                   
                    String  durValue = TimeSeriesUtil.convertDurNameToValue(selection);
                    lidData.setDur(durValue);
                    lidData.setPe(pieces2[0]);

                    TraceData td = new TraceData();
                    td.setLid(pieces[0]);
                    td.setName(pieces[1].trim());
                    td.setPe(pieces2[0]);
                    td.setTs(pieces2[1]);
                    td.setExtremum(pieces2[2]);
                   
                    if (pieces2[3].contains("=")) {
                        td.setDur("0");
                    } else {
                    	td.setDur(durValue);
                    }

                    dataList.add(td);

                    if ((indices.length > 1)
                            && (lidCheck(firstLidData, lidData))) {
                        numberGraphs++;
                    }
                }
                /* If only one PE display in a single graph */
                if (numberGraphs == 1) {
                    GraphData graphData = new GraphData();
                    for (int i = 0; i < dataList.size(); i++) {
                        TraceData td = dataList.get(i);
                        td.setForecast(false);
                        if (td.getTs().startsWith("F")
                                || td.getTs().startsWith("C")) {
                            td.setForecast(true);
                        }
                        graphData.addTrace(td);
                    }
                    graphData.setBeginDate(beginDate);
                    graphData.setEndDate(endDate);
                    graphData.setGraph_pos(1);
                    graphData.saveTraceInfo();
                    pageInfo.addGraphData(graphData);
                    groupInfo.addPageInfo(pageInfo);
                } else {
                    /* Need two graphs to display the traces */
                    ArrayList<TraceData> secondTraceDataList = new ArrayList<TraceData>();
                    ;
                    // first graph
                    GraphData graph1 = new GraphData();
                    graph1.setGraph_pos(1);
                    graph1.setXsize(6);
                    graph1.setYsize(1);
                    for (int i = 0; i < dataList.size(); i++) {
                        TraceData td = dataList.get(i);
                        td.setForecast(false);
                        if (td.getTs().startsWith("F")
                                || td.getTs().startsWith("C")) {
                            td.setForecast(true);
                        }

                        LIDData tempLidData = new LIDData();
                        tempLidData.setPe(td.getPe());
                        tempLidData.setDur(td.getDur());

                        if (!lidCheck(firstLidData, tempLidData)) {
                            graph1.addTrace(td);
                        } else {
                            secondTraceDataList.add(td);
                        }
                    }

                    graph1.saveTraceInfo();
                    pageInfo.addGraphData(graph1);
                    if (secondTraceDataList.size() > 0) {

                        /* Second graph setup */
                        GraphData graph2 = new GraphData();
                        graph2.setNum_traces(0);
                        graph2.setGraph_pos(7);
                        graph2.setXsize(6);
                        graph2.setYsize(1);

                        firstLidData.setPe(secondTraceDataList.get(0).getPe());
                        firstLidData
                                .setDur(secondTraceDataList.get(0).getDur());

                        for (int i = 0; i < secondTraceDataList.size(); i++) {
                            TraceData td = secondTraceDataList.get(i);
                            td.setForecast(false);
                            if (td.getTs().startsWith("F")
                                    || td.getTs().startsWith("C")) {
                                td.setForecast(true);
                            }

                            LIDData tempLidData = new LIDData();
                            tempLidData.setPe(td.getPe());
                            tempLidData.setDur(td.getDur());

                            graph2.addTrace(td);
                        }
                        graph2.saveTraceInfo();
                        pageInfo.addGraphData(graph2);
                    }
                }
                groupInfo.addPageInfo(pageInfo);
                timeSeriesDisplayDlg.setGroupInfo(groupInfo);
            } else {
                GroupInfo groupInfo = groupList.get(groupDataList
                        .getSelectionIndex());

                int pastHours = groupInfo.getPastHours();
                int futureHours = groupInfo.getFutureHours();

//                long beginMillis = beginDate.getTime();
//                long endMillis = endDate.getTime();
//                long currentMillis = SimulatedTime.getSystemTime().getTime().getTime();
//                
//                long hoursBack = (currentMillis - beginMillis) / (1000*60*60);
//                long hoursAhead = (endMillis - currentMillis) / (1000*60*60);
//                groupInfo.setPastHours((int) hoursBack);
//                groupInfo.setFutureHours((int) hoursAhead);
                groupInfo.setPastHours(pastHours);
                groupInfo.setFutureHours(futureHours);
                
//                Calendar futureCal = Calendar.getInstance(TimeZone
//                        .getTimeZone("GMT"));
//                Date d = SimulatedTime.getSystemTime().getTime();
//                futureCal.setTime(d);
//                futureCal.add(Calendar.HOUR_OF_DAY, futureHours);
//                Calendar pastCal = Calendar.getInstance(TimeZone
//                        .getTimeZone("GMT"));
//                pastCal.setTime(d);
//                pastCal.add(Calendar.HOUR_OF_DAY, pastHours * -1);
                
               
                timeSeriesDisplayDlg.setGroupInfo(groupInfo);
            }
            timeSeriesDisplayDlg.setBeginDate(beginDate);
            timeSeriesDisplayDlg.setEndDate(endDate);

            if (!timeSeriesDisplayDlg.isOpen()) {
                timeSeriesDisplayDlg.open();
            } else {
                timeSeriesDisplayDlg.createNewGraph();
            }
            
            graphButton.setEnabled(true);
        }
    }

    /**
     * Change the selected mode to group selection mode and display the groups
     * in the user-defined group configuration file.
     */
    private void updateInterfaceForStandalone() {
        this.modeCbo.select(0);
        this.prevModeIdx = 0;
        stackLayout.topControl = groupGroup;
        stackComp.layout();
        stnLayoutDisplayed = false;

        this.populateGroupListForStandalone();
    }

    /**
     * Validate the user's selections.
     * 
     * @return
     */
    private boolean validateForm() {
        boolean valid = true;
        if (modeCbo.getText().equals(STATION_SELECTION)) {
            if (endCal.getTimeInMillis() - beginCal.getTimeInMillis() < 0) {
                MessageDialog.openWarning(shell, "Invalid Date Selection",
                        "Ending Time is prior to Beginning Time");
                valid = false;
            }

            long numberOfDays = (endCal.getTimeInMillis() - beginCal
                    .getTimeInMillis())
                    / HydroConstants.MILLIS_PER_MINUTE
                    / 60
                    / 24;

            if (numberOfDays > HydroConstants.DAYS_MAX) {
                MessageDialog.openWarning(shell, "Invalid Date Selection",
                        "Time Period exceeds " + HydroConstants.DAYS_MAX
                                + " days");
                valid = false;
            }

            if (topDataList.getSelectionIndex() == -1) {
                MessageDialog.openWarning(shell, "Invalid Selection",
                        "A Location ID is required");
                valid = false;
            }

            if (bottomDataList.getSelectionIndex() == -1) {
                MessageDialog.openWarning(shell, "Invalid Selection",
                        "A Physical Element is required");
                valid = false;
            }
        } else {
            if (groupDataList.getSelectionIndex() == -1) {
                MessageDialog.openWarning(shell, "Invalid Selection",
                        "A Group is required");
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Set the PEDTSEP list selection.
     */
    private void setCurrentData() {
        if (gageData == null) {
            gageData = HydroDisplayManager.getInstance().getCurrentData();
        }

        if (gageData != null) {
            String pe = gageData.getPe();
            String ts = gageData.getTs();
            String ext = gageData.getExtremum();
            StringBuilder sb = new StringBuilder();

            int itemCount = bottomDataList.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                sb.append(bottomDataList.getItem(i));
                String[] parts = sb.toString().split("\\s+", 4);
                if ((pe + ts + ext).equalsIgnoreCase(parts[0] + parts[1]
                        + parts[2])) {
                    bottomDataList.setSelection(i);
                    break;
                }
                sb.setLength(0);
            }
        }
        /* used for questionable and Bad Data Gui */

        if ((currentPe != null) && (currentTs != null)) {
            String qPe = currentPe;
            String qTs = currentTs;
            StringBuilder stb = new StringBuilder();

            int itemCount = bottomDataList.getItemCount();
            for (int ii = 0; ii < itemCount; ii++) {
                stb.append(bottomDataList.getItem(ii));
                String[] parts = stb.toString().split("\\s+", 4);
                if ((qPe + qTs).equalsIgnoreCase(parts[0] + parts[1])) {
                    bottomDataList.setSelection(ii);
                    break;
                }
                stb.setLength(0);
            }
        }
    }

    /**
     * Process the data in the list and display it in the bottomDataList.
     * 
     * @param list
     *            The ArrayList of data
     * @param tsSelected
     *            true if a TS was selected
     */
    private void processDataList(
            LinkedHashMap<String, ArrayList<SiteInfo>> dataMap,
            boolean tsSelected) {
        if (dataMap.size() > 0) {
            Set<String> peSet = dataMap.keySet();
            Iterator<String> iter = peSet.iterator();
            ArrayList<SiteInfo> list = null;

            while (iter.hasNext()) {
                String pe = iter.next();
                list = dataMap.get(pe);

                // Get the selected TS first
                if (tsSelected) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i)
                                .getTs()
                                .equals(tsOrderCbo.getItem(tsOrderCbo
                                        .getSelectionIndex()))) {
                            bottomDataList.add(formatDataLine(list.get(i)));
                            siteInfoList.add(list.get(i));
                            // Remove this item since it was already used
                            list.remove(i);
                            // Must decrement i since all items in the list
                            // have been moved back one
                            i--;
                        }
                    }
                }

                // Now get the rest of the listed ts records
                for (int i = 0; i < TS_LIST.length; i++) {
                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j).getTs().equals(TS_LIST[i])) {
                            bottomDataList.add(formatDataLine(list.get(j)));
                            siteInfoList.add(list.get(j));
                            // Remove this item since it was already used
                            list.remove(j);
                            // Must decrement j since all items in the list
                            // have been moved back one
                            j--;
                        }
                    }
                }

                for (int i = 0; i < list.size(); i++) {
                    bottomDataList.add(formatDataLine(list.get(i)));
                    siteInfoList.add(list.get(i));
                    list.remove(i);
                    i--;
                }

            }
        }

    }

    public void updateSelection(GageData gageData, boolean displayGraph) {
        currentLid = gageData.getLid();
        this.displayGraph = displayGraph;
        openTimeSeriesDisplays = true;
        this.gageData = gageData;

        /* Add a complete list of stations to the station list */
        try {
            populateStationList();
        } catch (VizException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setCurrentData();
        opened();
//        topDataList.select(topDataList.getSelectionIndex());
//        topDataList.showSelection();
    }

    /**
     * Build a Bottom Data List entry.
     * 
     * @param si
     *            The SiteInfo object
     * @return The String entry
     */
    private String formatDataLine(SiteInfo si) {
        HydroDataCache hydroCache = HydroDataCache.getInstance();
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%-4s%-4s%-2s%-9s", si.getPe(), si.getTs(),
                si.getExt(), TimeSeriesUtil.convertDur2Text(si.getDur())));

        String peDesc = hydroCache.getPEDescription(si.getPe());
        if (peDesc != null) {
            buffer.append(String.format("%-2s=%-22s", si.getPe(), peDesc));
        } else {
            buffer.append(String.format("%25s", ""));
        }

        String tsDesc = hydroCache.getTSDesc(si.getTs());
        if (tsDesc != null) {
            buffer.append(String.format("%s=%s", si.getTs(), tsDesc));
        }

        return buffer.toString();
    }

    public void enableGraphButton() {
        this.graphButton.setEnabled(true);
    }

    public void enableTableButton() {
        this.tableButton.setEnabled(true);
    }

    public void enableBothButton() {
        this.bothButton.setEnabled(true);
    }
}
