package org.eobjects.analyzer.lifecycle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.util.ReflectionUtils;

public class HsqldbCollectionProvider implements CollectionProvider {

	private static final AtomicInteger _nextDatabaseId = new AtomicInteger(1);
	private final AtomicInteger _nextTableId = new AtomicInteger(1);
	private final Connection _connection;

	public HsqldbCollectionProvider() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not initialize the Hsqldb driver", e);
		}

		try {
			_connection = DriverManager.getConnection("jdbc:hsqldb:mem:analyzerbeans" + _nextDatabaseId.getAndIncrement());

			// optimize
			_connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			throw new IllegalStateException("Could not create a Hsqldb database", e);
		}
	}
	
	@Override
	protected void finalize() {
		try {
			_connection.close();
		} catch (SQLException e) {
			// nothing to do
		}
	}

	public static void performUpdate(Connection connection, String sql) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			safeClose(null, st);
		}
	}

	private String getSqlType(Class<?> valueType) {
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
	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException {
		String tableName = "ab_list_" + _nextTableId.getAndIncrement();
		String valueTypeName = getSqlType(valueType);
		performUpdate(_connection, "CREATE TABLE " + tableName + " (list_index INTEGER PRIMARY KEY, list_value "
				+ valueTypeName + ");");
		return new HsqldbList<E>(_connection, tableName);
	}

	@Override
	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException {
		String tableName = "ab_set_" + _nextTableId.getAndIncrement();
		String valueTypeName = getSqlType(valueType);
		performUpdate(_connection, "CREATE TABLE " + tableName + " (set_value " + valueTypeName + " PRIMARY KEY);");
		return new HsqldbSet<E>(_connection, tableName);
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException {
		String tableName = "ab_map_" + _nextTableId.getAndIncrement();
		String keyTypeName = getSqlType(keyType);
		String valueTypeName = getSqlType(valueType);
		performUpdate(_connection, "CREATE TABLE " + tableName + " (map_key " + keyTypeName + " PRIMARY KEY, map_value "
				+ valueTypeName + ");");
		return new HsqldbMap<K, V>(_connection, tableName);
	}

	@Override
	public void cleanUp(Object providedObj) {
		HsqldbCollection col = (HsqldbCollection) providedObj;
		performUpdate(_connection, "DROP TABLE " + col.getTableName());
	}

	public static void safeClose(ResultSet rs, Statement st) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
