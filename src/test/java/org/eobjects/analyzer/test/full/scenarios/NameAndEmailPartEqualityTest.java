package org.eobjects.analyzer.test.full.scenarios;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.EqualityValidationAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.NameStandardizerTransformer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ValidationResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.storage.StorageProvider;
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
		StorageProvider storageProvider = TestHelper.createStorageProvider();
		TaskRunner taskRunner = new SingleThreadedTaskRunner();
		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper.createReferenceDataCatalog();
		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner, storageProvider);

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

		final List<MutableInputColumn<?>> nameColumns = nameTransformerJobBuilder.getOutputColumns();
		assertEquals(4, nameColumns.size());
		assertEquals("Firstname", nameColumns.get(0).getName());
		assertEquals("Lastname", nameColumns.get(1).getName());
		assertEquals("Middlename", nameColumns.get(2).getName());
		assertEquals("Titulation", nameColumns.get(3).getName());

		TransformerJobBuilder<EmailStandardizerTransformer> emailTransformerJobBuilder = analysisJobBuilder
				.addTransformer(EmailStandardizerTransformer.class);
		emailTransformerJobBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

		assertTrue(emailTransformerJobBuilder.isConfigured());

		@SuppressWarnings("unchecked")
		final MutableInputColumn<String> usernameColumn = (MutableInputColumn<String>) emailTransformerJobBuilder
				.getOutputColumnByName("Username");
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

		RowProcessingAnalyzerJobBuilder<EqualityValidationAnalyzer> equalsAnalyzerJobBuilder = analysisJobBuilder
				.addRowProcessingAnalyzer(EqualityValidationAnalyzer.class);

		assertEquals("Input1", equalsAnalyzerJobBuilder.getDescriptor().getConfiguredPropertiesForInput().iterator().next()
				.getName());

		equalsAnalyzerJobBuilder.addInputColumn(nameTransformerJobBuilder.getOutputColumnByName("Firstname"),
				equalsAnalyzerJobBuilder.getDescriptor().getConfiguredProperty("Input1"));
		equalsAnalyzerJobBuilder.addInputColumn(usernameColumn, equalsAnalyzerJobBuilder.getDescriptor()
				.getConfiguredProperty("Input2"));
		assertTrue(equalsAnalyzerJobBuilder.isConfigured());

		AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder.toAnalysisJob());
		List<AnalyzerResult> results = resultFuture.getResults();

		assertEquals(6, results.size());

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
		InputColumn<?>[] highlightedColumns = validationResult.getHighlightedColumns();
		assertEquals(2, highlightedColumns.length);
		assertEquals("Firstname", highlightedColumns[0].getName());
		assertEquals("Username", highlightedColumns[1].getName());
		InputRow[] invalidRows = validationResult.getRows();
		assertEquals(2, invalidRows.length);

		// sort the rows to make order determinable
		Arrays.sort(invalidRows, new Comparator<InputRow>() {
			@Override
			public int compare(InputRow o1, InputRow o2) {
				String emailUsername1 = o1.getValue(usernameColumn);
				String emailUsername2 = o2.getValue(usernameColumn);
				return emailUsername1.compareTo(emailUsername2);
			}
		});
		assertEquals("barack.hussein.obama", invalidRows[0].getValue(usernameColumn));
		assertEquals("barack.obama", invalidRows[1].getValue(usernameColumn));
	}
}
