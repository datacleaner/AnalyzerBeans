package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.TimeUnit;

public interface JobTaskListener extends TaskListener {

	public boolean isDone();

	public void await() throws InterruptedException;

	public void await(long timeout, TimeUnit timeUnit) throws InterruptedException;
}
