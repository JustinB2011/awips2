/**
 * This software was developed and / or mimport com.raytheon.hprof.BigByteBuffer;
to Contract DG133W-05-CQ-1067 with the US Government.
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
package com.raytheon.hprof.data.heap.root;

import com.raytheon.hprof.BigByteBuffer;

/**
 * 
 * GC Root for a JNI Local.
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
public class RootJNILocal extends AbstractRunningRoot {

    public static final int TAG = 0x02;

    private final int frameNumberInStack;

    public RootJNILocal(BigByteBuffer buffer, int idSize) {
        super(buffer, idSize);
        frameNumberInStack = buffer.getInt();
    }

    public int getFrameNumberInStack() {
        return frameNumberInStack;
    }

}
