package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadedTaskRunner implements TaskRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ExecutorService threadPool;

	public MultiThreadedTaskRunner() {
		threadPool = Executors.newCachedThreadPool();
	}
	
	public MultiThreadedTaskRunner(int numThreads) {
		threadPool = Executors.newFixedThreadPool(numThreads);
	}
	
	@Override
	public void run(Task task) {
		logger.debug("run({})", task);
		threadPool.submit(new TaskRunnable(task));
	}
}
