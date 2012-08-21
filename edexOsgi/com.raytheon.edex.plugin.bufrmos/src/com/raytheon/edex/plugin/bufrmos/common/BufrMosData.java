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
package com.raytheon.edex.plugin.bufrmos.common;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.IDecoderGettable;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.annotations.DataURIConfig;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.pointdata.IPointData;
import com.raytheon.uf.common.pointdata.PointDataView;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * MOSData "mirrors" the mosdata data base table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20080221            862 jkorman     Initial Coding.
 * 02/06/09     1990       bphillip    removed populateDataStore method
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */
@Entity
@Table(name = "bufrmos", uniqueConstraints = { @UniqueConstraint(columnNames = { "dataURI" }) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@DataURIConfig(persistentIndex = 2)
public abstract class BufrMosData extends PersistablePluginDataObject implements
		IDecoderGettable, IPersistable, IPointData {

	public static enum MOSType {
		ETA, GFS, AVN, LAMP, HPC, MRF, NGM
	};

	private static final long serialVersionUID = 1L;

	public static final String MOS_DATA = "Data";

	// Text of the WMO header
	@Transient
	@XmlAttribute
	@DynamicSerializeElement
	private String wmoHeader;

	@Embedded
	@DynamicSerializeElement
	private PointDataView pointDataView = null;

	@ManyToOne(cascade = { CascadeType.REFRESH })
	@PrimaryKeyJoinColumn
	@DataURI(position = 1, embedded = true)
	@XmlElement
	@DynamicSerializeElement
	private BufrMosDataLocation location;

	/**
	 * Create an empty MOSData object.
	 */
	public BufrMosData() {
		this.pluginName = "bufrmos" + getType();
	}

	/**
	 * Constructor for DataURI construction through base class. This is used by
	 * the notification service.
	 * 
	 * @param uri
	 *            A data uri applicable to this class.
	 * @param tableDef
	 *            The table definitions for this class.
	 */
	public BufrMosData(String uri) {
		super(uri);
	}

	/**
	 * Get the geometry latitude.
	 * 
	 * @return The geometry latitude.
	 */
	public double getLatitude() {
		return location.getLatitude();
	}

	/**
	 * Get the geometry longitude.
	 * 
	 * @return The geometry longitude.
	 */
	public double getLongitude() {
		return location.getLongitude();
	}

	/**
	 * Get the station identifier for this observation.
	 * 
	 * @return the stationId
	 */
	public String getStationId() {
		return location.getStationId();
	}

	/**
	 * @return the type
	 */
	public abstract MOSType getType();

	/**
	 * @return the wmoHeader
	 */
	public String getWmoHeader() {
		return wmoHeader;
	}

	/**
	 * @param wmoHeader
	 *            the wmoHeader to set
	 */
	public void setWmoHeader(String wmoHeader) {
		this.wmoHeader = wmoHeader;
	}

	/**
	 * 
	 * @param dataURI
	 */
	@Override
	public void setDataURI(String dataURI) {
		identifier = dataURI;
	}

	/**
	 * @see com.raytheon.uf.common.dataplugin.PluginDataObject#getDecoderGettable()
	 */
	@Override
	public IDecoderGettable getDecoderGettable() {
		return null;
	}

	/**
	 * @see com.raytheon.uf.common.dataplugin.IDecoderGettable#getString(java.lang.String)
	 */
	@Override
	public String getString(String paramName) {
		return null;
	}

	/**
	 * @see com.raytheon.uf.common.dataplugin.IDecoderGettable#getStrings(java.lang.String)
	 */
	@Override
	public String[] getStrings(String paramName) {
		return null;
	}

	/**
	 * @see com.raytheon.uf.common.dataplugin.IDecoderGettable#getValue(java.lang.String)
	 */
	@Override
	public Amount getValue(String paramName) {

		Amount retValue = null;

		// Object element = elementMap.get(paramName);
		// TODO:
		// if (element != null) {
		// Unit<?> units = BUFRTableB.mapUnits(element.getUnits());
		// if (units != null) {
		// if ("FLOAT".equals(element.getElementType())) {
		// retValue = new Amount(element.getDoubleVal(), units);
		// } else if ("INTEGER".equals(element.getElementType())) {
		// retValue = new Amount(element.getIntegerVal(), units);
		// }
		// }
		// }

		return retValue;
	}

	/**
	 * @see com.raytheon.uf.common.dataplugin.IDecoderGettable#getValues(java.lang.String)
	 */
	@Override
	public Collection<Amount> getValues(String paramName) {
		return null;
	}

	public BufrMosDataLocation getLocation() {
		return location;
	}

	public void setLocation(BufrMosDataLocation mosLocation) {
		this.location = mosLocation;
	}

	@Override
	public PointDataView getPointDataView() {
		return this.pointDataView;
	}

	@Override
	public void setPointDataView(PointDataView pointDataView) {
		this.pointDataView = pointDataView;

	}
}
