package org.eobjects.analyzer.job;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

/**
 * Represents the information used to run an AnalyzerBean. The AnalysisJob
 * itself contains no execution logic but is used as configuration objects to
 * the AnalysisRunner, so that it will know what to run.
 * 
 * The AnalysisJob class contains maps that create the link between @Configured
 * annotated methods and fields and the values to assign to them before
 * execution. To represent column and table objects in these maps String are
 * used to ensure easy serialisation. The strings used are equal to the
 * getPath() method on Column and Table objects.
 */
public final class AnalysisJob implements Serializable {

	private static final long serialVersionUID = 6996682701564901059L;

	private Class<?> _analyzerClass;
	private Map<String, Boolean[]> _booleanProperties;
	private Map<String, Integer[]> _integerProperties;
	private Map<String, Long[]> _longProperties;
	private Map<String, Double[]> _doubleProperties;
	private Map<String, String[]> _stringProperties;
	private Map<String, String[]> _columnProperties;
	private Map<String, String[]> _tableProperties;
	private Map<String, String[]> _schemaProperties;

	public AnalysisJob() {
		this(null);
	}

	public AnalysisJob(Class<?> analyzerClass) {
		_analyzerClass = analyzerClass;
		_booleanProperties = new HashMap<String, Boolean[]>();
		_integerProperties = new HashMap<String, Integer[]>();
		_longProperties = new HashMap<String, Long[]>();
		_doubleProperties = new HashMap<String, Double[]>();
		_stringProperties = new HashMap<String, String[]>();
		_columnProperties = new HashMap<String, String[]>();
		_tableProperties = new HashMap<String, String[]>();
	}

	public Class<?> getAnalyzerClass() {
		return _analyzerClass;
	}

	public void setAnalyzerClass(Class<?> analyzerClass) {
		_analyzerClass = analyzerClass;
	}

	public Map<String, Boolean[]> getBooleanProperties() {
		return Collections.unmodifiableMap(_booleanProperties);
	}

	public void setBooleanProperties(Map<String, Boolean[]> booleanProperties) {
		_booleanProperties = booleanProperties;
	}

	public void putBooleanProperty(String key, Boolean... value) {
		_booleanProperties.put(key, value);
	}

	public Map<String, String[]> getColumnProperties() {
		return Collections.unmodifiableMap(_columnProperties);
	}

	public void setColumnProperties(Map<String, String[]> columnProperties) {
		_columnProperties = columnProperties;
	}

	public void putColumnProperty(String key, String... columnPaths) {
		_columnProperties.put(key, columnPaths);
	}

	public void putColumnProperty(String key, Column... columns) {
		String[] strings = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			Column column = columns[i];
			strings[i] = column.getQualifiedLabel();
		}
		putColumnProperty(key, strings);
	}

	public Map<String, Integer[]> getIntegerProperties() {
		return Collections.unmodifiableMap(_integerProperties);
	}

	public void setIntegerProperties(Map<String, Integer[]> integerProperties) {
		_integerProperties = integerProperties;
	}

	public void putIntegerProperty(String key, Integer... value) {
		_integerProperties.put(key, value);
	}

	public Map<String, Long[]> getLongProperties() {
		return Collections.unmodifiableMap(_longProperties);
	}

	public void setLongProperties(Map<String, Long[]> longProperties) {
		_longProperties = longProperties;
	}

	public void putLongProperty(String key, Long... value) {
		_longProperties.put(key, value);
	}

	public Map<String, Double[]> getDoubleProperties() {
		return Collections.unmodifiableMap(_doubleProperties);
	}

	public void setDoubleProperties(Map<String, Double[]> doubleProperties) {
		_doubleProperties = doubleProperties;
	}

	public void putDoubleProperty(String key, Double... value) {
		_doubleProperties.put(key, value);
	}

	public Map<String, String[]> getStringProperties() {
		return Collections.unmodifiableMap(_stringProperties);
	}

	public void setStringProperties(Map<String, String[]> stringProperties) {
		_stringProperties = stringProperties;
	}

	public void putStringProperty(String key, String... value) {
		_stringProperties.put(key, value);
	}

	public Map<String, String[]> getTableProperties() {
		return Collections.unmodifiableMap(_tableProperties);
	}

	public void setTableProperties(Map<String, String[]> tableProperties) {
		_tableProperties = tableProperties;
	}

	public void putTableProperty(String key, String... tablePaths) {
		_tableProperties.put(key, tablePaths);
	}

	public void putTableProperty(String key, Table... tables) {
		String[] strings = new String[tables.length];
		for (int i = 0; i < tables.length; i++) {
			Table table = tables[i];
			strings[i] = table.getQualifiedLabel();
		}
		putTableProperty(key, strings);
	}

	public Map<String, String[]> getSchemaProperties() {
		return _schemaProperties;
	}

	public void setSchemaProperties(Map<String, String[]> schemaProperties) {
		_schemaProperties = schemaProperties;
	}

	public void putSchemaProperty(String key, String... tablePaths) {
		_schemaProperties.put(key, tablePaths);
	}

	public void putSchemaProperty(String key, Schema... schemas) {
		String[] strings = new String[schemas.length];
		for (int i = 0; i < schemas.length; i++) {
			Schema schema = schemas[i];
			strings[i] = schema.getName();
		}
		putSchemaProperty(key, strings);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("analyzerClass", _analyzerClass)
				.append("booleanProperties", _booleanProperties).append("integerProperties", _integerProperties)
				.append("longProperties", _longProperties).append("doubleProperties", _doubleProperties).append(
						"stringProperties", _stringProperties).append("columnProperties", _columnProperties).append(
						"tableProperties", _tableProperties).toString();
	}
}
