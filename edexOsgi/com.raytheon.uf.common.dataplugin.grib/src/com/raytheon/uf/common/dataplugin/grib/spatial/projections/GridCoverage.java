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

package com.raytheon.uf.common.dataplugin.grib.spatial.projections;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.geotools.coverage.grid.GridGeometry2D;
import org.hibernate.annotations.Type;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.raytheon.uf.common.dataplugin.grib.exception.GribException;
import com.raytheon.uf.common.dataplugin.grib.subgrid.SubGrid;
import com.raytheon.uf.common.dataplugin.grib.subgrid.SubGridDef;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.geospatial.CRSCache;
import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Base class for encapsulating grib spatial information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 4/7/09       1994        bphillip    Initial Creation
 * 09/10/2012   DR 15270    D. Friedman Fix subgrid model name handling.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public abstract class GridCoverage extends PersistableDataObject implements
        ISpatialObject {

    private static final long serialVersionUID = -1355232934065074837L;
    
    protected static final String SUBGRID_TOKEN = "-SubGrid-";

    /** The id for this grid. This value is generated in the initialize method **/
    @Id
    @DynamicSerializeElement
    protected int id;

    /** The name of the grid */
    @Column(length = 2047)
    @XmlElement
    @DynamicSerializeElement
    protected String name;

    /** A description of the grid coverage */
    @Column(length = 3071)
    @XmlElement
    @DynamicSerializeElement
    protected String description;

    /** Geometry object holding the corner points of the grid */
    @Column(name = "the_geom", columnDefinition = "geometry")
    @Type(type = "com.raytheon.edex.db.objects.hibernate.GeometryType")
    @DynamicSerializeElement
    protected Geometry geometry;

    /** The CRS as a WKT String */
    @Column(name = "crs", length = 2047)
    @DynamicSerializeElement
    protected String crsWKT;

    /** Latitude of first grid point */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected double la1;

    /** Longitude of the first grid point */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected double lo1;

    /** Latitude of the lower left grid point */
    @Transient
    protected Double lowerLeftLat;

    /** Longitude of the lower left grid point */
    @Transient
    protected Double lowerLeftLon;

    /** Corner of the first grid point */
    @XmlElement
    @Column
    @DynamicSerializeElement
    @Enumerated(EnumType.STRING)
    protected Corner firstGridPointCorner;

    /** The CRS object */
    @Transient
    protected CoordinateReferenceSystem crs;

    /** The Grid geometry */
    @Transient
    protected transient GridGeometry2D gridGeometry;

    /** Number of points along I direction */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected Integer nx;

    /** Number of points along J direction */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected Integer ny;

    /** I direction increment */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected double dx;

    /** J direction increment */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected double dy;

    /** Spacing unit of dx and dy */
    @Column
    @XmlElement
    @DynamicSerializeElement
    protected String spacingUnit;

    /** The scan mode. */
    @Transient
    @XmlElement(required = false)
    protected Integer scanMode;

    /** The resolution. */
    @Transient
    @XmlElement(required = false)
    protected Integer resolution;

    /** If a data point for the pole is included, either row or column. */
    @Transient
    @XmlElement(required = false)
    protected String includePole;

    /**
     * Creates an empty GridCoverage object
     */
    protected GridCoverage() {

    }

    @Override
    public String toString() {
        return "Coverage Information Not Specified yet";
    }

    @Override
    public int hashCode() {
        return generateHash();
    }

    /**
     * Generates a hash code based on selected fields in the grid coverage
     * object. The fields used will vary among different projections.
     * 
     * @return The hash code generated from selected fields in the object
     */
    public int generateHash() {
        HashCodeBuilder hashBuilder = new HashCodeBuilder();
        if (getName() == null) {
            generateName();
        }
        hashBuilder.append(name);
        hashBuilder.append(nx);
        hashBuilder.append(ny);
        hashBuilder.append(dx);
        hashBuilder.append(dy);
        hashBuilder.append(la1);
        hashBuilder.append(lo1);
        // enum hashCode is unstable, use StringRep
        hashBuilder.append(firstGridPointCorner.name());
        return hashBuilder.toHashCode();
    }

    /**
     * Initializes the grib coverage object. Initialization should entail
     * creation of the crs and geometry object as well as assigning the id field
     * 
     * @throws GribException
     *             If problems occur while creating the crs, geometry, or the id
     */
    public abstract void initialize() throws GribException;

    /**
     * Gets the name of the projection. The projection type is specified by each
     * subclass and accessed through this method.
     * 
     * @return The name/type of the projection
     */
    public abstract String getProjectionType();

    /**
     * If this grid coverage object describes a grid that is not predefined,
     * this method is used to generate and assign a descriptive name.
     */
    public abstract void generateName();

    /**
     * Trim this GridCoverage to a sub grid.
     * 
     * @param subGridDef
     * @param subGrid
     * @return trimmed coverage
     */
    public abstract GridCoverage trim(SubGridDef subGridDef, SubGrid subGrid);

    public Geometry getGeometry() {
        return geometry;
    }

    public CoordinateReferenceSystem getCrs() {
        if (crs == null) {
            try {
                this.crs = CRSCache.getInstance().getCoordinateReferenceSystem(
                        crsWKT);
            } catch (FactoryException e) {
                this.crs = null;
            }
        }
        return crs;
    }

    /**
     * Gets the id
     * 
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id
     * 
     * @param id
     *            The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     * 
     * @param name
     *            The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the geometry
     * 
     * @param geometry
     *            The geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Sets the CRS object
     * 
     * @param crs
     *            The crs object
     */
    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Gets the CRS WKT object
     * 
     * @return The CRS WKT object
     */
    public String getCrsWKT() {
        return crsWKT;
    }

    /**
     * Sets the CRS WKT object
     * 
     * @param crsWKT
     */
    public void setCrsWKT(String crsWKT) {
        this.crsWKT = crsWKT;

    }

    /**
     * Gets the description
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     * 
     * @param description
     *            The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public GridGeometry2D getGridGeometry() {
        if (gridGeometry == null) {
            gridGeometry = MapUtil.getGridGeometry(this);
        }

        return gridGeometry;
    }

    public void setGridGeometry(GridGeometry2D gridGeometry) {
        this.gridGeometry = gridGeometry;
    }

    public double getLa1() {
        return la1;
    }

    public void setLa1(double la1) {
        this.la1 = la1;
    }

    public double getLo1() {
        return lo1;
    }

    public void setLo1(double lo1) {
        this.lo1 = lo1;
    }

    public Double getLowerLeftLat() throws GribException {
        if (lowerLeftLat == null) {
            generateLowerLeft();
        }
        return lowerLeftLat;
    }

    public Double getLowerLeftLon() throws GribException {
        if (lowerLeftLon == null) {
            generateLowerLeft();
        }
        return lowerLeftLon;
    }

    public Corner getFirstGridPointCorner() {
        return firstGridPointCorner;
    }

    public void setFirstGridPointCorner(Corner firstGridPointCorner) {
        this.firstGridPointCorner = firstGridPointCorner;
    }

    public Integer getNx() {
        return nx;
    }

    public void setNx(Integer nx) {
        this.nx = nx;
    }

    public Integer getNy() {
        return ny;
    }

    public void setNy(Integer ny) {
        this.ny = ny;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public String getSpacingUnit() {
        return spacingUnit;
    }

    public void setSpacingUnit(String spacingUnit) {
        this.spacingUnit = spacingUnit;
    }

    public Integer getScanMode() {
        return scanMode;
    }

    public void setScanMode(Integer scanMode) {
        this.scanMode = scanMode;
    }

    public Integer getResolution() {
        return resolution;
    }

    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    public String getIncludePole() {
        return includePole;
    }

    public void setIncludePole(String includePole) {
        this.includePole = includePole;
    }

    /**
     * 
     */
    protected void generateLowerLeft() throws GribException {
        try {
            if ("degree".equals(spacingUnit)) {
                switch (firstGridPointCorner) {
                case LowerLeft: {
                    lowerLeftLat = la1;
                    lowerLeftLon = lo1;
                    break;
                }
                case UpperLeft: {
                    lowerLeftLat = la1 - dy * (ny - 1);
                    lowerLeftLon = lo1;
                    break;
                }
                case UpperRight: {
                    lowerLeftLat = la1 - dy * (ny - 1);
                    lowerLeftLon = lo1 - dx * (nx - 1);
                    break;
                }
                case LowerRight: {
                    lowerLeftLat = la1;
                    lowerLeftLon = lo1 - dx * (nx - 1);
                    break;
                }
                }
            } else if (Corner.LowerLeft.equals(firstGridPointCorner)) {
                lowerLeftLat = la1;
                lowerLeftLon = lo1;
            } else {
                if (getCrs() == null) {
                    throw new GribException("CRS is null.");
                }

                Unit<?> spacingUnitObj = Unit.valueOf(spacingUnit);
                if (spacingUnitObj.isCompatible(SI.METRE)) {
                    UnitConverter converter = spacingUnitObj
                            .getConverterTo(SI.METRE);
                    double dxMeter = converter.convert(dx);
                    double dyMeter = converter.convert(dy);
                    MathTransform fromLatLon = MapUtil
                            .getTransformFromLatLon(getCrs());
                    MathTransform toLatLon = fromLatLon.inverse();
                    double[] lonLat = { lo1, la1 };
                    double[] lonLatInMeters = new double[2];
                    fromLatLon.transform(lonLat, 0, lonLatInMeters, 0, 1);

                    switch (firstGridPointCorner) {
                    case LowerLeft: {
                        // LL point is already correct
                        break;
                    }
                    case UpperLeft: {
                        // longitude point is already correct
                        lonLatInMeters[1] -= (ny - 1) * dyMeter;
                        break;
                    }
                    case UpperRight: {
                        lonLatInMeters[0] -= (nx - 1) * dxMeter;
                        lonLatInMeters[1] -= (ny - 1) * dyMeter;
                        break;
                    }
                    case LowerRight: {
                        // latitude point is already correct
                        lonLatInMeters[0] -= (nx - 1) * dxMeter;
                        break;
                    }
                    }
                    toLatLon.transform(lonLatInMeters, 0, lonLat, 0, 1);
                    lowerLeftLon = lonLat[0];
                    lowerLeftLat = lonLat[1];
                } else {
                    throw new GribException("Cannot converter " + spacingUnit
                            + " to meters");
                }
            }
        } catch (Exception e) {
            throw new GribException(
                    "Cannot determine LowerLeft and UpperRight points of grid",
                    e);
        }
        lowerLeftLon = MapUtil.correctLon(lowerLeftLon);
        lowerLeftLat = MapUtil.correctLat(lowerLeftLat);
    }

    protected void generateGeometry() throws GribException {
        if ("degree".equals(spacingUnit)) {
            // lower left is cell center, we want cell corners.
            double minLat = MapUtil.correctLat(getLowerLeftLat() - dy / 2);
            double maxLat = minLat + dy * ny;
            double minLon = getLowerLeftLon() - dx / 2;
            if (dx * nx <= 360) {
                // Do not correct lon if larger than worldwide, most notably the
                // grid range for ECMWF-LowRes goes from -181.25 to 181.25 but
                // if you correct you end up at 178.75 to 538.75 which doesn't
                // work very well
                minLon = MapUtil.correctLon(minLon);
            }
            double maxLon = minLon + dx * nx;
            if (dx * nx == 360) {
                // Grids that wrap around the world need to be shrunk slightly
                // to account for inaccuracies when converting between degrees
                // and radians which can result in an invalid envelope.
                maxLon -= 1.0E-12;
                minLon += 1.0E-12;
            }
            try {
                geometry = MapUtil.createGeometry(minLat, minLon, maxLat,
                        maxLon);
            } catch (Exception e) {
                throw new GribException("Error creating geometry", e);
            }
        } else {
            try {
                Unit<?> spacingUnitObj = Unit.valueOf(spacingUnit);
                if (spacingUnitObj.isCompatible(SI.METRE)) {
                    UnitConverter converter = spacingUnitObj
                            .getConverterTo(SI.METRE);
                    geometry = MapUtil.createGeometry(crs, getLowerLeftLat(),
                            getLowerLeftLon(), converter.convert(dx),
                            converter.convert(dy), nx, ny);
                } else {
                    throw new GribException("Unable to convert " + spacingUnit
                            + " to meters while creating geometry!");
                }
            } catch (Exception e) {
                throw new GribException("Error creating geometry", e);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GridCoverage)) {
            return false;
        }
        GridCoverage other = (GridCoverage) obj;
        if (Double.doubleToLongBits(dx) != Double.doubleToLongBits(other.dx)) {
            return false;
        }
        if (Double.doubleToLongBits(dy) != Double.doubleToLongBits(other.dy)) {
            return false;
        }
        if (Double.doubleToLongBits(la1) != Double.doubleToLongBits(other.la1)) {
            return false;
        }
        if (Double.doubleToLongBits(lo1) != Double.doubleToLongBits(other.lo1)) {
            return false;
        }
        if (nx == null) {
            if (other.nx != null) {
                return false;
            }
        } else if (!nx.equals(other.nx)) {
            return false;
        }
        if (ny == null) {
            if (other.ny != null) {
                return false;
            }
        } else if (!ny.equals(other.ny)) {
            return false;
        }
        if (spacingUnit == null) {
            if (other.spacingUnit != null) {
                return false;
            }
        } else if (!spacingUnit.equals(other.spacingUnit)) {
            return false;
        }
        if (firstGridPointCorner != other.firstGridPointCorner) {
            return false;
        }
        return true;
    }

    public void determineFirstGridPointCorner(int scanMode) {
        if ((scanMode & 128) > 0) {
            // -i
            if ((scanMode & 64) > 0) {
                // +j
                setFirstGridPointCorner(Corner.LowerRight);
            } else {
                // -j
                setFirstGridPointCorner(Corner.UpperRight);
            }
        } else {
            // +i
            if ((scanMode & 64) > 0) {
                // +j
                setFirstGridPointCorner(Corner.LowerLeft);
            } else {
                // -j
                setFirstGridPointCorner(Corner.UpperLeft);
            }

        }
    }

    public GridCoverage(GridCoverage coverage) {
        this.id = coverage.id;
        this.name = coverage.name;
        this.description = coverage.description;
        this.geometry = coverage.geometry;
        this.crsWKT = coverage.crsWKT;
        this.la1 = coverage.la1;
        this.lo1 = coverage.lo1;
        this.lowerLeftLat = coverage.lowerLeftLat;
        this.lowerLeftLon = coverage.lowerLeftLon;
        this.firstGridPointCorner = coverage.firstGridPointCorner;
        this.crs = coverage.crs;
        this.gridGeometry = coverage.gridGeometry;
        this.nx = coverage.nx;
        this.ny = coverage.ny;
        this.dx = coverage.dx;
        this.dy = coverage.dy;
        this.spacingUnit = coverage.spacingUnit;
        this.scanMode = coverage.scanMode;
        this.resolution = coverage.resolution;
        this.includePole = coverage.includePole;
    }

    /**
     * Determines if this coverage is a subGrid.
     * 
     * @return
     */
    public boolean isSubGridded() {
        String subGridName = getName();

        if (subGridName != null) {
            return getName().contains(SUBGRID_TOKEN);
        }

        return false;
    }

    /**
     * If this coverage is subGridded, return the model it is a subGrid for,
     * else null.
     * 
     * @return
     */
    public String getSubGridModel() {
        String model = null;
        if (isSubGridded()) {
            String subGridName = getName();
            int index = subGridName.lastIndexOf(SUBGRID_TOKEN);
            if (index >= 0 && index + SUBGRID_TOKEN.length() < subGridName.length()) {
                model = subGridName.substring(index + SUBGRID_TOKEN.length());
            }
        }
        return model;
    }

    public String getParentGridName() {
        String parentName = null;
        if (isSubGridded()) {
            String subGridName = getName();
            int index = subGridName.indexOf(SUBGRID_TOKEN);
            if (index >= 0) {
                parentName = subGridName.substring(0, index);
            }
        }
        return parentName;
    }
}
