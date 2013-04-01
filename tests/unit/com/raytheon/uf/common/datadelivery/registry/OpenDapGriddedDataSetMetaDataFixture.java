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
package com.raytheon.uf.common.datadelivery.registry;

import java.text.ParseException;
import java.util.Random;

import com.raytheon.uf.common.time.util.ImmutableDate;
import com.raytheon.uf.common.util.AbstractFixture;

/**
 * {@link AbstractFixture} implementation for
 * {@link OpenDapGriddedDataSetMetaData} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 05, 2012 1102      djohnson     Initial creation
 * Oct 16, 2012 0726      djohnson     Always use OpenDAP service type, use TimeFixture.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */

public class OpenDapGriddedDataSetMetaDataFixture extends
        AbstractFixture<OpenDapGriddedDataSetMetaData> {

    public static final OpenDapGriddedDataSetMetaDataFixture INSTANCE = new OpenDapGriddedDataSetMetaDataFixture();

    /**
     * Disabled constructor.
     */
    private OpenDapGriddedDataSetMetaDataFixture() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenDapGriddedDataSetMetaData getInstance(long seedValue, Random random) {
        final Time time = TimeFixture.INSTANCE.get(seedValue);
        final OpenDapGriddedDataSet dataSet = OpenDapGriddedDataSetFixture.INSTANCE
                .get(seedValue);

        OpenDapGriddedDataSetMetaData obj = new OpenDapGriddedDataSetMetaData();
        obj.setCycle(TimeFixture.getCycleForSeed(seedValue));
        obj.setDataSetDescription("description" + seedValue);
        obj.setDataSetName(dataSet.getDataSetName());
        try {
            obj.setDate(new ImmutableDate(time.getStartDate()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // TODO: Ensemble fixture
        // obj.setEnsemble(new EnsembleFixture().get(seedValue));
        // TODO: Levels fixture
        // obj.setLevelTypes(levelTypes);
        obj.setProviderName(dataSet.getProviderName());
        obj.setTime(time);
        obj.setUrl("http://" + seedValue);

        return obj;
    }

}
