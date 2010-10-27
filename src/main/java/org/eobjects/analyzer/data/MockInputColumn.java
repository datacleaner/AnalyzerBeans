package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;

/**
 * A mock-implementation of the input column. Use this only for testing purposes
 * or in cases where you want to circumvent the actual framework!
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public class MockInputColumn<E> extends AbstractInputColumn<E> implements InputColumn<E> {

	private String _name;
	private Class<E> _clazz;

	public MockInputColumn(String name, Class<E> clazz) {
		_name = name;
		_clazz = clazz;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public DataTypeFamily getDataTypeFamily() {
		return DataTypeFamily.valueOf(_clazz);
	}

	@Override
	protected Column getPhysicalColumnInternal() {
		return null;
	}

	@Override
	protected int hashCodeInternal() {
		return _name.hashCode();
	}

	@Override
	protected boolean equalsInternal(AbstractInputColumn<?> that) {
		return this == that;
	}

	@Override
	public String toString() {
		return "MockInputColumn[name=" + _name + "]";
	}
}
