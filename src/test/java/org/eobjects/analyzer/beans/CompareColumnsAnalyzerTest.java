package org.eobjects.analyzer.beans;

import java.util.List;

import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.ColumnDifference;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import junit.framework.TestCase;

public class CompareColumnsAnalyzerTest extends TestCase {

	public void testNoDiffs() throws Exception {
		Column column1 = new Column("column", ColumnType.VARCHAR, null, 4, true);

		CompareColumnsAnalyzer analyzer = new CompareColumnsAnalyzer(column1,
				column1);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isColumnsEqual());

		Column column2 = new Column("column", ColumnType.VARCHAR, null, 4, true);

		analyzer = new CompareColumnsAnalyzer(column1, column2);
		analyzer.run(null);
		assertTrue(analyzer.getResult().isColumnsEqual());

		assertEquals(CompareColumnsAnalyzer.class, analyzer.getResult()
				.getProducerClass());
	}

	public void testSimpleDiffs() throws Exception {
		Column column1 = new Column("column1", ColumnType.INTEGER, null, 3,
				false);
		Column column2 = new Column("column2", ColumnType.VARCHAR, null, 4,
				false);

		CompareColumnsAnalyzer analyzer = new CompareColumnsAnalyzer(column1,
				column2);
		analyzer.run(null);
		ColumnComparisonResult result = analyzer.getResult();
		assertFalse(result.isColumnsEqual());

		List<ColumnDifference<?>> diffs = result.getColumnDifferences();
		assertEquals(3, diffs.size());
		assertEquals(
				"Columns 'column1' and 'column2' differ on 'name': [column1] vs. [column2]",
				diffs.get(0).toString());
		assertEquals(
				"Columns 'column1' and 'column2' differ on 'type': [INTEGER] vs. [VARCHAR]",
				diffs.get(1).toString());
		assertEquals(
				"Columns 'column1' and 'column2' differ on 'column number': [3] vs. [4]",
				diffs.get(2).toString());

		// new column 2
		column2 = new Column("column1", ColumnType.INTEGER, null, 3, true);
		analyzer = new CompareColumnsAnalyzer(column1, column2);
		analyzer.run(null);
		result = analyzer.getResult();
		assertFalse(result.isColumnsEqual());

		diffs = result.getColumnDifferences();
		assertEquals(1, diffs.size());

		assertEquals(
				"Columns 'column1' and 'column1' differ on 'nullable': [false] vs. [true]",
				diffs.get(0).toString());
	}
}
