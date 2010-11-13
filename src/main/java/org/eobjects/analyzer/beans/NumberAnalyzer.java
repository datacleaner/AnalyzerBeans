/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.NumberAnalyzerResult;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

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
@Description("Provides insight into number-column values.")
public class NumberAnalyzer implements RowProcessingAnalyzer<NumberAnalyzerResult> {

	private Map<InputColumn<? extends Number>, NumberAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<? extends Number>, NumberAnalyzerColumnDelegate>();

	@Configured
	InputColumn<? extends Number>[] _columns;

	@Provided
	RowAnnotationFactory _annotationFactory;

	public NumberAnalyzer() {
	}

	public NumberAnalyzer(InputColumn<? extends Number>... columns) {
		this();
		_columns = columns;
		_annotationFactory = new InMemoryRowAnnotationFactory();
		init();
	}

	@Initialize
	public void init() {
		for (InputColumn<? extends Number> column : _columns) {
			_columnDelegates.put(column, new NumberAnalyzerColumnDelegate(_annotationFactory));
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<? extends Number> column : _columns) {
			NumberAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
			Number value = row.getValue(column);

			delegate.run(row, value, distinctCount);
		}
	}

	@Override
	public NumberAnalyzerResult getResult() {
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
		for (InputColumn<? extends Number> column : _columns) {
			columnDimension.addCategory(column.getName());
		}

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, columnDimension, measureDimension);
		for (InputColumn<? extends Number> column : _columns) {
			NumberAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

			SummaryStatistics s = delegate.getStatistics();
			int nullCount = delegate.getNullCount();

			CrosstabNavigator<Number> navigator = crosstab.navigate().where(columnDimension, column.getName());
			long nonNullCount = s.getN();

			if (nonNullCount > 0) {
				double highestValue = s.getMax();
				double lowestValue = s.getMin();
				double sum = s.getSum();
				double mean = s.getMean();
				double geometricMean = s.getGeometricMean();
				double standardDeviation = s.getStandardDeviation();
				double variance = s.getVariance();

				navigator.where(measureDimension, "Highest value").put(highestValue);
				addAttachment(navigator, delegate.getMaxAnnotation());

				navigator.where(measureDimension, "Lowest value").put(lowestValue);
				addAttachment(navigator, delegate.getMinAnnotation());

				navigator.where(measureDimension, "Sum").put(sum);
				navigator.where(measureDimension, "Mean").put(mean);
				navigator.where(measureDimension, "Geometric mean").put(geometricMean);
				navigator.where(measureDimension, "Standard deviation").put(standardDeviation);
				navigator.where(measureDimension, "Variance").put(variance);
			}
			navigator.where(measureDimension, "Null values").put(nullCount);

			if (nullCount > 0) {
				addAttachment(navigator, delegate.getNullAnnotation());
			}

			navigator.where(measureDimension, "Non-null values").put(nonNullCount);
			if (nonNullCount > 0) {
				addAttachment(navigator, delegate.getNonNullAnnotation());
			}
		}
		return new NumberAnalyzerResult(_columns, crosstab);
	}

	private void addAttachment(CrosstabNavigator<Number> nav, RowAnnotation annotation) {
		nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory));
	}
}
