package org.eobjects.analyzer.job;

import java.util.Queue;

public class MultiThreadedRunnableConsumer implements RunnableConsumer {

	private int maxThreads;

	public MultiThreadedRunnableConsumer(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	@Override
	public void execute(Queue<? extends Runnable> runnables) {
		int threadCount = maxThreads;
		if (runnables.size() < maxThreads) {
			threadCount = runnables.size();
		}

		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new QueueConsumingThread(runnables);
			threads[i].start();
		}
	}

	class QueueConsumingThread extends Thread {
		private Queue<? extends Runnable> runnables;

		public QueueConsumingThread(Queue<? extends Runnable> runnables) {
			this.runnables = runnables;
		}

		@Override
		public void run() {
			Runnable r = poll();
			while (r != null) {
				r.run();
				r = poll();
			}
		}

		private Runnable poll() {
			synchronized (runnables) {
				return runnables.poll();
			}
		}
	}
}
