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
package com.raytheon.uf.viz.datadelivery.rsc;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.RGBColors;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.datadelivery.common.spatial.SpatialUtils;
import com.raytheon.uf.viz.datadelivery.subscription.subset.BoxChangeEvent;
import com.raytheon.uf.viz.datadelivery.subscription.subset.BoxListener;
import com.raytheon.viz.ui.input.PanHandler;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Resource for drawing a bounding box on the Spatial Subset Map Dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 19, 2012            mpduff      Initial creation.
 * Oct 31, 2012   1278     mpduff      Added functionality for other datasets in NOMADS.
 * Jun 21, 2013   2132     mpduff      Convert coordinates to East/West before drawing.
 * Oct 10, 2013   2428     skorolev    Fixed memory leak for Regions
 * Oct 24, 2013   2486     skorolev    Fixed an error of editing subset box.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DrawBoxResource extends
        AbstractVizResource<AbstractResourceData, MapDescriptor> {
    /** The mouse adapter */
    private SpatialSubsetMouseAdapter mouseAdapter;

    /** Upper left Coordinate */
    private Coordinate c1;

    /** Lower Right Coordinate */
    private Coordinate c2;

    /** Copy of upper left Coordinate */
    private Coordinate origC1;

    /** Copy of lower right coordinate */
    private Coordinate origC2;

    /** upper left x */
    private int x1;

    /** upper left y */
    private int y1;

    /** lower right x */
    private int x2;

    /** lower right y */
    private int y2;

    /** The graphics target object reference */
    private IGraphicsTarget target;

    /** Defined area box line color */
    private final RGB boxColor = RGBColors.getRGBColor("white");

    /** BoxListener list */
    private final ArrayList<BoxListener> boxListeners = new ArrayList<BoxListener>();

    /** Region list */
    private final ArrayList<Region> regionList = new ArrayList<Region>();

    /** the Shell */
    private Shell shell;

    /** Actively drawing a box flag */
    private boolean drawingBox = false;

    /** Actively resizing a box flag */
    private boolean resizingBox = false;

    /** Which side of the box are we working with */
    private int boxSide = 0;

    /** The SpatialUtils class */
    private SpatialUtils spatialUtils;

    /** The x value of the 360 degree longitude line */
    private double x360;

    /** Map pixel rectangle */
    private Rectangle mapRctgl;

    /**
     * @param resourceData
     * @param loadProperties
     */
    public DrawBoxResource(DrawBoxResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#disposeInternal()
     */
    @Override
    protected void disposeInternal() {
        this.disposeRegions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractVizResource#paintInternal(com.raytheon
     * .uf.viz.core.IGraphicsTarget,
     * com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        this.target = target;
        if ((c1 != null) && (c2 != null)) {
            c1.x = spatialUtils.convertToEastWest(c1.x);
            c2.x = spatialUtils.convertToEastWest(c2.x);
            double[] ul = descriptor.worldToPixel(new double[] { c1.x, c1.y });
            double[] lr = descriptor.worldToPixel(new double[] { c2.x, c2.y });
            PixelExtent pe = new PixelExtent(ul[0], lr[0], ul[1], lr[1]);
            target.drawRect(pe, boxColor, 3, 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractVizResource#initInternal(com.raytheon
     * .uf.viz.core.IGraphicsTarget)
     */
    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        IDisplayPaneContainer container = getResourceContainer();
        mouseAdapter = new SpatialSubsetMouseAdapter(container);
        if (container != null) {
            container.registerMouseHandler(mouseAdapter);
        }

        Coordinate c = new Coordinate();
        c.x = 360;
        c.y = 0;
        double[] point360 = getResourceContainer().translateInverseClick(c);
        this.x360 = Math.round(point360[0]);

        getMapRectangle();

        // Create initial regions
        if ((c1 != null) && (c2 != null)) {
            double[] luBox = getResourceContainer().translateInverseClick(c1);
            x1 = (int) luBox[0];
            y1 = (int) luBox[1];
            double[] rbBox = getResourceContainer().translateInverseClick(c2);
            x2 = (int) rbBox[0];
            y2 = (int) rbBox[1];
            createRegions();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#getName()
     */
    @Override
    public String getName() {
        return ((DrawBoxResourceData) resourceData).getMapName();
    }

    /**
     * Mouse adapter class.
     */
    private class SpatialSubsetMouseAdapter extends PanHandler {

        /**
         * @param container
         */
        public SpatialSubsetMouseAdapter(IDisplayPaneContainer container) {
            super(container);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.InputAdapter#handleMouseDown(int, int,
         * int)
         */
        @Override
        public boolean handleMouseDown(int x, int y, int mouseButton) {
            if (mouseButton == 1) {
                // handle mouse only in the map space
                if (mapRctgl.contains(x, y)) {
                    if (!resizingBox) {
                        x1 = x;
                        y1 = y;
                        c1 = getResourceContainer().translateClick(x, y);
                        if (c1 != null) {
                            c1.x = spatialUtils.convertToEasting(c1.x);
                            if (spatialUtils.getLongitudinalShift() > 0
                                    && x >= x360) {
                                c1.x += 360;
                            }
                        }
                    }
                }
            } else if (mouseButton == 2) {
                super.handleMouseDown(x, y, 1);
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.InputAdapter#handleMouseDownMove(int,
         * int, int)
         */
        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (mouseButton == 1) {
                drawingBox = true;
                // handle mouse only in the map space
                if (mapRctgl.contains(x, y)) {
                    if (resizingBox) {
                        Coordinate c = getResourceContainer().translateClick(x,
                                y);
                        if (c != null) {
                            if (boxSide == 0) {
                                c1.y = c.y;
                            } else if (boxSide == 1) {
                                c1.x = c.x;
                                c1.x = spatialUtils.convertToEasting(c1.x);
                                if (spatialUtils.getLongitudinalShift() > 0
                                        && x >= x360) {
                                    c1.x += 360;
                                }
                            } else if (boxSide == 2) {
                                c2.y = c.y;
                            } else if (boxSide == 3) {
                                c2.x = c.x;
                                c2.x = spatialUtils.convertToEasting(c2.x);
                                if (spatialUtils.getLongitudinalShift() > 0
                                        && x >= x360) {
                                    c2.x += 360;
                                }
                            }
                        }
                    } else {
                        c2 = getResourceContainer().translateClick(x, y);
                        if (c2 != null) {
                            c2.x = spatialUtils.convertToEasting(c2.x);
                            if (spatialUtils.getLongitudinalShift() > 0
                                    && x >= x360) {
                                c2.x += 360;
                            }
                        }
                        x2 = x;
                        y2 = y;
                    }

                    fireBoxChangedEvent();
                    target.setNeedsRefresh(true);
                }
            } else if (mouseButton == 2) {
                super.handleMouseDownMove(x, y, 1);

            }

            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.InputAdapter#handleMouseMove(int, int)
         */
        @Override
        public boolean handleMouseMove(int x, int y) {
            for (int i = 0; i < regionList.size(); i++) {
                Region r = regionList.get(i);
                boxSide = i;
                if (r.contains(x, y)) {
                    resizingBox = true;
                    if ((boxSide == 0) || (boxSide == 2)) {
                        shell.setCursor(shell.getDisplay().getSystemCursor(
                                SWT.CURSOR_SIZENS));
                        break;
                    } else {
                        shell.setCursor(shell.getDisplay().getSystemCursor(
                                SWT.CURSOR_SIZEWE));
                        break;
                    }
                } else {
                    shell.setCursor(shell.getDisplay().getSystemCursor(
                            SWT.CURSOR_ARROW));
                    resizingBox = false;
                }
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.InputAdapter#handleMouseUp(int, int,
         * int)
         */
        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {
            if (mouseButton == 1) {
                // handle mouse only in the map space
                if (mapRctgl.contains(x, y)) {
                    if (resizingBox) {
                        Coordinate c = getResourceContainer().translateClick(x,
                                y);
                        if (c != null) {
                            c.x = spatialUtils.convertToEasting(c.x);
                            if (spatialUtils.getLongitudinalShift() > 0
                                    && x >= x360) {
                                c.x += 360;
                            }
                            if (boxSide == 0) {
                                c1.y = c.y;
                                y1 = y;
                            } else if (boxSide == 1) {
                                c1.x = c.x;
                                x1 = x;
                            } else if (boxSide == 2) {
                                c2.y = c.y;
                                y2 = y;
                            } else if (boxSide == 3) {
                                c2.x = c.x;
                                x2 = x;
                            }
                        }
                    } else {
                        if (drawingBox) {
                            x2 = x;
                            y2 = y;
                            c2 = getResourceContainer().translateClick(x, y);
                            if (c2 != null) {
                                c2.x = spatialUtils.convertToEasting(c2.x);
                                if (spatialUtils.getLongitudinalShift() > 0
                                        && x >= x360) {
                                    c2.x += 360;
                                }
                            }
                        } else {
                            c1 = getResourceContainer().translateClick(x, y);
                        }
                    }
                }
                createRegions();
                target.setNeedsRefresh(true);
                fireBoxChangedEvent();
                drawingBox = false;
            } else if (mouseButton == 2) {
                super.handleMouseUp(x, y, 1);
                getMapRectangle();
            }
            return true;
        }

        /*
         * Turn off mouse wheel.
         * 
         * (non-Javadoc)
         * 
         * @see
         * com.raytheon.viz.ui.input.PanHandler#handleMouseWheel(org.eclipse
         * .swt.widgets.Event, int, int)
         */
        @Override
        public boolean handleMouseWheel(Event event, int x, int y) {
            return false;
        }
    }

    /**
     * Add a box listener.
     * 
     * @param listener
     *            the listener to add
     */
    public void addListener(BoxListener listener) {
        this.boxListeners.add(listener);
    }

    /**
     * Remove the box listener.
     * 
     * @param listener
     *            the listener to remove
     */
    public void removeListener(BoxListener listener) {
        this.boxListeners.remove(listener);
    }

    /**
     * Fire a box changed event.
     */
    private void fireBoxChangedEvent() {
        if (c1 != null && c2 != null) {
            Coordinate[] corners = spatialUtils.adjustCorners(c1, c2);
            Coordinate ul = corners[0];
            Coordinate lr = corners[1];

            BoxChangeEvent event = new BoxChangeEvent(
                    new Coordinate[] { ul, lr });
            for (BoxListener listener : boxListeners) {
                listener.cornerPointsChanged(event);
            }
        }
    }

    /**
     * Create the regions for the mouseover for resizing the existing box
     */
    private void createRegions() {
        if (!regionList.isEmpty()) {
            disposeRegions();
        }
        int buffer = 10;

        // Top
        Region region = new Region();
        int[] ia = new int[8];
        ia[0] = x1 - buffer;
        ia[1] = y1 - buffer;
        ia[2] = x1 + buffer;
        ia[3] = y1 + buffer;
        ia[4] = x2 - buffer;
        ia[5] = y1 - buffer;
        ia[6] = x2 + buffer;
        ia[7] = y1 + buffer;
        region.add(ia);
        regionList.add(region);

        // Left
        region = new Region();
        ia = new int[8];
        ia[0] = x1 - buffer;
        ia[1] = y1 - buffer;
        ia[2] = x1 + buffer;
        ia[3] = y1 + buffer;
        ia[4] = x1 - buffer;
        ia[5] = y2 - buffer;
        ia[6] = x1 + buffer;
        ia[7] = y2 + buffer;
        region.add(ia);
        regionList.add(region);

        // Bottom
        region = new Region();
        ia = new int[8];
        ia[0] = x2 - buffer;
        ia[1] = y2 - buffer;
        ia[2] = x2 + buffer;
        ia[3] = y2 + buffer;
        ia[4] = x1 - buffer;
        ia[5] = y2 - buffer;
        ia[6] = x1 + buffer;
        ia[7] = y2 + buffer;
        region.add(ia);
        regionList.add(region);

        // Right
        region = new Region();
        ia = new int[8];
        ia[0] = x2 - buffer;
        ia[1] = y2 - buffer;
        ia[2] = x2 + buffer;
        ia[3] = y2 + buffer;
        ia[4] = x2 - buffer;
        ia[5] = y1 - buffer;
        ia[6] = x2 + buffer;
        ia[7] = y1 + buffer;
        region.add(ia);
        regionList.add(region);
    }

    /**
     * Set the parent shell.
     * 
     * @param shell
     *            the parent shell
     */
    public void setShell(Shell shell) {
        this.shell = shell;
    }

    /**
     * Set the coordinates.
     * 
     * @param ul
     *            upper left coordinates
     * @param lr
     *            lower right coordinates
     */
    public void setCoordinates(Coordinate ul, Coordinate lr) {
        this.c1 = ul;
        this.c2 = lr;
        this.origC1 = new Coordinate();
        origC1.x = c1.x;
        origC1.y = c1.y;
        this.origC2 = new Coordinate();
        origC2.x = c2.x;
        origC2.y = c2.y;
    }

    /**
     * @param spatialUtils
     *            the spatialUtils to set
     */
    public void setSpatialUtils(SpatialUtils spatialUtils) {
        this.spatialUtils = spatialUtils;
    }

    /**
     * Dispose regions.
     */
    private void disposeRegions() {
        for (Region i : regionList) {
            i.dispose();
        }
        regionList.clear();
    }

    /**
     * Map's rectangle in pixels
     */
    private void getMapRectangle() {
        Coordinate top = new Coordinate();
        top.x = spatialUtils.getUpperLeft().x;
        top.y = spatialUtils.getUpperLeft().y;
        double[] pointTop = getResourceContainer().translateInverseClick(top);
        int xTop = (int) Math.round(pointTop[0]);
        int yTop = (int) Math.round(pointTop[1]);

        Coordinate bot = new Coordinate();
        bot.x = spatialUtils.getLowerRight().x;
        bot.y = spatialUtils.getLowerRight().y;
        double[] pointBot = getResourceContainer().translateInverseClick(bot);
        int xBottom = (int) Math.round(pointBot[0]);
        int yBottom = (int) Math.round(pointBot[1]);

        mapRctgl = new Rectangle(xTop, yTop, (xBottom - xTop), (yBottom - yTop));
    }
}
