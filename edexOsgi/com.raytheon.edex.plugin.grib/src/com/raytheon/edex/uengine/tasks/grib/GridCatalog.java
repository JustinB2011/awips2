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
package com.raytheon.edex.uengine.tasks.grib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.raytheon.edex.plugin.grib.util.GribModelLookup;
import com.raytheon.edex.plugin.grib.util.GridModel;
import com.raytheon.edex.uengine.tasks.ScriptTask;
import com.raytheon.uf.common.dataplugin.grib.GribModel;
import com.raytheon.uf.common.derivparam.tree.DataTree;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * The GridCatalog script task is used to retrieve the necessary elements needed
 * to populate a grid tree. This includes center id, subcenter id, generating
 * process, parameter abbreviation, and level info.
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date			Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * Apr 10, 2008				brockwoo	Initial creation
 * May 1, 2009  2321        brockwoo    Check if a bad row out of grib model is returned
 * 
 * </pre>
 * 
 * @author brockwoo
 * @version 1.0
 */

public class GridCatalog extends ScriptTask {

    protected final transient static Log logger = LogFactory
            .getLog(GridCatalog.class);

    private static final String[] GRIDFIELDS = { "modelName",
            "parameterAbbreviation", "parameterName", "parameterUnit",
            "level.id" };

    private Long insertTime;

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.edex.uengine.tasks.ScriptTask#execute()
     */
    @Override
    public Object execute() throws EdexException {
        DataTree gridTree = null;
        CoreDao gribDao = null;
        List<?> queryResults = null;
        gribDao = new CoreDao(DaoConfig.forClass(GribModel.class));

        // if we do not get a table back, just return an empty list
        gridTree = new DataTree();
        DatabaseQuery query = new DatabaseQuery(GribModel.class.getName());
        if (insertTime != null) {
            Calendar time = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            time.setTimeInMillis(insertTime);
            query.addQueryParam("insertTime", time, ">");
        }
        List<String> distinctFields = Arrays.asList(GRIDFIELDS);

        query.addOrder(distinctFields.get(0), true);
        query.addDistinctParameter(distinctFields);
        queryResults = gribDao.queryByCriteria(query);
        if (queryResults.size() > 0) {
            for (Object gridField : queryResults) {
                if (gridField.getClass().isArray()) {
                    ArrayList<Object> gridFields = new ArrayList<Object>(Arrays
                            .asList((Object[]) gridField));
                    String model = gridFields.get(0).toString();
                    gridTree.addBranch(model, getDt(model), gridFields.get(1)
                            .toString(), gridFields.get(2).toString(),
                            gridFields.get(3).toString(), gridFields.get(4)
                                    .toString());
                }
            }
        }
        return gridTree;
    }

    private int getDt(String modelName) {
        GridModel model = GribModelLookup.getInstance().getModelByName(
                modelName);
        if (model != null && model.getDt() != null) {
            int dTinSeconds = model.getDt();

            // dT <= 24 is in hours, need to convert to seconds
            if (Math.abs(dTinSeconds) <= 24) {
                dTinSeconds *= 3600;
            }
            return dTinSeconds;
        }
        return -1;
    }

    public void setInsertTime(long time) {
        insertTime = new Long(time);
    }

}
