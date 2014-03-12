/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalysisListener used for INFO level logging. This listener will log
 * interesting progress information for each thousands rows being processed.
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
	public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
		// do nothing
	}

	@Override
	public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
		// do nothing
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
		logger.info("Beginning row processing of {}", metrics.getTable());
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
		if (currentRow > 0 && currentRow % 1000 == 0) {
			logger.info("Reading row no. {} in {}", new Object[] { currentRow, metrics.getTable().getName() });
		}
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
		// do nothing
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
		// do nothing
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
		// do nothing
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
		// do nothing
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		// do nothing
	}
}
