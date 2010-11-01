package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;
import org.eobjects.analyzer.storage.CollectionFactoryImpl;
import org.eobjects.analyzer.storage.StorageProvider;

public final class AssignProvidedCallback implements LifeCycleCallback {

	private final StorageProvider _storageProvider;
	private final DataContextProvider _dataContextProvider;

	public AssignProvidedCallback(StorageProvider storageProvider, DataContextProvider dataContextProvider) {
		_storageProvider = storageProvider;
		_dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean, BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<Object> providedCollections = new LinkedList<Object>();
		Set<ProvidedPropertyDescriptor> providedDescriptors = descriptor.getProvidedProperties();
		for (ProvidedPropertyDescriptor providedDescriptor : providedDescriptors) {
			if (providedDescriptor.isCollectionFactory()) {
				CollectionFactoryImpl factory = new CollectionFactoryImpl(_storageProvider);
				providedDescriptor.setValue(analyzerBean, factory);
			} else if (providedDescriptor.isDataContext()) {
				providedDescriptor.setValue(analyzerBean, _dataContextProvider.getDataContext());
			} else if (providedDescriptor.isSchemaNavigator()) {
				providedDescriptor.setValue(analyzerBean, _dataContextProvider.getSchemaNavigator());
			} else {
				Class<?> clazz1 = (Class<?>) providedDescriptor.getTypeArgument(0);
				if (providedDescriptor.isList()) {
					List<?> list = _storageProvider.createList(clazz1);
					providedDescriptor.setValue(analyzerBean, list);
					providedCollections.add(list);
				} else if (providedDescriptor.isSet()) {
					Set<?> set = _storageProvider.createSet(clazz1);
					providedDescriptor.setValue(analyzerBean, set);
					providedCollections.add(set);
				} else if (providedDescriptor.isMap()) {
					Class<?> clazz2 = (Class<?>) providedDescriptor.getTypeArgument(1);
					Map<?, ?> map = _storageProvider.createMap(clazz1, clazz2);
					providedDescriptor.setValue(analyzerBean, map);
					providedCollections.add(map);
				}
			}
		}
	}
}
