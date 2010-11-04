package org.eobjects.analyzer.storage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

final class SqlDatabaseSet<E> extends AbstractSet<E> implements Set<E>, SqlDatabaseCollection {

	private final Connection _connection;
	private final String _tableName;
	private final CallableStatement _iteratorStatement;
	private final CallableStatement _addStatement;
	private final CallableStatement _containsStatement;
	private final CallableStatement _deleteStatement;
	private volatile int _size;

	public SqlDatabaseSet(Connection connection, String tableName, String valueTypeName) {
		_connection = connection;
		_tableName = tableName;

		SqlDatabaseUtils.performUpdate(_connection, "CREATE TABLE " + tableName + " (set_value " + valueTypeName
				+ " PRIMARY KEY)");

		try {
			_iteratorStatement = _connection.prepareCall("SELECT set_value FROM " + _tableName);
			_containsStatement = _connection.prepareCall("SELECT COUNT(*) FROM " + _tableName + " WHERE set_value=?");
			_addStatement = _connection.prepareCall("INSERT INTO " + _tableName + " VALUES(?)");
			_deleteStatement = _connection.prepareCall("DELETE FROM " + _tableName + " WHERE set_value=?");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	public synchronized boolean add(E elem) {
		if (contains(elem)) {
			return false;
		}
		try {
			_addStatement.setObject(1, elem);
			_addStatement.executeUpdate();
			_size++;
			return true;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	};

	@Override
	public boolean remove(Object o) {
		if (!contains(o)) {
			return false;
		}

		try {
			_deleteStatement.setObject(1, o);
			_deleteStatement.executeUpdate();
			_size--;
			return true;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean contains(Object o) {
		ResultSet rs = null;
		try {
			_containsStatement.setObject(1, o);
			rs = _containsStatement.executeQuery();
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

	@Override
	public Iterator<E> iterator() {
		try {
			ResultSet rs = _iteratorStatement.executeQuery();
			return new SqlDatabaseSetIterator<E>(this, rs);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int size() {
		return _size;
	}

	@Override
	public String getTableName() {
		return _tableName;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + getTableName());
	}
}
