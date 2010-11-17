package org.eobjects.analyzer.beans;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

@AnalyzerBean("Boolean analyzer")
@Description("Inspect your boolean values. How is the distribution of true/false? Are there null values?")
public class BooleanAnalyzer implements RowProcessingAnalyzer<BooleanAnalyzerResult> {

	private Map<InputColumn<Boolean>, BooleanAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<Boolean>, BooleanAnalyzerColumnDelegate>();

	@Configured
	InputColumn<Boolean>[] _columns;

	@Provided
	RowAnnotationFactory _annotationFactory;

	/**
	 * An annotation for multiple true values (in all columns analyzed)
	 */
	private RowAnnotation _multipleTrueAnnotation;

	/**
	 * An annotation for only false values (in all columns analyzed)
	 */
	private RowAnnotation _onlyFalseAnnotation;

	/**
	 * An annotation for only true values (in all columns analyzed)
	 */
	private RowAnnotation _onlyTrueAnnotation;

	public BooleanAnalyzer(InputColumn<Boolean>[] columns) {
		_columns = columns;
		_annotationFactory = new InMemoryRowAnnotationFactory();
	}

	public BooleanAnalyzer() {
	}

	@Initialize
	public void init() {
		for (InputColumn<Boolean> col : _columns) {
			_columnDelegates.put(col, new BooleanAnalyzerColumnDelegate(_annotationFactory));
		}
		if (_columns.length > 1) {
			_multipleTrueAnnotation = _annotationFactory.createAnnotation();
			_onlyFalseAnnotation = _annotationFactory.createAnnotation();
			_onlyTrueAnnotation = _annotationFactory.createAnnotation();
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		int numTrue = 0;
		for (InputColumn<Boolean> col : _columns) {
			BooleanAnalyzerColumnDelegate delegate = _columnDelegates.get(col);
			Boolean value = row.getValue(col);
			delegate.run(value, row, distinctCount);
			if (value != null && value.booleanValue()) {
				numTrue++;
			}
		}

		if (_columns.length > 1) {
			if (numTrue == 0) {
				_annotationFactory.annotate(row, distinctCount, _onlyFalseAnnotation);
			}
			if (numTrue == _columns.length) {
				_annotationFactory.annotate(row, distinctCount, _onlyTrueAnnotation);
			}
			if (numTrue > 1) {
				_annotationFactory.annotate(row, distinctCount, _multipleTrueAnnotation);
			}
		}
	}

	@Override
	public BooleanAnalyzerResult getResult() {
		CrosstabDimension measureDimension = new CrosstabDimension("Measure");
		measureDimension.addCategory("Row count");
		measureDimension.addCategory("Null count");
		measureDimension.addCategory("True count");
		measureDimension.addCategory("False count");

		CrosstabDimension columnDimension = new CrosstabDimension("Column");
		for (InputColumn<Boolean> column : _columns) {
			columnDimension.addCategory(column.getName());
		}

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);
		for (InputColumn<Boolean> column : _columns) {
			CrosstabNavigator<Number> nav = crosstab.navigate().where(columnDimension, column.getName());
			BooleanAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

			nav.where(measureDimension, "Row count").put(delegate.getRowCount());

			int nullCount = delegate.getNullCount();
			nav.where(measureDimension, "Null count").put(nullCount);
			if (nullCount > 0) {
				nav.attach(new AnnotatedRowsResult(delegate.getNullAnnotation(), _annotationFactory, column));
			}

			RowAnnotation annotation = delegate.getTrueAnnotation();
			nav.where(measureDimension, "True count").put(annotation.getRowCount());
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}

			annotation = delegate.getFalseAnnotation();
			nav.where(measureDimension, "False count").put(annotation.getRowCount());
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}
		}
		if (_columns.length > 1) {
			return new BooleanAnalyzerResult(crosstab, _annotationFactory, _onlyTrueAnnotation, _onlyFalseAnnotation,
					_columns, _multipleTrueAnnotation);
		} else {
			return new BooleanAnalyzerResult(crosstab);
		}
	}

}
