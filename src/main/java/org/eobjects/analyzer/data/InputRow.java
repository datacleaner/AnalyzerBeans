package org.eobjects.analyzer.data;

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

	public <E> E getValue(InputColumn<E> column);
}
