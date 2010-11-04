package org.eobjects.analyzer.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputRow;

public class InMemoryRowAnnotationFactory implements RowAnnotationFactory {

	private final Map<RowAnnotation, List<InputRow>> annotatedRows = new HashMap<RowAnnotation, List<InputRow>>();

	@Override
	public RowAnnotation createAnnotation() {
		return new RowAnnotationImpl();
	}

	protected int getRowCount(RowAnnotation annotation) {
		List<InputRow> rows = annotatedRows.get(annotation);
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	@Override
	public void annotate(InputRow row, int distinctRowCount, RowAnnotation annotation) {
		List<InputRow> rows = annotatedRows.get(annotation);
		if (rows == null) {
			synchronized (this) {
				rows = annotatedRows.get(annotation);
				if (rows == null) {
					rows = new ArrayList<InputRow>();
					annotatedRows.put(annotation, rows);
				}
			}
		}
		if (!rows.contains(row)) {
			rows.add(row);
			((RowAnnotationImpl) annotation).incrementRowCount(distinctRowCount);
		}
	}

	@Override
	public InputRow[] getRows(RowAnnotation annotation) {
		List<InputRow> rows = annotatedRows.get(annotation);
		if (rows == null) {
			return new InputRow[0];
		}
		return rows.toArray(new InputRow[rows.size()]);
	}

	@Override
	public void reset(RowAnnotation annotation) {
		synchronized (this) {
			annotatedRows.remove(annotation);
		}
		((RowAnnotationImpl) annotation).resetRowCount();
	}

}
