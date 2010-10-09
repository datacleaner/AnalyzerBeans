package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskRunnable implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(TaskRunnable.class);
	private final Task _task;
	private final TaskListener _listener;

	public TaskRunnable(Task task, TaskListener listener) {
		if (task == null) {
			throw new IllegalArgumentException("task cannot be null");
		}
		_task = task;
		_listener = listener;
	}

	@Override
	public final void run() {
		if (_listener == null) {

			// execute without listener
			try {
				_task.execute();
			} catch (Throwable t) {
				logger.warn("No TaskListener to inform of error!", t);
			}

		} else {

			// execute with listener
			_listener.onBegin(_task);
			try {
				_task.execute();
				_listener.onComplete(_task);
			} catch (Throwable t) {
				_listener.onError(_task, t);
			}
		}
	}

	public final Task getTask() {
		return _task;
	}

	public final TaskListener getListener() {
		return _listener;
	}
}
