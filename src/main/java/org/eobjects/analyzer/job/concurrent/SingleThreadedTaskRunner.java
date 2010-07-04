package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

public class SingleThreadedTaskRunner implements TaskRunner {

	@Override
	public void run(Task task) {
		try {
			task.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
