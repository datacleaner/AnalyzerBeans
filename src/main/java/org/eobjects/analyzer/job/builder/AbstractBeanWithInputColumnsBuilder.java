/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.util.CollectionUtils;

@SuppressWarnings("unchecked")
public class AbstractBeanWithInputColumnsBuilder<D extends BeanDescriptor<E>, E, B> extends AbstractBeanJobBuilder<D, E, B>
		implements InputColumnSinkJob, OutcomeSinkJob {

	private Outcome _requirement;

	public AbstractBeanWithInputColumnsBuilder(AnalysisJobBuilder analysisJobBuilder, D descriptor, Class<?> builderClass) {
		super(analysisJobBuilder, descriptor, builderClass);
	}

	/**
	 * Removes/clears all input columns
	 */
	public void clearInputColumns() {
		Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor().getConfiguredPropertiesForInput();
		for (ConfiguredPropertyDescriptor configuredProperty : configuredProperties) {
			if (configuredProperty.isArray()) {
				setConfiguredProperty(configuredProperty, new InputColumn[0]);
			} else {
				setConfiguredProperty(configuredProperty, null);
			}
		}
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

	// this is the main "addInputColumn" method that the other similar methods
	// delegate to
	public B addInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor) {
		DataTypeFamily expectedDataTypeFamily = propertyDescriptor.getInputColumnDataTypeFamily();
		if (expectedDataTypeFamily != DataTypeFamily.UNDEFINED) {
			DataTypeFamily actualDataTypeFamily = inputColumn.getDataTypeFamily();
			if (expectedDataTypeFamily != actualDataTypeFamily) {
				throw new IllegalArgumentException("Unsupported InputColumn type: " + actualDataTypeFamily + ", expected: "
						+ expectedDataTypeFamily);
			}
		}

		Object inputColumns = getConfiguredProperty(propertyDescriptor);
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
		Object inputColumns = getConfiguredProperty(propertyDescriptor);
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
			Object inputColumns = getConfiguredProperty(configuredPropertyForInput);
			if (inputColumns != null) {
				if (inputColumns.getClass().isArray()) {
					int length = Array.getLength(inputColumns);
					for (int i = 0; i < length; i++) {
						result.add((InputColumn<?>) Array.get(inputColumns, i));
					}
				} else {
					result.add((InputColumn<?>) inputColumns);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public Outcome getRequirement() {
		return _requirement;
	}

	public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		EnumSet<?> categories = filterJobBuilder.getDescriptor().getOutcomeCategories();
		if (!categories.contains(category)) {
			throw new IllegalArgumentException("No such category found in available outcomes: " + category);
		}
		setRequirement(filterJobBuilder.getOutcome(category));
	}

	public void setRequirement(Outcome requirement) {
		if (_requirement != requirement) {
			_requirement = requirement;
			onRequirementChanged();
		}
	}

	/**
	 * method that can be used by sub-classes to add callback logic when the
	 * requirement of the bean changes
	 */
	public void onRequirementChanged() {
	}

	public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, String category) {
		EnumSet<?> categories = filterJobBuilder.getDescriptor().getOutcomeCategories();
		for (Enum<?> c : categories) {
			if (c.name().equals(category)) {
				setRequirement(filterJobBuilder.getOutcome(c));
				return;
			}
		}
		throw new IllegalArgumentException("No such category found in available outcomes: " + category);
	}

	@Override
	public Outcome[] getRequirements() {
		if (_requirement == null) {
			return new Outcome[0];
		}
		return new Outcome[] { _requirement };
	}

	@Override
	public InputColumn<?>[] getInput() {
		List<InputColumn<?>> inputColumns = getInputColumns();
		return inputColumns.toArray(new InputColumn[inputColumns.size()]);
	}

}
