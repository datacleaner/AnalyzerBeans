package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestedCompletionListener implements CompletionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleTasksCompletionListener.class);

	private String _name;
	private AtomicInteger _countDown;
	private CompletionListener _nestedCompletionListener;

	public NestedCompletionListener(String name, int tasksToWaitFor,
			CompletionListener nestedCompletionListener) {
		_name = name;
		_countDown = new AtomicInteger(tasksToWaitFor);
		_nestedCompletionListener = nestedCompletionListener;

		if (tasksToWaitFor == 0) {
			// immediate completion
			_countDown = new AtomicInteger(1);
			onComplete();
		}
	}

	@Override
	public void onComplete() {
		int count = _countDown.decrementAndGet();
		logger.debug("onComplete(), count = {}", count);
		if (count == 0) {
			logger.info(
					"Calling onComplete() on nested CompletionListener ({})",
					_name);
			_nestedCompletionListener.onComplete();
		}
	}
}
