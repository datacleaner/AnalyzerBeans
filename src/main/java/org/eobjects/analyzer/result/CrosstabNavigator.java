package org.eobjects.analyzer.result;

import java.io.Serializable;

public class CrosstabNavigator<E extends Serializable> {

	private Crosstab<E> crosstab;
	private String[] categories;

	public CrosstabNavigator(Crosstab<E> crosstab) {
		this.crosstab = crosstab;
		categories = new String[crosstab.getDimensionCount()];
	}

	public CrosstabNavigator<E> where(String dimension, String isCategory) {
		int index = crosstab.getDimensionIndex(dimension);
		categories[index] = isCategory;
		return this;
	}

	public CrosstabNavigator<E> where(CrosstabDimension dimension,
			String isCategory) {
		return where(dimension.getName(), isCategory);
	}

	public void put(E value) throws IllegalArgumentException,
			NullPointerException {
		put(value, false);
	}

	public void put(E value, boolean createCategories) throws IllegalArgumentException,
			NullPointerException {
		if (createCategories) {
			for (int i = 0; i < categories.length; i++) {
				String category = categories[i];
				CrosstabDimension dimension = crosstab.getDimension(i);
				dimension.addCategory(category);
			}
		}
		crosstab.putValue(value, categories);
	}

	public E get() throws IllegalArgumentException, NullPointerException {
		return crosstab.getValue(categories);
	}
}
