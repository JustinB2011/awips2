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
 /**
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 1, 2011            bclement     Initial creation
 *
 **/

package com.raytheon.uf.edex.ogc.common.db;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class SimpleDimension implements Comparable<SimpleDimension> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

    @Column
	@XmlElement
	@DynamicSerializeElement
	protected String name;

	@Column
	@XmlElement
	@DynamicSerializeElement
	protected String units;

	/**
	 * @return live reference to values set, should not return null
	 */
	public abstract Set<String> getValues();

	/**
	 * @param layer
	 *            the layer that this dimension belongs to. Used by method to
	 *            determine the best default for dimension.
	 * @return
	 */
	public abstract String getDefaultValue(SimpleLayer layer);

	public SimpleDimension() {
	}

	/**
	 * @param lowest
	 *            set true to return lowest value, otherwise highest is returned
	 * @return null if there are no values for this dimension
	 */
	protected String getDouble(boolean lowest) {
		Double rval = getValue(lowest);
		return rval != null ? rval.toString() : null;
	}

	/**
	 * @param lowest
	 *            set true to return lowest value, otherwise highest is returned
	 * @return null if there are no values for this dimension
	 */
	protected Double getValue(boolean lowest) {
		Double rval;
		TreeSet<Double> vals = LayerTransformer.getDimValuesAsDouble(this);
		if (vals.isEmpty()) {
			rval = null;
		} else {
			rval = lowest ? vals.first() : vals.last();
		}
		return rval;
	}

	/**
	 * @param lowest
	 *            set true to return lowest value, otherwise highest is returned
	 * @return null if there are no values for this dimension
	 */
	protected String getInt(boolean lowest) {
		Double val = getValue(lowest);
		return val != null ? String.valueOf(val.intValue()) : null;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    @Override
    public int compareTo(SimpleDimension o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleDimension other = (SimpleDimension) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
