package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

public interface TaskRunner {

	public void run(Task task);
	
	public void shutdown();
}
