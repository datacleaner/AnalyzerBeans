package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public interface AnalyzerJob {

	public AnalyzerBeanDescriptor getDescriptor();

	public ComponentConfiguration getConfiguration();

	public InputColumn<?>[] getInput();
}
