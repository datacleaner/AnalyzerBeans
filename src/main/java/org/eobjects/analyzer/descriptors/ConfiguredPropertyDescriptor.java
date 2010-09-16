package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.data.DataTypeFamily;

public interface ConfiguredPropertyDescriptor extends PropertyDescriptor {

	public boolean isInputColumn();
	
	public DataTypeFamily getInputColumnDataTypeFamily();

	public boolean isRequired();
}
