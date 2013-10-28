package gov.noaa.nws.ncep.edex.plugin.geomag.calculation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ncep.common.dataplugin.geomag.table.KFitTime;
import gov.noaa.nws.ncep.common.dataplugin.geomag.table.KsThree;
import gov.noaa.nws.ncep.common.dataplugin.geomag.util.KStationCoefficientLookup;

/*
 * The k index and decoder calculation utility.
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

public class CalcUtil {
	private static final float MISSING_VAL = 99999.99f;
	private static final float K_EXPONENT = 3.3f;
	private static KStationCoefficientLookup stationCoeff = KStationCoefficientLookup.getInstance();
	 
//	public static enum Stations { BOU,FRD,FRN,NEW,SIT,HAD,CL2,CNB,OTT,MEA,NGK,WNG} 
//		BOU(500, 254.76f), 
//		CL2(500, 2.261f), 
//		CNB(450, 149.36f), 
//		FRD(500, 282.63f), 
//		FRN(350, 240.28f), 
//		HAD(500, 355.517f), 
//		NEW(700, 242.88f), 
//		SIT(1000, 224.67f),
//		CMO(2500, 212.14f);
//		
//		private final int k9Limit;   
//		private final float longitude; 
//		private Stations (int k9Limit, float longitude) {
//			this.k9Limit = k9Limit;
//			this.longitude = longitude;
//		}
//	}
	
	// Gamma limit table
	private static enum Limit {
		K0(0), 
		K1(5),
		K2(10),
		K3(20),
		K4(40),
		K5(70),
		K6(120),
		K7(200),
		K8(330),
		K9(500);
		
		private final int kConst;    
		private Limit (int kConst) {
			this.kConst = kConst;
		}
	}

//	public static String getStations() {
//		return Stations;
//	}
	
	public static int getKConst(int k) {
		int kConst = 0;
		if (k == 0)
			kConst = Limit.K0.kConst;
		else if (k == 1)
			kConst = Limit.K1.kConst;
		else if (k == 2)
			kConst = Limit.K2.kConst;
		else if (k == 3)
			kConst = Limit.K3.kConst;
		else if (k == 4)
			kConst = Limit.K4.kConst;
		else if (k == 5)
			kConst = Limit.K5.kConst;
		else if (k == 6)
			kConst = Limit.K6.kConst;
		else if (k == 7)
			kConst = Limit.K7.kConst;
		else if (k == 8)
			kConst = Limit.K8.kConst;
		else if (k == 9)
			kConst = Limit.K9.kConst;
			
		   return kConst;
	}
	
	// A-index table
	private static enum K2a {
		a0(0), 
		a1(3),
		a2(7),
		a3(15),
		a4(27),
		a5(48),
		a6(80),
		a7(140),
		a8(240),
		a9(400);
		
		private final int a;    
		private K2a (int a) {
			this.a = a;
		}
	}
	
	public static int geta2k(int k) {
		int a = 0;
		if (k == 0)
			a = K2a.a0.a;
		else if (k == 1)
			a = K2a.a1.a;
		else if (k == 2)
			a = K2a.a2.a;
		else if (k == 3)
			a = K2a.a3.a;
		else if (k == 4)
			a = K2a.a4.a;
		else if (k == 5)
			a = K2a.a5.a;
		else if (k == 6)
			a = K2a.a6.a;
		else if (k == 7)
			a = K2a.a7.a;
		else if (k == 8)
			a = K2a.a8.a;
		else if (k == 9)
			a = K2a.a9.a;
			
		   return a;
	}
	
	public static KStationCoefficientLookup getStationCoeff() {
		return stationCoeff;
	}
	
	public static int getK9Limit(String station) throws NumberFormatException  {
		int k9 = 0;
		
		String k9Limit = getStationCoeff().getStationByCode(station).getK9Limit();
		k9 = Integer.parseInt(k9Limit);
		
		return k9;
	}
	
	public static float getLongitude(String station) throws NumberFormatException  {
		float lon = 0;
		if (station != null && !station.equalsIgnoreCase("")) {
			String longitude = getStationCoeff().getStationByCode(station).getLongitude();
			lon = Float.parseFloat(longitude);
		}
		return lon;
	}
	

	/*
	 * map of the A and the B values in the order of 00-03, 03-06...
	 */
	public static Map<Float, Float> getCoeffAandB(String station) {
		Map<Float, Float> abCoeff = new HashMap<Float, Float>();
		
		List<KFitTime> fitTime = getStationCoeff().getStationByCode(station).getKFitTime();
		if (fitTime.size() != 8)
			return abCoeff;
		
		for (int i = 0; i < 8; i++) {
			float a = fitTime.get(i).getCoeffA();
			float b = fitTime.get(i).getCoeffB();
			abCoeff.put(a, b);
		}
//		KStationCoefficientLookup look = KStationCoefficientLookup.getInstance();
//		Map<String, KStationCoefficient> stationMap = look.getStationsByCodeMap();
//		int size = 1+31;
//		stations = new ArrayList<List>();//String[]: station, uri0, ...uri31
//		for (Map.Entry<String, KStationCoefficient> entry : stationMap.entrySet()) {
//			List<String> astation = new ArrayList<String>();
//			astation.add( entry.getKey());
//			stations.add(astation);
////			System.out.println("***entry "+ entry.getKey());
//		}
		return abCoeff;
	}
	
	/*
	 * map of the time period and the W values in the order of 00-03, 03-06...
	 */
	public static Map<String, Float> getCoeffW(String station) {
		Map<String, Float> wCoeff = new HashMap<String, Float>();
		
		List<KFitTime> fitTime = getStationCoeff().getStationByCode(station).getKFitTime();
		if (fitTime.size() != 8)
			return wCoeff;
		
		for (int i = 0; i < 8; i++) {
			String a = fitTime.get(i).getKey();
			float b = fitTime.get(i).getCoeffW();
			wCoeff.put(a, b);
		}
		
		return wCoeff;
	}
	
//	public static int getK9Limit(String station) {
//		int k9Limit = 0;
//		if (station.equalsIgnoreCase(Stations.BOU.toString()))
//			k9Limit = Stations.BOU.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.CL2.toString()))
//			k9Limit = Stations.CL2.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.CNB.toString()))
//			k9Limit = Stations.CNB.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.FRD.toString()))
//			k9Limit = Stations.FRD.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.FRN.toString()))
//			k9Limit = Stations.FRN.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.HAD.toString()))
//			k9Limit = Stations.HAD.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.NEW.toString()))
//			k9Limit = Stations.NEW.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.SIT.toString()))
//			k9Limit = Stations.SIT.k9Limit;
//		else if (station.equalsIgnoreCase(Stations.CMO.toString()))
//			k9Limit = Stations.CMO.k9Limit;
//		
//		   return k9Limit;
//	}
//	
//	public static float getLongitude(String station) {
//		float longitude = 0;
//		if (station.equalsIgnoreCase(Stations.BOU.toString()))
//			longitude = Stations.BOU.longitude;
//		else if (station.equalsIgnoreCase(Stations.CL2.toString()))
//			longitude = Stations.CL2.longitude;
//		else if (station.equalsIgnoreCase(Stations.CNB.toString()))
//			longitude = Stations.CNB.longitude;
//		else if (station.equalsIgnoreCase(Stations.FRD.toString()))
//			longitude = Stations.FRD.longitude;
//		else if (station.equalsIgnoreCase(Stations.FRN.toString()))
//			longitude = Stations.FRN.longitude;
//		else if (station.equalsIgnoreCase(Stations.HAD.toString()))
//			longitude = Stations.HAD.longitude;
//		else if (station.equalsIgnoreCase(Stations.NEW.toString()))
//			longitude = Stations.NEW.longitude;
//		else if (station.equalsIgnoreCase(Stations.SIT.toString()))
//			longitude = Stations.SIT.longitude;
//		else if (station.equalsIgnoreCase(Stations.CMO.toString()))
//			longitude = Stations.CMO.longitude;
//		
//		   return longitude;
//	}
	
	public static int[] getKLimit(String station) {
		int[] kLimit = new int[10];
		int k9Limit = getK9Limit(station);
		for (int i = 0; i < kLimit.length; i++) {			
			kLimit[i] = Math.round( k9Limit * getKConst(i) /500);			
		}
		return kLimit;
	}
	
//	public static int[] getAIndex(String station, float[] k-index) {
//		int[] aIndex = new int[10];
//		//int k9Limit = getK9Limit(station);
//		for (int i = 0; i < kLimit.length; i++) {			
//			aIndex[i] = Math.round( getK2a(i));			
//		}
//		return aIndex;
//	}
	
	public static int getKfromTable(int[] kLimit, float gamma) {
		int kIndex;		
		//int[] kLimit = getKLimit(station);

		int i = 0;
		for ( i = 0; i < 10; i++) {
			if (gamma > kLimit[i])
				continue;
			else
				break;
		}
		
		//take the lower of i
		if (i > 0)
			i = i-1;
		
		if (i <= 9)
			kIndex = i;
		else 
			kIndex = 9;
		
		return kIndex;
	}
	
	public static int getGammaFromK(String station, int kIndex) {
		int gamma = getK9Limit(station) * getKConst(kIndex) / 500;		
		
		return gamma;
	}
//	// assume db time format yyyy-mm-dd hh:mm:ss
//	public static int getYear(String time) {
//		int year = 0;
//		
//		String t = time.substring(0, 4);
//		try {
//			year = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//	
//		return year;
//	}
//
//	public static int getMonth(String time) {
//		int month = 0;
//		
//		String t = time.substring(5, 7);
//		try {
//			month = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		return month;
//	}
//
//	public static int getDay(String time) {
//		int day = 0;
//		
//		String t = time.substring(8, 10);
//		try {
//			day = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		return day;
//	}
//
//	public static int getHour(String time) {
//		int hr = 0;
//		
//		String t = time.substring(11, 13);
//		try {
//			hr = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		return hr;
//	}
//
//	public static int getMin(String time) {
//		int min = 0;
//		
//		String t = time.substring(14, 16);
//		try {
//			min = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		return min;
//	}
//
//	public static int getSecond(String time) {
//		int ss = 0;
//		
//		String t = time.substring(17);
//		try {
//			ss = Integer.parseInt(t);
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		return ss;
//	}
	
	// assume db time format yyyy-mm-dd hh:mm:ss
	public static Date getSPTime(Date currTime ){
		Date spTime = currTime;
		
		int hour = currTime.getHours(); 
		
		if (hour >= 0 && hour < 3)
			hour = 0;
		else if (hour >= 3 && hour < 6)
			hour = 3;
		else if (hour >= 6 && hour < 9)
			hour = 6;
		else if (hour >= 9 && hour < 12)
			hour = 9;
		else if (hour >= 12 && hour < 15)
			hour = 12;
		else if (hour >= 15 && hour < 18)
			hour = 15;
		else if (hour >= 18 && hour < 21)
			hour = 18;
		else if (hour >= 21 && hour < 24)
			hour = 21;
		
		spTime.setHours(hour);
		spTime.setMinutes(0);
		spTime.setSeconds(0);
//		spTime = String.valueOf(currTime.getYear()) + "-" +
//					String.valueOf(currTime.getMonth()) + "-" +
//					String.valueOf(currTime.getDay()) + " " +
//					hour + ":" + "00:00.0";
					
		return spTime;
	}
	public static Date getEPTime(Date currTime ){
		Date epTime = (Date)currTime.clone();
		
		int hour = currTime.getHours(); 
		if (hour >= 0 && hour < 3)
			hour = 3;
		else if (hour >= 3 && hour < 6)
			hour = 6;
		else if (hour >= 6 && hour < 9)
			hour = 9;
		else if (hour >= 9 && hour < 12)
			hour = 12;
		else if (hour >= 12 && hour < 15)
			hour = 15;
		else if (hour >= 15 && hour < 18)
			hour = 18;
		else if (hour >= 18 && hour < 21)
			hour = 21;
		else if (hour >= 21 && hour < 24)
			hour = 0;
		
		if (hour != 0)
			epTime.setHours(hour);
		else {			
			int day = currTime.getDate() +1;
			epTime.setDate(day);
			epTime.setHours(hour);
		}
		
		epTime.setMinutes(0);
		epTime.setSeconds(0);
				
		return epTime;
	}
	public static boolean isHalfMissing(float[] items) {
		boolean halfMissaing = false;
		
		int i = 0;
		for (i = 0; i < items.length; i++) {
			if (items[i] == MISSING_VAL)
				i++;
		}
		if (i > items.length/2)
			halfMissaing = true;
		
		return halfMissaing;
	}
	
	public static float getThird(float kpEst) {
		float half = 0.333333f/2;
		float x = kpEst - (int) kpEst; //get decimal fraction
		
		if (x >= 0 && x <= half)
			x = 0;
		else if (x >= half && x <= 2*half)
			x = 0.333333f;
		else if (x >= 2*half && x <= 3*half)
			x = 0.333333f;
		else if (x >= 3*half && x <= 4*half)
			x = 0.666666f;
		else if (x >= 4*half && x <= 5*half)
			x = 0.666666f;
		else if (x >= 5*half && x <= 6*half)
			x = 1;
		
		return x;
	}
	
	public static float maxValue(float[] dev) {
		float max = -99999;
		for (int i = 0; i < dev.length; i++) {
			if (dev[i] > max && dev[i] < MISSING_VAL) {
				max = dev[i];
			}
		}
		return max;
	}
	
	public static float minValue(float[] dev) {
		float min = 99999;
		for (int i = 0; i < dev.length; i++) {
			if (dev[i] < min && dev[i] > -MISSING_VAL) {
				min = dev[i];
			}
		}
		return min;
	}
	
	/*
	 * 10 element floating point array
	 */
	public static float[] geKLength() {
		float[] kLength = new float[10];
		
		kLength[0] = 0;
		for (int i = 1; i < 10; i++) {
			kLength[i] = (float) Math.exp( K_EXPONENT * Math.log(i));
			if (kLength[i] > 1080)
				kLength[i]  = 1080;
		}
		
		return kLength;
	}

	// uri: /geomag/2013-05-20_00:00:00.0/HAD/101/GEOMAG
	public static String getSourceFromUri(String uri) {		
		if (uri != null && uri.length() >= 37)
			return uri.substring(34, 37);
		else
			return "";
	}
	
	public static String getStationFromUri(String uri) {		
		if (uri != null && uri.length() >= 37)
			return uri.substring(30, 33);
		else
			return "";
	}
	
	public static Date getTimeFromUri(String uri) throws ParseException {
		String format = "yyyy-MM-dd'_'HH:mm:ss.s";     	    		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		if (uri != null && uri.length() >= 37) {
			String time = uri.substring(8, 29);
		    Date date = sdf.parse(time);
			return date;
		}
		else
			return new Date();
	}

	// get the front part before the source in the uri
	public static String separateSourceFrontUri(String uri) {		
		if (uri != null && uri.length() >= 37) 
			return uri.substring(0, 34);
		else
			return "";
	}
	
	public static float[] toFloatArray(List<Float> list)  {
		float[] ret = new float[list.size()];
	    int i = 0;
	    for (Float e : list)  
	        ret[i++] = e.floatValue();
	    return ret;
	}
	public static int[] toIntArray(List<Integer> list)  {
		int[] ret = new int[list.size()];
	    int i = 0;
	    for (Integer e : list)  
	        ret[i++] = e.intValue();
	    return ret;
	}
	
	public static boolean isLeapYear(int year) {
		boolean isLeap;
		
		if (year/400 ==0)
			isLeap = true;
		else if (year/100 == 0)
			isLeap = false;
		else if (year/4 == 0)
			isLeap = true;
		else
			isLeap = false;
		
		return isLeap;
	}
	
//	public static String getMonthDayFromNumber(int year, int number) {	//CL22013041.min
//		String temp = "";
//		String month = "";
//		String day = "";
//		String monthDay = "";
//		Boolean isLeapYear = isLeapYear( year);
//		int[] days = {31,28,31,30,31,30,31,31,30,31,30,31};
//		int[] leapDays = {31,29,31,30,31,30,31,31,30,31,30,31};
//		Calendar cal = Calendar.getInstance();
//		cal.get(Calendar.DAY_OF_MONTH);
//		cal.get(Calendar.MONTH);
//		cal.get(Calendar.DAY_OF_YEAR);
//		cal.set(Calendar.DAY_OF_YEAR, number);
//		int[] num = 
//		if (isLeapYear) {
//			
//		}
//		else {
//			
//		}
//		if (number<=31){ //JEJ, m130212.txt
//			month = "01";
//			day = String.valueOf(number);
//		}	
//		else if (number > 31 && number <= 59){
//			month = "02";
//			day = String.valueOf(number-31);
//		}
//		else if (number > 31 && number <= 59){
//			month = "03";
//			day = String.valueOf(number-31);
//		}
//		else if (fileName.startsWith("ha")){ CNB,NGK, WNG
//			temp = fileName.substring(3, 10);
//			year = temp.substring(0, 4);
//		}
//		
//		return monthDay;
//	}
	
	public static String getTimeFromFileName(String fileName) {	//CL22013041.min
		String time = "";
		String temp = "";
		String year = "";
		String month = "";
		String day = "";
		String num = "";
		
		Calendar cal = Calendar.getInstance();
		
		if (fileName.startsWith("m")){ //JEJ, m130212.txt
			temp = fileName.substring(1, 7);
			year = "20"+temp.substring(4, 6);
			month = temp.substring(2, 4);
			day = temp.substring(0, 2);
		}	
		else if (fileName.startsWith("ha")){
			temp = fileName.substring(2, 9);
			year = temp.substring(3, 7);
			num = temp.substring(0, 3);
			try {
				cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(num));
			} catch (NumberFormatException e) {
				
			}
			month = String.valueOf(cal.get(Calendar.MONTH));
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		}
		else if (fileName.startsWith("BOU") || fileName.startsWith("CL2") || fileName.startsWith("CMO")
				|| fileName.startsWith("OTT") || fileName.startsWith("MEA")) {
			temp = fileName.substring(3, 10);
			year = temp.substring(0, 4);
			num = temp.substring(4, 7);
			
			try {
				cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(num));
			} catch (NumberFormatException e) {
				
			}
			month = String.valueOf(cal.get(Calendar.MONTH) +1);  
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)); 
		}
		else if (fileName.startsWith("ha") || fileName.startsWith("CNB") 
				|| fileName.startsWith("OTT") || fileName.startsWith("WNG")) { 
			temp = fileName.substring(3, 10);
			year = temp.substring(0, 4);
			month = temp.substring(4, 6);
			day = temp.substring(6, 8);
		}
		
		if (month.length() == 1)
			month = "0"+ month;
		if (day.length() == 1)
			day = "0" + day;
		time = year + "-" + month + "-" + day;
		return time;
	}
	
	public static float getMedian(float[] array) {
		float median = 0;
		if (array.length <= 1)
			return array[0];
			
		float[] arraySort = array.clone();
		Arrays.sort(arraySort);
		
		// remove missing data
		List<Float> newArray = new ArrayList<Float>();
		for ( int k = 0; k < arraySort.length-1;  k++ )
			if (arraySort[k] != MISSING_VAL)
				newArray.add(arraySort[k]);
			else
				break; //to sorted arraySort
		
		int size = newArray.size();
		if (size/2 == 0)
			median = (newArray.get(size/2) + newArray.get(size/2 -1) ) / 2;
		else
			median = newArray.get((size-1)/2);
		
		return median;
	}
}

