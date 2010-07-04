package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

public class TaskRunnable implements Runnable {
	
	private Task _task;
	
	public TaskRunnable(Task task) {
		_task = task;
	}

	@Override
	public void run() {
		try {
			_task.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
