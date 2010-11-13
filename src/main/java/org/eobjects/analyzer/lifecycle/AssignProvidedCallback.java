/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;
import org.eobjects.analyzer.storage.CollectionFactoryImpl;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.storage.StorageProvider;

public final class AssignProvidedCallback implements LifeCycleCallback {

	private final StorageProvider _storageProvider;
	private final DataContextProvider _dataContextProvider;
	private final RowAnnotationFactory _rowAnnotationFactory;

	public AssignProvidedCallback(StorageProvider storageProvider, RowAnnotationFactory rowAnnotationFactory,
			DataContextProvider dataContextProvider) {
		_storageProvider = storageProvider;
		_rowAnnotationFactory = rowAnnotationFactory;
		_dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object bean, BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.ASSIGN_PROVIDED;

		List<Object> providedCollections = new LinkedList<Object>();
		Set<ProvidedPropertyDescriptor> providedDescriptors = descriptor.getProvidedProperties();
		for (ProvidedPropertyDescriptor providedDescriptor : providedDescriptors) {
			if (providedDescriptor.isCollectionFactory()) {
				CollectionFactoryImpl factory = new CollectionFactoryImpl(_storageProvider);
				providedDescriptor.setValue(bean, factory);
			} else if (providedDescriptor.isRowAnnotationFactory()) {
				providedDescriptor.setValue(bean, _rowAnnotationFactory);
			} else if (providedDescriptor.isDataContext()) {
				providedDescriptor.setValue(bean, _dataContextProvider.getDataContext());
			} else if (providedDescriptor.isSchemaNavigator()) {
				providedDescriptor.setValue(bean, _dataContextProvider.getSchemaNavigator());
			} else {
				Class<?> clazz1 = (Class<?>) providedDescriptor.getTypeArgument(0);
				if (providedDescriptor.isList()) {
					List<?> list = _storageProvider.createList(clazz1);
					providedDescriptor.setValue(bean, list);
					providedCollections.add(list);
				} else if (providedDescriptor.isSet()) {
					Set<?> set = _storageProvider.createSet(clazz1);
					providedDescriptor.setValue(bean, set);
					providedCollections.add(set);
				} else if (providedDescriptor.isMap()) {
					Class<?> clazz2 = (Class<?>) providedDescriptor.getTypeArgument(1);
					Map<?, ?> map = _storageProvider.createMap(clazz1, clazz2);
					providedDescriptor.setValue(bean, map);
					providedCollections.add(map);
				}
			}
		}
	}
}
