package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public class TransformerBeanInstance extends AbstractBeanInstance {

	public TransformerBeanInstance(TransformerBeanDescriptor descriptor) {
		super(descriptor);
	}
	
	@Override
	public Transformer<?> getBean() {
		return (Transformer<?>) super.getBean();
	}
}
