package org.eobjects.analyzer.util;

public final class CompareUtils {

	public static final boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
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
