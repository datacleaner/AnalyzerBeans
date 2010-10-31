package org.eobjects.analyzer.lifecycle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.List;

class HsqldbList<E> extends AbstractList<E> implements List<E>, HsqldbCollection {

	private final Connection _connection;
	private final String _tableName;
	private final PreparedStatement _addStatement;
	private final PreparedStatement _addAtIndexStatement;
	private final PreparedStatement _updateStatement;
	private volatile int _size;

	public HsqldbList(Connection connection, String tableName) {
		_connection = connection;
		_tableName = tableName;
		_size = 0;

		try {
			_addStatement = _connection.prepareStatement("INSERT INTO " + tableName
					+ " VALUES((SELECT MAX(list_index)+1 FROM " + tableName + "), ?)");
			_addAtIndexStatement = _connection.prepareStatement("INSERT INTO " + tableName + " VALUES(?, ?)");
			_updateStatement = _connection.prepareStatement("UPDATE " + tableName + " SET list_value=? WHERE list_index=?");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public synchronized E remove(int index) {
		E oldValue = get(index);
		HsqldbCollectionProvider.performUpdate(_connection, "DELETE FROM " + _tableName + " WHERE list_index=" + index);
		HsqldbCollectionProvider.performUpdate(_connection, "UPDATE " + _tableName
				+ " SET list_index = list_index-1 WHERE list_index > " + index);
		_size--;
		return oldValue;
	}

	@Override
	public synchronized void clear() {
		HsqldbCollectionProvider.performUpdate(_connection, "DELETE FROM " + _tableName);
		_size = 0;
	}

	@Override
	public E get(int index) {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = _connection.createStatement();
			rs = st.executeQuery("SELECT list_value FROM " + _tableName + " WHERE list_index=" + index + ";");
			if (rs.next()) {
				@SuppressWarnings("unchecked")
				E result = (E) rs.getObject(1);
				if (rs.wasNull()) {
					return null;
				}
				return result;
			}
			throw new IndexOutOfBoundsException("No such index: " + index);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			HsqldbCollectionProvider.safeClose(rs, st);
		}
	}

	@Override
	public int size() {
		return _size;
	}

	public synchronized boolean add(E elem) {
		try {
			PreparedStatement st = null;
			if (_size == 0) {
				// first time is different
				st = _connection.prepareStatement("INSERT INTO " + _tableName + " VALUES(0, ?)");
			} else {
				st = _addStatement;
			}
			st.setObject(1, elem);
			st.execute();
			_size++;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return true;
	}

	public synchronized void add(int index, E element) {
		HsqldbCollectionProvider.performUpdate(_connection, "UPDATE " + _tableName
				+ " SET list_index = list_index+1 WHERE list_index > " + index);
		try {
			_addAtIndexStatement.setObject(1, index);
			_addAtIndexStatement.setObject(2, element);
			_addAtIndexStatement.executeUpdate();
			_size++;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	};

	public synchronized E set(int index, E element) {
		E oldValue = get(index);
		try {
			_updateStatement.setObject(1, element);
			_updateStatement.setObject(2, index);
			_updateStatement.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return oldValue;
	};

	@Override
	public String getTableName() {
		return _tableName;
	};
}
