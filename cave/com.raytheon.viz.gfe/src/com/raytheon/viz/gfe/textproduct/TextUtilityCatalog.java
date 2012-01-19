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
/**
 * 
 */
package com.raytheon.viz.gfe.textproduct;

import java.io.File;

import com.raytheon.uf.common.dataplugin.gfe.python.GfePyIncludeUtil;
import com.raytheon.viz.gfe.core.script.AbstractScriptCatalog;

/**
 * @author wldougher
 * 
 */
public class TextUtilityCatalog extends AbstractScriptCatalog {

    private static final String[] EXTENSIONS = { ".py" };

    private static final String PATH_PREFIX = GfePyIncludeUtil.TEXT_UTILITIES
            + File.separator + "regular";

    /**
     * @see com.raytheon.viz.gfe.core.script.AbstractScriptCatalog#getExtensions()
     */
    @Override
    public String[] getExtensions() {
        return EXTENSIONS;
    }

    /**
     * @see com.raytheon.viz.gfe.core.script.AbstractScriptCatalog#getScriptTypePathPrefix()
     */
    @Override
    public String getScriptTypePathPrefix() {
        return PATH_PREFIX;
    }
}
