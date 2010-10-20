package org.eobjects.analyzer.lifecycle;

import java.util.Set;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;

public final class InitializeCallback implements LifeCycleCallback {

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean, BeanDescriptor<?> descriptor) {
		assert state == LifeCycleState.INITIALIZE;

		Set<InitializeMethodDescriptor> initializeDescriptors = descriptor.getInitializeMethods();
		for (InitializeMethodDescriptor initializeDescriptor : initializeDescriptors) {
			initializeDescriptor.initialize(analyzerBean);
		}
	}

}
