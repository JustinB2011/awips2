//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.12 at 11:26:14 AM EDT 
//


package gov.noaa.nws.ncep.ui.pgen.productTypes;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the types package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PgenClass }
     * 
     */
    public PgenClass createPgenClass() {
        return new PgenClass();
    }

    /**
     * Create an instance of {@link PgenControls }
     * 
     */
    public PgenControls createPgenControls() {
        return new PgenControls();
    }

    /**
     * Create an instance of {@link ProductTypes }
     * 
     */
    public ProductTypes createProductTypes() {
        return new ProductTypes();
    }

    /**
     * Create an instance of {@link PgenSave }
     * 
     */
    public PgenSave createPgenSave() {
        return new PgenSave();
    }

    /**
     * Create an instance of {@link PgenObjects }
     * 
     */
    public PgenObjects createPgenObjects() {
        return new PgenObjects();
    }

    /**
     * Create an instance of {@link ProductType }
     * 
     */
    public ProductType createProductType() {
        return new ProductType();
    }

    /**
     * Create an instance of {@link PgenActions }
     * 
     */
    public PgenActions createPgenActions() {
        return new PgenActions();
    }

    /**
     * Create an instance of {@link Color }
     * 
     */
    public Color createColor() {
        return new Color();
    }

    /**
     * Create an instance of {@link PgenLayer }
     * 
     */
    public PgenLayer createPgenLayer() {
        return new PgenLayer();
    }

}
