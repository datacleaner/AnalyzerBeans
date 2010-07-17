package org.eobjects.analyzer.job;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public interface TransformerJob {
	
	public TransformerBeanDescriptor getDescriptor();
	
	public ComponentConfiguration getConfiguration();
	
	public InputColumn<?>[] getInput();
	
	public MutableInputColumn<?>[] getOutput();
}
