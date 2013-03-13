package gov.noaa.nws.ncep.viz.rsc.solarimage;

import gov.noaa.nws.ncep.common.dataplugin.solarimage.SolarImageRecord;

import java.nio.FloatBuffer;

import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.image.ColorMapData.ColorMapDataType;
import com.raytheon.uf.viz.core.exception.VizException;


public class LogSolarImageDataCallback extends SolarImageDataCallback {
	
    /**
     * @param record
     * @throws VizException 
     */
    public LogSolarImageDataCallback(SolarImageRecord record) throws VizException {
        super(record);
    }

    @Override
    public ColorMapData getColorMapData() throws VizException {
        System.out.println("Retrieving SolarImage data from HDF5...");

        int[] dimensions = new int[] { imgData.getNx(), imgData.getNy() };

        float[] vals = imgData.getImageValues();
        float[] logVals = new float[vals.length];

        for (int n = 0; n < vals.length; n++) {
            if (vals[n] <= 0)
                logVals[n] = Float.NEGATIVE_INFINITY;
            else
                logVals[n] = (float) (Math.log10(vals[n]));
        }

        FloatBuffer buffer = FloatBuffer.wrap(logVals);

        return new ColorMapData(buffer, dimensions, ColorMapDataType.FLOAT);
    }

    @Override
    public double getOriginalValue(double val) {
        return Math.pow(10.0, val);
    }

}
