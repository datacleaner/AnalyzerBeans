package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface CollectionProvider {

	public <E> List<E> createList(Type valueType) throws IllegalStateException;

	public <K, V> Map<K, V> createMap(Type keyType, Type valueType)
			throws IllegalStateException;

	public void cleanUp(Object providedObj);
}
