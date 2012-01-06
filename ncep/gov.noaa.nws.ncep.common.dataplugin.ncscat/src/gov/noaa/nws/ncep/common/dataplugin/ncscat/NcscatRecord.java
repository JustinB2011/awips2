/**
 * NcscatRecord
 * 
 * This java class performs the mapping to the database table for ASCAT,Quikscat
 * 
 * HISTORY
 *
 * Date     	Author		Description
 * ------------	----------	-----------	--------------------------
 * 11/2009		Uma Josyula	Initial creation	
 * 
 * This code has been developed by the SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.common.dataplugin.ncscat;



import java.util.Calendar;
import java.util.Date;


import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.IDecoderGettable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;


@Entity
@Table(name = "ncscat", uniqueConstraints = { @UniqueConstraint(columnNames = { "dataURI" }) })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize

public class NcscatRecord extends PersistablePluginDataObject implements
IPersistable{

	private static final long serialVersionUID = 1L;


	// The time  from the report 
	@Column
	@DataURI(position=1)
	@XmlElement
	@DynamicSerializeElement
	private Calendar startTime;

	@Column
	@DataURI(position=2)
	@XmlElement
	@DynamicSerializeElement
	private Calendar endTime;

	@Column
	@DataURI(position=3)
	@XmlElement
	@DynamicSerializeElement
	private String reportType;
	

	@Transient
	private byte[] convertedMessage;


	@Column
	@XmlElement
	@DynamicSerializeElement
	private int recordLength;
	


	/**
	 * Default Constructor
	 */
	public NcscatRecord() { 	
	}

	/**
	 * Constructs a ascat record from a dataURI
	 * 
	 * @param uri The dataURI
	 */
	public NcscatRecord(String uri) {
		super(uri);
	}


	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public byte[] getConvertedMessage() {
		return convertedMessage;
	}

	public void setConvertedMessage(byte[] convertedMessage) {
		this.convertedMessage = convertedMessage;
	}

	@Override
	public IDecoderGettable getDecoderGettable() {
		// TODO Auto-generated method stub
		return null;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	@Override
	public void setIdentifier(Object dataURI)
	{

		this.identifier = dataURI;

	}

    public int getRecordLength() {
		return recordLength;
	}

	public void setRecordLength(int recordLength) {
		this.recordLength = recordLength;
	}


    /**
     * Get the time to use for persisting this data.
     * 
     * @return The persistence time for this data.
     */
    @Override
    public Date getPersistenceTime() {
    	if(getInsertTime()== null)	return null;
    	else
    		return getInsertTime().getTime();
    }
    
    /**
     * Set the time to be used for the persistence time for this object.
     * 
     * @param persistTime
     *            The persistence time to be used.
     */
    @Override
    public void setPersistenceTime(Date persistTime){
    	Calendar insertTime=Calendar.getInstance();
    	insertTime.setTime(persistTime);
    	setInsertTime(insertTime);
    }



}














