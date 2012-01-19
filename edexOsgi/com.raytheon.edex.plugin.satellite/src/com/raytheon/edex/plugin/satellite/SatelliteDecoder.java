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

package com.raytheon.edex.plugin.satellite;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.raytheon.edex.exception.DecoderException;
import com.raytheon.edex.plugin.AbstractDecoder;
import com.raytheon.edex.plugin.satellite.dao.SatelliteDao;
import com.raytheon.edex.util.Util;
import com.raytheon.edex.util.satellite.SatSpatialFactory;
import com.raytheon.edex.util.satellite.SatellitePosition;
import com.raytheon.edex.util.satellite.SatelliteUnit;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.satellite.SatMapCoverage;
import com.raytheon.uf.common.dataplugin.satellite.SatelliteRecord;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.edex.decodertools.time.TimeTools;
import com.raytheon.uf.edex.wmo.message.WMOHeader;

/**
 * Decoder implementation for satellite plugin.
 * 
 * <pre>
 * 
 * OFTWARE HISTORY
 *                   
 * ate          Ticket#     Engineer    Description
 * -----------  ----------  ----------- --------------------------
 * 006                      garmenda    Initial Creation
 * /14/2007     139         Phillippe   Modified to follow refactored plugin pattern
 * 8/30/07                  njensen     Added units, commented out data that
 *                                      is currently decoded but not used.    
 * 12/01/07     555         garmendariz Modified decompress method.     
 * 12/06/07     555         garmendariz Modifed start point to remove satellite header
 * Dec 17, 2007 600         bphillip    Added dao pool usage
 * 04Apr2008    1068        MW Fegan    Modified decompression routine to prevent
 *                                       process hang-up.
 * 11/11/2008               chammack    Refactored to be thread safe in camel
 * 02/05/2010   4120        jkorman     Modified removeWmoHeader to handle WMOHeader in
 *                                      various start locations.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class SatelliteDecoder extends AbstractDecoder {

    private String traceId = "";

    private static final int MAX_IMAGE_SIZE = 30000000;

    private static final String SAT_HDR_TT = "TI";

    private SatelliteDao dao;

    public PluginDataObject[] decode(byte[] data) throws Exception {

        PluginDataObject[] retData = null;

        SatelliteRecord record = null;

        if ((data != null) && (data.length > 0)) {
            try {
                data = removeWmoHeader(data);
            } catch (DecoderException e) {
                logger.error(e);
                data = null;
            }
            if (data != null) {
                Calendar calendar = Calendar.getInstance(TimeZone
                        .getTimeZone("GMT"));
                ByteBuffer byteBuffer = null;
                int intValue = 0;
                byte byteValue = 0;
                byte[] tempBytes = null;
                byte[] header = null;
                byte threeBytesArray[] = new byte[3];

                record = new SatelliteRecord();

                if (isCompressed(data)) {
                    byte[][] retVal = decompressSatellite(data);
                    header = retVal[0];
                    data = retVal[1];
                }

                if (header == null) {
                    header = new byte[512];
                    System.arraycopy(data, 0, header, 0, 512);
                    // drop the header from the data in the header
                    byte[] fullTempBytes = new byte[data.length - 512];
                    System.arraycopy(data, 512, fullTempBytes, 0,
                            fullTempBytes.length);
                    int endOfGoodData = getIndex(fullTempBytes, 0);
                    tempBytes = new byte[endOfGoodData];
                    System.arraycopy(fullTempBytes, 0, tempBytes, 0,
                            endOfGoodData);

                } else {
                    tempBytes = data;
                }

                // create a byte buffer
                byteBuffer = ByteBuffer.allocate(512);

                // add the header
                byteBuffer.put(header, 0, 512);

                // get the scanning mode
                int scanMode = byteBuffer.get(37);

                // read the source
                record.setSource(dao.getSource(byteBuffer.get(0))
                        .getSourceName());

                // read the creating entity
                record.setCreatingEntity(dao.getCreatingEntity(
                        byteBuffer.get(1)).getEntityName());

                // read the sector ID
                record.setSectorID(dao.getSectorId(byteBuffer.get(2))
                        .getSectorName());

                // read the physical element
                record.setPhysicalElement(dao.getPhysicalElement(
                        (byteBuffer.get(3))).getElementName());

                // read the units
                SatelliteUnit unit = dao.getUnit(byteBuffer.get(3));
                if (unit != null) {
                    record.setUnits(unit.getUnitName());
                }
                // read the number of records
                record.setNumRecords((int) byteBuffer.getShort(4));

                // read the size of each record
                record.setSizeRecords((int) byteBuffer.getShort(6));

                // read the century
                intValue = 1900 + byteBuffer.get(8);
                calendar.set(Calendar.YEAR, intValue);

                // read the month of the year
                // Calendar months = 0 - 11, so subtract 1
                byteValue = byteBuffer.get(9);
                calendar.set(Calendar.MONTH, byteValue - 1);

                // read the day of the month
                byteValue = byteBuffer.get(10);
                calendar.set(Calendar.DAY_OF_MONTH, byteValue);

                // read the hour of the day
                byteValue = byteBuffer.get(11);
                calendar.set(Calendar.HOUR_OF_DAY, byteValue);

                // read the minute of the hour
                byteValue = byteBuffer.get(12);
                calendar.set(Calendar.MINUTE, byteValue);

                // read the second of the minute
                byteValue = byteBuffer.get(13);
                calendar.set(Calendar.SECOND, byteValue);

                // read the hundredths of a second
                byteValue = byteBuffer.get(14);
                calendar.set(Calendar.MILLISECOND, byteValue * 10);

                record.setDataTime(new DataTime(calendar));

                // read the projection
                byteValue = byteBuffer.get(15);
                int mapProjection = byteBuffer.get(15);

                // get the image resolution
                // imageResolution = (int) byteBuffer.get(41);

                // get the data compression
                // if (byteBuffer.get(42) == 0) {
                // compression = false;
                // }

                // get the version number
                // int pdbVersion = (int) byteBuffer.get(43);

                // get the number of octects in the PDB
                // int pdbBytes = (int) byteBuffer.getShort(44);

                // get the navigation/calibration indicator
                int navCalIndicator = byteBuffer.get(46);

                // Get latitude of satellite sub point
                byteBuffer.position(47);
                byteBuffer.get(threeBytesArray, 0, 3);
                float latSub = transformLatitude(threeBytesArray);

                // Get longitude of satellite sub point
                byteBuffer.position(50);
                byteBuffer.get(threeBytesArray, 0, 3);
                float lonSub = transformLongitude(threeBytesArray);

                // Get the Satellite Height
                int satHeight = byteBuffer.getShort(53);

                if (latSub != 0 || lonSub != 0 || satHeight != 0) {
                    // Correct the longitude so negative is west
                    lonSub *= -1;
                    // Correct the height to be height above ground
                    satHeight = Math.abs(satHeight);

                    record.setSatSubPointLat(latSub);
                    record.setSatSubPointLon(lonSub);
                    record.setSatHeight(satHeight);
                } else {
                    SatellitePosition position = dao
                            .getSatellitePosition(record.getCreatingEntity());
                    if (position == null) {
                        logger
                                .info("Unable to determine geostationary location of ["
                                        + record.getCreatingEntity()
                                        + "].  Zeroing out fields.");
                    } else {
                        record.setSatSubPointLat(position.getLatitude());
                        record.setSatSubPointLon(position.getLongitude());
                        record.setSatHeight(position.getHeight());
                    }
                }

                if (navCalIndicator != 0) {
                    logger.info("Nav/Cal info provided.  Currently unused.");
                }

                // get number of points along x-axis
                int nx = byteBuffer.getShort(16);
                // get number of points along y-axis
                int ny = byteBuffer.getShort(18);

                /*
                 * Rotate image if necessary
                 */

                switch (scanMode) {
                case 1:
                    Util.flipHoriz(tempBytes, ny, nx);
                    break;
                case 2:
                    Util.flipVert(tempBytes, ny, nx);
                    break;
                case 3:
                    Util.flipVert(tempBytes, ny, nx);
                    Util.flipVert(tempBytes, ny, nx);
                    break;
                default:
                    break;
                }

                record.setMessageData(tempBytes);

                // get the latitude of the first point
                byteBuffer.position(20);
                byteBuffer.get(threeBytesArray, 0, 3);
                float la1 = transformLatitude(threeBytesArray);

                // get longitude of the first point
                byteBuffer.position(23);
                byteBuffer.get(threeBytesArray, 0, 3);
                float lo1 = transformLongitude(threeBytesArray);

                // bytes are received with the first bit set to 1 to indicate
                // South
                byteBuffer.position(38);
                byteBuffer.get(threeBytesArray, 0, 3);
                float latin = transformLatitude(threeBytesArray);

                // get the scanning mode
                scanMode = byteBuffer.get(37);

                // Get latitude of upper right hand corner
                byteBuffer.position(55);
                byteBuffer.get(threeBytesArray, 0, 3);
                record.setUpperRightLat(transformLatitude(threeBytesArray));

                // Get longitude of upper right hand corner
                byteBuffer.position(58);
                byteBuffer.get(threeBytesArray, 0, 3);
                record.setUpperRightLon(transformLongitude(threeBytesArray));

                float dx = 0.0f, dy = 0.0f, lov = 0.0f, lo2 = 0.0f, la2 = 0.0f;
                // Do specialized decoding and retrieve spatial data for Lambert
                // Conformal and Polar Stereographic projections
                if ((mapProjection == 3) || (mapProjection == 5)) {
                    byteBuffer.position(30);
                    byteBuffer.get(threeBytesArray, 0, 3);
                    dx = byteArrayToFloat(threeBytesArray) / 10;

                    byteBuffer.position(33);
                    byteBuffer.get(threeBytesArray, 0, 3);
                    dy = byteArrayToFloat(threeBytesArray) / 10;

                    byteBuffer.position(27);
                    byteBuffer.get(threeBytesArray, 0, 3);
                    lov = transformLongitude(threeBytesArray);
                }

                // Do specialized decoding and retrieve spatial data for
                // Mercator
                // projection
                else if (mapProjection == 1) {
                    dx = byteBuffer.getShort(33);
                    dy = byteBuffer.getShort(35);

                    byteBuffer.position(27);
                    byteBuffer.get(threeBytesArray, 0, 3);
                    la2 = transformLatitude(threeBytesArray);

                    byteBuffer.position(30);
                    byteBuffer.get(threeBytesArray, 0, 3);
                    lo2 = transformLongitude(threeBytesArray);

                } else {
                    throw new DecoderException(
                            "Unable to decode GINI Satellite: Encountered Unknown projection");
                }

                SatMapCoverage mapCoverage = null;

                try {
                    mapCoverage = SatSpatialFactory.getInstance()
                            .getMapCoverage(mapProjection, nx, ny, dx, dy, lov,
                                    latin, la1, lo1, la2, lo2);
                } catch (Exception e) {
                    StringBuffer buf = new StringBuffer();
                    buf
                            .append(
                                    "Error getting or constructing SatMapCoverage for values: ")
                            .append("\n\t");
                    buf.append("mapProjection=" + mapProjection).append("\n\t");
                    buf.append("nx=" + nx).append("\n\t");
                    buf.append("ny=" + ny).append("\n\t");
                    buf.append("dx=" + dx).append("\n\t");
                    buf.append("dy=" + dy).append("\n\t");
                    buf.append("lov=" + lov).append("\n\t");
                    buf.append("latin=" + latin).append("\n\t");
                    buf.append("la1=" + la1).append("\n\t");
                    buf.append("lo1=" + lo1).append("\n\t");
                    buf.append("la2=" + la2).append("\n\t");
                    buf.append("lo2=" + lo2).append("\n");
                    throw new DecoderException(buf.toString(), e);
                }

                if (record != null) {
                    record.setTraceId(traceId);
                    record.setCoverage(mapCoverage);
                    record.setPersistenceTime(TimeTools.getSystemCalendar()
                            .getTime());
                    record.setPluginName("satellite");
                    record.constructDataURI();
                }

            }
        }
        if (record == null) {
            retData = new PluginDataObject[0];
        } else {
            retData = new PluginDataObject[] { record };
        }
        return retData;
    }

    /**
     * Verifies that this data is satellite imager and removes the WMO header
     * from the data and extracts the data from the file. Method expects that
     * the input data has been null checked prior to invocation.
     * 
     * @throws DecoderException
     *             If WMO header is not found, or is incorrect.
     * @return The byte array data with all leading information to the end of
     *         the wmo header removed.
     */
    private byte[] removeWmoHeader(byte[] messageData) throws DecoderException {

        int readSize = (messageData.length > 1024) ? 1024 : messageData.length;

        byte[] retMessage = null;

        // Copy to a char [], carefully, as creating a string from
        // a byte [] with binary data can create erroneous data
        char[] message = new char[readSize];
        for (int i = 0; i < message.length; i++) {
            message[i] = (char) (messageData[i] & 0xFF);
        }
        String msgStr = new String(message);
        Matcher matcher = null;
        if (msgStr != null) {
            matcher = Pattern.compile(WMOHeader.WMO_HEADER).matcher(msgStr);
            if (matcher.find()) {
                int headerStart = matcher.start();
                if (SAT_HDR_TT.equals(msgStr.substring(headerStart,
                        headerStart + 2))) {
                    int startOfSatellite = matcher.end();
                    retMessage = new byte[messageData.length - startOfSatellite];
                    System.arraycopy(messageData, startOfSatellite, retMessage,
                            0, retMessage.length);
                } else {
                    throw new DecoderException(
                            "First character of the WMO header must be 'T'");
                }
            } else {
                throw new DecoderException("Cannot decode an empty WMO header");
            }
        } else {
            throw new DecoderException(
                    "Could not create data for WMO header search");
        }

        return retMessage;
    }

    /**
     * Checks to see if the current satellite product is compressed.
     * 
     * @return A boolean indicating if the file is compressed or not
     */
    private boolean isCompressed(byte[] messageData) {
        boolean compressed = true;
        byte[] placeholder = new byte[10];
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(messageData);
            decompressor.inflate(placeholder);
        } catch (DataFormatException e) {
            compressed = false;
        } finally {
            decompressor.end();
        }
        return compressed;
    }

    /**
     * Method to handle compressed satellite data.
     * 
     * @param messageData
     * @return
     * @throws DecoderException
     */
    private byte[][] decompressSatellite(byte[] messageData)
            throws DecoderException {
        byte[] retVal = null;

        boolean firstCall = true;
        byte[] zSatellite = messageData;
        byte[] header = new byte[512];

        int inflatedBytes = 0;
        byte[] inflateArray = new byte[1024 * 10];
        // Allocate 30MB for a possible max size
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MAX_IMAGE_SIZE);
        int totalBytesDecomp = 0;
        int decompByteCounter = 0;
        byte[] inputArray = new byte[1024 * 10];
        Inflater decompressor = new Inflater();
        int index = -1;
        try {
            while (totalBytesDecomp < zSatellite.length) {

                int compChunkSize = zSatellite.length - totalBytesDecomp > 10240 ? 10240
                        : zSatellite.length - totalBytesDecomp;

                // copy compChunkSize compressed data from zSatellite, offset by
                // compByteCounter to inputArray
                System.arraycopy(zSatellite, totalBytesDecomp, inputArray, 0,
                        compChunkSize);

                // set the data to the decompressor
                decompressor.setInput(inputArray, 0, compChunkSize);

                // reset the total bytes decompressed
                inflatedBytes = 0;
                while (!decompressor.finished()) {

                    // inflate the compressed data and get total inflated size
                    inflatedBytes = decompressor.inflate(inflateArray);

                    // check to see if the decompression used the buffer w/o
                    // finishing - this indicates a truncated file
                    if (inflatedBytes == 0) {
                        throw new DecoderException(
                                "Unable to decompress satellite data - input data appears to be truncated");
                    }
                    // add the total bytes decompressed from inflate call
                    decompByteCounter += inflatedBytes;

                    // retrieve the total compressed bytes input so far
                    totalBytesDecomp += decompressor.getTotalIn();

                    // check for first decoded row
                    if (firstCall) {
                        // copy header to a separate array,
                        // start at the end of the WMO header
                        System.arraycopy(inflateArray, 21, header, 0, 512);

                        // reflect the total bytes
                        inflatedBytes = 0;

                        // set sentinel
                        firstCall = false;
                    } else if (totalBytesDecomp == zSatellite.length) {
                        // check for the last decoded row
                        // search for the index
                        index = getIndex(inflateArray, 0);
                    }

                    if (index == -1) {
                        // did not search, or search failed, write all data
                        bos.write(inflateArray, 0, inflatedBytes);
                    } else {
                        // found starting point, writing to it
                        bos.write(inflateArray, 0, index);
                    }

                }

                // reset the decompressor to set additional input
                decompressor.reset();
            }
        } catch (DataFormatException e) {
            throw new DecoderException("Unable to decompress satellite data", e);
        } finally {
            decompressor.end();
            inputArray = null;
            inflateArray = null;
        }

        retVal = bos.toByteArray();
        bos = null;
        return new byte[][] { header, retVal };
    }

    /**
     * Retrieve the starting point for -1,0,-1, 0 in the array. Data after this
     * token is filler and may be eliminated from the buffer.
     * 
     * @param inflateArray
     *            buffer containing inflated data
     * @param startingIndex
     *            buffer point to start the search
     * 
     * @return The index to the invalid data
     */
    private int getIndex(byte[] inflateArray, int startingIndex) {

        int index = -1;
        for (int i = startingIndex; i < inflateArray.length; i++) {
            if (inflateArray[i] == -1) {
                index = i;
                break;
            }

        }

        if (index != -1 && (index + 3 <= inflateArray.length - 1)) {
            if (!(inflateArray[index] == -1 && inflateArray[index + 1] == 0
                    && inflateArray[index + 2] == -1 && inflateArray[index + 3] == 0)) {
                index = getIndex(inflateArray, index + 1);
            }
        } else {
            index = -1;
        }

        return index;

    }

    /**
     * Converts a 3 element byte array into a float
     * 
     * @param b
     *            the byte array
     * @return The byte array represented as a float
     */
    private float byteArrayToFloat(byte[] b) {
        int i = 0;
        i |= b[0] & 0xFF;
        i <<= 8;
        i |= b[1] & 0xFF;
        i <<= 8;
        i |= b[2] & 0xFF;
        return i;
    }

    /**
     * Transforms a latitude in the form of 3 element byte array into the
     * corresponding float value.
     * 
     * @param byteArray
     *            The latitude as a byte array
     * @return The float value of the latitude. A negative return value
     *         indicates south latitude
     */
    private float transformLatitude(byte[] byteArray) {
        float latitude;

        if (byteArray[0] < 0) {
            // remove the negative value
            byteArray[0] &= 127;
            latitude = byteArrayToFloat(byteArray) / 10000 * -1;
        } else {
            latitude = byteArrayToFloat(byteArray) / 10000;
        }
        return latitude;
    }

    /**
     * Transforms a longitude in the form of a 3 element byte array into the
     * corresponding float value
     * 
     * @param byteArray
     *            The longitude as a byte array
     * @return The float value of the longitude. A negative return value
     *         indicates west longitude
     */
    private float transformLongitude(byte[] byteArray) {

        float longitude;
        if (byteArray[0] < 0) {
            // west longitude
            // remove the negative value
            byteArray[0] &= 127;
            longitude = byteArrayToFloat(byteArray);

            if (longitude <= 1800000) {
                longitude *= -1;
            } else {
                longitude = 3600000 - longitude;
            }

        } else {
            // east longitude
            longitude = byteArrayToFloat(byteArray);
            if (longitude > 1800000) {
                longitude = longitude - 3600000;
            }
        }
        return longitude / 10000;
    }

    public SatelliteDao getDao() {
        return dao;
    }

    public void setDao(SatelliteDao dao) {
        this.dao = dao;
    }
}
