package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ConcurrencyProvider {

	public <T> Future<T> exec(Callable<T> callable);
	
	public void exec(Runnable runnable);
}
