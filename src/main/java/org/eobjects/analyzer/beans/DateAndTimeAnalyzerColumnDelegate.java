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

import java.util.Date;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Helper class for the Date/time Analyzer. This class collects all the
 * statistics for a single column. The Date/time Analyzer then consists of a
 * number of these delegates.
 * 
 * @author Kasper Sørensen
 */
final class DateAndTimeAnalyzerColumnDelegate {

	private final RowAnnotationFactory _annotationFactory;
	private final RowAnnotation _nullAnnotation;
	private final RowAnnotation _maxDateAnnotation;
	private final RowAnnotation _minDateAnnotation;
	private final RowAnnotation _maxTimeAnnotation;
	private final RowAnnotation _minTimeAnnotation;
	private volatile LocalDate _minDate;
	private volatile LocalDate _maxDate;
	private volatile LocalTime _minTime;
	private volatile LocalTime _maxTime;
	private volatile int _numRows;

	public DateAndTimeAnalyzerColumnDelegate(RowAnnotationFactory annotationFactory) {
		_annotationFactory = annotationFactory;
		_nullAnnotation = _annotationFactory.createAnnotation();
		_maxDateAnnotation = _annotationFactory.createAnnotation();
		_minDateAnnotation = _annotationFactory.createAnnotation();
		_maxTimeAnnotation = _annotationFactory.createAnnotation();
		_minTimeAnnotation = _annotationFactory.createAnnotation();
	}

	public void run(Date value, InputRow row, int distinctCount) {
		_numRows += distinctCount;
		if (value == null) {
			_annotationFactory.annotate(row, distinctCount, _nullAnnotation);
		} else {
			LocalDate localDate = new LocalDate(value);
			LocalTime localTime = new LocalTime(value);
			if (_minDate == null) {
				// first non-null value
				_minDate = localDate;
				_maxDate = localDate;
				_minTime = localTime;
				_maxTime = localTime;
			} else {
				if (localDate.isAfter(_maxDate)) {
					_maxDate = localDate;
					_annotationFactory.reset(_maxDateAnnotation);
				} else if (localDate.isBefore(_minDate)) {
					_minDate = localDate;
					_annotationFactory.reset(_minDateAnnotation);
				}

				if (localTime.isAfter(_maxTime)) {
					_maxTime = localTime;
					_annotationFactory.reset(_maxTimeAnnotation);
				} else if (localTime.isBefore(_minTime)) {
					_minTime = localTime;
					_annotationFactory.reset(_minTimeAnnotation);
				}
			}

			if (localDate.isEqual(_maxDate)) {
				_annotationFactory.annotate(row, distinctCount, _maxDateAnnotation);
			}
			if (localDate.isEqual(_minDate)) {
				_annotationFactory.annotate(row, distinctCount, _minDateAnnotation);
			}

			if (localTime.isEqual(_maxTime)) {
				_annotationFactory.annotate(row, distinctCount, _maxTimeAnnotation);
			}
			if (localTime.isEqual(_minTime)) {
				_annotationFactory.annotate(row, distinctCount, _minTimeAnnotation);
			}
		}
	}

	public LocalDate getMaxDate() {
		return _maxDate;
	}

	public LocalTime getMaxTime() {
		return _maxTime;
	}

	public LocalDate getMinDate() {
		return _minDate;
	}

	public LocalTime getMinTime() {
		return _minTime;
	}

	public int getNumRows() {
		return _numRows;
	}

	public void setNumRows(int numRows) {
		_numRows = numRows;
	}

	public RowAnnotation getNullAnnotation() {
		return _nullAnnotation;
	}

	public RowAnnotation getMaxDateAnnotation() {
		return _maxDateAnnotation;
	}

	public RowAnnotation getMinDateAnnotation() {
		return _minDateAnnotation;
	}

	public RowAnnotation getMaxTimeAnnotation() {
		return _maxTimeAnnotation;
	}

	public RowAnnotation getMinTimeAnnotation() {
		return _minTimeAnnotation;
	}

	public int getNumNull() {
		return _nullAnnotation.getRowCount();
	}

}
