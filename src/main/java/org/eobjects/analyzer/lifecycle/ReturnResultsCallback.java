package org.eobjects.analyzer.lifecycle;

import java.util.Collection;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.result.AnalyzerResult;

public final class ReturnResultsCallback implements AnalyzerLifeCycleCallback {

	private final Collection<AnalyzerResult> results;

	public ReturnResultsCallback(Collection<AnalyzerResult> results) {
		if (results == null) {
			throw new IllegalArgumentException("results cannot be null");
		}
		this.results = results;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean,
			AnalyzerBeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.RETURN_RESULTS;

		AnalyzerResult result = analyzerBean.getResult();
		if (result == null) {
			throw new IllegalStateException("Analyzer (" + analyzerBean
					+ ") returned null as a result");
		}
		this.results.add(result);
	}
}
