package org.eobjects.analyzer.job;

/**
 * An object that enables writing/serializing an AnalysisJob to an output.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            The output type of the writer, typically OutputStream, but could
 *            also be a different output type as well.
 */
public interface JobWriter<E> {

	public void write(AnalysisJob job, E output);
}
