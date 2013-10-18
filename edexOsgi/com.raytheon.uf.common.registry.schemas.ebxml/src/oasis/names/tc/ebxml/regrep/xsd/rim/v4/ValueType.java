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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.raytheon.uf.common.dataplugin.persist.IPersistableDataObject;
import com.raytheon.uf.common.registry.RegrepUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Abstract base type for all types of slot values.
 * 
 * 
 * <p>
 * Java class for ValueType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 2012                     bphillip    Initial implementation
 * 10/17/2013    1682       bphillip    Added software history
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
@XmlRootElement(name = "Value")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueType")
@XmlSeeAlso({ StringValueType.class, DateTimeValueType.class,
        InternationalStringValueType.class, VocabularyTermValueType.class,
        IntegerValueType.class, AnyValueType.class, BooleanValueType.class,
        FloatValueType.class, MapValueType.class, SlotValueType.class,
        DurationValueType.class, CollectionValueType.class })
@DynamicSerialize
@Entity
@Cache(region = RegrepUtil.DB_CACHE_REGION, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(schema = RegrepUtil.EBXML_SCHEMA, name = "Value")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ValueType implements IPersistableDataObject<Integer> {

    @Id
    @SequenceGenerator(name = "ValueTypeGenerator", schema = RegrepUtil.EBXML_SCHEMA, sequenceName = RegrepUtil.EBXML_SCHEMA
            + ".Value_sequence")
    @GeneratedValue(generator = "ValueTypeGenerator")
    @XmlTransient
    protected Integer key;

    public abstract <T extends Object> T getValue();

    public abstract void setValue(Object obj);

    public abstract String getColumnName();

    @Override
    public Integer getIdentifier() {
        return key;
    }

}
