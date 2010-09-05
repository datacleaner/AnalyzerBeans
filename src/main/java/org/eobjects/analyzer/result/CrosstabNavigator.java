package org.eobjects.analyzer.result;

import java.io.Serializable;

public class CrosstabNavigator<E extends Serializable> implements Cloneable {

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

	/**
	 * Puts the given value to the navigated position in the crosstab.
	 * 
	 * @param value
	 *            the value to put.
	 * @param createCategories
	 *            if true, the chosen categories will automatically be created
	 *            if they do not already exists in the dimensions of the
	 *            crosstab.
	 * @throws IllegalArgumentException
	 *             if the position or value is invalid, typically because one or
	 *             more dimensions lacks a specified category or the value type
	 *             is not acceptable (typically because of class casting issues)
	 * @throws NullPointerException
	 *             if some of the specified categories are null
	 */
	public void put(E value, boolean createCategories)
			throws IllegalArgumentException, NullPointerException {
		if (createCategories) {
			for (int i = 0; i < categories.length; i++) {
				String category = categories[i];
				CrosstabDimension dimension = crosstab.getDimension(i);
				dimension.addCategory(category);
			}
		}
		crosstab.putValue(value, categories);
	}

	/**
	 * Gets the value associated with the navigated position of the crosstab.
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 *             if the position is invalid, typically because one or more
	 *             dimensions lacks a specified category.
	 * @throws NullPointerException
	 *             if some of the specified categories are null
	 */
	public E get() throws IllegalArgumentException, NullPointerException {
		return crosstab.getValue(categories);
	}

	/**
	 * Attaches an AnalyzerResult as result-exploration data for the navigated
	 * position of the crosstab.
	 * 
	 * @param explorationResult
	 */
	public void attach(AnalyzerResult explorationResult) {
		attach(new DefaultResultProducer(explorationResult));
	}

	/**
	 * Attaches a ResultProducer as result-exploration data-provider for the
	 * navigated position of the crosstab. Note that if the ResultProducer is
	 * Serializable, it will be saved with the crosstab on serialization.
	 * 
	 * @param explorationResultProducer
	 */
	public void attach(ResultProducer explorationResultProducer) {
		crosstab.attachResultProducer(explorationResultProducer, categories);
	}

	public ResultProducer explore() {
		return crosstab.explore(categories);
	}

	@Override
	public CrosstabNavigator<E> clone() {
		CrosstabNavigator<E> n = new CrosstabNavigator<E>(crosstab);
		n.categories = categories.clone();
		return n;
	}

	public String getCategory(CrosstabDimension dimension) {
		int index = crosstab.getDimensionIndex(dimension);
		return categories[index];
	}
}
