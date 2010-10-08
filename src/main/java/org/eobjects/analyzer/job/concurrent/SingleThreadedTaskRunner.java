package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SingleThreadedTaskRunner implements TaskRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final BlockingQueue<TaskRunnable> _taskRunnableQueue = new LinkedBlockingQueue<TaskRunnable>();
	private final boolean _queueTasks;
	private boolean _running = false;

	public SingleThreadedTaskRunner() {
		this(false);
	}

	public SingleThreadedTaskRunner(boolean queueTasks) {
		// TODO: Consider removing the ability to queue tasks = it may introduce
		// bugs because of endless waiting for tasks (that have been queued) to
		// complete.
		_queueTasks = queueTasks;
	}

	@Override
	public void run(final Task task, final ErrorReporter errorReporter) {
		logger.debug("run({})", task);
		TaskRunnable taskRunnable = new TaskRunnable(task, errorReporter);
		if (_queueTasks) {
			_taskRunnableQueue.add(taskRunnable);
			if (!_running) {
				_running = true;
				while (!_taskRunnableQueue.isEmpty()) {
					TaskRunnable nextTaskRunnable = _taskRunnableQueue.poll();
					nextTaskRunnable.run();
				}
				_running = false;
			}
		} else {
			taskRunnable.run();
		}
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, nothing to do");
	}
}
