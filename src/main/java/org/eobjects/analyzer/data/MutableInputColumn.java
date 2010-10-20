package org.eobjects.analyzer.data;

/**
 * Represents a column that is mutable (editable by the user).
 * 
 * Mutable columns have editable names but unique id's to identify them (whereas
 * the names identify the immutable columns).
 * 
 * @author Kasper Sørensen
 */
public interface MutableInputColumn<E> extends InputColumn<E> {

	/**
	 * Sets the name of the column
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return an id that is unique within the AnalysisJob that is being built
	 *         or executed.
	 */
	public String getId();
}
