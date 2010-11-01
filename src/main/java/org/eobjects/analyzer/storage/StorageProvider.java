package org.eobjects.analyzer.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configurable component which provides cached/persistent storage for
 * collections and other types that are needed during execution.
 * 
 * @author Kasper Sørensen
 */
public interface StorageProvider {

	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException;

	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException;

	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException;
}
