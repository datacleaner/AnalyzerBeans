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
package org.eobjects.analyzer.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

final class BerkeleyDbMap<K, V> implements Map<K, V> {

	private final Map<K, V> _wrappedMap;
	private final Database _database;
	private final Environment _environment;

	@SuppressWarnings("unchecked")
	public BerkeleyDbMap(Environment environment, Database database, StoredMap map) {
		_environment = environment;
		_database = database;
		_wrappedMap = map;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		String name = _database.getDatabaseName();
		_database.close();
		_environment.removeDatabase(null, name);
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
		try {
			return _wrappedMap.get(key);
		} catch (ArrayIndexOutOfBoundsException e) {
			// there's a bug in berkeley that sometime causes this exception
			// when the value is null!
			return null;
		}
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
