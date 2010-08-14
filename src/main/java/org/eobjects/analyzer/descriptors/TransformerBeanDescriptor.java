package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.data.DataTypeFamily;

public interface TransformerBeanDescriptor<B extends Transformer<?>> extends BeanDescriptor<B> {

	public DataTypeFamily getOutputDataTypeFamily();
}
