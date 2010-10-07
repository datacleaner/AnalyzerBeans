package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerLifeCycleCallback;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AssignCallbacksAndInitializeTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private CompletionListener _completionListener;
	private CollectionProvider _collectionProvider;
	private DataContextProvider _dataContextProvider;

	// represents the default lifecycle callbacks ...
	private LifeCycleCallback _assignConfiguredCallback;
	private LifeCycleCallback _initializeCallback;
	private AnalyzerLifeCycleCallback _runCallback;
	private AnalyzerLifeCycleCallback _returnResultsCallback;
	private LifeCycleCallback _closeCallback;
	private AbstractBeanInstance<?> _beanInstance;
	private AnalyzerBeanInstance _analyzerBeanInstance;

	private void init(CompletionListener completionListener, AbstractBeanInstance<?> beanInstance,
			CollectionProvider collectionProvider, DataContextProvider dataContextProvider,
			LifeCycleCallback assignConfiguredCallback, LifeCycleCallback initializeCallback, LifeCycleCallback closeCallback) {
		_completionListener = completionListener;
		_beanInstance = beanInstance;
		_collectionProvider = collectionProvider;
		_dataContextProvider = dataContextProvider;
		_assignConfiguredCallback = assignConfiguredCallback;
		_initializeCallback = initializeCallback;
		_closeCallback = closeCallback;
	}

	public AssignCallbacksAndInitializeTask(CompletionListener completionListener,
			AbstractBeanInstance<?> transformerBeanInstance, CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider, LifeCycleCallback assignConfiguredCallback,
			LifeCycleCallback initializeCallback, LifeCycleCallback closeCallback) {
		init(completionListener, transformerBeanInstance, collectionProvider, dataContextProvider, assignConfiguredCallback,
				initializeCallback, closeCallback);
	}

	public AssignCallbacksAndInitializeTask(CompletionListener completionListener,
			AnalyzerBeanInstance analyzerBeanInstance, CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider, LifeCycleCallback assignConfiguredCallback,
			LifeCycleCallback initializeCallback, AnalyzerLifeCycleCallback runCallback,
			AnalyzerLifeCycleCallback returnResultsCallback, LifeCycleCallback closeCallback) {
		init(completionListener, analyzerBeanInstance, collectionProvider, dataContextProvider, assignConfiguredCallback,
				initializeCallback, closeCallback);
		_analyzerBeanInstance = analyzerBeanInstance;
		_runCallback = runCallback;
		_returnResultsCallback = returnResultsCallback;
	}

	@Override
	public void execute() throws Exception {
		logger.debug("execute()");

		if (_assignConfiguredCallback != null) {
			_beanInstance.getAssignConfiguredCallbacks().add(_assignConfiguredCallback);
		}

		AssignProvidedCallback assignProvidedCallback = new AssignProvidedCallback(_beanInstance, _collectionProvider,
				_dataContextProvider);
		_beanInstance.getAssignProvidedCallbacks().add(assignProvidedCallback);

		if (_initializeCallback != null) {
			_beanInstance.getInitializeCallbacks().add(_initializeCallback);
		}

		if (_runCallback != null) {
			_analyzerBeanInstance.getRunCallbacks().add(_runCallback);
		}

		if (_returnResultsCallback != null) {
			_analyzerBeanInstance.getReturnResultsCallbacks().add(_returnResultsCallback);
		}

		if (_closeCallback != null) {
			_beanInstance.getCloseCallbacks().add(_closeCallback);
		}

		logger.debug("calling assignConfigured() on bean: {}", _beanInstance);
		_beanInstance.assignConfigured();

		logger.debug("calling assignProvided() on bean: {}", _beanInstance);
		_beanInstance.assignProvided();

		logger.debug("calling initialize() on bean: {}", _beanInstance);
		_beanInstance.initialize();

		logger.debug("invoking completion listener: {}", _completionListener);
		_completionListener.onComplete();
	}

	@Override
	public String toString() {
		return "AssignCallbacksAndInitializeTasks[beanInstance=" + _beanInstance + "]";
	}
}
