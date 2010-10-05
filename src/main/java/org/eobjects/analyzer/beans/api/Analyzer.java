package org.eobjects.analyzer.beans.api;

import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * Super-interface for analyzers. Analyzers should implement one of the two
 * sub-interfaces, ExploringAnalyzer or RowProcessingAnalyzer.
 * 
 * @see ExploringAnalyzer
 * @see RowProcessingAnalyzer
 * 
 * @author Kasper Sørensen
 * 
 * @param <R>
 *            the result type of this analyzer.
 */
public interface Analyzer<R extends AnalyzerResult> {

	public R getResult();
}
