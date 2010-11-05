package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class SqlDatabaseMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, SqlDatabaseCollection {

	private final Connection _connection;
	private final String _tableName;
	private volatile int _size;

	public SqlDatabaseMap(Connection connection, String tableName, String keyTypeName, String valueTypeName) {
		_connection = connection;
		_tableName = tableName;

		SqlDatabaseUtils.performUpdate(_connection, SqlDatabaseUtils.CREATE_TABLE_PREFIX + tableName + " (map_key "
				+ keyTypeName + " PRIMARY KEY, map_value " + valueTypeName + ")");
	}

	@Override
	public int size() {
		return _size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			st = _connection.prepareStatement("SELECT map_value FROM " + _tableName + " WHERE map_key = ?;");
			st.setObject(1, key);
			rs = st.executeQuery();
			if (rs.next()) {
				return (V) rs.getObject(1);
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, st);
		}
	}

	@Override
	public boolean containsKey(Object key) {
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			st = _connection.prepareStatement("SELECT COUNT(*) FROM  " + _tableName + " WHERE map_key = ?");
			st.setObject(1, key);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
			return false;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, st);
		}
	}

	public synchronized V put(K key, V value) {
		PreparedStatement st = null;
		try {
			if (containsKey(key)) {
				V v = get(key);
				st = _connection.prepareStatement("UPDATE " + _tableName + " SET map_value = ? WHERE map_key = ?;");
				st.setObject(1, value);
				st.setObject(2, key);
				st.executeUpdate();
				return v;
			} else {
				st = _connection.prepareStatement("INSERT INTO  " + _tableName + " VALUES (?,?);");
				st.setObject(1, key);
				st.setObject(2, value);
				st.executeUpdate();
				_size++;
				return null;
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(null, st);
		}
	};

	@Override
	public synchronized V remove(Object key) {
		if (containsKey(key)) {
			V result = get(key);
			PreparedStatement st = null;
			try {
				st = _connection.prepareStatement("DELETE FROM " + _tableName + " WHERE map_key = ?");
				st.setObject(1, key);
				st.executeUpdate();
				_size--;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			} finally {
				SqlDatabaseUtils.safeClose(null, st);
			}
			return result;
		}
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			Set<Entry<K, V>> result = new HashSet<Map.Entry<K, V>>();
			st = _connection.prepareStatement("SELECT map_key FROM " + _tableName + " ORDER BY map_key ASC;");
			rs = st.executeQuery();
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
