package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;

public interface ConfigurableBeanJob<E extends BeanDescriptor<?>> extends ComponentJob {
	
	public InputColumn<?>[] getInput();
	
	public Outcome getRequirement();

	public E getDescriptor();

	public BeanConfiguration getConfiguration();
}
