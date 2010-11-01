package org.eobjects.analyzer.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sleepycat.collections.StoredMap;

final class BerkeleyDbMap<K, V> implements Map<K, V> {

	private final Map<K, V> _wrappedMap;
	private final BerkeleyDbStorageProvider _storageProvider;

	@SuppressWarnings("unchecked")
	public BerkeleyDbMap(BerkeleyDbStorageProvider storageProvider, StoredMap map) {
		_storageProvider = storageProvider;
		_wrappedMap = map;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_storageProvider.cleanUp(_wrappedMap);
	}

	public int size() {
		return _wrappedMap.size();
	}

	public boolean isEmpty() {
		return _wrappedMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return _wrappedMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return _wrappedMap.containsValue(value);
	}

	public V get(Object key) {
		return _wrappedMap.get(key);
	}

	public V put(K key, V value) {
		return _wrappedMap.put(key, value);
	}

	public V remove(Object key) {
		return _wrappedMap.remove(key);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		_wrappedMap.putAll(m);
	}

	public void clear() {
		_wrappedMap.clear();
	}

	public Set<K> keySet() {
		return _wrappedMap.keySet();
	}

	public Collection<V> values() {
		return _wrappedMap.values();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return _wrappedMap.entrySet();
	}

	public boolean equals(Object o) {
		return _wrappedMap.equals(o);
	}

	public int hashCode() {
		return _wrappedMap.hashCode();
	}
}
