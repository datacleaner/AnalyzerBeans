package org.eobjects.analyzer.descriptors;

import java.util.Collection;

/**
 * An interface for components that provide descriptors for analyzer beans.
 * 
 * @author Kasper Sørensen
 */
public interface DescriptorProvider {

	public Collection<AnalyzerBeanDescriptor> getDescriptors();

	public AnalyzerBeanDescriptor getDescriptorForClass(
			Class<?> analyzerBeanClass);
}
