package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.concurrent.JobCompletionListener;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisResultFutureImpl implements AnalysisResultFuture {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisResultFutureImpl.class);

	private Queue<AnalyzerResult> _resultQueue = new LinkedBlockingQueue<AnalyzerResult>();
	private JobCompletionListener _closeCompletionListener;
	private List<Throwable> _errors;

	public AnalysisResultFutureImpl(Queue<AnalyzerResult> resultQueue, JobCompletionListener closeCompletionListener) {
		super();
		_resultQueue = resultQueue;
		_closeCompletionListener = closeCompletionListener;
	}

	@Override
	public boolean isDone() {
		return _closeCompletionListener.isDone();
	}

	@Override
	public void await(long timeout, TimeUnit timeUnit) {
		if (!isDone()) {
			try {
				logger.debug("_closeCompletionListener.await({},{})", timeout, timeUnit);
				_closeCompletionListener.await(timeout, timeUnit);
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
				_closeCompletionListener.await();
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
		return getErrors() == null;
	}

	public void addError(Throwable error) {
		if (_errors == null) {
			synchronized (this) {
				if (_errors == null) {
					_errors = Collections.synchronizedList(new LinkedList<Throwable>());
				}
			}
		}
		_errors.add(error);
	}

	@Override
	public List<Throwable> getErrors() {
		if (_errors == null) {
			return null;
		}
		return Collections.unmodifiableList(_errors);
	}

	@Override
	public JobStatus getStatus() {
		if (isDone()) {
			if (isSuccessful()) {
				return JobStatus.SUCCESSFUL;
			}
			return JobStatus.ERRORNOUS;
		}
		if (_errors.isEmpty()) {
			return JobStatus.NOT_FINISHED;
		}
		return JobStatus.ERRORNOUS;
	}
}
