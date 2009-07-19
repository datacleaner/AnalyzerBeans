package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.ExecutionType;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;
import org.eobjects.analyzer.result.DataSetAnalyzerBeanResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.FromItem;
import dk.eobjects.metamodel.query.JoinType;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

@AnalyzerBean(displayName = "Join matcher", execution = ExecutionType.EXPLORING)
public class JoinMatcher {

	@Configured
	private Table leftTable;

	public void setLeftTable(Table leftTable) {
		this.leftTable = leftTable;
	}

	@Configured
	private Table rightTable;

	public void setRightTable(Table rightTable) {
		this.rightTable = rightTable;
	}

	@Configured
	private Column leftTableJoinColumn;

	public void setLeftTableJoinColumn(Column leftTableJoinColumn) {
		this.leftTableJoinColumn = leftTableJoinColumn;
	}

	@Configured
	private Column rightTableJoinColumn;

	public void setRightTableJoinColumn(Column rightTableJoinColumn) {
		this.rightTableJoinColumn = rightTableJoinColumn;
	}

	private List<Row> unmatchedRows = new ArrayList<Row>();

	@Run
	public void run(DataContext dc) {
		DataSet ds;
		Schema leftSchema = leftTable.getSchema();
		Schema rightSchema = rightTable.getSchema();
		if (leftSchema == rightSchema || leftSchema.equals(rightSchema)) {
			FromItem joinFromItem = new FromItem(JoinType.LEFT, new FromItem(
					leftTable), new FromItem(rightTable),
					new SelectItem[] { new SelectItem(leftTableJoinColumn) },
					new SelectItem[] { new SelectItem(rightTableJoinColumn) });
			Query q = new Query().select(leftTable.getColumns()).select(
					rightTable.getColumns()).from(joinFromItem);
			ds = dc.executeQuery(q);
		} else {
			DataSet ds1 = dc.executeQuery(new Query().select(
					leftTable.getColumns()).from(leftTable));
			DataSet ds2 = dc.executeQuery(new Query().select(
					rightTable.getColumns()).from(rightTable));
			FilterItem[] onConditions = new FilterItem[] { new FilterItem(
					new SelectItem(leftTableJoinColumn),
					OperatorType.EQUALS_TO,
					new SelectItem(rightTableJoinColumn)) };
			ds = MetaModelHelper.getLeftJoin(ds1, ds2, onConditions);
		}

		while (ds.next()) {
			Row row = ds.getRow();
			if (row.getValue(rightTableJoinColumn) == null) {
				unmatchedRows.add(row);
			}
		}
	}

	@Result
	public DataSetAnalyzerBeanResult getUnmatchedRows() {
		return new DataSetAnalyzerBeanResult(unmatchedRows, getClass());
	}
}
