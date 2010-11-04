package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for storage providers that use an SQL database as a backend to
 * store values.
 * 
 * @author Kasper Sørensen
 */
public abstract class SqlDatabaseStorageProvider implements StorageProvider {

	private static final Logger logger = LoggerFactory.getLogger(SqlDatabaseStorageProvider.class);
	private final AtomicInteger _nextTableId = new AtomicInteger(1);
	private final Connection _connection;

	public SqlDatabaseStorageProvider(String driverClassName, String connectionUrl) {
		this(driverClassName, connectionUrl, null, null);
	}

	public SqlDatabaseStorageProvider(String driverClassName, String connectionUrl, String username, String password) {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not initialize the Hsqldb driver", e);
		}

		try {
			if (username != null) {
				_connection = DriverManager.getConnection(connectionUrl, username, password);
			} else {
				_connection = DriverManager.getConnection(connectionUrl);
			}

			// optimize
			_connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			throw new IllegalStateException("Could not open connection to database: " + connectionUrl, e);
		}
	}

	protected Connection getConnection() {
		return _connection;
	}

	@Override
	protected void finalize() {
		try {
			_connection.close();
		} catch (SQLException e) {
			// nothing to do
		}
	}

	protected String getSqlType(Class<?> valueType) {
		if (String.class == valueType) {
			return "VARCHAR";
		}
		if (Number.class == valueType) {
			return "DOUBLE";
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
		if (ReflectionUtils.isDate(valueType)) {
			return "DATE";
		}
		if (ReflectionUtils.isByteArray(valueType)) {
			return "BLOB";
		}
		throw new UnsupportedOperationException("Unsupported value type: " + valueType);
	}

	@Override
	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException {
		String tableName = "ab_list_" + _nextTableId.getAndIncrement();
		String valueTypeName = getSqlType(valueType);
		logger.info("Creating table {} for List", tableName);
		return new SqlDatabaseList<E>(_connection, tableName, valueTypeName);
	}

	@Override
	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException {
		String tableName = "ab_set_" + _nextTableId.getAndIncrement();
		String valueTypeName = getSqlType(valueType);
		logger.info("Creating table {} for Set", tableName);
		return new SqlDatabaseSet<E>(_connection, tableName, valueTypeName);
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException {
		String tableName = "ab_map_" + _nextTableId.getAndIncrement();
		String keyTypeName = getSqlType(keyType);
		String valueTypeName = getSqlType(valueType);
		logger.info("Creating table {} for Map", tableName);
		return new SqlDatabaseMap<K, V>(_connection, tableName, keyTypeName, valueTypeName);
	}

	@Override
	public final RowAnnotationFactory createRowAnnotationFactory() {
		String tableName = "ab_row_annotations_" + _nextTableId.getAndIncrement();
		logger.info("Creating table {} for RowAnnotationFactory", tableName);
		return new SqlDatabaseRowAnnotationFactory(_connection, tableName, this);
	}
}
