package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

public interface TaskListener {

	public void onBegin(Task task);

	public void onComplete(Task task);

	public void onError(Task task, Throwable throwable);
}
