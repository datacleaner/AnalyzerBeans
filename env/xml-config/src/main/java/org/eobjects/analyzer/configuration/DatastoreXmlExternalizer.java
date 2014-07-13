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
package org.eobjects.analyzer.configuration;

import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.schema.TableType;
import org.eobjects.metamodel.util.FileResource;
import org.eobjects.metamodel.util.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

/**
 * Utility class for externalizing datastores to the XML format of conf.xml.
 * 
 * Generally speaking, XML elements created by this class, and placed in a the
 * <datastore-catalog> element of conf.xml, will be readable by
 * {@link JaxbConfigurationReader}.
 */
public class DatastoreXmlExternalizer {

    /**
     * Determines if the given datastore is externalizable by this object.
     * 
     * @param datastore
     * @return
     */
    public boolean isExternalizable(final Datastore datastore) {
        if (datastore == null) {
            return false;
        }

        if (datastore instanceof JdbcDatastore) {
            return true;
        }

        if (datastore instanceof CsvDatastore) {
            final Resource resource = ((CsvDatastore) datastore).getResource();
            if (resource instanceof FileResource) {
                return true;
            }
        }

        if (datastore instanceof ExcelDatastore) {
            final Resource resource = ((ExcelDatastore) datastore).getResource();
            if (resource instanceof FileResource) {
                return true;
            }
        }

        return false;
    }

    /**
     * Externalizes the given datastore
     * 
     * @param datastore
     * @return
     * @throws UnsupportedOperationException
     */
    public Element externalize(Datastore datastore) throws UnsupportedOperationException {
        return externalize(datastore, createDocument());
    }

    /**
     * Externalizes the given datastore
     * 
     * @param datastore
     * @param doc
     * @return
     * @throws UnsupportedOperationException
     */
    public Element externalize(final Datastore datastore, final Document doc) throws UnsupportedOperationException {
        if (datastore == null) {
            throw new IllegalArgumentException("Datastore cannot be null");
        }

        if (datastore instanceof CsvDatastore) {
            final Resource resource = ((CsvDatastore) datastore).getResource();
            final String filename = toFilename(resource);
            return externalize((CsvDatastore) datastore, filename, doc);
        }

        if (datastore instanceof ExcelDatastore) {
            final Resource resource = ((ExcelDatastore) datastore).getResource();
            final String filename = toFilename(resource);
            return externalize((ExcelDatastore) datastore, filename, doc);
        }

        if (datastore instanceof JdbcDatastore) {
            return externalize((JdbcDatastore) datastore, doc);
        }

        throw new UnsupportedOperationException("Non-supported datastore: " + datastore);
    }

    /**
     * Creates a filename string to externalize, based on a given
     * {@link Resource}.
     * 
     * @param resource
     * @return
     * @throws UnsupportedOperationException
     */
    protected String toFilename(final Resource resource) throws UnsupportedOperationException {
        if (resource instanceof FileResource) {
            return ((FileResource) resource).getFile().getPath();
        }

        throw new UnsupportedOperationException("Unsupported resource type: " + resource);
    }

    /**
     * Externalizes a {@link ExcelDatastore} to a XML element.
     * 
     * @param datastore
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @return
     */
    public Element externalize(ExcelDatastore datastore, String filename) {
        return externalize(datastore, filename, createDocument());
    }

    /**
     * Externalizes a {@link JdbcDatastore} to a XML element.
     * 
     * @param datastore
     * @return
     */
    public Element externalize(JdbcDatastore datastore) {
        return externalize(datastore, createDocument());
    }

    /**
     * Externalizes a {@link JdbcDatastore} to a XML element.
     * 
     * @param datastore
     * @param doc
     * @return
     */
    public Element externalize(JdbcDatastore datastore, Document doc) {
        final Element ds = doc.createElement("jdbc-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!StringUtils.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        String jndiUrl = datastore.getDatasourceJndiUrl();
        if (Strings.isNullOrEmpty(jndiUrl)) {
            appendElement(doc, ds, "url", datastore.getJdbcUrl());
            appendElement(doc, ds, "driver", datastore.getDriverClass());
            appendElement(doc, ds, "username", datastore.getUsername());
            appendElement(doc, ds, "password", datastore.getPassword());
            appendElement(doc, ds, "multiple-connections", datastore.isMultipleConnections() + "");
        } else {
            appendElement(doc, ds, "datasource-jndi-url", jndiUrl);
        }

        final TableType[] tableTypes = datastore.getTableTypes();
        if (tableTypes != null && tableTypes.length != 0 && !Arrays.equals(TableType.DEFAULT_TABLE_TYPES, tableTypes)) {
            final Element tableTypesElement = doc.createElement("table-types");
            ds.appendChild(tableTypesElement);

            for (final TableType tableType : tableTypes) {
                appendElement(doc, tableTypesElement, "table-type", tableType.name());
            }
        }

        final String catalogName = datastore.getCatalogName();
        if (!Strings.isNullOrEmpty(catalogName)) {
            appendElement(doc, ds, "catalog-name", catalogName);
        }

        return ds;
    }

    /**
     * Externalizes a {@link ExcelDatastore} to a XML element.
     * 
     * @param datastore
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @param doc
     * @return
     */
    public Element externalize(ExcelDatastore datastore, String filename, Document doc) {
        final Element ds = doc.createElement("excel-datastore");

        ds.setAttribute("name", datastore.getName());
        if (!StringUtils.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(doc, ds, "filename", filename);

        return ds;
    }

    /**
     * Externalizes a {@link CsvDatastore} to a XML element.
     * 
     * @param datastore
     *            the datastore to externalize
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @return a XML element representing the datastore.
     */
    public Element externalize(CsvDatastore datastore, String filename) {
        return externalize(datastore, filename, createDocument());
    }

    /**
     * Externalizes a {@link CsvDatastore} to a XML element.
     * 
     * @param datastore
     *            the datastore to externalize
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @param doc
     *            the document used to create the element.
     * @return a XML element representing the datastore.
     */
    public Element externalize(CsvDatastore datastore, String filename, Document doc) {
        final Element datastoreElement = doc.createElement("csv-datastore");
        datastoreElement.setAttribute("name", datastore.getName());

        final String description = datastore.getDescription();
        if (!StringUtils.isNullOrEmpty(description)) {
            datastoreElement.setAttribute("description", description);
        }

        appendElement(doc, datastoreElement, "filename", filename);
        appendElement(doc, datastoreElement, "quote-char", datastore.getQuoteChar());
        appendElement(doc, datastoreElement, "separator-char", datastore.getSeparatorChar());
        appendElement(doc, datastoreElement, "escape-char", datastore.getEscapeChar());
        appendElement(doc, datastoreElement, "encoding", datastore.getEncoding());
        appendElement(doc, datastoreElement, "fail-on-inconsistencies", datastore.isFailOnInconsistencies());
        appendElement(doc, datastoreElement, "multiline-values", datastore.isMultilineValues());
        appendElement(doc, datastoreElement, "header-line-number", datastore.getHeaderLineNumber());

        return datastoreElement;
    }

    private Document createDocument() {
        final DocumentBuilder documentBuilder;
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return documentBuilder.newDocument();
    }

    private void appendElement(Document doc, Element parent, String elementName, Object value) {
        if (value == null) {
            return;
        }

        String stringValue = value.toString();

        if (value instanceof Character) {
            final char c = ((Character) value).charValue();
            if (c == CsvConfiguration.NOT_A_CHAR) {
                stringValue = "NOT_A_CHAR";
            } else if (c == '\t') {
                stringValue = "\\t";
            } else if (c == '\n') {
                stringValue = "\\n";
            } else if (c == '\r') {
                stringValue = "\\r";
            }
        }

        final Element element = doc.createElement(elementName);
        element.setTextContent(stringValue);
        parent.appendChild(element);
    }
}
