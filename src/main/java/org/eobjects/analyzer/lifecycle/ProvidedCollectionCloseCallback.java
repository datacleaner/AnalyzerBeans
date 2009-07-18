package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

import com.sleepycat.collections.StoredContainer;

public class ProvidedCollectionCloseCallback implements LifeCycleCallback {

	private ProvidedCollectionHandler collectionHandler;
	private List<StoredContainer> storedContainers;
	
	public ProvidedCollectionCloseCallback(
			ProvidedCollectionHandler collectionHandler,
			List<StoredContainer> storedContainers) {
		this.collectionHandler = collectionHandler;
		this.storedContainers = storedContainers;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.CLOSE;

		for (StoredContainer container : storedContainers) {
			collectionHandler.cleanUp(container);
		}
	}

}
