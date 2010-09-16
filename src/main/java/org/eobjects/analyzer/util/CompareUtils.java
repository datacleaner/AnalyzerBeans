package org.eobjects.analyzer.util;

import java.lang.reflect.Array;

public final class CompareUtils {
	
	private CompareUtils() {
		// prevent instantiation
	}

	public static final boolean equals(final Object obj1, final Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
		}

		Class<? extends Object> class1 = obj1.getClass();
		Class<? extends Object> class2 = obj2.getClass();
		if (class1.isArray()) {
			if (!class2.isArray()) {
				return false;
			} else {
				Class<?> componentType1 = class1.getComponentType();
				Class<?> componentType2 = class2.getComponentType();
				if (!componentType1.equals(componentType2)) {
					return false;
				}

				int length1 = Array.getLength(obj1);
				int length2 = Array.getLength(obj2);
				if (length1 != length2) {
					return false;
				}
				for (int i = 0; i < length1; i++) {
					Object elem1 = Array.get(obj1, i);
					Object elem2 = Array.get(obj2, i);
					if (!equals(elem1, elem2)) {
						return false;
					}
				}
				return true;
			}
		} else {
			if (class2.isArray()) {
				return false;
			}
		}

		return obj1.equals(obj2);
	}

	/**
	 * 
	 * @param <E>
	 * @param obj1
	 * @param obj2
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	public static final <E> int compare(Comparable<E> obj1, E obj2) {
		if (obj1 == obj2) {
			return 0;
		}
		if (obj1 == null) {
			return -1;
		}
		if (obj2 == null) {
			return 1;
		}

		return obj1.compareTo(obj2);
	}
}
