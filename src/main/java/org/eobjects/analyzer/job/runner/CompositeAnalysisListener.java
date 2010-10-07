package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.schema.Table;

public final class CompositeAnalysisListener implements AnalysisListener {

	private final AnalysisListener[] _delegates;

	public CompositeAnalysisListener(AnalysisListener firstDelegate, AnalysisListener... delegates) {
		if (delegates == null || delegates.length == 0) {
			delegates = new AnalysisListener[0];
		}
		_delegates = new AnalysisListener[1 + delegates.length];
		_delegates[0] = firstDelegate;
		for (int i = 0; i < delegates.length; i++) {
			_delegates[i + 1] = delegates[i];
		}
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
