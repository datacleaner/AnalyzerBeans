package org.eobjects.analyzer.job;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.result.TableDifference;
import org.eobjects.analyzer.test.TestHelper;

public class JaxbJobFactoryTest extends TestCase {

	public void testNamedInputs() throws Exception {
		JaxbJobFactory factory = new JaxbJobFactory(
				TestHelper.createAnalyzerBeansConfiguration(TestHelper
						.createSampleDatabaseDatastore("my database")));
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

		Object col1 = configuration.getProperty(analyzerJob.getDescriptor()
				.getConfiguredProperty("From column"));
		assertEquals(
				"TransformedInputColumn[id=trans-1,name=date 1,type=DATE]",
				col1.toString());

		Object col2 = configuration.getProperty(analyzerJob.getDescriptor()
				.getConfiguredProperty("To column"));
		assertEquals(
				"TransformedInputColumn[id=trans-2,name=date 2,type=DATE]",
				col2.toString());
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
				"MetaModelInputColumn[JdbcColumn[name=FIRSTNAME,columnNumber=2,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]",
				sourceColumns.get(0).toString());
		assertEquals(
				"MetaModelInputColumn[JdbcColumn[name=LASTNAME,columnNumber=1,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]",
				sourceColumns.get(1).toString());
		assertEquals(
				"MetaModelInputColumn[JdbcColumn[name=EMAIL,columnNumber=4,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=100]]",
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
						+ "MetaModelInputColumn[JdbcColumn[name=FIRSTNAME,columnNumber=2,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]], "
						+ "MetaModelInputColumn[JdbcColumn[name=LASTNAME,columnNumber=1,type=VARCHAR,nullable=false,indexed=false,nativeType=VARCHAR,columnSize=50]]]",
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
