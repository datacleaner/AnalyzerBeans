package org.eobjects.analyzer.util;

import java.util.HashSet;
import java.util.Set;

public final class CollectionUtils {

	private CollectionUtils() {
		// prevent instantiation
	}

	public static <T> Set<T> set(T... elements) {
		return set(HashSet.class, elements);
	}

	@SuppressWarnings("unchecked")
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
