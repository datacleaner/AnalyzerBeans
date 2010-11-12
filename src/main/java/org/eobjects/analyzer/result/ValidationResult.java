package org.eobjects.analyzer.result;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Represents a result with rows that has been annotated as invalid in terms of
 * some analysis validation condition.
 * 
 * @author Kasper Sørensen
 * 
 */
public class ValidationResult extends AnnotatedRowsResult {

	private static final long serialVersionUID = 1L;

	public ValidationResult(RowAnnotation annotation, RowAnnotationFactory annotationFactory,
			InputColumn<?>... highlightedColumns) {
		super(annotation, annotationFactory, highlightedColumns);
	}
}
