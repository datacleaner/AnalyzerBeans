package org.eobjects.analyzer.beans;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Helper class for the number analyzer, which handles the processing of a
 * single column's values.
 * 
 * @author Kasper Sørensen
 */
final class NumberAnalyzerColumnDelegate {

	private final RowAnnotationFactory _annotationFactory;
	private final SummaryStatistics _statistics;
	private int _nullCount;
	private final RowAnnotation _nullAnnotation;
	private final RowAnnotation _maxAnnotation;
	private final RowAnnotation _minAnnotation;
	private final RowAnnotation _nonNullAnnotation;

	public NumberAnalyzerColumnDelegate(RowAnnotationFactory annotationFactory) {
		_annotationFactory = annotationFactory;
		_nullAnnotation = _annotationFactory.createAnnotation();
		_maxAnnotation = _annotationFactory.createAnnotation();
		_minAnnotation = _annotationFactory.createAnnotation();
		_nonNullAnnotation = _annotationFactory.createAnnotation();
		_statistics = new SummaryStatistics();
		_nullCount = 0;
	}

	public void run(InputRow row, Number value, int distinctCount) {
		if (value != null) {
			_annotationFactory.annotate(row, _nonNullAnnotation);
			double doubleValue = value.doubleValue();
			double max = _statistics.getMax();
			double min = _statistics.getMin();

			if (max < doubleValue) {
				_annotationFactory.reset(_maxAnnotation);
			}
			if (min > doubleValue) {
				_annotationFactory.reset(_minAnnotation);
			}

			for (int i = 0; i < distinctCount; i++) {
				_statistics.addValue(doubleValue);
			}

			max = _statistics.getMax();
			min = _statistics.getMin();

			if (max == doubleValue) {
				_annotationFactory.annotate(row, _maxAnnotation);
			}
			if (min == doubleValue) {
				_annotationFactory.annotate(row, _minAnnotation);
			}
		} else {
			_nullCount += distinctCount;
			_annotationFactory.annotate(row, _nullAnnotation);
		}
	}

	public RowAnnotation getNullAnnotation() {
		return _nullAnnotation;
	}

	public SummaryStatistics getStatistics() {
		return _statistics;
	}

	public int getNullCount() {
		return _nullCount;
	}

	public RowAnnotation getMaxAnnotation() {
		return _maxAnnotation;
	}

	public RowAnnotation getMinAnnotation() {
		return _minAnnotation;
	}

	public RowAnnotation getNonNullAnnotation() {
		return _nonNullAnnotation;
	}
}
