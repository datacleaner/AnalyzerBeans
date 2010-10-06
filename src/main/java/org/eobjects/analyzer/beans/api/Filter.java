package org.eobjects.analyzer.beans.api;

import org.eobjects.analyzer.data.InputRow;

/**
 * Interface for components that filter/categorize rows.
 * 
 * @author Kasper Sørensen
 *
 * @param <C> an enum type with the available categories
 */
public interface Filter<C extends Enum<C>> {

	public C categorize(InputRow inputRow);
}
