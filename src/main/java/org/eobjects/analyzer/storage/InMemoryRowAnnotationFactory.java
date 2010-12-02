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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.ImmutableEntry;

public class InMemoryRowAnnotationFactory extends AbstractRowAnnotationFactory implements RowAnnotationFactory {

	// contains annotations, mapped to row-ids
	private final Map<RowAnnotation, Set<Integer>> _annotatedRows = new LinkedHashMap<RowAnnotation, Set<Integer>>();

	// contains row id's mapped to rows mapped to distinct counts
	private final Map<Integer, Map.Entry<InputRow, Integer>> _distinctCounts = new LinkedHashMap<Integer, Map.Entry<InputRow, Integer>>();

	public InMemoryRowAnnotationFactory() {
		super(1000);
	}

	protected int getInMemoryRowCount(RowAnnotation annotation) {
		Set<Integer> rows = _annotatedRows.get(annotation);
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	@Override
	protected void resetRows(RowAnnotation annotation) {
		_annotatedRows.remove(annotation);
	}

	@Override
	protected int getDistinctCount(InputRow row) {
		return _distinctCounts.get(row.getId()).getValue();
	}

	@Override
	protected void storeRowAnnotation(int rowId, RowAnnotation annotation) {
		Set<Integer> rowIds = _annotatedRows.get(annotation);
		if (rowIds == null) {
			rowIds = new LinkedHashSet<Integer>();
			_annotatedRows.put(annotation, rowIds);
		}
		rowIds.add(rowId);
	}

	@Override
	protected void storeRowValues(int rowId, InputRow row, int distinctCount) {
		_distinctCounts.put(rowId, new ImmutableEntry<InputRow, Integer>(row, distinctCount));
	}

	@Override
	public InputRow[] getRows(RowAnnotation annotation) {
		Set<Integer> rowIds = _annotatedRows.get(annotation);
		if (rowIds == null) {
			return new InputRow[0];
		}
		InputRow[] rows = new InputRow[rowIds.size()];
		int i = 0;
		for (Integer rowId : rowIds) {
			rows[i] = _distinctCounts.get(rowId).getKey();
			i++;
		}
		return rows;
	}
}
