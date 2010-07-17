package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AbstractBeanDescriptor;

public interface BeanJob<E extends AbstractBeanDescriptor> {

	public E getDescriptor();

	public BeanConfiguration getConfiguration();

	public InputColumn<?>[] getInput();
}
