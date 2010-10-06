package org.eobjects.analyzer.lifecycle;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

/**
 * Represents an instance of an @TransformerBean annotated class at runtime. The
 * TransformerBeanInstance class is responsible for performing life-cycle actions
 * at an per-instance level. This makes it possible to add callbacks at various
 * stages in the life-cycle of a TransformerBean
 */
public class TransformerBeanInstance extends AbstractBeanInstance<Transformer<?>> {

	public TransformerBeanInstance(TransformerBeanDescriptor<?> descriptor) {
		super(descriptor);
	}
}
