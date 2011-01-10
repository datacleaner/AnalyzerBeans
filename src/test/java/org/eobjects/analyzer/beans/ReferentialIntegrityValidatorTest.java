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
package org.eobjects.analyzer.beans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.test.QueryMatcher;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.MetaModelTestCase;
import org.eobjects.metamodel.data.DefaultRow;
import org.eobjects.metamodel.data.InMemoryDataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.FromItem;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

public class ReferentialIntegrityValidatorTest extends MetaModelTestCase {

	private Connection con;
	private DataContext dc;
	private Table employeesTable;
	private Table officesTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		con = getTestDbConnection();
		dc = DataContextFactory.createJdbcDataContext(con);
		employeesTable = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		officesTable = dc.getDefaultSchema().getTableByName("OFFICES");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		con.close();
	}

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor<ReferentialIntegrityValidator> descriptor = AnnotationBasedAnalyzerBeanDescriptor
				.create(ReferentialIntegrityValidator.class);
		assertEquals(
				"AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.ReferentialIntegrityValidator]",
				descriptor.toString());

		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
		Iterator<ConfiguredPropertyDescriptor> it = configuredProperties.iterator();
		assertTrue(it.hasNext());
		assertEquals("Primary key column", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Foreign key column", it.next().getName());
		assertTrue(it.hasNext());
		assertEquals("Accept NULL foreign keys?", it.next().getName());
		assertFalse(it.hasNext());

		assertEquals("[]", descriptor.getProvidedProperties().toString());

		assertEquals("[]", descriptor.getInitializeMethods().toString());

		assertTrue(descriptor.isExploringAnalyzer());
		assertFalse(descriptor.isRowProcessingAnalyzer());

		assertEquals("[]", descriptor.getCloseMethods().toString());
	}

	public void testSeparateTables() throws Exception {
		Column columnInOfficesTable = officesTable.getColumnByName("OFFICECODE");
		Column columnInEmployeesTable = employeesTable.getColumnByName("OFFICECODE");

		ReferentialIntegrityValidator bean = new ReferentialIntegrityValidator();
		bean.setAcceptNullForeignKey(false);
		bean.setPrimaryKeyColumn(columnInOfficesTable);
		bean.setForeignKeyColumn(columnInEmployeesTable);

		bean.run(dc);

		DataSetResult invalidRows = bean.getResult();
		assertEquals(0, invalidRows.getRows().size());
	}

	public void testReferentialInconsistency() throws Exception {
		Schema schema = getExampleSchema();
		Column foreignKeyColumn = schema.getTableByName(TABLE_ROLE).getColumnByName(COLUMN_ROLE_CONTRIBUTOR_ID);
		Column primaryKeyColumn = schema.getTableByName(TABLE_CONTRIBUTOR)
				.getColumnByName(COLUMN_CONTRIBUTOR_CONTRIBUTOR_ID);

		ReferentialIntegrityValidator bean = new ReferentialIntegrityValidator();
		bean.setAcceptNullForeignKey(false);
		bean.setPrimaryKeyColumn(primaryKeyColumn);
		bean.setForeignKeyColumn(foreignKeyColumn);

		DataContext dcMock = createMock(DataContext.class);

		List<Row> rows = new ArrayList<Row>();

		// Create SelectItems similar to those created in the bean (to make
		// query-mocking work)
		FromItem leftSide = new FromItem(new Query().select(foreignKeyColumn).from(foreignKeyColumn.getTable()))
				.setAlias("a");
		SelectItem leftOn = leftSide.getSubQuery().getSelectClause().getItem(0);
		FromItem rightSide = new FromItem(new Query().select(primaryKeyColumn).from(primaryKeyColumn.getTable()))
				.setAlias("b");
		SelectItem rightOn = rightSide.getSubQuery().getSelectClause().getItem(0);
		SelectItem primaryKeySelectItem = new SelectItem(rightOn, rightSide);
		SelectItem foreignKeySelectItem = new SelectItem(leftOn, leftSide);

		// An valid row
		rows.add(new DefaultRow(new SelectItem[] { foreignKeySelectItem, primaryKeySelectItem },
				new Object[] { "foo", "foo" }));
		// An invalid row
		rows.add(new DefaultRow(new SelectItem[] { foreignKeySelectItem, primaryKeySelectItem },
				new Object[] { "bar", null }));
		// Simulate an (impossible?) error in the join
		rows.add(new DefaultRow(new SelectItem[] { foreignKeySelectItem, primaryKeySelectItem }, new Object[] { "foobar",
				"foo" }));
		// Another valid row
		rows.add(new DefaultRow(new SelectItem[] { foreignKeySelectItem, primaryKeySelectItem },
				new Object[] { "foo", "foo" }));

		EasyMock.reportMatcher(new QueryMatcher("SELECT b.contributor_id, a.contributor_id, a.project_id, a.name "
				+ "FROM (SELECT role.contributor_id, role.project_id, role.name FROM MetaModelSchema.role) a "
				+ "LEFT JOIN (SELECT contributor.contributor_id FROM MetaModelSchema.contributor) b "
				+ "ON a.contributor_id = b.contributor_id"));
		EasyMock.expect(dcMock.executeQuery(null)).andReturn(new InMemoryDataSet(rows));

		replayMocks();

		bean.run(dcMock);

		verifyMocks();

		DataSetResult invalidRows = bean.getResult();
		assertEquals(2, invalidRows.getRows().size());

		assertEquals("Row[values=[bar, null]]", invalidRows.getRows().get(0).toString());
		assertEquals("Row[values=[foobar, foo]]", invalidRows.getRows().get(1).toString());
	}

	public void testParentChildRelationship() throws Exception {
		ReferentialIntegrityValidator bean = new ReferentialIntegrityValidator();
		bean.setAcceptNullForeignKey(false);
		bean.setPrimaryKeyColumn(employeesTable.getColumnByName("EMPLOYEENUMBER"));
		bean.setForeignKeyColumn(employeesTable.getColumnByName("REPORTSTO"));

		bean.run(dc);

		DataSetResult invalidRows = bean.getResult();
		assertEquals(1, invalidRows.getRows().size());
		assertEquals("Row[values=[null, null, 1002, Murphy, Diane, x5800, dmurphy@classicmodelcars.com, 1, President]]",
				invalidRows.getRows().get(0).toString());

		bean.setAcceptNullForeignKey(true);

		bean.run(dc);

		invalidRows = bean.getResult();
		assertEquals(0, invalidRows.getRows().size());
	}
}
