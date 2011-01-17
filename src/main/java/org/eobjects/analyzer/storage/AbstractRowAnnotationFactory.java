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
package org.eobjects.analyzer.storage;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.h2.util.SoftHashMap;

/**
 * An abstract RowAnnotationFactory that supports a (optional) threshold
 * 
 * @author Kasper Sørensen
 * 
 */
public abstract class AbstractRowAnnotationFactory implements RowAnnotationFactory {

	private final Map<RowAnnotationImpl, AtomicInteger> _rowCounts = new IdentityHashMap<RowAnnotationImpl, AtomicInteger>();
	private final SoftHashMap<Integer, Boolean> _cachedRows = new SoftHashMap<Integer, Boolean>();
	private final Integer _storedRowsThreshold;

	public AbstractRowAnnotationFactory(Integer storedRowsThreshold) {
		if (storedRowsThreshold == null) {
			_storedRowsThreshold = Integer.MAX_VALUE;
		} else {
			_storedRowsThreshold = storedRowsThreshold;
		}
	}

	@Override
	public final void annotate(InputRow row, int distinctCount, RowAnnotation annotation) {
		final RowAnnotationImpl ann = (RowAnnotationImpl) annotation;

		AtomicInteger count = _rowCounts.get(ann);
		if (count == null) {
			synchronized (_rowCounts) {
				count = _rowCounts.get(ann);
				if (count == null) {
					count = new AtomicInteger();
					_rowCounts.put(ann, count);
				}
			}
		}

		boolean storeRow = true;
		if (_storedRowsThreshold != null) {
			if (count.getAndIncrement() >= _storedRowsThreshold.intValue()) {
				storeRow = false;
			}
		}

		if (storeRow) {
			int rowId = row.getId();
			if (!_cachedRows.containsKey(rowId)) {
				synchronized (_cachedRows) {
					if (!_cachedRows.containsKey(rowId)) {
						storeRowValues(rowId, row, distinctCount);
						_cachedRows.put(rowId, Boolean.TRUE);
					}
				}
			}
			storeRowAnnotation(rowId, annotation);
		}

		ann.incrementRowCount(distinctCount);
	}

	@Override
	public final void reset(RowAnnotation annotation) {
		RowAnnotationImpl ann = (RowAnnotationImpl) annotation;
		ann.resetRowCount();
		resetRows(annotation);
	}

	@Override
	public final RowAnnotation createAnnotation() {
		RowAnnotationImpl ann = new RowAnnotationImpl();
		return ann;
	}

	@Override
	public final Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
		HashMap<Object, Integer> map = new HashMap<Object, Integer>();

		InputRow[] rows = getRows(annotation);

		if (rows == null || rows.length == 0) {
			return map;
		}

		for (InputRow row : rows) {
			Object value = row.getValue(inputColumn);
			Integer count = map.get(value);
			if (count == null) {
				count = 0;
			}
			count = count.intValue() + getDistinctCount(row);
			map.put(value, count);
		}
		return map;
	}

	/**
	 * Removes the annotation from any rows that has been annotated with it.
	 * 
	 * @param annotation
	 */
	protected abstract void resetRows(RowAnnotation annotation);

	/**
	 * Gets the distinct count from a row that has been stored and retried using
	 * the getRows(...) method.
	 * 
	 * @param row
	 * @return
	 */
	protected abstract int getDistinctCount(InputRow row);

	protected abstract void storeRowAnnotation(int rowId, RowAnnotation annotation);

	protected abstract void storeRowValues(int rowId, InputRow row, int distinctCount);

	public final Integer getStoredRowsThreshold() {
		return _storedRowsThreshold;
	}
}
