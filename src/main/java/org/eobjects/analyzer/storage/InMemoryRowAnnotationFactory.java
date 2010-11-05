package org.eobjects.analyzer.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public class InMemoryRowAnnotationFactory implements RowAnnotationFactory {

	private final Map<RowAnnotation, List<InMemoryAnnotatedRow>> _annotatedRows = new HashMap<RowAnnotation, List<InMemoryAnnotatedRow>>();

	@Override
	public RowAnnotation createAnnotation() {
		return new RowAnnotationImpl();
	}

	protected int getInMemoryRowCount(RowAnnotation annotation) {
		List<InMemoryAnnotatedRow> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	@Override
	public void annotate(InputRow row, int distinctRowCount, RowAnnotation annotation) {
		InMemoryAnnotatedRow inMemoryAnnotatedRow = new InMemoryAnnotatedRow(row, distinctRowCount);
		List<InMemoryAnnotatedRow> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			synchronized (this) {
				rows = _annotatedRows.get(annotation);
				if (rows == null) {
					rows = new ArrayList<InMemoryAnnotatedRow>();
					_annotatedRows.put(annotation, rows);
				}
			}
		}
		if (!rows.contains(inMemoryAnnotatedRow)) {
			rows.add(inMemoryAnnotatedRow);
			((RowAnnotationImpl) annotation).incrementRowCount(distinctRowCount);
		}
	}

	@Override
	public InputRow[] getRows(RowAnnotation annotation) {
		List<InMemoryAnnotatedRow> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			return new InputRow[0];
		}
		return rows.toArray(new InputRow[rows.size()]);
	}

	@Override
	public void reset(RowAnnotation annotation) {
		synchronized (this) {
			_annotatedRows.remove(annotation);
		}
		((RowAnnotationImpl) annotation).resetRowCount();
	}

	@Override
	public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
		HashMap<Object, Integer> map = new HashMap<Object, Integer>();
		List<InMemoryAnnotatedRow> list = _annotatedRows.get(annotation);
		if (list == null) {
			return map;
		}
		for (InMemoryAnnotatedRow row : list) {
			Object value = row.getValue(inputColumn);
			Integer count = map.get(value);
			if (count == null) {
				count = 0;
			}
			count = count.intValue() + row.getDistinctCount();
			map.put(value, count);
		}
		return map;
	}
}
