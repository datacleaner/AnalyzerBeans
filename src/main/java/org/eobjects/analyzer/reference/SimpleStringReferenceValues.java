package org.eobjects.analyzer.reference;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class SimpleStringReferenceValues implements ReferenceValues<String> {

	private final String[] _values;
	private final boolean _caseSensitive;

	public SimpleStringReferenceValues(String[] values, boolean caseSensitive) {
		_values = values;
		_caseSensitive = caseSensitive;
	}

	public SimpleStringReferenceValues(List<String> values, boolean caseSensitive) {
		_values = values.toArray(new String[values.size()]);
		_caseSensitive = caseSensitive;
	}

	@Override
	public Collection<String> getValues() {
		return Arrays.asList(_values);
	}

	@Override
	public boolean containsValue(String value) {
		if (value == null) {
			for (String v : _values) {
				if (v == null) {
					return true;
				}
			}
		} else {
			for (String v : _values) {
				if (value.equals(v)) {
					return true;
				}
			}
			if (!_caseSensitive) {
				for (String v : _values) {
					if (value.equalsIgnoreCase(v)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
