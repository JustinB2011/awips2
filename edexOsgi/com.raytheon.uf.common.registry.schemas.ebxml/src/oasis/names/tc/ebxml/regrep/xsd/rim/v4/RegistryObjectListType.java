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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import org.hibernate.annotations.Cascade;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Represents a list of RegistryObjectType instances.
 * 
 * 
 * <p>
 * Java class for RegistryObjectListType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="RegistryObjectListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}RegistryObject" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "RegistryObjectList")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryObjectListType", propOrder = { "registryObject" })
@DynamicSerialize
@Entity
@Cache(region = "registryObjects", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(schema = "ebxml", name = "RegistryObjectList")
public class RegistryObjectListType implements Serializable {

    private static final long serialVersionUID = -254507015539461400L;

    @Id
    @SequenceGenerator(name = "RegistryObjectListTypeGenerator", schema = "ebxml", sequenceName = "ebxml.RegistryObjectList_sequence")
    @GeneratedValue(generator = "RegistryObjectListTypeGenerator")
    @XmlTransient
    private Integer key;

    @ManyToMany
    @Cascade({})
    @JoinTable(schema = "ebxml")
    @XmlElement(name = "RegistryObject")
    @DynamicSerializeElement
    protected List<RegistryObjectType> registryObject;

    /**
     * Constructor.
     */
    public RegistryObjectListType() {

    }

    /**
     * Constructor.
     * 
     * @param registryObjects
     *            the collection of registry objects
     */
    public RegistryObjectListType(List<RegistryObjectType> registryObjects) {
        // Defensive list copy, not using the original list
        this.registryObject = new ArrayList<RegistryObjectType>(registryObjects);
    }

    public RegistryObjectListType(RegistryObjectType registryObject) {
        List<RegistryObjectType> list = new ArrayList<RegistryObjectType>(1);
        list.add(registryObject);
        this.registryObject = list;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    /**
     * Gets the value of the registryObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the registryObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getRegistryObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegistryObjectType }
     * 
     * 
     */
    public List<RegistryObjectType> getRegistryObject() {
        if (registryObject == null) {
            registryObject = new ArrayList<RegistryObjectType>();
        }
        return this.registryObject;
    }

    public void setRegistryObject(List<RegistryObjectType> registryObject) {
        this.registryObject = registryObject;
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
                + ((registryObject == null) ? 0 : registryObject.hashCode());
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
        RegistryObjectListType other = (RegistryObjectListType) obj;
        if (registryObject == null) {
            if (other.registryObject != null) {
                return false;
            }
        } else if (!registryObject.equals(other.registryObject)) {
            return false;
        }
        return true;
    }

}
