package org.eobjects.analyzer.job.tasks;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.job.concurrent.CompletionListener;

public final class ConsumeRowTaskCompletionListener implements CompletionListener {

	private final AtomicInteger _count = new AtomicInteger(0);

	@Override
	public void onComplete() {
		_count.incrementAndGet();
		synchronized (this) {
			notifyAll();
		}
	}

	public void awaitCount(final int count) {
		while (_count.get() < count) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

}
