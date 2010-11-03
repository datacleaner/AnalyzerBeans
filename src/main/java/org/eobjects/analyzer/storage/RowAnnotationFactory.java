package org.eobjects.analyzer.storage;

import org.eobjects.analyzer.data.InputRow;

public interface RowAnnotationFactory {

	public RowAnnotation createAnnotation();

	public void annotate(InputRow row, RowAnnotation annotation);

	public void reset(RowAnnotation annotation);

	public InputRow[] getRows(RowAnnotation annotation);
}
