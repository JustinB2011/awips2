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
package com.raytheon.uf.common.dataplugin.bufrsigwx;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
import com.raytheon.uf.common.dataplugin.bufrsigwx.common.SigWxLayer;
import com.raytheon.uf.common.dataplugin.bufrsigwx.common.SigWxType;
import com.raytheon.uf.common.dataplugin.bufrsigwx.common.TropopauseLayerData;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.pointdata.IPointData;
import com.raytheon.uf.common.pointdata.PointDataView;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 18, 2009            jkorman     Initial creation
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.0
 */
@Entity
@Table(name = "bufrsigwx", uniqueConstraints = { @UniqueConstraint(columnNames = { "dataURI" }) })
@DynamicSerialize
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SigWxData extends PersistablePluginDataObject implements
        IDecoderGettable, IPointData, IPersistable {

    private static final long serialVersionUID = 1L;

    @Column
    @DataURI(position = 1)
    @XmlElement
    @DynamicSerializeElement
    private SigWxLayer wxLayer;

    @Column
    @DataURI(position = 2)
    @XmlElement
    @DynamicSerializeElement
    private SigWxType wxType;

    @Column
    @DataURI(position = 3)
    @XmlAttribute
    @DynamicSerializeElement
    private Integer key;

    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Integer baseHeight;

    @Column
    @XmlAttribute
    @DynamicSerializeElement
    private Integer topHeight;

    @Embedded
    private PointDataView pdv;

    // Text of the WMO header
    @Column(length = 32)
    @DynamicSerializeElement
    @XmlElement
    private String wmoHeader;

    @Transient
    @DynamicSerializeElement
    @XmlElement
    private TropopauseLayerData tropData;

    /**
     * Empty constructor.
     */
    public SigWxData() {
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
    public SigWxData(String uri) {
        super(uri);
    }

    /**
     * @return the wxLayer
     */
    public SigWxLayer getWxLayer() {
        return wxLayer;
    }

    /**
     * @param wxLayer
     *            the wxLayer to set
     */
    public void setWxLayer(SigWxLayer wxLayer) {
        this.wxLayer = wxLayer;
    }

    /**
     * @return the wxType
     */
    public SigWxType getWxType() {
        return wxType;
    }

    /**
     * @param wxType
     *            the wxType to set
     */
    public void setWxType(SigWxType wxType) {
        this.wxType = wxType;
    }

    /**
     * @return the baseHeight
     */
    public Integer getBaseHeight() {
        return baseHeight;
    }

    /**
     * @param baseHeight
     *            the baseHeight to set
     */
    public void setBaseHeight(Integer baseHeight) {
        this.baseHeight = baseHeight;
    }

    /**
     * @return the topHeight
     */
    public Integer getTopHeight() {
        return topHeight;
    }

    /**
     * @param topHeight
     *            the topHeight to set
     */
    public void setTopHeight(Integer topHeight) {
        this.topHeight = topHeight;
    }

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
     * @return the tropData
     */
    public TropopauseLayerData getTropData() {
        return tropData;
    }

    /**
     * @param tropData
     *            the tropData to set
     */
    public void setTropData(TropopauseLayerData tropData) {
        this.tropData = tropData;
    }

    /**
     * @return the key
     */
    public Integer getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(Integer key) {
        this.key = key;
    }

    /**
     * @return the pdv
     */
    public PointDataView getPdv() {
        return pdv;
    }

    /**
     * @param pdv
     *            the pdv to set
     */
    public void setPdv(PointDataView pdv) {
        this.pdv = pdv;
    }

    @Override
    public IDecoderGettable getDecoderGettable() {
        return null;
    }

    @Override
    public String getString(String paramName) {
        return null;
    }

    @Override
    public String[] getStrings(String paramName) {
        return null;
    }

    @Override
    public Amount getValue(String paramName) {
        return null;
    }

    @Override
    public Collection<Amount> getValues(String paramName) {
        return null;
    }

    /**
     * 
     */
    @Override
    public PointDataView getPointDataView() {
        return pdv;
    }

    /**
     * 
     */
    @Override
    public void setPointDataView(PointDataView pdv) {
        this.pdv = pdv;
    }

    /**
     * 
     * @return
     */
    public final SigWxData copyObs() {
        SigWxData obs = new SigWxData();

        obs.dataTime = dataTime.clone();
        obs.pluginName = pluginName;

        obs.baseHeight = baseHeight;
        obs.topHeight = topHeight;
        obs.wxLayer = wxLayer;
        obs.wxType = wxType;
        obs.wmoHeader = wmoHeader;

        return obs;
    }
}
