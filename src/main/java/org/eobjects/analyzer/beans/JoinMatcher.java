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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.result.DataSetResult;

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

/**
 * Matches values from separate tables and returns rows where the values does
 * not match
 * 
 * @author Kasper Sørensen
 */
@AnalyzerBean("Join matcher")
public class JoinMatcher implements ExploringAnalyzer<DataSetResult> {

	@Configured(order = 1)
	private Table leftTable;

	public void setLeftTable(Table leftTable) {
		this.leftTable = leftTable;
	}

	@Configured(order = 2)
	private Table rightTable;

	public void setRightTable(Table rightTable) {
		this.rightTable = rightTable;
	}

	@Configured(order = 3)
	private Column leftTableJoinColumn;

	public void setLeftTableJoinColumn(Column leftTableJoinColumn) {
		this.leftTableJoinColumn = leftTableJoinColumn;
	}

	@Configured(order = 4)
	private Column rightTableJoinColumn;

	public void setRightTableJoinColumn(Column rightTableJoinColumn) {
		this.rightTableJoinColumn = rightTableJoinColumn;
	}

	private List<Row> unmatchedRows = new ArrayList<Row>();

	@Override
	public void run(DataContext dc) {
		DataSet ds;
		Schema leftSchema = leftTable.getSchema();
		Schema rightSchema = rightTable.getSchema();
		if (leftSchema == rightSchema || leftSchema.equals(rightSchema)) {
			FromItem joinFromItem = new FromItem(JoinType.LEFT, new FromItem(leftTable), new FromItem(rightTable),
					new SelectItem[] { new SelectItem(leftTableJoinColumn) }, new SelectItem[] { new SelectItem(
							rightTableJoinColumn) });
			Query q = new Query().select(leftTable.getColumns()).select(rightTable.getColumns()).from(joinFromItem);
			ds = dc.executeQuery(q);
		} else {
			DataSet ds1 = dc.executeQuery(new Query().select(leftTable.getColumns()).from(leftTable));
			DataSet ds2 = dc.executeQuery(new Query().select(rightTable.getColumns()).from(rightTable));
			FilterItem[] onConditions = new FilterItem[] { new FilterItem(new SelectItem(leftTableJoinColumn),
					OperatorType.EQUALS_TO, new SelectItem(rightTableJoinColumn)) };
			ds = MetaModelHelper.getLeftJoin(ds1, ds2, onConditions);
		}

		while (ds.next()) {
			Row row = ds.getRow();
			if (row.getValue(rightTableJoinColumn) == null) {
				unmatchedRows.add(row);
			}
		}
	}

	@Override
	public DataSetResult getResult() {
		return new DataSetResult(unmatchedRows);
	}
}
