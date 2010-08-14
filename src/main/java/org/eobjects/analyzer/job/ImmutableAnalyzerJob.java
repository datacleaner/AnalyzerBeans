package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

final class ImmutableAnalyzerJob implements AnalyzerJob {

	private final AnalyzerBeanDescriptor<?> _descriptor;
	private final BeanConfiguration _beanConfiguration;

	public ImmutableAnalyzerJob(AnalyzerBeanDescriptor<?> descriptor,
			BeanConfiguration beanConfiguration) {
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
	}

	@Override
	public AnalyzerBeanDescriptor<?> getDescriptor() {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((_beanConfiguration == null) ? 0 : _beanConfiguration
						.hashCode());
		result = prime * result
				+ ((_descriptor == null) ? 0 : _descriptor.hashCode());
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
		ImmutableAnalyzerJob other = (ImmutableAnalyzerJob) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableAnalyzerJob[analyzer=" + _descriptor.getDisplayName()
				+ "]";
	}
}
