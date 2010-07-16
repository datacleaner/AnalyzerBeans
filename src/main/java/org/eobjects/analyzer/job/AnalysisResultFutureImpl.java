package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.concurrent.WaitableCompletionListener;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisResultFutureImpl implements AnalysisResultFuture {

	private static final Logger logger = LoggerFactory
			.getLogger(AnalysisResultFutureImpl.class);

	private Queue<AnalyzerResult> _resultQueue = new LinkedBlockingQueue<AnalyzerResult>();
	private WaitableCompletionListener _closeCompletionListener;
	
	public AnalysisResultFutureImpl(Queue<AnalyzerResult> resultQueue,
			WaitableCompletionListener closeCompletionListener) {
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
				logger.debug("_closeCompletionListener.await({},{})", timeout,
						timeUnit);
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
}
