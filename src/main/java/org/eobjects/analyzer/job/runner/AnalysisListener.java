package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.schema.Table;

/**
 * Listener interface for analysis execution. Typically the user interface and
 * maybe also system services would implement this interface to be able to react
 * to progress notifications or errors occurring in the execution of the
 * analysis.
 * 
 * @author Kasper Sørensen
 */
public interface AnalysisListener {

	public void jobBegin(AnalysisJob job);

	public void jobSuccess(AnalysisJob job);

	/**
	 * 
	 * @param job
	 * @param table
	 * @param expectedRows
	 *            the amount of rows (may be approximated) in the table or -1 if
	 *            the count could not be determined.
	 */
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows);

	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow);

	public void rowProcessingSuccess(AnalysisJob job, Table table);

	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob);

	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result);

	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable);

	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable);

	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable);

	public void errorUknown(AnalysisJob job, Throwable throwable);
}
