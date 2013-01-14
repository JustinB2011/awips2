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
package com.raytheon.viz.gfe.dialogs;

import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.edex.util.Util;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord.GridType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DFloat;
import com.raytheon.uf.common.dataplugin.gfe.server.lock.Lock;
import com.raytheon.uf.common.dataplugin.gfe.slice.ScalarGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.VectorGridSlice;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.viz.gfe.core.DataManager;
import com.raytheon.viz.gfe.core.UIFormat;
import com.raytheon.viz.gfe.core.UIFormat.FilterType;
import com.raytheon.viz.gfe.core.griddata.IGridData;
import com.raytheon.viz.gfe.core.parm.Parm;
import com.raytheon.viz.gfe.edittool.GridID;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Dialog to display information about the given grid
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 25, 2008            ebabin       Initial creation
 * Jun 20, 2008  #875      bphillip     Implemented Dialog functionality
 * Sep 20, 2012  #1190     dgilling     Use new WsId.getHostName() method.
 * Nov 12, 2012  #1298     rferrel      Code cleanup for non-blocking dialog.
 * Jan 10, 2013  #DR15572  jzeng        add getWindowMax() and getMaxWidth(String str) 
 *                                      and adjustDlg(String str), 
 *                                      change gridInfoText from Label to Text
 *                                      to make sure the info get displayed inside the screen
 *                                      and can be scrolled.
 * 
 * </pre>
 * 
 * @author ebabin
 * @version 1.0
 */

public class GridInfoDialog extends CaveJFACEDialog implements
        SelectionListener {

    /**
     * @param parentShell
     */
    private Parm parm;

    private IGridData gridData;

    private UIFormat uiFormat;

    private Composite top;

    private final String[] gridInfoElements = { "Grid Info", "Grid History",
            "ISC History", "Weather Element Info", "Weather Element State",
            "Locks", "Data Distribution" };

    // set gridInfoText to be Text
    private Text gridInfoText;
    
    // width of the screen
    private int WidthofScreen;
    
    // width of the grouplist
    private final int WidthofGroup = 240;
    
    private SimpleDateFormat gmtFormatter;

    public GridInfoDialog(Shell parent, Parm parm, Date clickTime) {
        super(parent);
        this.setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
        this.parm = parm;
        gridData = parm.overlappingGrid(clickTime);

        this.uiFormat = new UIFormat(DataManager.getCurrentInstance()
                .getParmManager(), FilterType.DISPLAYED, FilterType.DISPLAYED);

        gmtFormatter = new SimpleDateFormat("MMM dd yy HH:mm:ss zzz");
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        //make sure there is enough space for the group list
        WidthofScreen = this.getWindowMax() - WidthofGroup; 
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        top = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) top.getLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        
        initializeComponents();

        return top;
    }

    private void initializeComponents() {
        Group group = new Group(top, SWT.BORDER);
        GridData layoutData = new GridData(SWT.DEFAULT, SWT.FILL, false, true);
        group.setLayoutData(layoutData);
        group.setLayout(new GridLayout(1, true));
        for (int i = 0; i < gridInfoElements.length; i++) {
            Button b = new Button(group, SWT.RADIO);
            b.setText(gridInfoElements[i]);
            b.addSelectionListener(this);
        }
        // Composite composite2 = new Composite(top, SWT.NONE);
        // layoutData = new GridData(SWT.DEFAULT, SWT.FILL, false, true);
        // composite2.setLayoutData(layoutData);
        // composite2.setLayout(new GridLayout(1, true));

        gridInfoText = new Text(top, SWT.NONE | SWT.H_SCROLL);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridInfoText.setEditable(false);
        gridInfoText.setBackground(group.getBackground());
        gridInfoText.setLayoutData(layoutData);
    }

    /*
	 * To get the width of the screen
	 */
	private int getWindowMax(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getScreenSize().width;
	}
	
	/*
	 * adjust the width of the dialog
	 */
	private void adjustDlg(String infoText){
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		int maxLength = getMaxWidth(infoText);
		if ( maxLength > WidthofScreen ) {
			gd.widthHint = WidthofScreen;
			gridInfoText.setLayoutData(gd);
		} else {
        	gridInfoText.setLayoutData(gd);
    	}
		gridInfoText.setText(infoText);
	}
	
	/*
	 * get the maximum width of the info
	 */
	private int getMaxWidth(String textInfo){
		String[] splits = textInfo.split("\\n");
		GC gc = new GC (gridInfoText);
		FontMetrics fm = gc.getFontMetrics ();
		int acw = fm.getAverageCharWidth ();
		int maxStr = 0;
		for (String str : splits){
			maxStr = Math.max(maxStr, str.length());
		}
		return maxStr*acw;		
	}
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButton(parent, Window.CANCEL, "Cancel", false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Grid Information");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
     * .swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
     * .events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        Button b = (Button) e.getSource();
        if (!b.getSelection()) {
            return;
        }

        String choice = b.getText();

        if (choice.equalsIgnoreCase("Grid Info")) {
        	adjustDlg(getGridInfo());
        } else if (choice.equalsIgnoreCase("Grid History")) {
            adjustDlg(getGridHistory());
        } else if (choice.equalsIgnoreCase("ISC History")) {
            adjustDlg(getISCHistory());
        } else if (choice.equalsIgnoreCase("Weather Element Info")) {
        	adjustDlg(getWEInfo());
        } else if (choice.equalsIgnoreCase("Weather Element State")) {
            adjustDlg(getWEState());
        } else if (choice.equalsIgnoreCase("Locks")) {
        	adjustDlg(getLockInfo());
        } else if (choice.equalsIgnoreCase("Data Distribution")) {
            adjustDlg(getDataDistribution());
        } else {
            gridInfoText.setText("");
        }

        this.getShell().layout();
        this.getShell().pack();
    }

    private String getLockInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(null, false, "Current Data Locks"));
        List<Lock> locks = parm.getLockTable().getLocks();

        if (locks.isEmpty()) {
            info.append("Weather element is not locked");
        } else {
            for (Lock lock : locks) {
                info.append("(")
                        .append(gmtFormatter.format(lock.getTimeRange()
                                .getStart())).append(", ");
                info.append(gmtFormatter.format(lock.getTimeRange().getEnd()))
                        .append("):");
                info.append("Locked by ");
                if (lock.getWsId().equals(
                        DataManager.getCurrentInstance().getWsId())) {
                    info.append("me\n");
                } else {
                    info.append(lock.getWsId()).append("\n");
                }
            }
        }

        return info.toString();
    }

    private String getISCHistory() {
        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(null, false, "ISC History"));

        GridID iscGridID = new GridID(parm, DataManager.getCurrentInstance()
                .getSpatialDisplayManager().getSpatialEditorTime());
        if (!parm.isIscParm()) {
            iscGridID = DataManager.getCurrentInstance().getIscDataAccess()
                    .getISCGridID(iscGridID, true);
        }

        IGridData grid = null;
        if (iscGridID != null) {
            grid = iscGridID.grid();
        }
        if (grid == null) {
            info.append("No ISC Grid");
        } else {
            info.append("ISC Grid Valid Time: ").append(grid.getGridTime())
                    .append("\n\n");

            GridDataHistory[] history = grid.getHistory();
            info.append("Site  ---Last Updated---\n");
            for (GridDataHistory h : history) {
                String site = h.getOriginParm().getDbId().getSiteId();
                String upTime = gmtFormatter.format(h.getUpdateTime());
                String ago = this.formatAgo(h.getUpdateTime());
                String pubTime = "";
                if (Util.getUnixTime(h.getPublishTime()) != 0) {
                    pubTime = " PUBLISHED";
                }
                info.append(site).append(" ").append(upTime).append(" ")
                        .append(ago).append(" ").append(pubTime).append("\n");

            }

            info.append("\n");
            info.append("Site  -----History Info-----\n");
            for (GridDataHistory h : history) {
                String site = h.getOriginParm().getDbId().getSiteId();
                String org = h.getOrigin().toString();
                String orgParm = uiFormat.uiParmIDcollapsed(h.getOriginParm());
                String orgTR = gmtFormatter.format(h.getOriginTimeRange()
                        .getStart())
                        + " -> "
                        + gmtFormatter.format(h.getOriginTimeRange().getEnd());
                String timeMod = "Not Modified";
                String ago = "";
                String user = "";
                if (Util.getUnixTime(h.getTimeModified()) != 0) {
                    timeMod = gmtFormatter.format(h.getTimeModified());
                    ago = this.formatAgo(h.getTimeModified());
                    user = this.whoLabel(h.getModified());
                }

                info.append(site).append(" ").append(org).append(" ")
                        .append(orgParm).append(" ").append(orgTR).append(" ")
                        .append(timeMod).append(" ").append(ago).append(" ")
                        .append(user).append("\n");

            }
        }
        return info.toString();
    }

    private String getWEState() {
        StringBuilder info = new StringBuilder();

        info.append(getBoxTitle(null, false, "Weather Element State"));

        if (parm.getGridInfo().getGridType().equals(GridType.SCALAR)
                || parm.getGridInfo().getGridType().equals(GridType.VECTOR)) {

            info.append("Fuzz Value: ")
                    .append(parm.getParmState().getFuzzValue()).append("\n");

            if (parm.isMutable()) {

                if (parm.getGridInfo().getGridType().equals(GridType.SCALAR)
                        || parm.getGridInfo().getGridType()
                                .equals(GridType.VECTOR)) {
                    info.append("Delta value: ")
                            .append(parm.getParmState().getDeltaValue())
                            .append("\n");
                }
                info.append("Assign Value: ")
                        .append(parm.getParmState().getPickUpValue())
                        .append("\n");

                if (parm.getGridInfo().getGridType().equals(GridType.VECTOR)) {
                    info.append("Vector Edit Mode: ")
                            .append(parm.getParmState().getVectorMode())
                            .append("\n");
                }
                if (parm.getGridInfo().getGridType().equals(GridType.DISCRETE)
                        || parm.getGridInfo().getGridType()
                                .equals(GridType.WEATHER)) {
                    info.append("Weather/Discrete Combine Mode: ")
                            .append(parm.getParmState().getCombineMode())
                            .append("\n");
                }
            }

            info.append("Selected: ")
                    .append((parm.getParmState().isSelected() ? "Yes" : "No"))
                    .append("\n");

            if (parm.getParmState().isSelected()) {
                info.append("Selected Time Range: ")
                        .append(this.timeRangeToGMT(parm.getParmState()
                                .getSelectedTimeRange())).append("\n");
            }
            info.append("Graphic Color: ")
                    .append(formatRGB(parm.getDisplayAttributes()
                            .getBaseColor())).append("\n");
        }

        return info.toString();
    }

    private String formatRGB(RGB rgb) {
        return String.format("#%02x%02x%02x", rgb.red, rgb.green, rgb.blue);
    }

    private float getMaxMin(Grid2DFloat grid, boolean getMax) {
        float value = getMax ? -Float.MAX_VALUE : Float.MAX_VALUE;

        for (int y = 0; y < grid.getYdim(); y++) {
            for (int x = 0; x < grid.getXdim(); x++) {
                if (getMax) {
                    value = Math.max(value, grid.get(x, y));
                } else {
                    value = Math.min(value, grid.get(x, y));
                }
            }
        }
        return value;
    }

    private String getDataDistribution() {

        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(gridData, true, "Data Distribution"));

        if (gridData == null) {
            info.append("No Grid");
        } else {
            if (parm.getGridInfo().getGridType().equals(GridType.SCALAR)) {
                Grid2DFloat grid = ((ScalarGridSlice) gridData.getGridSlice())
                        .getScalarGrid();
                info.append("Maximum Data Value: ")
                        .append(getMaxMin(grid, true))
                        .append("\nMinimum Data Value: ")
                        .append(getMaxMin(grid, false)).append("\n");
            } else if (parm.getGridInfo().getGridType().equals(GridType.VECTOR)) {
                Grid2DFloat grid = ((VectorGridSlice) gridData.getGridSlice())
                        .getMagGrid();
                info.append("Maximum Data Value: ")
                        .append(getMaxMin(grid, true))
                        .append("\nMinimum Data Value: ")
                        .append(getMaxMin(grid, false)).append("\n");
            } else if (parm.getGridInfo().getGridType()
                    .equals(GridType.WEATHER)) {

                // TODO
                // for i in xrange(len(dta[1])):
                // count = self._calcCounts(dta, i)
                // info += `count` + " ----> " \
                // + AFPS.WeatherKey_string(dta[1][i]).keyAsPrettyString() \
                // + "\n"
            } else if (parm.getGridInfo().getGridType()
                    .equals(GridType.DISCRETE)) {
                // TODO
                // elif grid.type() == AFPS.Parm.DISCRETE:
                // for i in xrange(len(dta[1])):
                // count = self._calcCounts(dta, i)
                // info += `count` + " ----> " \
                // + dta[1][i] \
                // + "\n" else:

            }
        }

        return info.toString();

    }

    private String getGridInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(gridData, true, "Grid Information"));

        if (gridData == null) {
            info.append("No Grid");
        } else {
            info.append("Grid okay to edit: ")
                    .append((gridData.isOkToEdit() ? "Yes" : "No"))
                    .append("\n");

            info.append("Grid Lock State: ");
            List<Lock> locks = parm.getLockTable().getLocks();
            boolean found = false;
            for (Lock lock : locks) {
                if (lock.getTimeRange().overlaps(gridData.getGridTime())) {
                    found = true;
                    if (lock.getWsId().equals(
                            DataManager.getCurrentInstance().getWsId())) {
                        info.append("Locked by me\n");
                    } else {
                        info.append("Locked by ").append(lock.getWsId())
                                .append("\n");
                    }
                    break;
                }
            }
            if (!found) {
                info.append("Unlocked\n");
            }
        }

        return info.toString();
    }

    private String getWEInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(null, false, "Weather Element Information"));

        info.append("Weather Element Name: ")
                .append(parm.getParmID().getParmName()).append("\n");

        info.append("Weather Element Level: ")
                .append(parm.getParmID().getParmLevel()).append("\n");

        info.append("Weather Element Model: ")
                .append(uiFormat.uiDatabaseID(parm.getParmID().getDbId()))
                .append("\n");

        info.append("Modified, but not yet saved: ")
                .append((parm.isModified() ? "Yes" : "No")).append("\n");

        info.append("Mutable Weather Element: ")
                .append((parm.isMutable() ? "Yes" : "No")).append("\n");

        info.append("Weather Element Type: ")
                .append(parm.getGridInfo().getGridType()).append("\n");

        info.append("Units: ").append(parm.getGridInfo().getUnitString())
                .append("\n");

        java.awt.Point gridSize = parm.getGridInfo().getGridLoc().gridSize();
        info.append("Grid Size is: (").append(gridSize.x).append(",")
                .append(gridSize.y).append(")\n");

        Coordinate origin = parm.getGridInfo().getGridLoc().getOrigin();
        Coordinate extent = parm.getGridInfo().getGridLoc().getExtent();
        DecimalFormat df = new DecimalFormat("0.#");
        info.append("Grid Location is: [o=(").append(df.format(origin.x))
                .append(",").append(df.format(origin.y)).append("),e=(")
                .append(df.format(extent.x)).append(",")
                .append(df.format(extent.y)).append(")]\n");
        info.append("Grid Projection is: ")
                .append(parm.getGridInfo().getGridLoc().getProjection()
                        .getProjectionID()).append("\n");

        if (parm.getGridInfo().getGridType().equals(GridType.SCALAR)
                || parm.getGridInfo().getGridType().equals(GridType.VECTOR)) {
            info.append("Minimum Allowed Value: ")
                    .append(parm.getGridInfo().getMinValue())
                    .append("  Maximum Allowed Value: ")
                    .append(parm.getGridInfo().getMaxValue()).append("\n");

        }

        info.append("Descriptive Name: ")
                .append(parm.getGridInfo().getDescriptiveName()).append("\n");

        TimeConstraints sb = parm.getGridInfo().getTimeConstraints();

        info.append("Time Constraints:  Duration=");

        info.append(sb.getDuration() / 3600).append("hr. Repeats every ");
        info.append(sb.getRepeatInterval() / 3600).append("hr. Starts at ");
        info.append(sb.getStartTime() / 3600).append("Z\n");

        info.append("Rate Dependent WE: ")
                .append((parm.getGridInfo().isRateParm() ? "Yes" : "No"))
                .append("\n");

        return info.toString();

    }

    private String getGridHistory() {
        StringBuilder info = new StringBuilder();
        info.append(getBoxTitle(gridData, true, "Grid History"));

        if (gridData == null) {
            info.append("No Grid");
        } else {
            for (GridDataHistory h : gridData.getHistory()) {

                info.append("Grid origin: ").append(h.getOrigin()).append("\n");
                info.append("Original Source: ")
                        .append(uiFormat.uiParmIDcollapsed(h.getOriginParm()))
                        .append("\n");

                info.append("Original ValidTime: ")
                        .append(this.timeRangeToGMT(h.getOriginTimeRange()))
                        .append("\n");

                if (!gridData.getGridTime().equals(h.getOriginTimeRange())) {
                    info.append("Grid has been time-shifted\n");
                }
                if (h.getTimeModified() == null) {
                    info.append("Grid not modified\n");
                } else {
                    info.append("Grid last modified at: ")
                            .append(gmtFormatter.format(h.getTimeModified()
                                    .getTime())).append(" ")
                            .append(formatAgo(h.getTimeModified()))
                            .append("\n");
                    if (h.getWhoModified().equals(
                            parm.getDataManager().getWsId())) {
                        info.append("Modified by me\n");
                    } else {
                        info.append("Modified by user: ")
                                .append(whoLabel(h.getWhoModified()))
                                .append("\n");
                    }
                }
                if (h.getPublishTime() != null) {
                    info.append("Last Published: ")
                            .append(gmtFormatter.format(h.getPublishTime()
                                    .getTime())).append(" ")
                            .append(formatAgo(h.getPublishTime())).append("\n");
                }
                if (h.getUpdateTime() != null) {
                    info.append("Last Stored: ")
                            .append(gmtFormatter.format(h.getUpdateTime()
                                    .getTime())).append(" ")
                            .append(formatAgo(h.getUpdateTime())).append("\n");
                }
                if (h.getLastSentTime() != null) {
                    info.append("Last Sent: ")
                            .append(gmtFormatter.format(h.getLastSentTime()
                                    .getTime())).append(" ")
                            .append(formatAgo(h.getLastSentTime()))
                            .append("\n");
                }
            }
        }

        return info.toString();
    }

    private String formatAgo(Date modTime) {

        long tdiff = (SimulatedTime.getSystemTime().getTime().getTime() - modTime
                .getTime()) / 1000;

        if (tdiff < 60) {
            return "(< 1 minute ago)";
        } else if (tdiff < 3600) {
            return "(" + (int) tdiff / 60 + " minutes ago)";
        } else if (tdiff < 86400) {
            int hours = (int) (tdiff / 3600);
            int minutes = (int) (tdiff % 3600 / 60);

            return "(" + hours + " hours " + minutes + " minutes ago)";
        } else {
            return "(more than 1 day ago)";
        }
    }

    private String getBoxTitle(IGridData gridData, boolean includeGridID,
            String title) {

        StringBuilder info = new StringBuilder();
        info.append("Weather Element: ")
                .append(uiFormat.uiParmIDcollapsed(parm.getParmID()))
                .append("\n");

        if (includeGridID) {
            info.append("Grid Valid Time: ");
            if (gridData != null) {
                info.append(this.timeRangeToGMT(gridData.getGridTime()));
            } else {
                info.append("No Grid");
            }
            info.append("\n");
        }

        info.append("\n").append(title).append(":\n\n");
        return info.toString();
    }

    private String timeRangeToGMT(TimeRange range) {
        if (range.isValid()) {
            return "(" + gmtFormatter.format(range.getStart()) + ", "
                    + gmtFormatter.format(range.getEnd()) + ")";
        } else {
            return range.toString();
        }
    }

    private String whoLabel(WsId wsId) {
        String progName = wsId.getProgName();
        // pos = string.rfind(progName, '/')
        // if pos != -1:
        // progName = progName[pos+1:len(progName)]
        String hostname = wsId.getHostName();

        String label = wsId.getUserName() + " (" + progName + ")" + "  on: "
                + hostname;
        return label;
    }
}
