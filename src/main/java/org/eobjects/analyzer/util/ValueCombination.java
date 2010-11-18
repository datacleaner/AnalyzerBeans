package org.eobjects.analyzer.util;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.util.BaseObject;

/**
 * Represents a combination of values that are of interest to the user.
 * Typically such a combination is used to find dependencies between the values
 * of a couple of columns.
 * 
 * A ValueCombination has proper hashCode and equals methods. It also implements
 * Comparable, comparing value-by-value.
 * 
 * @author Kasper Sørensen
 */
public class ValueCombination<E> extends BaseObject implements Comparable<ValueCombination<E>> {

	private static final Logger logger = LoggerFactory.getLogger(ValueCombination.class);
	private final E[] _values;

	public ValueCombination(E... values) {
		_values = values;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_values);
	}

	public int getValueCount() {
		return _values.length;
	}

	public E getValueAt(int index) {
		return _values[index];
	}

	@Override
	public String toString() {
		return "ValueCombination[" + Arrays.toString(_values) + "]";
	}

	@Override
	public int compareTo(ValueCombination<E> o) {
		if (this.equals(o)) {
			return 0;
		}
		int count1 = this.getValueCount();
		int count2 = o.getValueCount();
		int minCount = Math.min(count1, count2);
		for (int i = 0; i < minCount; i++) {
			E value1 = this.getValueAt(i);
			E value2 = o.getValueAt(i);
			if (value1 instanceof Comparable) {
				try {
					@SuppressWarnings("unchecked")
					int result = ((Comparable<E>) value1).compareTo(value2);
					if (result != 0) {
						return result;
					}
				} catch (Exception e) {
					// do nothing - the typecase to Comparable<E> was invalid
					logger.warn("Could not compare {} and {}, comparable threw exception: {}", new Object[] { value1,
							value2, e.getMessage() });
					logger.debug("Comparable threw exception", e);
				}
			} else {
				logger.warn("Could not compare {} and {}, not comparable", value1, value2);
			}
		}
		int result = count1 - count2;
		if (result == 0) {
			result = -1;
		}
		return result;
	}
}
