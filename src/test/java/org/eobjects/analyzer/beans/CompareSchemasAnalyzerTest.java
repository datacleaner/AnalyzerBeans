package org.eobjects.analyzer.beans;

import java.util.List;

import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.ColumnDifference;
import org.eobjects.analyzer.result.SchemaDifference;
import org.eobjects.analyzer.result.TableComparisonResult;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.schema.TableType;
import junit.framework.TestCase;

public class CompareSchemasAnalyzerTest extends TestCase {

	public void testNoDiffs() throws Exception {
		Schema schema1 = new Schema("schema");

		CompareSchemasAnalyzer analyzer = new CompareSchemasAnalyzer(schema1,
				schema1);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isSchemasEqual());

		Schema schema2 = new Schema("schema");

		analyzer = new CompareSchemasAnalyzer(schema1, schema2);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isSchemasEqual());
	}

	public void testNameDiff() throws Exception {
		Schema schema1 = new Schema("schema1");
		Schema schema2 = new Schema("schema2");

		CompareSchemasAnalyzer analyzer = new CompareSchemasAnalyzer(schema1,
				schema2);
		analyzer.run(null);
		assertFalse(analyzer.getResult().isSchemasEqual());

		List<SchemaDifference<?>> diffs = analyzer.getResult()
				.getSchemaDifferences();
		assertEquals(1, diffs.size());
		assertEquals(
				"Schemas 'schema1' and 'schema2' differ on 'name': [schema1] vs. [schema2]",
				diffs.get(0).toString());
	}

	public void testTableDiffs() throws Exception {
		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2");
		Column col3 = new Column("col1", ColumnType.BIGINT);
		Table table1 = new Table("table", TableType.TABLE, null, col1, col2);
		Table table2 = new Table("table", TableType.VIEW, null, col3, col2);
		Schema schema1 = new Schema("schema", table1);
		Schema schema2 = new Schema("schema", table2);

		CompareSchemasAnalyzer analyzer = new CompareSchemasAnalyzer(schema1,
				schema2);
		analyzer.run(null);
		assertFalse(analyzer.getResult().isSchemasEqual());

		assertEquals(0, analyzer.getResult().getSchemaDifferences().size());
		List<TableComparisonResult> tableComparisonResults = analyzer
				.getResult().getTableComparisonResults();
		assertEquals(1, tableComparisonResults.size());
		TableComparisonResult tableComparisonResult = tableComparisonResults
				.get(0);

		assertEquals(1, tableComparisonResult.getTableDifferences().size());
		assertEquals(
				"Tables 'table' and 'table' differ on 'type': [TABLE] vs. [VIEW]",
				tableComparisonResult.getTableDifferences().get(0).toString());
		assertEquals(1, tableComparisonResult.getColumnComparisonResults()
				.size());
		ColumnComparisonResult columnComparisonResult = tableComparisonResult
				.getColumnComparisonResults().get(0);
		List<ColumnDifference<?>> columnDifferences = columnComparisonResult
				.getColumnDifferences();
		assertEquals(1, columnDifferences.size());
		assertEquals(
				"Columns 'col1' and 'col1' differ on 'type': [VARCHAR] vs. [BIGINT]",
				columnDifferences.get(0).toString());
	}
}
