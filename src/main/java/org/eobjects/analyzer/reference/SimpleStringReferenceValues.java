package org.eobjects.analyzer.reference;

import java.util.Arrays;
import java.util.Collection;

public final class SimpleStringReferenceValues implements ReferenceValues<String> {

	private final String[] _synonyms;
	private final boolean _caseSensitive;

	public SimpleStringReferenceValues(String[] synonyms, boolean caseSensitive) {
		_synonyms = synonyms;
		_caseSensitive = caseSensitive;
	}

	@Override
	public Collection<String> getValues() {
		return Arrays.asList(_synonyms);
	}

	@Override
	public boolean containsValue(String value) {
		if (value == null) {
			for (String v : _synonyms) {
				if (v == null) {
					return true;
				}
			}
		} else {
			for (String v : _synonyms) {
				if (value.equals(v)) {
					return true;
				}
			}
			if (!_caseSensitive) {
				for (String v : _synonyms) {
					if (value.equalsIgnoreCase(v)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
