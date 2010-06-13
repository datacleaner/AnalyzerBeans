package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ConcurrencyProvider {

	public <T> Future<T> schedule(Callable<T> callable);
}
