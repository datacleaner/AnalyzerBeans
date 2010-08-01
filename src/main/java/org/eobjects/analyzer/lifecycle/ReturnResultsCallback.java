package org.eobjects.analyzer.lifecycle;

import java.util.Collection;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.result.AnalyzerResult;

public class ReturnResultsCallback implements AnalyzerLifeCycleCallback {

	private Collection<AnalyzerResult> results;

	public ReturnResultsCallback(Collection<AnalyzerResult> results) {
		this.results = results;
	}

	@Override
	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RETURN_RESULTS;
		
		this.results.add(analyzerBean.getResult());
	}
}
