package org.eobjects.analyzer.beans.api;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * A row processing analyzer is a component that recieves rows of data and
 * produces some sort of result from it.
 * 
 * The run(InputRow, int) method will be invoked on the analyzer for each row in
 * a configured datastore. To retrieve the values from the row InputColumn
 * instances must be used as qualifiers. These InputColumns needs to be injected
 * (either a single instance or an array) using the @Configured annotation. If
 * no @Configured InputColumns are found in the class, the analyzer will not be
 * able to execute.
 * 
 * Use of the @AnalyzerBean annotation is required for analyzers in order to be
 * automatically discovered.
 * 
 * @see AnalyzerBean
 * @see Configured
 * @see ExploringAnalyzer
 * 
 * @author Kasper Sørensen
 * 
 * @param <R>
 *            the result type returned by this analyzer
 */
public interface RowProcessingAnalyzer<R extends AnalyzerResult> extends Analyzer<R> {

	public void run(InputRow row, int distinctCount);
}
