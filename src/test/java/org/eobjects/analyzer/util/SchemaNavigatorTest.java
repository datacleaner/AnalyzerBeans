package org.eobjects.analyzer.util;

import java.io.File;

import org.eobjects.analyzer.connection.CsvDatastore;

import junit.framework.TestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.schema.Column;

public class SchemaNavigatorTest extends TestCase {

	public void testSchemaWithDot() throws Exception {
		DataContext dc = DataContextFactory.createCsvDataContext(new File(
				"src/test/resources/employees.csv"), ',', '\"', false);

		assertEquals(2, dc.getDefaultSchema().getTables()[0].getColumnCount());

		SchemaNavigator sn = new SchemaNavigator(dc);

		Column column = sn.convertToColumn("employees.csv.employees.email");
		assertEquals(
				"Column[name=email,columnNumber=1,type=VARCHAR,nullable=true,indexed=false,nativeType=<null>,columnSize=<null>]",
				column.toString());
	}
	
	public void testConvertToColumnWithNoSchemaOrTable() throws Exception {
		CsvDatastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");
		SchemaNavigator schemaNavigator = datastore.getDataContextProvider().getSchemaNavigator();
		Column col = schemaNavigator.convertToColumn("product");
		assertNotNull(col);
	}
}
