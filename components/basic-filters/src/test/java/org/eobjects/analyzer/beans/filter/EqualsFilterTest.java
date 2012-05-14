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
package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;

import junit.framework.TestCase;

public class EqualsFilterTest extends TestCase {

	public void testSingleString() throws Exception {
		EqualsFilter f = new EqualsFilter(new String[] { "hello" }, new MockInputColumn<String>("col", String.class));
		assertEquals(ValidationCategory.VALID, f.filter("hello"));
		assertEquals(ValidationCategory.INVALID, f.filter("Hello"));
		assertEquals(ValidationCategory.INVALID, f.filter(""));
		assertEquals(ValidationCategory.INVALID, f.filter(null));
	}

	public void testSingleNumber() throws Exception {
		EqualsFilter f = new EqualsFilter(new String[] { "1234" }, new MockInputColumn<Number>("col", Number.class));
		assertEquals(ValidationCategory.VALID, f.filter(1234));
		assertEquals(ValidationCategory.VALID, f.filter(1234.0));
		assertEquals(ValidationCategory.INVALID, f.filter(2));
		assertEquals(ValidationCategory.INVALID, f.filter(-2));
	}

	public void testMultipleStrings() throws Exception {
		EqualsFilter f = new EqualsFilter(new String[] { "hello", "Hello", "World" }, new MockInputColumn<String>("col",
				String.class));
		assertEquals(ValidationCategory.VALID, f.filter("hello"));
		assertEquals(ValidationCategory.VALID, f.filter("Hello"));
		assertEquals(ValidationCategory.INVALID, f.filter(""));
		assertEquals(ValidationCategory.INVALID, f.filter("world"));
		assertEquals(ValidationCategory.INVALID, f.filter(null));
	}

	public void testMultipleNumbers() throws Exception {
		EqualsFilter f = new EqualsFilter(new String[] { "1234", "1235" }, new MockInputColumn<Number>("col", Number.class));
		assertEquals(ValidationCategory.VALID, f.filter(1234));
		assertEquals(ValidationCategory.VALID, f.filter(1234.0));
		assertEquals(ValidationCategory.VALID, f.filter(1235));
		assertEquals(ValidationCategory.VALID, f.filter(1235.0));
		assertEquals(ValidationCategory.INVALID, f.filter(2));
		assertEquals(ValidationCategory.INVALID, f.filter(-2));
	}

	public void testOptimizeQuery() throws Exception {
		JdbcDatastore ds = TestHelper.createSampleDatabaseDatastore("ds");
		DatastoreConnection con = ds.openConnection();
		Column column = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.FIRSTNAME");
		InputColumn<?> inputColumn = new MetaModelInputColumn(column);

		EqualsFilter filter = new EqualsFilter(new String[] { "foobar" }, inputColumn);
		assertTrue(filter.isOptimizable(ValidationCategory.VALID));
		assertTrue(filter.isOptimizable(ValidationCategory.INVALID));

		Query query = con.getDataContext().query().from(column.getTable()).select(column).toQuery();
		String originalSql = query.toSql();
		assertEquals("SELECT \"EMPLOYEES\".\"FIRSTNAME\" FROM PUBLIC.\"EMPLOYEES\"", originalSql);

		Query result;
		result = filter.optimizeQuery(query.clone(), ValidationCategory.VALID);
		assertEquals(originalSql + " WHERE (\"EMPLOYEES\".\"FIRSTNAME\" = 'foobar')", result.toSql());

		result = filter.optimizeQuery(query.clone(), ValidationCategory.INVALID);
		assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" <> 'foobar'", result.toSql());

		con.close();
	}
}
