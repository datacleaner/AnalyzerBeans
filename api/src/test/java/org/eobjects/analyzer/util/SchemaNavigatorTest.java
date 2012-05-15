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

import junit.framework.TestCase;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.schema.Column;

public class SchemaNavigatorTest extends TestCase {

	public void testSchemaWithDot() throws Exception {
		DataContext dc = DataContextFactory.createCsvDataContext(new File("src/test/resources/employees.csv"), ',', '\"');

		assertEquals(2, dc.getDefaultSchema().getTables()[0].getColumnCount());

		SchemaNavigator sn = new SchemaNavigator(dc);

		Column column = sn.convertToColumn("employees.csv.employees.email");
		assertEquals(
				"Column[name=email,columnNumber=1,type=VARCHAR,nullable=true,indexed=false,nativeType=null,columnSize=null]",
				column.toString());
	}
}
