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
import org.eobjects.analyzer.result.SchemaComparisonResult;
import org.eobjects.analyzer.result.SchemaDifference;
import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.CompareUtils;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

@AnalyzerBean("Compare schema structures")
public class CompareSchemasAnalyzer implements ExploringAnalyzer<SchemaComparisonResult> {

	@Inject
	@Configured
	Schema schema1;

	@Inject
	@Configured
	Schema schema2;

	private SchemaComparisonResult result;

	public CompareSchemasAnalyzer(Schema schema1, Schema schema2) {
		this.schema1 = schema1;
		this.schema2 = schema2;
	}

	public CompareSchemasAnalyzer() {
	}

	@Override
	public SchemaComparisonResult getResult() {
		return result;
	}

	@Override
	public void run(DataContext dc) {
		assert schema1 != null;
		assert schema2 != null;

		List<SchemaDifference<?>> differences = new ArrayList<SchemaDifference<?>>();
		List<TableComparisonResult> tableComparisonResults = new ArrayList<TableComparisonResult>();

		if (schema1 == schema2) {
			result = new SchemaComparisonResult(differences, tableComparisonResults);
			return;
		}

		addDiff(differences, "name", schema1.getName(), schema2.getName());

		List<String> tableNames1 = CollectionUtils.list(schema1.getTableNames());
		List<String> tableNames2 = CollectionUtils.list(schema2.getTableNames());

		List<String> tablesOnlyInSchema1 = new ArrayList<String>(tableNames1);
		tablesOnlyInSchema1.removeAll(tableNames2);

		for (String tableName : tablesOnlyInSchema1) {
			SchemaDifference<String> diff = new SchemaDifference<String>(schema1, schema2, "unmatched table", tableName,
					null);
			differences.add(diff);
		}

		List<String> tablesOnlyInSchema2 = new ArrayList<String>(tableNames2);
		tablesOnlyInSchema2.removeAll(tableNames1);

		for (String tableName : tablesOnlyInSchema2) {
			SchemaDifference<String> diff = new SchemaDifference<String>(schema1, schema2, "unmatched table", null,
					tableName);
			differences.add(diff);
		}

		List<String> tablesInBothSchemas = new ArrayList<String>(tableNames1);
		tablesInBothSchemas.retainAll(tableNames2);

		for (String tableName : tablesInBothSchemas) {
			Table table1 = schema1.getTableByName(tableName);
			Table table2 = schema2.getTableByName(tableName);

			CompareTablesAnalyzer analyzer = new CompareTablesAnalyzer(table1, table2);
			analyzer.run(dc);
			TableComparisonResult tableComparisonResult = analyzer.getResult();

			if (!tableComparisonResult.isTablesEqual()) {
				tableComparisonResults.add(tableComparisonResult);
			}
		}

		// TODO: Include relationship analysis?

		result = new SchemaComparisonResult(differences, tableComparisonResults);
	}

	private <T> void addDiff(List<SchemaDifference<?>> differences, String valueName, T value1, T value2) {
		if (!CompareUtils.equals(value1, value2)) {
			SchemaDifference<T> diff = new SchemaDifference<T>(schema1, schema2, valueName, value1, value2);
			differences.add(diff);
		}
	}

}
