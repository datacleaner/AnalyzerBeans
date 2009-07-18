package org.eobjects.analyzer.lifecycle;

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
		return super.getConfiguredValue(configuredDescriptor);
	}
}
