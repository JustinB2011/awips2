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
package com.raytheon.hprof.data.heap.dump;

import com.raytheon.hprof.BigByteBuffer;
import com.raytheon.hprof.Id;
import com.raytheon.hprof.data.HeapDumpRecord;

/**
 * 
 * Base class for all the types of data dumped into a {@link HeapDumpRecord}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jan 08, 2014  2648     bsteffen    Initial doc
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class AbstractDump {

    protected final Id id;

    protected final int stackSerialNumber;

    protected AbstractDump(BigByteBuffer buffer, int idSize) {
        id = new Id(buffer, idSize);
        stackSerialNumber = buffer.getInt();
    }

    public Id getId() {
        return id;
    }

}
