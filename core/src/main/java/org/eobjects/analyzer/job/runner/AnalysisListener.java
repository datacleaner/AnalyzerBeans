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

/**
 * Listener interface for analysis execution. Typically the user interface and
 * maybe also system services would implement this interface to be able to react
 * to progress notifications or errors occurring in the execution of the
 * analysis.
 * 
 * 
 */
public interface AnalysisListener {

	public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics);

	public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics);

	/**
	 * 
	 * @param job
	 * @param table
	 * @param expectedRows
	 *            the amount of rows (may be approximated) in the table or -1 if
	 *            the count could not be determined.
	 */
	public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics);

	public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow);

	public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics);

	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics);

	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result);

	public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable);

	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable);

	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable);

	public void errorUknown(AnalysisJob job, Throwable throwable);
}
