package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

public class AssignConfiguredRowProcessingCallback extends
		AssignConfiguredCallback {

	private Column[] tableColumns;

	public AssignConfiguredRowProcessingCallback(AnalysisJob job,
			SchemaNavigator schemaNavigator, Column[] tableColumns) {
		super(job, schemaNavigator);
		this.tableColumns = tableColumns;
	}

	@Override
	protected Object getConfiguredValue(
			ConfiguredDescriptor configuredDescriptor) {
		if (configuredDescriptor.isArray() && configuredDescriptor.isColumn()) {
			return tableColumns;
		}
		if (configuredDescriptor.isArray() && configuredDescriptor.isInputColumn()) {
			InputColumn<?>[] inputColumns = new InputColumn[tableColumns.length];
			for (int i = 0; i < tableColumns.length; i++) {
				inputColumns[i] = new MetaModelInputColumn(tableColumns[i]);
			}
			return inputColumns;
		}
		return super.getConfiguredValue(configuredDescriptor);
	}
}
