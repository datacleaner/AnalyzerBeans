package org.eobjects.analyzer.test.full.scenarios;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.NameStandardizerTransformer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionResult;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.job.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.InMemoryCollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class NameStringAnalysisAndValueDistribution extends TestCase {

	public void testScenario() throws Exception {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider()
				.scanPackage("org.eobjects.analyzer.beans", true);
		CollectionProvider collectionProvider = new InMemoryCollectionProvider();
		TaskRunner taskRunner = new SingleThreadedTaskRunner(false);
		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper
				.createReferenceDataCatalog();
		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(
				datastoreCatalog, referenceDataCatalog, descriptorProvider,
				taskRunner, collectionProvider);

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

		DataContext dc = DataContextFactory
				.createCsvDataContext(new File(
						"src/test/resources/NameStringAnalysisAndValueDistributionTest-data.csv"));

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
				configuration);
		analysisJobBuilder
				.setDataContextProvider(new SingleDataContextProvider(dc));

		Schema schema = dc.getDefaultSchema();
		Table table = schema.getTables()[0];
		assertNotNull(table);

		Column column = table.getColumnByName("name");

		analysisJobBuilder.addSourceColumn(column);

		TransformerJobBuilder transformerJobBuilder = analysisJobBuilder
				.addTransformer(NameStandardizerTransformer.class);
		transformerJobBuilder.addInputColumn(analysisJobBuilder
				.getSourceColumns().get(0));
		transformerJobBuilder.setConfiguredProperty("Name patterns",
				NameStandardizerTransformer.DEFAULT_PATTERNS);

		assertTrue(transformerJobBuilder.isConfigured());

		List<MutableInputColumn<?>> transformedColumns = transformerJobBuilder
				.getOutputColumns();
		assertEquals(3, transformedColumns.size());
		assertEquals("Firstname", transformedColumns.get(0).getName());
		assertEquals("Lastname", transformedColumns.get(1).getName());
		assertEquals("Middlename", transformedColumns.get(2).getName());

		assertTrue(analysisJobBuilder.addAnalyzer(StringAnalyzer.class)
				.addInputColumns(transformedColumns).isConfigured());

		for (InputColumn<?> inputColumn : transformedColumns) {
			RowProcessingAnalyzerJobBuilder analyzerJobBuilder = analysisJobBuilder
					.addAnalyzer(ValueDistributionAnalyzer.class);
			analyzerJobBuilder.addInputColumn(inputColumn);
			analyzerJobBuilder.setConfiguredProperty("Record unique values",
					false);
			analyzerJobBuilder.setConfiguredProperty(
					"Top n most frequent values", 1000);
			analyzerJobBuilder.setConfiguredProperty(
					"Bottom n most frequent values", 1000);
			assertTrue(analyzerJobBuilder.isConfigured());
		}

		AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder
				.toAnalysisJob());
		List<AnalyzerResult> results = resultFuture.getResults();

		assertEquals(4, results.size());

		AnalyzerResult result = results.get(0);
		assertEquals("StringAnalyzer", result.getProducerClass()
				.getSimpleName());

		ValueDistributionResult vdResult = (ValueDistributionResult) results
				.get(1);
		assertEquals("Firstname", vdResult.getColumnName());
		assertEquals(0, vdResult.getNullCount());
		assertEquals(2, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[barack->4]]]", vdResult.getTopValues()
				.toString());

		vdResult = (ValueDistributionResult) results.get(2);
		assertEquals("Lastname", vdResult.getColumnName());
		assertEquals(0, vdResult.getNullCount());
		assertEquals(0, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[obama->4], [doe->2]]]", vdResult.getTopValues()
				.toString());

		vdResult = (ValueDistributionResult) results.get(3);
		assertEquals("Middlename", vdResult.getColumnName());
		assertEquals(4, vdResult.getNullCount());
		assertEquals(0, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[hussein->2]]]", vdResult.getTopValues()
				.toString());
	}
}
