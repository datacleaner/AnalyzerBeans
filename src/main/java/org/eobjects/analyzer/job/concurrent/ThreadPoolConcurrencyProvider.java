package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolConcurrencyProvider implements ConcurrencyProvider {

	private ExecutorService threadPool;

	public ThreadPoolConcurrencyProvider() {
		threadPool = Executors.newCachedThreadPool();
	}
	
	public ThreadPoolConcurrencyProvider(int numThreads) {
		threadPool = Executors.newFixedThreadPool(numThreads);
	}

	@Override
	public <T> Future<T> exec(Callable<T> callable) {
		return threadPool.submit(callable);
	}
	
	@Override
	public void exec(Runnable runnable) {
		threadPool.submit(runnable);
	}

}
