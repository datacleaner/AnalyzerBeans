package org.eobjects.analyzer.job.concurrent;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleTasksCompletionListener implements CompletionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleTasksCompletionListener.class);

	private AtomicInteger _countDown;
	private TaskRunner _taskRunner;
	private Collection<? extends Task> _tasks;

	public ScheduleTasksCompletionListener(TaskRunner taskRunner,
			int tasksToWaitFor, Collection<? extends Task> tasksToSchedule) {
		_taskRunner = taskRunner;
		_tasks = tasksToSchedule;
		_countDown = new AtomicInteger(tasksToWaitFor);
	}

	@Override
	public void onComplete() {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete(), count = {}", count);
		if (count == 0) {
			logger.info("Scheduling {} tasks", _tasks.size());
			for (Task task : _tasks) {
				_taskRunner.run(task);
			}
		}
	}

}
