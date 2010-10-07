package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.concurrent.ErrorReporter;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the TaskRunner that only submits jobs when no errors have been
 * reported yet. This makes sure that a job will finish early if an error is
 * reported.
 * 
 * @author Kasper Sørensen
 */
final class ErrorAwareTaskRunnerWrapper implements TaskRunner {

	private static final Logger logger = LoggerFactory.getLogger(ErrorAwareTaskRunnerWrapper.class);
	private final AnalysisResultFuture _analysisResultFuture;
	private final TaskRunner _delegate;

	public ErrorAwareTaskRunnerWrapper(AnalysisResultFuture analysisResultFuture, TaskRunner delegate) {
		_analysisResultFuture = analysisResultFuture;
		_delegate = delegate;
	}

	@Override
	public void run(Task task, ErrorReporter errorReporter) {
		JobStatus status = _analysisResultFuture.getStatus();

		if (status == JobStatus.NOT_FINISHED) {
			_delegate.run(task, errorReporter);
		} else {
			logger.warn("Skipping task ({}) because errors have been reported in previous tasks (status={}).", task, status);
			// TODO: Add a special exception to the ErrorReporter?
		}
	}

	@Override
	public void shutdown() {
		_delegate.shutdown();
	}

}
