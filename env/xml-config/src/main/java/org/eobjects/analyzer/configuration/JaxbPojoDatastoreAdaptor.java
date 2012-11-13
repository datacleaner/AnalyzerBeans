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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eobjects.analyzer.configuration.jaxb.AbstractDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.PojoDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.analyzer.util.convert.StringConverter;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.MetaModelHelper;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.pojo.ArrayTableDataProvider;
import org.eobjects.metamodel.pojo.TableDataProvider;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.SimpleTableDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenient utility class for reading and writing POJO datastores from and to
 * XML (JAXB) elements.
 */
public class JaxbPojoDatastoreAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(JaxbPojoDatastoreAdaptor.class);

    private final StringConverter _converter;

    public JaxbPojoDatastoreAdaptor() {
        _converter = new StringConverter(null);
    }

    public PojoDatastore read(PojoDatastoreType pojoDatastore) {
        final String name = pojoDatastore.getName();
        final String schemaName = (pojoDatastore.getSchemaName() == null ? name : pojoDatastore.getSchemaName());

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>();
        final List<PojoTableType> tables = pojoDatastore.getTable();
        for (PojoTableType table : tables) {
            final String tableName = table.getName();

            final List<Columns.Column> columns = table.getColumns().getColumn();
            final int columnCount = columns.size();
            final String[] columnNames = new String[columnCount];
            final ColumnType[] columnTypes = new ColumnType[columnCount];

            for (int i = 0; i < columnCount; i++) {
                final Columns.Column column = columns.get(i);
                columnNames[i] = column.getName();
                columnTypes[i] = ColumnType.valueOf(column.getType());
            }

            final SimpleTableDef tableDef = new SimpleTableDef(tableName, columnNames, columnTypes);

            final Collection<Object[]> arrays = new ArrayList<Object[]>();
            final List<Rows.Row> rows = table.getRows().getRow();
            for (Rows.Row row : rows) {
                final List<Object> values = row.getV();
                if (values.size() != columnCount) {
                    throw new IllegalStateException("Row value count is not equal to column count in datastore '"
                            + name + "'. Expected " + columnCount + " values, found " + values.size() + " (table "
                            + tableName + ", row no. " + arrays.size() + ")");
                }
                final Object[] array = new Object[columnCount];
                for (int i = 0; i < array.length; i++) {

                    final Class<?> expectedClass = columnTypes[i].getJavaEquivalentClass();

                    final Object rawValue = values.get(i);
                    final Object value = deserializeValue(rawValue, expectedClass);
                    array[i] = value;
                }
                arrays.add(array);
            }

            final TableDataProvider<?> tableDataProvider = new ArrayTableDataProvider(tableDef, arrays);
            tableDataProviders.add(tableDataProvider);
        }

        final PojoDatastore ds = new PojoDatastore(name, schemaName, tableDataProviders);
        return ds;
    }

    private Object deserializeValue(final Object value, Class<?> expectedClass) {
        if (value == null) {
            return null;
        }

        if (value instanceof Node) {
            final Node node = (Node) value;
            logger.debug("Value is a DOM node: {}", node);
            return getNodeValue(node, expectedClass);
        }

        if (value instanceof JAXBElement) {
            final JAXBElement<?> element = (JAXBElement<?>) value;
            logger.debug("Value is a JAXBElement: {}", element);
            final Object jaxbValue = element.getValue();
            return deserializeValue(jaxbValue, expectedClass);
        }

        if (value instanceof String) {
            String str = (String) value;
            return _converter.deserialize(str, expectedClass);
        } else {
            throw new UnsupportedOperationException("Unknown value type: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getNodeValue(Node node, Class<T> expectedClass) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            final String str = node.getNodeValue();
            final Class<T> typeToReturn = (Class<T>) (expectedClass == null ? String.class : expectedClass);
            return _converter.deserialize(str, typeToReturn);
        }

        // a top-level value
        final List<Node> childNodes = getChildNodes(node);
        switch (childNodes.size()) {
        case 0:
            return null;
        case 1:
            final Node child = childNodes.get(0);
            return getNodeValue(child, expectedClass);
        default:
            if (expectedClass == null) {
                final Node firstChild = childNodes.get(0);
                if ("i".equals(firstChild.getNodeName())) {
                    final List<Object> list = getNodeList(childNodes);
                    return (T) list;
                } else {
                    final Map<String, Object> map = getNodeMap(childNodes);
                    return (T) map;
                }
            } else if (ReflectionUtils.is(expectedClass, List.class)) {
                final List<Object> list = getNodeList(childNodes);
                return (T) list;
            } else if (ReflectionUtils.is(expectedClass, Map.class)) {
                final Map<String, Object> map = getNodeMap(childNodes);
                return (T) map;
            }
        }

        throw new UnsupportedOperationException("Not a value (v) node type: " + node);
    }

    private List<Object> getNodeList(List<Node> childNodes) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<Node> getChildNodes(Node node) {
        final List<Node> list = new ArrayList<Node>();
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            switch (child.getNodeType()) {
            case Node.ELEMENT_NODE:
                list.add(child);
            case Node.TEXT_NODE:
                String text = child.getNodeValue();
                if (!StringUtils.isNullOrEmpty(text)) {
                    list.add(child);
                }
            default: // ignore
            }
        }
        return list;
    }

    private Map<String, Object> getNodeMap(List<Node> entryNodes) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Node entryNode : entryNodes) {
            assert "e".equals(entryNode.getNodeName());

            String key = null;
            Object value = null;

            final List<Node> keyOrValueNodes = getChildNodes(entryNode);

            assert keyOrValueNodes.size() == 2;

            for (Node keyOrValueNode : keyOrValueNodes) {
                final String keyOrValueNodeName = keyOrValueNode.getNodeName();
                if ("k".equals(keyOrValueNodeName)) {
                    key = getNodeValue(keyOrValueNode, String.class);
                } else if ("v".equals(keyOrValueNodeName)) {
                    value = getNodeValue(keyOrValueNode, null);
                }
            }

            if (key == null) {
                throw new UnsupportedOperationException("Map key (k) node not set in entry: " + entryNode);
            }

            map.put(key, value);
        }
        return map;
    }

    private org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row createPojoRow(Row row, Document document) {
        final org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row rowType = new org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row();
        final Object[] values = row.getValues();
        for (Object value : values) {
            final Element elem = document.createElement("v");
            createPojoValue(value, elem, document);
            rowType.getV().add(elem);
        }
        return rowType;
    }

    private void createPojoValue(Object value, Element elem, Document document) {
        if (value == null) {
            // return an empty element
            return;
        }

        if (value.getClass().isArray()) {
            value = CollectionUtils.toList(value);
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
                final Element itemElement = document.createElement("i");
                createPojoValue(item, itemElement, document);
                elem.appendChild(itemElement);
            }
            return;
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Entry<?, ?> entry : map.entrySet()) {
                final Element keyElement = document.createElement("k");
                createPojoValue(entry.getKey(), keyElement, document);

                final Element valueElement = document.createElement("v");
                createPojoValue(entry.getValue(), valueElement, document);

                final Element entryElement = document.createElement("e");
                entryElement.appendChild(keyElement);
                entryElement.appendChild(valueElement);

                elem.appendChild(entryElement);
            }
            return;
        }

        final String stringValue = _converter.serialize(value);
        elem.setTextContent(stringValue);
        return;
    }

    protected DocumentBuilder createDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create DocumentBuilder", e);
        }
    }

    private org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column createPojoColumn(String name,
            ColumnType type) {
        org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column columnType = new org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column();
        columnType.setName(name);
        columnType.setType(type.toString());
        return columnType;
    }

    private PojoTableType createPojoTable(final DataContext dataContext, final Table table, final Column[] usedColumns,
            int maxRows) {
        final PojoTableType tableType = new PojoTableType();
        tableType.setName(table.getName());

        // read columns
        final Columns columnsType = new Columns();
        for (Column column : usedColumns) {
            columnsType.getColumn().add(createPojoColumn(column.getName(), column.getType()));
        }
        tableType.setColumns(columnsType);

        // read values
        final Query q = dataContext.query().from(table).select(usedColumns).toQuery();
        q.setMaxRows(maxRows);

        final DocumentBuilder documentBuilder = createDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        final Rows rowsType = new Rows();
        final DataSet ds = dataContext.executeQuery(q);
        try {
            while (ds.next()) {
                Row row = ds.getRow();
                rowsType.getRow().add(createPojoRow(row, document));
            }
        } finally {
            ds.close();
        }

        tableType.setRows(rowsType);

        return tableType;
    }

    public AbstractDatastoreType createPojoDatastore(Datastore datastore, Set<Column> columns, int maxRowsToQuery) {
        final PojoDatastoreType datastoreType = new PojoDatastoreType();
        datastoreType.setName(datastore.getName());
        datastoreType.setDescription(datastore.getDescription());

        final DatastoreConnection con = datastore.openConnection();
        try {
            final DataContext dataContext = con.getDataContext();

            final Schema schema;
            final Table[] tables;
            if (columns == null || columns.isEmpty()) {
                schema = dataContext.getDefaultSchema();
                tables = schema.getTables();
            } else {
                tables = MetaModelHelper.getTables(columns);
                // TODO: There's a possibility that tables span multiple
                // schemas, but we cannot currently support that in a
                // PojoDatastore, so we just pick the first and cross our
                // fingers.
                schema = tables[0].getSchema();
            }

            datastoreType.setSchemaName(schema.getName());

            for (final Table table : tables) {
                final Column[] usedColumns;
                if (columns == null || columns.isEmpty()) {
                    usedColumns = table.getColumns();
                } else {
                    usedColumns = MetaModelHelper.getTableColumns(table, columns);
                }

                final PojoTableType tableType = createPojoTable(dataContext, table, usedColumns, maxRowsToQuery);
                datastoreType.getTable().add(tableType);
            }
        } finally {
            con.close();
        }

        return datastoreType;
    }
}
