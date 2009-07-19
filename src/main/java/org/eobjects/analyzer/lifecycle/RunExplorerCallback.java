package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.RunDescriptor;
import org.eobjects.analyzer.job.DataContextProvider;

public class RunExplorerCallback implements LifeCycleCallback {

	private DataContextProvider dataContextProvider;

	public RunExplorerCallback(DataContextProvider dataContextProvider) {
		this.dataContextProvider = dataContextProvider;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringExecutionType();

		List<RunDescriptor> runDescriptors = descriptor.getRunDescriptors();
		for (RunDescriptor runDescriptor : runDescriptors) {
			runDescriptor.explore(analyzerBean, dataContextProvider.getDataContext());
		}
	}

}
