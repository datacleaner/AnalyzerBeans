package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

final class BooleanAnalyzerColumnDelegate {

	private final RowAnnotationFactory _annotationFactory;
	private final RowAnnotation _nullAnnotation;
	private final RowAnnotation _trueAnnotation;
	private final RowAnnotation _falseAnnotation;
	private volatile int _rowCount;

	public BooleanAnalyzerColumnDelegate(RowAnnotationFactory annotationFactory) {
		_annotationFactory = annotationFactory;
		_nullAnnotation = _annotationFactory.createAnnotation();
		_trueAnnotation = _annotationFactory.createAnnotation();
		_falseAnnotation = _annotationFactory.createAnnotation();
	}

	public void run(Boolean value, InputRow row, int distinctCount) {
		_rowCount += distinctCount;
		if (value == null) {
			_annotationFactory.annotate(row, distinctCount, _nullAnnotation);
		} else {
			if (value.booleanValue()) {
				_annotationFactory.annotate(row, distinctCount, _trueAnnotation);
			} else {
				_annotationFactory.annotate(row, distinctCount, _falseAnnotation);
			}
		}
	}

	public int getRowCount() {
		return _rowCount;
	}

	public int getNullCount() {
		return _nullAnnotation.getRowCount();
	}

	public RowAnnotation getFalseAnnotation() {
		return _falseAnnotation;
	}

	public RowAnnotation getTrueAnnotation() {
		return _trueAnnotation;
	}

	public RowAnnotation getNullAnnotation() {
		return _nullAnnotation;
	}
}
