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
package com.raytheon.uf.edex.plugin.vaa.decoder;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.edex.esb.Headers;
import com.raytheon.uf.common.dataplugin.vaa.VAARecord;
import com.raytheon.uf.common.dataplugin.vaa.VAASubPart;
import com.raytheon.uf.common.pointdata.spatial.SurfaceObsLocation;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.edex.wmo.message.WMOHeader;

/**
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 05, 2009 3267       jkorman     Initial creation
 * Aug 30, 2013 2298       rjpeter     Make getPluginName abstract
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */
public class VAAParser implements Iterable<VAARecord> {

    private static class LatLon {
        public Double lat;

        public Double lon;

        @Override
        public String toString() {
            return String.format("[%5.2f %6.2f]", lat, lon);
        }
    }

    public static class VAAShape {
        public List<LatLon> points;

        public String shapeType;
    }

    private final String pluginName;

    private final WMOHeader wmoHeader;

    private final String traceId;

    private final List<VAARecord> records = new ArrayList<VAARecord>();

    private List<InternalReport> reports;

    /**
     * 
     * @param message
     * @param wmoHeader
     * @param pdd
     */
    public VAAParser(String name, byte[] message, String traceId,
            Headers headers) {
        pluginName = name;
        this.traceId = traceId;
        wmoHeader = new WMOHeader(message, headers);
        setData(message, headers);
    }

    /**
     * 
     */
    @Override
    public Iterator<VAARecord> iterator() {
        return records.iterator();
    }

    /**
     * Set the message data and decode all message reports.
     * 
     * @param message
     *            Raw message data.
     * @param traceId
     *            Trace id for this data.
     */
    private void setData(byte[] message, Headers headers) {

        reports = InternalReport.identifyMessage(message, headers);

        VAARecord vaa = new VAARecord();
        vaa.setTraceId(traceId);
        vaa.setWmoHeader(wmoHeader.getWmoHeader());
        String cor = wmoHeader.getBBBIndicator();
        if (cor != null) {
            Matcher m = Pattern.compile("(C[A-Z]{2})( +.*)?").matcher(cor);
            if (m.find()) {
                vaa.setCorIndicator(m.group(1));
            }
        }

        SurfaceObsLocation loc = new SurfaceObsLocation();
        vaa.setLocation(loc);
        for (InternalReport rpt : reports) {
            switch (rpt.getLineType()) {
            case WMO_HEADER: {
                break;
            }
            case MESSAGE: {
                vaa.setMessage(rpt.getReportLine());
            }
            case NO_ID:
            case ADVISORY_LEAD: {
                break;
            }
            case MESSAGE_DTG: {
                DataTime dt = parseDTG(rpt.getReportLine());
                vaa.setDataTime(dt);
                break;
            }
            case VAAC_CNTR: {
                vaa.setCenterId(rpt.getReportLine());
                break;
            }
            case VOLCANO_ID: {
                loc.setStationId(rpt.getReportLine());
                break;
            }
            case VOLCANO_PSN: {
                LatLon ll = parseLatLon(rpt.getReportLine());
                if (ll != null) {
                    loc.assignLocation(ll.lat, ll.lon);
                }
                break;
            }
            case GEO_AREA: {

                break;
            }
            case SUMMIT_ELEV: {
                int elev = parseSummitElev(rpt.getReportLine());
                if (elev > -9999) {
                    loc.setElevation(elev);
                }
                break;
            }
            case ADVISORY_NR: {
                vaa.setAdvisoryNumber(rpt.getReportLine());
                break;
            }
            case INFO_SOURCE: {
                break;
            }
            case ERUPTION_DETAIL: {
                break;
            }
            case OBS_DTG: {
                break;
            }
            case OBS: {
                parseAnalData(rpt, vaa);
                break;
            }
            case FCST: {
                parseFcstData(rpt, vaa);
                break;
            }
            case RMKS:
            case NXT_ADVISORY:
            default: {
            }
            } // switch
        }

        records.add(vaa);
    }

    /**
     * 
     * @param latLon
     * @return
     */
    private LatLon parseLatLon(String latLon) {
        LatLon latlon = null;
        Pattern p = Pattern.compile(InternalReport.LAT_LON_P);
        Matcher m = p.matcher(latLon);
        if (m.find()) {
            latlon = new LatLon();
            latlon.lat = Double.parseDouble(m.group(2));
            latlon.lat += (Double.parseDouble(m.group(3)) / 60.0);
            latlon.lat *= ("S".equals(m.group(1))) ? -1 : 1;

            latlon.lon = Double.parseDouble(m.group(5));
            latlon.lon += (Double.parseDouble(m.group(6)) / 60.0);
            latlon.lon *= ("W".equals(m.group(4))) ? -1 : 1;
        }
        return latlon;
    }

    /**
     * 
     * @param summitElev
     * @return
     */
    private int parseSummitElev(String summitElev) {
        int elevation = -9999;
        Matcher m = Pattern.compile("(\\d+) +FT +\\((\\d+) +[Mm]\\)").matcher(
                summitElev);
        if (m.find()) {
            elevation = Integer.parseInt(m.group(2));
        }
        return elevation;
    }

    /**
     * 
     */
    private DataTime parseDTG(String dtg) {
        DataTime dt = null;

        // 20091104/1708Z
        SimpleDateFormat dtFmt = new SimpleDateFormat("yyyyMMdd/HHmmZ");
        if (dtg != null) {
            Pattern p = Pattern.compile("(\\d{8}/\\d{4})(Z)");
            Matcher m = p.matcher(dtg);
            if (m.find()) {
                if ("Z".equals(m.group(2))) {
                    dtg = m.group(1) + "GMT";
                }
            }
            ParsePosition pos = new ParsePosition(0);
            Date d = dtFmt.parse(dtg, pos);
            if (pos.getErrorIndex() < 0) {
                dt = new DataTime(d);
            }
        }
        return dt;
    }

    /**
     * 
     * @param rpt
     * @param vaa
     * @return
     */
    private void parseAnalData(InternalReport rpt, VAARecord vaa) {
        String rptData = unPack(rpt, false);

        Pattern p = Pattern.compile(InternalReport.ANAL_P);
        Matcher m = p.matcher(rptData);
        if (m.find()) {
            if ("OBS".equals(m.group(1))) {
                vaa.setAnal00Hr(unPack(rpt, true));
            }
        }
        List<VAAShape> features = parseFeature(rptData);
        if ((features != null) && (features.size() > 0)) {
            for (VAAShape feature : features) {
                VAASubPart part = null;
                String type = feature.shapeType;
                if ("LINE".equals(type)) {
                    part = new VAASubPart();
                    part.setShapeType(type);
                    int index = 0;
                    for (LatLon pos : feature.points) {
                        part.addVertex(pos.lat, pos.lon, index++);
                    }
                } else if ("AREA".equals(type)) {
                    part = new VAASubPart();
                    part.setShapeType(type);
                    int index = 0;
                    for (LatLon pos : feature.points) {
                        part.addVertex(pos.lat, pos.lon, index++);
                    }
                }
                if (part != null) {
                    part.setSubText("ANAL00");
                    vaa.addSubPart(part);
                }
            }
        }
    }

    /**
     * 
     * @param rpt
     * @param vaa
     * @return
     */
    private void parseFcstData(InternalReport rpt, VAARecord vaa) {
        String rptData = unPack(rpt, false);

        String fcstPd = null;

        Pattern p = Pattern.compile(InternalReport.FCST_P);
        Matcher m = p.matcher(rptData);
        if (m.find()) {
            if ("FCST".equals(m.group(1))) {
                if ("6".equals(m.group(4))) {
                    vaa.setFcst06Hr(unPack(rpt, true));
                    fcstPd = "FCST06";
                } else if ("12".equals(m.group(4))) {
                    vaa.setFcst12Hr(unPack(rpt, true));
                    fcstPd = "FCST12";
                } else if ("18".equals(m.group(4))) {
                    vaa.setFcst18Hr(unPack(rpt, true));
                    fcstPd = "FCST18";
                }
            }
        }
        List<VAAShape> features = parseFeature(rptData);
        if ((features != null) && (features.size() > 0)) {
            for (VAAShape feature : features) {
                VAASubPart part = null;
                String type = feature.shapeType;
                if ("LINE".equals(type)) {
                    part = new VAASubPart();
                    part.setShapeType(type);
                    int index = 0;
                    for (LatLon pos : feature.points) {
                        part.addVertex(pos.lat, pos.lon, index++);
                    }
                } else if ("AREA".equals(type)) {
                    part = new VAASubPart();
                    part.setShapeType(type);
                    int index = 0;
                    for (LatLon pos : feature.points) {
                        part.addVertex(pos.lat, pos.lon, index++);
                    }
                }
                if (part != null) {
                    part.setSubText(fcstPd);
                    vaa.addSubPart(part);
                }
            }
        }
    }

    /**
     * 
     * @param rptData
     * @return
     */
    private List<VAAShape> parseFeature(String rptData) {
        Pattern latLonP = Pattern.compile(InternalReport.LAT_LON_P);
        Pattern lineP = Pattern.compile("WID +LINE +BTN");
        Pattern areaP = Pattern.compile("  ");

        List<VAAShape> features = new ArrayList<VAAShape>();

        String[] descriptions = rptData.split("SFC/");
        if ((descriptions != null) && (descriptions.length > 1)) {
            for (String description : descriptions) {
                Matcher m = lineP.matcher(description);
                if (m.find()) {
                    // parse as a line
                    m = latLonP.matcher(description);
                    int pos = 0;
                    List<LatLon> points = new ArrayList<LatLon>();
                    while (m.find(pos)) {
                        int start = m.start();
                        int stop = m.end();
                        points.add(parseLatLon(description.substring(start,
                                stop)));
                        pos = stop;
                    }
                    if (points.size() == 2) {
                        VAAShape shape = new VAAShape();
                        shape.shapeType = "LINE";
                        shape.points = points;
                        features.add(shape);
                    }
                } else {
                    // handle as an area
                    m = latLonP.matcher(description);
                    int pos = 0;
                    List<LatLon> points = new ArrayList<LatLon>();
                    while (m.find(pos)) {
                        int start = m.start();
                        int stop = m.end();
                        points.add(parseLatLon(description.substring(start,
                                stop)));
                        pos = stop;
                    }
                    if (points.size() > 3) {
                        VAAShape shape = new VAAShape();
                        shape.shapeType = "AREA";
                        shape.points = points;
                        features.add(shape);
                    }
                }
            }
        }
        return features;
    }

    /**
     * 
     * @param rpt
     * @return
     */
    private String unPack(InternalReport rpt, boolean addLineFeed) {
        StringBuilder sb = new StringBuilder(rpt.getReportLine());
        if (rpt.getSubLines() != null) {
            for (InternalReport r : rpt.getSubLines()) {
                if (addLineFeed) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
                sb.append(r.getReportLine());
            }
        }
        return sb.toString().trim();
    }

    /**
     * 
     * @param args
     */
    public static final void main(String[] args) {

        String msg1 = "\u0001\r\r\n738\r\r\nFVXX20 KNES 041708 CAA"
                + "\r\r\nVA ADVISORY" + "\r\r\nDTG: 20091104/1708Z"
                + "\r\r\nVAAC: WASHINGTON"
                + "\r\r\nVOLCANO: SOUFRIERE HILLS 1600-05"
                + "\r\r\nPSN: N1642 W06210" + "\r\r\nAREA: W_INDIES"
                + "\r\r\nSUMMIT ELEV: 3002 FT (915 M)"
                + "\r\r\nADVISORY NR: 2009/146"
                + "\r\r\nINFO SOURCE: GOES-12. GFS WINDS."
                + "\r\r\nERUPTION DETAILS: CONTINUOUS EMISSIONS"
                + "\r\r\nOBS VA DTG: 04/1645Z"
                + "\r\r\nOBS VA CLD: SFC/FL100 42NM WID LINE BTN N1638"
                + "\r\r\nW06611 - N1643 W06214. MOV W 7KT"
                + "\r\r\nFCST VA CLD +6HR: 04/2300Z SFC/FL100 40NM WID"
                + "\r\r\nLINE BTN N1640 W06614 - N1644 W06214."
                + "\r\r\nFCST VA CLD +12HR: 05/0500Z SFC/FL100 40NM WID"
                + "\r\r\nLINE BTN N1638 W06614 - N1643 W06214. SFC/FL100"
                + "\r\r\n40NM WID LINE BTN N1641 W06616 - N1643 W06214."
                + "\r\r\nFCST VA CLD +18HR: 05/1100Z"
                + "\r\r\nRMK: A SPREADING 42 NMI WIDE ASH PLUME MOVING AT"
                + "\r\r\nA MEASURED 7 KTS EXTENDS AT LEAST 211 NMI TO THE"
                + "\r\r\nWEST OF THE VOLCANO, OR TO ABOUT 66W.  NO"
                + "\r\r\nSIGNIFICANT CHANGE IN DIRECTION OR SPEED IS"
                + "\r\r\nANTICIPATED DURING THE NEXT 12 HOURS. ...BALDWIN"
                + "\r\r\nNXT ADVISORY: WILL BE ISSUED BY 20091104/2315Z"
                + "\r\r\n\u0003";
        Headers headers = new Headers();
        headers.put("ingestFileName", "FVXX20.20110106");
        VAAParser p = new VAAParser("vaa", msg1.getBytes(), "TEST01", headers);
        Iterator<VAARecord> it = p.iterator();
        while (it.hasNext()) {
            VAARecord r = it.next();
            System.out.println(r);
            System.out.println(r.getMessage());
        }

        // Matcher m =
        // Pattern.compile("(\\d+) +FT +\\((\\d+) +[Mm]\\)").matcher("3002 FT (915 M)");
        // if(m.find()) {
        // for(int i = 0;i <= m.groupCount();i++) {
        // System.out.println(m.group(i));
        // }
        // }
    }
    // ashdescription ::= 'SFC' '/' 'FL' digit digit digit ( line | area ) (
    // movement ) .
    //
    // area ::= segments .
    //
    // line ::= digits 'NM' 'WID' 'LINE' 'BTN' segment '.' .
    //
    // movement ::=
    //
    // segments ::= segment ( spaces - spaces latlon ) .
    //
    // segment ::= latlon spaces - spaces latlon.
    //
    // latlon ::= lat spaces lon.
    //
    // lat ::= ['N' | 'S'] digit digit minutes .
    // lon ::= ['E' | 'W'] digit digit digit minutes . // 1(([0-7]\d)|(80))
    //
    // minutes ::= ( '0' | '1' | '2' | '3' | '4' | '5' ) digit .
    //
    // digits ::= digit ( digits ) .
    //
    // digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' .
    //
    // spaces ::= space ( spaces ) .
    //
    // space ::= ' ' .
}
