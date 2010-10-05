package org.eobjects.analyzer.data;

import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;

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

		DataTypeFamily dataTypeFamily = column.getDataTypeFamily();
		switch (dataTypeFamily) {
		case DATE:
			value = ConvertToDateTransformer.transformValue(value);
			break;
		case BOOLEAN:
			value = ConvertToBooleanTransformer.transformValue(value);
			break;
		case NUMBER:
			value = ConvertToNumberTransformer.transformValue(value);
			break;
		case STRING:
			value = ConvertToStringTransformer.transformValue(value);
			break;
		}

		return (E) value;
	}
}
