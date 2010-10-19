package org.eobjects.analyzer.test.full.scenarios;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobFactory;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.job.runner.JobStatus;
import org.eobjects.analyzer.test.ActivityAwareMultiThreadedTaskRunner;
import org.eobjects.analyzer.test.TestHelper;

/**
 * Tests that a job where the componens consume columns from different tables
 * will not work.
 * 
 * @author Kasper Sørensen
 */
public class UnavailableFilterErrorHandlingTest extends TestCase {

	public void testScenario() throws Exception {
		ActivityAwareMultiThreadedTaskRunner taskRunner = new ActivityAwareMultiThreadedTaskRunner();

		AnalyzerBeansConfiguration conf = TestHelper.createAnalyzerBeansConfiguration(taskRunner,
				TestHelper.createSampleDatabaseDatastore("orderdb"));

		AnalysisJobBuilder jobBuilder = new JaxbJobFactory(conf).create(new File(
				"src/test/resources/example-job-error-unavailable-filter.xml"));
		AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

		AnalysisRunner runner = new AnalysisRunnerImpl(conf);
		AnalysisResultFuture resultFuture = runner.run(analysisJob);

		JobStatus status = resultFuture.getStatus();
		assertTrue(JobStatus.NOT_FINISHED == status || JobStatus.ERRORNOUS == status);

		resultFuture.await();

		assertFalse(resultFuture.isSuccessful());

		List<Throwable> errors = resultFuture.getErrors();
		assertEquals(1, errors.size());
		assertEquals(IllegalStateException.class, errors.get(0).getClass());
		assertEquals("Could not detect next consumer in processing order", errors.get(0).getMessage());

		// We will give the remaining tasks (such as close tasks etc.) a few
		// second to stop
		int taskCount = taskRunner.assertAllBegunTasksFinished(6000);

		assertTrue(taskCount > 4);
	}
}
