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
	private String _name;

	public ScheduleTasksCompletionListener(String name, TaskRunner taskRunner,
			int tasksToWaitFor, Collection<? extends Task> tasksToSchedule) {
		_name = name;
		_taskRunner = taskRunner;
		_tasks = tasksToSchedule;
		_countDown = new AtomicInteger(tasksToWaitFor);
		
		if (tasksToWaitFor == 0) {
			// immediate completion
			_countDown = new AtomicInteger(1);
			onComplete();
		}
	}

	@Override
	public void onComplete() {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete({}), remaining count = {}", _name, count);
		if (count == 0) {
			logger.info("Scheduling {} tasks", _tasks.size());
			for (Task task : _tasks) {
				_taskRunner.run(task);
			}
		}
	}

}
