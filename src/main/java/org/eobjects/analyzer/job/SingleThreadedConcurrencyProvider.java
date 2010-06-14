package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SingleThreadedConcurrencyProvider implements ConcurrencyProvider {

	@Override
	public <T> Future<T> exec(Callable<T> callable) {
		T call;
		try {
			call = callable.call();
			return new EmptyFuture<T>(call);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void exec(Runnable runnable) {
		runnable.run();
	}
}
