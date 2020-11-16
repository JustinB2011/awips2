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
package com.raytheon.viz.radar.ui.xy;

import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.common.dataplugin.radar.RadarRecord;
import com.raytheon.uf.common.dataplugin.radar.level3.GSMBlock.GSMMessage;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.DrawableLine;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.TextStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.drawables.IFont;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ImagingCapability;
import com.raytheon.viz.awipstools.capabilities.RangeRingsOverlayCapability;
import com.raytheon.viz.radar.RadarHelper;
import com.raytheon.viz.radar.interrogators.IRadarInterrogator;
import com.raytheon.viz.radar.rsc.AbstractRadarResource;
import com.raytheon.viz.radar.rsc.RadarResourceData;

/**
 * For the General Status Message product
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 13, 2010            mnash     Initial creation
 * 03/01/2013   DR 15496   zwang     Handled expanded GSM, display more status
 * 07/16/2014   DR 17214   zwang     Handled B15 GSM change about super res flag
 * 07/24/2014   #3429      mapeters  Updated deprecated drawLine() calls.
 * 07/29/2014   #3465      mapeters  Updated deprecated drawString() calls.
 * 06/09/2016   DR 17748   jdynina   Ignore SAILS cuts for super-res display
 * 11/28/2017   DR 16763   jdynina   Acknowledge SPG for OP Mode
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RadarGSMResource extends AbstractRadarResource<RadarXYDescriptor> {

    public RGB color;

    /**
     * @param resourceData
     * @param loadProperties
     * @param interrogator
     * @throws VizException
     */
    public RadarGSMResource(RadarResourceData resourceData,
            LoadProperties loadProperties, IRadarInterrogator interrogator)
            throws VizException {
        super(resourceData, loadProperties, interrogator);
        getCapability(ColorableCapability.class).setColor(new RGB(0, 255, 0));

        // remove the unneeded capabilities
        getCapabilities().removeCapability(ImagingCapability.class);
        getCapabilities().removeCapability(RangeRingsOverlayCapability.class);

        color = getCapability(ColorableCapability.class).getColor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.radar.ui.xy.RadarXYResource#paintInternal(com.raytheon
     * .uf.viz.core.IGraphicsTarget,
     * com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        super.paintInternal(target, paintProps);
        color = getCapability(ColorableCapability.class).getColor();
        RadarRecord record = getRadarRecord(paintProps.getDataTime());
        if (record == null) {
            return;
        }
        GSMMessage message = record.getGsmMessage();
        if (message != null) {
            String rpgNarrow = "";
            String dedicatedComms = "";
            String rdaOpStatusString = "";
            String rdaAvailString = "";
            String rdaStatusString = "";
            int xOffset = 15;
            int yOffset = 30;
            int lineSpace = 25;
            int boxHeight = 120;
            int halfHeight = boxHeight / 2;
            boolean rdaDown = false, rpgDown = false;
            String temp = new String();
            descriptor.pixelToWorld(new double[] { 700, 700 });
            String title = "NEXRAD UNIT STATUS";
            String rpg_spg = "RPG";
            String rda_tdwr = "RDA";
            int vcp = message.getVolumeCoveragePattern();

            rdaStatusString = RadarHelper.formatBits(
                    (short) message.getRdaStatus(), RadarHelper.rdaStatusStr);

            if (vcp == 80 || vcp == 90) {
                title = "TDWR SPG UNIT STATUS";
                rpg_spg = "SPG";
                rda_tdwr = "TDWR";
            }

            DrawableString string = new DrawableString(title, color);
            string.setCoordinates(400, yOffset);
            string.addTextStyle(TextStyle.BOXED);
            string.horizontalAlignment = HorizontalAlignment.CENTER;
            string.verticalAlignment = VerticalAlignment.TOP;
            target.drawStrings(string);

            yOffset += 40;
            switch (message.getMode()) {
            case 0:
                temp = "Maintenance Mode";
                break;
            case 1:
                temp = "Clear Air Mode";
                if (vcp == 80) {
                    temp = "Hazardous Mode (clear air)";
                } else if (vcp ==90) {
                    temp = "Monitor Mode (clear air)";
                }
                break;
            case 2:
                temp = "Precipitation/Severe Weather Mode";
                if (vcp == 80) {
                    temp = "Hazardous Mode";
                } else if (vcp ==90) {
                    temp = "Monitor Mode";
                }
                break;
            }
            drawNexradString(
                    "Op Mode/VCP = " + temp + "/VCP"
                            + message.getVolumeCoveragePattern(), xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;
            drawNexradString(
                    "VCP Supplemental Info = "
                            + RadarHelper.formatBits(
                              (short) message.getVcpInfo(),
                              RadarHelper.vcpInfoStr), xOffset, yOffset,
                              target, color);
            yOffset += lineSpace;
            rdaAvailString = RadarHelper.formatBits(
                    (short) message.getProductAvail(),
                    RadarHelper.productAvailStr);
            drawNexradString("New Prod Status = " + rdaAvailString, xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;
            drawNexradString(
                    "Base Data = "
                            + RadarHelper.formatBits(
                                    (short) message.getDataTransmissionEnable(),
                                    RadarHelper.dteStr), xOffset, yOffset,
                    target, color);
            
            yOffset += lineSpace;
            if (message.getCmdStatus() > 0)
                drawNexradString("CMD = Enabled", xOffset, yOffset, target,
                        color);
            else
                drawNexradString("CMD = Disabled", xOffset, yOffset, target,
                        color);
            yOffset += lineSpace;
            if ("Operate".equals(RadarHelper.formatBits(
                    (short) message.getRpgStatus(), RadarHelper.rpgStatusStr))) {
                dedicatedComms = "Connected";
            } else {
                dedicatedComms = "Disconnected";
            }
            drawNexradString("Ded RPG Comms = " + dedicatedComms, xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;
            drawNexradString(
                    "RPG Avail = "
                            + RadarHelper.formatBits(
                                    (short) message.getRpgOperability(),
                                    RadarHelper.rpgOpStr), xOffset, yOffset,
                    target, color);
            yOffset += lineSpace;
            rpgNarrow = RadarHelper.formatBits(
                    (short) message.getRpgNarrowbandStatus(),
                    RadarHelper.rpgNarrowbandStatus);
            if ("".equals(rpgNarrow)) {
                rpgNarrow = "Normal";
            }
            drawNexradString("RPG Narrowband = " + rpgNarrow, xOffset, yOffset,
                    target, color);
            yOffset += lineSpace;

            drawNexradString(
                    "RPG Software = "
                            + RadarHelper.formatBits(
                                    (short) message.getRpgStatus(),
                                    RadarHelper.rpgStatusStr), xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;

            DecimalFormat df = new DecimalFormat("0.00");
            final int DUAL_POL_FLAG = 0x20;
            if ((message.getDataTransmissionEnable() & DUAL_POL_FLAG) != 0)
                drawNexradString(
                        "Delta Sys Cal: "
                                + df.format(message.getReflectCalibCorr())
                                + "H, "
                                + df.format(message.getReflectCalibCorr2())
                                + "V", xOffset, yOffset, target, color);
            else
                drawNexradString(
                        "Delta Sys Cal: "
                                + df.format(message.getReflectCalibCorr()),
                        xOffset, yOffset, target, color);
            yOffset += lineSpace;

            drawNexradString(
                    "RPG Alarm = "
                            + RadarHelper.formatBits(
                                    (short) message.getRpgAlarms(),
                                    RadarHelper.rpgAlarmStr), xOffset, yOffset,
                    target, color);
            yOffset += lineSpace;

            drawNexradString(
                    "RDA Avail = "
                            + RadarHelper.formatBits(
                                    (short) message.getOperabilityStatus(),
                                    RadarHelper.rdaOpStatusStr), xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;
            rdaOpStatusString = RadarHelper.formatBits(
                    (short) message.getRdaStatus(), RadarHelper.rdaStatusStr);
            drawNexradString("RDA Software = " + rdaOpStatusString, xOffset,
                    yOffset, target, color);
            yOffset += lineSpace;
            temp = RadarHelper.formatBits((short) message.getRdaAlarms(),
                    RadarHelper.rdaAlarmStr);
            if ("".equals(temp)) {
                temp = "No Alarms";
            }

            drawNexradString("RDA Alarm = " + temp, xOffset, yOffset, target,
                    color);
            yOffset += lineSpace;
            drawNexradString(
                    "RDA Version = "
                            + String.valueOf((double) message.getRdaBuildNum() / 10),
                    xOffset, yOffset, target, color);
            yOffset += lineSpace;
            if (vcp == 80 || vcp == 90)
                drawNexradString(
                        "SPG Version = "
                                + String.valueOf((double) message
                                        .getBuildVersion() / 10), xOffset,
                        yOffset, target, color);
            else {
                drawNexradString(
                        "RDA Channel = "
                                + RadarHelper.getRdaChannelName(message
                                        .getRdaChannelNum()), xOffset, yOffset,
                        target, color);
                yOffset += lineSpace;
                drawNexradString(
                        "RPG Version = "
                                + String.valueOf((double) message
                                        .getBuildVersion() / 10), xOffset,
                        yOffset, target, color);
            }

            // Plot elevations and filter out SAILS cuts
            double[] elev = message.getElevation().clone();
            int superResFlags = message.getSuperResolutionCuts();

            class ElevCompare implements Comparator<Pair<Double, Boolean>> {
                public int compare (Pair<Double, Boolean> a, Pair<Double, Boolean> b) {
                    return Double.compare(b.getFirst(), a.getFirst());
                }
            }

            ElevCompare elevCompare = new ElevCompare();
            TreeSet<Pair<Double, Boolean>> flaggedElevations = new TreeSet<>(elevCompare);

            for (int i = 0; i < elev.length; ++i) {
                if (elev[i] != 0 && (i == 0 || elev[i] != elev[0])) {
                    flaggedElevations.add(new Pair<>(elev[i],
                            (superResFlags & (1 << i)) != 0));
                }
            }

            List<String> theTemp = new ArrayList<>();
            for (Pair<Double, Boolean> e : flaggedElevations) {
                theTemp.add(Double.toString(e.getFirst() / 10) + "     " +
                        (e.getSecond() ? "S" : ""));
            }

            int height = 780;
            List<DrawableLine> lines = new ArrayList<>(
                    theTemp.size() + 8);
            for (int i = 0; i < theTemp.size(); i++) {
                DrawableLine line = new DrawableLine();
                line.setCoordinates(xOffset + 50, height);
                line.addPoint(800, height - i * lineSpace);
                line.basics.color = color;
                lines.add(line);
                drawNexradString(
                        String.valueOf(theTemp.get(theTemp.size() - 1 - i)),
                        800, height - i * lineSpace - 10, target, color);
            }
            yOffset = height - theTemp.size() * lineSpace - lineSpace;
            drawNexradString("Elevations", 780, yOffset, target, color);

            yOffset = height + lineSpace;
            // first box
            DrawableLine box1 = new DrawableLine();
            box1.setCoordinates(xOffset, yOffset);
            box1.addPoint(xOffset + 200, yOffset);
            box1.addPoint(xOffset + 200, yOffset + boxHeight);
            box1.addPoint(xOffset, yOffset + boxHeight);
            box1.addPoint(xOffset, yOffset);
            box1.basics.color = color;;
            lines.add(box1);
            drawNexradString(rda_tdwr, xOffset + 85, yOffset + halfHeight,
                    target, color);

            final int RDA_OP_STATUS_WIDEBAND_DISCONNECT = 1 << 7;
            final int RDA_STATUS_STANDBY = 1 << 2;
            final int RDA_STATUS_RESTART = 1 << 3;
            final int RDA_STATUS_OFFLINE = 1 << 6;
            if ((message.getOperabilityStatus() & RDA_OP_STATUS_WIDEBAND_DISCONNECT) != 0
                    || (message.getRdaStatus() & RDA_STATUS_RESTART) != 0
                    || (message.getRdaStatus() & RDA_STATUS_STANDBY) != 0
                    || (message.getRdaStatus() & RDA_STATUS_OFFLINE) != 0)
                rdaDown = true;
            if (!rdaDown) {
                DrawableLine arrow1line = new DrawableLine();
                arrow1line.setCoordinates(xOffset + 200, yOffset + halfHeight);
                arrow1line.addPoint(xOffset + 300, yOffset + halfHeight);
                arrow1line.basics.color = color;
                DrawableLine arrow1head = new DrawableLine();
                arrow1head.setCoordinates(xOffset + 280, yOffset + halfHeight - 10);
                arrow1head.addPoint(xOffset + 300, yOffset + halfHeight);
                arrow1head.addPoint(xOffset + 280, yOffset + halfHeight + 10);
                arrow1head.basics.color = color;
                lines.add(arrow1line);
                lines.add(arrow1head);
            }
            xOffset += 300;
            // second box
            DrawableLine box2 = new DrawableLine();
            box2.setCoordinates(xOffset, yOffset);
            box2.addPoint(xOffset + 200, yOffset);
            box2.addPoint(xOffset + 200, yOffset + boxHeight);
            box2.addPoint(xOffset, yOffset + boxHeight);
            box2.addPoint(xOffset, yOffset);
            box2.basics.color = color;;
            lines.add(box2);
            
            drawNexradString(rpg_spg, xOffset + 85, yOffset + halfHeight,
                    target, color);

            if (rpgNarrow.equals("Commanded Disconnect")
                    || dedicatedComms.equals("Disconnected"))
                rpgDown = true;
            if (!rpgDown) {
                DrawableLine arrow2line = new DrawableLine();
                arrow2line.setCoordinates(xOffset + 200, yOffset + halfHeight);
                arrow2line.addPoint(xOffset + 300, yOffset + halfHeight);
                arrow2line.basics.color = color;
                DrawableLine arrow2head1 = new DrawableLine();
                arrow2head1.setCoordinates(xOffset + 280, yOffset + halfHeight - 10);
                arrow2head1.addPoint(xOffset + 300, yOffset + halfHeight);
                arrow2head1.addPoint(xOffset + 280, yOffset + halfHeight + 10);
                arrow2head1.basics.color = color;
                DrawableLine arrow2head2 = new DrawableLine();
                arrow2head2.setCoordinates(xOffset + 220, yOffset + halfHeight - 10);
                arrow2head2.addPoint(xOffset + 200, yOffset + halfHeight);
                arrow2head2.addPoint(xOffset + 220, yOffset + halfHeight + 10);
                arrow2head2.basics.color = color;
                lines.add(arrow2line);
                lines.add(arrow2head1);
                lines.add(arrow2head2);
            }
            xOffset += 300;
            // third box
            DrawableLine box3 = new DrawableLine();
            box3.setCoordinates(xOffset, yOffset);
            box3.addPoint(xOffset + 200, yOffset);
            box3.addPoint(xOffset + 200, yOffset + boxHeight);
            box3.addPoint(xOffset, yOffset + boxHeight);
            box3.addPoint(xOffset, yOffset);
            box3.basics.color = color;;
            lines.add(box3);
            
            target.drawLine(lines.toArray(new DrawableLine[0]));

            drawNexradString("WFO", xOffset + 85, yOffset + 58, target, color);
        }
    }

    /**
     * Convenience function to draw strings for the nexrad product
     * 
     * @param text
     * @param xOffset
     * @param yOffset
     * @param target
     * @throws VizException
     */
    public static void drawNexradString(String text, int xOffset, int yOffset,
            IGraphicsTarget target, RGB color) throws VizException {
        DrawableString string = new DrawableString(text, color);
        string.font = (IFont) Font.getFont("serif");
        string.setCoordinates(xOffset, yOffset);
        string.verticalAlignment = VerticalAlignment.TOP;
        target.drawStrings(string);
    }
}
