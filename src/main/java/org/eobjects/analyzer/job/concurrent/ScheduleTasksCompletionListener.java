package org.eobjects.analyzer.job.concurrent;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleTasksCompletionListener implements CompletionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleTasksCompletionListener.class);

	private AtomicInteger _countDown;
	private ConcurrencyProvider _concurrencyProvider;
	private Collection<? extends Callable<?>> _callables;

	public ScheduleTasksCompletionListener(
			ConcurrencyProvider concurrencyProvider, int callablesToWaitFor,
			Collection<? extends Callable<?>> callablesToSchedule) {
		_concurrencyProvider = concurrencyProvider;
		_callables = callablesToSchedule;
		_countDown = new AtomicInteger(callablesToWaitFor);
	}

	@Override
	public void onComplete() {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete(), count = {}", count);
		if (count == 0) {
			logger.info("Scheduling {} callables", _callables.size());
			for (Callable<?> callable : _callables) {
				_concurrencyProvider.exec(callable);
			}
		}
	}

}
