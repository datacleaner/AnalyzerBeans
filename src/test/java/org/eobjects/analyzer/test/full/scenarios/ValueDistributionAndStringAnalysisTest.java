package org.eobjects.analyzer.test.full.scenarios;

import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionResult;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.test.TestHelper;

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
		TaskRunner taskRunner = new MultiThreadedTaskRunner(3);

		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper
				.createReferenceDataCatalog();

		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(
				datastoreCatalog, referenceDataCatalog, descriptorProvider,
				taskRunner, collectionProvider);

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

		DataContext dc = DataContextFactory
				.createJdbcDataContext(getTestDbConnection());

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
				configuration);
		analysisJobBuilder
				.setDataContextProvider(new SingleDataContextProvider(dc));

		Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		assertNotNull(table);

		Column[] columns = table.getColumns();

		analysisJobBuilder.addSourceColumns(columns);

		for (InputColumn<?> inputColumn : analysisJobBuilder.getSourceColumns()) {
			RowProcessingAnalyzerJobBuilder<ValueDistributionAnalyzer> valueDistribuitionJobBuilder = analysisJobBuilder
					.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class);
			valueDistribuitionJobBuilder.addInputColumn(inputColumn);
			valueDistribuitionJobBuilder.setConfiguredProperty(
					"Record unique values", false);
			valueDistribuitionJobBuilder.setConfiguredProperty(
					"Top n most frequent values", null);
			valueDistribuitionJobBuilder.setConfiguredProperty(
					"Bottom n most frequent values", null);
		}

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> stringAnalyzerJob = analysisJobBuilder
				.addRowProcessingAnalyzer(StringAnalyzer.class);
		stringAnalyzerJob.addInputColumns(analysisJobBuilder
				.getAvailableInputColumns(DataTypeFamily.STRING));

		AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();

		AnalysisResultFuture resultFuture = runner.run(analysisJob);

		assertFalse(resultFuture.isDone());

		List<AnalyzerResult> results = resultFuture.getResults();

		assertTrue(resultFuture.isDone());

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
				assertEquals("[Column, Measure]",
						Arrays.toString(crosstab.getDimensionNames()));
				assertEquals(
						"[LASTNAME, FIRSTNAME, EXTENSION, EMAIL, OFFICECODE, JOBTITLE]",
						crosstab.getDimension(0).getCategories().toString());
				assertEquals(
						"[Char count, Max chars, Min chars, Avg chars, Max white spaces, Min white spaces, Avg white spaces, Uppercase chars, Lowercase chars, Non-letter chars, Word count, Max words, Min words]",
						crosstab.getDimension(1).getCategories().toString());
				CrosstabNavigator<?> nav = crosstab.navigate();
				nav.where("Column", "EMAIL");
				nav.where("Measure", "Char count");
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

		assertEquals("Patterson", lastnameResult.getTopValues()
				.getValueCounts().get(0).getValue());
		assertEquals(3, lastnameResult.getTopValues().getValueCounts().get(0)
				.getCount());
		assertEquals(16, lastnameResult.getUniqueCount());
		assertEquals(0, lastnameResult.getNullCount());

		assertEquals("Sales Rep", jobTitleResult.getTopValues()
				.getValueCounts().get(0).getValue());
	}
}
