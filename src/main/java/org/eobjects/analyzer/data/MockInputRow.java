package org.eobjects.analyzer.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mock implementation of the InputRow interface. Allows for adhoc generation
 * of a row using the put(...) method.
 * 
 * @author Kasper Sørensen
 * 
 */
public class MockInputRow implements InputRow {

	Map<InputColumn<?>, Object> map = new HashMap<InputColumn<?>, Object>();

	public MockInputRow() {
	}

	public MockInputRow(InputColumn<?>[] columns, Object[] values) {
		for (int i = 0; i < values.length; i++) {
			put(columns[i], values[i]);
		}
	}

	public Set<InputColumn<?>> getInputColumns() {
		return map.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getValue(InputColumn<E> column) {
		return (E) map.get(column);
	}

	/**
	 * Puts/adds a new value to the row.
	 * 
	 * @param column
	 * @param value
	 * @return
	 */
	public MockInputRow put(InputColumn<?> column, Object value) {
		map.put(column, value);
		return this;
	}
}