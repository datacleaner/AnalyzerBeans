package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;

public class ProvidedCollectionCloseCallback implements LifeCycleCallback {

	private CollectionProvider collectionHandler;
	private List<Object> providedObjects;

	public ProvidedCollectionCloseCallback(
			CollectionProvider collectionHandler, List<Object> providedObjects) {
		this.collectionHandler = collectionHandler;
		this.providedObjects = providedObjects;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AbstractBeanDescriptor descriptor) {
		assert state == LifeCycleState.CLOSE;

		for (Object providedObject : providedObjects) {
			collectionHandler.cleanUp(providedObject);
		}
	}

}
