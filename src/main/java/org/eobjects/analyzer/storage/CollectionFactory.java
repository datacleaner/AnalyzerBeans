package org.eobjects.analyzer.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory for collections to be used by components. Typically these
 * collections are provided by the framework an are implemented using some
 * persistent storage strategy, making them safe to fill with even millions of
 * elements without running out of memory.
 * 
 * @author Kasper Sørensen
 */
public interface CollectionFactory {

	public <E> List<E> createList(Class<E> elementClass);

	public <E> Set<E> createSet(Class<E> elementClass);

	public <K, V> Map<K, V> createSet(Class<K> keyClass, Class<V> valueClass);
}
