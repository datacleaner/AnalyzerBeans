package org.eobjects.analyzer.job;

import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public interface BeanConfiguration {

	public Map<String, Boolean> getBooleanProperties();

	public Map<String, Number> getNumberProperties();

	public Map<String, String> getStringProperties();

	public Map<String, InputColumn<?>> getInputColumnProperties();

	public Map<String, Column> getColumnProperties();

	public Map<String, Table> getTableProperties();

	public Map<String, Schema> getSchemaProperties();
	
	// TODO: Reference data?
}