package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;

public class EmptyCallable<V> implements Callable<V> {
	
	private V value;

	public EmptyCallable(V value) {
		this.value = value;
	}

	@Override
	public V call() throws Exception {
		return value;
	}

}
