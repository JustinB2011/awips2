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
package com.raytheon.uf.edex.plugin.redbook.ingest;

import java.io.File;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ndm.ingest.INationalDatasetSubscriber;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookCpcMenuUtil;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookHazardsMenuUtil;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookHpcMenuUtil;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookMpcMenuUtil;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookNcoMenuUtil;
import com.raytheon.uf.edex.plugin.redbook.menu.RedbookUaMenuUtil;

/**
 * Redbook menu subscriber. Takes redbook menu files and passes them to the
 * correct menu generators.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 10, 2014    2858    mpduff      Initial creation.
 * Mar 17, 2014    2855    mpduff      Implement HPC.
 * Mar 17, 2014    2856    mpduff      Implement CPC.
 * Mar 19, 2014    2857    mpduff      Implement NCO.
 * Mar 19, 2014    2859    mpduff      Implement MPC.
 * Mar 19, 2014    2860    mpduff      Implement Upper Air.
 * Jan 28, 2015    4030    mpduff      Changed constants to public.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class RedbookMenuSubscriber implements INationalDatasetSubscriber {
    /** Status handler */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RedbookMenuSubscriber.class);

    /** Hazard menu file */
    public static final String HAZARD_MENU_FILE = "RedbookHazardMenus.xml";

    /** HPC menu file */
    public static final String HPC_MENU_FILE = "RedbookHPCMenus.xml";

    /** CPC menu file */
    public static final String CPC_MENU_FILE = "RedbookCPCMenus.xml";

    /** MPC menu file */
    public static final String MPC_MENU_FILE = "RedbookMPCMenus.xml";

    /** NCO menu file */
    public static final String NCO_MENU_FILE = "RedbookNCOMenus.xml";

    /** Upper air menu file */
    public static final String UA_MENU_FILE = "RedbookUAMenus.xml";

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(String fileName, File file) {
        statusHandler.info("Processing " + fileName);
        if (HAZARD_MENU_FILE.equals(fileName)) {
            // Convert input file to output menu format
            RedbookHazardsMenuUtil menuUtil = new RedbookHazardsMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        } else if (HPC_MENU_FILE.equals(fileName)) {
            RedbookHpcMenuUtil menuUtil = new RedbookHpcMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        } else if (CPC_MENU_FILE.equals(fileName)) {
            RedbookCpcMenuUtil menuUtil = new RedbookCpcMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        } else if (MPC_MENU_FILE.equals(fileName)) {
            RedbookMpcMenuUtil menuUtil = new RedbookMpcMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        } else if (NCO_MENU_FILE.equals(fileName)) {
            RedbookNcoMenuUtil menuUtil = new RedbookNcoMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        } else if (UA_MENU_FILE.equals(fileName)) {
            RedbookUaMenuUtil menuUtil = new RedbookUaMenuUtil();
            menuUtil.createMenusFromFile(file.getAbsolutePath());
        }
    }
}
