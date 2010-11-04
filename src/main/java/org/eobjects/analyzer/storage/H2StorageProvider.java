package org.eobjects.analyzer.storage;

import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * H2 based implementation of the StorageProvider.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class H2StorageProvider extends SqlDatabaseStorageProvider implements StorageProvider {

	public H2StorageProvider() {
		super("org.h2.Driver", "jdbc:h2:mem:");
	}

	@Override
	protected String getSqlType(Class<?> valueType) {
		if (String.class == valueType) {
			return "VARCHAR";
		}
		if (Integer.class == valueType) {
			return "INTEGER";
		}
		if (Long.class == valueType) {
			return "BIGINT";
		}
		if (Double.class == valueType) {
			return "DOUBLE";
		}
		if (Short.class == valueType) {
			return "SHORT";
		}
		if (Float.class == valueType) {
			return "FLOAT";
		}
		if (Character.class == valueType) {
			return "CHAR";
		}
		if (Boolean.class == valueType) {
			return "BOOLEAN";
		}
		if (Byte.class == valueType) {
			return "BINARY";
		}
		if (ReflectionUtils.isByteArray(valueType)) {
			return "BLOB";
		}
		throw new UnsupportedOperationException("Unsupported value type: " + valueType);
	}

	@Override
	public RowAnnotationFactory createRowAnnotationFactory() {
		// TODO: Create a persistent RowAnnotationFactory
		return new InMemoryRowAnnotationFactory();
	}

}
