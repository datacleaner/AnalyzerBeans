package org.eobjects.analyzer.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CollectionUtils {

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
		boolean found = false;
		@SuppressWarnings("unchecked")
		E[] result = (E[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
		int nextIndex = 0;
		for (E e : array) {
			if (e == elementToRemove) {
				found = true;
			} else {
				result[nextIndex] = e;
				nextIndex++;
			}
		}
		if (!found) {
			return array;
		}
		return result;
	}

	public static Object arrayRemove(Object array, Object elementToRemove) {
		boolean found = false;
		int oldLength = Array.getLength(array);
		Object result = Array.newInstance(array.getClass().getComponentType(), oldLength - 1);
		int nextIndex = 0;
		for (int i = 0; i < oldLength; i++) {
			Object e = Array.get(array, i);
			if (e == elementToRemove) {
				found = true;
			} else {
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
	public static <E> List<E> filterOnClass(List<?> datastoreTypes, Class<E> clazz) {
		List<E> result = new ArrayList<E>();
		for (Object object : datastoreTypes) {
			if (object != null) {
				if (ReflectionUtils.is(object.getClass(), clazz)) {
					result.add((E) object);
				}
			}
		}
		return result;
	}
}
