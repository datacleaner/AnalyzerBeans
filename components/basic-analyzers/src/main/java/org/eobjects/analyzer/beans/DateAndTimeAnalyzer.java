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

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

@AnalyzerBean("Date/time analyzer")
@Description("Records a variety of interesting measures for date or time based data. Which are the highest/lowest values? How is the year distribution of dates? Are there null values?")
@Concurrent(true)
public class DateAndTimeAnalyzer implements Analyzer<DateAndTimeAnalyzerResult> {

    public static final String MEASURE_LOWEST_TIME = "Lowest time";
    public static final String MEASURE_HIGHEST_TIME = "Highest time";
    public static final String MEASURE_LOWEST_DATE = "Lowest date";
    public static final String MEASURE_HIGHEST_DATE = "Highest date";
    public static final String MEASURE_AVERAGE_DATE = "Average date";
    public static final String MEASURE_NULL_COUNT = "Null count";
    public static final String MEASURE_ROW_COUNT = "Row count";
    public static final String DIMENSION_MEASURE = "Measure";
    public static final String DIMENSION_COLUMN = "Column";

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
        CrosstabDimension measureDimension = new CrosstabDimension(DIMENSION_MEASURE);
        measureDimension.addCategory(MEASURE_ROW_COUNT);
        measureDimension.addCategory(MEASURE_NULL_COUNT);
        measureDimension.addCategory(MEASURE_HIGHEST_DATE);
        measureDimension.addCategory(MEASURE_LOWEST_DATE);
        measureDimension.addCategory(MEASURE_AVERAGE_DATE);
        measureDimension.addCategory(MEASURE_HIGHEST_TIME);
        measureDimension.addCategory(MEASURE_LOWEST_TIME);

        CrosstabDimension columnDimension = new CrosstabDimension(DIMENSION_COLUMN);
        for (InputColumn<Date> column : _columns) {
            columnDimension.addCategory(column.getName());
        }

        Crosstab<Serializable> crosstab = new Crosstab<Serializable>(Serializable.class, columnDimension,
                measureDimension);
        CrosstabNavigator<Serializable> nav = crosstab.navigate();
        for (InputColumn<Date> column : _columns) {
            DateAndTimeAnalyzerColumnDelegate delegate = _delegates.get(column);

            nav.where(columnDimension, column.getName());

            nav.where(measureDimension, MEASURE_ROW_COUNT).put(delegate.getNumRows());

            int numNull = delegate.getNumNull();
            nav.where(measureDimension, MEASURE_NULL_COUNT).put(numNull);
            if (numNull > 0) {
                nav.attach(new AnnotatedRowsResult(delegate.getNullAnnotation(), _annotationFactory, column));
            }

            LocalDate maxDate = delegate.getMaxDate();
            nav.where(measureDimension, MEASURE_HIGHEST_DATE).put(toString(maxDate));
            RowAnnotation annotation = delegate.getMaxDateAnnotation();
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }

            LocalDate minDate = delegate.getMinDate();
            nav.where(measureDimension, MEASURE_LOWEST_DATE).put(toString(minDate));
            annotation = delegate.getMinDateAnnotation();
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }
            
            LocalDate averageDate = delegate.getAverageDate();
            nav.where(measureDimension, MEASURE_AVERAGE_DATE).put(toString(averageDate));

            LocalTime maxTime = delegate.getMaxTime();
            nav.where(measureDimension, MEASURE_HIGHEST_TIME).put(toString(maxTime));
            annotation = delegate.getMaxTimeAnnotation();
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }

            LocalTime minTime = delegate.getMinTime();
            nav.where(measureDimension, MEASURE_LOWEST_TIME).put(toString(minTime));
            annotation = delegate.getMinTimeAnnotation();
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }
        }

        return new DateAndTimeAnalyzerResult(crosstab);
    }

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

}
