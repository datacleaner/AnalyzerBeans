package org.eobjects.analyzer.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A mock implementation of the InputRow interface. Allows for adhoc generation
 * of a row using the put(...) method.
 * 
 * @author Kasper Sørensen
 * 
 */
public class MockInputRow implements InputRow {

	private static final AtomicInteger _idGenerator = new AtomicInteger(Integer.MIN_VALUE);

	private final Map<InputColumn<?>, Object> map = new LinkedHashMap<InputColumn<?>, Object>();
	private final int _id;

	public MockInputRow() {
		this(_idGenerator.getAndIncrement());
	}

	public MockInputRow(int id) {
		_id = id;
	}

	public MockInputRow(InputColumn<?>[] columns, Object[] values) {
		this(_idGenerator.getAndIncrement(), columns, values);
	}

	public MockInputRow(int id, InputColumn<?>[] columns, Object[] values) {
		this(id);
		for (int i = 0; i < values.length; i++) {
			put(columns[i], values[i]);
		}
	}

	@Override
	public int getId() {
		return _id;
	}

	public List<InputColumn<?>> getInputColumns() {
		return new ArrayList<InputColumn<?>>(map.keySet());
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		MockInputRow other = (MockInputRow) obj;
		if (_id != other._id)
			return false;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
}