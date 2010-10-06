package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;

public interface BeanJob<E extends BeanDescriptor<?>> {

	public E getDescriptor();

	public BeanConfiguration getConfiguration();

	public InputColumn<?>[] getInput();
	
	public FilterOutcome getRequirement();
}
