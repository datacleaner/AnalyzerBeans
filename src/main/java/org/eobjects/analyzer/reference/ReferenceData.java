package org.eobjects.analyzer.reference;

import java.io.Serializable;

/**
 * Abstraction over all reference data types in AnalyzerBeans
 * 
 * @author Kasper Sørensen
 */
public interface ReferenceData extends Serializable {

	/**
	 * Gets the name of the reference data item.
	 * 
	 * @return a String containing the name of this reference data item.
	 */
	public String getName();
}
