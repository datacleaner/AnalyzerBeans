package org.eobjects.analyzer.job;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignAndInitializeTask implements Callable<Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(AssignAndInitializeTask.class);

	private AnalyzerBeanInstance analyzerBeanInstance;
	private CountDownLatch initializeCount;
	private CollectionProvider collectionProvider;
	private DataContextProvider dataContextProvider;
	private LifeCycleCallback initializeCallback;
	private LifeCycleCallback returnResultsCallback;
	private LifeCycleCallback closeCallback;

	public AssignAndInitializeTask(CountDownLatch countDownLatch,
			AnalyzerBeanInstance analyzerBeanInstance,
			CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider,
			LifeCycleCallback initializeCallback,
			LifeCycleCallback returnResultsCallback,
			LifeCycleCallback closeCallback) {
		this.analyzerBeanInstance = analyzerBeanInstance;
		this.initializeCount = countDownLatch;
		this.collectionProvider = collectionProvider;
		this.dataContextProvider = dataContextProvider;
		this.initializeCallback = initializeCallback;
		this.returnResultsCallback = returnResultsCallback;
		this.closeCallback = closeCallback;
	}

	@Override
	public Object call() throws Exception {
		AssignProvidedCallback assignProvidedCallback = new AssignProvidedCallback(
				analyzerBeanInstance, collectionProvider, dataContextProvider);

		analyzerBeanInstance.getAssignProvidedCallbacks().add(
				assignProvidedCallback);
		analyzerBeanInstance.getInitializeCallbacks().add(initializeCallback);
		analyzerBeanInstance.getReturnResultsCallbacks().add(
				returnResultsCallback);
		analyzerBeanInstance.getCloseCallbacks().add(closeCallback);

		analyzerBeanInstance.assignConfigured();
		analyzerBeanInstance.assignProvided();
		analyzerBeanInstance.initialize();
		initializeCount.countDown();
		logger.info("initializeCount.countDown() returned count="
				+ initializeCount.getCount());
		return Boolean.TRUE;
	}
}
