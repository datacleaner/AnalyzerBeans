package org.eobjects.analyzer.test.full.scenarios;

import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionResult;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisRunner;
import org.eobjects.analyzer.job.AnalysisRunnerImpl;
import org.eobjects.analyzer.job.ConcurrencyProvider;
import org.eobjects.analyzer.job.ThreadPoolConcurrencyProvider;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ValueDistributionAndStringAnalysisTest extends MetaModelTestCase {

	public void testScenario() throws Exception {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.beans", true);
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();

		ConcurrencyProvider concurrencyProvider = new ThreadPoolConcurrencyProvider(3);

		AnalysisRunner runner = new AnalysisRunnerImpl(descriptorProvider,
				concurrencyProvider, collectionProvider);

		DataContext dc = DataContextFactory
				.createJdbcDataContext(getTestDbConnection());

		Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		assertNotNull(table);

		Column[] columns = table.getColumns();

		for (Column column : columns) {
			AnalysisJob vdJob = new AnalysisJob(ValueDistributionAnalyzer.class);
			vdJob.putColumnProperty("Column", column.getQualifiedLabel());
			vdJob.putBooleanProperty("Record unique values", false);
			vdJob.putIntegerProperty("Top n most frequent values",
					(Integer) null);
			vdJob.putIntegerProperty("Bottom n most frequent values",
					(Integer) null);
			runner.addJob(vdJob);
		}

		columns = table.getLiteralColumns();

		AnalysisJob saJob = new AnalysisJob(StringAnalyzer.class);
		saJob.putColumnProperty("Columns", columns);
		runner.addJob(saJob);

		runner.run(dc);

		// TODO: any assertions on the future?
		// assertFalse(runner.isDone());

		// TODO: It seems that the value dist profiler is being categorized as
		// an exploring profiler!

		List<AnalyzerResult> results = runner.getResults();

		assertTrue(runner.isDone());

		// expect 1 result for each column (the value distributions) and 1
		// result for the string analyzer
		assertEquals(table.getColumnCount() + 1, results.size());

		int stringAnalyzerResults = 0;
		int valueDistributionResults = 0;

		for (AnalyzerResult result : results) {
			if (StringAnalyzer.class.getName().equals(
					result.getProducerClass().getName())) {
				stringAnalyzerResults++;

				assertTrue(result instanceof CrosstabResult);
				CrosstabResult cr = (CrosstabResult) result;
				Crosstab<?> crosstab = cr.getCrosstab();
				assertEquals("[column, measure]", Arrays.toString(crosstab
						.getDimensionNames()));
				assertEquals(
						"[LASTNAME, FIRSTNAME, EXTENSION, EMAIL, OFFICECODE, JOBTITLE]",
						crosstab.getDimension(0).getCategories().toString());
				assertEquals(
						"[Char count, Max chars, Min chars, Avg chars, Max white spaces, Min white spaces, Avg white spaces, Uppercase chars, Lowercase chars, Non-letter chars, Word count, Max words, Min words]",
						crosstab.getDimension(1).getCategories().toString());
				CrosstabNavigator<?> nav = crosstab.navigate();
				nav.where("column", "EMAIL");
				nav.where("measure", "Char count");
				assertEquals("655", nav.get().toString());
			} else {
				assertEquals(ValueDistributionAnalyzer.class.getName(), result
						.getProducerClass().getName());
				assertTrue(result instanceof ValueDistributionResult);

				valueDistributionResults++;
			}
		}

		assertEquals(1, stringAnalyzerResults);
		assertEquals(8, valueDistributionResults);

		ValueDistributionResult jobTitleResult = null;
		ValueDistributionResult lastnameResult = null;
		for (AnalyzerResult result : results) {
			if (result.getProducerClass() == ValueDistributionAnalyzer.class) {
				ValueDistributionResult vdResult = (ValueDistributionResult) result;
				if ("JOBTITLE".equals(vdResult.getColumnName())) {
					jobTitleResult = vdResult;
				} else if ("LASTNAME".equals(vdResult.getColumnName())) {
					lastnameResult = vdResult;
				}
			}
		}
		
		assertNotNull(jobTitleResult);
		assertNotNull(lastnameResult);
		
		assertEquals("Patterson", lastnameResult.getTopValues().getValueCounts().get(0).getValue());
		assertEquals(3, lastnameResult.getTopValues().getValueCounts().get(0).getCount());
		assertEquals(16, lastnameResult.getUniqueCount());
		assertEquals(0, lastnameResult.getNullCount());
		
		assertEquals("Sales Rep", jobTitleResult.getTopValues().getValueCounts().get(0).getValue());
	}
}
