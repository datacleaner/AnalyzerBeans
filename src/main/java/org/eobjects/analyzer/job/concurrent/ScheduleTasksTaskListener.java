package org.eobjects.analyzer.job.concurrent;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScheduleTasksTaskListener implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTasksTaskListener.class);

	private final AtomicInteger _countDown;
	private final TaskRunner _taskRunner;
	private final Collection<TaskRunnable> _tasks;
	private final String _name;
	private volatile Throwable _error;

	public ScheduleTasksTaskListener(String name, TaskRunner taskRunner, int tasksToWaitFor,
			Collection<TaskRunnable> tasksToSchedule) {
		_name = name;
		_taskRunner = taskRunner;
		_tasks = tasksToSchedule;

		if (tasksToWaitFor == 0) {
			// immediate completion
			_countDown = new AtomicInteger(1);
			onComplete(null);
		} else {
			_countDown = new AtomicInteger(tasksToWaitFor);
		}
	}

	@Override
	public void onComplete(Task task) {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete({}), remaining count = {}", _name, count);
		checkCount(count, task);
	}

	public void onBegin(Task task) {
		// do nothing
	};

	@Override
	public void onError(Task task, Throwable throwable) {
		_error = throwable;
		int count = _countDown.decrementAndGet();
		checkCount(count, task);
	}

	private void checkCount(int count, Task task) {
		if (count == 0) {
			if (_error == null) {
				logger.info("Scheduling {} tasks", _tasks.size());
				for (TaskRunnable tr : _tasks) {
					_taskRunner.run(tr);
				}
			} else {
				for (TaskRunnable tr : _tasks) {
					TaskListener listener = tr.getListener();
					listener.onError(task, _error);
				}
			}
		}
	}
}
