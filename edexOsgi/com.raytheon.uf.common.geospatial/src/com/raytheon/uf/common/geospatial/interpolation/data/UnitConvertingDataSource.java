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

import javax.measure.converter.UnitConverter;

/**
 * 
 * Source which wraps another source and converts data on demand.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 25, 2014  2791     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class UnitConvertingDataSource implements DataSource {

    protected UnitConverter unitConverter;

    protected DataSource wrappedSource;

    /**
     * Constructor
     * 
     * @param converter
     *            the unit converter to apply when setting the values in the
     *            destination
     * @param source
     *            the source to get values from
     */
    public UnitConvertingDataSource(UnitConverter converter, DataSource source) {
        this.unitConverter = converter;
        this.wrappedSource = source;
    }

    @Override
    public double getDataValue(int x, int y) {
        double val = unitConverter.convert(wrappedSource.getDataValue(x, y));
        return val;
    }

}
