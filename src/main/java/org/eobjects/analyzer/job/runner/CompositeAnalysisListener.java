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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.schema.Table;

public final class CompositeAnalysisListener implements AnalysisListener {

	private final List<AnalysisListener> _delegates;

	public CompositeAnalysisListener(AnalysisListener firstDelegate, AnalysisListener... delegates) {
		_delegates = new ArrayList<AnalysisListener>(1 + delegates.length);
		_delegates.add(firstDelegate);
		for (AnalysisListener analysisListener : delegates) {
			addDelegate(analysisListener);
		}
	}

	public void addDelegate(AnalysisListener analysisListener) {
		_delegates.add(analysisListener);
	}

	@Override
	public void jobBegin(AnalysisJob job) {
		for (AnalysisListener delegate : _delegates) {
			delegate.jobBegin(job);
		}
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
		for (AnalysisListener delegate : _delegates) {
			delegate.jobSuccess(job);
		}
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
		for (AnalysisListener delegate : _delegates) {
			delegate.rowProcessingBegin(job, table, expectedRows);
		}
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
		for (AnalysisListener delegate : _delegates) {
			delegate.rowProcessingProgress(job, table, currentRow);
		}
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
		for (AnalysisListener delegate : _delegates) {
			delegate.rowProcessingSuccess(job, table);
		}
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
		for (AnalysisListener delegate : _delegates) {
			delegate.analyzerBegin(job, analyzerJob);
		}
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
		for (AnalysisListener delegate : _delegates) {
			delegate.analyzerSuccess(job, analyzerJob, result);
		}
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
		for (AnalysisListener delegate : _delegates) {
			delegate.errorInFilter(job, filterJob, throwable);
		}
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
		for (AnalysisListener delegate : _delegates) {
			delegate.errorInTransformer(job, transformerJob, throwable);
		}
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
		for (AnalysisListener delegate : _delegates) {
			delegate.errorInAnalyzer(job, analyzerJob, throwable);
		}
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
		for (AnalysisListener delegate : _delegates) {
			delegate.errorUknown(job, throwable);
		}
	}
}
