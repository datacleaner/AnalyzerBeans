package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.job.tasks.Task;

public class SingleThreadedTaskRunner implements TaskRunner {

	private BlockingQueue<Task> _tasks = new LinkedBlockingQueue<Task>();
	private boolean _queueTasks;
	private boolean _running = false;

	public SingleThreadedTaskRunner() {
		this(true);
	}

	public SingleThreadedTaskRunner(boolean queueTasks) {
		_queueTasks = queueTasks;
	}

	@Override
	public void run(Task task) {
		if (_queueTasks) {
			_tasks.add(task);
			if (!_running) {
				_running = true;
				while (!_tasks.isEmpty()) {
					Task nextTask = _tasks.poll();
					exec(nextTask);
				}
				_running = false;
			}
		} else {
			exec(task);
		}
	}

	private void exec(Task task) {
		try {
			task.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
