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
package org.eobjects.analyzer.util;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.MetaModelException;
import org.eobjects.metamodel.QueryPostprocessDataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.HasNameMapper;

public class SchemaNavigatorTest extends TestCase {

    public void testSchemaWithDot() throws Exception {
        DataContext dc = DataContextFactory.createCsvDataContext(new File("src/test/resources/employees.csv"), ',',
                '\"');

        assertEquals(2, dc.getDefaultSchema().getTables()[0].getColumnCount());

        SchemaNavigator sn = new SchemaNavigator(dc);

        Column column = sn.convertToColumn("employees.csv.employees.email");
        assertEquals("Column[name=email,columnNumber=1,type=VARCHAR,nullable=true,nativeType=null,columnSize=null]",
                column.toString());
    }

    public void testColumnNamesWithDots() throws Exception {
        final MutableSchema schema = new MutableSchema("SCHE");
        schema.addTable(new MutableTable("tabl1").setSchema(schema));
        MutableTable orgTable = new MutableTable("tabl2").setSchema(schema);

        orgTable.addColumn(new MutableColumn("source_id").setTable(orgTable));
        orgTable.addColumn(new MutableColumn("BLOB.BLOBNumberMain").setTable(orgTable));
        orgTable.addColumn(new MutableColumn("BLOB.BLOBNumberBranch").setTable(orgTable));

        schema.addTable(orgTable);

        DataContext dataContext = new QueryPostprocessDataContext() {
            @Override
            protected DataSet materializeMainSchemaTable(Table table, Column[] columns, int maxRows) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String getMainSchemaName() throws MetaModelException {
                return schema.getName();
            }

            @Override
            protected Schema getMainSchema() throws MetaModelException {
                return schema;
            }
        };

        final SchemaNavigator schemaNavigator = new SchemaNavigator(dataContext);
        String[] columnNames = new String[] { "SCHE.tabl2.source_id", "SCHE.tabl2.BLOB.BLOBNumberMain",
                "SCHE.tabl2.BLOB.BLOBNumberBranch" };

        Column[] columnsResult = schemaNavigator.convertToColumns(columnNames);

        for (Column column : columnsResult) {
            assertNotNull(column);
        }

        List<String> columnNamesResult = CollectionUtils.map(columnsResult, new HasNameMapper());
        assertEquals("[source_id, BLOB.BLOBNumberMain, BLOB.BLOBNumberBranch]", columnNamesResult.toString());
    }
}
