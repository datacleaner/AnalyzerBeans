package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputRow;

public interface Transformer<E> {

	/**
	 * @return an object with the information needed to create the output
	 *         columns
	 */
	public OutputColumns getOutputColumns();

	/**
	 * Transforms a row of input values to the corresponding transformed values
	 * 
	 * @param inputRow
	 * @return an array of transformed values.
	 */
	public E[] transform(InputRow inputRow);
}