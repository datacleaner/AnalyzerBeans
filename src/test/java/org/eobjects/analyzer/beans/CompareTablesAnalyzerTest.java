package org.eobjects.analyzer.beans;

import java.util.List;

import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.result.TableDifference;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.schema.TableType;
import junit.framework.TestCase;

public class CompareTablesAnalyzerTest extends TestCase {

	public void testNoDiffs() throws Exception {
		Table table1 = new Table("table1", TableType.TABLE);

		CompareTablesAnalyzer analyzer = new CompareTablesAnalyzer(table1,
				table1);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isTablesEqual());

		Table table2 = new Table("table1", TableType.TABLE);

		analyzer = new CompareTablesAnalyzer(table1, table2);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isTablesEqual());
	}

	public void testSimpleDiffs() throws Exception {
		Table table1 = new Table("table1", TableType.TABLE);
		Table table2 = new Table("table2", TableType.TABLE);

		CompareTablesAnalyzer analyzer = new CompareTablesAnalyzer(table1,
				table2);
		analyzer.run(null);
		TableComparisonResult result = analyzer.getResult();
		assertFalse(result.isTablesEqual());

		assertTrue(result.getColumnComparisonResults().isEmpty());
		List<TableDifference<?>> diffs = result.getTableDifferences();
		assertEquals(1, diffs.size());
		assertEquals(
				"Tables 'table1' and 'table2' differ on 'name': [table1] vs. [table2]",
				diffs.get(0).toString());

		table2 = new Table("table1", TableType.VIEW);
		analyzer = new CompareTablesAnalyzer(table1, table2);
		analyzer.run(null);
		result = analyzer.getResult();
		assertFalse(result.isTablesEqual());

		assertTrue(result.getColumnComparisonResults().isEmpty());
		diffs = result.getTableDifferences();
		assertEquals(1, diffs.size());
		assertEquals(
				"Tables 'table1' and 'table1' differ on 'type': [TABLE] vs. [VIEW]",
				diffs.get(0).toString());
	}

	public void testColumnDiffs() throws Exception {
		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2");
		Table table1 = new Table("table", TableType.TABLE, null, col1, col2);
		Table table2 = new Table("table", TableType.TABLE, null, col1);

		CompareTablesAnalyzer analyzer = new CompareTablesAnalyzer(table1,
				table2);
		analyzer.run(null);
		TableComparisonResult result = analyzer.getResult();
		assertFalse(result.isTablesEqual());
		assertEquals(1, result.getTableDifferences().size());
		assertEquals(
				"Tables 'table' and 'table' differ on 'unmatched column': [col2] vs. [null]",
				result.getTableDifferences().get(0).toString());

		assertTrue(result.getColumnComparisonResults().isEmpty());

		Column col3 = new Column("col1", ColumnType.BIT);
		table2 = new Table("table", TableType.TABLE, null, col3, col2);

		analyzer = new CompareTablesAnalyzer(table1, table2);
		analyzer.run(null);
		result = analyzer.getResult();
		assertFalse(result.isTablesEqual());
		assertEquals(1, result.getColumnComparisonResults().size());
		assertEquals(1, result.getColumnComparisonResults().get(0)
				.getColumnDifferences().size());

		assertEquals(
				"Columns 'col1' and 'col1' differ on 'type': [VARCHAR] vs. [BIT]",
				result.getColumnComparisonResults().get(0)
						.getColumnDifferences().get(0).toString());

		assertTrue(result.getTableDifferences().isEmpty());
	}
}
