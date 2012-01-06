/*
 * gov.noaa.nws.ncep.ui.pgen.elements
 * 
 * 17 May 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */


package gov.noaa.nws.ncep.ui.pgen.elements;

import gov.noaa.nws.ncep.viz.common.dbQuery.NcDirectDbQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.viz.core.catalog.DirectDbQuery.QueryLanguage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

/**
 * Class to hold county/marine zone information
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 05/10		#159		B. Yin   	Initial Creation.
 * 04/11		?			B. Yin		Read from Raytheon's tables
 *
 * </pre>
 * 
 * @author	B. Yin
 */

public class County {
	
	private static List<County> allCounties;
	private static volatile boolean countyLoaded = false; 
	
	//county name
	private String name;
	private String fips;
	private String wfo;
	private String ugcId;
	private String state;
	private String country;
	
	//for marine zone
	private String zoneName;
	private boolean marineZone;
	
	private Coordinate centriod;
	private Geometry shape;
	
	//constructor
	public County(){
		
	}
	
	//constructor
	public County( String fips,
				   String name,
				   String wfo,
				   String ugcId,
				   String state,
				   String country,
				   String zoneName,
				   Coordinate centroid,
				   Geometry shape,
				   boolean marineZone ){
		this.setFips(fips);
		this.setName(name);
		this.setWfo(wfo);
		this.setUgcId(ugcId);
		this.setState(state);
		this.setCountry(country);
		this.setZoneName(zoneName);
		this.setCentriod(centroid);
		this.setShape(shape);
		this.setMarineZone(marineZone);
		
	}

	/**
	 * Get all counties and marine zones from the database
	 * @return
	 */
	public static List<County> getAllCounties(){
		if ( !countyLoaded ){
			loadCountyTable();
		}
		
		return allCounties;
	}
	
	public static synchronized List<County> loadCountyTable(){
		
		if ( !countyLoaded ) {
			
			allCounties = new ArrayList<County>();

			List<Object[]> bnds;
			List<Object[]> stns;
			List<Object[]> zones;

			String queryStnTbl = "Select station_number, station_id, state, country FROM stns.mzcntys";
			String queryCntyBnds = "Select AsBinary(the_geom), countyname, state, fe_area, cwa, fips, lat, lon FROM mapdata.county";
			String queryZoneBnds = "Select AsBinary(the_geom), id, name, wfo, fips, lat, lon FROM mapdata.marinezones";

			try {
				bnds = NcDirectDbQuery.executeQuery(
						queryCntyBnds, "maps", QueryLanguage.SQL);
				zones = NcDirectDbQuery.executeQuery(
						queryZoneBnds, "maps", QueryLanguage.SQL);
				stns = NcDirectDbQuery.executeQuery(
						queryStnTbl, "ncep", QueryLanguage.SQL);

				WKBReader wkbReader = new WKBReader();

				int tt = 0;
				for ( Object[] bnd : bnds ){

					if ( bnd[0] != null && bnd[5] != null ) { //bounds and fips cannot be null

						//read shape
						byte[] wkb = (byte[]) bnd[0];
						Geometry cntyGeo = null;
						try {
							cntyGeo =  wkbReader.read(wkb);
						} catch (ParseException e) {
							e.printStackTrace();
						}

						//get county ugc
						if ( cntyGeo != null ){
							String cntyName = (String)bnd[1];
							String cntySt = (String)bnd[2];
							String cntyWfo = (String)bnd[4];

							if ( cntyName == null ) cntyName="";
							if ( cntySt == null ) cntySt = "";
							if ( cntyWfo == null ) cntyWfo = "";

							String cntyFips = (String)bnd[5];

							Coordinate loc = new Coordinate(0,0); 
							try {
								loc.x = ((Double)bnd[7]).doubleValue(); 
								loc.y =	((Double)bnd[6]).doubleValue();
							}
							catch ( Exception e ){
								// center location missing in database 
							}

							String cntyUgc = "";
							String cntyCountry = "";
							boolean mZone = false;
							String znName = "";

							//test for invalid county shapes
							int ii = 0;
							if ( !cntyGeo.isValid() ){
								IsValidOp vld = new IsValidOp(cntyGeo);
								TopologyValidationError err = vld.getValidationError();
								ii++;

								System.out.println("invalid county geo: " + err.getErrorType() +
										" " + cntyName + " " + cntyFips + " " + ii );

							}

							if ( cntyFips.charAt(0) == '0' ){
								cntyFips = cntyFips.substring(1);
							}

							for ( Object[] stn :stns ){
								if ( stn[0] != null && ((String)stn[0]).equalsIgnoreCase(cntyFips)){
									cntyUgc = (String)stn[1];
									cntyCountry = (String)stn[3];
								}
							}

							County existingCnty = County.findCounty( cntyFips );
							//				if ( existingCnty != null ){
							//					System.out.println( "Existing: " + existingCnty.getName() + " " + existingCnty.getState() + " " + existingCnty.getFips());
							//					System.out.println( "New:      " + cntyName + " " + cntySt + " " + cntyFips);
							//				}
							//both fips and name are same 
							if ( existingCnty != null && existingCnty.getName().equalsIgnoreCase(cntyName)  ){
								existingCnty.setShape(existingCnty.getShape().union( cntyGeo ));
							}
							else {
								County cnty = new County(cntyFips, cntyName, cntyWfo, cntyUgc, cntySt, cntyCountry,
										znName, loc, cntyGeo, mZone);
								allCounties.add(cnty);
								tt++;
							}
						}	
					}
				}

				//System.out.println("total counties: "+tt);
				for ( Object[] zn : zones ){

					if ( zn[0] != null  && zn[1] != null ){ //neither bound nor ugc can be null

						//read shape
						byte[] wkb = (byte[]) zn[0];
						Geometry zoneGeo = null;
						try {
							zoneGeo =  wkbReader.read(wkb);
						} catch (ParseException e) {
							e.printStackTrace();
						}

						//get zone f 
						if ( zoneGeo != null ){
							String ugc = (String)zn[1];
							String znName = (String)zn[2];
							String wfo = (String)zn[3];
							String cntyName = "";
							String fips = "";
							String znSt = "";
							String country = "";
							boolean mZone = true;

							Coordinate loc = new Coordinate(0,0); 
							try {
								loc.x = ((BigDecimal)zn[6]).doubleValue(); 
								loc.y = ((BigDecimal)zn[5]).doubleValue();
							}
							catch ( Exception e ){
								// center location missing in database 
							}

							if ( znName == null ) znName = "";
							if ( wfo == null ) wfo ="";
							if ( zn[4] != null ) fips = ((Integer)zn[4]).toString();
							else fips = "00000";

							//test for invalid county shapes
							int ii = 0;
							if ( !zoneGeo.isValid() ){
								IsValidOp vld = new IsValidOp(zoneGeo);
								TopologyValidationError err = vld.getValidationError();
								ii++;

								System.out.println("invalid zone geo: " + err.getErrorType() +
										" " + znName + " " + fips + " " + ii );

							}

							for ( Object[] stn :stns ){
								if ( stn[0] != null && ((String)stn[1]).equalsIgnoreCase(ugc)){
									znSt = (String)stn[2];
									country = (String)stn[3];
								}
							}

							County cnty = new County(fips, cntyName, wfo, ugc, znSt, country,
									znName, loc, zoneGeo, mZone);
							allCounties.add(cnty);

						}
					}
				}
			}
			catch (Exception e ){
				System.out.println("db exception reading county tables!");	
				e.printStackTrace();
			}
			countyLoaded = true;
		}
		return allCounties;
	}
	
	public void setFips(String fips) {
		this.fips = fips;
	}

	public String getFips() {
		return fips;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setWfo(String wfo) {
		this.wfo = wfo;
	}

	public String getWfo() {
		return wfo;
	}

	public void setUgcId(String ugcId) {
		this.ugcId = ugcId;
	}

	public String getUgcId() {
		return ugcId;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setMarineZone(boolean marineZone) {
		this.marineZone = marineZone;
	}

	public boolean isMarineZone() {
		return marineZone;
	}

	public void setCentriod(Coordinate centriod) {
		this.centriod = centriod;
	}

	public Coordinate getCentriod() {
		return centriod;
	}

	public void setShape(Geometry shape) {
		this.shape = shape;
	}

	public Geometry getShape() {
		return shape;
	}
	
	   /**
     * Get the county with the input fips.
     * @param fips
     * @return
     */
    public static County findCounty( String fips ){
    	
    	if ( allCounties == null ){
    		getAllCounties();
    	}

		for ( County cnty : allCounties ){
			if ( cnty.getFips() != null && fips.equalsIgnoreCase(cnty.getFips()) ){
				return cnty;
			}
		}
		
		return null;
    }
    
    /**
     * Get counties in the input geometry
     * @param geo
     * @return
     */
    static public List<County> getCountiesInGeometry(Geometry geo ){
    	
    	List<County> rtv = new ArrayList<County>();

		for ( int ii = 0; ii < geo.getNumGeometries(); ii++ ){
			Polygon poly = (Polygon)geo.getGeometryN(ii);
			
			for ( County county : County.getAllCounties() ){
	    			Geometry countyGeo = county.getShape();

	    			try {
	    				if (  countyGeo != null && (poly.intersects(countyGeo) ||
	    						poly.covers(countyGeo))) {
	    					rtv.add(county);
	    				}
	    			}
	    			catch (TopologyException te){
 //Calcasieu   					rtv.add(county);
	    				continue;
	    			}

			}
		}
			
    	return rtv;
    }
    
}
