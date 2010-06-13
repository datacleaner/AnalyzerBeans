package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SingleThreadedConcurrencyProvider implements ConcurrencyProvider {

	@Override
	public <T> Future<T> schedule(Callable<T> callable) {
		T call;
		try {
			call = callable.call();
			return new EmptyFuture<T>(call);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
