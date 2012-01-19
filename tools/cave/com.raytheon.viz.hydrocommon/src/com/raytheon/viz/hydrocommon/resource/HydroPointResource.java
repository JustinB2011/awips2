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
package com.raytheon.viz.hydrocommon.resource;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.viz.hydrocommon.resource.HydroPointResourceData.Style;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 22, 2009            snaples     Initial creation
 * 
 * </pre>
 * 
 * @author snaples
 * @version 1.0
 */

public abstract class HydroPointResource <T extends HydroPointResourceData<?>> extends
		AbstractVizResource<T, MapDescriptor> {

    private static final int LINE_LENGTH = 8;

    private static final int CIRCLE_RADIUS = 2;

    private static final RGB DEFAULT_COLOR = new RGB(0, 255, 0);

	public HydroPointResource(T resourceData,
			LoadProperties loadProperties) {
		super(resourceData, loadProperties);
		if (this.getCapability(ColorableCapability.class).getColor() == null) {
			this.getCapability(ColorableCapability.class).setColor(
					DEFAULT_COLOR);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#getName()
	 */
    @Override
    public String getName() {
		return resourceData.getName();
    }

	public void setName(String name) {
		this.resourceData.setName(name);
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.drawables.IRenderable#paint(com.raytheon.uf.
     * viz.core.IGraphicsTarget,
     * com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        double[] pixels = descriptor.worldToPixel(new double[] {
                getLocation().x, getLocation().y });

        if (pixels != null) {
            RGB color = getCapability(ColorableCapability.class).getColor();
            if (getStyle() == Style.STAR) {
                target.drawLine(pixels[0], pixels[1] - LINE_LENGTH, 0.0,
                        pixels[0], pixels[1] + LINE_LENGTH, 0.0, color,
                        getLineWidth());
                target.drawLine(pixels[0] - LINE_LENGTH, pixels[1]
                        + LINE_LENGTH, 0.0, pixels[0] + LINE_LENGTH, pixels[1]
                        - LINE_LENGTH, 0.0, color, getLineWidth());
                target.drawLine(pixels[0] - LINE_LENGTH, pixels[1]
                        - LINE_LENGTH, 0.0, pixels[0] + LINE_LENGTH, pixels[1]
                        + LINE_LENGTH, 0.0, color, getLineWidth());
            } else if (getStyle() == Style.RECTANGLE) {
                target.drawShadedRect(paintProps.getView().getExtent(), color,
                        1.0, null);
            } else if (getStyle() == Style.CIRCLE) {
                target.drawFilledCircle(pixels[0], pixels[1], 0.0,
                        CIRCLE_RADIUS, color);
            }

        }
    }

    private Style getStyle() {
        return (resourceData).getStyle();
    }

    /**
     * @return the location
     */
    public Coordinate getLocation() {
        return (resourceData).getLocation();
    }

    /**
     * @param location
     *            the location to set
     */
    public void setLocation(Coordinate location) {
        (resourceData).setLocation(location);
    }

    @Override
    protected void disposeInternal() {
        // TODO Auto-generated method stub

    }

    public void setLineWidth(float lineWidth) {
        (resourceData).setLineWidth(lineWidth);
    }

    private float getLineWidth() {
        return (resourceData).lineWidth;
    }
}
