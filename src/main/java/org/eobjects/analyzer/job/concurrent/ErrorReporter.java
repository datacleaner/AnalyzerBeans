package org.eobjects.analyzer.job.concurrent;

public interface ErrorReporter {

	public void reportError(Throwable throwable);
}
