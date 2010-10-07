package org.eobjects.analyzer.test.full.scenarios;

import java.util.List;

import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class TokenizerAndValueDistributionTest extends MetaModelTestCase {

	public void testScenario() throws Exception {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
				"org.eobjects.analyzer.beans", true);
		CollectionProvider collectionProvider = new BerkeleyDbCollectionProvider();
		TaskRunner taskRunner = new MultiThreadedTaskRunner(3);

		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper.createReferenceDataCatalog();

		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner, collectionProvider);

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

		DataContext dc = DataContextFactory.createJdbcDataContext(getTestDbConnection());

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDataContextProvider(new SingleDataContextProvider(dc, new JdbcDatastore("foobar", dc)));

		Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		assertNotNull(table);

		Column jobTitleColumn = table.getColumnByName("JOBTITLE");
		assertNotNull(jobTitleColumn);

		analysisJobBuilder.addSourceColumns(jobTitleColumn);

		TransformerJobBuilder<TokenizerTransformer> transformerJobBuilder = analysisJobBuilder
				.addTransformer(TokenizerTransformer.class);
		transformerJobBuilder.addInputColumn(analysisJobBuilder.getSourceColumns().get(0));
		transformerJobBuilder.setConfiguredProperty("Number of tokens", 4);
		List<MutableInputColumn<?>> transformerOutput = transformerJobBuilder.getOutputColumns();
		assertEquals(4, transformerOutput.size());

		transformerOutput.get(0).setName("first word");
		transformerOutput.get(1).setName("second word");
		transformerOutput.get(2).setName("third words");
		transformerOutput.get(3).setName("fourth words");

		for (InputColumn<?> inputColumn : transformerOutput) {
			RowProcessingAnalyzerJobBuilder<ValueDistributionAnalyzer> valueDistribuitionJobBuilder = analysisJobBuilder
					.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class);
			valueDistribuitionJobBuilder.addInputColumn(inputColumn);
			valueDistribuitionJobBuilder.setConfiguredProperty("Record unique values", true);
			valueDistribuitionJobBuilder.setConfiguredProperty("Top n most frequent values", null);
			valueDistribuitionJobBuilder.setConfiguredProperty("Bottom n most frequent values", null);
		}

		AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();

		AnalysisResultFuture resultFuture = runner.run(analysisJob);

		assertFalse(resultFuture.isDone());

		List<AnalyzerResult> results = resultFuture.getResults();

		assertTrue(resultFuture.isDone());

		// expect 1 result for each token
		assertEquals(4, results.size());

		for (AnalyzerResult analyzerResult : results) {
			ValueDistributionResult result = (ValueDistributionResult) analyzerResult;
			if ("first word".equals(result.getColumnName())) {
				assertEquals("ValueCountList[[[Sales->19], [VP->2]]]", result.getTopValues().toString());
				assertNull(result.getBottomValues());
				assertEquals(0, result.getNullCount());
				assertEquals(2, result.getUniqueCount());
			} else if ("second word".equals(result.getColumnName())) {
				assertEquals("ValueCountList[[[Rep->17], [Manager->3]]]", result.getTopValues().toString());
				assertNull(result.getBottomValues());
				assertEquals(1, result.getNullCount());
				assertEquals(2, result.getUniqueCount());
			} else if ("third words".equals(result.getColumnName())) {
				assertEquals("ValueCountList[[]]", result.getTopValues().toString());
				assertNull(result.getBottomValues());
				assertEquals(20, result.getNullCount());

				assertEquals(3, result.getUniqueCount());
				assertEquals("[(EMEA), (JAPAN,, (NA)]", result.getUniqueValues().toString());
			} else if ("fourth words".equals(result.getColumnName())) {
				assertEquals("ValueCountList[[]]", result.getTopValues().toString());
				assertNull(result.getBottomValues());
				assertEquals(22, result.getNullCount());

				assertEquals(1, result.getUniqueCount());
				assertEquals("[APAC)]", result.getUniqueValues().toString());
			} else {
				fail("Unexpected columnName: " + result.getColumnName());
			}
		}
	}
}
