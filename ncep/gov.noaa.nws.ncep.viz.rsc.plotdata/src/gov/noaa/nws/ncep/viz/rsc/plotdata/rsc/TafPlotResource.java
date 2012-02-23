package gov.noaa.nws.ncep.viz.rsc.plotdata.rsc;

import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsResource.AbstractFrameData;
import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsResource.IRscDataObject;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceName;
import gov.noaa.nws.ncep.viz.rsc.plotdata.rsc.PlotResource2.FrameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.catalog.LayerProperty;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceType;
import com.raytheon.viz.pointdata.PlotInfo;
import com.raytheon.viz.pointdata.rsc.retrieve.PointDataPlotInfoRetriever;


/**
 * Derive from PlotResource2 to handle querying for the TAF plot data
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 *  10/17/2011     #       ghull       Created 
 *  11/03/2011             sgurung     Use TafPlotInfoRetriever()
 *  11/16/2011             sgurung     Override populateFrame()
 *  12/05/2011             sgurung     Modify populateFrame() to fix a bug (calculateProgDisc being called multiple times)
 * </pre>
 * 
 * @author ghull
 * @version 1.0
 */
public class TafPlotResource extends PlotResource2 {

	public TafPlotResource(PlotResourceData data, LoadProperties props) {
		super(data, props);
		data.setPlotInfoRetriever( new TafPlotInfoRetriever() );
	}
	
	@Override
    public void initResource( IGraphicsTarget aTarget ) throws VizException {
		super.initResource(aTarget);
		
		// query all the data in the database and populate all frames.
		// 
		// taf is a forecast resource but the dataTimes in the db do not have 
		// a forecast hour. The dataTimes have a valid period for which the 
		// data is valid and the end time of the period may go out 30 hours past the 
		// refTime. If we change this to query for each frame (in populateFrame) then
		// we will need to add 30 hours to the end time. Or we could create a uengine 
		// script or otherwise do a smarter query that will only return data that is 
		// valid for this frame.
		
        HashMap<String, RequestConstraint> metadataMap = 
        				new HashMap<String, RequestConstraint>( plotRscData.getMetadataMap() );
        long t0 = System.currentTimeMillis();
        
        List<PlotInfo> plotInfoObjs = plotRscData.getPlotInfoRetriever().
        					getStations( metadataMap);
        
        long t1 = System.currentTimeMillis();
        System.out.println("InitFrame Took: " + (t1 - t0) + " To find "+ 
        		plotInfoObjs.size() + " Stations ( entries in metadata DB.)" );

        for( PlotInfo pltInfo : plotInfoObjs ) {	

        	for( IRscDataObject rscDataObj : processRecord( pltInfo )) {
        		newRscDataObjsQueue.add( rscDataObj );
        	}
        }
        
        //processNewRscDataList();
	}
	
	@Override
	protected void populateFrame( FrameData frameData ) throws VizException {
		if (!frameData.isStationMapEmpty() && frameData.getProgDiscDone()==false) {
			// frameData already populated but progressive disclosure hasn't been run 			
			frameData.calculateProgDisc();	
			frameData.setProgDiscDone(true);
		} else {		
			if (!frameData.isPopulated()) {
				// when area is changed 
				frameData.populateFrame();
				frameData.setProgDiscDone(false);
			}			
		}
    }
	
}
