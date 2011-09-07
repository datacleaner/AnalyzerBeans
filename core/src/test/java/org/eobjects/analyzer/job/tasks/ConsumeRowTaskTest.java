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
package org.eobjects.analyzer.job.tasks;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.test.MockAnalyzer;

public class ConsumeRowTaskTest extends TestCase {

	@SuppressWarnings("unchecked")
	public void testMultiRowTransformer() throws Throwable {
		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();

		AnalysisJob job;

		// build example job
		{
			AnalysisJobBuilder builder = new AnalysisJobBuilder(configuration);

			// number_col,string_col
			// 3,foo
			// 10,bar
			// 0,baz

			builder.setDatastore(new CsvDatastore("foo", "src/test/resources/multi_row_transformer_test.csv"));
			builder.addSourceColumns("number_col");

			TransformerJobBuilder<ConvertToNumberTransformer> convertTransformer = builder.addTransformer(
					ConvertToNumberTransformer.class).addInputColumn(builder.getSourceColumnByName("number_col"));
			MutableInputColumn<?> numberColumn = convertTransformer.getOutputColumns().get(0);

			TransformerJobBuilder<MockMultiRowTransformer> multiRowTransformer = builder.addTransformer(
					MockMultiRowTransformer.class).addInputColumn(numberColumn);

			builder.addRowProcessingAnalyzer(MockAnalyzer.class).addInputColumns(multiRowTransformer.getOutputColumns());

			job = builder.toAnalysisJob();
		}

		ListResult<InputRow> result;

		// run job
		{
			AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
			AnalysisResultFuture resultFuture = runner.run(job);
			if (resultFuture.isErrornous()) {
				throw resultFuture.getErrors().get(0);
			}
			result = (ListResult<InputRow>) resultFuture.getResults().get(0);
		}

		List<InputRow> list = result.getValues();

		// we expect 13 rows (3 + 10 + 0)
		assertEquals(13, list.size());
		List<InputColumn<?>> inputColumns = list.get(0).getInputColumns();
		assertEquals(4, inputColumns.size());
		InputColumn<?> countingColumn = inputColumns.get(2);
		assertEquals("Mock multi row transformer (1)", countingColumn.getName());
		assertEquals("Mock multi row transformer (2)", inputColumns.get(3).getName());

		assertEquals(1, list.get(0).getValue(countingColumn));
		assertEquals(2, list.get(1).getValue(countingColumn));
		assertEquals(3, list.get(2).getValue(countingColumn));
		assertEquals(1, list.get(3).getValue(countingColumn));
		assertEquals(2, list.get(4).getValue(countingColumn));
		assertEquals(3, list.get(5).getValue(countingColumn));
		assertEquals(4, list.get(6).getValue(countingColumn));
		assertEquals(5, list.get(7).getValue(countingColumn));
		assertEquals(6, list.get(8).getValue(countingColumn));
		assertEquals(7, list.get(9).getValue(countingColumn));
		assertEquals(8, list.get(10).getValue(countingColumn));
		assertEquals(9, list.get(11).getValue(countingColumn));
		assertEquals(10, list.get(12).getValue(countingColumn));
	}
}
