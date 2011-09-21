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
public final class JobCompletionTaskListener implements StatusAwareTaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ForkTaskListener.class);

	private final CountDownLatch _countDownLatch;
	private final AnalysisJob _job;
	private final AnalysisListener _analysisListener;

	public JobCompletionTaskListener(AnalysisJob job, AnalysisListener analysisListener, int callablesToWaitFor) {
		_job = job;
		_analysisListener = analysisListener;
		_countDownLatch = new CountDownLatch(callablesToWaitFor);
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
