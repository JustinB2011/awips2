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
package com.raytheon.uf.common.localization;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.TestPathManager.TestLocalizationAdapter;
import com.raytheon.uf.common.util.TestUtil;

/**
 * Utility class to initialize the test {@link IPathManager} implementation.
 * This allows tests to lookup baselined localization files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2012 740        djohnson     Initial creation
 * Oct 23, 2012 1286       djohnson     Handle executing tests in Eclipse/command-line transparently.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */

public class PathManagerFactoryTest {

    private static File savedLocalizationFileDir;

    /**
     * Creates a test-only PathManager that can be used during tests.
     */
    public static void initLocalization() {
        initLocalization("OAX");
    }

    /**
     * Creates a test-only PathManager that can be used during tests, it is
     * configured for the specified site.
     */
    public static void initLocalization(final String site) {

        // Clear known file cache and the directory each time
        PathManager.fileCache.clear();
        File file = TestUtil.setupTestClassDir(PathManagerFactoryTest.class);
        savedLocalizationFileDir = new File(file, "utility");
        savedLocalizationFileDir.mkdirs();

        // But only install the path manager if the test version is not already
        // installed
        if (!(PathManagerFactory.pathManager instanceof TestPathManager)) {
            TestLocalizationAdapter adapter = (isRunningInEclipse()) ? new EclipseTestLocalizationAdapter(
                    site, savedLocalizationFileDir)
                    : new CommandLineTestLocalizationAdapter(site,
                            savedLocalizationFileDir);
            PathManagerFactory.pathManager = new TestPathManager(adapter);
        }
    }

    /**
     * Returns true if the JUnit test is running in Eclipse.
     * 
     * @return true if running in Eclipse
     */
    private static boolean isRunningInEclipse() {
        return new File("..", "edexOsgi").isDirectory();
    }

    @Before
    public void setUp() {
        PathManagerFactoryTest.initLocalization();
    }

    @Test
    public void testFindingCommonBaselineLocalizationFile() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);

        LocalizationFile lf = pm.getLocalizationFile(lc,
                "datadelivery/proxy.properties");
        File file = lf.getFile();
        assertTrue("Unable to find common baseline localization file!",
                file.exists());
    }

    @Test
    public void testFindingWorkAssignmentPluginLocalizationFile() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);

        LocalizationFile lf = pm.getLocalizationFile(lc,
                "datadelivery/bandwidthmap.xml");
        File file = lf.getFile();
        assertTrue(
                "Unable to find work assignment plugin provided localization file!",
                file.exists());
    }

    @Test
    public void testFindingCommonBaselinePluginLocalizationFile() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);

        LocalizationFile lf = pm.getLocalizationFile(lc,
                "site3LetterTo4LetterOverride.dat");
        File file = lf.getFile();
        assertTrue(
                "Unable to find common baseline plugin provided localization file!",
                file.exists());
    }

    @Test
    public void testFindingFileCreatesVersionInTestDirectory() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);

        LocalizationFile lf = pm.getLocalizationFile(lc,
                "site3LetterTo4LetterOverride.dat");
        File file = lf.getFile();
        assertTrue(
                "Localization file does not seem to have been copied!",
                file.getParentFile().getAbsolutePath()
                        .startsWith(savedLocalizationFileDir.getAbsolutePath()));
    }
}
