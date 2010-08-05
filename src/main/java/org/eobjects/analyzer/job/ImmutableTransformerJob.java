package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

final class ImmutableTransformerJob implements TransformerJob {

	private final TransformerBeanDescriptor _descriptor;
	private final BeanConfiguration _beanConfiguration;
	private final List<MutableInputColumn<?>> _output;

	public ImmutableTransformerJob(TransformerBeanDescriptor descriptor,
			BeanConfiguration beanConfiguration,
			Collection<MutableInputColumn<?>> output) {
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
		_output = Collections
				.unmodifiableList(new ArrayList<MutableInputColumn<?>>(output));
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
				.getConfiguredPropertyForInput());
	}

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return _output.toArray(new MutableInputColumn<?>[_output.size()]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((_beanConfiguration == null) ? 0 : _beanConfiguration
						.hashCode());
		result = prime * result
				+ ((_descriptor == null) ? 0 : _descriptor.hashCode());
		result = prime * result + ((_output == null) ? 0 : _output.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableTransformerJob other = (ImmutableTransformerJob) obj;
		if (_beanConfiguration == null) {
			if (other._beanConfiguration != null)
				return false;
		} else if (!_beanConfiguration.equals(other._beanConfiguration))
			return false;
		if (_descriptor == null) {
			if (other._descriptor != null)
				return false;
		} else if (!_descriptor.equals(other._descriptor))
			return false;
		if (_output == null) {
			if (other._output != null)
				return false;
		} else if (!_output.equals(other._output))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableTransformerJob[transformer="
				+ _descriptor.getDisplayName() + "]";
	}
}
