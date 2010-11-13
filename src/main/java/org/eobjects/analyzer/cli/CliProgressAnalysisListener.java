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

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.schema.Table;

final class CliProgressAnalysisListener implements AnalysisListener {

	private Map<Table, AtomicInteger> rowCounts = new HashMap<Table, AtomicInteger>();

	@Override
	public void jobBegin(AnalysisJob job) {
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
		System.out.println("Analyzing " + expectedRows + " rows from table: " + table.getName());
		rowCounts.put(table, new AtomicInteger(0));
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
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
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
		System.out.println("Done processing rows from table: " + table.getName());
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
	}

}
