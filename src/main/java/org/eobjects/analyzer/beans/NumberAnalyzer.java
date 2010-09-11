package org.eobjects.analyzer.beans;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.QueryResultProducer;

import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * Number analyzer, which provides statistical information for number values:
 * 
 * <ul>
 * <li>Highest value</li>
 * <li>Lowest value</li>
 * <li>Sum</li>
 * <li>Mean</li>
 * <li>Geometric mean</li>
 * <li>Standard deviation</li>
 * <li>Variance</li>
 * </ul>
 */
@AnalyzerBean("Number analyzer")
public class NumberAnalyzer implements RowProcessingAnalyzer<CrosstabResult> {

	private Map<InputColumn<? extends Number>, SummaryStatistics> _statistics;
	private Map<InputColumn<? extends Number>, Long> _nullValues;

	@Configured
	InputColumn<? extends Number>[] columns;

	public NumberAnalyzer() {
	}

	public NumberAnalyzer(InputColumn<? extends Number>... columns) {
		this();
		this.columns = columns;
	}

	@Initialize
	public void init() {
		_statistics = new HashMap<InputColumn<? extends Number>, SummaryStatistics>();
		_nullValues = new HashMap<InputColumn<? extends Number>, Long>();
		for (InputColumn<? extends Number> column : columns) {
			SummaryStatistics statistics = _statistics.get(column);
			if (statistics == null) {
				statistics = new SummaryStatisticsImpl();
				_statistics.put(column, statistics);
			}
			_nullValues.put(column, 0l);
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<? extends Number> column : columns) {
			Number value = row.getValue(column);
			if (value != null) {
				SummaryStatistics statistics = _statistics.get(column);
				double doubleValue = value.doubleValue();
				for (int i = 0; i < distinctCount; i++) {
					statistics.addValue(doubleValue);
				}
			} else {
				Long count = _nullValues.get(column);
				count += distinctCount;
				_nullValues.put(column, count);
			}
		}
	}

	@Override
	public CrosstabResult getResult() {
		CrosstabDimension measureDimension = new CrosstabDimension("Measure");
		measureDimension.addCategory("Highest value");
		measureDimension.addCategory("Lowest value");
		measureDimension.addCategory("Sum");
		measureDimension.addCategory("Mean");
		measureDimension.addCategory("Geometric mean");
		measureDimension.addCategory("Standard deviation");
		measureDimension.addCategory("Variance");
		measureDimension.addCategory("Null values");
		measureDimension.addCategory("Non-null values");

		CrosstabDimension columnDimension = new CrosstabDimension("Column");
		for (InputColumn<? extends Number> column : columns) {
			columnDimension.addCategory(column.getName());
		}

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class,
				columnDimension, measureDimension);
		for (InputColumn<? extends Number> column : columns) {
			SummaryStatistics s = _statistics.get(column);
			Long nullCount = _nullValues.get(column);
			if (nullCount == null) {
				nullCount = 0l;
			}

			CrosstabNavigator<Number> navigator = crosstab.navigate().where(
					columnDimension, column.getName());
			long nonNullCount = s.getN();
			boolean queryable = column.isPhysicalColumn();

			if (nonNullCount > 0) {
				double highestValue = s.getMax();
				double lowestValue = s.getMin();
				double sum = s.getSum();
				double mean = s.getMean();
				double geometricMean = s.getGeometricMean();
				double standardDeviation = s.getStandardDeviation();
				double variance = s.getVariance();

				navigator.where(measureDimension, "Highest value").put(
						highestValue);
				if (queryable) {
					Column physicalColumn = column.getPhysicalColumn();
					Table table = physicalColumn.getTable();
					Query q = new Query()
							.select(table.getColumns())
							.from(table)
							.where(physicalColumn, OperatorType.EQUALS_TO,
									highestValue);
					navigator.attach(new QueryResultProducer(q, getClass()));
				}

				navigator.where(measureDimension, "Lowest value").put(
						lowestValue);
				if (queryable) {
					Column physicalColumn = column.getPhysicalColumn();
					Table table = physicalColumn.getTable();
					Query q = new Query()
							.select(table.getColumns())
							.from(table)
							.where(physicalColumn, OperatorType.EQUALS_TO,
									lowestValue);
					navigator.attach(new QueryResultProducer(q, getClass()));
				}

				navigator.where(measureDimension, "Sum").put(sum);
				navigator.where(measureDimension, "Mean").put(mean);
				navigator.where(measureDimension, "Geometric mean").put(
						geometricMean);
				navigator.where(measureDimension, "Standard deviation").put(
						standardDeviation);
				navigator.where(measureDimension, "Variance").put(variance);
			}
			navigator.where(measureDimension, "Null values").put(nullCount);
			if (queryable) {
				Column physicalColumn = column.getPhysicalColumn();
				Table table = physicalColumn.getTable();
				Query q = new Query().select(table.getColumns()).from(table)
						.where(physicalColumn, OperatorType.EQUALS_TO, null);
				navigator.attach(new QueryResultProducer(q, getClass()));
			}

			navigator.where(measureDimension, "Non-null values").put(
					nonNullCount);
			if (queryable) {
				Column physicalColumn = column.getPhysicalColumn();
				Table table = physicalColumn.getTable();
				Query q = new Query()
						.select(table.getColumns())
						.from(table)
						.where(physicalColumn, OperatorType.DIFFERENT_FROM,
								null);
				navigator.attach(new QueryResultProducer(q, getClass()));
			}
		}
		return new CrosstabResult(getClass(), crosstab);
	}
}
