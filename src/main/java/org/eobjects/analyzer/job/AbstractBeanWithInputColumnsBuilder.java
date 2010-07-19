package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

@SuppressWarnings("unchecked")
class AbstractBeanWithInputColumnsBuilder<D extends AbstractBeanDescriptor, B>
		extends AbstractBeanJobBuilder<D, B> {

	private List<InputColumn<?>> _inputColumns = new ArrayList<InputColumn<?>>();

	public AbstractBeanWithInputColumnsBuilder(D descriptor, Class<B> builderClass) {
		super(descriptor, builderClass);
	}

	/**
	 * 
	 * @param inputColumn
	 * @throws IllegalArgumentException
	 *             if the input column data type family doesn't match the types
	 *             accepted by this transformer.
	 */
	public B addInputColumn(InputColumn<?> inputColumn)
			throws IllegalArgumentException {
		DataTypeFamily expectedDataTypeFamily = getDescriptor()
				.getInputDataTypeFamily();
		if (expectedDataTypeFamily != DataTypeFamily.UNDEFINED) {
			DataTypeFamily actualDataTypeFamily = inputColumn
					.getDataTypeFamily();
			if (expectedDataTypeFamily != actualDataTypeFamily) {
				throw new IllegalArgumentException(
						"Unsupported InputColumn type: " + actualDataTypeFamily
								+ ", expected: " + expectedDataTypeFamily);
			}
		}
		_inputColumns.add(inputColumn);
		return (B) this;
	}

	public B addInputColumns(Collection<InputColumn<?>> inputColumns) {
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
		_inputColumns.remove(inputColumn);
		// TODO: Notify consumers
		return (B) this;
	}

	public List<InputColumn<?>> getInputColumns() {
		return Collections.unmodifiableList(_inputColumns);
	}

	public boolean isConfigured() {
		if (_inputColumns.isEmpty()) {
			// no input given
			return false;
		}

		if (!super.isConfigured()) {
			return false;
		}

		ConfiguredDescriptor configuredDescriptorForInput = getDescriptor()
				.getConfiguredDescriptorForInput();
		if (!configuredDescriptorForInput.isArray()) {
			if (_inputColumns.size() != 1) {
				// exactly one input column is required
				return false;
			}
		}
		return true;
	}

	@Override
	public Map<ConfiguredDescriptor, Object> getConfiguredProperties() {
		Map<ConfiguredDescriptor, Object> properties = new HashMap<ConfiguredDescriptor, Object>(
				super.getConfiguredProperties());

		// explicitly add the input columns (because they are handled as a
		// separate variable in this builder
		List<InputColumn<?>> inputColumns = getInputColumns();
		properties.put(getDescriptor().getConfiguredDescriptorForInput(),
				inputColumns.toArray(new InputColumn<?>[inputColumns.size()]));

		return Collections.unmodifiableMap(properties);
	}
}
