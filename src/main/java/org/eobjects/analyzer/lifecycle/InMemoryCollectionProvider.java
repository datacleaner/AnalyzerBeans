package org.eobjects.analyzer.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InMemoryCollectionProvider implements CollectionProvider {

	@Override
	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException {
		return new ArrayList<E>();
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException {
		return new HashMap<K, V>();
	}

	@Override
	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException {
		return new HashSet<E>();
	}

	@Override
	public void cleanUp(Object providedObj) {
	}

}
