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
		Schema schema = null;
		String[] schemaNames = dataContext.getSchemaNames();
		for (String schemaName : schemaNames) {
			if (columnName.startsWith(schemaName)) {
				schema = dataContext.getSchemaByName(schemaName);
				String tableAndColumnPath = columnName.substring(schemaName
						.length());

				assert tableAndColumnPath.charAt(0) == '.';

				tableAndColumnPath = tableAndColumnPath.substring(1);

				Column column = getColumn(schema, tableAndColumnPath);
				if (column != null) {
					return column;
				}
			}
		}

		schema = dataContext.getDefaultSchema();
		if (schema != null) {
			Column column = getColumn(schema, columnName);
			if (column != null) {
				return column;
			}
		}

		return null;
	}

	private Column getColumn(Schema schema, final String tableAndColumnPath) {
		Table table = null;
		String columnPath = tableAndColumnPath;
		String[] tableNames = schema.getTableNames();
		for (String tableName : tableNames) {
			if (tableAndColumnPath.startsWith(tableName)) {
				table = schema.getTableByName(tableName);
				columnPath = tableAndColumnPath.substring(tableName.length());

				assert columnPath.charAt(0) == '.';

				columnPath = columnPath.substring(1);
				break;
			}
		}
		if (table == null && tableNames.length == 1) {
			table = schema.getTables()[0];
		}

		if (table != null) {
			Column column = table.getColumnByName(columnPath);
			if (column != null) {
				return column;
			}
		}

		return null;
	}
}
