package org.eobjects.analyzer.result;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

public class BooleanAnalyzerResult extends CrosstabResult {

	private static final long serialVersionUID = 1L;
	private final RowAnnotation _onlyTrueAnnotation;
	private final RowAnnotation _onlyFalseAnnotation;
	private final RowAnnotation _multipleTrueAnnotation;
	private final RowAnnotationFactory _annotationFactory;
	private final InputColumn<?>[] _columns;

	public BooleanAnalyzerResult(Crosstab<Number> crosstab) {
		this(crosstab, null, null, null, null, null);
	}

	public BooleanAnalyzerResult(Crosstab<Number> crosstab, RowAnnotationFactory annotationFactory,
			RowAnnotation onlyTrueAnnotation, RowAnnotation onlyFalseAnnotation, InputColumn<?>[] columns,
			RowAnnotation multipleTrueAnnotation) {
		super(crosstab);
		_annotationFactory = annotationFactory;
		_onlyTrueAnnotation = onlyTrueAnnotation;
		_onlyFalseAnnotation = onlyFalseAnnotation;
		_columns = columns;
		_multipleTrueAnnotation = multipleTrueAnnotation;
	}

	public AnnotatedRowsResult getOnlyTrueRows() {
		return new AnnotatedRowsResult(_onlyTrueAnnotation, _annotationFactory, _columns);
	}

	public AnnotatedRowsResult getOnlyFalseRows() {
		return new AnnotatedRowsResult(_onlyFalseAnnotation, _annotationFactory, _columns);
	}

	public AnnotatedRowsResult getMultipleTrueRows() {
		return new AnnotatedRowsResult(_multipleTrueAnnotation, _annotationFactory, _columns);
	}
}
