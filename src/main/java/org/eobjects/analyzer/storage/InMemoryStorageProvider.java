package org.eobjects.analyzer.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StorageProvider that actually doesn't store data on disk, but only in memory.
 * This implementation is prone to out of memory errors, but is on the other
 * hand very quick for small jobs.
 * 
 * @author Kasper Sørensen
 */
public final class InMemoryStorageProvider implements StorageProvider {

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

}
