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
package com.raytheon.uf.common.dataplugin.gfe.dataaccess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.coverage.grid.GridGeometry2D;

import com.raytheon.uf.common.dataaccess.exception.DataRetrievalException;
import com.raytheon.uf.common.dataaccess.grid.IGridData;
import com.raytheon.uf.common.dataaccess.grid.IGridDataFactory;
import com.raytheon.uf.common.dataaccess.grid.IGridRequest;
import com.raytheon.uf.common.dataaccess.impl.AbstractGridDataPluginFactory;
import com.raytheon.uf.common.dataaccess.impl.DefaultGridData;
import com.raytheon.uf.common.dataaccess.util.DataWrapperUtil;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.DatabaseID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DFloat;
import com.raytheon.uf.common.dataplugin.gfe.slice.IGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.ScalarGridSlice;
import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataplugin.level.MasterLevel;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.geospatial.MapUtil;

/**
 * A data factory for getting gfe data from the metadata database. There are
 * currently not any required identifiers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 4, 2013            bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class GFEGridFactory extends AbstractGridDataPluginFactory implements
        IGridDataFactory {

    private static final String[] VALID_IDENTIFIERS = { GFEDataAccessUtil.MODEL_NAME,
            GFEDataAccessUtil.MODEL_TIME, GFEDataAccessUtil.SITE_ID };

    @Override
    public String[] getValidIdentifiers() {
        return VALID_IDENTIFIERS;
    }

    @Override
    protected IGridData constructGridDataResponse(IGridRequest request,
            PluginDataObject pdo, GridGeometry2D gridGeometry,
            IDataRecord dataRecord) {
        GFERecord gfeRecord = asGFERecord(pdo);

        DefaultGridData defaultGridData = new DefaultGridData(
                DataWrapperUtil.constructArrayWrapper(dataRecord, false),
                gridGeometry);
        defaultGridData.setDataTime(pdo.getDataTime());
        defaultGridData.setParameter(gfeRecord.getParmName());
        Level level = new Level();
        level.setMasterLevel(new MasterLevel(gfeRecord.getParmLevel()));
        defaultGridData.setLevel(level);
        defaultGridData.setUnit(gfeRecord.getGridInfo().getUnitObject());
        defaultGridData.setLocationName(gfeRecord.getDbId().getSiteId());

        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(GFEDataAccessUtil.MODEL_NAME, gfeRecord.getDbId()
                .getModelName());
        attrs.put(GFEDataAccessUtil.MODEL_TIME, gfeRecord.getDbId()
                .getModelTime());
        attrs.put(GFEDataAccessUtil.SITE_ID, gfeRecord.getDbId().getSiteId());

        defaultGridData.setAttributes(attrs);

        return defaultGridData;
    }

    @Override
    protected Map<String, RequestConstraint> buildConstraintsFromRequest(
            IGridRequest request) {
        HashMap<String, RequestConstraint> constraints = new HashMap<String, RequestConstraint>();

        Map<String, String> parmIdComponents = new HashMap<String, String>();
        Map<String, Object> identifiers = request.getIdentifiers();
        if (identifiers != null) {
            for (Entry<String, Object> entry : identifiers.entrySet()) {
                parmIdComponents.put(entry.getKey(), entry.getValue()
                        .toString());
            }
        }

        String[] parameters = request.getParameters();
        if (parameters != null) {
            if (parameters.length == 1) {
                parmIdComponents.put(GFEDataAccessUtil.PARM_NAME, parameters[0]);
            } else if (parameters.length > 1) {
                RequestConstraint paramNameConstraint = new RequestConstraint(
                        null, ConstraintType.IN);
                paramNameConstraint.setConstraintValueList(parameters);
                constraints.put(GFEDataAccessUtil.PARM_NAME, paramNameConstraint);
            }
        }

        Level[] levels = request.getLevels();
        if (levels != null) {
            if (levels.length == 1) {
                parmIdComponents.put(GFEDataAccessUtil.PARM_LEVEL, levels[0]
                        .getMasterLevel().getName());
            } else if (levels.length > 1) {
                RequestConstraint paramLevelConstraint = new RequestConstraint(
                        null, ConstraintType.IN);
                for (Level level : levels) {
                    paramLevelConstraint.addToConstraintValueList(level
                            .getMasterLevel().getName());
                }
                constraints.put(GFEDataAccessUtil.PARM_LEVEL, paramLevelConstraint);
            }
        }

        String[] locationNames = request.getLocationNames();
        if (locationNames != null) {
            if (locationNames.length == 1) {
                parmIdComponents.put(GFEDataAccessUtil.SITE_ID,
                        locationNames[0]);
            } else if (locationNames.length > 1) {
                RequestConstraint dbIdConstraint = new RequestConstraint(null,
                        ConstraintType.IN);
                HashSet<String> locationNamesSet = new HashSet<String>(
                        Arrays.asList(locationNames));
                DbQueryRequest dbRequest = new DbQueryRequest();
                dbRequest.addRequestField(GFEDataAccessUtil.DB_ID);
                dbRequest.setDistinct(true);
                DbQueryResponse dbResonse = executeDbQueryRequest(dbRequest,
                        request.toString());
                for (Map<String, Object> resultMap : dbResonse.getResults()) {
                    DatabaseID dbId = (DatabaseID) resultMap
                            .get(GFEDataAccessUtil.DB_ID);
                    if (locationNamesSet.contains(dbId.getSiteId())) {
                        dbIdConstraint
                                .addToConstraintValueList(dbId.toString());
                    }
                }
                constraints.put(GFEDataAccessUtil.DB_ID, dbIdConstraint);

            }
        }

        return constraints;
    }

    @Override
    protected IDataRecord getDataRecord(PluginDataObject pdo,
            Request storageRequest) {
        GFERecord gfeRecord = asGFERecord(pdo);

        try {
            IGridSlice slice = GFEDataAccessUtil
                    .getSlice(gfeRecord);
            GridLocation loc = slice.getGridInfo().getGridLoc();
            gfeRecord.setGridInfo(slice.getGridInfo());
            Grid2DFloat data = null;
            if(slice instanceof ScalarGridSlice){
                // This also grabs vector data.
                data = ((ScalarGridSlice) slice).getScalarGrid();
            } else {
                throw new DataRetrievalException("Unknown slice of type "
                        + slice.getClass().getSimpleName());
            }
            return new FloatDataRecord("Data", gfeRecord.getDataURI(), data.getFloats(), 2, new long[] {
                    loc.getNx(), loc.getNy() });
        } catch (Exception e) {
            throw new DataRetrievalException(e);
        }
    }

    @Override
    protected GridGeometry2D getGridGeometry(PluginDataObject pdo) {
        GFERecord gfeRecord = asGFERecord(pdo);
        GridParmInfo info = gfeRecord.getGridInfo();
        if (info == null) {
            try {
                info = GFEDataAccessUtil.getGridParmInfo(gfeRecord.getParmId());
            } catch (Exception e) {
                throw new DataRetrievalException(e);
            }
            gfeRecord.setGridInfo(info);
        }
        return MapUtil.getGridGeometry(info.getGridLoc());
    }

    @Override
    public GridGeometry2D getGeometry(IGridRequest request) {
        DbQueryRequest dbRequest = buildDbQueryRequest(request);
        dbRequest.setLimit(1);
        DbQueryResponse dbResonse = executeDbQueryRequest(dbRequest,
                request.toString());
        for (Map<String, Object> resultMap : dbResonse.getResults()) {
            GFERecord gfeRecord = asGFERecord(resultMap.get(null));
            return getGridGeometry(gfeRecord);
        }
        return null;
    }

    @Override
    public String[] getAvailableLocationNames(IGridRequest request) {
        DbQueryRequest dbRequest = buildDbQueryRequest(request);
        dbRequest.addRequestField(GFEDataAccessUtil.DB_ID);
        dbRequest.setDistinct(true);
        DbQueryResponse dbResonse = executeDbQueryRequest(dbRequest,
                request.toString());
        Set<String> locationNames = new HashSet<String>();
        for (Map<String, Object> resultMap : dbResonse.getResults()) {
            DatabaseID dbId = (DatabaseID) resultMap
                    .get(GFEDataAccessUtil.DB_ID);
            locationNames.add(dbId.getSiteId());
        }
        return locationNames.toArray(new String[0]);
    }

    private GFERecord asGFERecord(Object obj) {
        if (obj instanceof GFERecord == false) {
            throw new DataRetrievalException(this.getClass().getSimpleName()
                    + " cannot handle " + obj.getClass().getSimpleName());
        }

        return (GFERecord) obj;
    }

}
