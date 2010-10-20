package org.eobjects.analyzer.job.builder;

/**
 * Listener interface for receiving notifications that analyzers are being added
 * or removed from a job that is being built.
 * 
 * @author Kasper Sørensen
 */
public interface AnalyzerChangeListener {

	public void onAdd(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder);

	public void onAdd(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder);

	public void onRemove(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder);

	public void onRemove(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder);
}
