package org.eobjects.analyzer.storage;

import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * The RowAnnotationFactory represents a mechanism used to annotate/label rows
 * of data during execution. Typically these annotations will be stored on disk
 * and thus provide a convenient storage mechanism for situations where a
 * component needs to manage a set of labels but where storing them in
 * collections would be too complicated and would fill up memory.
 * 
 * The RowAnnotationFactory is injectable into any row processing component
 * (analyzer, transformer, filter) using the @Provided annotation.
 * 
 * @see Provided
 * 
 * @author Kasper Sørensen
 */
public interface RowAnnotationFactory {

	/**
	 * Creates a new annotation
	 * 
	 * @return a new annotation
	 */
	public RowAnnotation createAnnotation();

	/**
	 * Annotates/labels a row with an annotation. The row will be retrievable
	 * using the getRows(...) method later in the process.
	 * 
	 * @param row
	 * @param distinctCount
	 * @param annotation
	 */
	public void annotate(InputRow row, int distinctCount, RowAnnotation annotation);

	/**
	 * Removes/resets all annotations of a specific kind. This method can be
	 * used for situations where eg. an analyzer is annotating extreme values
	 * (highest/lowest values etc.) and the threshold is changing, cancelling
	 * all previous annotations.
	 * 
	 * @param annotation
	 */
	public void reset(RowAnnotation annotation);

	/**
	 * Gets all the rows with a given annotation.
	 * 
	 * @param annotation
	 * @return
	 */
	public InputRow[] getRows(RowAnnotation annotation);

	/**
	 * Gets a summarized view of the distinct values and their counts for a
	 * single column and annotation.
	 * 
	 * @param annotation
	 * @param inputColumn
	 * @return
	 */
	public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn);
}
