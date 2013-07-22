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
package com.raytheon.uf.viz.archive.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.archive.config.DisplayData;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.SizeUtil;
import com.raytheon.uf.viz.archive.data.ArchiveInfo;
import com.raytheon.uf.viz.archive.data.CategoryInfo;
import com.raytheon.uf.viz.archive.data.IArchiveTotals;
import com.raytheon.uf.viz.archive.data.IUpdateListener;
import com.raytheon.uf.viz.archive.data.SizeJobRequest;
import com.raytheon.uf.viz.archive.ui.ArchiveTableComp.TableType;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.ui.dialogs.AwipsCalendar;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Case creation dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013 #1964     lvenable     Initial creation
 * Jun 10, 2013 #1966      rferrel     Implemented back in hooks for display
 *                                      and generation of cases.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CaseCreationDlg extends AbstractArchiveDlg implements
        IArchiveTotals, IUpdateListener {

    /** Start time label. */
    private Label startTimeLbl;

    /** End time label. */
    private Label endTimeLbl;

    /** Start date. */
    private Date startDate;

    /** End date. */
    private Date endDate;

    /** Action to bring up the case name dialog. */
    private Button caseNameCreate;

    /** Display the case name. */
    private Label caseNameLbl;

    /** Compression check box. */
    private Button compressChk;

    /** Break files check box. */
    private Button breakFilesChk;

    /** File size spinner control. */
    private Spinner fileSizeSpnr;

    /** File size combo box. */
    private Combo fileSizeCbo;

    /** Maximum file size label. */
    private Label maxFileSizeLbl;

    /** Directory location label. */
    private Label locationLbl;

    /** Directory location state. */
    private Label locationStateLbl;

    /** Uncompressed file size label. */
    private Label uncompressSizeLbl;

    /** Displays total number of items selected from all categories. */
    private Label totalSelectedItemsLbl;

    /** Button to generate the case. */
    private Button generateBtn;

    /** Date format. */
    private SimpleDateFormat dateFmt = new SimpleDateFormat(
            "E MMM dd yyyy HH:00 z");

    /** Number of selected items. */
    private int selectedItemsSize = 0;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public CaseCreationDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN, CAVE.DO_NOT_BLOCK
                | CAVE.MODE_INDEPENDENT | CAVE.INDEPENDENT_SHELL);
        this.setSelect = false;
        this.tableType = TableType.Case;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 2;
        mainLayout.marginWidth = 2;

        return mainLayout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);
        setText("Archive Case Creation");
        Composite mainComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        mainComp.setLayout(gl);
        init();
    }

    /**
     * Initialize method to create all of the composite & controls.
     */
    private void init() {
        createTimeControls();
        GuiUtil.addSeparator(shell, SWT.HORIZONTAL);
        createCaseCompressionControls();
        GuiUtil.addSeparator(shell, SWT.HORIZONTAL);
        createFileBrowserControls();
        createTable();
        GuiUtil.addSeparator(shell, SWT.HORIZONTAL);
        createBottomActionButtons();
    }

    /**
     * Create the time controls.
     */
    private void createTimeControls() {
        Composite timeComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        timeComp.setLayout(gl);
        timeComp.setLayoutData(gd);

        Button startBtn = new Button(timeComp, SWT.PUSH);
        startBtn.setText(" Start Time... ");
        startBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayDateTimeControls(true);
            }
        });

        gd = new GridData(220, SWT.DEFAULT);
        startTimeLbl = new Label(timeComp, SWT.BORDER);
        startTimeLbl.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = 20;
        Button endBtn = new Button(timeComp, SWT.PUSH);
        endBtn.setText(" End Time... ");
        endBtn.setLayoutData(gd);
        endBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayDateTimeControls(false);
            }
        });

        gd = new GridData(220, SWT.DEFAULT);
        endTimeLbl = new Label(timeComp, SWT.BORDER);
        endTimeLbl.setLayoutData(gd);

        endDate = TimeUtil.newDate();
        long time = endDate.getTime();
        time -= TimeUtil.MILLIS_PER_DAY;
        startDate = new Date(time);
        startTimeLbl.setText(dateFmt.format(startDate));
        endTimeLbl.setText(dateFmt.format(endDate));
    }

    /**
     * Create the case compression controls.
     */
    private void createCaseCompressionControls() {
        Composite caseCompressionComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        caseCompressionComp.setLayout(gl);
        caseCompressionComp.setLayoutData(gd);

        createComboControls(caseCompressionComp);
        GuiUtil.addSeparator(caseCompressionComp, SWT.VERTICAL);
        createCompressionControls(caseCompressionComp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.archive.ui.AbstractArchiveDlg#createComboControls
     * (org.eclipse.swt.widgets.Composite)
     */
    protected Composite createComboControls(Composite comp) {
        Composite comboComp = super.createComboControls(comp);

        caseNameCreate = new Button(comboComp, SWT.PUSH);
        caseNameCreate.setText(" Case Name... ");
        caseNameCreate.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleCaseNameSelection();
            }
        });
        caseNameCreate.setToolTipText("Must first select \"Case Location\".");

        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        caseNameLbl = new Label(comboComp, SWT.BORDER);
        caseNameLbl.setLayoutData(gd);
        return comboComp;
    }

    /**
     * Create the compression controls.
     * 
     * @param comp
     *            Composite to put the control in.
     */
    private void createCompressionControls(Composite comp) {
        Composite compressionComp = new Composite(comp, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        compressionComp.setLayout(gl);
        compressionComp.setLayoutData(gd);

        /*
         * Uncompressed file size label
         */
        Composite compressionLblComp = new Composite(compressionComp, SWT.NONE);
        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        compressionLblComp.setLayout(gl);
        compressionLblComp.setLayoutData(gd);

        Label uncompressLbl = new Label(compressionLblComp, SWT.NONE);
        uncompressLbl.setText("Uncompressed Case Size: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        uncompressSizeLbl = new Label(compressionLblComp, SWT.NONE);
        uncompressSizeLbl.setLayoutData(gd);

        Label totSelectedLbl = new Label(compressionLblComp, SWT.NONE);
        totSelectedLbl.setText("Total Selected Items: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        totalSelectedItemsLbl = new Label(compressionLblComp, SWT.NONE);
        totalSelectedItemsLbl.setLayoutData(gd);

        /*
         * Compression controls
         */
        compressChk = new Button(compressionComp, SWT.CHECK);
        compressChk.setText("Compress Files");
        compressChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleCompressSelection();
            }
        });

        gd = new GridData();
        gd.horizontalIndent = 20;
        breakFilesChk = new Button(compressionComp, SWT.CHECK);
        breakFilesChk.setText("Break into multiple files");
        breakFilesChk.setLayoutData(gd);
        breakFilesChk.setEnabled(false);
        breakFilesChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBreakFilesSelection(breakFilesChk.getSelection());
            }
        });

        Composite maxFileSizeComp = new Composite(compressionComp, SWT.NONE);
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalIndent = 20;
        maxFileSizeComp.setLayout(gl);
        maxFileSizeComp.setLayoutData(gd);

        maxFileSizeLbl = new Label(maxFileSizeComp, SWT.NONE);
        maxFileSizeLbl.setText("Max File Size: ");
        maxFileSizeLbl.setEnabled(false);

        gd = new GridData(60, SWT.DEFAULT);
        fileSizeSpnr = new Spinner(maxFileSizeComp, SWT.BORDER);
        fileSizeSpnr.setIncrement(1);
        fileSizeSpnr.setPageIncrement(50);
        fileSizeSpnr.setMaximum(2000);
        fileSizeSpnr.setMinimum(500);
        fileSizeSpnr.setLayoutData(gd);
        fileSizeSpnr.setEnabled(false);

        fileSizeCbo = new Combo(maxFileSizeComp, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        fileSizeCbo.setEnabled(false);
        fileSizeCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleFileSizeChangeSelection();
            }
        });
        fileSizeCbo.add("MB");
        fileSizeCbo.add("GB");
        fileSizeCbo.select(0);
    }

    /**
     * Create the file browser controls.
     */
    private void createFileBrowserControls() {
        Composite fileBrowserComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(6, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        fileBrowserComp.setLayout(gl);
        fileBrowserComp.setLayoutData(gd);

        Label caseLbl = new Label(fileBrowserComp, SWT.NONE);
        caseLbl.setText("Case Location: ");

        gd = new GridData(300, SWT.DEFAULT);
        locationLbl = new Label(fileBrowserComp, SWT.BORDER);
        locationLbl.setLayoutData(gd);

        Button browseBtn = new Button(fileBrowserComp, SWT.PUSH);
        browseBtn.setText(" Browse... ");
        browseBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBrowserSelection();
            }
        });
        browseBtn.setToolTipText("Select directory to place case.");

        Label stateLbl = new Label(fileBrowserComp, SWT.NONE);
        stateLbl.setText("Full - Available:");

        locationStateLbl = new Label(fileBrowserComp, SWT.BORDER);
        gd = new GridData(200, SWT.DEFAULT);
        locationStateLbl.setLayoutData(gd);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomActionButtons() {

        Composite actionControlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        actionControlComp.setLayout(gl);
        actionControlComp.setLayoutData(gd);

        // TODO - Future implementation.
        // Button exportBtn = new Button(actionControlComp, SWT.PUSH);
        // exportBtn.setText(" Export Case Config... ");
        // exportBtn.addSelectionListener(new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        //
        // }
        // });

        generateBtn = new Button(actionControlComp, SWT.PUSH);
        generateBtn.setText(" Generate ");
        generateBtn.setEnabled(false);
        generateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                generateCase();
            }
        });

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        Button closeBtn = new Button(actionControlComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Display modal dialog that performs the Generation of the case.
     */
    private void generateCase() {
        setCursorBusy(true);
        File targetDir = (File) locationLbl.getData();
        File caseDir = (File) caseNameLbl.getData();
        Calendar startCal = getStart();
        Calendar endCal = getEnd();

        List<DisplayData> displayDatas = getSelectedData();
        boolean doCompress = compressChk.getSelection();
        boolean doMultiFiles = breakFilesChk.getSelection();
        int compressSize = fileSizeSpnr.getSelection();
        String sizeType = fileSizeCbo.getItem(fileSizeCbo.getSelectionIndex());

        // Assume Model dialog.
        GenerateCaseDlg dialog = new GenerateCaseDlg(shell, targetDir, caseDir,
                startCal, endCal, displayDatas, doCompress, doMultiFiles,
                compressSize, sizeType);
        dialog.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                VizApp.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        updateLocationState();
                    }
                });
            }
        });
        dialog.setCloseCallback(new ICloseCallback() {

            @Override
            public void dialogClosed(Object returnValue) {
                setCursorBusy(false);
            }
        });
        dialog.open();

    }

    /**
     * Enable/Disable controls based on the compression check box.
     */
    private void handleCompressSelection() {
        if (compressChk.getSelection()) {
            handleBreakFilesSelection(breakFilesChk.getSelection());
        } else {
            handleBreakFilesSelection(false);
        }

        breakFilesChk.setEnabled(compressChk.getSelection());
    }

    /**
     * Bring up modal dialog to get the case's directory name.
     */
    private void handleCaseNameSelection() {
        Object o = locationLbl.getData();
        if (!(o instanceof File)) {
            MessageDialog.openError(shell, "Error",
                    "Must select \"Case Location\".");
            return;
        }

        File targetDir = (File) o;
        // Assume modal dialog.
        CaseNameDialog dialog = new CaseNameDialog(shell, targetDir);
        dialog.setCloseCallback(new ICloseCallback() {

            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof File) {
                    File caseDir = (File) returnValue;
                    String caseName = caseDir.getAbsolutePath();
                    caseNameLbl.setText(caseName);
                    caseNameLbl.setData(caseDir);
                    checkGenerateButton();
                }
            }
        });
        dialog.open();
    }

    /**
     * Enable/Disable file size controls.
     * 
     * @param enabled
     *            Enabled flag.
     */
    private void handleBreakFilesSelection(boolean enabled) {
        maxFileSizeLbl.setEnabled(enabled);
        fileSizeSpnr.setEnabled(enabled);
        fileSizeCbo.setEnabled(enabled);
    }

    /**
     * Enables the generate button will user has entered all needed elements.
     */
    private void checkGenerateButton() {
        if (generateBtn != null && !generateBtn.isDisposed()) {
            generateBtn.setEnabled(locationLbl.getData() != null
                    && caseNameLbl.getData() != null && selectedItemsSize > 0);
        }
    }

    /**
     * Action performed when the file size has changed.
     */
    private void handleFileSizeChangeSelection() {
        /*
         * If the same item was selected just return.
         */
        if (fileSizeCbo.getItem(fileSizeCbo.getSelectionIndex()).equals(
                (String) fileSizeCbo.getData())) {
            return;
        }

        if (fileSizeCbo.getItem(fileSizeCbo.getSelectionIndex()).equals("MB")) {
            fileSizeSpnr.setIncrement(1);
            fileSizeSpnr.setPageIncrement(50);
            fileSizeSpnr.setMaximum(2000);
            fileSizeSpnr.setMinimum(500);
            fileSizeSpnr.setSelection(500);
        } else {
            fileSizeSpnr.setIncrement(1);
            fileSizeSpnr.setPageIncrement(5);
            fileSizeSpnr.setMinimum(1);
            fileSizeSpnr.setMaximum(10);
            fileSizeSpnr.setSelection(1);
        }

        fileSizeCbo
                .setData(fileSizeCbo.getItem(fileSizeCbo.getSelectionIndex()));
    }

    /**
     * Display the directory browser dialog.
     */
    private void handleBrowserSelection() {
        DirectoryDialog dlg = new DirectoryDialog(shell, SWT.OPEN);
        dlg.setText("Case Location");
        String dirName = dlg.open();
        if (dirName != null) {
            locationLbl.setText(trimDirectoryName(dirName));
            locationLbl.setToolTipText(dirName);
            locationLbl.setData(new File(dirName));
            caseNameCreate.setToolTipText(null);
            caseNameLbl.setText("");
            caseNameLbl.setData(null);
            updateLocationState();
            checkGenerateButton();
        }
    }

    /**
     * Update location's state.
     */
    private void updateLocationState() {
        File dir = (File) locationLbl.getData();
        long totSpace = dir.getTotalSpace();
        long freeSpace = dir.getUsableSpace();
        long percentFull = (long) Math.round(((totSpace - freeSpace) * 100.0)
                / totSpace);
        String state = null;
        Color bgColor = null;
        Color fgColor = null;
        Display display = shell.getDisplay();
        if (percentFull <= 84) {
            state = "GOOD";
            bgColor = display.getSystemColor(SWT.COLOR_GREEN);
            fgColor = display.getSystemColor(SWT.COLOR_BLACK);
        } else if (percentFull <= 94) {
            state = "CAUTION";
            bgColor = display.getSystemColor(SWT.COLOR_YELLOW);
            fgColor = display.getSystemColor(SWT.COLOR_BLACK);
        } else if (percentFull <= 97) {
            state = "DANGER";
            bgColor = display.getSystemColor(SWT.COLOR_RED);
            fgColor = display.getSystemColor(SWT.COLOR_BLACK);
        } else {
            state = "FATAL";
            bgColor = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
            fgColor = display.getSystemColor(SWT.COLOR_WHITE);
        }

        String text = String.format("%1$3d%% %2$s - %3$s", percentFull, state,
                SizeUtil.prettyByteSize(freeSpace));
        locationStateLbl.setText(text);
        locationStateLbl.setBackground(bgColor);
        locationStateLbl.setForeground(fgColor);
    }

    /**
     * Display the date/time controls.
     * 
     * @param startTimeFlag
     *            True for start time, false for end time.
     */
    private void displayDateTimeControls(boolean startTimeFlag) {

        Date acDate = startTimeFlag ? startDate : endDate;
        AwipsCalendar ac = new AwipsCalendar(shell, acDate, 1);
        ac.setTimeZone(TimeUtil.newCalendar().getTimeZone());
        Date date = (Date) ac.open();

        if (date == null) {
            return;
        }

        if (startTimeFlag) {
            if (date.after(endDate)) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION
                        | SWT.OK);
                mb.setText("Date Error");
                mb.setMessage("The selected start date is after the end date.  Resetting.");
                mb.open();
                return;
            }

            if (!startDate.equals(date)) {
                startDate = date;
                resetSizes();
            }
        } else {
            if (date.before(startDate)) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION
                        | SWT.OK);
                mb.setText("Date Error");
                mb.setMessage("The selected end date is before the start date.  Resetting.");
                mb.open();
                return;
            }
            if (!endDate.equals(date)) {
                endDate = date;
                resetSizes();
            }
        }

        if (startTimeFlag) {
            startTimeLbl.setText(dateFmt.format(date));
        } else {
            endTimeLbl.setText(dateFmt.format(date));
        }
    }

    /**
     * Trim the directory name if it is too long to fit in the label. The
     * beginning of the path will be replaced with "...".
     * 
     * @param str
     * @return
     */
    private String trimDirectoryName(String str) {
        Point strExtent;
        Point ellipseExt;

        GC gc = new GC(shell);
        gc.setFont(locationLbl.getFont());

        ellipseExt = gc.stringExtent("...");
        int labelWidth = locationLbl.getBounds().width - (5 + ellipseExt.x);

        strExtent = gc.stringExtent(str);

        if (strExtent.x > labelWidth) {
            while (strExtent.x > labelWidth) {
                str = str.substring(2);
                strExtent = gc.stringExtent(str);
            }
            str = "..." + str;
        }

        gc.dispose();
        return str;
    }

    protected void setTotalSizeText(String text) {
        uncompressSizeLbl.setText(text);
    }

    protected void setTotalSelectedItems(int totalSelected) {
        selectedItemsSize = totalSelected;
        totalSelectedItemsLbl.setText("" + totalSelected);
        checkGenerateButton();
    }

    /**
     * Reset all entries to unknown size, recompute the sizes for the current
     * display table and and other selected entries.
     */
    private void resetSizes() {
        List<DisplayData> selectedDatas = new ArrayList<DisplayData>();
        for (String archiveName : archiveInfoMap.keySet()) {
            ArchiveInfo archiveInfo = archiveInfoMap.get(archiveName);
            for (String categoryName : archiveInfo.getCategoryNames()) {
                CategoryInfo categoryInfo = archiveInfo.get(categoryName);
                for (DisplayData displayData : categoryInfo
                        .getDisplayDataList()) {
                    displayData.setSize(DisplayData.UNKNOWN_SIZE);
                    if (displayData.isSelected()) {
                        selectedDatas.add(displayData);
                    }
                }
            }
        }

        populateTableComp();

        if (selectedDatas.size() > 0) {
            String archiveName = getSelectedArchiveName();
            String categoryName = getSelectedCategoryName();
            Calendar startCal = getStart();
            Calendar endCal = getEnd();

            for (DisplayData displayData : selectedDatas) {
                if (!displayData.isArchive(archiveName)
                        || !displayData.isCategory(categoryName)) {
                    sizeJob.queue(new SizeJobRequest(displayData, startCal,
                            endCal));
                }
            }
        }
    }

    /**
     * Get the data information on all selected items; not just the currently
     * displayed table.
     * 
     * @return selectedDatas
     */
    private List<DisplayData> getSelectedData() {
        List<DisplayData> selectedDatas = new ArrayList<DisplayData>();
        for (String archiveName : archiveInfoMap.keySet()) {
            ArchiveInfo archiveInfo = archiveInfoMap.get(archiveName);
            for (String categoryName : archiveInfo.getCategoryNames()) {
                CategoryInfo categoryInfo = archiveInfo.get(categoryName);
                for (DisplayData displayData : categoryInfo
                        .getDisplayDataList()) {
                    if (displayData.isSelected()) {
                        selectedDatas.add(displayData);
                    }
                }
            }
        }
        return selectedDatas;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.archive.ui.AbstractArchiveDlg#getStart()
     */
    // @Override
    protected Calendar getStart() {
        Calendar startCal = TimeUtil.newCalendar();
        startCal.setTimeInMillis(startDate.getTime());
        return startCal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.archive.ui.AbstractArchiveDlg#getEnd()
     */
    // @Override
    protected Calendar getEnd() {
        Calendar endCal = TimeUtil.newCalendar();
        endCal.setTimeInMillis(endDate.getTime());
        return endCal;
    }
}
