package org.eobjects.analyzer.job;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.job.concurrent.CompletionListener;
import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class AnalysisRowProcessor {

	private static final Logger logger = LoggerFactory
			.getLogger(AnalysisRowProcessor.class);

	private Queue<RowProcessingAnalyzer> analyzerBeansAndRunDescriptors = new LinkedBlockingQueue<RowProcessingAnalyzer>();
	private Set<Column> columns = new HashSet<Column>();
	private DataContextProvider dataContextProvider;
	private Table table;

	public AnalysisRowProcessor(DataContextProvider dataContextProvider) {
		this.dataContextProvider = dataContextProvider;
	}

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

	public void addEndPoint(RowProcessingAnalyzer analyzerBean) {
		analyzerBeansAndRunDescriptors.add(analyzerBean);
	}

	public void run() {
		logger.info("run()");
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
			logger.info("executing query: " + q);
			dataSet = dataContextProvider.getDataContext().executeQuery(q);
			int i = 0;
			while (dataSet.next()) {
				logger.info("2");
				Row row = dataSet.getRow();
				Integer count;
				Object countValue = row.getValue(countAllItem);
				if (countValue instanceof Integer) {
					count = (Integer) countValue;
				} else if (countValue instanceof Number) {
					count = ((Number) countValue).intValue();
				} else {
					count = Integer.parseInt(countValue.toString());
				}

				processRow(row, count);
				i++;
			}
			logger.info(i + " rows successfully processed");
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if (dataSet != null) {
				dataSet.close();
			}
		}
	}

	protected void processRow(Row row, Integer count) {
		for (RowProcessingAnalyzer analyzerBean : analyzerBeansAndRunDescriptors) {
			analyzerBean.run(row, count);
		}
	}

	public Task createTask(final CompletionListener completionListener) {
		return new Task() {
			@Override
			public void execute() throws Exception {
				run();
				completionListener.onComplete();
			}
		};
	}
}
