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
package com.raytheon.uf.viz.monitor.ffmp.ui.rsc;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.colormap.ColorMap;
import com.raytheon.uf.common.colormap.IColorMap;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPRecord;
import com.raytheon.uf.common.dataplugin.ffmp.FFMPRecord.FIELDS;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.viz.core.drawables.ColorMapLoader;
import com.raytheon.uf.viz.core.drawables.ColorMapParameters;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.style.ParamLevelMatchCriteria;
import com.raytheon.uf.viz.core.style.StyleManager;
import com.raytheon.uf.viz.core.style.StyleRule;
import com.raytheon.uf.viz.core.style.VizStyleException;
import com.raytheon.viz.core.style.image.ImagePreferences;

/**
 * FFMPColor Utility
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 08/29/09      2152       D. Hladky   Initial release
 * 
 * </pre>
 * 
 * @author dhladky
 * @version 1
 */

public class FFMPColorUtils {

    private ColorMapParameters colormapparams = null;

    private FIELDS field = null;

    private boolean tableLoad = false;

    private double time = 0.0;

    private ArrayList<String> fileArray = new ArrayList<String>();

    private TreeMap<Double, String> hourColorMapMap = new TreeMap<Double, String>();

    /**
     * Set up FFMP Color maps
     * 
     * @param field
     * @param time
     * @param tableLoad
     */
    public FFMPColorUtils(FIELDS field, double time, boolean tableLoad) {

        this.field = field;
        this.time = time;
        this.tableLoad = tableLoad;
        this.colormapparams = null;

        // LocalizationFile[] files = ColorMapLoader.listColorMapFiles();
        // for (LocalizationFile file : files) {
        // String fn = file.getName();
        // if (fn.startsWith("colormaps/ffmp/qpe"))
        // {
        // System.out.println(file.getName());
        // String hour = fn.s
        // }
        //
        // }

        StyleRule sr = null;
        try {
            sr = StyleManager.getInstance().getStyleRule(
                    StyleManager.StyleType.IMAGERY, getMatchCriteria());
        } catch (VizStyleException e) {
            e.printStackTrace();
        }
        String colormapfile = ((ImagePreferences) sr.getPreferences())
                .getDefaultColormap();

        IColorMap cxml = null;

        try {
            cxml = ColorMapLoader.loadColorMap(colormapfile);
        } catch (VizException e) {
            e.printStackTrace();
        }

        ColorMap colorMap = new ColorMap(colormapfile, (ColorMap) cxml);
        colormapparams = new ColorMapParameters();
        colormapparams.setColorMap(colorMap);
        colormapparams.setDisplayUnit(((ImagePreferences) sr.getPreferences())
                .getDisplayUnits());
        colormapparams.setDataMapping(((ImagePreferences) sr.getPreferences())
                .getDataMapping());

        colormapparams.setColorMapMin(0);
        colormapparams.setColorMapMax(255);
    }

    public ColorMapParameters getColorMapParameters() {
        return colormapparams;
    }

    /**
     * Gets the ColorMap
     * 
     * @return
     */
    private ColorMap getColorMap() {
        return (ColorMap) colormapparams.getColorMap();
    }

    /**
     * convert color
     * 
     * @param color
     * @return
     */
    private static RGB convert(Color color) {
        int blue;
        int green;
        int red;
        RGB returnColor = null;
        if (color != null) {
            blue = new Float(color.getBlue() * 255.0).intValue();
            green = new Float(color.getGreen() * 255.0).intValue();
            red = new Float(color.getRed() * 255.0).intValue();
            returnColor = new RGB(red, green, blue);
        }

        return returnColor;
    }

    /**
     * the check and assignment of colors
     * 
     * @param value
     * @return rgb value
     * @throws VizException
     */
    protected RGB colorByValue(double valueArg) throws VizException {

        int ret = 0;
        RGB rgb = null;

        if (Double.isNaN(valueArg)) {
            rgb = convert(getColorMap().getColors().get(ret));
            return rgb;
        }

        double val2 = (Math.round(valueArg * 100.0)) / 100.0;
        Double value = val2;

        if (value < 0.005 && field != FIELDS.DIFF) {
            ret = 0;
        } else if (field == FIELDS.DIFF) {

            Color color = colormapparams.getColorByValue(value.floatValue());
            rgb = convert(color);
            return rgb;

        } else {
            if (value < 0.0) {
                ret = 0;
            } else {
                Color color = colormapparams
                        .getColorByValue(value.floatValue());
                rgb = convert(color);
                return rgb;
            }
        }

        if (ret >= getColorMap().getColors().size()) {
            ret = getColorMap().getColors().size() - 1;
        }

        if (ret < 0) {
            ret = 0;
        }

        rgb = convert(getColorMap().getColors().get(ret));
        return rgb;
    }

    /**
     * Get and load the style rule
     * 
     * @return
     */
    private ParamLevelMatchCriteria getMatchCriteria() {
        ParamLevelMatchCriteria match = new ParamLevelMatchCriteria();
        ArrayList<String> paramList = new ArrayList<String>();

        if (field == FFMPRecord.FIELDS.QPE || field == FIELDS.QPF
                || field == FIELDS.GUIDANCE) {
            // qpe cases
            if (tableLoad) {
                String qpeName = FIELDS.QPE.getFieldName()
                        + determineQpeToUse(time);

                paramList.add(qpeName);
                // if (time < 6.0) {
                // paramList.add(FIELDS.QPE.getFieldName());
                // // System.out.println("QPE: less than 6");
                // } else if (time >= 6.0 && time < 12.0) {
                // paramList.add(FIELDS.QPE.getFieldName() + "6");
                // // System.out.println("QPE: greater than 6 less than 12");
                // } else if (time >= 12.0) {
                // paramList.add(FIELDS.QPE.getFieldName() + "12");
                // // System.out.println("QPE: greater than 12");
                // }
            } else {
                if (field == FIELDS.GUIDANCE) {
                    paramList.add(FIELDS.GUIDANCE.getFieldName());
                } else {
                    paramList.add(FIELDS.QPE.getFieldName());
                }
            }
        } else if (field == FIELDS.RATIO) {
            // ratio case
            paramList.add(FIELDS.RATIO.getFieldName());
        } else if (field == FIELDS.DIFF) {
            // rate, ratio and diff cases
            paramList.add(FIELDS.DIFF.getFieldName());
        } else if (field == FFMPRecord.FIELDS.RATE) {
            // rate case
            paramList.add(FIELDS.RATE.getFieldName());
        }

        match.setParameterName(paramList);

        return match;
    }

    private String determineQpeToUse(double time) {
        getQpeColorMapFiles();
        parseFileNames();
        String qpeHourToUse = determineColorMap(time);

        return qpeHourToUse;
    }

    private void parseFileNames() {
        double hour = 0.0;
        for (String fn : fileArray) {
            hour = 0.0;

            if (fn.indexOf("ffmp/qpe") >= 0) {

                String name1 = fn.replaceAll("colormaps/ffmp/qpe", "");
                String name2 = name1.replaceAll(".cmap", "");

                if (name2.length() == 0) {
                    hourColorMapMap.put(0.0, fn);
                } else {
                    hour = Double.parseDouble(name2);
                    hourColorMapMap.put(hour, fn);
                }
            }
        }
    }

    private String determineColorMap(double durHour) {
        String qpeHourToUse = null;
        for (Double dblHour : hourColorMapMap.keySet()) {
            if (durHour < dblHour) {
                break;
            }

            if (dblHour != 0.0) {

                // create a tmp value that will store the integer
                // part of the time. Example: 6.25 --> 6
                int intHour = dblHour.intValue();

                /*
                 * If the difference between double hour and int hour is greater
                 * than 0, set the qpeHourToUse to the double value.
                 * 
                 * If the difference between double hour and int hour is zero,
                 * then set qpeHourToUse to the int hour value.
                 * 
                 * The reason for this is that a color map name would be
                 * "qpe6.cmap" not qpe6.0.cmap. However, if you have an hour
                 * with a decimal greater than zero then qpe4.5.cmap would be
                 * valid and the qpeHourToUse would be 4.5
                 */
                if ((dblHour - intHour) > 0) {
                    qpeHourToUse = String.valueOf(dblHour);
                } else {
                    qpeHourToUse = String.valueOf(intHour);
                }

            }
        }

        /*
         * If qpeHourToUse is null then set qpeHourToUse to "". qpeHourToUse
         * will be added to the qpe file name to determine the color map to use.
         */
        if (qpeHourToUse == null) {
            qpeHourToUse = "";
        }

        return qpeHourToUse;
    }

    private void getQpeColorMapFiles() {
        LocalizationFile[] files = ColorMapLoader.listColorMapFiles();
        for (LocalizationFile file : files) {
            String fn = file.getName();
            if (fn.indexOf("ffmp/qpe") > 0) {
                fileArray.add(fn);
            }
        }
    }
}
