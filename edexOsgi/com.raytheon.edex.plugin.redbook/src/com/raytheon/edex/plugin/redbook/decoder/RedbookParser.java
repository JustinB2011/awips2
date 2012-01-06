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
package com.raytheon.edex.plugin.redbook.decoder;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.raytheon.edex.plugin.redbook.common.RedbookRecord;
import com.raytheon.edex.plugin.redbook.common.blocks.ProductIdBlock;
import com.raytheon.edex.plugin.redbook.common.blocks.RedbookBlock;
import com.raytheon.edex.plugin.redbook.common.blocks.RedbookBlockFactory;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.edex.decodertools.time.TimeTools;
import com.raytheon.uf.edex.wmo.message.WMOHeader;

/**
 * The Redbook parser accepts a potential Redbook record and attempts to decode
 * certain identifying data to determine the the data is indeed valid. If the
 * data is determined valid, the date time and product identification
 * information is extracted and used to populate a new RedbookRecord.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20080512           1131 jkorman     Initial implementation.
 * 20080529           1131 jkorman     Added traceId, implemented in logger.
 * 20101022           6424 kshrestha   Added fcsttime
 * 20110516           8296 mhuang      fixed fcsttime problem
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */
public class RedbookParser {

    private final Log logger = LogFactory.getLog(getClass());

    // private Calendar issueDate = null;

    private List<RedbookBlock> redbookDocument;

    private RedbookRecord rRecord;

    private final RedbookBlockFactory blockFactory;

    /**
     * 
     * @param traceId
     * @param data
     * @param hdr
     */
    public RedbookParser(String traceId, byte[] data, WMOHeader hdr) {
        blockFactory = RedbookBlockFactory.getInstance();

        rRecord = internalParse(traceId, data, hdr);
        if (rRecord != null) {

            rRecord.setWmoCCCCdt(hdr.getWmoHeader().substring(6));
            rRecord.setWmoTTAAii(hdr.getWmoHeader().substring(0, 6));

            int day = hdr.getDay();
            int hour = hdr.getHour();
            int min = hdr.getMinute();

            int fcstTime = rRecord.getFcstHours() * 3600;
            
            Calendar wmoTime = TimeTools.copy(rRecord.getTimeObs());

            if (day - wmoTime.get(Calendar.DAY_OF_MONTH) < 0) {
                wmoTime.add(Calendar.MONTH, 1);
            }
            wmoTime.set(Calendar.DAY_OF_MONTH, day);
            wmoTime.set(Calendar.HOUR_OF_DAY, hour);
            wmoTime.set(Calendar.MINUTE, min);

            long binnedTime = getBinnedTime(traceId, hdr, wmoTime.getTimeInMillis());

            DataTime dt = null;
            
            if ( fcstTime > 0 )
                dt = new DataTime(new Date(binnedTime), fcstTime);
            else
            	dt = new DataTime(new Date(binnedTime));
            
            rRecord.setDataTime(dt);

            String cor = hdr.getBBBIndicator();
            if ((cor != null) && (cor.indexOf("CC") >= 0)) {
                rRecord.setCorIndicator(cor);
            }

        }
    }

    /**
     * Return the last decoded Redbook record from this parser.
     * 
     * @return A RedbookRecord.
     */
    public RedbookRecord getDecodedRecord() {
        return rRecord;
    }

    /**
     * 
     * @param hdr 
     * @param separator
     */
    private RedbookRecord internalParse(String traceId, byte[] redbookMsg, WMOHeader hdr) {

        RedbookRecord record = null;

        ByteBuffer dataBuf = ByteBuffer.wrap(redbookMsg);

        redbookDocument = new ArrayList<RedbookBlock>();
        while (dataBuf.hasRemaining()) {

            RedbookBlock currBlock = null;

            try {
                currBlock = blockFactory.getBlock(dataBuf);

                redbookDocument.add(currBlock);

                if (currBlock.isEndBlock()) {
                    int endPos = dataBuf.position();
                    record = new RedbookRecord();
                    byte[] redBookData = new byte[endPos];
                    System.arraycopy(redbookMsg, 0, redBookData, 0, endPos);
                    record.setRedBookData(redBookData);
                    break;
                } else if (currBlock.getMode() == 2 && currBlock.getSubMode() == 3) {
                    /*
                     * Upper air plots are malformed and require special
                     * handling to extract the data. If we get this far, it is
                     * enough.
                     */
                    if (hdr.getTtaaii().substring(0, 4).equals("PYMA")) {
                        record = new RedbookRecord();
                        record.setRedBookData(redbookMsg);
                        break;
                    }
                }
            } catch (BufferUnderflowException bue) {
                logger.error(traceId + "- Out of data");
                return record;
            } catch (Exception e) {
                logger.error(traceId + "- Error in parser", e);
                return record;
            }
        }
        if (record != null) {
            for (RedbookBlock block : redbookDocument) {
                if ((block.getMode() == 1) && (block.getSubMode() == 1)) {
                    ProductIdBlock id = (ProductIdBlock) block;
                    record.setTimeObs(id.getProductFileTime());
                    record.setRetentionHours(id.getRetentionHours());

                    record.setFileId(id.getFileIndicator());
                    record.setProductId(id.getProductId());
                    record.setOriginatorId(id.getOriginatorId());

/*                    record.setFcstHours(id.getFcstHours()); */
                    record.setFcstHours(getForecastTime(traceId, hdr));
                    
                    int fcstTime = this.getForecastTime(traceId, hdr);
                    if (fcstTime == 0)
                      record.setFcstHours(fcstTime); 
                    
                    record.setTraceId(traceId);
                }
            }
        } else {
            logger.info(traceId + "- No EndOfProductBlock found");
        }

        return record;
    }
    
	public int getForecastTime(String traceId, WMOHeader hdr)
    {
     RedbookFcstMap map;
        try {
            map = RedbookFcstMap.load();           
            if (map != null){
            	RedbookFcstMap.MapFcstHr xmlInfo = map.mapping.get(hdr.getTtaaii());
                if (xmlInfo != null && xmlInfo.fcstHR != null && !xmlInfo.fcstHR.isEmpty())
                     return(Integer.parseInt(xmlInfo.fcstHR));
             }
            return 0;
        } catch (Exception e) {
        	logger.error(traceId + " - Error in parser - mappingFCST: ", e);
        }
		return 0;
	}
	
	public long getBinnedTime(String traceId, WMOHeader hdr, long timeMillis) {
        try {
            long period = 43200 * 1000; // default period is 12 hours
            long offset = 0;
            
            RedbookFcstMap map = RedbookFcstMap.load();
            if (map != null) {
                RedbookFcstMap.MapFcstHr xmlInfo = map.mapping.get(hdr
                        .getTtaaii());
                if (xmlInfo != null) {
                    /* Does not support AWIPS 1 semantics of "period < 0 means
                     * apply offset first". 
                     */
                    if (xmlInfo.binPeriod != null && xmlInfo.binPeriod > 0)
                        period = (long) xmlInfo.binPeriod * 1000;
                    if (xmlInfo.binOffset != null)
                        offset = (long) xmlInfo.binOffset * 1000;
                }
            }

            timeMillis = (timeMillis / period) * period + offset;
        } catch (Exception e) {
            logger.error(traceId + " - Error in parser - mappingFCST: ", e);
        }
        return timeMillis;
	}	
	
}