package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion listener for a full AnalysisJob. Use the isDone() method to ask
 * whether or not the job is finished.
 * 
 * @author Kasper Sørensen
 */
public final class JobCompletionListenerImpl implements JobCompletionListener {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTasksCompletionListener.class);

	private final CountDownLatch _countDownLatch;
	private final AnalysisJob _job;
	private final AnalysisListener[] _analysisListeners;

	public JobCompletionListenerImpl(AnalysisJob job, AnalysisListener[] analysisListeners, int callablesToWaitFor) {
		_job = job;
		_analysisListeners = analysisListeners;
		_countDownLatch = new CountDownLatch(callablesToWaitFor);
	}

	@Override
	public void onComplete() {
		logger.debug("onComplete()");
		_countDownLatch.countDown();
		if (_countDownLatch.getCount() == 0) {
			if (_analysisListeners != null) {
				for (AnalysisListener listener : _analysisListeners) {
					listener.jobSuccess(_job);
				}
			}
		}
	}

	public void cancel() {
		logger.warn("Cancelling job: {}", _job);
		while (_countDownLatch.getCount() > 0) {
			_countDownLatch.countDown();
		}
	}

	public void await() throws InterruptedException {
		_countDownLatch.await();
	}

	public boolean isDone() {
		return _countDownLatch.getCount() == 0;
	}

	@Override
	public void await(long timeout, TimeUnit timeUnit) throws InterruptedException {
		_countDownLatch.await(timeout, timeUnit);
	}
}
