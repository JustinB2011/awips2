package gov.noaa.nws.ncep.ui.nsharp;
/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.NsharpGraphProperty
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    	Engineer    Description
 * -------		------- 	-------- 	-----------
 * 03/21/2012	229			Chin Chen	Initial coding
 *
 * </pre>
 * 
 * @author Chin Chen
 * @version 1.0
 */
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
@XmlRootElement(name = "NsharpGraphProperty")
@XmlAccessorType(XmlAccessType.NONE)
public class NsharpGraphProperty implements ISerializableObject{
	@XmlAttribute
	private boolean temp=true;
	
	@XmlAttribute
	private boolean dewp=true;
	
	@XmlAttribute
	private boolean parcel=true;
	
	@XmlAttribute
	private boolean VTemp=true;
	
	@XmlAttribute
	private boolean wetBulb=true;
	
	@XmlAttribute
	private boolean mixratio=false;
	
	@XmlAttribute
	private boolean dryAdiabat=true;
	
	@XmlAttribute
	private boolean moistAdiabat=false;
	
	@XmlAttribute
	private boolean effLayer=true;
	
	@XmlAttribute
	private boolean cloud=false;
	
	@XmlAttribute
	private boolean hodo=true;
	
	@XmlAttribute
	private boolean meanWind=true;
	
	@XmlAttribute
	private boolean smv3075=false;
	
	@XmlAttribute
	private boolean smv1585=false;
	
	@XmlAttribute
	private boolean smvBunkersR=true;
	
	@XmlAttribute
	private boolean smvBunkersL=true;
	
	@XmlAttribute
	private boolean omega=true;
	
	@XmlAttribute
	private boolean corfidiV=false;
	
	@XmlAttribute
	private boolean windBarb=true;
	
	@XmlAttribute
	private int windBarbDistance=400;
	
	@XmlAttribute
	private int tempOffset=0;
	
	
	public boolean isTemp() {
		return temp;
	}

	public void setTemp(boolean temp) {
		this.temp = temp;
	}

	public boolean isDewp() {
		return dewp;
	}

	public void setDewp(boolean dewp) {
		this.dewp = dewp;
	}

	public boolean isParcel() {
		return parcel;
	}

	public void setParcel(boolean parcel) {
		this.parcel = parcel;
	}

	public boolean isVTemp() {
		return VTemp;
	}

	public void setVTemp(boolean vTemp) {
		VTemp = vTemp;
	}

	public boolean isWetBulb() {
		return wetBulb;
	}

	public void setWetBulb(boolean wetBulb) {
		this.wetBulb = wetBulb;
	}

	public boolean isMixratio() {
		return mixratio;
	}

	public void setMixratio(boolean mixratio) {
		this.mixratio = mixratio;
	}

	public boolean isDryAdiabat() {
		return dryAdiabat;
	}

	public void setDryAdiabat(boolean dryAdiabat) {
		this.dryAdiabat = dryAdiabat;
	}

	public boolean isMoistAdiabat() {
		return moistAdiabat;
	}

	public void setMoistAdiabat(boolean moistAdiabat) {
		this.moistAdiabat = moistAdiabat;
	}

	public boolean isEffLayer() {
		return effLayer;
	}

	public void setEffLayer(boolean effLayer) {
		this.effLayer = effLayer;
	}

	public boolean isCloud() {
		return cloud;
	}

	public void setCloud(boolean cloud) {
		this.cloud = cloud;
	}

	public boolean isHodo() {
		return hodo;
	}

	public void setHodo(boolean hodo) {
		this.hodo = hodo;
	}

	public boolean isMeanWind() {
		return meanWind;
	}

	public void setMeanWind(boolean meanWind) {
		this.meanWind = meanWind;
	}

	public boolean isSmv3075() {
		return smv3075;
	}

	public void setSmv3075(boolean smv3075) {
		this.smv3075 = smv3075;
	}

	public boolean isSmv1585() {
		return smv1585;
	}

	public void setSmv1585(boolean smv1585) {
		this.smv1585 = smv1585;
	}

	public boolean isSmvBunkersR() {
		return smvBunkersR;
	}

	public void setSmvBunkersR(boolean smvBunkersR) {
		this.smvBunkersR = smvBunkersR;
	}

	public boolean isSmvBunkersL() {
		return smvBunkersL;
	}

	public void setSmvBunkersL(boolean smvBunkersL) {
		this.smvBunkersL = smvBunkersL;
	}

	public boolean isOmega() {
		return omega;
	}

	public void setOmega(boolean omega) {
		this.omega = omega;
	}


	public boolean isCorfidiV() {
		return corfidiV;
	}

	public void setCorfidiV(boolean corfidiV) {
		this.corfidiV = corfidiV;
	}

	public boolean isWindBarb() {
		return windBarb;
	}

	public void setWindBarb(boolean windBarb) {
		this.windBarb = windBarb;
	}

	public int getWindBarbDistance() {
		return windBarbDistance;
	}

	public void setWindBarbDistance(int windBarbDistance) {
		this.windBarbDistance = windBarbDistance;
	}

	public int getTempOffset() {
		return tempOffset;
	}

	public void setTempOffset(int tempOffset) {
		this.tempOffset = tempOffset;
	}
}
