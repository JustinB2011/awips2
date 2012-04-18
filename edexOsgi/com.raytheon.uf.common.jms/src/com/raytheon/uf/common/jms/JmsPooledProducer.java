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
package com.raytheon.uf.common.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

import com.raytheon.uf.common.jms.wrapper.JmsProducerWrapper;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Synchronization Principle To prevent deadlocks: Chained sync blocks can only
 * happen in a doward direction. A manager has a synchonized lock can make a
 * call down to a wrapper, but not nice versa. Also a session inside a sync
 * block can make a call down to a producer but not vice versa.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 18, 2011            rjpeter     Initial creation
 * Mar 08, 2012   194   njensen   Improved logging
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */

public class JmsPooledProducer {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(JmsPooledProducer.class);

    private final JmsPooledSession sess;

    private final Destination destination;

    private MessageProducer producer;

    private final String destKey;

    private boolean exceptionOccurred = false;

    private Object stateLock = new Object();

    private State state = State.InUse;

    /**
     * Technically a pooled producer should only have 1 reference at a time.
     * Bullet proofing in case 3rd party ever tries to get multiple producers to
     * the same destination.
     */
    private List<JmsProducerWrapper> references = new ArrayList<JmsProducerWrapper>(
            1);

    public JmsPooledProducer(JmsPooledSession sess, String destKey,
            Destination destination) {
        this.sess = sess;
        this.destKey = destKey;
        this.destination = destination;
    }

    public String getDestKey() {
        return destKey;
    }

    public boolean isValid() {
        return isValid(State.Closed, false);
    }

    /**
     * Verifies if an exception has occurred, the state is the desired state,
     * and the underlying resource is still valid.
     * 
     * @param requiredState
     * @param mustBeRequiredState
     *            If true, current state must match requiredState for isValid to
     *            be true. If false, current state must not be the
     *            requiredState.
     * @return
     */
    public boolean isValid(State requiredState, boolean mustBeRequiredState) {
        boolean valid = false;
        if (!exceptionOccurred) {
            valid = state.equals(requiredState);
            if (!mustBeRequiredState) {
                valid = !valid;
            }

            if (valid) {
                // check underlying resource
                try {
                    if (producer != null) {
                        producer.getDeliveryMode();
                    }
                } catch (JMSException e) {
                    // underlying producer has been closed
                    valid = false;
                }
            }
        }
        return valid;
    }

    public boolean isExceptionOccurred() {
        return exceptionOccurred;
    }

    public void setExceptionOccurred(boolean exceptionOccurred) {
        this.exceptionOccurred = exceptionOccurred;
    }

    /**
     * Close down this pooled producer, closes the internal producer reference,
     * and removes from session pool.
     */
    public void close() {
        boolean close = false;

        // only thing in sync block is setting close to prevent dead locking
        // between manager and wrapper, general design principal on sync blocks
        // is chained blocks only in a downward direction (i.e. a
        synchronized (stateLock) {
            if (!State.Closed.equals(state)) {
                state = State.Closed;
                close = true;

                for (JmsProducerWrapper wrapper : references) {
                    wrapper.closeInternal();
                }

                references.clear();
            }
        }

        if (close) {
            if (producer != null) {
                try {
                    statusHandler.info("Closing producer " + producer); // njensen
                    producer.close();
                } catch (Throwable e) {
                    statusHandler.handle(Priority.INFO,
                            "Failed to close producer", e);
                }
                producer = null;
            }

            sess.removeProducerFromPool(this);
        }
    }

    public JmsProducerWrapper createReference() {
        synchronized (stateLock) {
            if (isValid(State.InUse, true)) {
                JmsProducerWrapper wrapper = new JmsProducerWrapper(this);
                references.add(wrapper);
                return wrapper;
            }
        }

        return null;
    }

    public void removeReference(JmsProducerWrapper wrapper) {
        boolean returnToPool = false;
        synchronized (stateLock) {
            if (references.remove(wrapper) && references.isEmpty()
                    && State.InUse.equals(state)) {
                state = State.Available;
                returnToPool = true;
            }
        }

        boolean valid = isValid();
        if (valid && returnToPool) {
            valid = sess.returnProducerToPool(this);
        }

        if (!valid) {
            close();
        }
    }

    public MessageProducer getProducer() throws JMSException {
        // TODO: allow this to automatically grab a new producer if the current
        // one fails, try up to 3 times so that we don't always drop messages on
        // a single failure
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    producer = sess.getSession().createProducer(destination);
                }
            }
        }

        return producer;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Object getStateLock() {
        return stateLock;
    }
}
