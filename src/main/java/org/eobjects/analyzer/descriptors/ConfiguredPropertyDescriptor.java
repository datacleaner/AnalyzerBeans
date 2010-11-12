package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.data.DataTypeFamily;

public interface ConfiguredPropertyDescriptor extends PropertyDescriptor {

	public boolean isInputColumn();

	/**
	 * @return a humanly readable description of the property
	 */
	public String getDescription();

	public DataTypeFamily getInputColumnDataTypeFamily();

	public boolean isRequired();
}
