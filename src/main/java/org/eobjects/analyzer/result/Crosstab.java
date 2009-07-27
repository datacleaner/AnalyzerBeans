package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.descriptors.AnnotationHelper;

public class Crosstab<E extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<CrosstabDimension> dimensions;
	private Map<String, E> values = new HashMap<String, E>();
	private Class<E> valueClass;

	public Crosstab(Class<E> valueClass, CrosstabDimension... dimensions) {
		this.valueClass = valueClass;
		this.dimensions = Arrays.asList(dimensions);
	}

	public Crosstab(Class<E> valueClass,
			Collection<CrosstabDimension> dimensions) {
		this.valueClass = valueClass;
		this.dimensions = new ArrayList<CrosstabDimension>(dimensions);
	}

	public Crosstab(Class<E> valueClass, String... dimensionNames) {
		this.valueClass = valueClass;
		dimensions = new ArrayList<CrosstabDimension>();
		for (String name : dimensionNames) {
			dimensions.add(new CrosstabDimension(name));
		}
	}

	public Class<E> getValueClass() {
		return valueClass;
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> Crosstab<T> castValueClass(
			Class<T> valueClass) {
		if (AnnotationHelper.is(this.valueClass, valueClass)) {
			return (Crosstab<T>) this;
		}
		throw new IllegalArgumentException("Unable to cast [" + this.valueClass
				+ "] to [" + valueClass + "]");
	}

	public List<CrosstabDimension> getDimensions() {
		return Collections.unmodifiableList(dimensions);
	}

	private String getKey(String[] categories) throws IllegalArgumentException,
			NullPointerException {
		if (categories.length != dimensions.size()) {
			throw new IllegalArgumentException(
					"Not all dimensions have been specified (differences in size of parameter and Crosstab's dimensions)");
		}
		if (ArrayUtils.contains(categories, null)) {
			throw new NullPointerException(
					"Not all dimensions have been specified (some are null)");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < categories.length; i++) {
			CrosstabDimension dimension = dimensions.get(i);
			String category = categories[i];
			if (!dimension.containsCategory(category)) {
				throw new IllegalArgumentException("Unknown category ["
						+ category + "] for dimension [" + dimension.getName()
						+ "]");
			}
			if (i != 0) {
				sb.append('^');
			}
			sb.append(category);
		}
		return sb.toString();
	}

	public CrosstabNavigator<E> navigate() {
		return new CrosstabNavigator<E>(this);
	}

	protected E getValue(String[] categories) throws IllegalArgumentException,
			NullPointerException {
		String key = getKey(categories);
		return values.get(key);
	}

	public CrosstabNavigator<E> where(String dimension, String isCategory) {
		return navigate().where(dimension, isCategory);
	}

	public CrosstabNavigator<E> where(CrosstabDimension dimension,
			String isCategory) {
		return navigate().where(dimension, isCategory);
	}

	protected void putValue(E value, String[] categories)
			throws IllegalArgumentException, NullPointerException {
		if (value != null) {
			if (!AnnotationHelper.is(value.getClass(), valueClass)) {
				throw new IllegalArgumentException("Cannot put value [" + value
						+ "] of type [" + value.getClass()
						+ "] when Crosstab.valueClass is [" + valueClass + "]");
			}
		}
		String key = getKey(categories);
		values.put(key, value);
	}

	public int getDimensionCount() {
		return dimensions.size();
	}

	public String[] getDimensionNames() {
		int size = dimensions.size();
		String[] result = new String[size];
		for (int i = 0; i < size; i++) {
			result[i] = dimensions.get(i).getName();
		}
		return result;
	}

	public int getDimensionIndex(CrosstabDimension dimension) {
		if (dimension != null) {
			int size = dimensions.size();
			for (int i = 0; i < size; i++) {
				if (dimension.equals(dimensions.get(i))) {
					return i;
				}
			}
		}
		throw new IllegalArgumentException("No such dimension: " + dimension);
	}

	public int getDimensionIndex(String dimensionName) {
		if (dimensionName != null) {
			int size = dimensions.size();
			for (int i = 0; i < size; i++) {
				if (dimensionName.equals(dimensions.get(i).getName())) {
					return i;
				}
			}
		}
		throw new IllegalArgumentException("No such dimension: "
				+ dimensionName);
	}

	public CrosstabDimension getDimension(int i) {
		return dimensions.get(i);
	}
}