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
package com.raytheon.uf.viz.kml.export.graphics.ext;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.measure.unit.Unit;

import org.geotools.coverage.grid.GridGeometry2D;

import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.geospatial.interpolation.data.ByteBufferWrapper;
import com.raytheon.uf.common.geospatial.interpolation.data.DataSource;
import com.raytheon.uf.common.geospatial.interpolation.data.FloatBufferWrapper;
import com.raytheon.uf.common.geospatial.interpolation.data.IntBufferWrapper;
import com.raytheon.uf.common.geospatial.interpolation.data.ShortBufferWrapper;
import com.raytheon.uf.common.geospatial.interpolation.data.UnsignedByteBufferWrapper;
import com.raytheon.uf.common.geospatial.interpolation.data.UnsignedShortBufferWrapper;
import com.raytheon.uf.viz.core.data.IColorMapDataRetrievalCallback;
import com.raytheon.uf.viz.core.drawables.IColormappedImage;
import com.raytheon.uf.viz.core.drawables.ext.IImagingExtension;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * 
 * Implements the interface and converts all data callbacks into raw float
 * arrays.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2012            bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class KmlColormappedImage extends KmlImage implements IColormappedImage {

    private final IColorMapDataRetrievalCallback dataCallback;

    private ColorMapParameters colorMapParameters;

    private Unit<?> dataUnit;

    public KmlColormappedImage(IColorMapDataRetrievalCallback dataCallback,
            ColorMapParameters colorMapParameters) {
        this.dataCallback = dataCallback;
        this.colorMapParameters = colorMapParameters;
    }

    public DataSource getData(GridGeometry2D geometry) throws VizException {
        ColorMapData data = dataCallback.getColorMapData();
        this.dataUnit = data.getDataUnit();
        switch (data.getDataType()) {
        case FLOAT:
            return new FloatBufferWrapper(((FloatBuffer) data.getBuffer()),
                    geometry);
        case BYTE: {
            return new UnsignedByteBufferWrapper(
                    ((ByteBuffer) data.getBuffer()), geometry);
        }
        case SIGNED_BYTE: {
            return new ByteBufferWrapper(((ByteBuffer) data.getBuffer()),
                    geometry);
        }
        case INT: {
            return new IntBufferWrapper(((IntBuffer) data.getBuffer()),
                    geometry);
        }
        case SHORT: {
            return new ShortBufferWrapper(((ShortBuffer) data.getBuffer()),
                    geometry);
        }
        case UNSIGNED_SHORT: {
            return new UnsignedShortBufferWrapper(
                    ((ShortBuffer) data.getBuffer()), geometry);
        }
        }
        throw new UnsupportedOperationException(
                "Kml Export does not supprt image type: " + data.getDataType());

    }

    @Override
    public Class<? extends IImagingExtension> getExtensionClass() {
        return KmlColormappedImageExtension.class;
    }

    @Override
    public ColorMapParameters getColorMapParameters() {
        return colorMapParameters;
    }

    @Override
    public void setColorMapParameters(ColorMapParameters params) {
        this.colorMapParameters = params;
    }

    @Override
    public double getValue(int x, int y) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.drawables.IColormappedImage#getDataUnit()
     */
    @Override
    public Unit<?> getDataUnit() {
        return dataUnit == null ? getColorMapParameters().getDataUnit()
                : dataUnit;
    }

}
