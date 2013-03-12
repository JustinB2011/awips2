package gov.noaa.nws.ncep.viz.rsc.solarimage.wcs;

import gov.noaa.nws.ncep.viz.rsc.solarimage.util.HeaderData;

/**
 * Provides utility methods for Coordinate conversions.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer        Description
 * ------------ ---------- --------------- --------------------------
 * 02/21/2013   958        qzhou, sgurung   Initial creation.
 * 
 * </pre>
 * 
 * @author qzhou
 * @version 1.0
 */


public class CSConversions {
	private int dim = 0;
    public HeaderData headerData;
       
    public CSConversions(HeaderData headerData) {
		
		this.headerData = headerData;
		dim = headerData.getDim();
		
	}
	
    public double[] heliocentricToHeliographic(double[] cs, boolean isCarrington) {
    	
    	/*		
		Equations used: 
		r2= x2 + y 2 + z2 , 
		Θ = sin−1 ((y cos B0 + z sin B0 )/r), 
		Φ = Φ0 + arg(z cos B0 − y sin B0 , x),   Where arg( x, y) = tan−1 (y/ x) .*/
				
    	double[] helio = new double[dim];
		double r = headerData.getRsun();		
			
		double z = (double) Math.sqrt(r*r -cs[0]*cs[0] -cs[1]*cs[1]); 
		double b0 = headerData.getHglt();
		double a0 = headerData.getHgln();
		double crln = headerData.getCrln();
		double L0 = 0.0;
		
		if (!headerData.isStereo()) {
			b0 = headerData.getL0B0()[1];
		}
				
		if (isCarrington) {
			
			if (!headerData.isStereo()) {
				L0 = headerData.getL0B0()[0];
			} 
			else {
				L0 = crln - a0;
			}
		}
		//System.out.println(" heliocentricToHeliographic() ***  L0 = " +L0 +" crln = "+ crln+" b0 = "+ b0);
		double temp = (double) ((cs[1] * Math.cos(Math.toRadians(b0)) + z * Math.sin(Math.toRadians(b0)) )/ r);
		helio[1] = (double) Math.asin(temp);
		helio[1] = Math.toDegrees(helio[1]);
		
		temp = (double) ((cs[0]) / ( z * Math.cos(Math.toRadians(b0)) - cs[1] * Math.sin(Math.toRadians(b0)) ));
		helio[0] = (double) (Math.toRadians(a0) + Math.atan(temp));
		helio[0] = Math.toDegrees(helio[0]) + L0;		
		
		return helio;
	}

	public double[] heliographicToHeliocentric(double[] cs, boolean isCarrington) {

		/*		
		Equations used: 	
		centric[0] = r cos Θ sin(Φ − Φ0 ), 
		centric[1] = r[sin Θ cos B0 − cos Θ cos(Φ − Φ0 ) sin B0 ], 
		z = r[sin Θ sin B0 + cos Θ cos(Φ − Φ0 ) cos B0 ], */
		
		double[] centric = new double[dim];
		double r = headerData.getRsun();
		double b0 = headerData.getHglt();
		double a0 = headerData.getHgln();		
		double crln = headerData.getCrln();			
		double L0 = 0.0;
		
		if (!headerData.isStereo()) {
			b0 = -headerData.getL0B0()[1];
		}
				
		if (isCarrington) {
			
			if (!headerData.isStereo()) {
				L0 = headerData.getL0B0()[0];
			} 
			else {
				L0 = crln - a0;
			}
			
			cs[0] = cs[0] - L0; //change to stony
		}		
		
		//System.out.println(" heliographicToHeliocentric() ***  L0 = " +L0 +" crln = "+ crln+" b0 = "+ b0 + " B0 = " + headerData.getL0B0()[1]);
		centric[0] = r* Math.cos(Math.toRadians( cs[1])) * Math.sin(Math.toRadians( cs[0]-a0));
		centric[1] = r* ( Math.sin(Math.toRadians( cs[1])) *Math.cos(Math.toRadians(b0)) - Math.cos(Math.toRadians( cs[1]))*Math.cos(Math.toRadians(cs[0]-a0))*Math.sin(Math.toRadians(b0) ));
		
		return centric;
	}
	
	public double[] helioprojectiveToHeliocentric(double[] cs) {
		/*		
		Equations used: 		
		z = D − d cos θy cos θ x ,
		x =  D ( PI/180) θ x, (approximate)
		y =  D ( PI/180) θ y.*/
		
		double[] centric = new double[dim];
		double d = headerData.getDsun(); 
		centric[0] = (double) (d * Math.cos( Math.toRadians( cs[1]/3600)) * Math.sin( Math.toRadians( cs[0]/3600)));
		centric[1] = (double) (d * Math.sin( Math.toRadians( cs[1]/3600))); 
		
		return centric;
	}
	
	public double[] heliocentricToHelioprojective(double[] cs) {
		
		/*
		Equations used: 
		d= x2 + y2 + (D − z)2 , 
		θ x = arg(D − z, x), 
		θy = sin−1 (y/d). */		
		
		double[] projective = new double[dim];
		double d = headerData.getDsun();
		double d0 = (double) Math.sqrt(d*d -cs[0]*cs[0] -cs[1]*cs[1]); 		
		projective[0] = Math.atan(cs[0] / d0);
		projective[1] = (double) Math.asin(cs[1] / d); 
		projective[0] = Math.toDegrees(projective[0]);
		projective[1] = Math.toDegrees(projective[1]);
		
		return projective;
	}
	
	public HeaderData getHeaderData() {
		
		return headerData;
		
	}
}
