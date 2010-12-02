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

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.DateAndTimeAnalyzerResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.TestHelper;

public class DateAndTimeAnalyzerTest extends TestCase {

	public void testOrderFactTable() throws Throwable {
		AnalyzerBeansConfiguration conf = TestHelper.createAnalyzerBeansConfiguration(TestHelper
				.createSampleDatabaseDatastore("orderdb"));
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);

		ajb.setDatastore("orderdb");

		ajb.addSourceColumns("ORDERFACT.ORDERDATE", "ORDERFACT.REQUIREDDATE", "ORDERFACT.SHIPPEDDATE");

		RowProcessingAnalyzerJobBuilder<DateAndTimeAnalyzer> analyzer = ajb
				.addRowProcessingAnalyzer(DateAndTimeAnalyzer.class);
		analyzer.addInputColumns(ajb.getSourceColumns());

		AnalysisJob job = ajb.toAnalysisJob();

		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(job);
		resultFuture.await();

		if (!resultFuture.isSuccessful()) {
			throw resultFuture.getErrors().get(0);
		}
		assertTrue(resultFuture.isSuccessful());

		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(1, results.size());

		DateAndTimeAnalyzerResult result = (DateAndTimeAnalyzerResult) results.get(0);

		String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");
		assertEquals(7, resultLines.length);

		assertEquals("             ORDERDATE    REQUIREDDATE SHIPPEDDATE  ", resultLines[0]);
		assertEquals("Row count            2996         2996         2996 ", resultLines[1]);
		assertEquals("Null count              0            0          141 ", resultLines[2]);
		assertEquals("Highest date 2005-05-31   2005-06-11   2005-05-20   ", resultLines[3]);
		assertEquals("Lowest date  2003-01-06   2003-01-13   2003-01-10   ", resultLines[4]);
		assertEquals("Highest time 00:00:00.000 00:00:00.000 00:00:00.000 ", resultLines[5]);
		assertEquals("Lowest time  00:00:00.000 00:00:00.000 00:00:00.000 ", resultLines[6]);

		CrosstabNavigator<?> nav = result.getCrosstab().where("Column", "ORDERDATE");
		InputColumn<?> column = ajb.getSourceColumnByName("ORDERDATE");

		ResultProducer resultProducer = nav.where("Measure", "Highest date").explore();
		testAnnotatedRowResult(resultProducer.getResult(), column, 19, 2);

		resultProducer = nav.where("Measure", "Lowest date").explore();
		testAnnotatedRowResult(resultProducer.getResult(), column, 4, 1);

		resultProducer = nav.where("Measure", "Highest time").explore();
		testAnnotatedRowResult(resultProducer.getResult(), column, 2996, 323);

		resultProducer = nav.where("Measure", "Lowest time").explore();
		testAnnotatedRowResult(resultProducer.getResult(), column, 2996, 323);
	}

	private void testAnnotatedRowResult(AnalyzerResult result, InputColumn<?> col, int rowCount, int distinctRowCount) {
		assertTrue("Unexpected result type: " + result.getClass(), result instanceof AnnotatedRowsResult);
		AnnotatedRowsResult res = (AnnotatedRowsResult) result;
		InputColumn<?>[] highlightedColumns = res.getHighlightedColumns();
		assertEquals(1, highlightedColumns.length);
		assertEquals(col, highlightedColumns[0]);

		assertEquals(rowCount, res.getRowCount());
		assertEquals(distinctRowCount, res.getRows().length);
	}
}
