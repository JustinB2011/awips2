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

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.raytheon.uf.common.registry.schemas.ebxml.util.annotations.RegistryObjectReference;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Represents invocation of a parameterized query.
 * 
 * 
 * <p>
 * Java class for QueryType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}ExtensibleObjectType">
 *       &lt;attribute name="queryDefinition" use="required" type="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:4.0}objectReferenceType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "Query")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType")
@DynamicSerialize
@Entity
@Cache(region = "registryObjects", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(schema = "ebxml", name = "Query")
public class QueryType extends ExtensibleObjectType {

    @Id
    @SequenceGenerator(name = "QueryTypeGenerator", schema = "ebxml", sequenceName = "ebxml.Query_sequence")
    @GeneratedValue(generator = "QueryTypeGenerator")
    @XmlTransient
    private Integer key;

    @XmlAttribute(required = true)
    @DynamicSerializeElement
    @RegistryObjectReference
    protected String queryDefinition;

    public QueryType() {

    }

    public QueryType(String queryDefinition, Collection<SlotType> slots) {
        super(slots);
        this.queryDefinition = queryDefinition;
    }

    public QueryType(String queryDefinition, SlotType... slots) {
        this.queryDefinition = queryDefinition;
        for (SlotType slot : slots) {
            this.getSlot().add(slot);
        }
    }

    /**
     * Gets the value of the queryDefinition property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * Sets the value of the queryDefinition property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setQueryDefinition(String value) {
        this.queryDefinition = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

}
