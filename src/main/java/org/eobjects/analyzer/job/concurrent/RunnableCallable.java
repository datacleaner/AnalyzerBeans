package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.Callable;

public class RunnableCallable implements Callable<Object> {

	private Runnable _runnable;

	public RunnableCallable(Runnable runnable) {
		_runnable = runnable;
	}
	
	@Override
	public Object call() throws Exception {
		_runnable.run();
		return Boolean.TRUE;
	}

}
