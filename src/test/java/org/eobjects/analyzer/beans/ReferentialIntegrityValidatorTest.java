package org.eobjects.analyzer.beans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.test.QueryMatcher;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.IDataContextStrategy;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FromItem;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

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
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				ReferentialIntegrityValidator.class);
		assertEquals(
				"AnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.ReferentialIntegrityValidator]",
				descriptor.toString());

		assertEquals(
				"[ConfiguredDescriptor[method=null,field=dk.eobjects.metamodel.schema.Column org.eobjects.analyzer.beans.ReferentialIntegrityValidator.primaryKeyColumn], ConfiguredDescriptor[method=null,field=dk.eobjects.metamodel.schema.Column org.eobjects.analyzer.beans.ReferentialIntegrityValidator.foreignKeyColumn], ConfiguredDescriptor[method=null,field=boolean org.eobjects.analyzer.beans.ReferentialIntegrityValidator.acceptNullForeignKey]]",
				descriptor.getConfiguredDescriptors().toString());

		assertEquals("[]", descriptor.getProvidedDescriptors().toString());

		assertEquals("[]", descriptor.getInitializeDescriptors().toString());

		assertTrue(descriptor.isExploringAnalyzer());
		assertFalse(descriptor.isRowProcessingAnalyzer());

		assertEquals("[]", descriptor.getCloseDescriptors().toString());
	}

	public void testSeparateTables() throws Exception {
		Column columnInOfficesTable = officesTable
				.getColumnByName("OFFICECODE");
		Column columnInEmployeesTable = employeesTable
				.getColumnByName("OFFICECODE");

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
		Column foreignKeyColumn = schema.getTableByName(TABLE_ROLE)
				.getColumnByName(COLUMN_ROLE_CONTRIBUTOR_ID);
		Column primaryKeyColumn = schema.getTableByName(TABLE_CONTRIBUTOR)
				.getColumnByName(COLUMN_CONTRIBUTOR_CONTRIBUTOR_ID);

		ReferentialIntegrityValidator bean = new ReferentialIntegrityValidator();
		bean.setAcceptNullForeignKey(false);
		bean.setPrimaryKeyColumn(primaryKeyColumn);
		bean.setForeignKeyColumn(foreignKeyColumn);

		IDataContextStrategy dcMock = createMock(IDataContextStrategy.class);
		DataContext dc = new DataContext(dcMock);

		List<Row> rows = new ArrayList<Row>();

		// Create SelectItems similar to those created in the bean (to make
		// query-mocking work)
		FromItem leftSide = new FromItem(new Query().select(foreignKeyColumn)
				.from(foreignKeyColumn.getTable())).setAlias("a");
		SelectItem leftOn = leftSide.getSubQuery().getSelectClause().getItem(0);
		FromItem rightSide = new FromItem(new Query().select(primaryKeyColumn)
				.from(primaryKeyColumn.getTable())).setAlias("b");
		SelectItem rightOn = rightSide.getSubQuery().getSelectClause().getItem(
				0);
		SelectItem primaryKeySelectItem = new SelectItem(rightOn, rightSide);
		SelectItem foreignKeySelectItem = new SelectItem(leftOn, leftSide);

		// An valid row
		rows.add(new Row(new SelectItem[] { foreignKeySelectItem,
				primaryKeySelectItem }, new Object[] { "foo", "foo" }));
		// An invalid row
		rows.add(new Row(new SelectItem[] { foreignKeySelectItem,
				primaryKeySelectItem }, new Object[] { "bar", null }));
		// Simulate an (impossible?) error in the join
		rows.add(new Row(new SelectItem[] { foreignKeySelectItem,
				primaryKeySelectItem }, new Object[] { "foobar", "foo" }));
		// Another valid row
		rows.add(new Row(new SelectItem[] { foreignKeySelectItem,
				primaryKeySelectItem }, new Object[] { "foo", "foo" }));

		EasyMock
				.reportMatcher(new QueryMatcher(
						"SELECT b.contributor_id, a.contributor_id, a.project_id, a.name "
								+ "FROM (SELECT role.contributor_id, role.project_id, role.name FROM MetaModelSchema.role) a "
								+ "LEFT JOIN (SELECT contributor.contributor_id FROM MetaModelSchema.contributor) b "
								+ "ON a.contributor_id = b.contributor_id"));
		EasyMock.expect(dcMock.executeQuery(null)).andReturn(new DataSet(rows));

		replayMocks();

		bean.run(dc);

		verifyMocks();

		DataSetResult invalidRows = bean.getResult();
		assertEquals(2, invalidRows.getRows().size());

		assertEquals("Row[values={bar,<null>}]", invalidRows.getRows().get(0)
				.toString());
		assertEquals("Row[values={foobar,foo}]", invalidRows.getRows().get(1)
				.toString());
	}

	public void testParentChildRelationship() throws Exception {
		ReferentialIntegrityValidator bean = new ReferentialIntegrityValidator();
		bean.setAcceptNullForeignKey(false);
		bean.setPrimaryKeyColumn(employeesTable
				.getColumnByName("EMPLOYEENUMBER"));
		bean.setForeignKeyColumn(employeesTable.getColumnByName("REPORTSTO"));

		bean.run(dc);

		DataSetResult invalidRows = bean.getResult();
		assertEquals(1, invalidRows.getRows().size());
		assertEquals(
				"Row[values={<null>,<null>,1002,Murphy,Diane,x5800,dmurphy@classicmodelcars.com,1,President}]",
				invalidRows.getRows().get(0).toString());

		bean.setAcceptNullForeignKey(true);

		bean.run(dc);

		invalidRows = bean.getResult();
		assertEquals(0, invalidRows.getRows().size());
	}
}
