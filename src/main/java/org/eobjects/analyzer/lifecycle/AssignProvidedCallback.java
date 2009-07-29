package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedDescriptor;
import org.eobjects.analyzer.job.DataContextProvider;

public class AssignProvidedCallback implements LifeCycleCallback {

	private AnalyzerBeanInstance analyzerBeanInstance;
	private ProvidedCollectionHandler collectionHandler;
	private DataContextProvider dataContextProvider;

	public AssignProvidedCallback(AnalyzerBeanInstance analyzerBeanInstance,
			ProvidedCollectionHandler collectionProvider,
			DataContextProvider dataContextProvider) {
		this.analyzerBeanInstance = analyzerBeanInstance;
		this.collectionHandler = collectionProvider;
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<Object> providedCollections = new LinkedList<Object>();
		List<ProvidedDescriptor> providedDescriptors = descriptor
				.getProvidedDescriptors();
		for (ProvidedDescriptor providedDescriptor : providedDescriptors) {
			if (providedDescriptor.isList()) {
				List<?> list = collectionHandler.createList(providedDescriptor
						.getTypeArgument(0));
				providedDescriptor.assignValue(analyzerBean, list);
				providedCollections.add(list);
			} else if (providedDescriptor.isMap()) {
				Map<?, ?> map = collectionHandler.createMap(providedDescriptor
						.getTypeArgument(0), providedDescriptor
						.getTypeArgument(1));
				providedDescriptor.assignValue(analyzerBean, map);
				providedCollections.add(map);
			} else if (providedDescriptor.isDataContext()) {
				providedDescriptor.assignValue(analyzerBean,
						dataContextProvider.getDataContext());
			} else if (providedDescriptor.isSchemaNavigator()) {
				providedDescriptor.assignValue(analyzerBean,
						dataContextProvider.getSchemaNavigator());
			}
		}

		if (!providedCollections.isEmpty()) {
			// Add a callback for cleaning up the provided collections
			analyzerBeanInstance.getCloseCallbacks().add(
					new ProvidedCollectionCloseCallback(collectionHandler,
							providedCollections));
		}
	}

}
