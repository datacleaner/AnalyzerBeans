package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;

public class AssignAndInitializeTask implements Task {

	private AnalyzerBeanInstance analyzerBeanInstance;
	private CompletionListener completionListener;
	private CollectionProvider collectionProvider;
	private DataContextProvider dataContextProvider;
	private LifeCycleCallback initializeCallback;
	private LifeCycleCallback returnResultsCallback;
	private LifeCycleCallback closeCallback;

	public AssignAndInitializeTask(CompletionListener completionListener,
			AnalyzerBeanInstance analyzerBeanInstance,
			CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider,
			LifeCycleCallback initializeCallback,
			LifeCycleCallback returnResultsCallback,
			LifeCycleCallback closeCallback) {
		this.analyzerBeanInstance = analyzerBeanInstance;
		this.completionListener = completionListener;
		this.collectionProvider = collectionProvider;
		this.dataContextProvider = dataContextProvider;
		this.initializeCallback = initializeCallback;
		this.returnResultsCallback = returnResultsCallback;
		this.closeCallback = closeCallback;
	}

	@Override
	public void execute() throws Exception {
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
		completionListener.onComplete();
	}
}
