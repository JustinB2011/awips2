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
package com.raytheon.uf.viz.core.drawables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeneralDerivedCRS;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.serialization.adapters.GridGeometryAdapter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.datastructure.LoopProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.exception.WrongProjectionException;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceGroup;
import com.raytheon.uf.viz.core.rsc.ResourceGroup;
import com.raytheon.uf.viz.core.rsc.ResourceList;

/**
 * AbstractDescriptor
 * 
 * <pre>
 * 
 *    SOFTWARE HISTORY
 *   
 *    Date         Ticket#     Engineer    Description
 *    ------------ ----------  ----------- --------------------------
 *    Aug 15, 2007             chammack    Initial Creation.
 *    Nov 30, 2007 461         bphillip    Using VizTime now for time matching
 *    Oct  22, 2009   #3348    bsteffen    added ability to limit number of frames
 * </pre>
 * 
 * @author chammack
 * @version 1
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractDescriptor extends ResourceGroup implements
        IDescriptor {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractDescriptor.class);

    private static class TimeManager {
        DataTime[] frames;

        AbstractTimeMatcher timeMatcher;

        int numberOfFrames = 12;

        public TimeManager() {
            Integer frames = ((Integer) VizGlobalsManager.getCurrentInstance()
                    .getPropery(VizConstants.FRAMES_ID));
            if (frames != null) {
                numberOfFrames = frames.intValue();
            }
        }
    }

    protected Set<IFrameChangedListener> listeners = new HashSet<IFrameChangedListener>();

    protected TimeManager timeManager = new TimeManager();

    /** The renderable display descriptor is loaded to */
    protected IRenderableDisplay renderableDisplay;

    /** The time matching map */
    protected ConcurrentHashMap<AbstractVizResource<?, ?>, DataTime[]> timeMatchingMap;

    /** The index of the currently shown time */
    private int frameIndex;

    /** The time to restore to when setting frames */
    private DataTime restoredTime = null;

    /** The number of frames */
    @XmlElement
    protected int limitedNumberOfFrames = Integer.MAX_VALUE;

    /** The frame coordination object */
    protected IFrameCoordinator frameCoordinator;

    private MathTransform worldToPixel;

    private MathTransform pixelToWorld;

    /** The spatial grid for the descriptor */
    private GeneralGridGeometry gridGeometry;

    public AbstractDescriptor(GeneralGridGeometry gridGeometry) {
        this();
        this.gridGeometry = gridGeometry;
        init();
    }

    /**
     * Constructor
     */
    public AbstractDescriptor() {
        super();
        frameCoordinator = new FrameCoordinator(this);
        timeMatchingMap = new ConcurrentHashMap<AbstractVizResource<?, ?>, DataTime[]>();
        resourceList.addPreAddListener(new ResourceList.AddListener() {

            @Override
            public void notifyAdd(ResourcePair rp) throws VizException {
                preAddListener(rp);
            }

        });

        resourceList.addPostAddListener(new ResourceList.AddListener() {

            @Override
            public void notifyAdd(ResourcePair rp) throws VizException {
                postAddListener(rp);
            }

        });

        resourceList.addPreRemoveListener(new ResourceList.RemoveListener() {

            @Override
            public void notifyRemove(ResourcePair rp) throws VizException {
                preRemoveListener(rp.getResource());
            }

        });

        resourceList.addPostRemoveListener(new ResourceList.RemoveListener() {

            @Override
            public void notifyRemove(ResourcePair rp) throws VizException {
                AbstractTimeMatcher tm = getTimeMatcher();
                AbstractVizResource<?, ?> basis = null;
                if (tm != null) {
                    basis = tm.getTimeMatchBasis();
                }

                postRemoveListener(rp.getResource());

                AbstractVizResource<?, ?> newBasis = null;
                if (tm != null) {
                    newBasis = tm.getTimeMatchBasis();
                }

                if (basis != newBasis) {
                    redoTimeMatching();
                }
            }
        });
    }

    private void redoTimeMatching() throws VizException {
        if (getTimeMatcher() != null) {
            getTimeMatcher().redoTimeMatching(this);
        }

        if (renderableDisplay != null
                && renderableDisplay.getContainer() != null) {
            IDisplayPaneContainer container = renderableDisplay.getContainer();
            for (IDisplayPane pane : container.getDisplayPanes()) {
                if (pane.getDescriptor() != this) {
                    if (pane.getDescriptor().getTimeMatcher() != null) {
                        pane.getDescriptor().getTimeMatcher()
                                .redoTimeMatching(pane.getDescriptor());
                    }
                }
            }
        }
    }

    protected void postAddListener(ResourcePair rp) {

    }

    @SuppressWarnings("unchecked")
    protected void preAddListener(ResourcePair rp)
            throws WrongProjectionException {

        AbstractVizResource<?, AbstractDescriptor> resource = (AbstractVizResource<?, AbstractDescriptor>) rp
                .getResource();

        resource.setDescriptor(this);

        if (resource.getResourceData() instanceof IResourceGroup) {
            ResourceList rl = ((IResourceGroup) resource.getResourceData())
                    .getResourceList();
            synchronized (rl) {
                for (ResourcePair rp1 : rl) {
                    preAddListener(rp1);
                }
            }
        }

    }

    protected void preRemoveListener(AbstractVizResource<?, ?> resource) {

    }

    protected void postRemoveListener(AbstractVizResource<?, ?> resource) {
        if (getTimeMatcher() != null) {
            getTimeMatcher().handleRemove(resource, this);
        }
        // Remove autoupdating references

        if (resource != null) {
            synchronized (timeManager) {
                timeMatchingMap.remove(resource);
            }
            if (resource.getResourceData() instanceof IResourceGroup) {
                ResourceList rl = ((IResourceGroup) resource.getResourceData())
                        .getResourceList();
                synchronized (rl) {
                    for (ResourcePair rp : rl) {
                        AbstractVizResource<?, ?> rsc = rp.getResource();
                        if (rsc != null) {
                            postRemoveListener(rsc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Use getFramesInfo() for thread safe use!
     * 
     * The times of the frames
     * 
     * @return
     */
    @Deprecated
    public DataTime[] getFrames() {
        return getFramesInfo().frameTimes;
    }

    /**
     * Use setFramesInfo(...) for thread safe use!
     * 
     * The times of the frames
     * 
     * @return
     */
    @Deprecated
    public void setFrameTimes(DataTime[] dataTime) {
        setFramesInfo(new FramesInfo(dataTime));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getCurrentTimeFrame()
     */
    @Override
    @Deprecated
    public int getCurrentFrame() {
        return getFramesInfo().getFrameIndex();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getNumberOfFrames()
     */
    @Override
    public int getNumberOfFrames() {
        return Math.min(timeManager.numberOfFrames, limitedNumberOfFrames);
    }

    /**
     * used only to serialize the actual number of frames properly.
     * 
     * @return
     */
    @XmlElement(name = "numberOfFrames")
    public int getNumberOfFramesSerialize() {
        return timeManager.numberOfFrames;
    }

    /**
     * used for (reverse?) serialization.
     * 
     * @param val
     */
    public void setNumberOfFramesSerialize(int val) {
        timeManager.numberOfFrames = val;
    }

    @Override
    @Deprecated
    public int getFrameCount() {
        return getFramesInfo().getFrameCount();
    }

    @Override
    @Deprecated
    public void setFrame(int frame) {
        setFramesInfo(new FramesInfo(frame));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#setNumberOfFrames(int)
     */
    @Override
    public void setNumberOfFrames(int frameCount) {
        timeManager.numberOfFrames = frameCount;
        // This will clear out the times for the basis which should redo time
        // matching for all resource next time redo is called for this
        // descriptor
        if (getTimeMatcher() != null
                && getTimeMatcher().getTimeMatchBasis() != null) {
            getTimeMatcher().redoTimeMatching(
                    getTimeMatcher().getTimeMatchBasis());
        }
    }

    @Override
    public boolean limitNumberOfFrames(int frameCount) {
        FramesInfo info = getFramesInfo();
        int frameIndex = info.frameIndex;
        DataTime[] frames = info.frameTimes;
        if (frameCount <= getNumberOfFrames()) {
            if (frames != null && frameIndex >= 0 && frames.length > frameIndex) {
                restoredTime = frames[frameIndex];
            }
            limitedNumberOfFrames = frameCount;
            // This will clear out the times for the basis which should redo
            // time
            // matching for all resource next time redo is called for this
            // descriptor
            if (getTimeMatcher() != null
                    && getTimeMatcher().getTimeMatchBasis() != null) {
                getTimeMatcher().redoTimeMatching(
                        getTimeMatcher().getTimeMatchBasis());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean unlimitNumberOfFrames() {
        FramesInfo info = getFramesInfo();
        int frameIndex = info.frameIndex;
        DataTime[] frames = info.frameTimes;
        if (limitedNumberOfFrames <= getNumberOfFrames()) {
            if (frames != null && frameIndex >= 0 && frames.length > frameIndex) {
                restoredTime = frames[frameIndex];
            }
            limitedNumberOfFrames = Integer.MAX_VALUE;
            // This will clear out the times for the basis which should redo
            // time
            // matching for all resource next time redo is called for this
            // descriptor
            if (getTimeMatcher() != null
                    && getTimeMatcher().getTimeMatchBasis() != null) {
                getTimeMatcher().redoTimeMatching(
                        getTimeMatcher().getTimeMatchBasis());
            }
            return true;
        }
        limitedNumberOfFrames = Integer.MAX_VALUE;
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // Remove all the resources so that the removal handlers execute
        List<AbstractVizResource<?, ?>> rscs = new ArrayList<AbstractVizResource<?, ?>>();
        for (ResourcePair rp : resourceList) {
            rscs.add(rp.getResource());
        }

        for (AbstractVizResource<?, ?> rsc : rscs) {
            resourceList.remove(rsc);
        }

    }

    /**
     * Lock object AbstractDescriptor uses internally for frame locking
     */
    final Object getLockObject() {
        return timeManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getDataTimes()
     */
    @Override
    @Deprecated
    public DataTime[] getDataTimes() {
        return getFramesInfo().getFrameTimes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#setDataTimes(com.raytheon
     * .uf.common.time.DataTime[])
     */
    @Override
    @Deprecated
    public void setDataTimes(DataTime[] dataTimes) {
        setFramesInfo(new FramesInfo(dataTimes));
    }

    /**
     * Returns reference to time matching map for reading/writing. Use
     * getTimeForResource(...) where possible if reading only!
     * 
     * @return the timeMatchingMap
     */
    public Map<AbstractVizResource<?, ?>, DataTime[]> getTimeMatchingMap() {
        return timeMatchingMap;
    }

    /**
     * @return the timeMatcher
     */
    @XmlElement
    public AbstractTimeMatcher getTimeMatcher() {
        return timeManager.timeMatcher;
    }

    /**
     * @param timeMatcher
     *            the timeMatcher to set
     */
    public void setTimeMatcher(AbstractTimeMatcher timeMatcher) {
        this.timeManager.timeMatcher = timeMatcher;
    }

    @Override
    public DataTime getTimeForResource(AbstractVizResource<?, ?> rsc) {
        FramesInfo currInfo = getFramesInfo();
        return currInfo.getTimeForResource(rsc);
    }

    public void synchronizeTimeMatching(IDescriptor other) {
        if (other instanceof AbstractDescriptor) {
            timeManager = ((AbstractDescriptor) other).timeManager;
        }
    }

    private void resetValidTimes(DataTime[] origTimes) {
        DataTime[] frames = getFramesInfo().getFrameTimes();
        if (frames != null) {
            for (DataTime dt : frames) {
                if (origTimes != null) {
                    for (DataTime origTime : origTimes) {
                        if (origTime.equals(dt)) {
                            dt.setVisible(origTime.isVisible());
                        }
                    }
                } else {
                    dt.setVisible(true);
                }
            }
        }
    }

    @Override
    public IRenderableDisplay getRenderableDisplay() {
        return renderableDisplay;
    }

    @Override
    public void setRenderableDisplay(IRenderableDisplay display) {
        if (this.renderableDisplay == null || display.getDescriptor() == this) {
            this.renderableDisplay = display;
        }
    }

    @Override
    public void addFrameChangedListener(IFrameChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeFrameChangedListener(IFrameChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify the listeners that the frame changed
     * 
     * @param oldTime
     * @param newTime
     */
    protected void notifyFrameChanged(DataTime oldTime, DataTime newTime) {
        for (IFrameChangedListener listener : listeners) {
            listener.frameChanged(this, oldTime, newTime);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#loadCompatible(com.raytheon
     * .uf.viz.core.drawables.IDescriptor)
     */
    @Override
    public boolean isCompatible(IDescriptor other) {
        return this.getClass() == other.getClass();
    }

    @Override
    public void setFramesInfo(FramesInfo info) {
        // We should probably always verify but legacy code might set frames and
        // then index, the solution to this is to get rid of that code.
        if (info.setFrames && info.setIndex) {
            // Verify that this is valid
            String error = null;
            DataTime[] times = info.frameTimes;
            int idx = info.frameIndex;
            if (times == null && idx >= 0) {
                error = "Index should be less than zero when there are no frame times.";
            } else if (times != null && idx >= times.length) {
                error = "Index must be less than the number of frames.";
            } else if (idx < 0 && times != null && times.length > 0) {
                error = "Index must be positive when frames are provided";
            }
            if (times != null) {
                for (int i = 0; i < times.length; i++) {
                    if (times[i] == null) {
                        error = "Descriptor should not contain null times";
                        break;
                    }
                }
            }

            if (error != null) {
                statusHandler.handle(Priority.SIGNIFICANT, "Error: " + error,
                        new VizException("Error setting FrameInfo"));
                return;
            }
        }
        synchronized (timeManager) {
            if (info.setFrames) {
                if (info.frameTimes != null) {
                    DataTime[] newTimes = Arrays.copyOf(info.frameTimes,
                            info.frameTimes.length);
                    setFrameTimesInternal(newTimes);
                } else {
                    timeManager.frames = null;
                }
            }
            if (info.setIndex) {
                setFrameInternal(info.frameIndex);
            }
            if (info.setMap) {
                timeMatchingMap = new ConcurrentHashMap<AbstractVizResource<?, ?>, DataTime[]>(
                        info.timeMap);
            }
        }
    }

    @Override
    public FramesInfo getFramesInfo() {
        synchronized (timeManager) {
            DataTime[] frames = timeManager.frames;
            int idx = frameIndex;
            if (frames != null) {
                frames = Arrays.copyOf(frames, frames.length);
            }
            Map<AbstractVizResource<?, ?>, DataTime[]> timeMap = new HashMap<AbstractVizResource<?, ?>, DataTime[]>(
                    timeMatchingMap);
            return new FramesInfo(frames, idx, timeMap);
        }
    }

    /**
     * @param frame
     */
    private void setFrameInternal(int frame) {
        FramesInfo currInfo = getFramesInfo();
        int frameIndex = currInfo.frameIndex;
        if (frame != frameIndex) {
            DataTime[] times = currInfo.frameTimes;
            DataTime oldTime = null, newTime = null;
            // Get the old and new time
            if (times != null && frameIndex >= 0 && frameIndex < times.length) {
                oldTime = times[frameIndex];
            }
            if (times != null && frame >= 0 && frame < times.length) {
                newTime = times[frame];
            }
            this.frameIndex = frame;
            notifyFrameChanged(oldTime, newTime);
        }
    }

    private void setFrameTimesInternal(DataTime[] dataTimes) {
        DataTime[] orig = timeManager.frames;
        timeManager.frames = dataTimes;
        resetValidTimes(orig);
        if (restoredTime != null) {
            boolean found = false;
            for (int i = 0; i < timeManager.frames.length; ++i) {
                DataTime time = timeManager.frames[i];
                if (time.equals(restoredTime)) {
                    frameIndex = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                setFrameInternal(timeManager.frames.length - 1);
            }
            restoredTime = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getFrameCoordinator()
     */
    @Override
    public IFrameCoordinator getFrameCoordinator() {
        return frameCoordinator;
    }

    private void init() {
        GeneralGridGeometry gridGeometry = getGridGeometry();
        MathTransform worldToCRS = getWorldToCRSTransform(gridGeometry);
        if (worldToCRS != null) {
            try {
                MathTransform crsToPixel = gridGeometry.getGridToCRS(
                        PixelInCell.CELL_CENTER).inverse();
                worldToPixel = new DefaultMathTransformFactory()
                        .createConcatenatedTransform(worldToCRS, crsToPixel);
                pixelToWorld = worldToPixel.inverse();
            } catch (Exception e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error setting up Math Transforms,"
                                + " this descriptor may not work properly", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getCRS()
     */
    @Override
    public final CoordinateReferenceSystem getCRS() {
        if (gridGeometry != null && gridGeometry.getEnvelope() != null) {
            return gridGeometry.getEnvelope().getCoordinateReferenceSystem();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IDescriptor#getGridGeometry()
     */
    @Override
    @XmlElement
    @XmlJavaTypeAdapter(value = GridGeometryAdapter.class)
    public final GeneralGridGeometry getGridGeometry() {
        return gridGeometry;
    }

    /**
     * Set the grid geometry
     * 
     * @param gridGeometry
     *            the gridGeometry to set
     * @throws VizException
     */
    public void setGridGeometry(GeneralGridGeometry gridGeometry)
            throws VizException {
        this.gridGeometry = gridGeometry;
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#pixelToWorld(double[])
     */
    @Override
    public final double[] pixelToWorld(double[] pixel) {
        double[] output = new double[3];
        double[] wpixel = pixel;

        if (pixel.length == 2) {
            wpixel = new double[] { pixel[0], pixel[1], 0 };
        }

        if (pixelToWorld != null) {
            try {
                pixelToWorld.transform(wpixel, 0, output, 0, 1);
            } catch (TransformException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.arraycopy(wpixel, 0, output, 0, wpixel.length);
        }

        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#worldToPixel(double[])
     */
    @Override
    public final double[] worldToPixel(double[] world) {
        double[] output = new double[3];
        double[] input = world;
        if (world.length == 2) {
            input = new double[] { world[0], world[1], 0 };
        }

        if (worldToPixel != null) {
            try {
                worldToPixel.transform(input, 0, output, 0, 1);
            } catch (TransformException e) {
                return null;
            }
        } else {
            System.arraycopy(input, 0, output, 0, input.length);
        }

        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IDescriptor#changeFrame(com.raytheon
     * .uf.viz.core.drawables.IFrameCoordinator.FrameChangeOperation,
     * com.raytheon.uf.viz.core.drawables.IFrameCoordinator.FrameChangeMode)
     */
    @Override
    @Deprecated
    public void changeFrame(FrameChangeOperation operation, FrameChangeMode mode) {
        IFrameCoordinator.FrameChangeOperation fop = IFrameCoordinator.FrameChangeOperation
                .valueOf(operation.name());
        IFrameCoordinator.FrameChangeMode fmode = IFrameCoordinator.FrameChangeMode
                .valueOf(mode.name());
        getFrameCoordinator().changeFrame(fop, fmode);
    }

    /**
     * DEPRECATED: Use getFrameCoordinator().changeFrame(...) instead! This
     * function is no longer called by the DrawCoordinatedPane
     * 
     * @param loopProperties
     */
    @Deprecated
    public void checkDrawTime(LoopProperties loopProperties) {
        getFrameCoordinator().changeFrame(loopProperties);
    }

    protected static GeneralGridGeometry createGridGeometry(IExtent extent,
            CoordinateReferenceSystem crs) {
        GeneralEnvelope envelope = new GeneralEnvelope(2);
        envelope.setRange(0, extent.getMinX(), extent.getMaxX());
        envelope.setRange(1, extent.getMinY(), extent.getMaxY());
        envelope.setCoordinateReferenceSystem(crs);
        return new GridGeometry2D(
                new GeneralGridEnvelope(new int[] { 0, 0 }, new int[] {
                        (int) extent.getWidth(), (int) extent.getHeight() },
                        false), envelope);
    }

    /**
     * Get the world to CRS transform used for {@link #worldToPixel(double[])}
     * and {@link #pixelToWorld(double[])}
     * 
     * @param gridGeometry
     * @return The world to gridGeometry CRS transform or null if there is none
     */
    public static MathTransform getWorldToCRSTransform(
            GeneralGridGeometry gridGeometry) {
        CoordinateReferenceSystem crs = gridGeometry.getEnvelope()
                .getCoordinateReferenceSystem();
        if (crs instanceof GeneralDerivedCRS) {
            GeneralDerivedCRS projCRS = (GeneralDerivedCRS) crs;
            CoordinateReferenceSystem worldCRS = projCRS.getBaseCRS();
            try {
                return CRS.findMathTransform(worldCRS, crs);
            } catch (FactoryException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error setting up Math Transforms,"
                                + " this descriptor may not work properly", e);
            }
        }
        return null;
    }
}
