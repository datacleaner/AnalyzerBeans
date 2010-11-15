package org.eobjects.analyzer.job;

/**
 * An object that is capable of reading an AnalysisJob from a source.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            The source type, typically an InputStream, but could be another
 *            type of source as well.
 */
public interface JobReader<E> {

	public AnalysisJob read(E inputStream);
}
