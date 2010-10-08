package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.TimeUnit;

public interface JobCompletionListener extends CompletionListener {

	public void cancel();

	public boolean isDone();

	public void await() throws InterruptedException;

	public void await(long timeout, TimeUnit timeUnit) throws InterruptedException;
}
