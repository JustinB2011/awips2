/*
 * The following software products were developed by Raytheon:
 *
 * ADE (AWIPS Development Environment) software
 * CAVE (Common AWIPS Visualization Environment) software
 * EDEX (Environmental Data Exchange) software
 * uFrame™ (Universal Framework) software
 *
 * Copyright (c) 2010 Raytheon Co.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 *
 * Contractor Name: Raytheon Company
 * Contractor Address:
 * 6825 Pine Street, Suite 340
 * Mail Stop B8
 * Omaha, NE 68106
 * 402.291.0100
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 13, 2011            bclement     Initial creation
 *
 */
package com.raytheon.uf.common.spatial.repojection;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Arrays;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;

/**
 * 
 * @author bclement
 * @version 1.0
 */
public class IntDataReprojector extends
		AbstractDataReprojector<IntegerDataRecord> {

	protected int fill = 0;

    protected int dataMaskValue = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.raytheon.uf.common.spatial.repojection.DataReprojector#getGridCoverage
	 * (com.raytheon.uf.common.datastorage.records.IDataRecord,
	 * org.opengis.referencing.crs.CoordinateReferenceSystem,
	 * org.opengis.geometry.Envelope)
	 */
	@Override
	protected GridCoverage2D getGridCoverage(IDataRecord record,
			ReferencedEnvelope env) throws Exception {
		IntegerDataRecord dataRecord = (IntegerDataRecord) record;
		int[] data = dataRecord.getIntData();
		DataBuffer buff = new DataBufferInt(data, data.length);
		int x = (int) dataRecord.getSizes()[0];
		int y = (int) dataRecord.getSizes()[1];
		CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
		return constructGridCoverage(crs.getName() + " Grid", buff, x, y, env);
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.spatial.repojection.DataReprojector#getMaskCoverage
     * (com.raytheon.uf.common.datastorage.records.IDataRecord,
     * org.opengis.referencing.crs.CoordinateReferenceSystem,
     * org.opengis.geometry.Envelope)
     */
    @Override
    protected GridCoverage2D getMaskCoverage(IDataRecord record,
            ReferencedEnvelope env) throws Exception {
        int x = (int) record.getSizes()[0];
        int y = (int) record.getSizes()[1];
        int[] mask = new int[x * y];
        Arrays.fill(mask, dataMaskValue);
        DataBufferInt buff = new DataBufferInt(mask, mask.length);
        CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
        return constructGridCoverage(crs.getName() + " Grid", buff, x, y, env);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.raytheon.uf.common.spatial.repojection.DataReprojector#extractData(org
	 * .geotools.coverage.grid.GridCoverage2D)
	 */
	@Override
	protected IntegerDataRecord extractData(GridCoverage2D coverage) {
		RenderedImage image = coverage.getRenderedImage();
		Raster raster;
		if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
			// we can directly access data
			raster = image.getTile(0, 0);
		} else {
			// need to copy data out
			raster = image.getData();
		}
		DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
		int[] data = dataBuffer.getData();
		int height = raster.getHeight();
		int width = raster.getWidth();
		return new IntegerDataRecord("", "", data, 2, new long[] { width,
				height });
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.spatial.repojection.DataReprojector#extractData(org
     * .geotools.coverage.grid.GridCoverage2D,
     * org.geotools.coverage.grid.GridCoverage2D)
     */
    @Override
    protected IntegerDataRecord extractData(GridCoverage2D coverage,
            GridCoverage2D maskCoverage) {
        RenderedImage image = coverage.getRenderedImage();
        Raster raster;
        if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
            // we can directly access data
            raster = image.getTile(0, 0);
        } else {
            // need to copy data out
            raster = image.getData();
        }
        DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
        int[] data = dataBuffer.getData();

        // Extract mask
        image = maskCoverage.getRenderedImage();
        if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
            // we can directly access data
            raster = image.getTile(0, 0);
        } else {
            // need to copy data out
            raster = image.getData();
        }
        dataBuffer = (DataBufferInt) raster.getDataBuffer();
        int[] mask = dataBuffer.getData();

        if (mask.length == data.length) {
            for (int i = 0; i < data.length; ++i) {
                if (mask[i] != dataMaskValue) {
                    data[i] = fill;
                }
            }
        }

        int height = raster.getHeight();
        int width = raster.getWidth();
        return new IntegerDataRecord("", "", data, 2, new long[] { width,
                height });
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.raytheon.uf.common.spatial.repojection.DataReprojector#getDataSlice(com
	 * .raytheon.uf.common.datastorage.records.IDataRecord,
	 * com.raytheon.uf.common.datastorage.Request)
	 */
	@Override
	protected IntegerDataRecord getDataSlice(IDataRecord record, Request req) {
		IntegerDataRecord dataRecord = (IntegerDataRecord) record;
		int[] max = req.getMaxIndexForSlab();
		int[] min = req.getMinIndexForSlab();
		int toWidth = max[0] - min[0];
		int toHeight = max[1] - min[1];
		int[] from = dataRecord.getIntData();
		int fromWidth = (int) dataRecord.getSizes()[0];
		int[] to = new int[toWidth * toHeight];
		for (int fromY = min[1], toY = 0; fromY < max[1]; ++fromY, ++toY) {
			int toRow = toY * toWidth;
			int fromRow = fromY * fromWidth;
			for (int fromX = min[0], toX = 0; fromX < max[0]; ++fromX, ++toX) {
				to[toRow + toX] = from[fromRow + fromX];
			}
		}
		long[] sizes = { toWidth, toHeight };
		return new IntegerDataRecord("", "", to, 2, sizes);
	}

	/**
	 * @return the fill
	 */
	public int getFill() {
		return fill;
	}

	/**
	 * @param fill
	 *            the fill to set
	 */
	public void setFill(int fill) {
		this.fill = fill;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.raytheon.uf.common.spatial.repojection.AbstractDataReprojector#compatible
	 * (com.raytheon.uf.common.datastorage.records.IDataRecord)
	 */
	@Override
	protected boolean compatible(IDataRecord dataRecord) {
		return dataRecord instanceof IntegerDataRecord;
	}

	@Override
	protected IDataRecord getDataPoints(IDataRecord record, Request req) {
		IntegerDataRecord dataRecord = (IntegerDataRecord) record;
		int[] from = dataRecord.getIntData();
		int fromWidth = (int) dataRecord.getSizes()[0];
		Point[] points = req.getPoints();
		int[] to = new int[points.length];
		for (int i = 0; i < to.length; ++i) {
			Point p = points[i];
			to[i] = from[p.y * fromWidth + p.x];
		}
		return new IntegerDataRecord("", "", to, 1, new long[] { to.length });
	}

}
