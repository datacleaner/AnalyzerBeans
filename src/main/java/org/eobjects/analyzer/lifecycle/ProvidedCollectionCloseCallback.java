package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class ProvidedCollectionCloseCallback implements LifeCycleCallback {

	private ProvidedCollectionHandler collectionHandler;
	private List<Object> providedObjects;

	public ProvidedCollectionCloseCallback(
			ProvidedCollectionHandler collectionHandler,
			List<Object> providedObjects) {
		this.collectionHandler = collectionHandler;
		this.providedObjects = providedObjects;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.CLOSE;

		for (Object providedObject : providedObjects) {
			collectionHandler.cleanUp(providedObject);
		}
	}

}
