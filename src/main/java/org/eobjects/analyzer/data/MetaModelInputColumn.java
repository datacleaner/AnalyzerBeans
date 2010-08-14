package org.eobjects.analyzer.data;

import org.eobjects.analyzer.util.ReflectionUtils;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public final class MetaModelInputColumn extends AbstractInputColumn<Object> {

	private final Column _column;

	public MetaModelInputColumn(Column column) {
		if (column == null) {
			throw new IllegalArgumentException("column cannot be null");
		}
		_column = column;
	}

	@SuppressWarnings("unchecked")
	public <E> InputColumn<E> narrow(Class<E> e) {
		Class<?> javaEquivalentClass = _column.getType()
				.getJavaEquivalentClass();
		if (ReflectionUtils.is(javaEquivalentClass, e)) {
			return (InputColumn<E>) this;
		}
		throw new IllegalArgumentException(
				"Can only narrow this column to supertypes of: "
						+ javaEquivalentClass);
	}

	@Override
	public String getName() {
		return _column.getName();
	}

	@Override
	protected Column getPhysicalColumnInternal() {
		return _column;
	}

	@Override
	protected boolean equalsInternal(AbstractInputColumn<?> that) {
		MetaModelInputColumn that2 = (MetaModelInputColumn) that;
		return _column.equals(that2._column);
	}

	@Override
	protected int hashCodeInternal() {
		return _column.hashCode();
	}

	@Override
	public DataTypeFamily getDataTypeFamily() {
		ColumnType type = _column.getType();
		if (type == null) {
			return DataTypeFamily.UNDEFINED;
		}
		if (type.isBoolean()) {
			return DataTypeFamily.BOOLEAN;
		}
		if (type.isLiteral()) {
			return DataTypeFamily.STRING;
		}
		if (type.isNumber()) {
			return DataTypeFamily.NUMBER;
		}
		if (type.isTimeBased()) {
			return DataTypeFamily.DATE;
		}
		return DataTypeFamily.UNDEFINED;
	}

	@Override
	public String toString() {
		return "MetaModelInputColumn[" + _column + "]";
	}
}
