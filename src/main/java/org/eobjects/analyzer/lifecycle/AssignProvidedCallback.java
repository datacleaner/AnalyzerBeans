package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;

public final class AssignProvidedCallback implements LifeCycleCallback {

	private final CollectionProvider collectionProvider;
	private final DataContextProvider dataContextProvider;
	private final AbstractBeanInstance<?> beanInstance;

	public AssignProvidedCallback(AbstractBeanInstance<?> beanInstance, CollectionProvider collectionProvider,
			DataContextProvider dataContextProvider) {
		this.beanInstance = beanInstance;
		this.collectionProvider = collectionProvider;
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean, BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<Object> providedCollections = new LinkedList<Object>();
		Set<ProvidedPropertyDescriptor> providedDescriptors = descriptor.getProvidedProperties();
		for (ProvidedPropertyDescriptor providedDescriptor : providedDescriptors) {
			Class<?> clazz1 = (Class<?>) providedDescriptor.getTypeArgument(0);
			if (providedDescriptor.isList()) {
				List<?> list = collectionProvider.createList(clazz1);
				providedDescriptor.setValue(analyzerBean, list);
				providedCollections.add(list);
			} else if (providedDescriptor.isSet()) {
				Set<?> set = collectionProvider.createSet(clazz1);
				providedDescriptor.setValue(analyzerBean, set);
				providedCollections.add(set);
			} else if (providedDescriptor.isMap()) {
				Class<?> clazz2 = (Class<?>) providedDescriptor.getTypeArgument(1);
				Map<?, ?> map = collectionProvider.createMap(clazz1, clazz2);
				providedDescriptor.setValue(analyzerBean, map);
				providedCollections.add(map);
			} else if (providedDescriptor.isDataContext()) {
				providedDescriptor.setValue(analyzerBean, dataContextProvider.getDataContext());
			} else if (providedDescriptor.isSchemaNavigator()) {
				providedDescriptor.setValue(analyzerBean, dataContextProvider.getSchemaNavigator());
			}
		}

		if (!providedCollections.isEmpty()) {
			// Add a callback for cleaning up the provided collections
			beanInstance.getCloseCallbacks().add(
					new ProvidedCollectionCloseCallback(collectionProvider, providedCollections));
		}
	}

}
