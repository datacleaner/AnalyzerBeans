package org.eobjects.analyzer.lifecycle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ResultDescriptor;
import org.eobjects.analyzer.result.AnalyzerResult;

public class ReturnResultsCallback implements AnalyzerLifeCycleCallback {

	private Collection<AnalyzerResult> results;

	public ReturnResultsCallback(Collection<AnalyzerResult> results) {
		this.results = results;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RETURN_RESULTS;

		List<AnalyzerResult> resultsForBean = getResults(analyzerBean,
				descriptor);
		for (AnalyzerResult result : resultsForBean) {
			this.results.add(result);
		}
	}

	protected List<AnalyzerResult> getResults(Object analyzerBean,
			AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		List<AnalyzerResult> results = new LinkedList<AnalyzerResult>();
		List<ResultDescriptor> resultDescriptors = analyzerBeanDescriptor
				.getResultDescriptors();
		for (ResultDescriptor resultDescriptor : resultDescriptors) {
			if (resultDescriptor.isArray()) {
				AnalyzerResult[] analysisResult = resultDescriptor
						.getResults(analyzerBean);
				for (AnalyzerResult result : analysisResult) {
					results.add(result);
				}
			} else {
				AnalyzerResult result = resultDescriptor
						.getResult(analyzerBean);
				results.add(result);
			}
		}
		return results;
	}
}
