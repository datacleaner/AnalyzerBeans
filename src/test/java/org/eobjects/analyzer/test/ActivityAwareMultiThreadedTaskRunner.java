package org.eobjects.analyzer.test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.Task;

/**
 * Multithreaded task runner appropriate for thread-testing purposes. Unlike the
 * regular multithreaded task runner this TaskRunner saves the futures of all
 * submitted tasks in order to make it possible to inspect them from the
 * perspective of a unittest (typically to assert that no tasks are waiting or
 * such).
 * 
 * @author Kasper Sørensen
 */
public final class ActivityAwareMultiThreadedTaskRunner implements TaskRunner {

	private final Map<Future<?>, Task> _tasksAndFutures = Collections.synchronizedMap(new LinkedHashMap<Future<?>, Task>());
	private final ExecutorService _executorService;

	public ActivityAwareMultiThreadedTaskRunner() {
		_executorService = Executors.newFixedThreadPool(10);
	}

	@Override
	public void run(Task task, TaskListener listener) {
		Future<?> future = _executorService.submit(new TaskRunnable(task, listener));
		_tasksAndFutures.put(future, task);
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		Future<?> future = _executorService.submit(taskRunnable);
		_tasksAndFutures.put(future, taskRunnable.getTask());
	}

	@Override
	public void shutdown() {
		_executorService.shutdown();
	}

	public ExecutorService getExecutorService() {
		return _executorService;
	}

	public Set<Future<?>> getFutures() {
		return _tasksAndFutures.keySet();
	}

	public Map<Future<?>, Task> getTasksAndFutures() {
		return _tasksAndFutures;
	}

	/**
	 * Asserts that all submitted tasks have been executed
	 * 
	 * @param timeoutMillis
	 *            the amount of slack milliseconds to allow for remaining tasks
	 *            to finish
	 * @return the amount of tasks finished
	 * @throws Exception
	 *             any exceptions either thrown because of failing assertions or
	 *             because of exceptions thrown in the tasks
	 */
	public int assertAllBegunTasksFinished(int timeoutMillis) throws Exception {
		long millisBefore = System.currentTimeMillis();
		int taskCount = 0;
		for (Future<?> future : _tasksAndFutures.keySet()) {
			if (future.isDone()) {
				taskCount++;
			} else {
				long millisNow = System.currentTimeMillis();
				long millisUsed = millisNow - millisBefore;
				try {
					// using the timeout'ed get method to ensure that the future
					// will not just wait for the result to be ready. It SHOULD
					// be ready already!
					future.get(timeoutMillis - millisUsed, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					Task task = _tasksAndFutures.get(future);
					Assert.fail("Task is not finished: " + task);
				}
			}
		}
		return taskCount;
	}
}
