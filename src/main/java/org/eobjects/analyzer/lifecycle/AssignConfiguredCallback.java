package org.eobjects.analyzer.lifecycle;

import java.lang.reflect.Array;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

public class AssignConfiguredCallback implements LifeCycleCallback {

	private AnalysisJob job;
	private SchemaNavigator schemaNavigator;

	public AssignConfiguredCallback(AnalysisJob job,
			SchemaNavigator schemaNavigator) {
		this.job = job;
		this.schemaNavigator = schemaNavigator;
	}

	@Override
	public void onEvent(LifeCycleState state, Object analyzerBean,
			AnalyzerBeanDescriptor descriptor) {
		assert state == LifeCycleState.ASSIGN_CONFIGURED;

		List<ConfiguredDescriptor> configuredDescriptors = descriptor
				.getConfiguredDescriptors();
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			Object configuredValue = getConfiguredValue(configuredDescriptor);
			if (configuredValue == null) {
				throw new IllegalStateException(
						"No value for @Configured property: "
								+ configuredDescriptor.getName());
			} else {
				if (configuredDescriptor.isArray()) {
					configuredDescriptor.assignValue(analyzerBean,
							configuredValue);
				} else {
					if (configuredValue.getClass().isArray()) {
						configuredValue = Array.get(configuredValue, 0);
					}
					configuredDescriptor.assignValue(analyzerBean,
							configuredValue);
				}
			}
		}
	}

	protected Object getConfiguredValue(
			ConfiguredDescriptor configuredDescriptor) {
		Object configuredValue = null;
		String configuredName = configuredDescriptor.getName();
		if (configuredDescriptor.isBoolean()) {
			configuredValue = job.getBooleanProperties().get(configuredName);
		} else if (configuredDescriptor.isInteger()) {
			configuredValue = job.getIntegerProperties().get(configuredName);
		} else if (configuredDescriptor.isLong()) {
			configuredValue = job.getLongProperties().get(configuredName);
		} else if (configuredDescriptor.isDouble()) {
			configuredValue = job.getDoubleProperties().get(configuredName);
		} else if (configuredDescriptor.isString()) {
			configuredValue = job.getStringProperties().get(configuredName);
		} else if (configuredDescriptor.isColumn()) {
			String[] columnNames = job.getColumnProperties()
					.get(configuredName);
			Column[] physicalColumns = schemaNavigator
					.convertToColumns(columnNames);
			configuredValue = physicalColumns;
		} else if (configuredDescriptor.isInputColumn()) {
			String[] columnNames = job.getColumnProperties()
					.get(configuredName);
			Column[] physicalColumns = schemaNavigator
					.convertToColumns(columnNames);
			InputColumn<?>[] inputColumns = new InputColumn[physicalColumns.length];
			for (int i = 0; i < physicalColumns.length; i++) {
				inputColumns[i] = new MetaModelInputColumn(physicalColumns[i]);
			}
			configuredValue = inputColumns;
		} else if (configuredDescriptor.isTable()) {
			String[] tableNames = job.getTableProperties().get(configuredName);
			configuredValue = schemaNavigator.convertToTables(tableNames);
		} else if (configuredDescriptor.isSchema()) {
			String[] schemaNames = job.getSchemaProperties()
					.get(configuredName);
			configuredValue = schemaNavigator.convertToSchemas(schemaNames);
		}
		return configuredValue;
	}
}
