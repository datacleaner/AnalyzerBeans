package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.DataContext;

/**
 * An analyzer that explores a datastore (in the form of a DataContext).
 * 
 * Exploring analyzers have a special compared to Transformers and
 * RowProcessingAnalyzers in a number of ways:
 * <ul>
 * <li>The run(DataContext) method is only invoked once. The Exploring analyzer
 * can then do with the DataContext whatever is fitting.</li>
 * <li>Exploring analyzers doesn't support chained execution where transformers
 * play some part in "preparing" the data before execution.</li>
 * <li>Exploring analyzers should be used when there is a querying-based
 * performance gain. Typically such a gain can be archieved if the analyzer
 * needs aggregates that can be pushed down to queries in stead of processing
 * each row in a particular table.</li>
 * <li>Furthermore, exploring analyzers have superior capabilities when it comes
 * to joining, grouping etc. because they can create their own queries.</li>
 * <li>When many exploring analyzers run in parallel, they may introduce a
 * performance penalty because they cannot share queries like row processing
 * analyzers.</li>
 * </ul>
 * 
 * Use of the @AnalyzerBean annotation is required for analyzers in order to be
 * automatically discovered.
 * 
 * @see RowProcessingAnalyzer
 * @see AnalyzerBean
 * 
 * @author Kasper Sørensen
 * 
 * @param <R>
 *            the result type returned by this analyzer
 */
public interface ExploringAnalyzer<R extends AnalyzerResult> extends
		Analyzer<R> {

	public void run(DataContext dc);
}
