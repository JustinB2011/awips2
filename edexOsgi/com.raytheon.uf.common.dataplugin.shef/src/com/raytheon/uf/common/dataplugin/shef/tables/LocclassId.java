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
package com.raytheon.uf.common.dataplugin.shef.tables;
// default package
// Generated Oct 17, 2008 2:22:17 PM by Hibernate Tools 3.2.2.GA

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * LocclassId generated by hbm2java
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2008                        Initial generation by hbm2java
 * Aug 19, 2011      10672     jkorman Move refactor to new project
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.1
 */
@Embeddable
@javax.xml.bind.annotation.XmlRootElement
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.NONE)
@com.raytheon.uf.common.serialization.annotations.DynamicSerialize
public class LocclassId extends com.raytheon.uf.common.dataplugin.persist.PersistableDataObject implements java.io.Serializable, com.raytheon.uf.common.serialization.ISerializableObject {

    private static final long serialVersionUID = 1L;

   @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String lid;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String name;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Double lat;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Double lon;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String wfo;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String hsa;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Integer post;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String dispClass;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String isDcp;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String isObserver;

    @javax.xml.bind.annotation.XmlElement
    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String telemType;

    public LocclassId() {
    }

    public LocclassId(String lid, String name, Double lat, Double lon,
            String wfo, String hsa, Integer post, String dispClass,
            String isDcp, String isObserver, String telemType) {
        this.lid = lid;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.wfo = wfo;
        this.hsa = hsa;
        this.post = post;
        this.dispClass = dispClass;
        this.isDcp = isDcp;
        this.isObserver = isObserver;
        this.telemType = telemType;
    }

    @Column(name = "lid", length = 8)
    public String getLid() {
        return this.lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    @Column(name = "name", length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "lat", precision = 17, scale = 17)
    public Double getLat() {
        return this.lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Column(name = "lon", precision = 17, scale = 17)
    public Double getLon() {
        return this.lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @Column(name = "wfo", length = 3)
    public String getWfo() {
        return this.wfo;
    }

    public void setWfo(String wfo) {
        this.wfo = wfo;
    }

    @Column(name = "hsa", length = 3)
    public String getHsa() {
        return this.hsa;
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
    }

    @Column(name = "post")
    public Integer getPost() {
        return this.post;
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    @Column(name = "disp_class", length = 10)
    public String getDispClass() {
        return this.dispClass;
    }

    public void setDispClass(String dispClass) {
        this.dispClass = dispClass;
    }

    @Column(name = "is_dcp", length = 1)
    public String getIsDcp() {
        return this.isDcp;
    }

    public void setIsDcp(String isDcp) {
        this.isDcp = isDcp;
    }

    @Column(name = "is_observer", length = 1)
    public String getIsObserver() {
        return this.isObserver;
    }

    public void setIsObserver(String isObserver) {
        this.isObserver = isObserver;
    }

    @Column(name = "telem_type", length = 10)
    public String getTelemType() {
        return this.telemType;
    }

    public void setTelemType(String telemType) {
        this.telemType = telemType;
    }

    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof LocclassId))
            return false;
        LocclassId castOther = (LocclassId) other;

        return ((this.getLid() == castOther.getLid()) || (this.getLid() != null
                && castOther.getLid() != null && this.getLid().equals(
                castOther.getLid())))
                && ((this.getName() == castOther.getName()) || (this.getName() != null
                        && castOther.getName() != null && this.getName()
                        .equals(castOther.getName())))
                && ((this.getLat() == castOther.getLat()) || (this.getLat() != null
                        && castOther.getLat() != null && this.getLat().equals(
                        castOther.getLat())))
                && ((this.getLon() == castOther.getLon()) || (this.getLon() != null
                        && castOther.getLon() != null && this.getLon().equals(
                        castOther.getLon())))
                && ((this.getWfo() == castOther.getWfo()) || (this.getWfo() != null
                        && castOther.getWfo() != null && this.getWfo().equals(
                        castOther.getWfo())))
                && ((this.getHsa() == castOther.getHsa()) || (this.getHsa() != null
                        && castOther.getHsa() != null && this.getHsa().equals(
                        castOther.getHsa())))
                && ((this.getPost() == castOther.getPost()) || (this.getPost() != null
                        && castOther.getPost() != null && this.getPost()
                        .equals(castOther.getPost())))
                && ((this.getDispClass() == castOther.getDispClass()) || (this
                        .getDispClass() != null
                        && castOther.getDispClass() != null && this
                        .getDispClass().equals(castOther.getDispClass())))
                && ((this.getIsDcp() == castOther.getIsDcp()) || (this
                        .getIsDcp() != null
                        && castOther.getIsDcp() != null && this.getIsDcp()
                        .equals(castOther.getIsDcp())))
                && ((this.getIsObserver() == castOther.getIsObserver()) || (this
                        .getIsObserver() != null
                        && castOther.getIsObserver() != null && this
                        .getIsObserver().equals(castOther.getIsObserver())))
                && ((this.getTelemType() == castOther.getTelemType()) || (this
                        .getTelemType() != null
                        && castOther.getTelemType() != null && this
                        .getTelemType().equals(castOther.getTelemType())));
    }

    public int hashCode() {
        int result = 17;

        result = 37 * result
                + (getLid() == null ? 0 : this.getLid().hashCode());
        result = 37 * result
                + (getName() == null ? 0 : this.getName().hashCode());
        result = 37 * result
                + (getLat() == null ? 0 : this.getLat().hashCode());
        result = 37 * result
                + (getLon() == null ? 0 : this.getLon().hashCode());
        result = 37 * result
                + (getWfo() == null ? 0 : this.getWfo().hashCode());
        result = 37 * result
                + (getHsa() == null ? 0 : this.getHsa().hashCode());
        result = 37 * result
                + (getPost() == null ? 0 : this.getPost().hashCode());
        result = 37 * result
                + (getDispClass() == null ? 0 : this.getDispClass().hashCode());
        result = 37 * result
                + (getIsDcp() == null ? 0 : this.getIsDcp().hashCode());
        result = 37
                * result
                + (getIsObserver() == null ? 0 : this.getIsObserver()
                        .hashCode());
        result = 37 * result
                + (getTelemType() == null ? 0 : this.getTelemType().hashCode());
        return result;
    }

}
