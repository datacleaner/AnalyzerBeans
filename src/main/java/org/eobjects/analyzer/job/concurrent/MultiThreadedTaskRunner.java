package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiThreadedTaskRunner implements TaskRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ExecutorService executorService;

	public MultiThreadedTaskRunner() {
		executorService = Executors.newCachedThreadPool();
	}

	public MultiThreadedTaskRunner(int numThreads) {
		executorService = Executors.newFixedThreadPool(numThreads);
	}

	@Override
	public void run(final Task task, final TaskListener listener) {
		logger.debug("run({},{})", task, listener);
		executorService.submit(new TaskRunnable(task, listener));
	}
	
	@Override
	public void run(TaskRunnable taskRunnable) {
		logger.debug("run({})", taskRunnable);
		executorService.submit(taskRunnable);
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, shutting down executor service");
		executorService.shutdown();
	}

	@Override
	protected void finalize() throws Throwable {
		shutdown();
	}
}
