/**
 * This class is to represent a map coverage area for McIDAS satellite images. It 
 * contains the geometry information necessary for client applications to correctly
 * geo-locate and project satellite imagery.
 * 
 * This class maps to the mcidas_spatial table in the postGres database via Hibernate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 10/2009      144         T. Lee      Created
 * 12/2009		144			T. Lee		Migrated to TO11D6
 * 01/2010	    201		    M. Li		Split into dataplugin project
 * 05/2010		144			L. Lin		Migration to TO11DR11.
 *    
 * </pre>
 */

package gov.noaa.nws.ncep.common.dataplugin.mcidas;

import gov.noaa.nws.ncep.common.tools.IDecoderConstantsN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.geotools.referencing.CRS;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.adapters.GeometryAdapter; 
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

@Entity
@Table(name = "mcidas_spatial")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class McidasMapCoverage extends PersistableDataObject implements ISpatialObject {

    private static final long serialVersionUID = 1;
    @Id
    private int pid;

    /**
     * The projection of the map coverage 1 = Mercator, 3 = Lambert Conformal or TANC
     * 5 = Polar Stereographic
     * 7585 = native satellite navigation e.g. GVAR, ...
     */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Integer projection;

    /** The x-dimension of the image * */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Integer nx;

    /** The y-dimension of the image * */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Integer ny;
       
    /** The pixel resolution of the image */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float dx;

    /** The line resolution of the image */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float dy;

    /** The central longitude */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float clon;

    /**
     * The standard latitude 1.  For the Lambert Conformal projection this is the latitude
     * of the proection cone intersects the earth. For the Polar Stereographic this is the 
     * latitude at which projection plan intersects the earth. For Mercator this is the 
     * latitude at which the Mercator projection cylinder intersects the earth.
     */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float stdlat1;

    /**
     * The standard latitude 2 is the second latitude of a secant cone which intersects the
     * earth for the Lambert Conformal projection. 
     */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float stdlat2;
    
    /** The latitude of the lower-left corner */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float lllat;

    /** The longitude of the lower-left corner */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float lllon;

    /** The latitude of the upper-right corner */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float urlat;

    /** The longitude of the upper-right corner */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Float urlon;
    
    /** image element coordinate of area line 0, element 0 */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private int upperLeftElement;
    
    /** image line coordinate of area line 0, element 0 */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private int upperLeftLine;
    
    /** element resolution */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private int elementRes;
    
    /** line resolution */
    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private int lineRes;
    
    @Column(length = 5120)
    @XmlAttribute
    @DynamicSerializeElement
    private String crsWKT;

    @Transient
    private CoordinateReferenceSystem crsObject;

    /** The map coverage */
    @Column(name = "the_geom", columnDefinition = "geometry")
    @Type(type = "com.raytheon.edex.db.objects.hibernate.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Polygon location;

    public McidasMapCoverage() {
        super();
    }

    /**
     * Constructs a new SatMapCoverage Object for remapped projection
     * 
     * @param projection
     * @param nx
     *            The number of horizontal scan lines
     * @param ny
     *            The number vertical scan lines
     * @param dx
     *            The horizontal resolution
     * @param dy
     *            The vertical resolution
     * @param clon
     *            The orientation of the grid
     * @param stdlat1
     *            The standard latitude 1
     * @param stdlat2
     *            The standard latitude 2
     * @param lllat
     *            The latitude of the lower-left pixel
     * @param lllon
     *            The longitude of the lower-left pixel
     * @param urlat
     *            The latitude of the upper-right pixel
     * @param urlon
     *            The longitude of the upper-right pixel
     * @param crs
     *            The coordinate reference system
     * @param geometry
     *            The geometry
     */
    public McidasMapCoverage(Integer projection, Integer nx, Integer ny, Float dx,
            Float dy, Float clon, Float stdlat1, Float stdlat2, Float lllat, Float lllon, 
            Float urlat,Float urlon, CoordinateReferenceSystem crs, Geometry geometry) {
        this.projection = projection;
        this.nx = nx;
        this.ny = ny;
        this.dx = dx;
        this.dy = dy;
        this.clon = clon;
        this.stdlat1 = stdlat1;
        this.stdlat2 = stdlat2;
        this.lllat = lllat;
        this.lllon = lllon;
        this.urlat = urlat;
        this.urlon = urlon;
        this.upperLeftElement = IDecoderConstantsN.INTEGER_MISSING;
        this.upperLeftLine = IDecoderConstantsN.INTEGER_MISSING;
        this.elementRes = IDecoderConstantsN.INTEGER_MISSING;
        this.lineRes = IDecoderConstantsN.INTEGER_MISSING;
        this.crsObject = crs;
        this.crsWKT = crsObject.toWKT();
        this.location = (Polygon) geometry;
        pid = this.hashCode();
    }

    /**
     * Constructs a new SatMapCoverage Object for native satellite navigation
     * 
     * @param mapProjection
     * @param nx The number of horizontal scan lines
     * @param ny The number vertical scan lines
     * @param reflon Reference Longitude
     * @param upperLeftElement image element coordinate of area line 0, element 0
     * @param upperLeftLine image line coordinate of area line 0, element 0
     * @param xres Element resolution
     * @param yres Line resolution
     * @param crs The coordinate reference system
     * @param geometry
     */
    public McidasMapCoverage(Integer projection, Integer nx, Integer ny,
			Float reflon, int upperLeftElement, int upperLeftLine, int xres,
			int yres, ProjectedCRS crs, Geometry geometry) {
        this.projection = projection;
        this.nx = nx;
        this.ny = ny;
        this.dx = IDecoderConstantsN.FLOAT_MISSING;
        this.dy = IDecoderConstantsN.FLOAT_MISSING;
        this.clon = reflon;
        this.stdlat1 = IDecoderConstantsN.FLOAT_MISSING;
        this.stdlat2 = IDecoderConstantsN.FLOAT_MISSING;
        this.lllat = IDecoderConstantsN.FLOAT_MISSING;
        this.lllon = IDecoderConstantsN.FLOAT_MISSING;
        this.urlat = IDecoderConstantsN.FLOAT_MISSING;
        this.urlon = IDecoderConstantsN.FLOAT_MISSING;
        this.upperLeftElement = upperLeftElement;
        this.upperLeftLine = upperLeftLine;
        this.elementRes = xres;
        this.lineRes = yres;
        this.crsObject = crs;
        this.crsWKT = crsObject.toWKT();
        this.location = (Polygon) geometry;
        pid = this.hashCode();
	}

    @Override
	public int hashCode() {
        HashCodeBuilder hashBuilder = new HashCodeBuilder();
        hashBuilder.append(projection);
        hashBuilder.append(nx);
        hashBuilder.append(ny);
        hashBuilder.append(dx);
        hashBuilder.append(dy);
        hashBuilder.append(clon);
        hashBuilder.append(stdlat1);
        hashBuilder.append(stdlat2);
        hashBuilder.append(lllat);
        hashBuilder.append(urlat);
        hashBuilder.append(lllon);
        hashBuilder.append(urlon);
        hashBuilder.append(upperLeftElement);
        hashBuilder.append(upperLeftLine);
        hashBuilder.append(elementRes);
        hashBuilder.append(lineRes);
        hashBuilder.append(crsWKT);
        return hashBuilder.toHashCode();
    }

    @Override
    public Polygon getGeometry() {
        return location;
    }

    @Override
    public CoordinateReferenceSystem getCrs() {
        if (crsObject == null) {
            try {
                crsObject = CRS.parseWKT(crsWKT);
                // ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(crsWKT); 
            } catch (Exception e) {
            	/*
            	 * parseWKT() doesn't recognize PROJCS PARAMETERS whose value is a "String" (it
            	 * assumes all PARAMETER values are doubles.)
            	 * If this crsWKT is a MCIDAS NAV, use McidasSpatialFactory instead.
            	 */
            	//e.printStackTrace();
                Pattern p = Pattern.compile("PROJCS\\[\"MCIDAS\\sAREA\\s(.*)\"");
                Matcher m = p.matcher(crsWKT);
                m.find();

                if ( m.groupCount() == 1 ) {
                	String type = m.group(1);
                	//System.out.println("FOUND PROJCS:"+m.group(0)+":"+type);
                	p = Pattern.compile("\\[\"NAV_BLOCK_BASE64\",\\s\"(.*)\"\\]");
                	m = p.matcher(crsWKT);
                	boolean found = m.find();

                	//System.out.println(m.group());
                	//System.out.println(m.groupCount()+m.group(1));
                	if ( found ) {
                		String navBlock = m.group(1);
                		crsObject = McidasSpatialFactory.getInstance().constructCRS(type, navBlock);
                	}
                	else {
                		crsObject = null;
                	}
                }
                else {
                	crsObject = null;
                }
                
            }
        }
        return crsObject;
    }

    public Float getDx() {
        return dx;
    }

    public void setDx(Float dx) {
        this.dx = dx;
    }

    public Float getDy() {
        return dy;
    }

    public void setDy(Float dy) {
        this.dy = dy;
    }

    public Float getClon() {
        return clon;
    }

    public void setClon(Float clon) {
        this.clon = clon;
    }

    public Float getStdlat1() {
        return stdlat1;
    }

    public void setStdlat1(Float stdlat1) {
        this.stdlat1 = stdlat1;
    }
    
    public Float getStdlat2() {
        return stdlat2;
    }

    public void setStdlat2(Float stdlat2) {
        this.stdlat2 = stdlat2;
    }

    public Float getLllat() {
        return lllat;
    }

    public void setLllat(Float lllat) {
        this.lllat = lllat;
    }

    public Float getLllon() {
        return lllon;
    }

    public void setLllon(Float lllon) {
        this.lllon = lllon;
    }

    public Float getUrlat() {
        return urlat;
    }

    public void setUrlat(Float urlat) {
        this.urlat = urlat;
    }

    public Float getUrlon() {
        return urlon;
    }

    public void setUrlon(Float urlon) {
        this.urlon = urlon;
    }

    public Integer getProjection() {
        return projection;
    }

    public void setProjection(Integer projection) {
        this.projection = projection;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public Integer getNx() {
        return nx;
    }

    public void setNx(Integer nx) {
        this.nx = nx;
    }

    @Override
    public Integer getNy() {
        return ny;
    }

    public void setNy(Integer ny) {
        this.ny = ny;
    }

    
    /**
	 * @return the upperLeftElement
	 */
	public int getUpperLeftElement() {
		return upperLeftElement;
	}

	/**
	 * @param upperLeftElement the upperLeftElement to set
	 */
	public void setUpperLeftElement(int upperLeftElement) {
		this.upperLeftElement = upperLeftElement;
	}

	/**
	 * @return the upperLeftLine
	 */
	public int getUpperLeftLine() {
		return upperLeftLine;
	}

	/**
	 * @param upperLeftLine the upperLeftLine to set
	 */
	public void setUpperLeftLine(int upperLeftLine) {
		this.upperLeftLine = upperLeftLine;
	}

	/**
	 * @return the elementRes
	 */
	public int getElementRes() {
		return elementRes;
	}

	/**
	 * @param elementRes the elementRes to set
	 */
	public void setElementRes(int elementRes) {
		this.elementRes = elementRes;
	}

	/**
	 * @return the lineRes
	 */
	public int getLineRes() {
		return lineRes;
	}

	/**
	 * @param lineRes the lineRes to set
	 */
	public void setLineRes(int lineRes) {
		this.lineRes = lineRes;
	}

	public String getCrsWKT() {
        return crsWKT;
    }

    public void setCrsWKT(String crsWKT) {
    	//TODO new 2.6 version of geotools adds \r\n to long String parameters in WKT format
    	// this temp hack removes the extraneous characters, but we may want to investigate
    	// using a specific formatter to keep this consistent and in our control
        this.crsWKT = crsWKT.replaceAll("\r\n", "");
        //this.crsWKT = crsWKT;
    }

    public Polygon getLocation() {
        return location;
    }

    public void setLocation(Polygon location) {
        this.location = location;
    }
}
