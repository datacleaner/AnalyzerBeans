package org.eobjects.analyzer.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

final class SqlDatabaseSetIterator<E> implements Iterator<E> {

	private final SqlDatabaseSet<E> _set;
	private final ResultSet _rs;
	private final Statement _st;

	private volatile boolean _hasNext;
	private volatile E _currentValue;
	private volatile E _nextValue;

	public SqlDatabaseSetIterator(SqlDatabaseSet<E> set, ResultSet rs, Statement st) {
		_set = set;
		_rs = rs;
		_st = st;
		moveNext();
	}

	@SuppressWarnings("unchecked")
	private void moveNext() {
		try {
			_currentValue = _nextValue;
			_hasNext = _rs.next();
			if (_hasNext) {
				_nextValue = (E) _rs.getObject(1);
			} else {
				_nextValue = null;
				SqlDatabaseUtils.safeClose(_rs, _st);
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return _hasNext;
	}

	@Override
	public E next() {
		moveNext();
		return _currentValue;
	}

	@Override
	public void remove() {
		_set.remove(_currentValue);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		SqlDatabaseUtils.safeClose(_rs, _st);
	}
}
