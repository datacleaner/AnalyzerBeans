package org.eobjects.analyzer.job;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.descriptors.RunDescriptor;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class AnalysisRowProcessor {

	private List<Object[]> analyzerBeansAndRunDescriptors = new LinkedList<Object[]>();
	private Set<Column> columns = new HashSet<Column>();;
	private Table table;

	public void addColumns(Column... newColumns) {
		for (Column column : newColumns) {
			if (table == null) {
				table = column.getTable();
			} else if (!table.equals(column.getTable())) {
				throw new IllegalArgumentException(
						"Cannot add columns from different tables to the same RowProcessor");
			}
			columns.add(column);
		}
	}

	public void addEndPoint(Object analyzerBean, RunDescriptor runDescriptor) {
		analyzerBeansAndRunDescriptors.add(new Object[] { analyzerBean,
				runDescriptor });
	}

	public void run(DataContext dataContext) {
		if (table == null) {
			throw new IllegalStateException(
					"No table and no columns defined to process");
		}

		Column[] columnArray = columns.toArray(new Column[columns.size()]);
		Query q = new Query();
		q.select(columnArray);
		SelectItem countAllItem = SelectItem.getCountAllItem();
		q.select(countAllItem);
		q.from(table);
		q.groupBy(columnArray);

		DataSet dataSet = null;
		try {
			dataSet = dataContext.executeQuery(q);
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				Long count;
				Object countValue = row.getValue(countAllItem);
				if (countValue instanceof Long) {
					count = (Long) countValue;
				} else if (countValue instanceof Number) {
					count = ((Number) countValue).longValue();
				} else {
					count = new Long(countValue.toString());
				}

				processRow(row, count);
			}
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if (dataSet != null) {
				dataSet.close();
			}
		}
	}

	protected void processRow(Row row, Long count) {
		for (Object[] obj : analyzerBeansAndRunDescriptors) {
			Object analyzerBean = obj[0];
			RunDescriptor runDescriptor = (RunDescriptor) obj[1];
			runDescriptor.processRow(analyzerBean, row, count);
		}
	}
}
