package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedDescriptor;

import com.sleepycat.collections.StoredContainer;

public class AssignProvidedCallback implements LifeCycleCallback {

	private AnalyzerBeanInstance analyzerBeanInstance;
	private ProvidedCollectionHandler collectionHandler;

	public AssignProvidedCallback(AnalyzerBeanInstance analyzerBeanInstance,
			ProvidedCollectionHandler collectionProvider) {
		this.analyzerBeanInstance = analyzerBeanInstance;
		this.collectionHandler = collectionProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<StoredContainer> storedContainers = new LinkedList<StoredContainer>();
		List<ProvidedDescriptor> providedDescriptors = descriptor
				.getProvidedDescriptors();
		for (ProvidedDescriptor providedDescriptor : providedDescriptors) {
			StoredContainer col = collectionHandler
					.createProvidedCollection(providedDescriptor);
			providedDescriptor.assignValue(analyzerBean, col);
			storedContainers.add(col);
		}

		// Add a callback for cleaning up the provided collections
		analyzerBeanInstance.getCloseCallbacks().add(
				new ProvidedCollectionCloseCallback(collectionHandler,
						storedContainers));
	}

}
