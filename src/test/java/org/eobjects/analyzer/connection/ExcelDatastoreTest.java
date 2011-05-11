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
package org.eobjects.analyzer.connection;

import org.eobjects.metamodel.schema.Column;
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

	public void testToString() throws Exception {
		Datastore datastore = new ExcelDatastore("foobar", "src/test/resources/Spreadsheet2007.xlsx");
		assertEquals("ExcelDatastore[name=foobar]", datastore.toString());
	}
}