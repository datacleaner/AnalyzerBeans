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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.ReferenceMap;
import org.eobjects.metamodel.util.CollectionUtils;

/**
 * Additional (to {@link CollectionUtils} utility methods for common collection
 * or array operations.
 * 
 * @author Kasper Sørensen
 */
public final class CollectionUtils2 {

	private CollectionUtils2() {
		// prevent instantiation
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> filterOnClass(List<?> superTypeList, Class<E> subType) {
		final List<E> result = new ArrayList<E>();
		for (Object object : superTypeList) {
			if (object != null) {
				if (ReflectionUtils.is(object.getClass(), subType)) {
					result.add((E) object);
				}
			}
		}
		return result;
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
