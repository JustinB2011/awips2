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
package com.raytheon.uf.edex.services.textdbimpl;

import static com.raytheon.edex.textdb.dbapi.impl.TextDB.asciiToHex;
import static com.raytheon.edex.textdb.dbapi.impl.TextDB.getProperty;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.raytheon.uf.common.message.Header;
import com.raytheon.uf.common.message.Message;
import com.raytheon.uf.common.message.Property;
import com.raytheon.uf.edex.services.textdbsrv.ICommandExecutor;
import com.raytheon.uf.edex.services.textdbsrv.TextDBSrvCommandTags;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2008        1538 jkorman     Initial creation
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */

public class CommandExecutor implements ICommandExecutor {

    private Log logger = LogFactory.getLog(getClass());

    private static final TextDBSrvCommandTags VIEW_CMD = TextDBSrvCommandTags.VIEW;

    private Map<String, ICommandExecutor> execMap = new HashMap<String, ICommandExecutor>();

    private String cmdView = null;

    /**
     * 
     */
    public CommandExecutor() {
        execMap.put(StateTableAdapter.getViewTag(), new StateTableAdapter());
        execMap.put(WarnTableAdapter.getViewTag(), new WarnTableAdapter());
        execMap.put(VersionsAdapter.getViewTag(), new VersionsAdapter());
        execMap.put(TextViewAdapter.getViewTag(), new TextViewAdapter());
    }

    /**
     * 
     * @param cmdMessage
     * @return
     */
    public Message execute(Message cmdMessage) {
        return getExecutor(cmdMessage).execute(cmdMessage);
    }

    /**
     * Dispose of any local resources before discarding this instance.
     */
    public void dispose() {

        for (ICommandExecutor e : execMap.values()) {
            e.dispose();
        }
        execMap.clear();
    }

    /**
     * 
     * @param message
     * @return
     */
    private String getView(Message message) {
        String cmdView = null;
        if (message != null) {
            Header header = message.getHeader();
            if (header != null) {
                cmdView = getProperty(header, VIEW_CMD.name());
            }
        }
        return cmdView;
    }

    /**
     * 
     * @param message
     * @return
     */
    private ICommandExecutor getExecutor(Message message) {
        ICommandExecutor execCmd = execMap.get(getView(message));
        if (execCmd == null) {
            execCmd = new ICommandExecutor() {
                @Override
                public void dispose() {
                }

                @Override
                public Message execute(Message cmdMessage) {
                    return createErrorMessage("ERROR:Unknown view [" + cmdView
                            + "]");
                }
            };
        }
        return execCmd;
    }

    /**
     * Create an error message for return to a client.
     * 
     * @param error
     *            An error message to include in a return Message.
     * @return A Message instance containing the error text.
     */
    public static final Message createErrorMessage(String error) {
        Message msg = new Message();
        Header h = new Header();
        h.setProperties(new Property[] { new Property("STDERR",
                asciiToHex(error)), });

        msg.setHeader(h);

        return msg;
    }

}
