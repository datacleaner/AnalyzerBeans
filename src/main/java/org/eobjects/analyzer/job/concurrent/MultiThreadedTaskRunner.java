package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eobjects.analyzer.job.tasks.Task;

public class MultiThreadedTaskRunner implements TaskRunner {

	private ExecutorService threadPool;

	public MultiThreadedTaskRunner() {
		threadPool = Executors.newCachedThreadPool();
	}
	
	public MultiThreadedTaskRunner(int numThreads) {
		threadPool = Executors.newFixedThreadPool(numThreads);
	}
	
	@Override
	public void run(Task task) {
		threadPool.submit(new TaskRunnable(task));
	}
}
