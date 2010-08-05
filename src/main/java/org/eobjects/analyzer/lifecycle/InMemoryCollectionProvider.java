package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryCollectionProvider implements CollectionProvider {

	@Override
	public <E> List<E> createList(Type valueType)
			throws IllegalStateException {
		return new ArrayList<E>();
	}

	@Override
	public <K, V> Map<K, V> createMap(Type keyType, Type valueType)
			throws IllegalStateException {
		return new HashMap<K, V>();
	}

	@Override
	public void cleanUp(Object providedObj) {
	}

}
