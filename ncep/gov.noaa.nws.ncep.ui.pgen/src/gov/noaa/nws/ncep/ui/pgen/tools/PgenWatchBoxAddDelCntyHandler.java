/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenWatchBoxAddDelCntyHandler
 * 
 * 8 July 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import gov.noaa.nws.ncep.ui.pgen.attrDialog.WatchBoxAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.County;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.viz.ui.display.NCMapEditor;

/**
 * Mouse handler to add/delete counties when editing watch box.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 12/09		#159	 	B. Yin   	Initial Creation.
 *
 * </pre>
 * 
 * @author	B. Yin
 */
public class PgenWatchBoxAddDelCntyHandler extends InputHandlerDefaultImpl {
	
	private NCMapEditor mapEditor;
	private PgenResource drawingLayer;
	private WatchBox wb;
	private PgenWatchBoxModifyTool wbTool;

	//feature collection used to find which county a location is in
	static FeatureCollection<SimpleFeatureType,SimpleFeature> counties;
	
	/**
	 * Public constructor
	 * @param mapEditor
	 * @param drawingLayer
	 * @param watch box
	 */
	public PgenWatchBoxAddDelCntyHandler(NCMapEditor mapEditor, PgenResource drawingLayer,
			WatchBox wb, PgenWatchBoxModifyTool tool){
		
		this.mapEditor= mapEditor;
		this.drawingLayer = drawingLayer;
		this.wb = wb;
		this.wbTool = tool;
		
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int,
     *      int, int)
     */
    @Override	
    public boolean handleMouseDown(int anX, int aY, int button) {
       
    	//  Check if mouse is in geographic extent
    	Coordinate loc = mapEditor.translateClick(anX, aY);
    	if ( loc == null ) return false;
    	
    	if ( button == 1 ) {

    		//create county feature collection
    		if ( counties == null ){
    			createCountyFeatureCollection();
    		}
    		
    		GeometryFactory gf = new GeometryFactory();
    		Point click = gf.createPoint(loc);
    		
    		//apply filter
    		FeatureCollection<SimpleFeatureType,SimpleFeature> fc = counties.subCollection(createFilter(click));
    		
    		FeatureIterator<SimpleFeature> featureIterator = fc.features();

    		//find the ID of the county the location is inside of 
    		String ugc = null;
    		while (featureIterator.hasNext() ) {
    			
    			SimpleFeature f = featureIterator.next();
    			MultiPolygon mp = (MultiPolygon)f.getAttribute("Location");
    			
    			if ( mp.contains(click)) {
    				ugc = f.getID();
    				break;
    			}
    		}
    		
    		boolean gotCnty = false;
    		County county = null;
    		
    		//get the county with ID
    		if ( ugc != null ){
    			
    			List<County> cntyTbl = County.getAllCounties();

    			for ( County cnty : cntyTbl ){
    				if (  ugc.equalsIgnoreCase(cnty.getUgcId()) ){
    					gotCnty = true;
    					county = cnty;
    					break;
    				}

    			}
    		}
        	
    		boolean inList = false;
    		
    		//remove the county if it is the watch box county list,
    		//or add it into the list if it is not in
    		if ( gotCnty ){
    			
    			for ( County cnty : wb.getCountyList() ){
    				if ( ugc.equalsIgnoreCase(cnty.getUgcId() )){
    					inList = true;
    					break;
    				}
    			}

    			WatchBox newWb = (WatchBox)wb.copy();
    			newWb.setCountyList(wb.getCountyList());
    			
    			if (inList){
    				if ( ((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().isClusteringOn() ){
    					newWb.rmClstCnty(county);
    				}
    				else {
    					newWb.removeCounty(county);
    				}
    			}
    			else {
    				if ( ((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().isClusteringOn() ){
    					newWb.addClstCnty(county);
    				}
    				else {
    					newWb.addCounty(county);
    				}
    			}
    			
    			drawingLayer.replaceElement(wb, newWb);
    			drawingLayer.setSelected(newWb);
    			
    			wb = newWb;
    			
    			((WatchBoxAttrDlg)wbTool.attrDlg).setWatchBox(newWb);
    			((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().clearCwaPane();
    			((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().createCWAs(newWb.getWFOs());
    			((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().setCwaBtns();
    			((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().setStatesWFOs();

    			((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().enableAllButtons(false);
    			
    			mapEditor.refresh();
    		}
    		
    		return true;            	

    	}
        else if ( button == 3 ) {
        	
        	wbTool.resetMouseHandler();
        	((WatchBoxAttrDlg)wbTool.attrDlg).getWatchInfoDlg().enableAllButtons(true);
        	((WatchBoxAttrDlg)wbTool.attrDlg).enableDspBtn(true);
        	((WatchBoxAttrDlg)wbTool.attrDlg).buttonBar.setEnabled(true);

        	return true;
        	
        }
        else{
        	
           	return false;
           	
        }
    
    }
    
    /**
     * Create a feature collection of all counties
     */
    private void createCountyFeatureCollection(){
    	
    	counties = FeatureCollections.newCollection();

    	// create simple feature type 
    	SimpleFeatureTypeBuilder builder2 = new SimpleFeatureTypeBuilder();
    	builder2.setName( "cntyGeometry" );
    	builder2.setCRS( DefaultGeographicCRS.WGS84 );
    	builder2.add( "Location", MultiPolygon.class );
    	builder2.add( "cntyName", String.class );

    	final SimpleFeatureType POLY = builder2.buildFeatureType();

    	SimpleFeatureBuilder fbuild = new SimpleFeatureBuilder( POLY );

    	//create feature collection
    	List<County> cntyTbl = County.getAllCounties();
    	for ( County cnty : cntyTbl ){
    		if ( cnty.getShape() != null && cnty.getName() != null ){
    			Geometry countyGeo = cnty.getShape();
    			if (  countyGeo != null ){

    				fbuild.add(countyGeo);
    				fbuild.add(cnty.getName());

    				SimpleFeature feature = fbuild.buildFeature( cnty.getUgcId() );
    				counties.add(feature);
    			}
    		}
    	}

    }

    /**
     * Create a filter for the input location
     * @param loc
     * @return
     */
    private Filter createFilter(Point loc){
    	//create filter
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
				.getDefaultHints());
		
		Filter filter = ff.contains(ff.property(
				"Location"), ff.literal(loc));

        return filter;
    }

}
