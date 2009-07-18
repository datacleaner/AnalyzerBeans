package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.RunDescriptor;

import dk.eobjects.metamodel.DataContext;

public class RunExplorerCallback implements LifeCycleCallback {

	private DataContext dataContext;

	public RunExplorerCallback(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.RUN;
		assert descriptor.isExploringExecutionType();

		List<RunDescriptor> runDescriptors = descriptor.getRunDescriptors();
		for (RunDescriptor runDescriptor : runDescriptors) {
			runDescriptor.explore(analyzerBean, dataContext);
		}
	}

}
