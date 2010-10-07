package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.schema.Table;

/**
 * AnalysisListener that will cancel the job if fatal errors occur
 * 
 * @author Kasper Sørensen
 */
final class AnalysisCancellationListener implements AnalysisListener {

	private AnalysisResultFutureImpl _analysisResultFuture;

	public AnalysisCancellationListener(AnalysisResultFutureImpl analysisResultFuture) {
		_analysisResultFuture = analysisResultFuture;
	}

	@Override
	public void jobBegin(AnalysisJob job) {
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
	}

	private void storeError(AnalysisJob job, Throwable throwable) {
		_analysisResultFuture.addError(throwable);
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		storeError(job, throwable);
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		storeError(job, throwable);
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		storeError(job, throwable);
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		storeError(job, throwable);
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
	}
}
