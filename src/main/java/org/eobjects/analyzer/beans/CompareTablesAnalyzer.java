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

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.result.ColumnComparisonResult;
import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.result.TableDifference;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Compare table structures")
public class CompareTablesAnalyzer implements ExploringAnalyzer<TableComparisonResult> {

	private static final Logger logger = LoggerFactory.getLogger(TableComparisonResult.class);

	@Inject
	@Configured
	Table table1;

	@Inject
	@Configured
	Table table2;

	private TableComparisonResult result;
	private boolean relationshipsAnalyzed;

	public CompareTablesAnalyzer(Table table1, Table table2) {
		this.table1 = table1;
		this.table2 = table2;
		this.relationshipsAnalyzed = false;
	}

	public CompareTablesAnalyzer() {
		this.relationshipsAnalyzed = true;
	}

	@Override
	public void run(DataContext dc) {
		assert table1 != null;
		assert table2 != null;

		List<TableDifference<?>> differences = new ArrayList<TableDifference<?>>();
		List<ColumnComparisonResult> columnComparisonResults = new ArrayList<ColumnComparisonResult>();

		if (table1 == table2) {
			result = new TableComparisonResult(differences, columnComparisonResults);
			return;
		}

		addDiff(differences, "name", table1.getName(), table2.getName());
		addDiff(differences, "type", table1.getType(), table2.getType());
		addDiff(differences, "remarks", table1.getRemarks(), table2.getRemarks());

		List<String> columnNames1 = CollectionUtils.list(table1.getColumnNames());
		List<String> columnNames2 = CollectionUtils.list(table2.getColumnNames());

		List<String> columnsOnlyInTable1 = new ArrayList<String>(columnNames1);
		columnsOnlyInTable1.removeAll(columnNames2);

		for (String columnName : columnsOnlyInTable1) {
			TableDifference<String> diff = new TableDifference<String>(table1, table2, "unmatched column", columnName, null);
			differences.add(diff);
		}

		List<String> columnsOnlyInTable2 = new ArrayList<String>(columnNames2);
		columnsOnlyInTable2.removeAll(columnNames1);

		for (String columnName : columnsOnlyInTable2) {
			TableDifference<String> diff = new TableDifference<String>(table1, table2, "unmatched column", null, columnName);
			differences.add(diff);
		}

		List<String> columnsInBothTables = new ArrayList<String>(columnNames1);
		columnsInBothTables.retainAll(columnNames2);
		for (String columnName : columnsInBothTables) {
			Column column1 = table1.getColumnByName(columnName);
			Column column2 = table2.getColumnByName(columnName);
			CompareColumnsAnalyzer analyzer = new CompareColumnsAnalyzer(column1, column2);
			analyzer.run(dc);
			ColumnComparisonResult columnComparisonResult = analyzer.getResult();
			if (!columnComparisonResult.isColumnsEqual()) {
				columnComparisonResults.add(columnComparisonResult);
			}
		}

		if (isRelationshipsAnalyzed()) {
			// TODO: Include relationship analysis?
		} else {
			logger.debug("Skipping relationship analysis");
		}

		result = new TableComparisonResult(differences, columnComparisonResults);
	}

	private <T> void addDiff(List<TableDifference<?>> differences, String valueName, T value1, T value2) {
		if (!EqualsBuilder.equals(value1, value2)) {
			TableDifference<T> diff = new TableDifference<T>(table1, table2, valueName, value1, value2);
			differences.add(diff);
		}
	}

	@Override
	public TableComparisonResult getResult() {
		return result;
	}

	public boolean isRelationshipsAnalyzed() {
		return relationshipsAnalyzed;
	}

	public void setRelationshipsAnalyzed(boolean relationshipsAnalyzed) {
		this.relationshipsAnalyzed = relationshipsAnalyzed;
	}

}
