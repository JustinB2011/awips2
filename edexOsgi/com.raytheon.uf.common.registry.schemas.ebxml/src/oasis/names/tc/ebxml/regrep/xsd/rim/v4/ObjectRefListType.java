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

package oasis.names.tc.ebxml.regrep.xsd.rim.v4;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * A list of ObjectRefType instances.
 * 
 * 
 * <p>
 * Java class for ObjectRefListType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ObjectRefListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ObjectRef" type="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}ObjectRefType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "ObjectRefList")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectRefListType", propOrder = { "objectRef" })
@DynamicSerialize
@Entity
@Cache(region = "registryObjects", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(schema = "ebxml", name = "ObjectRefList")
public class ObjectRefListType {

    @Id
    @SequenceGenerator(name = "ObjectRefListTypeGenerator", schema = "ebxml", sequenceName = "ebxml.ObjectRefList_sequence")
    @GeneratedValue(generator = "ObjectRefListTypeGenerator")
    @XmlTransient
    private Integer key;

    @XmlElement(name = "ObjectRef")
    @DynamicSerializeElement
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(schema = "ebxml")
    protected List<ObjectRefType> objectRef;

    public Integer getKey() {
        return key;
    }

    /**
     * Gets the value of the objectRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the objectRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getObjectRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ObjectRefType }
     * 
     * 
     */
    public List<ObjectRefType> getObjectRef() {
        if (objectRef == null) {
            objectRef = new ArrayList<ObjectRefType>();
        }
        return this.objectRef;
    }

    public void setObjectRef(List<ObjectRefType> objectRef) {
        this.objectRef = objectRef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((objectRef == null) ? 0 : objectRef.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ObjectRefListType other = (ObjectRefListType) obj;
        if (objectRef == null) {
            if (other.objectRef != null) {
                return false;
            }
        } else if (!objectRef.equals(other.objectRef)) {
            return false;
        }
        return true;
    }

}
