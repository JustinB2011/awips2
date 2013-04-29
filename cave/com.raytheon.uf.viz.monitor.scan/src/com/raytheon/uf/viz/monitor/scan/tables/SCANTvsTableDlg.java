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
package com.raytheon.uf.viz.monitor.scan.tables;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.monitor.scan.config.SCANConfig;
import com.raytheon.uf.common.monitor.scan.config.SCANConfigEnums.ScanColors;
import com.raytheon.uf.common.monitor.scan.config.SCANConfigEnums.ScanTables;
import com.raytheon.uf.common.monitor.scan.config.TvsConfigMgr;
import com.raytheon.uf.viz.monitor.IMonitor;
import com.raytheon.uf.viz.monitor.events.IMonitorConfigurationEvent;
import com.raytheon.uf.viz.monitor.events.IMonitorEvent;
import com.raytheon.uf.viz.monitor.events.IMonitorThresholdEvent;
import com.raytheon.uf.viz.monitor.scan.ScanMonitor;
import com.raytheon.uf.viz.monitor.scan.commondialogs.IAttributeUpdate;
import com.raytheon.uf.viz.monitor.scan.commondialogs.IThresholdUpdate;
import com.raytheon.uf.viz.monitor.scan.commondialogs.SCANAlarmTimeLimitDlg;
import com.raytheon.uf.viz.monitor.scan.commondialogs.SCANAttributesDlg;
import com.raytheon.uf.viz.monitor.scan.commondialogs.SCANColorThreshDlg;
import com.raytheon.uf.viz.monitor.scan.data.ScanDataGenerator;

/**
 * 
 * Dialog for the SCAN TVS table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 29, 2013 #1945      lvenable    Code cleanup for SCAN performance.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SCANTvsTableDlg extends AbstractTableDlg implements
        IAttributeUpdate, IThresholdUpdate {
    private Button configBtn;

    private Button rankBtn;

    private Button attribBtn;

    private Button vertChk;

    private Button tipsChk;

    private Label timeLbl;

    private Menu filePopupMenu;

    private Menu configPopupMenu;

    private Menu rankPopupMenu;

    private SCANTvsTableComp scanTableComp;

    private final SCANTableData tableData;

    private SCANAttributesDlg attributeDlg;

    private SCANAlarmTimeLimitDlg alarmTimeLimitDlg;

    private SCANColorThreshDlg colorThresholdDlg;

    private Date currentTime = null;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param site
     *            Site name.
     * @param tableData
     *            Tabel data.
     */
    public SCANTvsTableDlg(Shell parentShell, String site,
            SCANTableData tableData) {
        super(parentShell);

        this.site = site;
        this.tableData = tableData;
        open();
    }

    @Override
    protected void setTableType() {
        scanTable = ScanTables.TVS;
    }

    @Override
    protected void initComponents() {
        createTopControls();
        createTvsTable();

        createFilePopupMenu();
        createConfigurationsPopupMenu();
        newUpRankPopUpMenu();
        createRankPopupMenu(rankPopupMenu, rankBtn);
    }

    private void createTopControls() {
        SCANConfig scanCfg = SCANConfig.getInstance();
        TvsConfigMgr tvsCfgMgr = (TvsConfigMgr) scanCfg
                .getAbsConfigMgr(scanTable);

        Composite controlComp = new Composite(shell, SWT.PUSH);
        GridLayout gl = new GridLayout(12, false);
        gl.horizontalSpacing = 3;
        controlComp.setLayout(gl);
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(100, SWT.DEFAULT);
        fileBtn = new Button(controlComp, SWT.PUSH);
        fileBtn.setText("File");
        displayFileButtonToolTip();
        fileBtn.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        fileBtn.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        fileBtn.setLayoutData(gd);
        fileBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetButtonForegroundColor(fileBtn);
                Point controlLoc = display.map(fileBtn, null, e.x, e.y
                        + fileBtn.getSize().y);
                filePopupMenu.setLocation(controlLoc);
                filePopupMenu.setVisible(true);
            }
        });
        setupButtonMouseListeners(fileBtn);

        fileBtn.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                displayFileButtonToolTip();
            }
        });

        gd = new GridData();
        configBtn = new Button(controlComp, SWT.PUSH);
        configBtn.setText("Configurations");
        configBtn
                .setBackground(scanCfg.getScanColor(ScanColors.Configurations));
        configBtn.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        configBtn.setLayoutData(gd);
        configBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetButtonForegroundColor(configBtn);
                Point controlLoc = display.map(configBtn, null, e.x, e.y
                        + configBtn.getSize().y);
                configPopupMenu.setLocation(controlLoc);
                configPopupMenu.setVisible(true);
            }
        });
        setupButtonMouseListeners(configBtn);

        gd = new GridData(100, SWT.DEFAULT);
        rankBtn = new Button(controlComp, SWT.PUSH);
        rankBtn.setText("Rank: " + scanCfg.getDefaultRank(ScanTables.TVS));
        rankBtn.setBackground(scanCfg.getScanColor(ScanColors.Rank));
        rankBtn.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        rankBtn.setLayoutData(gd);
        rankBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetButtonForegroundColor(rankBtn);
                Point controlLoc = display.map(rankBtn, null, e.x, e.y
                        + rankBtn.getSize().y);
                rankPopupMenu.setLocation(controlLoc);
                rankPopupMenu.setVisible(true);
            }
        });
        setupButtonMouseListeners(rankBtn);

        gd = new GridData();
        attribBtn = new Button(controlComp, SWT.PUSH);
        attribBtn.setText("Attributes");
        attribBtn.setBackground(scanCfg.getScanColor(ScanColors.Attributes));
        attribBtn.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        attribBtn.setLayoutData(gd);
        attribBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetButtonForegroundColor(attribBtn);
                displayAttributesDialog();
            }
        });
        setupButtonMouseListeners(attribBtn);

        // Vertical tables are not supported at this time.
        gd = new GridData();
        vertChk = new Button(controlComp, SWT.CHECK);
        vertChk.setText("Vert ");
        vertChk.setEnabled(false);
        vertChk.setBackground(scanCfg.getScanColor(ScanColors.Vert));
        vertChk.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        vertChk.setSelection(tvsCfgMgr.getScanTvsCfgXML().getFilterOption());
        vertChk.setLayoutData(gd);

        /*
         * The vertical table is a techblocked DR. This selection listener will
         * be commented out until it is needed.
         */
        // vertChk.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // }
        // });
        setupButtonMouseListeners(vertChk);

        gd = new GridData();
        tipsChk = new Button(controlComp, SWT.CHECK);
        tipsChk.setText("Tips ");
        tipsChk.setBackground(scanCfg.getScanColor(ScanColors.Tips));
        tipsChk.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        tipsChk.setSelection(tvsCfgMgr.getScanTvsCfgXML().getTipsOption());
        tipsChk.setLayoutData(gd);
        tipsChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println(tipsChk.getSelection());
                SCANConfig.getInstance().setShowTips(scanTable,
                        tipsChk.getSelection());
                System.out
                        .println(SCANConfig.getInstance().showTips(scanTable));
                System.out.println(scanTable.name());
                scanTableComp.updateColumnTips();
            }
        });
        setupButtonMouseListeners(tipsChk);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.widthHint = 160;
        timeLbl = new Label(controlComp, SWT.RIGHT);
        timeLbl.setText("*** No Date/Time ***");
        timeLbl.setLayoutData(gd);
    }

    private void createTvsTable() {
        scanTableComp = new SCANTvsTableComp(shell, tableData, this, site);
    }

    /**
     * New up the rank popup menu.
     */
    private void newUpRankPopUpMenu() {
        if (rankPopupMenu != null) {
            rankPopupMenu.dispose();
        }
        rankPopupMenu = new Menu(rankBtn);
    }

    private void createFilePopupMenu() {
        filePopupMenu = new Menu(fileBtn);

        MenuItem retDefaultMI = new MenuItem(filePopupMenu, SWT.NONE);
        retDefaultMI.setText("Retrieve Default TVS Configuration");
        retDefaultMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                retrieveDefaultConfig();
            }
        });

        MenuItem retMesoConfMI = new MenuItem(filePopupMenu, SWT.NONE);
        retMesoConfMI.setText("Retrieve TVS Configuration...");
        retMesoConfMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                retrieveExistingConfig();
            }
        });

        MenuItem saveMesoConfMI = new MenuItem(filePopupMenu, SWT.NONE);
        saveMesoConfMI.setText("Save TVS Configuration");
        saveMesoConfMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveCurrentConfiguration();
            }
        });

        MenuItem saveAsMesoConfMI = new MenuItem(filePopupMenu, SWT.NONE);
        saveAsMesoConfMI.setText("Save TVS Configuration As...");
        saveAsMesoConfMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveConfigurationAs();
            }
        });

        new MenuItem(filePopupMenu, SWT.SEPARATOR);

        MenuItem exitMI = new MenuItem(filePopupMenu, SWT.NONE);
        exitMI.setText("Close TVS Table");
        exitMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shellDisposeDialog();
            }
        });

        fileBtn.setMenu(filePopupMenu);
    }

    private void createConfigurationsPopupMenu() {
        configPopupMenu = new Menu(configBtn);

        /*
         * Alarm Time Setup
         */
        MenuItem alarmTimeSetupMI = new MenuItem(configPopupMenu, SWT.NONE);
        alarmTimeSetupMI.setText("Alarm Time Setup...");
        alarmTimeSetupMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayAlarmTimeLimitDialog();
            }
        });

        /*
         * Box Colors
         */
        MenuItem boxColorsMI = new MenuItem(configPopupMenu, SWT.NONE);
        boxColorsMI.setText("Box Colors...");
        boxColorsMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayColorThresholdDialog();
            }
        });

        configBtn.setMenu(configPopupMenu);
    }

    private void displayAttributesDialog() {
        if ((attributeDlg == null)
                || (attributeDlg.getParent().isDisposed() == true)) {
            attributeDlg = new SCANAttributesDlg(shell, scanTable, this);
            registerDialog(attributeDlg);
            attributeDlg.open();
            unregisterDialog(attributeDlg);
            attributeDlg = null;
        }
    }

    private void displayAlarmTimeLimitDialog() {
        if (alarmTimeLimitDlg == null) {
            alarmTimeLimitDlg = new SCANAlarmTimeLimitDlg(shell, scanTable,
                    this.site);
            registerDialog(alarmTimeLimitDlg);
            alarmTimeLimitDlg.open();
            unregisterDialog(alarmTimeLimitDlg);
            alarmTimeLimitDlg = null;
        }
    }

    private void displayColorThresholdDialog() {
        if (colorThresholdDlg == null) {
            colorThresholdDlg = new SCANColorThreshDlg(shell, scanTable, this);
            registerDialog(colorThresholdDlg);
            colorThresholdDlg.open();
            unregisterDialog(colorThresholdDlg);
            colorThresholdDlg = null;
        }
    }

    @Override
    protected void handleRankMenuEvent(SelectionEvent event) {
        String rank = ((MenuItem) event.getSource()).getText();

        String colName = rank;

        if (rank.compareTo(SCANConfig.getInstance().getDefaultName()) == 0) {
            colName = SCANConfig.getInstance().getDefaultRank(this.scanTable);
        }

        rankBtn.setText("Rank: " + colName);
        int colIndex = SCANConfig.getInstance().getColumnIndex(scanTable,
                colName);
        scanTableComp.sortTableColumnByIndex(colIndex);
    }

    private void updateTimeLabel() {
        if (currentTime == null) {
            timeLbl.setText("*** NO TIME ***");
            return;
        }

        SimpleDateFormat dateFmt = new SimpleDateFormat("E MMM dd HH:mm yyyy");
        dateFmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        timeLbl.setText(dateFmt.format(currentTime));
    }

    private void unregisterDialogFromMonitor() {
        this.fireDialogShutdown(this);
    }

    @Override
    protected void shellDisposeAction() {
        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                unregisterDialogFromMonitor();
            }
        });

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                unregisterDialogFromMonitor();
            }
        });
    }

    @Override
    public void shellDisposeDialog() {
        shell.dispose();
    }

    @Override
    protected void setShellText() {
        if (!shell.isDisposed()) {
            shell.setText(this.site + " TVS Table");
        }
    }

    @Override
    public void sortedColumn(String columnName) {
        rankBtn.setText("Rank: " + columnName);
    }

    @Override
    public void notify(IMonitorEvent me) {
        if (me.getSource() instanceof IMonitor) {
            ScanMonitor scan = (ScanMonitor) me.getSource();

            // If scan is null or the scan table has been disposed then return
            // since nothing will be done.
            if (scan == null || scanTableComp.isDisposed()) {
                return;
            }

            Date time = getScanTime(scan);

            if (time != null) {

                if (getLinkToFrame(scanTable.name())) {
                    currentTime = scan.getDialogTime(scanTable, site);
                    updateTimeLabel();
                    updateTable(scan, time);
                } else {
                    if (currentTime == null || !currentTime.equals(time)) {
                        currentTime = time;
                        updateTimeLabel();
                        updateTable(scan, time);
                    }
                }
            }
        }
    }

    /**
     * Update the table with new data.
     * 
     * @param scan
     *            Scan Monitor.
     * @param time
     *            New time.
     */
    private void updateTable(ScanMonitor scan, Date time) {
        ScanDataGenerator sdg = new ScanDataGenerator(site);
        scanTableComp.setTableData(sdg.generateTVSData(scan.getTableData(
                ScanTables.TVS, site, time)));
    }

    @Override
    public void centerByIdent(String ident) {
        fireRecenter(ident, ScanTables.TVS, site);
    }

    @Override
    public void centerByStormId(String stormId) {
        fireRecenter(stormId, ScanTables.CELL, site);
    }

    @Override
    public Date getCurrentDate() {
        return currentTime;
    }

    @Override
    public String getTrendSetName() {
        // Not used...
        return null;
    }

    @Override
    public void attributeUpdates(boolean[] visibleAttrs) {
        SCANConfig scanCfg = SCANConfig.getInstance();
        scanCfg.setVisibleColumns(scanTable, visibleAttrs);
        scanTableComp.columnVisiblityChanged(visibleAttrs);
        newUpRankPopUpMenu();
        createRankPopupMenu(rankPopupMenu, rankBtn);
    }

    @Override
    public void thresholdsUpdated(String attrName, double upper, double mid,
            double lower) {
        SCANConfig scanCfg = SCANConfig.getInstance();
        scanCfg.setThresholds(scanTable, attrName, upper, mid, lower);

        scanTableComp.updateThresholds(attrName);
        fireThresholdUpdate(new IMonitorThresholdEvent(this));
    }

    @Override
    protected void updateAfterConfigLoad() {
        /*
         * Fire a configuration updated - this will load in new table data as
         * well.
         */
        IMonitorConfigurationEvent imce = new IMonitorConfigurationEvent(this);
        this.fireConfigUpdate(imce);

        tipsChk.setSelection(scanCfg.showTips(scanTable));
        rankBtn.setText("Rank: " + scanCfg.getDefaultRank(scanTable));

        scanTableComp.newConfigLoaded();

        Iterator<IMonitor> iter = getMonitorControlListeners().iterator();
        while (iter.hasNext()) {
            ((ScanMonitor) iter.next()).configurationLoaded(scanTable, site);
        }
    }

    @Override
    public void displayTrendSetGraphs(String ident) {
        // NOT USED
    }

    /*
     * Update the threshold in the composite contained in this dialog for the
     * desired attribute in the dialog.
     */
    @Override
    public void updateThresh(String attr) {
        this.scanTableComp.updateThresholds(attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.monitor.scan.commondialogs.IRequestTrendGraphData
     * #cellValid(java.lang.String)
     */
    @Override
    public boolean cellValid(String ident) {
        // Not currently used for TVS
        return false;
    }

    @Override
    public void turnOffAlarm() {
        mgr.setRing(false);
    }

    @Override
    public void turnOnAlarm() {
        mgr.setRing(true);
    }
}
