package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public final class SimpleReferenceValues<E> implements ReferenceValues<E>, Serializable {

	private static final long serialVersionUID = 1L;

	private final E[] _values;

	public SimpleReferenceValues(E... values) {
		_values = values;
	}

	@Override
	public boolean containsValue(E value) {
		if (value == null) {
			for (E v : _values) {
				if (v == null) {
					return true;
				}
			}
		} else {
			for (E v : _values) {
				if (value.equals(v)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Collection<E> getValues() {
		return Arrays.asList(_values);
	}

}
