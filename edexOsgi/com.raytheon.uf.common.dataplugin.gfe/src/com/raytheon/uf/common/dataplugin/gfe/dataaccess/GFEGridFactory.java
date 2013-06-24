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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.coverage.grid.GridGeometry2D;

import com.raytheon.uf.common.dataaccess.IDataFactory;
import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.exception.DataRetrievalException;
import com.raytheon.uf.common.dataaccess.grid.IGridData;
import com.raytheon.uf.common.dataaccess.impl.AbstractGridDataPluginFactory;
import com.raytheon.uf.common.dataaccess.impl.DefaultGridData;
import com.raytheon.uf.common.dataaccess.util.DataWrapperUtil;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DByte;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DFloat;
import com.raytheon.uf.common.dataplugin.gfe.grid.IGrid2D;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.IGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.ScalarGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.slice.WeatherGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.weather.WeatherKey;
import com.raytheon.uf.common.dataplugin.level.Level;
import com.raytheon.uf.common.dataplugin.level.MasterLevel;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.util.StringUtil;

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
 * Feb 14, 2013 1614       bsteffen    Refactor data access framework to use
 *                                     single request.
 * May 02, 2013 1949       bsteffen    Update GFE data access in Product
 *                                     Browser, Volume Browser, and Data Access
 *                                     Framework.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class GFEGridFactory extends AbstractGridDataPluginFactory implements
        IDataFactory {

    public static final String MODEL_TIME = "modelTime";

    private static final String MODEL_NAME = "modelName";

    private static final String SITE_ID = "siteId";

    private static final String KEYS = "keys";

    // The more full version from GFEDataAccessUtil is prefered but the smaller
    // keys are needed for backwards compatibility.
    private static final String[] VALID_IDENTIFIERS = { MODEL_NAME, MODEL_TIME,
            SITE_ID };

    @Override
    public String[] getValidIdentifiers() {
        return VALID_IDENTIFIERS;
    }

    @Override
    protected IGridData constructGridDataResponse(IDataRequest request,
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
        attrs.put(MODEL_NAME, gfeRecord.getDbId().getModelName());
        attrs.put(MODEL_TIME, gfeRecord.getDbId().getModelTime());
        attrs.put(SITE_ID, gfeRecord.getDbId().getSiteId());
        if (dataRecord.getDataAttributes().containsKey(KEYS)) {
            attrs.put(KEYS, StringUtil.join((String[]) dataRecord
                    .getDataAttributes().get(KEYS), ','));
        }
        defaultGridData.setAttributes(attrs);

        return defaultGridData;
    }

    @Override
    protected Map<String, RequestConstraint> buildConstraintsFromRequest(
            IDataRequest request) {
        HashMap<String, RequestConstraint> constraints = new HashMap<String, RequestConstraint>();

        Map<String, Object> identifiers = request.getIdentifiers();
        if (identifiers != null) {
            for (Entry<String, Object> entry : identifiers.entrySet()) {
                if (entry.getKey().equals(MODEL_NAME)) {
                    constraints.put(GFEDataAccessUtil.MODEL_NAME,
                            new RequestConstraint(entry.getValue().toString()));
                } else if (entry.getKey().equals(SITE_ID)) {
                    constraints.put(GFEDataAccessUtil.SITE_ID,
                            new RequestConstraint(entry.getValue().toString()));
                } else if (entry.getKey().equals(MODEL_TIME)) {
                    constraints.put(GFEDataAccessUtil.MODEL_TIME,
                            new RequestConstraint(entry.getValue().toString()));
                } else {
                    constraints.put(entry.getKey(), new RequestConstraint(entry
                            .getValue().toString()));
                }
            }
        }

        String[] parameters = request.getParameters();
        if (parameters != null) {
            if (parameters.length == 1) {
                constraints.put(GFEDataAccessUtil.PARM_NAME,
                        new RequestConstraint(parameters[0]));
            } else if (parameters.length > 1) {
                RequestConstraint paramNameConstraint = new RequestConstraint(
                        null, ConstraintType.IN);
                paramNameConstraint.setConstraintValueList(parameters);
                constraints.put(GFEDataAccessUtil.PARM_NAME,
                        paramNameConstraint);
            }
        }

        Level[] levels = request.getLevels();
        if (levels != null) {
            if (levels.length == 1) {
                constraints.put(GFEDataAccessUtil.PARM_LEVEL,
                        new RequestConstraint(levels[0].getMasterLevel()
                                .getName()));
            } else if (levels.length > 1) {
                RequestConstraint paramLevelConstraint = new RequestConstraint(
                        null, ConstraintType.IN);
                for (Level level : levels) {
                    paramLevelConstraint.addToConstraintValueList(level
                            .getMasterLevel().getName());
                }
                constraints.put(GFEDataAccessUtil.PARM_LEVEL,
                        paramLevelConstraint);
            }
        }

        String[] locationNames = request.getLocationNames();
        if (locationNames != null) {
            if (locationNames.length == 1) {
                constraints.put(GFEDataAccessUtil.SITE_ID,
                        new RequestConstraint(locationNames[0]));
            } else if (locationNames.length > 1) {
                RequestConstraint siteConstraint = new RequestConstraint(null,
                        ConstraintType.IN);
                siteConstraint.setConstraintValueList(locationNames);
                constraints.put(GFEDataAccessUtil.SITE_ID, siteConstraint);
            }
        }

        return constraints;
    }

    @Override
    protected IDataRecord getDataRecord(PluginDataObject pdo) {
        GFERecord gfeRecord = asGFERecord(pdo);

        try {
            IGridSlice slice = GFEDataAccessUtil.getSlice(gfeRecord);
            GridLocation loc = slice.getGridInfo().getGridLoc();
            gfeRecord.setGridInfo(slice.getGridInfo());
            IGrid2D data = null;
            Map<String, Object> attrs = new HashMap<String, Object>();
            if (slice instanceof ScalarGridSlice) {
                // This also grabs vector data.
                data = ((ScalarGridSlice) slice).getScalarGrid();
                return new FloatDataRecord("Data", gfeRecord.getDataURI(),
                        ((Grid2DFloat) data).getFloats(), 2, new long[] {
                                loc.getNx(), loc.getNy() });
            } else if (slice instanceof DiscreteGridSlice) {
                DiscreteGridSlice castedSlice = (DiscreteGridSlice) slice;
                data = castedSlice.getDiscreteGrid();
                DiscreteKey[] dKeys = castedSlice.getKey();
                String[] keys = new String[dKeys.length];
                for (int i = 0; i < dKeys.length; i++) {
                    keys[i] = dKeys[i].toString();
                }
                byte[] bytes = ((Grid2DByte) data).getBytes();
                ByteDataRecord record = new ByteDataRecord("Data",
                        gfeRecord.getDataURI(), bytes, 2, new long[] {
                                loc.getNx(), loc.getNy() });
                attrs.put(KEYS, keys);
                record.setDataAttributes(attrs);
                return record;
            } else if (slice instanceof WeatherGridSlice) {
                WeatherGridSlice castedSlice = (WeatherGridSlice) slice;
                data = castedSlice.getWeatherGrid();
                WeatherKey[] wKeys = castedSlice.getKeys();
                String[] keys = new String[wKeys.length];
                for (int i = 0; i < wKeys.length; i++) {
                    keys[i] = wKeys[i].toString();
                }
                byte[] bytes = ((Grid2DByte) data).getBytes();
                ByteDataRecord record = new ByteDataRecord("Data",
                        gfeRecord.getDataURI(), bytes, 2, new long[] {
                                loc.getNx(), loc.getNy() });
                attrs.put(KEYS, keys);
                record.setDataAttributes(attrs);
                return record;
            } else {
                throw new DataRetrievalException("Unknown slice of type "
                        + slice.getClass().getSimpleName());
            }
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
    public String[] getAvailableLocationNames(IDataRequest request) {
        DbQueryRequest dbRequest = buildDbQueryRequest(request);
        dbRequest.addRequestField(GFEDataAccessUtil.SITE_ID);
        dbRequest.setDistinct(true);
        DbQueryResponse dbResonse = executeDbQueryRequest(dbRequest,
                request.toString());

        return dbResonse.getFieldObjects(GFEDataAccessUtil.SITE_ID,
                String.class);
    }

    private GFERecord asGFERecord(Object obj) {
        if (obj instanceof GFERecord == false) {
            throw new DataRetrievalException(this.getClass().getSimpleName()
                    + " cannot handle " + obj.getClass().getSimpleName());
        }

        return (GFERecord) obj;
    }

}
