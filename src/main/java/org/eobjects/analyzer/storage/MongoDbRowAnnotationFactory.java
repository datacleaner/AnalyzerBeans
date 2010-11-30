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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoDbRowAnnotationFactory extends AbstractRowAnnotationFactory implements RowAnnotationFactory {

	private static final Logger logger = LoggerFactory.getLogger(MongoDbRowAnnotationFactory.class);
	private static final String ROW_ID_KEY = "row_id";
	private static final String DISTINCT_COUNT_KEY = "count";

	private final Map<InputColumn<?>, String> _inputColumnNames = new LinkedHashMap<InputColumn<?>, String>();
	private final Map<RowAnnotation, String> _annotationColumnNames = new HashMap<RowAnnotation, String>();
	private final InputColumn<Integer> distinctCountColumn = new MockInputColumn<Integer>("COUNT(*)", Integer.class);
	private final AtomicInteger _nextColumnIndex = new AtomicInteger(1);
	private final DBCollection _dbCollection;

	public MongoDbRowAnnotationFactory(DBCollection dbCollection) {
		super(1000);
		logger.info("Creating new MongoDB RowAnnotationFactory collection: {}", dbCollection.getName());
		_dbCollection = dbCollection;
		_dbCollection.createIndex(new BasicDBObject(ROW_ID_KEY, 1));
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("Dropping unused MongoDB collection: {}", _dbCollection.getName());
		_dbCollection.drop();
	}

	private String getColumnName(InputColumn<?> inputColumn) {
		String columnName = _inputColumnNames.get(inputColumn);
		if (columnName == null) {
			int index = _nextColumnIndex.getAndIncrement();
			if (inputColumn.isPhysicalColumn()) {
				columnName = "source" + index;
			} else {
				columnName = "trans" + index;
			}
			_inputColumnNames.put(inputColumn, columnName);
		}
		return columnName;
	}

	private String getColumnName(RowAnnotation annotation) {
		String columnName = _annotationColumnNames.get(annotation);
		if (columnName == null) {
			int index = _nextColumnIndex.getAndIncrement();
			columnName = "annot" + index;
			_annotationColumnNames.put(annotation, columnName);
		}
		return columnName;
	}

	@Override
	public InputRow[] getRows(RowAnnotation annotation) {
		final String annotationColumnName = getColumnName(annotation);

		final BasicDBObject query = new BasicDBObject();
		query.put(annotationColumnName, true);

		logger.info("Finding all rows with annotation column: {}", annotationColumnName);
		final DBCursor cursor = _dbCollection.find(query);
		InputRow[] rows = new InputRow[cursor.size()];

		int i = 0;
		while (cursor.hasNext()) {
			DBObject dbRow = cursor.next();
			Object rowId = dbRow.get(ROW_ID_KEY);
			Object distinctCount = dbRow.get(DISTINCT_COUNT_KEY);
			MockInputRow row;

			if (rowId != null) {
				row = new MockInputRow(((Number) rowId).intValue());
			} else {
				row = new MockInputRow();
			}
			row.put(distinctCountColumn, distinctCount);

			Set<Entry<InputColumn<?>, String>> columnEntries = _inputColumnNames.entrySet();
			for (Entry<InputColumn<?>, String> entry : columnEntries) {
				InputColumn<?> column = entry.getKey();
				String columnName = entry.getValue();
				Object value = dbRow.get(columnName);
				if (value != null) {
					row.put(column, value);
				}
			}

			rows[i] = row;
			i++;
		}

		logger.info("Returning {} distinct rows", rows.length);

		return rows;
	}

	@Override
	protected void resetRows(RowAnnotation annotation) {
		final String annotationColumnName = getColumnName(annotation);
		logger.info("Resetting rows with annotation column: " + annotationColumnName);

		final BasicDBObject query = new BasicDBObject().append(annotationColumnName, true);
		_dbCollection.update(query, new BasicDBObject("$unset", new BasicDBObject().append(annotationColumnName, 1)), false,
				true);
	}

	@Override
	protected int getDistinctCount(InputRow row) {
		return row.getValue(distinctCountColumn);
	}

	@Override
	protected void storeRowAnnotation(int rowId, RowAnnotation annotation) {
		final String annotationColumnName = getColumnName(annotation);
		final BasicDBObject query = new BasicDBObject().append(ROW_ID_KEY, rowId);
		_dbCollection.update(query, new BasicDBObject("$set", new BasicDBObject(annotationColumnName, true)));
	}

	@Override
	protected void storeRowValues(int rowId, InputRow row, int distinctCount) {
		BasicDBObject dbRow = new BasicDBObject();
		dbRow.put(ROW_ID_KEY, rowId);
		List<InputColumn<?>> inputColumns = row.getInputColumns();
		for (InputColumn<?> inputColumn : inputColumns) {
			dbRow.put(getColumnName(inputColumn), row.getValue(inputColumn));
		}
		dbRow.put(DISTINCT_COUNT_KEY, distinctCount);
		_dbCollection.insert(dbRow);
	}

}
