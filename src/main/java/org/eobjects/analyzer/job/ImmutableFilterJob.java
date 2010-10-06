package org.eobjects.analyzer.job;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.util.CollectionUtils;

public final class ImmutableFilterJob implements FilterJob {

	private final FilterBeanDescriptor<?, ?> _descriptor;
	private final BeanConfiguration _beanConfiguration;
	private final FilterOutcome _requirement;

	public ImmutableFilterJob(FilterBeanDescriptor<?, ?> descriptor, BeanConfiguration beanConfiguration,
			FilterOutcome requirement) {
		_descriptor = descriptor;
		_beanConfiguration = beanConfiguration;
		_requirement = LazyFilterOutcome.load(requirement);
	}

	@Override
	public FilterBeanDescriptor<?, ?> getDescriptor() {
		return _descriptor;
	}

	@Override
	public BeanConfiguration getConfiguration() {
		return _beanConfiguration;
	}

	@Override
	public InputColumn<?>[] getInput() {
		List<InputColumn<?>> result = new LinkedList<InputColumn<?>>();
		Set<ConfiguredPropertyDescriptor> propertiesForInput = _descriptor.getConfiguredPropertiesForInput();
		for (ConfiguredPropertyDescriptor propertyDescriptor : propertiesForInput) {
			Object property = _beanConfiguration.getProperty(propertyDescriptor);
			InputColumn<?>[] inputs = CollectionUtils.arrayOf(InputColumn.class, property);
			if (inputs != null) {
				for (InputColumn<?> inputColumn : inputs) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn<?>[result.size()]);
	}

	@Override
	public FilterOutcome getRequirement() {
		return _requirement;
	}

	@Override
	public FilterOutcome[] getOutcomes() {
		EnumSet<?> categories = _descriptor.getCategories();
		FilterOutcome[] outcomes = new FilterOutcome[categories.size()];
		int i = 0;
		for (Enum<?> category : categories) {
			outcomes[i] = new ImmutableFilterOutcome(this, category);
			i++;
		}
		return outcomes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_beanConfiguration == null) ? 0 : _beanConfiguration.hashCode());
		result = prime * result + ((_descriptor == null) ? 0 : _descriptor.hashCode());
		result = prime * result + ((_requirement == null) ? 0 : _requirement.hashCode());
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
		ImmutableFilterJob other = (ImmutableFilterJob) obj;
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
		if (_requirement == null) {
			if (other._requirement != null)
				return false;
		} else if (!_requirement.equals(other._requirement))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableFilterJob[filter=" + _descriptor.getDisplayName() + "]";
	}
}
