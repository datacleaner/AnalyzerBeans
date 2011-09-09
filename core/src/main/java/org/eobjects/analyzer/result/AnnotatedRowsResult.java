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
package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Represents a typical "drill to detail" result consisting of a set of
 * annotated rows
 * 
 * @author Kasper Sørensen
 */
public class AnnotatedRowsResult implements AnalyzerResult, TableModelResult {

	private static final long serialVersionUID = 1L;

	private final transient RowAnnotationFactory _annotationFactory;
	private final transient RowAnnotation _annotation;
	private final transient InputColumn<?>[] _highlightedColumns;
	private transient InputRow[] _rows;;

	private TableModel _tableModel;
	private List<InputColumn<?>> _inputColumns;

	public AnnotatedRowsResult(RowAnnotation annotation, RowAnnotationFactory annotationFactory,
			InputColumn<?>... highlightedColumns) {
		_annotationFactory = annotationFactory;
		_annotation = annotation;
		_highlightedColumns = highlightedColumns;
	}

	public List<InputColumn<?>> getInputColumns() {
		if (_inputColumns == null) {
			InputRow[] rows = getRows();
			if (rows.length > 0) {
				InputRow firstRow = rows[0];
				_inputColumns = firstRow.getInputColumns();
			} else {
				_inputColumns = new ArrayList<InputColumn<?>>(0);
			}
		}
		return _inputColumns;
	}

	public InputRow[] getRows() {
		if (_rows == null) {
			_rows = _annotationFactory.getRows(_annotation);
			if (_rows == null) {
				_rows = new InputRow[0];
			}
		}
		return _rows;
	}

	/**
	 * Creates a table model containing only distinct values from a particular
	 * input column, and the counts of those distinct values. Note that the
	 * counts may only be the count from the data that is available in the
	 * annotation row storage, which may just be a preview/subset of the actual
	 * data.
	 * 
	 * @param inputColumnOfInterest
	 * @return
	 */
	public TableModel toDistinctValuesTableModel(InputColumn<?> inputColumnOfInterest) {
		Map<Object, Integer> valueCounts = _annotationFactory.getValueCounts(_annotation, inputColumnOfInterest);
		DefaultTableModel tableModel = new DefaultTableModel(new String[] { inputColumnOfInterest.getName(),
				"Count in dataset" }, valueCounts.size());

		// sort the set
		TreeSet<Entry<Object, Integer>> set = new TreeSet<Entry<Object, Integer>>(new Comparator<Entry<Object, Integer>>() {
			@Override
			public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
				int countDiff = o2.getValue().intValue() - o1.getValue().intValue();
				if (countDiff == 0) {
					return -1;
				}
				return countDiff;
			}
		});
		set.addAll(valueCounts.entrySet());
		valueCounts = null;

		int i = 0;
		for (Entry<Object, Integer> entry : set) {
			tableModel.setValueAt(entry.getKey(), i, 0);
			tableModel.setValueAt(entry.getValue(), i, 1);
			i++;
		}

		return tableModel;
	}

	@Override
	public TableModel toTableModel() {
		if (_tableModel == null) {
			InputRow[] rows = getRows();
			List<InputColumn<?>> inputColumns = getInputColumns();
			String[] headers = new String[inputColumns.size()];
			for (int i = 0; i < headers.length; i++) {
				headers[i] = inputColumns.get(i).getName();
			}

			_tableModel = new DefaultTableModel(headers, rows.length);
			int row = 0;
			for (InputRow inputRow : rows) {
				for (int i = 0; i < inputColumns.size(); i++) {
					InputColumn<?> inputColumn = inputColumns.get(i);
					Object value = inputRow.getValue(inputColumn);
					_tableModel.setValueAt(value, row, i);
				}
				row++;
			}
		}
		return _tableModel;
	}

	public InputColumn<?>[] getHighlightedColumns() {
		return _highlightedColumns;
	}

	public int getColumnIndex(InputColumn<?> col) {
		List<InputColumn<?>> inputColumns = getInputColumns();
		int i = 0;
		for (InputColumn<?> inputColumn : inputColumns) {
			if (col.equals(inputColumn)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public RowAnnotation getAnnotation() {
		return _annotation;
	}

	public int getRowCount() {
		return _annotation.getRowCount();
	}
}
