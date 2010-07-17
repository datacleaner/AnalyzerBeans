package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.AnnotationHelper;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;

public class TransformerJobBuilder {

	private List<InputColumn<?>> _inputColumns = new ArrayList<InputColumn<?>>();
	private LinkedList<MutableInputColumn<?>> _outputColumns = new LinkedList<MutableInputColumn<?>>();
	private Map<ConfiguredDescriptor, Object> _properties = new HashMap<ConfiguredDescriptor, Object>();
	private AnalysisJobBuilder _analysisJobBuilder;
	private TransformerBeanDescriptor _descriptor;
	private IdGenerator _idGenerator;

	public TransformerJobBuilder(TransformerBeanDescriptor descriptor,
			IdGenerator idGenerator, AnalysisJobBuilder analysisJobBuilder) {
		_descriptor = descriptor;
		_idGenerator = idGenerator;
		_analysisJobBuilder = analysisJobBuilder;
	}

	public AnalysisJobBuilder parentBuilder() {
		return _analysisJobBuilder;
	}

	public TransformerBeanDescriptor getDescriptor() {
		return _descriptor;
	}

	public TransformerJobBuilder addInputColumn(InputColumn<?> inputColumn) {
		DataTypeFamily expectedDataTypeFamily = _descriptor
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
		return this;
	}

	public TransformerJobBuilder addInputColumns(InputColumn<?>... inputColumns) {
		for (InputColumn<?> inputColumn : inputColumns) {
			addInputColumn(inputColumn);
		}
		return this;
	}

	public TransformerJobBuilder removeInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.remove(inputColumn);
		// TODO: Notify consumers
		return this;
	}

	public List<InputColumn<?>> getInputColumns() {
		return Collections.unmodifiableList(_inputColumns);
	}

	public TransformerJobBuilder setConfiguredProperty(String configuredName,
			Object value) {
		ConfiguredDescriptor configuredDescriptor = _descriptor
				.getConfiguredDescriptor(configuredName);
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException("No such configured property: "
					+ configuredName);
		}
		return setConfiguredProperty(configuredDescriptor, value);
	}

	public TransformerJobBuilder setConfiguredProperty(
			ConfiguredDescriptor configuredDescriptor, Object value) {
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException("configuredDescriptor cannot be null");
		}
		if (value != null) {
			if (!AnnotationHelper.is(value.getClass(),
					configuredDescriptor.getBaseType())) {
				throw new IllegalArgumentException("Invalid value type: "
						+ value.getClass().getName() + ", expected: "
						+ configuredDescriptor.getBaseType().getName());
			}
		}
		_properties.put(configuredDescriptor, value);
		return this;
	}

	public boolean isConfigured() {
		if (_inputColumns.isEmpty()) {
			// no input given
			return false;
		}
		ConfiguredDescriptor configuredDescriptorForInput = _descriptor
				.getConfiguredDescriptorForInput();
		if (!configuredDescriptorForInput.isArray()) {
			if (_inputColumns.size() != 1) {
				// exactly one input column is required
				return false;
			}
		}

		List<ConfiguredDescriptor> configuredDescriptors = new ArrayList<ConfiguredDescriptor>(
				_descriptor.getConfiguredDescriptors());
		configuredDescriptors.remove(configuredDescriptorForInput);

		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			if (!_properties.containsKey(configuredDescriptor)) {
				return false;
			}
		}

		return true;
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		TransformerBeanInstance transformerBeanInstance = new TransformerBeanInstance(
				_descriptor);
		// TODO: Configure the instance

		transformerBeanInstance.assignConfigured();

		int expectedCols = transformerBeanInstance.getBean().getOutputColumns();
		int existingCols = _outputColumns.size();
		if (expectedCols != existingCols) {
			int colDiff = expectedCols - existingCols;
			if (colDiff > 0) {
				for (int i = 0; i < colDiff; i++) {
					String name = _descriptor.getDisplayName() + " "
							+ (_outputColumns.size() + 1);
					DataTypeFamily type = _descriptor.getOutputDataTypeFamily();
					_outputColumns.add(new TransformedInputColumn<Object>(name,
							type, _idGenerator));
				}
			} else if (colDiff < 0) {
				for (int i = 0; i < Math.abs(colDiff); i++) {
					// remove from the tail
					_outputColumns.removeLast();
				}

				// TODO: Notify consumers of the removed columns
			}
		}

		return _outputColumns;
	}
}
