package org.eobjects.analyzer.job.runner;

import java.util.List;

import org.eobjects.analyzer.job.concurrent.PreviousErrorsExistException;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.Task;

/**
 * Wrapper for the TaskRunner that only submits jobs when no errors have been
 * reported yet. This makes sure that a job will finish early if an error is
 * reported.
 * 
 * @author Kasper Sørensen
 */
final class ErrorAwareTaskRunnerWrapper implements TaskRunner, ErrorAware {

	// a single shared exception is used if previous exceptions have been
	// reported. This is to make sure that the error message
	// ("A previous exception has occurred") will only be saved once.
	private static final PreviousErrorsExistException exception = new PreviousErrorsExistException(
			"A previous exception has occurred");

	private final TaskRunner _taskRunner;
	private final ErrorAware _errorAware;

	public ErrorAwareTaskRunnerWrapper(ErrorAware errorAware, TaskRunner taskRunner) {
		_taskRunner = taskRunner;
		_errorAware = errorAware;
	}

	@Override
	public void run(Task task, TaskListener taskListener) {
		if (isErrornous()) {
			taskListener.onError(task, exception);
		} else {
			_taskRunner.run(task, taskListener);
		}
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		run(taskRunnable.getTask(), taskRunnable.getListener());
	}

	@Override
	public void shutdown() {
		_taskRunner.shutdown();
	}

	@Override
	public boolean isErrornous() {
		return _errorAware.isErrornous();
	}

	@Override
	public List<Throwable> getErrors() {
		return _errorAware.getErrors();
	}

}
