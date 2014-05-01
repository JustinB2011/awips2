package gov.noaa.nws.ncep.edex.plugin.geomag.calculation;

import gov.noaa.nws.ncep.common.dataplugin.geomag.table.KsThree;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * The calculation of Kp and related.
 * 
 * <pre>
 * SOFTWARE HISTORY
 *                   
 * ate          Ticket#     Engineer   Description
 * -----------  ----------  ---------- --------------------------
 * 05/14/2013   #989        qzhou      Initial Creation
 * </pre>
 * 
 * @author qzhou
 * @version 1
 */
public class CalcKp {
	private static final float MISSING_VAL = 99999.99f;
	
	public CalcKp() {

	}
	
	public static float[] getKest(String station, float[] kIndex, float[] gamma) {
		float[] kest = new float[8];		
		
		for (int i=0; i<8; i++) {
			int[] gammaLimit = CalcUtil.getKLimit(station); //.getGammaFromK(station,  gamma); 
			if (kIndex[i] < 9) { 
				kest[i] = kIndex[i] + (gamma[i] - gammaLimit[(int)kIndex[i]]) / (gammaLimit[(int)kIndex[i]+1] - gammaLimit[(int)kIndex[i]]);}
			else if (kIndex[i] == MISSING_VAL)
				kest[i] = MISSING_VAL;
			else 
				kest[i] = 9.0f;
			//System.out.println("**kest "+kest[i] +" "+kIndex[i]+" "+gamma[i]);
		}
		
		return kest;
	}
	
	public static float getKest(String station, int kIndex, float gamma) {
		float kest = 0;		
				
		int[] gammaLimit = CalcUtil.getKLimit(station); 
		if (kIndex < 9) 
			kest = kIndex + (gamma - gammaLimit[kIndex])/(gammaLimit[kIndex+1] - gammaLimit[kIndex]);
		else if (kIndex == MISSING_VAL)
			kest = MISSING_VAL;
		else
			kest = 9.0f;
//		System.out.println("****kest "+kest +" "+kIndex);
			
		return kest;
	}
//	public static float getKest(String station,int[] kLimit, float gamma) {
//		float kest = 0;		
//		int kIndex = CalcUtil.getKfromTable(kLimit, gamma);		
//		
//		int gammaLimit = CalcUtil.getGammaFromK(station, kIndex+1); //kLimit[kIndex]; //getGammaLimit( station, kIndex);
//		int gammaLimit1 = CalcUtil.getGammaFromK(station, kIndex+1); //kLimit[kIndex+1]; //getKLimit( station, kIndex+1);;
//		
//		if (gammaLimit1 > gammaLimit)
//			kest = kIndex + (gamma - gammaLimit) / (gammaLimit1 - gammaLimit);
//		
//		return kest;
//	}
//	protected float[] getKest(String station, float[] gamma) {
//		float kest = 0;		
//		int kIndex[] = CalcUtil.getKfromTable(station, gamma);		
//		
//		int gammaLimit = CalcUtil.getGammaFromK(station, kIndex+1); //kLimit[kIndex]; //getGammaLimit( station, kIndex);
//		int gammaLimit1 = CalcUtil.getGammaFromK(station, kIndex+1); //kLimit[kIndex+1]; //getKLimit( station, kIndex+1);;
//		
//		if (gammaLimit1 > gammaLimit)
//			kest = kIndex + (gamma - gammaLimit) / (gammaLimit1 - gammaLimit);
//		
//		return kest;
//	}
	/*
	 * list of the station coefficient values in the order of 00-03, 03-06...
	 */
	public static ArrayList<KsThree> getKsThreeList(String station) {
		
		ArrayList<KsThree> threeKsList = CalcUtil.getStationCoeff().getStationByCode(station).getKsThree();//size 24
		
		return threeKsList;
	}
	
	public static List<Integer> getKsThree(Date time, String station, int k) {
		List<Integer> ks = new ArrayList<Integer>();;
		//KsThree ksThree = null;
		ArrayList<KsThree> ksThreeList = getKsThreeList(station);
		
		if (ksThreeList != null && !ksThreeList.isEmpty()) {
			
			int hour = CalcUtil.getSPTime(time).getHours();
			int period = hour/3;// 24 -> 8
			
			KsThree ksThree = ksThreeList.get(period);
			//System.out.println("**kss "+ksThree.getK1()+" "+ksThree.getK2()+" "+ksThree.getK3()+" "+ksThree.getK5()+" "+ksThree.getK6()+" "+getKsOfKsThree(k, ksThree));
			if (ksThree != null) 
				ks.add(getKsOfKsThree(k, ksThree));
			
			ksThree = ksThreeList.get(period + 8);
			//System.out.println("**kss "+ksThree.getK1()+" "+ksThree.getK2()+" "+ksThree.getK3()+" "+ksThree.getK5()+" "+ksThree.getK6()+" "+getKsOfKsThree(k, ksThree));
			if (ksThree != null) 
				ks.add(getKsOfKsThree(k, ksThree));
			
			ksThree = ksThreeList.get(period + 16);
			if (ksThree != null) 
				ks.add(getKsOfKsThree(k, ksThree));
			
//			ksThree = ksThreeList.get(period);
//			if (ksThree != null) 
//				ks.add(getKsOfKsThree(k, ksThree));
//			
//			if (mon == 1 || mon == 2 || mon == 11 || mon == 12) {
//				ksThree = ksThreeList.get(hour/3 -1);
//				ks = getKsOfKsThree(k, ksThree);
//			}
//			else if (mon == 3 || mon == 4 || mon == 9 || mon == 10) {
//				ksThree = ksThreeList.get(8 + hour/3 -1);
//				ks = getKsOfKsThree(k, ksThree);
//			}
//			else if (mon >= 5 && mon <= 8) {
//				ksThree = ksThreeList.get(16 + hour/3 -1);
//				ks = getKsOfKsThree(k, ksThree);
//			}
		}
//		for (int i = 0; i < threeKs.size(); i++) {
//			String season = threeKs.get(i).getSeason();
//			String period = threeKs.get(i).getPeriod();
//			int k1 = threeKs.get(i).getK1();
//			int k2 = threeKs.get(i).getK2();
//			int k3 = threeKs.get(i).getK3();
//			int k4 = threeKs.get(i).getK4();
//			int k5 = threeKs.get(i).getK5();
//			int k6 = threeKs.get(i).getK6();
//			int k7 = threeKs.get(i).getK7();
//			int k8 = threeKs.get(i).getK8();
//			int k9 = threeKs.get(i).getK9();
//			int k10 = threeKs.get(i).getK10();						
//			System.out.println("**k1 k2 "+season+" "+period+" "+k1+" "+k2);
//		}
		return ks;
	}
	
	private static int getKsOfKsThree(int k, KsThree ksThree) {
		int ks = 0;
		
		if (k == 0)
			ks = ksThree.getK0();
		else if (k == 1)
			ks = ksThree.getK1();
		else if (k == 2)
			ks = ksThree.getK2();
		else if (k == 3)
			ks = ksThree.getK3();
		else if (k == 4)
			ks = ksThree.getK4();
		else if (k == 5)
			ks = ksThree.getK5();
		else if (k == 6)
			ks = ksThree.getK6();
		else if (k == 7)
			ks = ksThree.getK7();
		else if (k == 8)
			ks = ksThree.getK8();
		else if (k == 9)
			ks = ksThree.getK9();
		return ks;
	}
	
	public static float getKs(String station, int k, Date time) throws ParseException {
		float a = 0;
		float b = 0;
		float ks = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		//int year = time.getYear();113
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		int year = cal.get(Calendar.YEAR);
				
    	Date date1 = sdf.parse(year+"-01-01");
    	Date date2 = sdf.parse(year+"-02-14");
    	Date date3 = sdf.parse(year+"-02-24");
    	Date date4 = sdf.parse(year+"-03-06");
    	Date date5 = sdf.parse(year+"-03-16");
    	Date date6 = sdf.parse(year+"-04-16");
    	Date date7 = sdf.parse(year+"-04-26");
    	Date date8 = sdf.parse(year+"-05-06");
    	Date date9 = sdf.parse(year+"-05-16");
    	Date date10 = sdf.parse(year+"-08-17");
    	Date date11 = sdf.parse(year+"-08-27");
    	Date date12 = sdf.parse(year+"-09-06");
    	Date date13 = sdf.parse(year+"-09-16");
    	Date date14 = sdf.parse(year+"-10-17");
    	Date date15 = sdf.parse(year+"-10-27");
    	Date date16 = sdf.parse(year+"-11-06");
    	Date date17 = sdf.parse(year+"-11-16");
    	Date date18 = sdf.parse(year+"-12-31");
    	
    	Date date2Leep = sdf.parse(year+"-02-15");
    	Date date3Leep = sdf.parse(year+"-02-25");
    	
    	List<Integer> ksThree = getKsThree(time, station, k );
    	
    	if(time.compareTo(date1) >= 0 && time.compareTo(date2) < 0){
    		ks = (float) ksThree.get(0) /3;
    	}
		else if (time.compareTo(date4) >= 0 && time.compareTo(date5) < 0){
    		ks = (float)(0.25f*ksThree.get(0) + 0.75f*ksThree.get(1)) /3;
    	}
		else if (time.compareTo(date5) >= 0 && time.compareTo(date6) < 0){
    		ks = (float)ksThree.get(1) /3;
    	}
		else if (time.compareTo(date6) >= 0 && time.compareTo(date7) < 0){
    		ks = (float) (0.75f*ksThree.get(1) + 0.25f*ksThree.get(2)) /3;
    	}
		else if (time.compareTo(date7) >= 0 && time.compareTo(date8) < 0){
    		ks = (float) (0.5f*ksThree.get(1) + 0.5f*ksThree.get(2)) /3;
    	}
		else if (time.compareTo(date8) >= 0 && time.compareTo(date9) < 0){
    		ks = (float) (0.25f*ksThree.get(1) + 0.75f*ksThree.get(2)) /3;
    	}
		else if (time.compareTo(date9) >= 0 && time.compareTo(date10) < 0){
    		ks = (float) ksThree.get(2) /3;
    	}
		else if (time.compareTo(date10) >= 0 && time.compareTo(date11) < 0){
    		ks = (float) (0.75f*ksThree.get(2) + 0.25f*ksThree.get(1)) /3;
    	}
		else if (time.compareTo(date11) >= 0 && time.compareTo(date12) < 0){
    		ks = (float) (0.5f*ksThree.get(2) + 0.5f*ksThree.get(1)) /3;
    	}
		else if (time.compareTo(date12) >= 0 && time.compareTo(date13) < 0){
    		ks = (float) (0.25f*ksThree.get(2) + 0.75f*ksThree.get(1)) /3;
    	}
		else if (time.compareTo(date13) >= 0 && time.compareTo(date14) < 0){
    		ks = (float) ksThree.get(1) /3;
    	}
		else if (time.compareTo(date14) >= 0 && time.compareTo(date15) < 0){
    		ks = (float) (0.75f*ksThree.get(1) + 0.25f*ksThree.get(0)) /3;
    	}
		else if (time.compareTo(date15) >= 0 && time.compareTo(date16) < 0){
    		ks = (float) (0.5f*ksThree.get(1) + 0.5f*ksThree.get(0)) /3;
    	}
		else if (time.compareTo(date16) >= 0 && time.compareTo(date17) < 0){
    		ks = (float) (0.25f*ksThree.get(1) + 0.75f*ksThree.get(0)) /3;
    	}
		else if (time.compareTo(date17) >= 0 && time.compareTo(date18) <= 0){
    		ks = (float) ksThree.get(0) /3;
    	}
		else if (CalcUtil.isLeapYear(year)) {
			if(time.compareTo(date2Leep) >= 0 && time.compareTo(date3Leep) < 0){
	    		ks = (float) (0.75f*ksThree.get(0) + 0.25f*ksThree.get(1)) /3;
	    	}
    		else if(time.compareTo(date3Leep) >= 0 && time.compareTo(date4) < 0){
        		ks = (float) (0.5f*ksThree.get(0) + 0.5f*ksThree.get(1)) /3;
        	}
    	}
    	else {
    		if(time.compareTo(date2) >= 0 && time.compareTo(date3) < 0){
	    		ks = (float) (0.75f*ksThree.get(0) + 0.25f*ksThree.get(1)) /3;
	    	}
    		else if(time.compareTo(date3) >= 0 && time.compareTo(date4) < 0){
        		ks = (float) (0.5f*ksThree.get(0) + 0.5f*ksThree.get(1)) /3;
        	}
    	}   	    	

		return ks;
	}
	
//	protected float[] getKs(String station, float[] kest) {
//		float a = 0;
//		float b = 0;
//		float[] ks = new float[8];
//		
//		Map<Float, Float> abCoeff = CalcUtil.getCoeffAandB(station);
//		if (abCoeff.size() != 8)
//			return ks;
//		
//		int i = 0;
//		Iterator<?> iter = abCoeff.entrySet().iterator();
//		while (iter.hasNext()) {
//			@SuppressWarnings("unchecked")
//			Map.Entry<Float, Float> mEntry = (Map.Entry<Float, Float>) iter.next();
//			
//			a = mEntry.getKey();
//			b = mEntry.getValue();
//			ks[i] = a + b * kest[i];
//			i++;
//		}	
//		
//		return ks;
//	}
	
	public static float getKs(String station, float kest, String timePrd) {
		float a = 0;
		float b = 0;
		float ks = 0;
		
		Map<Float, Float> abCoeff = CalcUtil.getCoeffAandB(station);
		if (abCoeff.size() != 8)
			return ks;
		
		int j = 0;
		if (timePrd.equalsIgnoreCase("00-03"))
			j = 0;
		else if (timePrd.equalsIgnoreCase("03-06"))
			j = 1;
		else if (timePrd.equalsIgnoreCase("06-09"))
			j = 2;
		else if (timePrd.equalsIgnoreCase("09-12"))
			j = 3;
		else if (timePrd.equalsIgnoreCase("12-15"))
			j = 4;
		else if (timePrd.equalsIgnoreCase("15-18"))
			j = 5;
		else if (timePrd.equalsIgnoreCase("18-21"))
			j = 6;
		else if (timePrd.equalsIgnoreCase("21-24"))
			j = 7;
		
		int i = 0;		
		Iterator<?> iter = abCoeff.entrySet().iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<Float, Float> mEntry = (Map.Entry<Float, Float>) iter.next();
			
			if (i == j) {
				a = mEntry.getKey();
				b = mEntry.getValue();	
				ks = a + b * kest;
				break;
			}
			i++;
		}					
		return ks;
	}
	public static int getAest(String station, int kIndex) {
		return CalcUtil.getK2a(kIndex);		
	}
	
	@SuppressWarnings("unchecked")
	public static float[] getKpEst(String[] station, float[] ks ) {
		float kpEst[] = new float[ks.length];
		float[][] wcoeff = new float[station.length][ks.length];
		
		if (ks.length != 8)
			return kpEst;
		
		for (int i = 0; i < station.length; i++) {
			Map<String, Float> coeff = CalcUtil.getCoeffW(station[i]);
			int j = 0;
			
			Iterator<?> iter = coeff.entrySet().iterator();
			while (iter.hasNext()) {				
				wcoeff[i][j] = ((Map.Entry<String, Float>) iter.next()).getValue();
				j++;
			}
		}	
		
		float sumW = 0;
		float sumWK = 0;
		
		for (int j = 0; j < ks.length; j++) {
			for (int i = 0; i < station.length; i++) {
				sumW += wcoeff[i][j];
				sumWK += wcoeff[i][j] * ks[i];
			}
			//kpEst[i] = (float) (Math.round(3 * sumWK / sumW)) / 3;	
			kpEst[j] = sumWK / sumW;
		    kpEst[j] = (int)kpEst[j] + CalcUtil.getThird(kpEst[j]);			
		}
		
		return kpEst;
	}
	@SuppressWarnings("unchecked")
	public static float getKpEst(String[] station, float ks, String fitTime ) {
		float kpEst = 0;
		float[] wcoeff = new float[8];
		
		for (int i = 0; i < station.length; i++) {
			Map<String, Float> coeff = CalcUtil.getCoeffW(station[i]);
			int j = 0;
			Iterator<?> iter = coeff.entrySet().iterator();
			while (iter.hasNext()) {
				if (((Map.Entry<String, Float>) iter.next()).getKey().equalsIgnoreCase(fitTime)) {
					wcoeff[i] = ((Map.Entry<String, Float>) iter.next()).getValue();
					break;
				}
				j++;
			}
		}
		
		float sumW = 0;
		float sumWK = 0;
		
		
		for (int i = 0; i < station.length; i++) {
			sumW += wcoeff[i];
			sumWK += wcoeff[i] * ks;
		}
			
		kpEst = sumWK / sumW;
	    kpEst = (int)kpEst + CalcUtil.getThird(kpEst);			
		
		return kpEst;
	}
	
	public static String[] getKp(float kpEst[], String[] kpModifier ) {
		String[] kp = new String[kpEst.length];
		if (kpEst.length != kpModifier.length)
			return kp;
		
		for (int i = 0; i < kpEst.length; i++) {
			int k	= Math.round(kpEst[i]);
			kp[i]	= k + kpModifier[i];
		}
		
		return kp;
	}
	
	public static String getKp(float kpEst, String kpModifier ) {
		int kp = Math.round(kpEst);
		
		return kp + kpModifier;
	}
}

