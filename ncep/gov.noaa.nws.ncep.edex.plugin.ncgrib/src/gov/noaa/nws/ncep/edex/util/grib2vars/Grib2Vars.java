//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.07 at 02:01:31 PM EST 
//


package gov.noaa.nws.ncep.edex.util.grib2vars;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}g2varsid" minOccurs="0"/>
 *         &lt;element ref="{}discipline" minOccurs="0"/>
 *         &lt;element ref="{}category" minOccurs="0"/>
 *         &lt;element ref="{}pid" minOccurs="0"/>
 *         &lt;element ref="{}pdt" minOccurs="0"/>
 *         &lt;element ref="{}name" minOccurs="0"/>
 *         &lt;element ref="{}units" minOccurs="0"/>
 *         &lt;element ref="{}gnam" minOccurs="0"/>
 *         &lt;element ref="{}scale" minOccurs="0"/>
 *         &lt;element ref="{}missing" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "g2Varsid",
    "discipline",
    "category",
    "pid",
    "pdt",
    "name",
    "units",
    "gnam",
    "scale",
    "missing"
})
@XmlRootElement(name = "grib2vars")
public class Grib2Vars {

    @XmlElement(name = "g2varsid")
    protected Integer g2Varsid;
    protected Integer discipline;
    protected Integer category;
    protected Integer pid;
    protected Integer pdt;
    protected String name;
    protected String units;
    protected String gnam;
    protected Integer scale;
    protected Float missing;

    /**
     * Gets the value of the g2Varsid property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getG2Varsid() {
        return g2Varsid;
    }

    /**
     * Sets the value of the g2Varsid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setG2Varsid(Integer value) {
        this.g2Varsid = value;
    }

    /**
     * Gets the value of the discipline property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDiscipline() {
        return discipline;
    }

    /**
     * Sets the value of the discipline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDiscipline(Integer value) {
        this.discipline = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCategory(Integer value) {
        this.category = value;
    }

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPid(Integer value) {
        this.pid = value;
    }

    /**
     * Gets the value of the pdt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPdt() {
        return pdt;
    }

    /**
     * Sets the value of the pdt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPdt(Integer value) {
        this.pdt = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnits(String value) {
        this.units = value;
    }

    /**
     * Gets the value of the gnam property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGnam() {
        return gnam;
    }

    /**
     * Sets the value of the gnam property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGnam(String value) {
        this.gnam = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScale(Integer value) {
        this.scale = value;
    }

    /**
     * Gets the value of the missing property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMissing() {
        return missing;
    }

    /**
     * Sets the value of the missing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMissing(Float value) {
        this.missing = value;
    }

}
