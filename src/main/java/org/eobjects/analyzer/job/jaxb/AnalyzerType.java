//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.09.16 at 09:34:58 PM CEST 
//


package org.eobjects.analyzer.job.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for analyzerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="analyzerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="descriptor" type="{http://eobjects.org/analyzerbeans/job/1.0}analyzerDescriptorType"/>
 *         &lt;element name="properties" type="{http://eobjects.org/analyzerbeans/job/1.0}configuredPropertiesType" minOccurs="0"/>
 *         &lt;element name="input" type="{http://eobjects.org/analyzerbeans/job/1.0}inputType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "analyzerType", propOrder = {
    "descriptor",
    "properties",
    "input"
})
public class AnalyzerType {

    @XmlElement(required = true)
    protected AnalyzerDescriptorType descriptor;
    protected ConfiguredPropertiesType properties;
    protected List<InputType> input;

    /**
     * Gets the value of the descriptor property.
     * 
     * @return
     *     possible object is
     *     {@link AnalyzerDescriptorType }
     *     
     */
    public AnalyzerDescriptorType getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the value of the descriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnalyzerDescriptorType }
     *     
     */
    public void setDescriptor(AnalyzerDescriptorType value) {
        this.descriptor = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link ConfiguredPropertiesType }
     *     
     */
    public ConfiguredPropertiesType getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfiguredPropertiesType }
     *     
     */
    public void setProperties(ConfiguredPropertiesType value) {
        this.properties = value;
    }

    /**
     * Gets the value of the input property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the input property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInput().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InputType }
     * 
     * 
     */
    public List<InputType> getInput() {
        if (input == null) {
            input = new ArrayList<InputType>();
        }
        return this.input;
    }

}
