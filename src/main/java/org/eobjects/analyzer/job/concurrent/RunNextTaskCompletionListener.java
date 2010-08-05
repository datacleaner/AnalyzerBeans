package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunNextTaskCompletionListener implements CompletionListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Task _nextTask;
	private final TaskRunner _taskRunner;
	
	public RunNextTaskCompletionListener(TaskRunner taskRunner, Task nextTask) {
		_taskRunner = taskRunner;
		_nextTask = nextTask;
	}

	@Override
	public void onComplete() {
		logger.debug("onComplete()");
		_taskRunner.run(_nextTask);
	}

}
