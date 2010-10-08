package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Table;

/**
 * AnalysisListener that will register errors and cancel the job if fatal errors
 * occur
 * 
 * @author Kasper Sørensen
 */
final class AnalysisErrorListener implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisErrorListener.class);
	private final AnalysisResultFutureImpl _analysisResultFuture;

	public AnalysisErrorListener(AnalysisResultFutureImpl analysisResultFuture) {
		_analysisResultFuture = analysisResultFuture;
	}

	@Override
	public void jobBegin(AnalysisJob job) {
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
	}

	private void storeError(AnalysisJob job, Throwable throwable) {
		logger.warn("Cancelling job because of error...", throwable);
		_analysisResultFuture.addError(throwable);
		_analysisResultFuture.cancel();
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		logger.warn("errorInFilter({},{},...)", job, filterJob);
		storeError(job, throwable);
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		logger.warn("errorInTransformer({},{},...)", job, transformerJob);
		storeError(job, throwable);
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		logger.warn("errorInAnalyzer({},{},...)", job, analyzerJob);
		storeError(job, throwable);
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		logger.warn("errorUnknown({},...)", job);
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
