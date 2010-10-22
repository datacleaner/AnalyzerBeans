package org.eobjects.analyzer.job.builder;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.util.CollectionUtils;

@SuppressWarnings("unchecked")
class AbstractBeanWithInputColumnsBuilder<D extends BeanDescriptor<E>, E, B> extends AbstractBeanJobBuilder<D, E, B> {

	private FilterOutcome _requirement;

	public AbstractBeanWithInputColumnsBuilder(D descriptor, Class<?> builderClass) {
		super(descriptor, builderClass);
	}

	/**
	 * 
	 * @param inputColumn
	 * @throws IllegalArgumentException
	 *             if the input column data type family doesn't match the types
	 *             accepted by this transformer.
	 */
	public B addInputColumn(InputColumn<?> inputColumn) throws IllegalArgumentException {
		Set<ConfiguredPropertyDescriptor> propertyDescriptors = getDescriptor().getConfiguredPropertiesForInput();
		if (propertyDescriptors.size() == 1) {
			ConfiguredPropertyDescriptor propertyDescriptor = propertyDescriptors.iterator().next();
			return addInputColumn(inputColumn, propertyDescriptor);
		} else {
			throw new UnsupportedOperationException("There are " + propertyDescriptors.size()
					+ " named input columns, please specify which one to configure");
		}
	}

	public B addInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor) {
		DataTypeFamily expectedDataTypeFamily = propertyDescriptor.getInputColumnDataTypeFamily();
		if (expectedDataTypeFamily != DataTypeFamily.UNDEFINED) {
			DataTypeFamily actualDataTypeFamily = inputColumn.getDataTypeFamily();
			if (expectedDataTypeFamily != actualDataTypeFamily) {
				throw new IllegalArgumentException("Unsupported InputColumn type: " + actualDataTypeFamily + ", expected: "
						+ expectedDataTypeFamily);
			}
		}

		Object inputColumns = propertyDescriptor.getValue(getConfigurableBean());
		if (inputColumns == null) {
			if (propertyDescriptor.isArray()) {
				inputColumns = new InputColumn[] { inputColumn };
			} else {
				inputColumns = inputColumn;
			}
		} else {
			inputColumns = CollectionUtils.array(InputColumn.class, inputColumns, inputColumn);
		}
		setConfiguredProperty(propertyDescriptor, inputColumns);
		return (B) this;
	}

	public B addInputColumns(Collection<? extends InputColumn<?>> inputColumns) {
		for (InputColumn<?> inputColumn : inputColumns) {
			addInputColumn(inputColumn);
		}
		return (B) this;
	}

	public B addInputColumns(InputColumn<?>... inputColumns) {
		for (InputColumn<?> inputColumn : inputColumns) {
			addInputColumn(inputColumn);
		}
		return (B) this;
	}

	public B removeInputColumn(InputColumn<?> inputColumn) {
		Set<ConfiguredPropertyDescriptor> propertyDescriptors = getDescriptor().getConfiguredPropertiesForInput();
		if (propertyDescriptors.size() == 1) {
			ConfiguredPropertyDescriptor propertyDescriptor = propertyDescriptors.iterator().next();
			return removeInputColumn(inputColumn, propertyDescriptor);
		} else {
			throw new UnsupportedOperationException("There are " + propertyDescriptors.size()
					+ " named input columns, please specify which one to configure");
		}
	}

	public B removeInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor) {
		Object inputColumns = propertyDescriptor.getValue(getConfigurableBean());
		if (inputColumns != null) {
			if (inputColumns == inputColumn) {
				inputColumns = null;
			} else {
				if (inputColumns.getClass().isArray()) {
					inputColumns = CollectionUtils.arrayRemove(inputColumns, inputColumn);
				}
			}
			propertyDescriptor.setValue(getConfigurableBean(), inputColumns);
			onConfigurationChanged();
		}
		return (B) this;
	}

	public List<InputColumn<?>> getInputColumns() {
		List<InputColumn<?>> result = new LinkedList<InputColumn<?>>();
		Set<ConfiguredPropertyDescriptor> configuredPropertiesForInput = getDescriptor().getConfiguredPropertiesForInput();
		for (ConfiguredPropertyDescriptor configuredPropertyForInput : configuredPropertiesForInput) {
			Object inputColumns = configuredPropertyForInput.getValue(getConfigurableBean());
			if (inputColumns == null) {
				return Collections.emptyList();
			}
			if (inputColumns.getClass().isArray()) {
				int length = Array.getLength(inputColumns);
				for (int i = 0; i < length; i++) {
					result.add((InputColumn<?>) Array.get(inputColumns, i));
				}
			} else {
				result.add((InputColumn<?>) inputColumns);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public FilterOutcome getRequirement() {
		return _requirement;
	}

	public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		EnumSet<?> categories = filterJobBuilder.getDescriptor().getCategories();
		if (!categories.contains(category)) {
			throw new IllegalArgumentException("No such category found in available outcomes: " + category);
		}
		setRequirement(new LazyFilterOutcome(filterJobBuilder, category));
	}

	public void setRequirement(FilterOutcome requirement) {
		_requirement = requirement;
	}

	public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, String category) {
		EnumSet<?> categories = filterJobBuilder.getDescriptor().getCategories();
		for (Enum<?> c : categories) {
			if (c.name().equals(category)) {
				setRequirement(new LazyFilterOutcome(filterJobBuilder, c));
				return;
			}
		}
		throw new IllegalArgumentException("No such category found in available outcomes: " + category);
	}

}
