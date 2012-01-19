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
package org.eobjects.analyzer.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalyzerMetrics;
import org.eobjects.analyzer.job.runner.ExplorerMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.result.AnalyzerResult;

import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CliProgressAnalysisListener implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(CliProgressAnalysisListener.class);

	private Map<Table, AtomicInteger> rowCounts = new HashMap<Table, AtomicInteger>();

	@Override
	public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
	}

	@Override
	public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
		Table table = metrics.getTable();
		logger.info("Analyzing rows from table: {}", table.getName());
		rowCounts.put(table, new AtomicInteger(0));
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
		Table table = metrics.getTable();
		AtomicInteger rowCount = rowCounts.get(table);
		if (rowCount != null) {
			int countBefore = rowCount.get();
			rowCount.lazySet(currentRow);
			int fiveHundredsBefore = countBefore / 500;
			int fiveHundredsAfter = currentRow / 500;
			if (fiveHundredsAfter != fiveHundredsBefore) {
				System.out.println(currentRow + " rows processed from table: " + table.getName());
			}
		}
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
		logger.info("Done processing rows from table: {}", metrics.getTable().getName());
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
	}

	@Override
	public void explorerBegin(AnalysisJob job, ExplorerJob explorerJob, ExplorerMetrics metrics) {
	}

	@Override
	public void explorerSuccess(AnalysisJob job, ExplorerJob explorerJob, AnalyzerResult result) {
	}

	@Override
	public void errorInExplorer(AnalysisJob job, ExplorerJob explorerJob, Throwable throwable) {
	}

}
