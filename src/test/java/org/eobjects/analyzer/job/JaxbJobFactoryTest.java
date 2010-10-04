package org.eobjects.analyzer.job;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.DateGapAnalyzerResult;
import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.result.TableDifference;
import org.eobjects.analyzer.result.renderer.DateGapTextRenderer;
import org.eobjects.analyzer.test.TestHelper;

public class JaxbJobFactoryTest extends TestCase {

	public void testNamedInputs() throws Exception {
		AnalyzerBeansConfiguration conf = TestHelper
				.createAnalyzerBeansConfiguration(TestHelper
						.createSampleDatabaseDatastore("my database"));
		JaxbJobFactory factory = new JaxbJobFactory(conf);
		AnalysisJobBuilder jobBuilder = factory.create(new File(
				"src/test/resources/example-job-named-inputs.xml"));
		assertEquals(true, jobBuilder.isConfigured());

		assertEquals(2, jobBuilder.getTransformerJobBuilders().size());

		List<AnalyzerJobBuilder<?>> analyzerJobBuilders = jobBuilder
				.getAnalyzerJobBuilders();
		assertEquals(1, analyzerJobBuilders.size());

		AnalyzerJobBuilder<?> analyzerJobBuilder = analyzerJobBuilders.get(0);
		AnalyzerJob analyzerJob = analyzerJobBuilder.toAnalyzerJob();
		BeanConfiguration configuration = analyzerJob.getConfiguration();

		InputColumn<?> col1 = (InputColumn<?>) configuration
				.getProperty(analyzerJob.getDescriptor().getConfiguredProperty(
						"From column"));
		assertEquals("date 1", col1.getName());

		InputColumn<?> col2 = (InputColumn<?>) configuration
				.getProperty(analyzerJob.getDescriptor().getConfiguredProperty(
						"To column"));
		assertEquals("date 2", col2.getName());

		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf)
				.run(jobBuilder.toAnalysisJob());
		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(1, results.size());
		DateGapAnalyzerResult result = (DateGapAnalyzerResult) results.get(0);
		String[] resultLines = new DateGapTextRenderer().render(result).split(
				"\n");
		assertEquals(58, resultLines.length);
		assertEquals(" - time gap: 2003-01-18 to 2003-01-29", resultLines[0]);
		assertEquals(" - time gap: 2003-02-09 to 2003-02-11", resultLines[1]);
		assertEquals(" - time gap: 2003-05-16 to 2003-05-20", resultLines[2]);
		assertEquals(" - time gap: 2003-07-23 to 2003-07-24", resultLines[3]);
		assertEquals(" - time gap: 2003-08-21 to 2003-08-25", resultLines[4]);
		assertEquals(" - time gap: 2003-09-02 to 2003-09-03", resultLines[5]);
		assertEquals(" - time gap: 2003-11-03 to 2003-11-04", resultLines[6]);
		assertEquals(" - time gap: 2003-12-17 to 2004-01-02", resultLines[7]);
		assertEquals(" - time gap: 2004-05-24 to 2004-05-26", resultLines[8]);
		assertEquals(" - time gap: 2004-09-22 to 2004-09-27", resultLines[9]);
		assertEquals(" - time gap: 2004-12-24 to 2005-01-05", resultLines[10]);
		assertEquals(" - time gap: 2005-05-28 to 2005-05-29", resultLines[11]);
		assertEquals(" - time overlap: 2003-01-09 to 2003-01-18",
				resultLines[12]);
		assertEquals(" - time overlap: 2003-01-31 to 2003-02-07",
				resultLines[13]);
		assertEquals(" - time overlap: 2005-05-29 to 2005-06-08",
				resultLines[57]);
	}

	public void testInvalidRead() throws Exception {
		JaxbJobFactory factory = new JaxbJobFactory(
				TestHelper.createAnalyzerBeansConfiguration());
		try {
			factory.create(new File(
					"src/test/resources/example-job-invalid.xml"));
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			assertEquals(
					"javax.xml.bind.UnmarshalException: unexpected element "
							+ "(uri:\"http://eobjects.org/analyzerbeans/job/1.0\", local:\"datacontext\"). "
							+ "Expected elements are <{http://eobjects.org/analyzerbeans/job/1.0}columns>,"
							+ "<{http://eobjects.org/analyzerbeans/job/1.0}data-context>",
					e.getMessage());
		}
	}

	public void testDeserializeTableReference() throws Exception {
		AnalyzerBeansConfiguration configuration = TestHelper
				.createAnalyzerBeansConfiguration(TestHelper
						.createSampleDatabaseDatastore("my database"));
		JaxbJobFactory factory = new JaxbJobFactory(configuration);
		AnalysisJobBuilder builder = factory.create(new File(
				"src/test/resources/example-job-compare-tables.xml"));
		AnalysisJob analysisJob = builder.toAnalysisJob();

		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(
				configuration).run(analysisJob);
		List<AnalyzerResult> results = resultFuture.getResults();
		assertEquals(1, results.size());

		TableComparisonResult result = (TableComparisonResult) results.get(0);
		List<TableDifference<?>> tableDifferences = result
				.getTableDifferences();
		assertEquals(4, tableDifferences.size());
		assertEquals(
				"Tables 'CUSTOMER_W_TER' and 'CUSTOMERS' differ on 'name': [CUSTOMER_W_TER] vs. [CUSTOMERS]",
				tableDifferences.get(0).toString());
		assertEquals(
				"Tables 'CUSTOMER_W_TER' and 'CUSTOMERS' differ on 'unmatched column': [EMPLOYEENUMBER] vs. [null]",
				tableDifferences.get(1).toString());
		assertEquals(
				"Tables 'CUSTOMER_W_TER' and 'CUSTOMERS' differ on 'unmatched column': [TERRITORY] vs. [null]",
				tableDifferences.get(2).toString());
		assertEquals(
				"Tables 'CUSTOMER_W_TER' and 'CUSTOMERS' differ on 'unmatched column': [null] vs. [SALESREPEMPLOYEENUMBER]",
				tableDifferences.get(3).toString());

		List<ColumnComparisonResult> columnComparisonResults = result
				.getColumnComparisonResults();
		assertEquals(9, columnComparisonResults.size());

		for (ColumnComparisonResult columnComparisonResult : columnComparisonResults) {
			int differences = columnComparisonResult.getColumnDifferences()
					.size();
			if (differences == 1) {
				assertEquals("nullable", columnComparisonResult
						.getColumnDifferences().get(0).getValueName());
			} else {
				assertEquals(
						"[Columns 'CREDITLIMIT' and 'CREDITLIMIT' differ on 'type': [DECIMAL] vs. [NUMERIC], "
								+ "Columns 'CREDITLIMIT' and 'CREDITLIMIT' differ on 'native type': [DECIMAL] vs. [NUMERIC]]",
						columnComparisonResult.getColumnDifferences()
								.toString());
			}
		}
	}

	public void testMissingDatastore() throws Exception {
		AnalyzerBeansConfiguration configuration = TestHelper
				.createAnalyzerBeansConfiguration();
		JaxbJobFactory factory = new JaxbJobFactory(configuration);
		try {
			factory.create(new File("src/test/resources/example-job-valid.xml"));
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("No such datastore: my database", e.getMessage());
		}
	}

	public void testMissingTransformerDescriptor() throws Exception {
		AnalyzerBeansConfiguration configuration = TestHelper
				.createAnalyzerBeansConfiguration(TestHelper
						.createSampleDatabaseDatastore("my database"));
		JaxbJobFactory factory = new JaxbJobFactory(configuration);
		try {
			factory.create(new File(
					"src/test/resources/example-job-missing-descriptor.xml"));
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("No such transformer descriptor: tokenizerDescriptor",
					e.getMessage());
		}
	}

	public void testValidJob() throws Exception {
		AnalyzerBeansConfiguration configuration = TestHelper
				.createAnalyzerBeansConfiguration(TestHelper
						.createSampleDatabaseDatastore("my database"));
		JaxbJobFactory factory = new JaxbJobFactory(configuration);
		AnalysisJobBuilder builder = factory.create(new File(
				"src/test/resources/example-job-valid.xml"));
		assertTrue(builder.isConfigured());

		List<MetaModelInputColumn> sourceColumns = builder.getSourceColumns();
		assertEquals(3, sourceColumns.size());
		assertEquals(
				"MetaModelInputColumn[Column[name=FIRSTNAME,columnNumber=2,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]",
				sourceColumns.get(0).toString());
		assertEquals(
				"MetaModelInputColumn[Column[name=LASTNAME,columnNumber=1,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]",
				sourceColumns.get(1).toString());
		assertEquals(
				"MetaModelInputColumn[Column[name=EMAIL,columnNumber=4,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=100]]",
				sourceColumns.get(2).toString());

		assertEquals(1, builder.getTransformerJobBuilders().size());
		assertEquals(
				"[TransformedInputColumn[id=trans-1,name=username,type=STRING], "
						+ "TransformedInputColumn[id=trans-2,name=domain,type=STRING]]",
				builder.getTransformerJobBuilders().get(0).getOutputColumns()
						.toString());
		assertEquals(
				"[TransformedInputColumn[id=trans-1,name=username,type=STRING], "
						+ "TransformedInputColumn[id=trans-2,name=domain,type=STRING], "
						+ "MetaModelInputColumn[Column[name=FIRSTNAME,columnNumber=2,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]], "
						+ "MetaModelInputColumn[Column[name=LASTNAME,columnNumber=1,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]]",
				Arrays.toString(builder.getAnalyzerJobBuilders().get(0)
						.toAnalyzerJob().getInput()));

		List<AnalyzerResult> results = new AnalysisRunnerImpl(configuration)
				.run(builder.toAnalysisJob()).getResults();
		assertEquals(1, results.size());
		CrosstabResult crosstabResult = (CrosstabResult) results.get(0);

		String[] resultLines = crosstabResult.toString().split("\n");
		assertEquals(53, resultLines.length);
		assertEquals("Crosstab:", resultLines[0]);
		assertEquals("FIRSTNAME,Avg chars: 5.391304347826087", resultLines[1]);
		assertEquals("FIRSTNAME,Avg white spaces: 0.043478260869565216",
				resultLines[2]);
		assertEquals("FIRSTNAME,Char count: 124", resultLines[3]);
		assertEquals("FIRSTNAME,Lowercase chars: 79%", resultLines[4]);
	}
}
