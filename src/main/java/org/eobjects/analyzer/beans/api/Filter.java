package org.eobjects.analyzer.beans.api;

import org.eobjects.analyzer.data.InputRow;

/**
 * Interface for components that filter/categorize rows.
 * 
 * A filter will process incoming rows and label them with a category. A
 * category is defined as a value in an enum. When a row is categorized, this
 * category can then be used to set up a requirement for succeeding row
 * processing.
 * 
 * @author Kasper Sørensen
 * 
 * @param <C>
 *            an enum type with the available categories
 */
public interface Filter<C extends Enum<C>> {

	public C categorize(InputRow inputRow);
}
