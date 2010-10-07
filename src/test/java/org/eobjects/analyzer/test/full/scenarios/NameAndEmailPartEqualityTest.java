package org.eobjects.analyzer.test.full.scenarios;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.EqualityAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.NameStandardizerTransformer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
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
import org.eobjects.analyzer.result.ValidationResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.test.TestHelper;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class NameAndEmailPartEqualityTest extends TestCase {

	public void testScenario() throws Exception {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
				"org.eobjects.analyzer.beans", true);
		CollectionProvider collectionProvider = new InMemoryCollectionProvider();
		TaskRunner taskRunner = new SingleThreadedTaskRunner(false);
		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper.createReferenceDataCatalog();
		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner, collectionProvider);

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

		DataContext dc = DataContextFactory.createCsvDataContext(new File(
				"src/test/resources/NameAndEmailPartEqualityTest-data.csv"));

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDataContextProvider(new SingleDataContextProvider(dc, null));

		Schema schema = dc.getDefaultSchema();
		Table table = schema.getTables()[0];
		assertNotNull(table);

		Column nameColumn = table.getColumnByName("name");
		Column emailColumn = table.getColumnByName("email");

		analysisJobBuilder.addSourceColumns(nameColumn, emailColumn);

		TransformerJobBuilder<NameStandardizerTransformer> nameTransformerJobBuilder = analysisJobBuilder
				.addTransformer(NameStandardizerTransformer.class);
		nameTransformerJobBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("name"));
		nameTransformerJobBuilder.setConfiguredProperty("Name patterns", NameStandardizerTransformer.DEFAULT_PATTERNS);

		assertTrue(nameTransformerJobBuilder.isConfigured());

		List<MutableInputColumn<?>> nameColumns = nameTransformerJobBuilder.getOutputColumns();
		assertEquals(4, nameColumns.size());
		assertEquals("Firstname", nameColumns.get(0).getName());
		assertEquals("Lastname", nameColumns.get(1).getName());
		assertEquals("Middlename", nameColumns.get(2).getName());
		assertEquals("Titulation", nameColumns.get(3).getName());

		TransformerJobBuilder<EmailStandardizerTransformer> emailTransformerJobBuilder = analysisJobBuilder
				.addTransformer(EmailStandardizerTransformer.class);
		emailTransformerJobBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

		assertTrue(emailTransformerJobBuilder.isConfigured());
		MutableInputColumn<?> usernameColumn = emailTransformerJobBuilder.getOutputColumnByName("Username");
		assertNotNull(usernameColumn);

		assertTrue(analysisJobBuilder.addRowProcessingAnalyzer(StringAnalyzer.class).addInputColumns(nameColumns)
				.addInputColumns(emailTransformerJobBuilder.getOutputColumns()).isConfigured());

		for (InputColumn<?> inputColumn : nameColumns) {
			RowProcessingAnalyzerJobBuilder<ValueDistributionAnalyzer> analyzerJobBuilder = analysisJobBuilder
					.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class);
			analyzerJobBuilder.addInputColumn(inputColumn);
			analyzerJobBuilder.setConfiguredProperty("Record unique values", false);
			analyzerJobBuilder.setConfiguredProperty("Top n most frequent values", 1000);
			analyzerJobBuilder.setConfiguredProperty("Bottom n most frequent values", 1000);
			assertTrue(analyzerJobBuilder.isConfigured());
		}

		RowProcessingAnalyzerJobBuilder<EqualityAnalyzer> equalsAnalyzerJobBuilder = analysisJobBuilder
				.addRowProcessingAnalyzer(EqualityAnalyzer.class);
		equalsAnalyzerJobBuilder.addInputColumns(nameTransformerJobBuilder.getOutputColumnByName("Firstname"),
				usernameColumn);
		assertTrue(equalsAnalyzerJobBuilder.isConfigured());

		AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder.toAnalysisJob());
		List<AnalyzerResult> results = resultFuture.getResults();

		assertEquals(6, results.size());

		AnalyzerResult result = results.get(0);
		assertEquals("StringAnalyzer", result.getProducerClass().getSimpleName());

		ValueDistributionResult vdResult = (ValueDistributionResult) results.get(1);
		assertEquals("Firstname", vdResult.getColumnName());
		assertEquals(0, vdResult.getNullCount());
		assertEquals(2, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[barack->4]]]", vdResult.getTopValues().toString());

		vdResult = (ValueDistributionResult) results.get(2);
		assertEquals("Lastname", vdResult.getColumnName());
		assertEquals(0, vdResult.getNullCount());
		assertEquals(0, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[obama->4], [doe->2]]]", vdResult.getTopValues().toString());

		vdResult = (ValueDistributionResult) results.get(3);
		assertEquals("Middlename", vdResult.getColumnName());
		assertEquals(4, vdResult.getNullCount());
		assertEquals(0, vdResult.getUniqueCount());
		assertEquals("ValueCountList[[[hussein->2]]]", vdResult.getTopValues().toString());

		ValidationResult validationResult = (ValidationResult) results.get(5);
		assertEquals("[Firstname, Username]", Arrays.toString(validationResult.getColumnNames()));
		List<Object[]> invalidRows = validationResult.getInvalidRows();
		assertEquals(2, invalidRows.size());

		// sort the rows to make order determinable
		Collections.sort(invalidRows, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				String emailUsername1 = (String) o1[1];
				String emailUsername2 = (String) o2[1];
				return emailUsername1.compareTo(emailUsername2);
			}
		});
		assertEquals("[barack, barack.hussein.obama]", Arrays.toString(invalidRows.get(0)));
		assertEquals("[barack, barack.obama]", Arrays.toString(invalidRows.get(1)));
	}
}
