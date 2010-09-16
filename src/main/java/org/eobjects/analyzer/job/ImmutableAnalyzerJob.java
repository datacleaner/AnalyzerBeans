package org.eobjects.analyzer.job;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.util.CollectionUtils;

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
		List<InputColumn<?>> result = new LinkedList<InputColumn<?>>();
		Set<ConfiguredPropertyDescriptor> propertiesForInput = _descriptor
				.getConfiguredPropertiesForInput();
		for (ConfiguredPropertyDescriptor propertyDescriptor : propertiesForInput) {
			Object property = _beanConfiguration
					.getProperty(propertyDescriptor);
			InputColumn<?>[] inputs = CollectionUtils.arrayOf(
					InputColumn.class, property);
			for (InputColumn<?> inputColumn : inputs) {
				result.add(inputColumn);
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
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
