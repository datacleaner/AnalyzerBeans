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
package org.eobjects.analyzer.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TransformedInputRow extends AbstractInputRow {

	private final InputRow _delegate;
	private final Map<InputColumn<?>, Object> _values;

	public TransformedInputRow(InputRow delegate) {
		this(delegate, new HashMap<InputColumn<?>, Object>());
	}

	public TransformedInputRow(InputRow delegateInputRow, Map<InputColumn<?>, Object> values) {
		_delegate = delegateInputRow;
		_values = values;
	}

	@Override
	public int getId() {
		return _delegate.getId();
	}

	public void addValue(InputColumn<?> inputColumn, Object value) {
		_values.put(inputColumn, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getValueInternal(InputColumn<E> column) {
		if (_values.containsKey(column)) {
			return (E) _values.get(column);
		}
		if (_delegate == null) {
			return null;
		}
		return _delegate.getValue(column);
	}

	public InputRow getDelegate() {
		return _delegate;
	}

	@Override
	public List<InputColumn<?>> getInputColumns() {
		List<InputColumn<?>> inputColumns = _delegate.getInputColumns();
		inputColumns.addAll(_values.keySet());
		return inputColumns;
	}

	public Set<InputColumn<?>> getTransformedInputColumns() {
		return _values.keySet();
	}

	@Override
	public String toString() {
		return "TransformedInputRow[values=" + _values + ",delegate=" + _delegate + "]";
	}
}
