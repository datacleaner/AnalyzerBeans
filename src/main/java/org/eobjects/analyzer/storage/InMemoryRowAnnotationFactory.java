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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.ImmutableEntry;

public class InMemoryRowAnnotationFactory implements RowAnnotationFactory {

	// contains annotations, mapped to row-id's, mapped to rows, mapped to
	// distinct counts (the nested maps are used to improve search speed)
	private final Map<RowAnnotation, Map<Integer, Entry<InputRow, Integer>>> _annotatedRows = new LinkedHashMap<RowAnnotation, Map<Integer, Entry<InputRow, Integer>>>();

	@Override
	public RowAnnotation createAnnotation() {
		return new RowAnnotationImpl();
	}

	protected int getInMemoryRowCount(RowAnnotation annotation) {
		Map<Integer, Entry<InputRow, Integer>> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	@Override
	public void annotate(InputRow row, int distinctRowCount, RowAnnotation annotation) {
		Map<Integer, Entry<InputRow, Integer>> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			synchronized (this) {
				rows = _annotatedRows.get(annotation);
				if (rows == null) {
					rows = new LinkedHashMap<Integer, Entry<InputRow, Integer>>();
					_annotatedRows.put(annotation, rows);
				}
			}
		}

		boolean found = rows.containsKey(row.getId());

		if (!found) {
			rows.put(row.getId(), new ImmutableEntry<InputRow, Integer>(row, distinctRowCount));
			((RowAnnotationImpl) annotation).incrementRowCount(distinctRowCount);
		}
	}

	@Override
	public InputRow[] getRows(RowAnnotation annotation) {
		if (!_annotatedRows.containsKey(annotation)) {
			return new InputRow[0];
		}
		Collection<Entry<InputRow, Integer>> rows = _annotatedRows.get(annotation).values();
		InputRow[] result = new InputRow[rows.size()];
		int i = 0;
		for (Entry<InputRow, Integer> entry : rows) {
			result[i] = entry.getKey();
			i++;
		}
		return result;
	}

	@Override
	public void reset(RowAnnotation annotation) {
		synchronized (this) {
			_annotatedRows.remove(annotation);
		}
		((RowAnnotationImpl) annotation).resetRowCount();
	}

	@Override
	public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
		HashMap<Object, Integer> map = new HashMap<Object, Integer>();
		if (!_annotatedRows.containsKey(annotation)) {
			return map;
		}

		for (Entry<InputRow, Integer> rowAndCount : _annotatedRows.get(annotation).values()) {
			Object value = rowAndCount.getKey().getValue(inputColumn);
			Integer count = map.get(value);
			if (count == null) {
				count = 0;
			}
			count = count.intValue() + rowAndCount.getValue();
			map.put(value, count);
		}
		return map;
	}

	public int getRowCount(RowAnnotation annotation, InputRow row) {
		Map<Integer, Entry<InputRow, Integer>> map = _annotatedRows.get(annotation);
		if (map == null) {
			return 0;
		}
		Entry<InputRow, Integer> entry = map.get(row.getId());
		if (entry == null) {
			return 0;
		}
		return entry.getValue();
	}
}
