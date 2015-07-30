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
package com.raytheon.viz.gfe.smarttool.script;

import jep.JepException;

import com.raytheon.viz.gfe.core.DataManager;

/**
 * Script factory for {@code SmartToolMetadataController} instances.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2015  #4263     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class SmartToolMetadataScriptFactory extends
        SmartToolFactory<SmartToolMetadataController> {

    private static final String SCRIPT_EXECUTOR_NAME = "smart-tool-metadata";

    private static final int EXECUTOR_NUM_THREADS = 1;

    public SmartToolMetadataScriptFactory(final DataManager dataMgr) {
        super(SCRIPT_EXECUTOR_NAME, EXECUTOR_NUM_THREADS, dataMgr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory#
     * createPythonScript()
     */
    @Override
    public SmartToolMetadataController createPythonScript() throws JepException {
        return new SmartToolMetadataController(getScriptPath(),
                getIncludePath(), getClass().getClassLoader(), dataMgr);
    }
}
