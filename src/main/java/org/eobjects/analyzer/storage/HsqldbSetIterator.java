package org.eobjects.analyzer.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

final class HsqldbSetIterator<E> implements Iterator<E> {

	private final HsqldbSet<E> _set;
	private final ResultSet _rs;
	
	private volatile boolean _hasNext;
	private volatile E _currentValue;
	private volatile E _nextValue;

	public HsqldbSetIterator(HsqldbSet<E> set, ResultSet rs) {
		_set = set;
		_rs = rs;
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

}
