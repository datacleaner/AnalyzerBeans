package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.List;

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

	private final RowAnnotationFactory _annotationFactory;
	private final RowAnnotation _annotation;
	private final InputColumn<?>[] _highlightedColumns;

	private TableModel _tableModel;
	private List<InputColumn<?>> _inputColumns;

	private InputRow[] _rows;;

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
