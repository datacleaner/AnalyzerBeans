package org.eobjects.analyzer.test.full.scenarios;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.LazyDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.PreviousErrorsExistException;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.InMemoryCollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.result.NumberResult;
import org.eobjects.analyzer.test.ActivityAwareMultiThreadedTaskRunner;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

/**
 * Tests that a job where one of the row processing consumers fail is gracefully
 * error handled.
 * 
 * @author Kasper Sørensen
 */
public class ErrorInRowProcessingConsumerTest extends TestCase {

	@AnalyzerBean("Errornous analyzer")
	public static class ErrornousAnalyzer implements RowProcessingAnalyzer<NumberResult> {

		private final AtomicInteger counter = new AtomicInteger(0);

		@Configured
		InputColumn<String> inputColumn;

		@Override
		public NumberResult getResult() {
			return new NumberResult(counter.get());
		}

		@Override
		public void run(InputRow row, int distinctCount) {
			assertNotNull(inputColumn);
			assertNotNull(row);
			assertEquals(1, distinctCount);
			String value = row.getValue(inputColumn);
			assertNotNull(value);
			int count = counter.incrementAndGet();
			if (count == 3) {
				throw new IllegalStateException("This analyzer can only analyze two rows!");
			}
		}

	}

	public void testScenario() throws Exception {
		ActivityAwareMultiThreadedTaskRunner taskRunner = new ActivityAwareMultiThreadedTaskRunner();

		Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
		DescriptorProvider descriptorProvider = new LazyDescriptorProvider();
		AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(datastore),
				new ReferenceDataCatalogImpl(), descriptorProvider, taskRunner, new InMemoryCollectionProvider());

		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
		ajb.setDatastore(datastore);

		SchemaNavigator schemaNavigator = datastore.getDataContextProvider().getSchemaNavigator();
		Column column = schemaNavigator.convertToColumn("PUBLIC.EMPLOYEES.EMAIL");
		assertNotNull(column);

		ajb.addSourceColumn(column);
		ajb.addRowProcessingAnalyzer(ErrornousAnalyzer.class).addInputColumn(ajb.getSourceColumns().get(0));

		AnalysisJob job = ajb.toAnalysisJob();

		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(job);

		assertTrue(resultFuture.isErrornous());

		// isErrornous should be blocking
		assertTrue(resultFuture.isDone());

		List<Throwable> errors = resultFuture.getErrors();
		assertEquals(2, errors.size());
		assertEquals(IllegalStateException.class, errors.get(0).getClass());
		assertEquals("This analyzer can only analyze two rows!", errors.get(0).getMessage());
		assertEquals(PreviousErrorsExistException.class, errors.get(1).getClass());
		assertEquals("A previous exception has occurred", errors.get(1).getMessage());

		int taskCount = taskRunner.assertAllBegunTasksFinished(500);
		assertTrue(taskCount > 4);
	}
}
