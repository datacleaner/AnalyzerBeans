package org.eobjects.analyzer.job;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.util.CollectionUtils;

@SuppressWarnings("unchecked")
class AbstractBeanWithInputColumnsBuilder<D extends BeanDescriptor<E>, E, B>
		extends AbstractBeanJobBuilder<D, E, B> {

	public AbstractBeanWithInputColumnsBuilder(D descriptor,
			Class<?> builderClass) {
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

		ConfiguredPropertyDescriptor configuredPropertyForInput = getDescriptor()
				.getConfiguredPropertyForInput();
		Object inputColumns = configuredPropertyForInput
				.getValue(getConfigurableBean());
		if (inputColumns == null) {
			if (configuredPropertyForInput.isArray()) {
				inputColumns = new InputColumn[] { inputColumn };
			} else {
				inputColumns = inputColumn;
			}
		} else {
			inputColumns = CollectionUtils.array(InputColumn.class, inputColumns, inputColumn);
		}
		configuredPropertyForInput
				.setValue(getConfigurableBean(), inputColumns);

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
		ConfiguredPropertyDescriptor configuredPropertyForInput = getDescriptor()
				.getConfiguredPropertyForInput();
		Object inputColumns = configuredPropertyForInput
				.getValue(getConfigurableBean());
		if (inputColumns != null) {
			if (inputColumns == inputColumn) {
				inputColumns = null;
			} else {
				if (inputColumns.getClass().isArray()) {
					inputColumns = CollectionUtils.arrayRemove(inputColumns,
							inputColumn);
				}
			}
			configuredPropertyForInput.setValue(getConfigurableBean(),
					inputColumns);
		}
		return (B) this;
	}

	public List<InputColumn<?>> getInputColumns() {
		ConfiguredPropertyDescriptor configuredPropertyForInput = getDescriptor()
				.getConfiguredPropertyForInput();
		Object inputColumns = configuredPropertyForInput
				.getValue(getConfigurableBean());
		if (inputColumns == null) {
			return Collections.emptyList();
		}
		List<InputColumn<?>> result;
		if (inputColumns.getClass().isArray()) {
			int length = Array.getLength(inputColumns);
			result = new ArrayList<InputColumn<?>>(length);
			for (int i = 0; i < length; i++) {
				result.add((InputColumn<?>) Array.get(inputColumns, i));
			}
		} else {
			result = new ArrayList<InputColumn<?>>(1);
			result.add((InputColumn<?>) inputColumns);
		}
		return Collections.unmodifiableList(result);
	}
}
