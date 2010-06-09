package org.eobjects.analyzer.lifecycle;

import java.util.List;
import java.util.Map;

public interface CollectionProvider {

	public <E> List<E> createList(Class<E> valueType)
			throws IllegalStateException;

	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType)
			throws IllegalStateException;

	public void cleanUp(Object providedObj);
}
