package org.eobjects.analyzer.beans;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.eobjects.analyzer.util.ValueCombination;

@AnalyzerBean("Boolean analyzer")
@Description("Inspect your boolean values. How is the distribution of true/false? Are there null values?")
public class BooleanAnalyzer implements RowProcessingAnalyzer<BooleanAnalyzerResult> {

	// comparator used to sort entries, getting the most frequent value
	// combinations to the top
	private static final Comparator<Map.Entry<ValueCombination<Boolean>, RowAnnotation>> frequentValueCombinationComparator = new Comparator<Map.Entry<ValueCombination<Boolean>, RowAnnotation>>() {
		@Override
		public int compare(Entry<ValueCombination<Boolean>, RowAnnotation> o1,
				Entry<ValueCombination<Boolean>, RowAnnotation> o2) {
			int result = o2.getValue().getRowCount() - o1.getValue().getRowCount();
			if (result == 0) {
				result = o2.getKey().compareTo(o1.getKey());
			}
			return result;
		}
	};

	private final Map<InputColumn<Boolean>, BooleanAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<Boolean>, BooleanAnalyzerColumnDelegate>();
	private final Map<ValueCombination<Boolean>, RowAnnotation> _valueCombinations = new HashMap<ValueCombination<Boolean>, RowAnnotation>();

	@Configured
	InputColumn<Boolean>[] _columns;

	@Provided
	RowAnnotationFactory _annotationFactory;

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
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Boolean[] values = new Boolean[_columns.length];
		int numTrue = 0;
		for (int i = 0; i < values.length; i++) {
			InputColumn<Boolean> col = _columns[i];
			Boolean value = row.getValue(col);
			BooleanAnalyzerColumnDelegate delegate = _columnDelegates.get(col);
			values[i] = value;
			delegate.run(value, row, distinctCount);
			if (value != null && value.booleanValue()) {
				numTrue++;
			}
		}

		// collect all combinations of booleans
		if (_columns.length > 1) {
			ValueCombination<Boolean> valueCombination = new ValueCombination<Boolean>(values);
			RowAnnotation annotation = _valueCombinations.get(valueCombination);
			if (annotation == null) {
				annotation = _annotationFactory.createAnnotation();
				_valueCombinations.put(valueCombination, annotation);
			}
			_annotationFactory.annotate(row, distinctCount, annotation);
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

		Crosstab<Number> valueCombinationCrosstab;

		if (_columns.length > 1) {
			measureDimension = new CrosstabDimension("Measure");

			columnDimension = new CrosstabDimension("Column");
			for (InputColumn<Boolean> column : _columns) {
				columnDimension.addCategory(column.getName());
			}
			columnDimension.addCategory("Frequency");

			valueCombinationCrosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);

			SortedSet<Entry<ValueCombination<Boolean>, RowAnnotation>> entries = new TreeSet<Map.Entry<ValueCombination<Boolean>, RowAnnotation>>(
					frequentValueCombinationComparator);
			entries.addAll(_valueCombinations.entrySet());

			int row = 0;
			for (Entry<ValueCombination<Boolean>, RowAnnotation> entry : entries) {

				String measureName;
				if (row == 0) {
					measureName = "Most frequent";
				} else if (row + 1 == entries.size()) {
					measureName = "Least frequent";
				} else {
					measureName = "Combination " + row;
				}
				measureDimension.addCategory(measureName);

				CrosstabNavigator<Number> nav = valueCombinationCrosstab.where(measureDimension, measureName);

				ValueCombination<Boolean> valueCombination = entry.getKey();
				RowAnnotation annotation = entry.getValue();

				nav.where(columnDimension, "Frequency");
				nav.put(annotation.getRowCount());
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, _columns));

				for (int i = 0; i < valueCombination.getValueCount(); i++) {
					InputColumn<Boolean> column = _columns[i];
					Boolean value = valueCombination.getValueAt(i);
					Byte numberValue = null;
					if (value != null) {
						if (value.booleanValue()) {
							numberValue = 1;
						} else {
							numberValue = 0;
						}
					}

					nav.where(columnDimension, column.getName());
					nav.put(numberValue);
				}

				row++;
			}

		} else {
			valueCombinationCrosstab = null;
		}

		return new BooleanAnalyzerResult(crosstab, valueCombinationCrosstab);
	}

}
