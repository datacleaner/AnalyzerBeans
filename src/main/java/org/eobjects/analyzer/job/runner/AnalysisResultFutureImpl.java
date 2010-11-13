/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.concurrent.JobTaskListener;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisResultFutureImpl implements AnalysisResultFuture {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisResultFutureImpl.class);

	private final Queue<AnalyzerJobResult> _resultQueue;
	private final ErrorAware _errorAware;
	private volatile JobTaskListener _jobTaskListener;
	private volatile boolean _done;

	public AnalysisResultFutureImpl(Queue<AnalyzerJobResult> resultQueue, JobTaskListener jobCompletionListener,
			ErrorAware errorAware) {
		_resultQueue = resultQueue;
		_jobTaskListener = jobCompletionListener;
		_errorAware = errorAware;
		_done = false;
	}

	@Override
	public boolean isDone() {
		if (!_done) {
			_done = _jobTaskListener.isDone();
		}
		return _done;
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
	public List<AnalyzerResult> getResults() throws IllegalStateException {
		await();
		if (isErrornous()) {
			throwError();
		}
		ArrayList<AnalyzerJobResult> resultQueueCopy = new ArrayList<AnalyzerJobResult>(_resultQueue);
		ArrayList<AnalyzerResult> result = new ArrayList<AnalyzerResult>(resultQueueCopy.size());
		for (AnalyzerJobResult jobResult : resultQueueCopy) {
			result.add(jobResult.getResult());
		}
		return result;
	}

	@Override
	public AnalyzerResult getResult(AnalyzerJob analyzerJob) throws IllegalStateException {
		await();
		if (isErrornous()) {
			throwError();
		}
		ArrayList<AnalyzerJobResult> resultQueueCopy = new ArrayList<AnalyzerJobResult>(_resultQueue);
		for (AnalyzerJobResult jobResult : resultQueueCopy) {
			if (jobResult.getJob().equals(analyzerJob)) {
				return jobResult.getResult();
			}
		}
		return null;
	}

	@Override
	public Map<AnalyzerJob, AnalyzerResult> getResultMap() throws IllegalStateException {
		await();
		if (isErrornous()) {
			throwError();
		}
		ArrayList<AnalyzerJobResult> resultQueueCopy = new ArrayList<AnalyzerJobResult>(_resultQueue);
		Map<AnalyzerJob, AnalyzerResult> result = new HashMap<AnalyzerJob, AnalyzerResult>();
		for (AnalyzerJobResult jobResult : resultQueueCopy) {
			AnalyzerJob job = jobResult.getJob();
			AnalyzerResult analyzerResult = jobResult.getResult();
			result.put(job, analyzerResult);
		}
		return result;
	}

	private void throwError() throws IllegalStateException {
		StringBuilder sb = new StringBuilder();
		List<Throwable> errors = getErrors();
		for (Throwable throwable : errors) {
			if (sb.length() == 0) {
				sb.append("The analysis ended with " + errors.size() + " errors: [");
			} else {
				sb.append(",");
			}
			String className = throwable.getClass().getSimpleName();
			sb.append(className);
			sb.append(":");
			String message = throwable.getMessage();
			sb.append(message);
		}
		sb.append("[");
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
