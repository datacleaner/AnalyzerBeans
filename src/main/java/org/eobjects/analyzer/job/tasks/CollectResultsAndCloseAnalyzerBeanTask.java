package org.eobjects.analyzer.job.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectResultsAndCloseAnalyzerBeanTask implements Callable<Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(CollectResultsAndCloseAnalyzerBeanTask.class);

	private AnalyzerBeanInstance analyzerBeanInstance;
	private CountDownLatch runCount;
	private CountDownLatch collectResultsCount;

	public CollectResultsAndCloseAnalyzerBeanTask(
			CountDownLatch countDownToWaitFor, CountDownLatch countDownToCount,
			AnalyzerBeanInstance analyzerBeanInstance) {
		this.runCount = countDownToWaitFor;
		this.collectResultsCount = countDownToCount;
		this.analyzerBeanInstance = analyzerBeanInstance;
	}

	@Override
	public Object call() throws Exception {
		logger.debug("runCount.await()");
		runCount.await();
		analyzerBeanInstance.returnResults();
		collectResultsCount.countDown();
		logger.info("collectResultsCount.countDown() returned count="
				+ collectResultsCount.getCount());

		// close can occur AFTER counting down the countDownLatch
		analyzerBeanInstance.close();
		return Boolean.TRUE;
	}

}
