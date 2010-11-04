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
 * AnalysisListener used for INFO level logging. This listener will log
 * interesting progress information for each thousands rows being processed.
 * 
 * @author Kasper Sørensen
 */
public class InfoLoggingAnalysisListener implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(InfoLoggingAnalysisListener.class);

	/**
	 * @return whether or not the debug logging level is enabled. Can be used to
	 *         find out of it is even feasable to add this listener or not.
	 */
	public static boolean isEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void jobBegin(AnalysisJob job) {
		// do nothing
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
		// do nothing
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
		logger.info("Beginning row processing of {} rows in {}", new Object[] { expectedRows, table });
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
		if (currentRow > 0 && currentRow % 1000 == 0) {
			logger.info("Reading row no. {} in {}", new Object[] { currentRow, table });
		}
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
		// do nothing
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
		// do nothing
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
		// do nothing
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		// do nothing
	}
}
