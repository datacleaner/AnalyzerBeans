package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public interface TransformerJob extends BeanJob<TransformerBeanDescriptor<?>> {
	
	public MutableInputColumn<?>[] getOutput();
}
