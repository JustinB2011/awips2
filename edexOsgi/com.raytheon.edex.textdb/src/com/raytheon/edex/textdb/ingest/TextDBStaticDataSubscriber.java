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
package com.raytheon.edex.textdb.ingest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.raytheon.edex.textdb.dbapi.impl.TextDBStaticData;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.site.ingest.INationalDatasetSubscriber;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 25, 2011            bfarmer     Initial creation
 * Oct 18, 2011 10909      rferrel     notify() now saves a file.
 * 
 * </pre>
 * 
 * @author bfarmer
 * @version 1.0
 */

public class TextDBStaticDataSubscriber implements INationalDatasetSubscriber {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(TextDBStaticDataSubscriber.class);

    @Override
    public void notify(String fileName, File file) {
        // Assumes the fileName is the name of the file to place
        // in the BASE directory.
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext lc = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        File outFile = pathMgr.getFile(lc, "textdb/" + fileName);
        saveFile(file, outFile);
    }

    private void saveFile(File file, File outFile) {
        if ((file != null) && file.exists()) {
            try {
                BufferedReader fis = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
                BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outFile)));
                String line = null;
                try {
                    while ((line = fis.readLine()) != null) {
                        fos.write(line);
                        fos.newLine();
                    }
                    fos.close();
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Could not read File ", e);

                }
            } catch (FileNotFoundException e) {
                statusHandler.handle(Priority.PROBLEM, "Failed to find File ",
                        e);

            }
        }
        TextDBStaticData.setDirty();
    }

}
