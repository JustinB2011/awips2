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
package com.raytheon.edex.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.localization.ILocalizationAdapter;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.exception.LocalizationOpFailedException;
import com.raytheon.uf.edex.core.props.EnvProperties;
import com.raytheon.uf.edex.core.props.PropertiesFactory;

/**
 * TODO: Should we be sending out FileUpdateMessages for save/delete?
 * 
 * Provides unified methods of accessing localization files from EDEX code.
 * <p>
 * Methods provided in this class are tailored to support paths to files from
 * EDEX specific code.
 * </p>
 * <p>
 * Some methods defined in the {@link ILocalizationAdapter} interface are not
 * useful from the EDEX side since all files are locally stored. If called, the
 * useless methods will no op.
 * </p>
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jul 11, 2008 1250        jelkins     Initial creation
 * </pre>
 * 
 * @author jelkins
 * @version 1.0
 */

public class EDEXLocalizationAdapter implements ILocalizationAdapter {

    private final Map<LocalizationType, LocalizationContext[]> contexts;

    /**
     * Constructs this class
     */
    public EDEXLocalizationAdapter() {
        this.contexts = new HashMap<LocalizationType, LocalizationContext[]>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.edex.utility.ILocalizationAdapter#getDirNameForType(com.
     * raytheon.edex.utility.LocalizationContext.LocalizationType)
     */
    @Override
    public String getDirNameForType(LocalizationType type) {

        if (type == LocalizationType.UNKNOWN) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        } else {
            return type.toString().toLowerCase();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.edex.utility.ILocalizationAdapter#getLocalSearchHierarchy
     * (com.raytheon.edex.utility.LocalizationContext.LocalizationType)
     */
    @Override
    public LocalizationContext[] getLocalSearchHierarchy(LocalizationType type) {

        synchronized (this.contexts) {
            LocalizationContext[] ctx = this.contexts.get(type);
            if (ctx == null) {
                ctx = new LocalizationContext[3];
                ctx[0] = getContext(type, LocalizationLevel.SITE);
                ctx[1] = getContext(type, LocalizationLevel.CONFIGURED);
                ctx[2] = getContext(type, LocalizationLevel.BASE);
                this.contexts.put(type, ctx);
            }
            // return a copy for safety in case someone messes with references
            // to the returned values
            LocalizationContext[] cloned = new LocalizationContext[ctx.length];
            for (int i = 0; i < ctx.length; i++) {
                cloned[i] = (LocalizationContext) ctx[i].clone();
            }
            return cloned;
        }
    }

    private String getSiteName() {
        String site = PropertiesFactory.getInstance().getEnvProperties()
                .getEnvValue("SITENAME");

        if ((site == null) || site.isEmpty()) {
            site = "none";
        }
        return site;
    }

    /**
     * Obtain file or directory metadata
     * <p>
     * Populate a ListResponse Array with metadata pertaining to the given file
     * or directory within the given contexts
     * </p>
     * 
     * @param context
     *            the contexts in which to obtain metadata for the given file or
     *            directory
     * 
     * @param fileName
     *            the file or directory for which to obtain metadata
     * 
     * @return a ListResponse Array where each entry details metadata for the
     *         given file or directory in the given contexts
     * 
     * @see com.raytheon.uf.common.localization.ILocalizationAdapter#getLocalizationMetadata(com.raytheon.uf.common.localization.LocalizationContext[],
     *      java.lang.String)
     */
    @Override
    public ListResponse[] getLocalizationMetadata(
            LocalizationContext[] context, String fileName)
            throws LocalizationOpFailedException {

        List<ListResponse> contents = new ArrayList<ListResponse>(
                context.length);

        for (LocalizationContext ctx : context) {
            ListResponse entry = createListResponse(ctx, fileName,
                    getPath(ctx, fileName));
            contents.add(entry);
        }

        return contents.toArray(new ListResponse[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.edex.utility.ILocalizationAdapter#getPath(com.raytheon.edex
     * .utility.LocalizationContext, java.lang.String)
     */
    @Override
    public File getPath(LocalizationContext context, String fileName) {

        String baseDir = null;

        EnvProperties env = PropertiesFactory.getInstance().getEnvProperties();

        String utilityDir = new File(env.getEnvValue("UTILITYDIR"))
                .getAbsolutePath();

        if (context.getLocalizationLevel() == LocalizationLevel.UNKNOWN) {
            throw new IllegalArgumentException(
                    "Unsupported localization level:"
                            + context.getLocalizationLevel());
            // } else if (false) {
            // TODO: Check for invalid type / level combinations
            // Change the above condition and add invalid type / level checking
            // if needed
        } else {
            baseDir = utilityDir + File.separator + context.toPath();
        }

        return new File(baseDir + File.separator + fileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.edex.utility.ILocalizationAdapter#getStaticContexts()
     */
    @Override
    public LocalizationType[] getStaticContexts() {

        LocalizationType[] type = new LocalizationType[] {
                LocalizationType.EDEX_STATIC, LocalizationType.COMMON_STATIC };

        return type;
    }

    /**
     * Create ListResponse metadata
     * <p>
     * Populate a ListResponce data object with file context, name, data
     * modified, directory flag, and MD5 checksum.
     * </p>
     * 
     * @param ctx
     *            the context to use when looking up the metadata for the
     *            filename
     * @param file
     *            the file for which to find metadata
     * @return a ListResponse containing metadata information. If the file does
     *         not exist calling the following fields of ListResponse will
     *         return:
     *         <ul>
     *         <li>directory: false</li>
     *         <li>date: 0ms since epoch</li>
     *         <li>checksum: null</li>
     *         </ul>
     */
    private ListResponse createListResponse(LocalizationContext ctx,
            String basePath, File file) {
        ListResponse entry = new ListResponse();

        entry.isDirectory = file.isDirectory();
        entry.context = ctx;
        String fullPath = file.getAbsolutePath();
        entry.fileName = fullPath.substring(fullPath.indexOf(basePath));
        entry.date = new Date(file.lastModified());

        entry.isProtected = ProtectedFiles.getProtectedLevel(null,
                ctx.getLocalizationType(), entry.fileName) != null;

        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.edex.utility.ILocalizationAdapter#listDirectory(com.raytheon
     * .edex.utility.LocalizationContext[], java.lang.String, boolean, boolean)
     */
    @Override
    public ListResponse[] listDirectory(LocalizationContext[] context,
            String path, boolean recursive, boolean filesOnly)
            throws LocalizationOpFailedException {

        // use the Set datatype to ensure no duplicate entries, use linked to
        // ensure order is deterministic when scanning multiple contexts
        Set<ListResponse> contents = new LinkedHashSet<ListResponse>();

        for (LocalizationContext ctx : context) {

            ArrayList<File> fileList = com.raytheon.uf.common.util.FileUtil
                    .listFiles(getPath(ctx, path), null, recursive);

            for (File file : fileList) {

                if (file.isDirectory() && filesOnly) {
                    // skip
                } else {
                    ListResponse entry = createListResponse(ctx, path, file);
                    contents.add(entry);
                }
            }

        }

        return contents.toArray(new ListResponse[0]);

    }

    // --- CRUD Operations ---------------------------------------------------

    /**
     * <EM>UNSUPPORTED</EM> Create a file
     * <p>
     * On EDEX all file operations are local and do not need to be created,
     * read, updated, or deleted from a remote source. Instead of using the CRUD
     * methods provided by the {@link ILocalizationAdapter} interface use
     * {@link #getPath(LocalizationContext, String)} and appropriate File object
     * methods.
     * </p>
     * 
     * @see com.raytheon.uf.common.localization.ILocalizationAdapter#save(java.io.File,
     *      com.raytheon.uf.common.localization.LocalizationContext,
     *      java.lang.String)
     */
    @Override
    public boolean save(File localFile, LocalizationContext context,
            String fileName) throws LocalizationOpFailedException {

        if (context.getLocalizationLevel().equals(LocalizationLevel.BASE)) {
            throw new UnsupportedOperationException(
                    "Saving to the BASE context is not supported.");
        }
        return true;
    }

    /**
     * Delete a file. Deletes the local file.
     * 
     * @see com.raytheon.uf.common.localization.ILocalizationAdapter#delete(java.io.File,
     *      com.raytheon.uf.common.localization.LocalizationContext,
     *      java.lang.String)
     */
    @Override
    public boolean delete(File file, LocalizationContext context,
            String fileName) throws LocalizationOpFailedException {
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    @Override
    public LocalizationContext getContext(LocalizationType type,
            LocalizationLevel level) {

        String contextName = null;
        if (level == LocalizationLevel.BASE) {
            // nothing to add
        } else if (level == LocalizationLevel.SITE
                || level == LocalizationLevel.CONFIGURED) {
            // fill in site name
            contextName = getSiteName();
        } else {
            // EDEX has no concept of current user or personality
            contextName = "none";
        }

        LocalizationContext ctx = new LocalizationContext(type, level,
                contextName);
        return ctx;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.localization.ILocalizationAdapter#getUserSiteList
     * ()
     */
    @Override
    public String[] getContextList(LocalizationLevel level) {
        return new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.localization.ILocalizationAdapter#retrieve(com
     * .raytheon.uf.common.localization.LocalizationFile)
     */
    @Override
    public void retrieve(LocalizationFile file)
            throws LocalizationOpFailedException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.localization.ILocalizationAdapter#getAvailableLevels
     * ()
     */
    @Override
    public LocalizationLevel[] getAvailableLevels() {
        return new LocalizationLevel[] { LocalizationLevel.BASE,
                LocalizationLevel.CONFIGURED, LocalizationLevel.SITE };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.localization.ILocalizationAdapter#exists(com.raytheon
     * .uf.common.localization.LocalizationFile)
     */
    @Override
    public boolean exists(LocalizationFile file) {
        return file.getFile().exists();
    }

}
