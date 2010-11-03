package org.eobjects.analyzer.storage;

/**
 * Represents an annotation (aka a mark, a label or a categorization) of a row.
 * RowAnnotations are used typically by analyzers in order to label rows for
 * later use, typically drill-to-detail functionality.
 * 
 * RowAnnotations are created through the RowAnnotationFactory, which is
 * injectable using the @Provided annotation.
 * 
 * @see RowAnnotationFactory
 * 
 * @author Kasper Sørensen
 */
public interface RowAnnotation {

	public int getRowCount();
}
