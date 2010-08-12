package org.eobjects.analyzer.beans;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.result.SchemaComparisonResult;
import org.eobjects.analyzer.result.SchemaDifference;
import org.eobjects.analyzer.result.TableComparisonResult;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.CompareUtils;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

@AnalyzerBean("Compare schema structures")
public class CompareSchemasAnalyzer implements
		ExploringAnalyzer<SchemaComparisonResult> {

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
			result = new SchemaComparisonResult(differences,
					tableComparisonResults);
			return;
		}

		addDiff(differences, "name", schema1.getName(), schema2.getName());

		List<String> tableNames1 = CollectionUtils
				.list(schema1.getTableNames());
		List<String> tableNames2 = CollectionUtils
				.list(schema2.getTableNames());

		List<String> tablesOnlyInSchema1 = new ArrayList<String>(tableNames1);
		tablesOnlyInSchema1.removeAll(tableNames2);

		for (String tableName : tablesOnlyInSchema1) {
			SchemaDifference<String> diff = new SchemaDifference<String>(
					schema1, schema2, "unmatched table", tableName, null);
			differences.add(diff);
		}

		List<String> tablesOnlyInSchema2 = new ArrayList<String>(tableNames2);
		tablesOnlyInSchema2.removeAll(tableNames1);

		for (String tableName : tablesOnlyInSchema2) {
			SchemaDifference<String> diff = new SchemaDifference<String>(
					schema1, schema2, "unmatched table", null, tableName);
			differences.add(diff);
		}

		List<String> tablesInBothSchemas = new ArrayList<String>(tableNames1);
		tablesInBothSchemas.retainAll(tableNames2);

		for (String tableName : tablesInBothSchemas) {
			Table table1 = schema1.getTableByName(tableName);
			Table table2 = schema2.getTableByName(tableName);

			CompareTablesAnalyzer analyzer = new CompareTablesAnalyzer(table1,
					table2);
			analyzer.run(dc);
			TableComparisonResult tableComparisonResult = analyzer.getResult();

			if (!tableComparisonResult.isTablesEqual()) {
				tableComparisonResults.add(tableComparisonResult);
			}
		}

		// TODO: Include relatinship analysis

		result = new SchemaComparisonResult(differences, tableComparisonResults);
	}

	private <T> void addDiff(List<SchemaDifference<?>> differences,
			String valueName, T value1, T value2) {
		if (!CompareUtils.equals(value1, value2)) {
			SchemaDifference<T> diff = new SchemaDifference<T>(schema1,
					schema2, valueName, value1, value2);
			differences.add(diff);
		}
	}

}
