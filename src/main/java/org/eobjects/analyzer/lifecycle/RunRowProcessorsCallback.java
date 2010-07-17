package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.runner.AnalysisRowProcessor;

public class RunRowProcessorsCallback implements AnalyzerLifeCycleCallback {

	private AnalysisRowProcessor processor;

	public RunRowProcessorsCallback(AnalysisRowProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isRowProcessingAnalyzer();

		RowProcessingAnalyzer rowProcessingAnalyzer = (RowProcessingAnalyzer) analyzerBean;
		processor.addEndPoint(rowProcessingAnalyzer);
	}

}
