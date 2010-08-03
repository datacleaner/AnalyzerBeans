package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.data.DataTypeFamily;

public interface TransformerBeanDescriptor extends BeanDescriptor {

	public DataTypeFamily getOutputDataTypeFamily();
}
