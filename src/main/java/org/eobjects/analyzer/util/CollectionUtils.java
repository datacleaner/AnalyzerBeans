package org.eobjects.analyzer.util;

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
	public static <T> List<T> list(Class<? extends List> listClass,
			T... elements) {
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
}
