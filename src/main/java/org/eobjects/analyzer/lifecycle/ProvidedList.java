package org.eobjects.analyzer.lifecycle;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

/**
 * A map wrapped in the List interface. This is used because the berkeley db
 * does not support persistent lists, but only maps. Instead a persistent map is
 * wrapped by this List-implementation.
 */
public class ProvidedList<E> extends AbstractList<E> implements List<E> {

	private Map<Integer, E> wrappedMap;

	public Map<Integer, E> getWrappedMap() {
		return wrappedMap;
	}

	public ProvidedList(Map<Integer, E> map) {
		super();
		this.wrappedMap = map;
	}

	@Override
	public E get(int index) {
		if (!wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = wrappedMap.get(index);
		return element;
	}

	@Override
	public int size() {
		return wrappedMap.size();
	}

	@Override
	public boolean add(E element) {
		wrappedMap.put(wrappedMap.size(), element);
		return true;
	};

	@Override
	public void add(int index, E element) {
		if (!wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = wrappedMap.size(); i > index; i--) {
			wrappedMap.put(i, wrappedMap.get(i - 1));
		}
		wrappedMap.put(index, element);
	};

	@Override
	public E set(int index, E element) {
		if (!wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		return wrappedMap.put(index, element);
	};

	@Override
	public E remove(int index) {
		if (!wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = wrappedMap.get(index);
		for (int i = index; i < wrappedMap.size() - 1; i++) {
			wrappedMap.put(i, wrappedMap.get(i + 1));
		}
		wrappedMap.remove(wrappedMap.size() - 1);
		return element;
	}
}
