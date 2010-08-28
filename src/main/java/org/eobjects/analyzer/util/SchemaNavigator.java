package org.eobjects.analyzer.util;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public final class SchemaNavigator {

	private final DataContext dataContext;

	public SchemaNavigator(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	public Schema convertToSchema(String schemaName) {
		return dataContext.getSchemaByName(schemaName);
	}

	public Schema[] convertToSchemas(String[] schemaNames) {
		Schema[] result = new Schema[schemaNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = convertToSchema(schemaNames[i]);
		}
		return result;
	}

	public Table[] convertToTables(String[] tableNames) {
		Table[] result = new Table[tableNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = convertToTable(tableNames[i]);
		}
		return result;
	}

	public Table convertToTable(String tableName) {
		return dataContext.getTableByQualifiedLabel(tableName);
	}

	public Column[] convertToColumns(String[] columnNames) {
		Column[] result = new Column[columnNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = convertToColumn(columnNames[i]);
		}
		return result;
	}

	public Column convertToColumn(String columnName) {
		return dataContext.getColumnByQualifiedLabel(columnName);
	}
}
