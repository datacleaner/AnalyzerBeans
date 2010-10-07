package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunNextTaskCompletionListener implements CompletionListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Task _nextTask;
	private final TaskRunner _taskRunner;
	private final ErrorReporter _nextErrorReporter;

	public RunNextTaskCompletionListener(TaskRunner taskRunner, Task nextTask, ErrorReporter nextErrorReporter) {
		_taskRunner = taskRunner;
		_nextTask = nextTask;
		_nextErrorReporter = nextErrorReporter;
	}

	@Override
	public void onComplete() {
		logger.debug("onComplete()");
		_taskRunner.run(_nextTask, _nextErrorReporter);
	}

}
