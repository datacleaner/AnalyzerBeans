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

	public Schema[] convertToSchemas(String[] schemaNames) {
		Schema[] result = new Schema[schemaNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getSchemaByName(schemaNames[i]);
		}
		return result;
	}

	public Table[] convertToTables(String[] tableNames) {
		Table[] result = new Table[tableNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getTableByQualifiedLabel(tableNames[i]);
		}
		return result;
	}

	public Column[] convertToColumns(String[] columnNames) {
		Column[] result = new Column[columnNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getColumnByQualifiedLabel(columnNames[i]);
		}
		return result;
	}
}
