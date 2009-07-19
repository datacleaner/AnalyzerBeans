package org.eobjects.analyzer.job;

import java.util.Queue;

public interface RunnableConsumer {

	public void execute(Queue<? extends Runnable> runnables);
}
