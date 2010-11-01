package org.eobjects.analyzer.storage;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

/**
 * A map wrapped in the List interface. This is used because the berkeley db
 * does not support persistent lists, but only maps. Instead a persistent map is
 * wrapped by this List-implementation.
 */
final class BerkeleyDbList<E> extends AbstractList<E> implements List<E> {

	private final Map<Integer, E> _wrappedMap;
	private final BerkeleyDbStorageProvider _storageProvider;

	public Map<Integer, E> getWrappedMap() {
		return _wrappedMap;
	}

	public BerkeleyDbList(BerkeleyDbStorageProvider storageProvider, Map<Integer, E> map) {
		super();
		_storageProvider = storageProvider;
		_wrappedMap = map;
	}

	@Override
	public E get(int index) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = _wrappedMap.get(index);
		return element;
	}

	@Override
	public int size() {
		return _wrappedMap.size();
	}

	@Override
	public boolean add(E element) {
		_wrappedMap.put(_wrappedMap.size(), element);
		return true;
	};

	@Override
	public void add(int index, E element) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = _wrappedMap.size(); i > index; i--) {
			_wrappedMap.put(i, _wrappedMap.get(i - 1));
		}
		_wrappedMap.put(index, element);
	};

	@Override
	public E set(int index, E element) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		return _wrappedMap.put(index, element);
	};

	@Override
	public E remove(int index) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = _wrappedMap.get(index);
		for (int i = index; i < _wrappedMap.size() - 1; i++) {
			_wrappedMap.put(i, _wrappedMap.get(i + 1));
		}
		_wrappedMap.remove(_wrappedMap.size() - 1);
		return element;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_storageProvider.cleanUp(this);
	}
}
