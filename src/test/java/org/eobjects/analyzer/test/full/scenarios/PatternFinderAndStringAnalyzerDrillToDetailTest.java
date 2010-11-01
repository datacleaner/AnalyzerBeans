package org.eobjects.analyzer.test.full.scenarios;

import java.util.List;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.result.DefaultResultProducer;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.result.QueryResultProducer;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.CollectionUtils;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.MetaModelTestCase;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class PatternFinderAndStringAnalyzerDrillToDetailTest extends MetaModelTestCase {

	public void testScenario() throws Throwable {
		DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
				"org.eobjects.analyzer.beans", true);
		StorageProvider storageProvider = TestHelper.createStorageProvider();
		TaskRunner taskRunner = new MultiThreadedTaskRunner(30);

		DatastoreCatalog datastoreCatalog = TestHelper.createDatastoreCatalog();
		ReferenceDataCatalog referenceDataCatalog = TestHelper.createReferenceDataCatalog();

		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
				referenceDataCatalog, descriptorProvider, taskRunner, storageProvider);

		DataContext dc = DataContextFactory.createJdbcDataContext(getTestDbConnection());

		AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
		ajb.setDataContextProvider(new SingleDataContextProvider(dc, new JdbcDatastore("foobar", dc)));

		Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
		assertNotNull(table);

		Column jobTitleColumn = table.getColumnByName("JOBTITLE");
		assertNotNull(jobTitleColumn);

		Column emailColumn = table.getColumnByName("EMAIL");
		assertNotNull(emailColumn);

		ajb.addSourceColumns(jobTitleColumn, emailColumn);

		TransformerJobBuilder<EmailStandardizerTransformer> emailStd1 = ajb.addTransformer(
				EmailStandardizerTransformer.class).addInputColumn(ajb.getSourceColumnByName("EMAIL"));

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> pf = ajb
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		pf.addInputColumn(ajb.getSourceColumnByName("JOBTITLE"));
		pf.getConfigurableBean().setDiscriminateTextCase(false);

		RowProcessingAnalyzerJobBuilder<StringAnalyzer> sa = ajb.addRowProcessingAnalyzer(StringAnalyzer.class);
		sa.addInputColumns(ajb.getSourceColumnByName("EMAIL"), emailStd1.getOutputColumnByName("Username"),
				emailStd1.getOutputColumnByName("Domain"));

		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(ajb.toAnalysisJob());
		if(!resultFuture.isSuccessful()) {
			throw resultFuture.getErrors().iterator().next();
		}

		CrosstabResult result;

		// pattern finder result tests
		{
			result = (CrosstabResult) resultFuture.getResult(pf.toAnalyzerJob());
			String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");

			assertEquals(5, resultLines.length);

			assertEquals("                            Match count Sample      ", resultLines[0]);
			assertTrue(resultLines[1].startsWith("aaaaa aaaaaaaaa                      19"));

			ResultProducer resultProducer = result.getCrosstab().where("Pattern", "aaaaa aaaaaaaaa")
					.where("Measures", "Match count").explore();
			assertEquals(DefaultResultProducer.class, resultProducer.getClass());
			AnalyzerResult result2 = resultProducer.getResult();
			assertEquals(ListResult.class, result2.getClass());

			@SuppressWarnings("unchecked")
			List<String> values = ((ListResult<String>) result2).getValues();
			assertEquals(19, values.size());
			values = CollectionUtils.sorted(values);
			assertEquals(
					"[Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, VP Marketing, VP Sales]",
					values.toString());
		}

		// string analyzer tests
		{
			result = (CrosstabResult) resultFuture.getResult(sa.toAnalyzerJob());
			String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");

			assertEquals("                                         EMAIL Username   Domain ", resultLines[0]);
			assertEquals("Total char count                           655      172      460 ", resultLines[5]);
			assertEquals("Max chars                                   31       10       20 ", resultLines[6]);
			assertEquals("Min chars                                   26        5       20 ", resultLines[7]);

			// username is a virtual columns so it is not queryable
			ResultProducer resultProducer = result.getCrosstab().where("Column", "Username").where("Measures", "Max chars")
					.explore();
			assertNull(resultProducer);

			// email is a physical column so it IS queryable
			resultProducer = result.getCrosstab().where("Column", "EMAIL").where("Measures", "Max chars").explore();
			assertNotNull(resultProducer);
			assertEquals(QueryResultProducer.class, resultProducer.getClass());

			AnalyzerResult result2 = resultProducer.getResult();
			assertEquals(DataSetResult.class, result2.getClass());

			DataSetResult dsr = (DataSetResult) result2;
			List<Row> rows = dsr.getRows();
			assertEquals(1, rows.size());
			assertEquals("Row[values=[wpatterson@classicmodelcars.com, 1]]", rows.get(0).toString());
		}

	}
}
