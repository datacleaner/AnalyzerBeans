package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class ImmutableAnalyzerJob implements AnalyzerJob {

	private AnalyzerBeanDescriptor _descriptor;
	private BeanConfiguration _beanConfiguration;

	public ImmutableAnalyzerJob(AnalyzerBeanDescriptor descriptor,
			BeanConfiguration beanConfiguration) {
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public AnalyzerBeanDescriptor getDescriptor() {
		return _descriptor;
	}

	@Override
	public BeanConfiguration getConfiguration() {
		return _beanConfiguration;
	}

	@Override
	public InputColumn<?>[] getInput() {
		return (InputColumn<?>[]) _beanConfiguration.getProperty(_descriptor
				.getConfiguredDescriptorForInput());
	}

}
