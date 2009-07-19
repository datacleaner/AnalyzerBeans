package org.eobjects.analyzer.job;

import java.util.Queue;

public class SingleThreadedRunnableConsumer implements RunnableConsumer {

	@Override
	public void execute(Queue<? extends Runnable> runnables) {
		for (Runnable r = runnables.poll(); r != null; r = runnables.poll()) {
			r.run();
		}
	}
}
