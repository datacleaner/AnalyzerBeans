package org.eobjects.analyzer.data;

import java.util.List;

/**
 * Represents a row of data where each value pertain to a column.
 * 
 * An InputRow can contain both values that are physical (ie. a raw output from
 * a datastore) and virtual (ie. generated values, created by Transformers).
 * 
 * The contents of an InputRow is visualized in the image below:
 * 
 * <img src="doc-files/AnalyzerBeans-inputrow.png" alt="InputRow contents" />
 * 
 * @see Transformer
 * @see InputColumn
 * 
 * @author Kasper Sørensen
 */
public interface InputRow {

	/**
	 * Gets a value from the row on a given column position, or null if no value
	 * exists at this column position.
	 * 
	 * @param <E>
	 * @param column
	 * @return
	 */
	public <E> E getValue(InputColumn<E> column);

	/**
	 * An id identifying this row. The id is guaranteed to be unique (and
	 * typically sequential) within a single dataset only.
	 * 
	 * @return an identifier for this row
	 */
	public int getId();

	/**
	 * @return the input columns represented in this row
	 */
	public List<InputColumn<?>> getInputColumns();
}
