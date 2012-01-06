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
package com.raytheon.edex.plugin.radar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.raytheon.edex.esb.Headers;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Decompresses all radar products if necessary
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2010            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RadarDecompressor {

    private static final int Z_DEFLATED = 8;

    private static final int DEF_WBITS = 15;

    /** The logger */
    private static final transient IUFStatusHandler theHandler = UFStatus
            .getHandler(RadarDecompressor.class);

    private static final Pattern WMO_PATTERN = Pattern
            .compile("([A-Z]{4}[0-9]{2} [A-Z]{4} [0-9]{6})\\x0D\\x0D\\x0A(\\w{6})\\x0D\\x0D\\x0A");

    public byte[] decompress(byte[] messageData, Headers headers) {
        byte[] radarData = null;
        try {
            int wmoHeaderSize;
            if (messageData.length < 80) {
                wmoHeaderSize = 0;
            } else {
                // skip the WMO header if any
                String headerSearch = new String(messageData, 0, 80);
                wmoHeaderSize = findStartRadarData(headerSearch);
            }

            if (isCompressed(messageData, wmoHeaderSize)) {
                radarData = decompressRadar(messageData, wmoHeaderSize, headers);
            } else {
                radarData = new byte[messageData.length - wmoHeaderSize];
                System.arraycopy(messageData, wmoHeaderSize, radarData, 0,
                        radarData.length);
            }
        } catch (Exception e) {
            theHandler.handle(Priority.ERROR, "Failed decompression on "
                    + headers.get("ingestfilename"), e);
            // theHandler
            // .handle(Priority.INFO,
            // "File size was : "
            // + new File(headers.get("ingestfilename")
            // .toString()).length()
            // + ", supposed to be : " + radarData.length);
        }

        return radarData;
    }

    /**
     * Checks to see if data at the specified offset of the buffer is the start
     * of a compressed block
     * 
     * @param inBuf
     *            the data buffer
     * @param inOff
     *            the offset into the buffer
     * @return true if data is compressed
     */
    private boolean isCompressed(byte[] inBuf, int inOff) {
        if (inOff == -1) {
            return false;
        }
        int b0 = inBuf[inOff] & 0xFF;
        int b1 = inBuf[inOff + 1] & 0xFF;
        if ((b0 & 0xf) == Z_DEFLATED) {
            if ((b0 >> 4) + 8 <= DEF_WBITS) {
                if ((((b0 << 8) + b1) % 31) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used for things that need to write the data back out to a file
     * 
     * @param messageData
     * @return
     */
    public File decompressToFile(File file, Headers headers) {
        int fileSize = (int) file.length();
        byte[] messageData = new byte[fileSize];
        FileInputStream input = null;

        try {
            input = new FileInputStream(file);
            input.read(messageData);
        } catch (FileNotFoundException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        } catch (IOException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    theHandler.handle(Priority.ERROR, e.getMessage());
                }
            }
        }

        String headerSearch = "";
        int start = 0;
        if (messageData.length < 80) {
        } else {
            // skip the WMO header if any
            headerSearch = new String(messageData, 0, 80);
            start = findStartRadarData(headerSearch);
            headerSearch = headerSearch.substring(0, start);
        }

        messageData = decompress(messageData, headers);

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        }
        try {
            output.write(headerSearch.getBytes());
            output.write(messageData);
            output.close();
        } catch (IOException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        }
        return file;
    }

    /**
     * Used for things that need to write the data back out to a file, without a
     * header. Same as decompressToFile, but will strip the header off before
     * writing it back out.
     * 
     * @param messageData
     * @return
     */
    public File decompressToFileWithoutHeader(File file, Headers headers) {
        int fileSize = (int) file.length();
        byte[] messageData = new byte[fileSize];
        FileInputStream input = null;

        try {
            input = new FileInputStream(file);
            input.read(messageData);
        } catch (FileNotFoundException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        } catch (IOException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    theHandler.handle(Priority.ERROR, e.getMessage());
                }
            }
        }

        String headerSearch = "";
        int start = 0;
        if (messageData.length < 80) {
        } else {
            // skip the WMO header if any
            headerSearch = new String(messageData, 0, 80);
            start = findStartRadarData(headerSearch);
            headerSearch = headerSearch.substring(0, start);
        }

        messageData = decompress(messageData, headers);

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        }
        try {
            output.write(messageData);
            output.close();
        } catch (IOException e) {
            theHandler.handle(Priority.ERROR, e.getMessage());
        }
        return file;
    }

    private int findStartRadarData(String headerInfo) {
        int startOfRadarData = 0;
        Matcher matcher = WMO_PATTERN.matcher(headerInfo);
        boolean foundHeader = matcher.find();
        if (foundHeader) {
            startOfRadarData = matcher.end();
        }

        return startOfRadarData;
    }

    /**
     * Method to handle compressed radar data. If data is not compressed it is
     * just copied to the output buffer.
     * 
     * @return The decompressed byte array for the radar data
     */
    private byte[] decompressRadar(byte[] inBuf, int offset, Headers headers) {
        // do 4000 bytes at a time
        byte[] outBuf = new byte[4000];
        int inOff = offset;
        int outOff = 0;
        Inflater decompressor = new Inflater();
        int decompressedLen = 0;
        try {
            do {
                if (inBuf.length - inOff <= 0) {
                    theHandler
                            .handle(Priority.ERROR,
                                    "An error occurred.  Apparently, the radar product expects more data.  Aborting decompress.");
                    break;
                }

                if (isCompressed(inBuf, inOff)) {
                    decompressor.reset();
                    decompressor.setInput(inBuf, inOff, inBuf.length - inOff);
                    decompressor
                            .inflate(outBuf, outOff, outBuf.length - outOff);
                    inOff += decompressor.getTotalIn();
                    outOff += decompressor.getTotalOut();
                } else {
                    int len = inBuf.length - inOff;
                    len = Math.min(len, outBuf.length - outOff);
                    System.arraycopy(inBuf, inOff, outBuf, outOff, len);
                    inOff += len;
                    outOff += len;
                }

                if (decompressedLen == 0) {
                    decompressedLen = (outBuf[62] << 24 & 0xFF000000)
                            | (outBuf[63] << 16 & 0x00FF0000)
                            | (outBuf[64] << 8 & 0x0000FF00)
                            | (outBuf[65] & 0x000000FF);
                    byte[] tmpBuf = outBuf;
                    outBuf = new byte[decompressedLen];
                    outOff = Math.min(decompressedLen, outOff - 54);
                    System.arraycopy(tmpBuf, 54, outBuf, 0, outOff);
                }
            } while (outOff < outBuf.length);

        } catch (DataFormatException e) {
            theHandler.handle(Priority.ERROR,
                    "Invalid data format encountered during decompression on "
                            + headers.get("ingestfilename"), e);
        } finally {
            decompressor.end();
        }

        return outBuf;
    }
}
