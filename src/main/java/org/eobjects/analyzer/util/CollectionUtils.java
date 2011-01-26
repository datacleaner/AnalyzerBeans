/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.ReferenceMap;
import org.eobjects.analyzer.reference.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollectionUtils {

	private static final Logger logger = LoggerFactory.getLogger(CollectionUtils.class);

	private CollectionUtils() {
		// prevent instantiation
	}

	public static <T> List<T> list(T... elements) {
		return list(ArrayList.class, elements);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> list(Class<? extends List> listClass, T... elements) {
		try {
			List list = listClass.newInstance();
			for (T t : elements) {
				list.add(t);
			}
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Set<T> set(T... elements) {
		return set(HashSet.class, elements);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Set<T> set(Class<? extends Set> setClass, T... elements) {
		try {
			Set set = setClass.newInstance();
			for (T t : elements) {
				set.add(t);
			}
			return set;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] array(E[] existingArray, E... elements) {
		if (existingArray == null) {
			return elements;
		}
		Object result = Array.newInstance(elements.getClass().getComponentType(), existingArray.length + elements.length);
		System.arraycopy(existingArray, 0, result, 0, existingArray.length);
		System.arraycopy(elements, 0, result, existingArray.length, elements.length);
		return (E[]) result;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] array(Class<E> elementClass, Object existingArray, E... elements) {
		if (existingArray == null) {
			return elements;
		}
		E[] result;
		if (existingArray.getClass().isArray()) {
			int length = Array.getLength(existingArray);
			result = (E[]) Array.newInstance(elementClass, length + elements.length);
			System.arraycopy(existingArray, 0, result, 0, length);
			System.arraycopy(elements, 0, result, length, elements.length);
		} else {
			result = (E[]) Array.newInstance(elementClass, 1 + elements.length);
			result[0] = (E) existingArray;
			System.arraycopy(elements, 0, result, 1, elements.length);
		}

		return result;
	}

	public static <E> E[] arrayRemove(E[] array, E elementToRemove) {
		@SuppressWarnings("unchecked")
		E[] result = (E[]) arrayRemoveInternal(array, elementToRemove);
		return result;
	}

	public static Object arrayRemove(Object array, Object elementToRemove) {
		return arrayRemoveInternal(array, elementToRemove);
	}

	private static Object arrayRemoveInternal(Object array, Object elementToRemove) {
		boolean found = false;
		final int oldLength = Array.getLength(array);
		final int newLength = oldLength - 1;
		final Object result = Array.newInstance(array.getClass().getComponentType(), newLength);
		int nextIndex = 0;
		for (int i = 0; i < oldLength; i++) {
			final Object e = Array.get(array, i);
			if (e.equals(elementToRemove)) {
				found = true;
			} else {
				if (nextIndex == newLength) {
					break;
				}
				Array.set(result, nextIndex, e);
				nextIndex++;
			}
		}
		if (!found) {
			return array;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] arrayOf(Class<E> elementClass, Object arrayOrElement) {
		if (arrayOrElement == null) {
			return null;
		}
		if (arrayOrElement.getClass().isArray()) {
			return (E[]) arrayOrElement;
		}
		Object result = Array.newInstance(elementClass, 1);
		Array.set(result, 0, arrayOrElement);
		return (E[]) result;
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> filterOnClass(List<?> superTypeList, Class<E> subType) {
		List<E> result = new ArrayList<E>();
		for (Object object : superTypeList) {
			if (object != null) {
				if (ReflectionUtils.is(object.getClass(), subType)) {
					result.add((E) object);
				}
			}
		}
		return result;
	}

	public static <E> List<E> filter(List<E> list, Function<E, Boolean> predicate) {
		List<E> result = new ArrayList<E>();
		for (E obj : list) {
			try {
				if (predicate.run(obj)) {
					result.add(obj);
				}
			} catch (Exception e) {
				logger.warn("Exception thrown while executing predicate", e);
				throw new IllegalArgumentException(e);
			}
		}
		return result;
	}

	public static <E> List<E> sorted(Collection<E> col, Comparator<? super E> comparator) {
		ArrayList<E> list = new ArrayList<E>(col);
		Collections.sort(list, comparator);
		return list;
	}

	public static <E extends Comparable<E>> List<E> sorted(Collection<E> col) {
		ArrayList<E> list = new ArrayList<E>(col);
		Collections.sort(list);
		return list;
	}

	public static <K, V> Map<K, V> createCacheMap() {
		return new ReferenceMap<K, V>(ReferenceMap.SOFT, ReferenceMap.SOFT, true);
	}
}
