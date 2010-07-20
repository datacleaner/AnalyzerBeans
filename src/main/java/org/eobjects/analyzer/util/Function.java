package org.eobjects.analyzer.util;

/**
 * Represents a generic function that transforms an input to an output
 * 
 * @author Kasper Sørensen
 * 
 * @param <I>
 *            the input type
 * @param <O>
 *            the output type
 */
public interface Function<I, O> {

	public O exec(I obj);
}
