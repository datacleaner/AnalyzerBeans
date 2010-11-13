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
// Generated on: 2010.11.01 at 10:47:51 AM CET 
//


package org.eobjects.analyzer.configuration.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for datastoreCatalogType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="datastoreCatalogType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="jdbc-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}jdbcDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="access-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}accessDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="csv-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}csvDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="excel-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}excelDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dbase-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}dbaseDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="odb-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}openOfficeDatabaseDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="custom-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}customElementType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="composite-datastore" type="{http://eobjects.org/analyzerbeans/configuration/1.0}compositeDatastoreType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "datastoreCatalogType", propOrder = {
    "jdbcDatastoreOrAccessDatastoreOrCsvDatastore"
})
public class DatastoreCatalogType {

    @XmlElements({
        @XmlElement(name = "dbase-datastore", type = DbaseDatastoreType.class),
        @XmlElement(name = "composite-datastore", type = CompositeDatastoreType.class),
        @XmlElement(name = "custom-datastore", type = CustomElementType.class),
        @XmlElement(name = "jdbc-datastore", type = JdbcDatastoreType.class),
        @XmlElement(name = "excel-datastore", type = ExcelDatastoreType.class),
        @XmlElement(name = "csv-datastore", type = CsvDatastoreType.class),
        @XmlElement(name = "odb-datastore", type = OpenOfficeDatabaseDatastoreType.class),
        @XmlElement(name = "access-datastore", type = AccessDatastoreType.class)
    })
    protected List<Object> jdbcDatastoreOrAccessDatastoreOrCsvDatastore;

    /**
     * Gets the value of the jdbcDatastoreOrAccessDatastoreOrCsvDatastore property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jdbcDatastoreOrAccessDatastoreOrCsvDatastore property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DbaseDatastoreType }
     * {@link CompositeDatastoreType }
     * {@link CustomElementType }
     * {@link JdbcDatastoreType }
     * {@link ExcelDatastoreType }
     * {@link CsvDatastoreType }
     * {@link OpenOfficeDatabaseDatastoreType }
     * {@link AccessDatastoreType }
     * 
     * 
     */
    public List<Object> getJdbcDatastoreOrAccessDatastoreOrCsvDatastore() {
        if (jdbcDatastoreOrAccessDatastoreOrCsvDatastore == null) {
            jdbcDatastoreOrAccessDatastoreOrCsvDatastore = new ArrayList<Object>();
        }
        return this.jdbcDatastoreOrAccessDatastoreOrCsvDatastore;
    }

}
