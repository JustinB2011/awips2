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
package com.raytheon.uf.common.geospatial.interpolation.data;

import org.geotools.coverage.grid.GeneralGridGeometry;

/**
 * Wraps a short array as an unsigned {@link DataSource} and
 * {@link DataDestination}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 3, 2012            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class UnsignedShortArrayWrapper extends ShortArrayWrapper {

    public UnsignedShortArrayWrapper(short[] array, GeneralGridGeometry geometry) {
        super(array, geometry);
    }

    public UnsignedShortArrayWrapper(short[] array, int nx, int ny) {
        super(array, nx, ny);
    }

    public UnsignedShortArrayWrapper(int nx, int ny) {
        super(nx, ny);
    }

    public UnsignedShortArrayWrapper(GeneralGridGeometry geometry) {
        super(geometry);
    }

    @Override
    protected double getDataValueInternal(int index) {
        return array[index] & 0xFFFF;
    }

}
