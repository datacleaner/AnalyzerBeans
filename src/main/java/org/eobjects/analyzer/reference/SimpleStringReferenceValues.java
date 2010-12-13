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
package org.eobjects.analyzer.reference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.util.CollectionUtils;

public final class SimpleStringReferenceValues implements ReferenceValues<String> {

	private final Set<String> _values;
	private final boolean _caseSensitive;

	public SimpleStringReferenceValues(String[] values, boolean caseSensitive) {
		_values = CollectionUtils.set(values);
		_caseSensitive = caseSensitive;
	}

	public SimpleStringReferenceValues(Collection<String> values, boolean caseSensitive) {
		if (values instanceof Set<?>) {
			_values =(Set<String>) values;
		} else {
			_values = new HashSet<String>(values);
		}
		_caseSensitive = caseSensitive;
	}

	@Override
	public Collection<String> getValues() {
		return Collections.unmodifiableSet(_values);
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
