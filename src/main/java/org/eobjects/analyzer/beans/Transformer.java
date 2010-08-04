package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputRow;

/**
 * Interface for components that transform data before analysis. Transformers
 * work pretty much the same way as row processing analyzers, except that their
 * output is not a finished result, but rather a new, enhanced, row with more
 * values than the incoming row.
 * 
 * The transform(InputRow) method will be invoked on the transformer for each
 * row in a configured datastore. To retrieve the values from the row
 * InputColumn instances must be used as qualifiers. These InputColumns needs to
 * be injected (either a single instance or an array) using the @Configured
 * annotation. If no @Configured InputColumns are found in the class, the
 * transformer will not be able to execute.
 * 
 * Use of the @TransformerBean annotation is required for transformers in order
 * to be automatically discovered.
 * 
 * @see TransformerBean
 * @see Configured
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            the type of the new/transformed values
 */
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