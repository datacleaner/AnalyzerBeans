package org.eobjects.analyzer.connection;

import dk.eobjects.metamodel.schema.Column;
import junit.framework.TestCase;

public class ExcelDatastoreTest extends TestCase {

	public void testOpenSpreadsheetXls() throws Exception {
		Datastore datastore = new ExcelDatastore("foobar", "src/test/resources/Spreadsheet2003.xls");
		assertEquals("foobar", datastore.getName());
		DataContextProvider dcp = datastore.getDataContextProvider();
		assertNotNull(dcp);

		Column col1 = dcp.getSchemaNavigator().convertToColumn("string");
		assertNotNull(col1);

		Column col2 = dcp.getSchemaNavigator().convertToColumn("number");
		assertNotNull(col2);

		Column col3 = dcp.getSchemaNavigator().convertToColumn("date");
		assertNotNull(col3);
		assertEquals(
				"Column[name=date,columnNumber=2,type=VARCHAR,nullable=true,indexed=false,nativeType=null,columnSize=null]",
				col3.toString());
	}

	public void testOpenSpreadsheetXlsx() throws Exception {
		Datastore datastore = new ExcelDatastore("foobar", "src/test/resources/Spreadsheet2007.xlsx");
		assertEquals("foobar", datastore.getName());
		DataContextProvider dcp = datastore.getDataContextProvider();
		assertNotNull(dcp);

		Column col1 = dcp.getSchemaNavigator().convertToColumn("string");
		assertNotNull(col1);

		Column col2 = dcp.getSchemaNavigator().convertToColumn("number");
		assertNotNull(col2);

		Column col3 = dcp.getSchemaNavigator().convertToColumn("date");
		assertNotNull(col3);
		assertEquals(
				"Column[name=date,columnNumber=2,type=VARCHAR,nullable=true,indexed=false,nativeType=null,columnSize=null]",
				col3.toString());
	}
}