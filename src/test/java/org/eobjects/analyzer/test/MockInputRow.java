package org.eobjects.analyzer.test;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public class MockInputRow implements InputRow {

	Map<InputColumn<?>, Object> map = new HashMap<InputColumn<?>, Object>();
	
	public MockInputRow() {
	}
	
	public MockInputRow(InputColumn<?>[] columns, Object[] values) {
		for (int i = 0; i < values.length; i++) {
			put(columns[i], values[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getValue(InputColumn<E> column) {
		return (E) map.get(column);
	}

	public MockInputRow put(InputColumn<?> column, Object value) {
		map.put(column, value);
		return this;
	}
}
