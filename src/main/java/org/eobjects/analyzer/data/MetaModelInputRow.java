package org.eobjects.analyzer.data;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public final class MetaModelInputRow implements InputRow {

	private final Row _row;

	public MetaModelInputRow(Row row) {
		_row = row;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getValue(InputColumn<E> column) {
		MetaModelInputColumn metaModelInputColumn = (MetaModelInputColumn) column;
		Column physicalColumn = metaModelInputColumn.getPhysicalColumn();
		Object value = _row.getValue(physicalColumn);
		return (E) value;
	}
}
