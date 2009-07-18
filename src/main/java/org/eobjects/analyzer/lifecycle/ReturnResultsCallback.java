package org.eobjects.analyzer.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ResultDescriptor;
import org.eobjects.analyzer.result.AnalysisResultImpl;
import org.eobjects.analyzer.result.AnalyzerBeanResult;

public class ReturnResultsCallback implements LifeCycleCallback {

	private AnalysisResultImpl analysisResult;

	public ReturnResultsCallback(AnalysisResultImpl results) {
		this.analysisResult = results;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RETURN_RESULTS;

		List<AnalyzerBeanResult> results = getResults(analyzerBean, descriptor);
		for (AnalyzerBeanResult result : results) {
			analysisResult.addResult(result);
		}
	}

	protected List<AnalyzerBeanResult> getResults(Object analyzerBean,
			AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		List<AnalyzerBeanResult> results = new LinkedList<AnalyzerBeanResult>();
		List<ResultDescriptor> resultDescriptors = analyzerBeanDescriptor
				.getResultDescriptors();
		for (ResultDescriptor resultDescriptor : resultDescriptors) {
			if (resultDescriptor.isArray()) {
				AnalyzerBeanResult[] analysisResult = resultDescriptor
						.getResults(analyzerBean);
				for (AnalyzerBeanResult result : analysisResult) {
					results.add(result);
				}
			} else {
				AnalyzerBeanResult result = resultDescriptor
						.getResult(analyzerBean);
				results.add(result);
			}
		}
		return results;
	}
}
