package org.eobjects.analyzer.util;

import java.util.Map.Entry;

public final class ImmutableEntry<K, V> implements Entry<K, V> {

	private final K _key;
	private final V _value;

	public ImmutableEntry(K key, V value) {
		super();
		_key = key;
		_value = value;
	}

	@Override
	public K getKey() {
		return _key;
	}

	@Override
	public V getValue() {
		return _value;
	}

	@Override
	public V setValue(V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_key == null) ? 0 : _key.hashCode());
		result = prime * result + ((_value == null) ? 0 : _value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		ImmutableEntry other = (ImmutableEntry) obj;
		if (_key == null) {
			if (other._key != null)
				return false;
		} else if (!_key.equals(other._key))
			return false;
		if (_value == null) {
			if (other._value != null)
				return false;
		} else if (!_value.equals(other._value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableEntry[" + _key + "," + _value + "]";
	}
}
