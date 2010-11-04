package org.eobjects.analyzer.job.tasks;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.lifecycle.AbstractBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.AnalyzerLifeCycleCallback;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleCallback;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AssignCallbacksAndInitializeTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private StorageProvider _storageProvider;
	private DataContextProvider _dataContextProvider;
	private RowAnnotationFactory _rowAnnotationFactory;
	private AbstractBeanInstance<?> _beanInstance;
	private AnalyzerBeanInstance _analyzerBeanInstance;

	// represents the default lifecycle callbacks ...
	private LifeCycleCallback _assignConfiguredCallback;
	private LifeCycleCallback _initializeCallback;
	private AnalyzerLifeCycleCallback _runCallback;
	private AnalyzerLifeCycleCallback _returnResultsCallback;
	private LifeCycleCallback _closeCallback;

	private void init(AbstractBeanInstance<?> beanInstance, StorageProvider storageProvider,
			RowAnnotationFactory rowAnnotationFactory, DataContextProvider dataContextProvider,
			LifeCycleCallback assignConfiguredCallback, LifeCycleCallback initializeCallback, LifeCycleCallback closeCallback) {
		_beanInstance = beanInstance;
		_storageProvider = storageProvider;
		_rowAnnotationFactory = rowAnnotationFactory;
		_dataContextProvider = dataContextProvider;
		_assignConfiguredCallback = assignConfiguredCallback;
		_initializeCallback = initializeCallback;
		_closeCallback = closeCallback;
	}

	public AssignCallbacksAndInitializeTask(AbstractBeanInstance<?> transformerBeanInstance,
			StorageProvider storageProvider, RowAnnotationFactory rowAnnotationFactory,
			DataContextProvider dataContextProvider, LifeCycleCallback assignConfiguredCallback,
			LifeCycleCallback initializeCallback, LifeCycleCallback closeCallback) {
		init(transformerBeanInstance, storageProvider, rowAnnotationFactory, dataContextProvider, assignConfiguredCallback,
				initializeCallback, closeCallback);
	}

	public AssignCallbacksAndInitializeTask(AnalyzerBeanInstance analyzerBeanInstance, StorageProvider storageProvider,
			RowAnnotationFactory rowAnnotationFactory, DataContextProvider dataContextProvider,
			LifeCycleCallback assignConfiguredCallback, LifeCycleCallback initializeCallback,
			AnalyzerLifeCycleCallback runCallback, AnalyzerLifeCycleCallback returnResultsCallback,
			LifeCycleCallback closeCallback) {
		init(analyzerBeanInstance, storageProvider, rowAnnotationFactory, dataContextProvider, assignConfiguredCallback,
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

		AssignProvidedCallback assignProvidedCallback = new AssignProvidedCallback(_storageProvider, _rowAnnotationFactory,
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
	}

	@Override
	public String toString() {
		return "AssignCallbacksAndInitializeTasks[beanInstance=" + _beanInstance + "]";
	}
}
