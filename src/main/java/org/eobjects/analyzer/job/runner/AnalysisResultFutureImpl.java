package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.concurrent.JobTaskListener;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisResultFutureImpl implements AnalysisResultFuture {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisResultFutureImpl.class);

	private Queue<AnalyzerResult> _resultQueue = new LinkedBlockingQueue<AnalyzerResult>();
	private JobTaskListener _jobTaskListener;
	private ErrorAware _errorAware;

	public AnalysisResultFutureImpl(Queue<AnalyzerResult> resultQueue, JobTaskListener jobCompletionListener,
			ErrorAware errorAware) {
		_resultQueue = resultQueue;
		_jobTaskListener = jobCompletionListener;
		_errorAware = errorAware;
	}

	@Override
	public boolean isDone() {
		return _jobTaskListener.isDone();
	}

	@Override
	public void await(long timeout, TimeUnit timeUnit) {
		if (!isDone()) {
			try {
				logger.debug("_closeCompletionListener.await({},{})", timeout, timeUnit);
				_jobTaskListener.await(timeout, timeUnit);
			} catch (InterruptedException e) {
				logger.error("Unexpected error while retreiving results", e);
			}
		}
	}

	@Override
	public void await() {
		while (!isDone()) {
			try {
				logger.debug("_closeCompletionListener.await()");
				_jobTaskListener.await();
			} catch (Exception e) {
				logger.error("Unexpected error while retreiving results", e);
			}
		}
	}

	@Override
	public List<AnalyzerResult> getResults() {
		await();
		return new ArrayList<AnalyzerResult>(_resultQueue);
	}

	@Override
	public boolean isSuccessful() {
		await();
		return !_errorAware.isErrornous();
	}

	@Override
	public List<Throwable> getErrors() {
		return _errorAware.getErrors();
	}

	@Override
	public boolean isErrornous() {
		return !isSuccessful();
	}

	@Override
	public JobStatus getStatus() {
		if (isDone()) {
			if (isSuccessful()) {
				return JobStatus.SUCCESSFUL;
			}
			return JobStatus.ERRORNOUS;
		}
		if (!_errorAware.isErrornous()) {
			return JobStatus.NOT_FINISHED;
		}
		return JobStatus.ERRORNOUS;
	}
}
