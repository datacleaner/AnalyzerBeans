package org.eobjects.analyzer.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionFactoryImpl implements CollectionFactory {

	private final StorageProvider _storageProvider;
	private final List<Object> _collections = new ArrayList<Object>();

	public CollectionFactoryImpl(StorageProvider storageProvider) {
		super();
		_storageProvider = storageProvider;
	}

	@Override
	public <E> List<E> createList(Class<E> elementClass) {
		List<E> list = _storageProvider.createList(elementClass);
		_collections.add(list);
		return list;
	}

	@Override
	public <E> Set<E> createSet(Class<E> elementClass) {
		Set<E> set = _storageProvider.createSet(elementClass);
		_collections.add(set);
		return set;
	}

	@Override
	public <K, V> Map<K, V> createSet(Class<K> keyClass, Class<V> valueClass) {
		Map<K, V> map = _storageProvider.createMap(keyClass, valueClass);
		_collections.add(map);
		return map;
	}
}
