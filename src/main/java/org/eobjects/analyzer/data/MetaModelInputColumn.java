package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.schema.Column;

public final class MetaModelInputColumn extends AbstractInputColumn<Object> {

	private Column _column;

	public MetaModelInputColumn(Column column) {
		_column = column;
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
}
