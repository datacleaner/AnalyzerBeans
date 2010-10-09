package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

public final class RunNextTaskCompletionListener implements TaskListener {

	private final Task _nextTask;
	private final TaskListener _nextListener;
	private final TaskRunner _taskRunner;

	public RunNextTaskCompletionListener(TaskRunner taskRunner, Task nextTask, TaskListener nextListener) {
		_taskRunner = taskRunner;
		_nextTask = nextTask;
		_nextListener = nextListener;
	}

	@Override
	public void onBegin(Task task) {
	}

	@Override
	public void onComplete(Task task) {
		_taskRunner.run(_nextTask, _nextListener);
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		_nextListener.onError(task, throwable);
	}

}
