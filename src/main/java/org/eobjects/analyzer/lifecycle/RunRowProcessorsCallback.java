package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.RunDescriptor;
import org.eobjects.analyzer.job.AnalysisRowProcessor;

public class RunRowProcessorsCallback implements LifeCycleCallback {

	private AnalysisRowProcessor processor;

	public RunRowProcessorsCallback(AnalysisRowProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isRowProcessingExecutionType();

		List<RunDescriptor> runDescriptors = descriptor.getRunDescriptors();

		for (RunDescriptor runDescriptor : runDescriptors) {
			// Register the EndPoint (Bean+RunDescriptor) to the processor which
			// will then handle shared row processing for multiple analyzers on
			// the same table/query
			processor.addEndPoint(analyzerBean, runDescriptor);
		}
	}

}
