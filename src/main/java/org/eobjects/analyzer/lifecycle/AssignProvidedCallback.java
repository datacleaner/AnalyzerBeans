package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedDescriptor;

public class AssignProvidedCallback implements LifeCycleCallback {

	private CollectionProvider collectionProvider;
	private DataContextProvider dataContextProvider;
	private AbstractBeanInstance beanInstance;

	public AssignProvidedCallback(AbstractBeanInstance beanInstance,
			CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider) {
		this.beanInstance = beanInstance;
		this.collectionProvider = collectionProvider;
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AbstractBeanDescriptor descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<Object> providedCollections = new LinkedList<Object>();
		List<ProvidedDescriptor> providedDescriptors = descriptor
				.getProvidedDescriptors();
		for (ProvidedDescriptor providedDescriptor : providedDescriptors) {
			if (providedDescriptor.isList()) {
				List<?> list = collectionProvider.createList(providedDescriptor
						.getTypeArgument(0));
				providedDescriptor.assignValue(analyzerBean, list);
				providedCollections.add(list);
			} else if (providedDescriptor.isMap()) {
				Map<?, ?> map = collectionProvider.createMap(
						providedDescriptor.getTypeArgument(0),
						providedDescriptor.getTypeArgument(1));
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
			beanInstance.getCloseCallbacks().add(
					new ProvidedCollectionCloseCallback(collectionProvider,
							providedCollections));
		}
	}

}
