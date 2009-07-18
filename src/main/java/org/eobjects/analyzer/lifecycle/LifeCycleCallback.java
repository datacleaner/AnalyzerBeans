package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public interface LifeCycleCallback {

	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor);
}
