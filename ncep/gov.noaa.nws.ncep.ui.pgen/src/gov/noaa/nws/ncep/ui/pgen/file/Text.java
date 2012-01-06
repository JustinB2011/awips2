//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.14 at 10:56:13 AM EDT 
//


package gov.noaa.nws.ncep.ui.pgen.file;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}Color" maxOccurs="unbounded"/>
 *         &lt;element ref="{}Point"/>
 *         &lt;element name="textLine" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="auto" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="hide" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="xOffset" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="yOffset" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="displayType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mask" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="rotationRelativity" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="rotation" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="justification" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="style" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fontName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fontSize" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="pgenType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pgenCategory" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "color",
    "point",
    "textLine"
})
@XmlRootElement(name = "Text")
public class Text {

    @XmlElement(name = "Color", required = true)
    protected List<Color> color;
    @XmlElement(name = "Point", required = true)
    protected Point point;
    @XmlElement(required = true)
    protected List<String> textLine;
    @XmlAttribute
    protected Boolean auto;
    @XmlAttribute
    protected Boolean hide;
    @XmlAttribute
    protected Integer xOffset;
    @XmlAttribute
    protected Integer yOffset;
    @XmlAttribute
    protected String displayType;
    @XmlAttribute
    protected Boolean mask;
    @XmlAttribute
    protected String rotationRelativity;
    @XmlAttribute
    protected Double rotation;
    @XmlAttribute
    protected String justification;
    @XmlAttribute
    protected String style;
    @XmlAttribute
    protected String fontName;
    @XmlAttribute
    protected Float fontSize;
    @XmlAttribute
    protected String pgenType;
    @XmlAttribute
    protected String pgenCategory;

    /**
     * Gets the value of the color property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the color property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Color }
     * 
     * 
     */
    public List<Color> getColor() {
        if (color == null) {
            color = new ArrayList<Color>();
        }
        return this.color;
    }

    /**
     * Gets the value of the point property.
     * 
     * @return
     *     possible object is
     *     {@link Point }
     *     
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Sets the value of the point property.
     * 
     * @param value
     *     allowed object is
     *     {@link Point }
     *     
     */
    public void setPoint(Point value) {
        this.point = value;
    }

    /**
     * Gets the value of the textLine property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the textLine property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTextLine().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTextLine() {
        if (textLine == null) {
            textLine = new ArrayList<String>();
        }
        return this.textLine;
    }

    /**
     * Gets the value of the auto property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuto() {
        return auto;
    }

    /**
     * Sets the value of the auto property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuto(Boolean value) {
        this.auto = value;
    }

    /**
     * Gets the value of the hide property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHide() {
        return hide;
    }

    /**
     * Sets the value of the hide property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHide(Boolean value) {
        this.hide = value;
    }

    /**
     * Gets the value of the xOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getXOffset() {
        return xOffset;
    }

    /**
     * Sets the value of the xOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setXOffset(Integer value) {
        this.xOffset = value;
    }

    /**
     * Gets the value of the yOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getYOffset() {
        return yOffset;
    }

    /**
     * Sets the value of the yOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setYOffset(Integer value) {
        this.yOffset = value;
    }

    /**
     * Gets the value of the displayType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * Sets the value of the displayType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayType(String value) {
        this.displayType = value;
    }

    /**
     * Gets the value of the mask property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMask() {
        return mask;
    }

    /**
     * Sets the value of the mask property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMask(Boolean value) {
        this.mask = value;
    }

    /**
     * Gets the value of the rotationRelativity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotationRelativity() {
        return rotationRelativity;
    }

    /**
     * Sets the value of the rotationRelativity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotationRelativity(String value) {
        this.rotationRelativity = value;
    }

    /**
     * Gets the value of the rotation property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRotation() {
        return rotation;
    }

    /**
     * Sets the value of the rotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRotation(Double value) {
        this.rotation = value;
    }

    /**
     * Gets the value of the justification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJustification() {
        return justification;
    }

    /**
     * Sets the value of the justification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJustification(String value) {
        this.justification = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the fontName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Sets the value of the fontName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontName(String value) {
        this.fontName = value;
    }

    /**
     * Gets the value of the fontSize property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFontSize() {
        return fontSize;
    }

    /**
     * Sets the value of the fontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFontSize(Float value) {
        this.fontSize = value;
    }

    /**
     * Gets the value of the pgenType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPgenType() {
        return pgenType;
    }

    /**
     * Sets the value of the pgenType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPgenType(String value) {
        this.pgenType = value;
    }

    /**
     * Gets the value of the pgenCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPgenCategory() {
        return pgenCategory;
    }

    /**
     * Sets the value of the pgenCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPgenCategory(String value) {
        this.pgenCategory = value;
    }

}
