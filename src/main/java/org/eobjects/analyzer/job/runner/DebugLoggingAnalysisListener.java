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

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eobjects.metamodel.schema.Table;

/**
 * AnalysisListener used for DEBUG level logging. This listener is obviously
 * very verbose.
 * 
 * @author Kasper Sørensen
 */
public class DebugLoggingAnalysisListener implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(DebugLoggingAnalysisListener.class);

	/**
	 * @return whether or not the debug logging level is enabled. Can be used to
	 *         find out of it is even feasable to add this listener or not.
	 */
	public static boolean isEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void jobBegin(AnalysisJob job) {
		logger.debug("jobBegin({})", job);
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
		logger.debug("jobSuccess({})", job);
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
		logger.debug("rowProcessingBegin({}, {}, {})", new Object[] { job, table, expectedRows });
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
		logger.debug("rowProcessingProgress({}, {}, {})", new Object[] { job, table, currentRow });
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
		logger.debug("rowProcessingSuccess({}, {})", new Object[] { job, table });
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
		logger.debug("analyzerBegin({}, {})", new Object[] { job, analyzerJob });
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
		logger.debug("analyzerSuccess({}, {})", new Object[] { job, analyzerJob, result });
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		logger.debug("errorInFilter(" + job + "," + filterJob + ")", throwable);
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		logger.debug("errorInTransformer(" + job + "," + transformerJob + ")", throwable);
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		logger.debug("errorInAnalyzer(" + job + "," + analyzerJob + ")", throwable);
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		logger.debug("errorUknown(" + job + ")", throwable);
	}
}
