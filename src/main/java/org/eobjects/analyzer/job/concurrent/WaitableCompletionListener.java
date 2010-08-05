package org.eobjects.analyzer.job.concurrent;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitableCompletionListener implements CompletionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleTasksCompletionListener.class);

	private final CountDownLatch _countDownLatch;

	public WaitableCompletionListener(CountDownLatch countDownLatch) {
		_countDownLatch = countDownLatch;
	}

	public WaitableCompletionListener(int callablesToWaitFor) {
		this(new CountDownLatch(callablesToWaitFor));
	}

	public WaitableCompletionListener(Collection<Callable<?>> callablesToWaitFor) {
		this(callablesToWaitFor.size());
	}

	@Override
	public void onComplete() {
		logger.debug("onComplete()");
		_countDownLatch.countDown();
	}

	public void await() throws InterruptedException {
		_countDownLatch.await();
	}

	public void await(long timeout, TimeUnit unit) throws InterruptedException {
		_countDownLatch.await(timeout, unit);
	}

	public boolean isDone() {
		return _countDownLatch.getCount() == 0;
	}
}
