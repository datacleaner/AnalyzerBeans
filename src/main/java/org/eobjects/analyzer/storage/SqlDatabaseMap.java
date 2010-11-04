package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class SqlDatabaseMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, SqlDatabaseCollection {

	private final Connection _connection;
	private final String _tableName;
	private final PreparedStatement _getStatement;
	private final PreparedStatement _addStatement;
	private final PreparedStatement _containsKeyStatement;
	private final PreparedStatement _updateStatement;
	private final PreparedStatement _deleteStatement;
	private volatile int _size;

	public SqlDatabaseMap(Connection connection, String tableName, String keyTypeName, String valueTypeName) {
		_connection = connection;
		_tableName = tableName;

		SqlDatabaseUtils.performUpdate(_connection, "CREATE TABLE " + tableName + " (map_key " + keyTypeName
				+ " PRIMARY KEY, map_value " + valueTypeName + ");");

		try {
			_getStatement = _connection.prepareStatement("SELECT map_value FROM " + _tableName + " WHERE map_key = ?;");
			_addStatement = _connection.prepareStatement("INSERT INTO  " + _tableName + " VALUES (?,?);");
			_updateStatement = _connection
					.prepareStatement("UPDATE " + _tableName + " SET map_value = ? WHERE map_key = ?;");
			_containsKeyStatement = _connection.prepareStatement("SELECT COUNT(*) FROM  " + _tableName
					+ " WHERE map_key = ?;");
			_deleteStatement = _connection.prepareStatement("DELETE FROM " + _tableName + " WHERE map_key = ?;");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int size() {
		return _size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		ResultSet rs = null;
		try {
			_getStatement.setObject(1, key);
			rs = _getStatement.executeQuery();
			if (rs.next()) {
				return (V) rs.getObject(1);
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, null);
		}
	}

	@Override
	public boolean containsKey(Object key) {
		ResultSet rs = null;
		try {
			_containsKeyStatement.setObject(1, key);
			rs = _containsKeyStatement.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
			return false;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, null);
		}
	}

	public synchronized V put(K key, V value) {
		try {
			if (containsKey(key)) {
				V v = get(key);
				_updateStatement.setObject(1, value);
				_updateStatement.setObject(2, key);
				_updateStatement.executeUpdate();
				return v;
			} else {
				_addStatement.setObject(1, key);
				_addStatement.setObject(2, value);
				_addStatement.executeUpdate();
				_size++;
				return null;
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	};

	@Override
	public synchronized V remove(Object key) {
		if (containsKey(key)) {
			V result = get(key);
			try {
				_deleteStatement.setObject(1, key);
				_deleteStatement.executeUpdate();
				_size--;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
			return result;
		}
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Statement st = null;
		ResultSet rs = null;
		try {
			Set<Entry<K, V>> result = new HashSet<Map.Entry<K, V>>();
			st = _connection.createStatement();
			rs = st.executeQuery("SELECT map_key FROM " + _tableName + " ORDER BY map_key ASC;");
			while (rs.next()) {
				@SuppressWarnings("unchecked")
				K key = (K) rs.getObject(1);
				result.add(new HsqldbEntry(key));
			}
			return result;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, st);
		}
	}

	@Override
	public String getTableName() {
		return _tableName;
	}

	private class HsqldbEntry implements Entry<K, V> {

		private final K _key;

		public HsqldbEntry(K key) {
			_key = key;
		}

		@Override
		public K getKey() {
			return _key;
		}

		@Override
		public V getValue() {
			return get(_key);
		}

		@Override
		public V setValue(V value) {
			return put(_key, value);
		}

		@Override
		public int hashCode() {
			return _key.hashCode();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + getTableName());
	}
}
