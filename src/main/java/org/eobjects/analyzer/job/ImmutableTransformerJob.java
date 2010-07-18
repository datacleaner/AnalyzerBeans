package org.eobjects.analyzer.job;

import java.util.Collection;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public class ImmutableTransformerJob implements TransformerJob {

	private TransformerBeanDescriptor _descriptor;
	private BeanConfiguration _beanConfiguration;
	private MutableInputColumn<?>[] _output;

	public ImmutableTransformerJob(TransformerBeanDescriptor descriptor,
			BeanConfiguration beanConfiguration, Collection<MutableInputColumn<?>> output) {
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
		_output = output.toArray(new MutableInputColumn<?>[output.size()]);;
	}

	@Override
	public TransformerBeanDescriptor getDescriptor() {
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

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return _output;
	}

}
