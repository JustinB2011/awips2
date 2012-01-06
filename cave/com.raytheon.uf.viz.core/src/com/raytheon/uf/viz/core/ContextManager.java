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
package com.raytheon.uf.viz.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * Uses extension point to look up contexts for editors
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 21, 2009            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class ContextManager {

    private static class Context {
        int refCount = 0;

        IContextActivation activation = null;
    }

    private static Map<String, String[]> contexts = null;

    private static final String EXTENSION_POINT = "com.raytheon.uf.viz.core.classContext";

    private static Map<IWorkbenchWindow, ContextManager> instanceMap = new HashMap<IWorkbenchWindow, ContextManager>();

    private static synchronized String[] getContextsForClass(String name) {
        if (contexts == null) {
            loadContexts();
        }
        String[] cons = contexts.get(name);
        if (cons == null) {
            cons = new String[] {};
        }
        return cons;
    }

    private static void loadContexts() {
        contexts = new HashMap<String, String[]>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        if (registry == null) {
            return;
        }
        IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT);

        IExtension[] extensions = point.getExtensions();

        for (IExtension ext : extensions) {
            IConfigurationElement[] config = ext.getConfigurationElements();

            for (IConfigurationElement cfg : config) {
                String name = cfg.getAttribute("class");
                Set<String> ids = new HashSet<String>();
                String[] current = contexts.get(name);
                if (current != null) {
                    for (String curr : current) {
                        ids.add(curr);
                    }
                }
                IConfigurationElement[] children = cfg.getChildren("contextId");
                for (IConfigurationElement child : children) {
                    ids.add(child.getAttribute("id"));
                }
                contexts.put(name, ids.toArray(new String[ids.size()]));
            }
        }
    }

    private static String[] getAllContextsForClass(Class<?> clazz) {
        Set<String> contexts = new HashSet<String>();

        String[] ids = getContextsForClass(clazz.getName());
        for (String id : ids) {
            contexts.add(id);
        }

        Class<?> superClass = clazz.getSuperclass();
        while (superClass.equals(Object.class) == false) {
            ids = getContextsForClass(superClass.getName());
            for (String id : ids) {
                contexts.add(id);
            }
            superClass = superClass.getSuperclass();
        }

        return contexts.toArray(new String[contexts.size()]);
    }

    public static synchronized ContextManager getInstance(
            IWorkbenchWindow window) {
        ContextManager manager = instanceMap.get(window);
        if (manager == null) {
            manager = new ContextManager(window);
            instanceMap.put(window, manager);
        }
        return manager;
    }

    private Set<Object> activeObjects;

    private Map<String, ContextManager.Context> activeMap;

    private IContextService service;

    private ContextManager(IWorkbenchWindow window) {
        service = (IContextService) window.getService(IContextService.class);
        activeObjects = new HashSet<Object>();
        activeMap = new HashMap<String, ContextManager.Context>();
    }

    public void activateContexts(Object obj) {
        synchronized (service) {
            if (obj == null) {
                return;
            }
            if (activeObjects.contains(obj)) {
                // we already activated the contexts
                return;
            }

            String[] ids = ContextManager
                    .getAllContextsForClass(obj.getClass());
            for (String context : ids) {
                ContextManager.Context ctx = activeMap.get(context);
                if (ctx == null) {
                    ctx = new ContextManager.Context();
                    ctx.activation = service.activateContext(context);
                    activeMap.put(context, ctx);
                }
                ctx.refCount++;
            }

            activeObjects.add(obj);
        }
    }

    public void deactivateContexts(Object obj) {
        synchronized (service) {
            if (obj == null) {
                return;
            }

            if (activeObjects.contains(obj) == false) {
                // don't deactive what was not activated
                return;
            }

            String[] ids = ContextManager
                    .getAllContextsForClass(obj.getClass());
            for (String context : ids) {
                ContextManager.Context ctx = activeMap.get(context);
                if (ctx != null) {
                    ctx.refCount--;
                    if (ctx.refCount <= 0) {
                        service.deactivateContext(ctx.activation);
                        activeMap.remove(context);
                    }
                }
            }
            activeObjects.remove(obj);
        }
    }
}
