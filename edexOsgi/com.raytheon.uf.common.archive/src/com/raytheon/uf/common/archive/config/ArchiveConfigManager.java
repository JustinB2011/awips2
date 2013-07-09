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
package com.raytheon.uf.common.archive.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.raytheon.uf.common.archive.exception.ArchiveException;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationFileInputStream;
import com.raytheon.uf.common.localization.LocalizationFileOutputStream;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Manager for access to archive data information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May  1, 2013 1966       rferrel     Initial creation
 * May 29, 2013 1965       bgonzale    Added archive creation, purge, and save methods.
 *                                     Updated purgeExpiredFromArchive to check time of files in
 *                                     directory before purging them.
 *                                     Added null check for topLevelDirs in purgeExpiredFromArchive.
 *                                     Changed to use File.delete() instead of Apache FileUtil.deleteQuietly().
 *                                     Added warn logging for failure to delete.
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class ArchiveConfigManager {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ArchiveConfigManager.class);

    /** The only instance or this class. */
    private final static ArchiveConfigManager instance = new ArchiveConfigManager();

    /** Localize directory for the archive configuration files. */
    public final String ARCHIVE_DIR = "archive";

    /** Localization manager. */
    private IPathManager pathMgr;

    private final Map<String, LocalizationFile> archiveNameToLocalizationFileMap = new HashMap<String, LocalizationFile>();

    /** Mapping of archive configuration data keyed to the name. */
    private final Map<String, ArchiveConfig> archiveMap = new HashMap<String, ArchiveConfig>();

    /** Get the singleton. */
    public final static ArchiveConfigManager getInstance() {
        return instance;
    }

    /**
     * Private constructor for singleton.
     */
    private ArchiveConfigManager() {
        pathMgr = PathManagerFactory.getPathManager();
        try {
            reset();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get list of site's archive data configuration files.
     * 
     * @return archiveConfigList
     */
    private LocalizationFile[] getArchiveConfigFiles() {
        LocalizationFile[] files = pathMgr.listStaticFiles(ARCHIVE_DIR,
                new String[] { ".xml" }, false, true);
        return files;
    }

    /**
     * Get a sorted list of the archive data names.
     * 
     * @return names
     */
    public String[] getArchiveDataNamesList() {
        String[] names = archiveMap.keySet().toArray(new String[0]);
        Arrays.sort(names, 0, names.length, String.CASE_INSENSITIVE_ORDER);

        return names;
    }

    /**
     * Get a sorted array of the archive's categories.
     * 
     * @param archiveConfigName
     * @return categoryNames
     */
    public String[] getCategoryNames(String archiveConfigName) {
        return getCategoryNames(archiveMap.get(archiveConfigName));
    }

    /**
     * Get a sorted array of the archive's categories.
     * 
     * @param archiveConfig
     * @return categoryNames
     */
    public String[] getCategoryNames(ArchiveConfig archiveConfig) {
        List<CategoryConfig> categories = archiveConfig.getCategoryList();
        List<String> nameList = new ArrayList<String>(categories.size());
        for (CategoryConfig category : archiveConfig.getCategoryList()) {
            String name = category.getName();
            nameList.add(name);
        }
        String[] names = nameList.toArray(new String[0]);
        Arrays.sort(names, 0, names.length, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /**
     * Load the archiveConfig information from the localized file.
     * 
     * @param lFile
     * @return archiveConfig
     * @throws IOException
     * @throws LocalizationException
     */
    public ArchiveConfig loadArchiveData(LocalizationFile lFile)
            throws IOException, LocalizationException {
        return unmarshalArhiveConfigFromXmlFile(lFile);
    }

    /**
     * @return the Collection of Archives.
     */
    public Collection<ArchiveConfig> getArchives() {
        return archiveMap.values();
    }

    /**
     * Create an archive in the given archive directory from the Files filtered
     * by the ArchiveElements that are within the start and end time frame
     * parameters.
     * 
     * @param archiveDir
     *            Destination directory of the Archive
     * @param displaysSelectedForArchive
     *            List of display elements to filter inclusion into Archive
     * @param start
     *            Start time boundary of the Archive
     * @param end
     *            End time boundary of the Archive
     * @return the list of files added to the created archive.
     * @throws IOException
     * @throws ArchiveException
     */
    public Collection<File> createArchive(File archiveDir,
            Collection<DisplayData> displaysSelectedForArchive, Calendar start,
            Calendar end) throws IOException, ArchiveException {
        Collection<File> archivedFiles = new ArrayList<File>();
        FileUtils.forceMkdir(archiveDir);
        if (archiveDir.exists()) {
            for (DisplayData display : displaysSelectedForArchive) {
                String archiveDirString = archiveDir.getAbsolutePath();
                String rootDirString = display.archiveConfig.getRootDir();

                rootDirString = (rootDirString.endsWith(File.separator) ? rootDirString
                        .substring(0,
                                rootDirString.lastIndexOf(File.separatorChar))
                        : rootDirString);
                for (File srcFile : getDisplayFiles(display, start, end)) {
                    String fileAbsPath = srcFile.getAbsolutePath();
                    File newArchiveDir = getDirRelativeToArchiveDirFromRoot(
                            srcFile.isDirectory(), fileAbsPath, rootDirString,
                            archiveDirString);
                    FileUtils.forceMkdir(newArchiveDir);

                    if (srcFile.isDirectory()) {
                        FileUtils.copyDirectory(srcFile, newArchiveDir);
                        archivedFiles.addAll(Arrays.asList(newArchiveDir
                                .listFiles()));
                    } else {
                        FileUtils.copyFileToDirectory(srcFile, newArchiveDir);
                        String filename = FilenameUtils.getName(fileAbsPath);
                        File dstFile = new File(newArchiveDir, filename);
                        archivedFiles.add(dstFile);
                    }
                }
            }
        } else {
            StringBuilder sbuff = new StringBuilder(
                    "Failed to create archive in: ");
            sbuff.append(archiveDir);
            throw new ArchiveException(sbuff.toString());
        }
        return archivedFiles;
    }

    private File getDirRelativeToArchiveDirFromRoot(boolean fileIsDir,
            String fileAbsPath, String rootDirString, String archiveDirString) {
        String path = null;
        if (fileIsDir) {
            path = fileAbsPath;
        } else {
            path = FilenameUtils.getFullPath(fileAbsPath);
        }
        String newArchivePathString = path.replace(rootDirString,
                archiveDirString);
        return new File(newArchivePathString);
    }

    /**
     * Purge the Files that fall outside of the time frame constraints for the
     * Archive.
     * 
     * @param archive
     * @return the list of expired Files purged
     */
    public Collection<File> purgeExpiredFromArchive(ArchiveConfig archive) {
        Collection<File> filesPurged = new ArrayList<File>();
        String archiveRootDirPath = archive.getRootDir();
        File archiveRootDir = new File(archiveRootDirPath);
        String[] topLevelDirs = archiveRootDir.list();
        List<String> topLevelDirsNotPurged = new ArrayList<String>();

        if (topLevelDirs != null) {
            topLevelDirsNotPurged.addAll(Arrays.asList(topLevelDirs));
        }
        for (CategoryConfig category : archive.getCategoryList()) {
            Calendar purgeTime = calculateExpiration(archive, category);
            CategoryFileDateHelper helper = new CategoryFileDateHelper(
                    category, archive.getRootDir());
            IOFileFilter fileDateFilter = FileFilterUtils.and(
                    FileFilterUtils.fileFileFilter(),
                    new FileDateFilter(null, purgeTime, helper));

            // Remove the directory associated with this category from the not
            // purged list since it is being purged.
            for (Iterator<String> iter = topLevelDirsNotPurged.iterator(); iter
                    .hasNext();) {
                String dirName = iter.next();
                if (helper.isCategoryDirectory(dirName)) {
                    iter.remove();
                    break;
                }
            }
            for (DisplayData display : getDisplayData(archive.getName(),
                    category.getName(), true)) {
                List<File> displayFiles = getDisplayFiles(display, null,
                        purgeTime);
                for (File file : displayFiles) {
                    filesPurged.addAll(purgeFile(file, fileDateFilter,
                            archiveRootDirPath));
                }
            }
        }

        // check for other expired in top level directories not covered
        // by the categories in the archives
        Calendar defaultPurgeTime = calculateExpiration(archive, null);
        for (String topDirName : topLevelDirsNotPurged) {
            IOFileFilter fileDateFilter = FileFilterUtils.and(FileFilterUtils
                    .fileFileFilter(), new FileDateFilter(null,
                    defaultPurgeTime));
            File topLevelDir = new File(archiveRootDir, topDirName);

            filesPurged.addAll(purgeFile(topLevelDir, fileDateFilter,
                    archiveRootDirPath));
        }
        return filesPurged;
    }

    private Collection<File> purgeFile(File fileToPurge,
            IOFileFilter filter, final String archiveRootDir) {
        Collection<File> filesPurged = new ArrayList<File>();

        if (fileToPurge.isFile() && filter.accept(fileToPurge)) {
            if (fileToPurge.delete()) {
                filesPurged.add(fileToPurge);
            } else {
                statusHandler.warn("Failed to purge file: "
                        + fileToPurge.getAbsolutePath());
            }
        } else if (fileToPurge.isDirectory()) {
            Collection<File> expiredFilesInDir = FileUtils.listFiles(
                    fileToPurge, filter, FileFilterUtils.trueFileFilter());

            for (File dirFile : expiredFilesInDir) {
                filesPurged.addAll(purgeFile(dirFile, filter, archiveRootDir));
            }

            // if the directory is empty and not the archive root dir, then
            // delete it
            if (fileToPurge.list().length == 0
                    && !fileToPurge.getAbsolutePath().equals(archiveRootDir)) {
                if (!fileToPurge.delete()) {
                    statusHandler.warn("Failed to purge directory: "
                            + fileToPurge.getAbsolutePath());
                }
            }
        }
        return filesPurged;
    }

    private Calendar calculateExpiration(ArchiveConfig archive,
            CategoryConfig category) {
        Calendar newCal = TimeUtil.newGmtCalendar();
        int retHours = category == null || category.getRetentionHours() == 0 ? archive
                .getRetentionHours() : category.getRetentionHours();
        if (retHours != 0) {
            newCal.add(Calendar.HOUR, (-1) * retHours);
        }
        return newCal;
    }

    /**
     * @return the archive with the given name; return null if it does not
     *         exist.
     */
    public ArchiveConfig getArchive(String archiveName) {
        return archiveMap.get(archiveName);
    }

    /**
     * Save the Archive Configuration data to the localized file.
     * 
     * @param lFile
     * @param archiveConfig
     * @throws IOException
     * @throws LocalizationException
     */
    public void saveArchiveConfig(LocalizationFile lFile,
            ArchiveConfig archiveConfig) {
        try {
            marshalArchiveConfigToXmlFile(archiveConfig, lFile);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Clear maps and reloads the maps from the configuration information.
     * 
     * @throws IOException
     * @throws LocalizationException
     */
    public void reset() {
        archiveMap.clear();
        archiveNameToLocalizationFileMap.clear();
        LocalizationFile[] files = getArchiveConfigFiles();
        for (LocalizationFile lFile : files) {
            try {
                ArchiveConfig archiveConfig = unmarshalArhiveConfigFromXmlFile(lFile);
                archiveNameToLocalizationFileMap.put(archiveConfig.getName(),
                        lFile);
                archiveMap.put(archiveConfig.getName(), archiveConfig);
            } catch (IOException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            } catch (LocalizationException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
    }

    /**
     * Save the cached configuration.
     */
    public void save() {
        LocalizationContext siteContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        for (String archiveName : archiveMap.keySet()) {
            ArchiveConfig archiveConfig = archiveMap.get(archiveName);
            LocalizationFile lFile = archiveNameToLocalizationFileMap
                    .get(archiveName);
            if (lFile.getContext().getLocalizationLevel() != LocalizationLevel.SITE) {
                // Modify the site not the base file.
                LocalizationFile tlFile = pathMgr.getLocalizationFile(
                        siteContext, lFile.getName());
                lFile = tlFile;
            }
            saveArchiveConfig(lFile, archiveConfig);
        }
    }

    /**
     * Get a list of directories/files for the desired display label bounded by
     * the start and end time inclusive.
     * 
     * @param displayData
     * @param startCal
     * @param endCal
     * @return files
     */
    public List<File> getDisplayFiles(DisplayData displayData,
            Calendar startCal, Calendar endCal) {
        long startTime = 0L;
        if (startCal != null) {
            // Set to beginning of the hour
            Calendar cal = (Calendar) startCal.clone();
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        }

        Calendar cal = null;
        if (endCal != null) {
            cal = (Calendar) endCal.clone();
        } else {
            cal = TimeUtil.newCalendar();
        }

        // Set to start of the next hour.
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR_OF_DAY, 1);

        long endTime = cal.getTimeInMillis();
        return getDisplayFiles(displayData, startTime, endTime);
    }

    /**
     * Get all of the files/directories associated with the display label.
     * 
     * @param archiveName
     * @param categoryName
     * @param displayLabel
     * @return files
     */
    public List<File> getDisplayFiles(DisplayData displayData) {
        return getDisplayFiles(displayData, null, null);
    }

    /**
     * Get the files/directories associated with the display label with time
     * stamps between inclusive start time and exclusive end time.
     * 
     * @param archiveName
     * @param categoryName
     * @param displayLabel
     * @param startTime
     * @param endTime
     * @return files
     */
    private List<File> getDisplayFiles(DisplayData displayData, long startTime,
            long endTime) {
        ArchiveConfig archiveConfig = displayData.archiveConfig;
        CategoryConfig categoryConfig = displayData.categoryConfig;

        String[] indexValues = categoryConfig.getDateGroupIndices().split(
                "\\s*,\\s*");
        int yearIndex = Integer.parseInt(indexValues[0]);
        int monthIndex = Integer.parseInt(indexValues[1]);
        int dayIndex = Integer.parseInt(indexValues[2]);
        int hourIndex = Integer.parseInt(indexValues[3]);

        String filePatternStr = categoryConfig.getFilePattern();

        boolean dirOnly = (filePatternStr == null)
                || ".*".equals(filePatternStr);

        List<File> dirs = displayData.dirs;

        int beginIndex = archiveConfig.getRootDir().length();

        Calendar fileCal = TimeUtil.newCalendar();
        fileCal.setTimeZone(TimeZone.getTimeZone("UTC"));

        List<File> fileList = new ArrayList<File>();

        if (dirOnly) {
            for (String dirPattern : categoryConfig.getDirPatternList()) {
                Pattern pattern = Pattern.compile(dirPattern);

                for (File dir : dirs) {
                    String path = dir.getAbsolutePath().substring(beginIndex);
                    Matcher matcher = pattern.matcher(path);
                    if (matcher.matches()) {
                        int year = Integer.parseInt(matcher.group(yearIndex));
                        // Adjust month value to Calendar's 0 - 11
                        int month = Integer.parseInt(matcher.group(monthIndex)) - 1;
                        int day = Integer.parseInt(matcher.group(dayIndex));
                        int hour = Integer.parseInt(matcher.group(hourIndex));
                        fileCal.set(year, month, day, hour, 0, 0);
                        long fileTime = fileCal.getTimeInMillis();
                        if ((startTime <= fileTime) && (fileTime < endTime)) {
                            fileList.add(dir);
                        }
                    }
                }
            }
        } else {
            for (String dirPattern : categoryConfig.getDirPatternList()) {
                Pattern pattern = Pattern.compile(dirPattern + File.separator
                        + categoryConfig.getFilePattern());
                final Pattern filePattern = Pattern.compile("^"
                        + filePatternStr + "$");
                for (File dir : dirs) {
                    List<File> fList = FileUtil.listDirFiles(dir,
                            new FileFilter() {

                                @Override
                                public boolean accept(File pathname) {
                                    return filePattern.matcher(
                                            pathname.getName()).matches();
                                }
                            }, false);
                    for (File file : fList) {
                        String path = file.getAbsolutePath().substring(
                                beginIndex);
                        Matcher matcher = pattern.matcher(path);
                        if (matcher.matches()) {
                            int year = Integer.parseInt(matcher
                                    .group(yearIndex));
                            // Adjust month value to Calendar's 0 - 11
                            int month = Integer.parseInt(matcher
                                    .group(monthIndex)) - 1;
                            int day = Integer.parseInt(matcher.group(dayIndex));
                            int hour = Integer.parseInt(matcher
                                    .group(hourIndex));
                            fileCal.set(year, month, day, hour, 0, 0);
                            long fileTime = fileCal.getTimeInMillis();
                            if ((startTime <= fileTime) && (fileTime < endTime)) {
                                fileList.add(file);
                            }
                        }
                    }
                }
            }
        }

        return fileList;
    }

    /**
     * Get a list of directories matching the categories directory patterns that
     * are sub-directories of the archive's root directory.
     * 
     * @param archiveConfig
     * @param categoryConfig
     * @return dirs
     */
    private List<File> getDirs(ArchiveConfig archiveConfig,
            CategoryConfig categoryConfig) {
        List<File> resultDirs = new ArrayList<File>();
        File rootFile = new File(archiveConfig.getRootDir());
        List<File> dirs = new ArrayList<File>();
        List<File> tmpDirs = new ArrayList<File>();
        List<File> swpDirs = null;

        for (String dirPattern : categoryConfig.getDirPatternList()) {
            String[] subExpr = dirPattern.split(File.separator);
            dirs.clear();
            dirs.add(rootFile);
            tmpDirs.clear();

            for (String regex : subExpr) {
                Pattern subPattern = Pattern.compile("^" + regex + "$");
                IOFileFilter filter = FileFilterUtils
                        .makeDirectoryOnly(new RegexFileFilter(subPattern));

                for (File dir : dirs) {
                    File[] list = dir.listFiles();
                    if (list != null) {
                        List<File> dirList = Arrays.asList(list);
                        tmpDirs.addAll(Arrays.asList(FileFilterUtils.filter(
                                filter, dirList)));
                    }
                }
                swpDirs = dirs;
                dirs = tmpDirs;
                tmpDirs = swpDirs;
                tmpDirs.clear();
            }
            resultDirs.addAll(dirs);
        }
        return resultDirs;
    }

    /**
     * Get the Display labels matching the pattern for the archive data's
     * category. Assumes the archive data's root directory is the mount point to
     * start the search.
     * 
     * @param archiveName
     * @param categoryName
     * @return displayInfoList order by display label
     */
    public List<DisplayData> getDisplayData(String archiveName,
            String categoryName) {
        return getDisplayData(archiveName, categoryName, false);
    }

    /**
     * Get the Display labels matching the pattern for the archive data's
     * category. Assumes the archive data's root directory is the mount point to
     * start the search.
     * 
     * @param archiveName
     * @param categoryName
     * @param setSelect
     *            - when true set the displayData selection base on category's
     *            selection list
     * @return displayDataList
     */
    public List<DisplayData> getDisplayData(String archiveName,
            String categoryName, boolean setSelect) {
        Map<String, List<File>> displayMap = new HashMap<String, List<File>>();

        ArchiveConfig archiveConfig = archiveMap.get(archiveName);
        CategoryConfig categoryConfig = findCategory(archiveConfig,
                categoryName);
        List<String> dirPatternList = categoryConfig.getDirPatternList();

        // index for making directory paths' relative to the root path.
        List<File> dirs = getDirs(archiveConfig, categoryConfig);

        File rootFile = new File(archiveMap.get(archiveName).getRootDir());
        int beginIndex = rootFile.getAbsolutePath().length() + 1;
        List<Pattern> patterns = new ArrayList<Pattern>(dirPatternList.size());

        for (String dirPattern : dirPatternList) {
            Pattern pattern = Pattern.compile("^" + dirPattern + "$");
            patterns.add(pattern);
        }

        TreeSet<String> displays = new TreeSet<String>(
                String.CASE_INSENSITIVE_ORDER);

        MessageFormat msgfmt = new MessageFormat(categoryConfig.getDisplay());
        StringBuffer sb = new StringBuffer();
        FieldPosition pos0 = new FieldPosition(0);

        for (File dir : dirs) {
            String path = dir.getAbsolutePath().substring(beginIndex);
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    sb.setLength(0);
                    String[] args = new String[matcher.groupCount() + 1];
                    args[0] = matcher.group();
                    for (int i = 1; i < args.length; ++i) {
                        args[i] = matcher.group(i);
                    }
                    String displayLabel = msgfmt.format(args, sb, pos0)
                            .toString();
                    List<File> displayDirs = displayMap.get(displayLabel);
                    if (displayDirs == null) {
                        displayDirs = new ArrayList<File>();
                        displayMap.put(displayLabel, displayDirs);
                    }
                    displayDirs.add(dir);
                    displays.add(displayLabel);
                    break;
                }
            }
        }

        List<DisplayData> displayDataList = new ArrayList<DisplayData>(
                displays.size());

        for (String displayLabel : displays) {
            DisplayData displayData = new DisplayData(archiveConfig,
                    categoryConfig, displayLabel, displayMap.get(displayLabel));
            if (setSelect) {
                displayData.setSelected(categoryConfig
                        .getSelectedDisplayNames().contains(displayLabel));
            }
            displayDataList.add(displayData);
        }

        return displayDataList;
    }

    /**
     * Obtain the category with the given name.
     * 
     * @param archiveConfig
     * @param categoryName
     * @return categoryConfig
     */
    private CategoryConfig findCategory(ArchiveConfig archiveConfig,
            String categoryName) {
        for (CategoryConfig category : archiveConfig.getCategoryList()) {
            if (category.getName().equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

    /**
     * This does the work of saving an instance of ArchiveConfig to a localized
     * file.
     * 
     * @param archiveConfig
     * @param lFile
     * @throws IOException
     * @throws LocalizationException
     */
    private void marshalArchiveConfigToXmlFile(ArchiveConfig archiveConfig,
            LocalizationFile lFile) throws IOException, LocalizationException {
        LocalizationFileOutputStream stream = null;
        stream = lFile.openOutputStream();
        JAXB.marshal(archiveConfig, stream);
        stream.closeAndSave();
    }

    /**
     * This does the work of taking a localized file and returning a instance of
     * the ArchiveConfig class from the data in the file.
     * 
     * @param lFile
     * @return archiveConfig
     * @throws IOException
     * @throws LocalizationException
     */
    private ArchiveConfig unmarshalArhiveConfigFromXmlFile(
            LocalizationFile lFile) throws IOException, LocalizationException {
        ArchiveConfig archiveConfig = null;
        LocalizationFileInputStream stream = null;
        try {
            stream = lFile.openInputStream();
            archiveConfig = JAXB.unmarshal(stream, ArchiveConfig.class);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        return archiveConfig;
    }

}
