package org.eobjects.analyzer.test;

import org.eobjects.analyzer.data.AbstractInputColumn;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;

import dk.eobjects.metamodel.schema.Column;

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

}
