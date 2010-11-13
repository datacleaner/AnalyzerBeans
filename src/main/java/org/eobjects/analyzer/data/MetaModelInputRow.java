/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.data;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

public final class MetaModelInputRow implements InputRow {

	private final Row _row;
	private final int _rowNumber;

	public MetaModelInputRow(int rowNumber, Row row) {
		_rowNumber = rowNumber;
		_row = row;
	}

	@Override
	public int getId() {
		return _rowNumber;
	}

	public Row getRow() {
		return _row;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getValue(InputColumn<E> column) {
		if (!column.isPhysicalColumn()) {
			return null;
		}
		Column physicalColumn = column.getPhysicalColumn();
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

	@Override
	public String toString() {
		return "MetaModelInputRow[" + _row + "]";
	}

	@Override
	public List<InputColumn<?>> getInputColumns() {
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		SelectItem[] selectItems = _row.getSelectItems();
		for (SelectItem selectItem : selectItems) {
			if (selectItem.getColumn() != null && selectItem.getFunction() == null) {
				result.add(new MetaModelInputColumn(selectItem.getColumn()));
			}
		}
		return result;
	}
}
