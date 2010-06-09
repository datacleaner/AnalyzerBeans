package org.eobjects.analyzer.descriptors;

import java.util.Collection;

public interface DescriptorProvider {

	public Collection<AnalyzerBeanDescriptor> getDescriptors();
	
	public AnalyzerBeanDescriptor getDescriptorForClass(Class<?> analyzerBeanClass);
}
