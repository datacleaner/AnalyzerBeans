package org.eobjects.analyzer.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TransformedInputRow implements InputRow {

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
	public <E> E getValue(InputColumn<E> column) {
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
