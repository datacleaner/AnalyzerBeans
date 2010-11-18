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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
import org.eobjects.analyzer.result.DateAndTimeAnalyzerResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

@AnalyzerBean("Date/time analyzer")
@Description("Records a variety of interesting measures for date or time based data. Which are the highest/lowest values? How is the year distribution of dates? Are there null values?")
public class DateAndTimeAnalyzer implements RowProcessingAnalyzer<DateAndTimeAnalyzerResult> {

	private Map<InputColumn<Date>, DateAndTimeAnalyzerColumnDelegate> _delegates = new HashMap<InputColumn<Date>, DateAndTimeAnalyzerColumnDelegate>();

	@Inject
	@Configured
	InputColumn<Date>[] _columns;

	@Inject
	@Provided
	RowAnnotationFactory _annotationFactory;

	@Initialize
	public void init() {
		for (InputColumn<Date> col : _columns) {
			_delegates.put(col, new DateAndTimeAnalyzerColumnDelegate(_annotationFactory));
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<Date> col : _columns) {
			Date value = row.getValue(col);
			DateAndTimeAnalyzerColumnDelegate delegate = _delegates.get(col);
			delegate.run(value, row, distinctCount);
		}
	}

	@Override
	public DateAndTimeAnalyzerResult getResult() {
		CrosstabDimension measureDimension = new CrosstabDimension("Measure");
		measureDimension.addCategory("Row count");
		measureDimension.addCategory("Null count");

		measureDimension.addCategory("Highest date");
		measureDimension.addCategory("Lowest date");
		measureDimension.addCategory("Highest time");
		measureDimension.addCategory("Lowest time");

		CrosstabDimension columnDimension = new CrosstabDimension("Column");
		for (InputColumn<Date> column : _columns) {
			columnDimension.addCategory(column.getName());
		}

		Crosstab<Serializable> crosstab = new Crosstab<Serializable>(Serializable.class, columnDimension, measureDimension);
		CrosstabNavigator<Serializable> nav = crosstab.navigate();
		for (InputColumn<Date> column : _columns) {
			DateAndTimeAnalyzerColumnDelegate delegate = _delegates.get(column);

			nav.where(columnDimension, column.getName());

			nav.where(measureDimension, "Row count").put(delegate.getNumRows());

			int numNull = delegate.getNumNull();
			nav.where(measureDimension, "Null count").put(numNull);
			if (numNull > 0) {
				nav.attach(new AnnotatedRowsResult(delegate.getNullAnnotation(), _annotationFactory, column));
			}

			LocalDate maxDate = delegate.getMaxDate();
			nav.where(measureDimension, "Highest date").put(maxDate.toString());
			RowAnnotation annotation = delegate.getMaxDateAnnotation();
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}

			LocalDate minDate = delegate.getMinDate();
			nav.where(measureDimension, "Lowest date").put(minDate.toString());
			annotation = delegate.getMinDateAnnotation();
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}

			LocalTime maxTime = delegate.getMaxTime();
			nav.where(measureDimension, "Highest time").put(maxTime.toString());
			annotation = delegate.getMaxTimeAnnotation();
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}

			LocalTime minTime = delegate.getMinTime();
			nav.where(measureDimension, "Lowest time").put(minTime.toString());
			annotation = delegate.getMinTimeAnnotation();
			if (annotation.getRowCount() > 0) {
				nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
			}
		}

		return new DateAndTimeAnalyzerResult(crosstab);
	}

}
