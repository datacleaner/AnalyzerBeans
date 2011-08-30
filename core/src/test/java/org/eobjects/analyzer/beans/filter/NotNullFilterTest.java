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

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.metamodel.query.Query;

public class NotNullFilterTest extends TestCase {

	public void testCategorize() throws Exception {
		InputColumn<Integer> col1 = new MockInputColumn<Integer>("col1", Integer.class);
		InputColumn<Boolean> col2 = new MockInputColumn<Boolean>("col2", Boolean.class);
		InputColumn<String> col3 = new MockInputColumn<String>("col3", String.class);
		InputColumn<?>[] columns = new InputColumn[] { col1, col2, col3 };

		NotNullFilter filter = new NotNullFilter(columns, true);
		assertEquals(ValidationCategory.VALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "foo")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, null).put(col3, "foo")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "")));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, null)));

		assertEquals(ValidationCategory.INVALID,
				filter.categorize(new MockInputRow().put(col1, null).put(col2, null).put(col3, null)));
	}

	public void testDescriptor() throws Exception {
		FilterBeanDescriptor<NotNullFilter, ValidationCategory> desc = Descriptors.ofFilter(NotNullFilter.class);
		Class<ValidationCategory> categoryEnum = desc.getOutcomeCategoryEnum();
		assertEquals(ValidationCategory.class, categoryEnum);
	}

	public void testOptimizeQuery() throws Exception {
		JdbcDatastore datastore = TestHelper.createSampleDatabaseDatastore("mydb");
		DataContextProvider dcp = datastore.getDataContextProvider();
		SchemaNavigator nav = dcp.getSchemaNavigator();

		MetaModelInputColumn col1 = new MetaModelInputColumn(nav.convertToColumn("EMPLOYEES.EMAIL"));
		MetaModelInputColumn col2 = new MetaModelInputColumn(nav.convertToColumn("EMPLOYEES.EMPLOYEENUMBER"));
		InputColumn<?>[] columns = new InputColumn[] { col1, col2 };

		NotNullFilter filter = new NotNullFilter(columns, true);

		Query baseQuery = dcp.getDataContext().query().from("EMPLOYEES").select("EMAIL").and("EMPLOYEENUMBER").toQuery();
		Query optimizedQuery = filter.optimizeQuery(baseQuery.clone(), ValidationCategory.VALID);

		assertEquals("SELECT \"EMPLOYEES\".\"EMAIL\", \"EMPLOYEES\".\"EMPLOYEENUMBER\" FROM "
				+ "PUBLIC.\"EMPLOYEES\" WHERE \"EMPLOYEES\".\"EMAIL\" IS NOT NULL AND \"EMPLOYEES\".\"EMAIL\" <> '' AND "
				+ "\"EMPLOYEES\".\"EMPLOYEENUMBER\" IS NOT NULL", optimizedQuery.toSql());

		optimizedQuery = filter.optimizeQuery(baseQuery.clone(), ValidationCategory.INVALID);

		assertEquals("SELECT \"EMPLOYEES\".\"EMAIL\", \"EMPLOYEES\".\"EMPLOYEENUMBER\" FROM "
				+ "PUBLIC.\"EMPLOYEES\" WHERE (\"EMPLOYEES\".\"EMAIL\" IS NULL OR \"EMPLOYEES\".\"EMAIL\" = '' OR "
				+ "\"EMPLOYEES\".\"EMPLOYEENUMBER\" IS NULL)", optimizedQuery.toSql());

		dcp.close();
	}
}
