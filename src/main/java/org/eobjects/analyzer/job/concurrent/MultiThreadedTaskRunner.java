/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiThreadedTaskRunner implements TaskRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ExecutorService _executorService;
	private final int _numThreads;

	public MultiThreadedTaskRunner() {
		this(30);
	}

	public MultiThreadedTaskRunner(int numThreads) {
		_numThreads = numThreads;
		BlockingQueue<Runnable> workQueue = new DoubleBlockingQueue<Runnable>(numThreads * 3);
		_executorService = new ThreadPoolExecutor(numThreads, numThreads, 60, TimeUnit.SECONDS, workQueue);
	}

	/**
	 * @return the amount of threads in the thread pool, or -1 if this
	 *         information is not available
	 */
	public int getNumThreads() {
		return _numThreads;
	}

	@Override
	public void run(final Task task, final TaskListener listener) {
		logger.debug("run({},{})", task, listener);
		_executorService.execute(new TaskRunnable(task, listener));
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		logger.debug("run({})", taskRunnable);
		_executorService.execute(taskRunnable);
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, shutting down executor service");
		_executorService.shutdown();
	}

	@Override
	protected void finalize() throws Throwable {
		shutdown();
	}
}
