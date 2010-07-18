package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationHelper;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;

public class AnalyzerJobBuilder {

	private List<InputColumn<?>> _inputColumns = new ArrayList<InputColumn<?>>();
	private Map<ConfiguredDescriptor, Object> _properties = new HashMap<ConfiguredDescriptor, Object>();
	private AnalyzerBeanDescriptor _descriptor;
	private AnalysisJobBuilder _analysisJobBuilder;

	public AnalyzerJobBuilder(AnalyzerBeanDescriptor descriptor,
			AnalysisJobBuilder analysisJobBuilder) {
		_descriptor = descriptor;
		_analysisJobBuilder = analysisJobBuilder;
	}

	public AnalysisJobBuilder parentBuilder() {
		return _analysisJobBuilder;
	}

	public AnalyzerBeanDescriptor getDescriptor() {
		return _descriptor;
	}

	public AnalyzerJobBuilder addInputColumn(InputColumn<?> inputColumn) {
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

	public AnalyzerJobBuilder addInputColumns(InputColumn<?>... inputColumns) {
		for (InputColumn<?> inputColumn : inputColumns) {
			addInputColumn(inputColumn);
		}
		return this;
	}

	public AnalyzerJobBuilder removeInputColumn(InputColumn<?> inputColumn) {
		_inputColumns.remove(inputColumn);
		// TODO: Notify consumers
		return this;
	}

	public List<InputColumn<?>> getInputColumns() {
		return Collections.unmodifiableList(_inputColumns);
	}

	public AnalyzerJobBuilder setConfiguredProperty(String configuredName,
			Object value) {
		ConfiguredDescriptor configuredDescriptor = _descriptor
				.getConfiguredDescriptor(configuredName);
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException("No such configured property: "
					+ configuredName);
		}
		return setConfiguredProperty(configuredDescriptor, value);
	}

	public AnalyzerJobBuilder setConfiguredProperty(
			ConfiguredDescriptor configuredDescriptor, Object value) {
		if (configuredDescriptor == null) {
			throw new IllegalArgumentException(
					"configuredDescriptor cannot be null");
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

	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException(
					"Analyzer job is not correctly configured");
		}
		return new ImmutableAnalyzerJob(_descriptor,
				new ImmutableBeanConfiguration(_properties));
	}

	@Override
	public String toString() {
		return "AnalyzerJobBuilder[analyzer=" + _descriptor.getDisplayName()
				+ ",inputColumns=" + _inputColumns + "]";
	}
}
