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

import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Rejecteddata generated by hbm2java
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2008                        Initial generation by hbm2java
 * Aug 19, 2011      10672     jkorman Move refactor to new project
 * Oct 07, 2013       2361     njensen Removed XML annotations
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.1
 */
@Entity
@Table(name = "rejecteddata")
@com.raytheon.uf.common.serialization.annotations.DynamicSerialize
public class Rejecteddata extends com.raytheon.uf.common.dataplugin.persist.PersistableDataObject implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private RejecteddataId id;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Double value;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Short revision;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String shefQualCode;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String productId;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Date producttime;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private Integer qualityCode;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String rejectType;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String userid;

    public Rejecteddata() {
    }

    public Rejecteddata(RejecteddataId id) {
        this.id = id;
    }

    public Rejecteddata(RejecteddataId id, Double value, Short revision,
            String shefQualCode, String productId, Date producttime,
            Integer qualityCode, String rejectType, String userid) {
        this.id = id;
        this.value = value;
        this.revision = revision;
        this.shefQualCode = shefQualCode;
        this.productId = productId;
        this.producttime = producttime;
        this.qualityCode = qualityCode;
        this.rejectType = rejectType;
        this.userid = userid;
    }

    @EmbeddedId
    @AttributeOverrides( {
            @AttributeOverride(name = "lid", column = @Column(name = "lid", nullable = false, length = 8)),
            @AttributeOverride(name = "pe", column = @Column(name = "pe", nullable = false, length = 2)),
            @AttributeOverride(name = "dur", column = @Column(name = "dur", nullable = false)),
            @AttributeOverride(name = "ts", column = @Column(name = "ts", nullable = false, length = 2)),
            @AttributeOverride(name = "extremum", column = @Column(name = "extremum", nullable = false, length = 1)),
            @AttributeOverride(name = "probability", column = @Column(name = "probability", nullable = false, precision = 8, scale = 8)),
            @AttributeOverride(name = "validtime", column = @Column(name = "validtime", nullable = false, length = 29)),
            @AttributeOverride(name = "basistime", column = @Column(name = "basistime", nullable = false, length = 29)),
            @AttributeOverride(name = "postingtime", column = @Column(name = "postingtime", nullable = false, length = 29)) })
    public RejecteddataId getId() {
        return this.id;
    }

    public void setId(RejecteddataId id) {
        this.id = id;
    }

    @Column(name = "value", precision = 17, scale = 17)
    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Column(name = "revision")
    public Short getRevision() {
        return this.revision;
    }

    public void setRevision(Short revision) {
        this.revision = revision;
    }

    @Column(name = "shef_qual_code", length = 1)
    public String getShefQualCode() {
        return this.shefQualCode;
    }

    public void setShefQualCode(String shefQualCode) {
        this.shefQualCode = shefQualCode;
    }

    @Column(name = "product_id", length = 10)
    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "producttime", length = 29)
    public Date getProducttime() {
        return this.producttime;
    }

    public void setProducttime(Date producttime) {
        this.producttime = producttime;
    }

    @Column(name = "quality_code")
    public Integer getQualityCode() {
        return this.qualityCode;
    }

    public void setQualityCode(Integer qualityCode) {
        this.qualityCode = qualityCode;
    }

    @Column(name = "reject_type", length = 1)
    public String getRejectType() {
        return this.rejectType;
    }

    public void setRejectType(String rejectType) {
        this.rejectType = rejectType;
    }

    @Column(name = "userid", length = 32)
    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

}
