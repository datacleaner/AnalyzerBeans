/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.02 at 04:34:15 PM CET 
//


package org.eobjects.analyzer.configuration.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for combinedStorageProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="combinedStorageProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="collections-storage" type="{http://eobjects.org/analyzerbeans/configuration/1.0}storageProviderType"/>
 *         &lt;element name="row-annotation-storage" type="{http://eobjects.org/analyzerbeans/configuration/1.0}storageProviderType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "combinedStorageProviderType", propOrder = {
    "collectionsStorage",
    "rowAnnotationStorage"
})
public class CombinedStorageProviderType {

    @XmlElement(name = "collections-storage", required = true)
    protected StorageProviderType collectionsStorage;
    @XmlElement(name = "row-annotation-storage", required = true)
    protected StorageProviderType rowAnnotationStorage;

    /**
     * Gets the value of the collectionsStorage property.
     * 
     * @return
     *     possible object is
     *     {@link StorageProviderType }
     *     
     */
    public StorageProviderType getCollectionsStorage() {
        return collectionsStorage;
    }

    /**
     * Sets the value of the collectionsStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link StorageProviderType }
     *     
     */
    public void setCollectionsStorage(StorageProviderType value) {
        this.collectionsStorage = value;
    }

    /**
     * Gets the value of the rowAnnotationStorage property.
     * 
     * @return
     *     possible object is
     *     {@link StorageProviderType }
     *     
     */
    public StorageProviderType getRowAnnotationStorage() {
        return rowAnnotationStorage;
    }

    /**
     * Sets the value of the rowAnnotationStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link StorageProviderType }
     *     
     */
    public void setRowAnnotationStorage(StorageProviderType value) {
        this.rowAnnotationStorage = value;
    }

}
