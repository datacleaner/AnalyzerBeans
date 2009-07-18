package org.eobjects.analyzer.lifecycle;

import java.util.List;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.InitializeDescriptor;

public class InitializeCallback implements LifeCycleCallback {

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.INITIALIZE;
		
		List<InitializeDescriptor> initializeDescriptors = descriptor.getInitializeDescriptors();
		for (InitializeDescriptor initializeDescriptor : initializeDescriptors) {
			initializeDescriptor.initialize(analyzerBean);
		}
	}

}
