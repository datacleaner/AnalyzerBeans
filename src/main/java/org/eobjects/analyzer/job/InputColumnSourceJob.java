package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;

/**
 * Interface for job objects that generate (virtual) input columns in a job.
 * 
 * @author Kasper Sørensen
 */
public interface InputColumnSourceJob {

	/**
	 * @return the originating (if any) columns that generated the new columns
	 */
	public InputColumn<?>[] getInput();

	/**
	 * @return the columns generated by this source
	 */
	public InputColumn<?>[] getOutput();
}