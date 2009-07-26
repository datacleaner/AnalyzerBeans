package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.ExecutionType;
import org.eobjects.analyzer.annotations.Run;
import org.eobjects.analyzer.result.DataSetAnalyzerBeanResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FromItem;
import dk.eobjects.metamodel.query.JoinType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectClause;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

@AnalyzerBean(displayName = "Referential Integrity validator", execution = ExecutionType.EXPLORING)
public class ReferentialIntegrityValidator {

	private Log log = LogFactory.getLog(getClass());
	private List<Row> invalidRows;

	@Configured("Primary key column")
	Column primaryKeyColumn;

	@Configured("Foreign key column")
	Column foreignKeyColumn;

	@Configured("Accept NULL foreign keys?")
	boolean acceptNullForeignKey;

	/**
	 * Returns a query with the following select items:
	 * <ol>
	 * <li>the primary key value</li>
	 * <li>the foreign key value</li>
	 * <li>the remaining "informational" values from the columns of the foreign
	 * table</li>
	 * </ol>
	 * 
	 * @return
	 */
	public Query createQuery() {
		Table primaryTable = primaryKeyColumn.getTable();
		Table foreignTable = foreignKeyColumn.getTable();
		List<Column> informationalForeignColumns = new ArrayList<Column>();
		for (Column column : foreignTable.getColumns()) {
			if (column != foreignKeyColumn) {
				informationalForeignColumns.add(column);
			}
		}

		Query leftQuery = new Query().select(foreignKeyColumn)
				.select(
						informationalForeignColumns
								.toArray(new Column[informationalForeignColumns
										.size()])).from(foreignTable);
		Query rightQuery = new Query().select(primaryKeyColumn).from(
				primaryTable);
		if (log.isDebugEnabled()) {
			log.debug("Left query: " + leftQuery);
			log.debug("Right query: " + rightQuery);
		}

		FromItem leftSide = new FromItem(leftQuery).setAlias("a");
		FromItem rightSide = new FromItem(rightQuery).setAlias("b");

		SelectClause leftSelectClause = leftQuery.getSelectClause();
		SelectClause rightSelectClause = rightQuery.getSelectClause();

		SelectItem leftOn = leftSelectClause.getItem(0);
		SelectItem rightOn = rightSelectClause.getItem(0);

		// Create master query
		Query q = new Query().from(new FromItem(JoinType.LEFT, leftSide,
				rightSide, new SelectItem[] { leftOn },
				new SelectItem[] { rightOn }));
		for (SelectItem si : rightSelectClause.getItems()) {
			q.select(new SelectItem(si, rightSide));
		}
		for (SelectItem si : leftSelectClause.getItems()) {
			q.select(new SelectItem(si, leftSide));
		}
		return q;
	}

	@Run
	public void run(DataContext dc) {
		invalidRows = new ArrayList<Row>();

		Query q = createQuery();

		SelectItem foreignKeySelectItem = q.getSelectClause().getItem(1);
		SelectItem primaryKeySelectItem = q.getSelectClause().getItem(0);

		DataSet dataSet = dc.executeQuery(q);
		while (dataSet.next()) {
			Row row = dataSet.getRow();

			Object foreignKey = row.getValue(foreignKeySelectItem);
			if (foreignKey == null) {
				if (acceptNullForeignKey) {
					if (log.isInfoEnabled()) {
						log.info("Accepting row with NULL primary key: " + row);
					}
				} else {
					invalidRows.add(row);
				}
			} else {
				Object primaryKey = row.getValue(primaryKeySelectItem);

				if (primaryKey == null) {
					invalidRows.add(row);
				} else if (!primaryKey.equals(foreignKey)) {
					if (log.isWarnEnabled()) {
						log
								.warn("Unexpected state! Primary and foreign key values are not null and different! PK="
										+ primaryKey + ", FK=" + foreignKey);
					}
					invalidRows.add(row);
				}
			}

		}
		dataSet.close();
	}

	public void setAcceptNullForeignKey(boolean acceptNullForeignKey) {
		this.acceptNullForeignKey = acceptNullForeignKey;
	}

	public void setPrimaryKeyColumn(Column idColumn) {
		this.primaryKeyColumn = idColumn;
	}

	public void setForeignKeyColumn(Column parentIdColumn) {
		this.foreignKeyColumn = parentIdColumn;
	}

	public DataSetAnalyzerBeanResult invalidRows() {
		return new DataSetAnalyzerBeanResult(invalidRows, getClass());
	}
}
