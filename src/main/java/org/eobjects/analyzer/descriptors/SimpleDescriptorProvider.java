package org.eobjects.analyzer.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple descriptor provider with a method signature suitable externalizing
 * class names of analyzer beans. For example, if you're using the Spring
 * Framework you initialize this descriptor provider as follows:
 * 
 * <pre>
 * &lt;bean id="descriptorProvider" class="org.eobjects.analyzer.descriptors.SimpleDescriptorProvider"&gt;
 *   &lt;property name="classNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.StringAnalyzer&lt;/value&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Kasper Sørensen
 */
public class SimpleDescriptorProvider implements DescriptorProvider {

	private List<AnalyzerBeanDescriptor> _descriptors = new ArrayList<AnalyzerBeanDescriptor>();

	public SimpleDescriptorProvider() {
	}

	public void addDescriptor(AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		_descriptors.add(analyzerBeanDescriptor);
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getDescriptors() {
		return _descriptors;
	}

	public void setDescriptors(List<AnalyzerBeanDescriptor> descriptors) {
		_descriptors = descriptors;
	}

	@Override
	public AnalyzerBeanDescriptor getDescriptorForClass(
			Class<?> analyzerBeanClass) {
		for (AnalyzerBeanDescriptor descriptor : _descriptors) {
			if (descriptor.getAnalyzerClass() == analyzerBeanClass) {
				return descriptor;
			}
		}
		return null;
	}

	public void setClassNames(Collection<String> classNames)
			throws ClassNotFoundException {
		for (String className : classNames) {
			Class<?> c = Class.forName(className);
			AnalyzerBeanDescriptor descriptor = getDescriptorForClass(c);
			if (descriptor == null) {
				addDescriptor(new AnalyzerBeanDescriptor(c));
			}
		}
	}
}
