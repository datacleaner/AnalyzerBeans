package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskRunnable implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(TaskRunnable.class);
	private final Task _task;
	private final ErrorReporter _errorReporter;

	public TaskRunnable(Task task, ErrorReporter errorReporter) {
		if (task == null) {
			throw new IllegalArgumentException("task cannot be null");
		}
		_task = task;
		_errorReporter = errorReporter;
	}

	@Override
	public void run() {
		try {
			_task.execute();
		} catch (Throwable t) {
			if (_errorReporter == null) {
				logger.warn("An error occurred while executing task: " + _task, t);
			} else {
				_errorReporter.reportError(t);
			}
		}
	}

	public Task getTask() {
		return _task;
	}

	public ErrorReporter getErrorReporter() {
		return _errorReporter;
	}
}
