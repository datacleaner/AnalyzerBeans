package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public interface AnalyzerLifeCycleCallback {

	public void onEvent(LifeCycleState state, Analyzer<?> analyzerBean,
			AnalyzerBeanDescriptor<?> descriptor);
}
