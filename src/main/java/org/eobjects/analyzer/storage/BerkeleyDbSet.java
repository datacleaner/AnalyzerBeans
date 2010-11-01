package org.eobjects.analyzer.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.sleepycat.collections.StoredKeySet;

final class BerkeleyDbSet<E> implements Set<E> {

	private final Set<E> _wrappedSet;
	private final BerkeleyDbStorageProvider _storageProvider;

	@SuppressWarnings("unchecked")
	public BerkeleyDbSet(BerkeleyDbStorageProvider storageProvider, StoredKeySet set) {
		_wrappedSet = set;
		_storageProvider = storageProvider;
	}

	public Set<E> getWrappedSet() {
		return _wrappedSet;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_storageProvider.cleanUp(_wrappedSet);
	}

	public int size() {
		return _wrappedSet.size();
	}

	public boolean isEmpty() {
		return _wrappedSet.isEmpty();
	}

	public boolean contains(Object o) {
		return _wrappedSet.contains(o);
	}

	public Iterator<E> iterator() {
		return _wrappedSet.iterator();
	}

	public Object[] toArray() {
		return _wrappedSet.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return _wrappedSet.toArray(a);
	}

	public boolean add(E e) {
		return _wrappedSet.add(e);
	}

	public boolean remove(Object o) {
		return _wrappedSet.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return _wrappedSet.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		return _wrappedSet.addAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return _wrappedSet.retainAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return _wrappedSet.removeAll(c);
	}

	public void clear() {
		_wrappedSet.clear();
	}

	public boolean equals(Object o) {
		return _wrappedSet.equals(o);
	}

	public int hashCode() {
		return _wrappedSet.hashCode();
	}
}
