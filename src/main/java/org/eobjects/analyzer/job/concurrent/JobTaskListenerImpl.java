package org.eobjects.analyzer.job.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completion listener for a full AnalysisJob. Use the isDone() method to ask
 * whether or not the job is finished.
 * 
 * @author Kasper Sørensen
 */
public final class JobTaskListenerImpl implements JobTaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTasksTaskListener.class);

	private final CountDownLatch _countDownLatch;
	private final AnalysisJob _job;
	private final AnalysisListener _analysisListener;

	public JobTaskListenerImpl(AnalysisJob job, AnalysisListener analysisListener, int callablesToWaitFor) {
		_job = job;
		_analysisListener = analysisListener;
		_countDownLatch = new CountDownLatch(callablesToWaitFor);
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

	@Override
	public void onBegin(Task task) {
	}

	@Override
	public void onComplete(Task task) {
		logger.debug("onComplete(...)");
		_countDownLatch.countDown();
		if (_countDownLatch.getCount() == 0) {
			_analysisListener.jobSuccess(_job);
		}
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		logger.debug("onError(...)");
		_analysisListener.errorUknown(_job, throwable);
		_countDownLatch.countDown();
	}
}
