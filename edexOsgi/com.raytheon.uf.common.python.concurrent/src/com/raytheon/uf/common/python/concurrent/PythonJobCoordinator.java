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
package com.raytheon.uf.common.python.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.raytheon.uf.common.python.PythonInterpreter;

/**
 * Interface to get to the {@link ExecutorService}. Allows multiple thread pools
 * to be created in a single JVM, by passing in a different application name.
 * 
 * This class will be used in this way:
 * 
 * 
 * <pre>
 * 
 *       AbstractPythonScriptFactory<PythonInterpreter, Object> factory = new CAVEPythonFactory();
 *       PythonJobCoordinator coordinator = PythonJobCoordinator
 *               .newInstance(factory);
 *       IPythonExecutor<PythonInterpreter, Object> executor = new CAVEExecutor(
 *               args);
 *       try {
 *           coordinator.submitJob(executor, listener);
 *       } catch (Exception e) {
 *           e.printStackTrace();
 *       }
 * 
 * }
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 31, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class PythonJobCoordinator<P extends PythonInterpreter> {

    private ExecutorService execService = null;

    private ThreadLocal<P> threadLocal = null;

    private static Map<String, PythonJobCoordinator<? extends PythonInterpreter>> pools = new ConcurrentHashMap<String, PythonJobCoordinator<? extends PythonInterpreter>>();

    private PythonJobCoordinator(final AbstractPythonScriptFactory<P> factory) {
        execService = Executors.newFixedThreadPool(factory.getMaxThreads(),
                new PythonThreadFactory(factory.getName()));
        threadLocal = new ThreadLocal<P>() {
            protected P initialValue() {
                return factory.createPythonScript();
            };
        };
    }

    /**
     * Gets the instance by name, or throw a {@link RuntimeException}.
     * 
     * @param name
     * @return
     */
    public static PythonJobCoordinator<? extends PythonInterpreter> getInstance(
            String name) {
        synchronized (pools) {
            if (pools.containsKey(name)) {
                return pools.get(name);
            } else {
                throw new RuntimeException(
                        "Unable to find instance of PythonJobCoordinator named "
                                + name
                                + ", please call newInstance(AbstractPythonScriptFactory)");
            }
        }
    }

    /**
     * Creates a new instance of this class for a new application. If the same
     * name already exists, it assumes that it is the same application and
     * returns the existing instance.
     * 
     * @param name
     * @param numThreads
     * @return
     */
    public static <S extends PythonInterpreter> PythonJobCoordinator<S> newInstance(
            AbstractPythonScriptFactory<S> factory) {
        synchronized (pools) {
            if (pools.containsKey(factory.getName())) {
                return (PythonJobCoordinator<S>) pools.get(factory.getName());
            } else {
                PythonJobCoordinator<S> pool = new PythonJobCoordinator<S>(
                        factory);
                pools.put(factory.getName(), pool);
                return pool;
            }
        }
    }

    /**
     * Submits a job to the {@link ExecutorService}.
     * 
     * @param callable
     * @return
     * @throws Exception
     */
    public <R> void submitJob(IPythonExecutor<P, R> executor,
            IPythonJobListener<R> listener, Object... args) throws Exception {
        // submit job
        PythonJob<P, R> job = new PythonJob<P, R>(executor, listener,
                threadLocal, args);
        execService.submit(job);
    }

    /**
     * This function should take the {@link PythonInterpreter} on each thread in
     * the thread pool and dispose of it and then shutdown the
     * {@link ExecutorService}
     * 
     * @param name
     */
    public void shutdownCoordinator(String name) {
        /*
         * TODO need to add for future functionality
         */
    }

    /**
     * This function should cancel any listeners for a certain task and then
     * remove those corresponding tasks off of the queue to be ran. It should
     * NOT try to cancel any running python interpreters.
     * 
     * @param name
     */
    public void shutdownTask(String name) {
        /*
         * TODO need to add for future functionality
         */
    }
}
