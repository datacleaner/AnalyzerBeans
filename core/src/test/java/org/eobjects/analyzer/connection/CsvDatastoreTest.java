package org.eobjects.analyzer.connection;

import junit.framework.TestCase;

import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class CsvDatastoreTest extends TestCase {


    public void testConvertToColumnWithNoSchema() throws Exception {
        CsvDatastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");
        SchemaNavigator schemaNavigator = datastore.openConnection().getSchemaNavigator();
        Column col = schemaNavigator.convertToColumn("product");
        assertNotNull(col);

        Table table = datastore.openConnection().getDataContext().getDefaultSchema().getTables()[0];
        assertEquals("projects", table.getName());
        col = schemaNavigator.convertToColumn("projects.product");
        assertNotNull(col);
    }
}
