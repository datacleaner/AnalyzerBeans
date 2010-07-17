package org.eobjects.analyzer.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple descriptor provider with a method signature suitable externalizing
 * class names of analyzer and transformer beans. For example, if you're using
 * the Spring Framework you initialize this descriptor provider as follows:
 * 
 * <pre>
 * &lt;bean id="descriptorProvider" class="org.eobjects.analyzer.descriptors.SimpleDescriptorProvider"&gt;
 *   &lt;property name="analyzerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.StringAnalyzer&lt;/value&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="transformerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.eobjects.analyzer.beans.TokenizerTransformer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Kasper Sørensen
 */
public class SimpleDescriptorProvider implements DescriptorProvider {

	private List<AnalyzerBeanDescriptor> _analyzerBeanDescriptors = new ArrayList<AnalyzerBeanDescriptor>();
	private List<TransformerBeanDescriptor> _transformerBeanDescriptors = new ArrayList<TransformerBeanDescriptor>();

	public SimpleDescriptorProvider() {
	}

	public void addAnalyzerBeanDescriptor(
			AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		_analyzerBeanDescriptors.add(analyzerBeanDescriptor);
	}

	public void addTransformerBeanDescriptor(
			TransformerBeanDescriptor transformerBeanDescriptor) {
		_transformerBeanDescriptors.add(transformerBeanDescriptor);
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getAnalyzerBeanDescriptors() {
		return _analyzerBeanDescriptors;
	}

	public void setAnalyzerBeanDescriptors(
			List<AnalyzerBeanDescriptor> descriptors) {
		_analyzerBeanDescriptors = descriptors;
	}

	@Override
	public AnalyzerBeanDescriptor getAnalyzerBeanDescriptorForClass(
			Class<?> analyzerBeanClass) {
		for (AnalyzerBeanDescriptor descriptor : _analyzerBeanDescriptors) {
			if (descriptor.getBeanClass() == analyzerBeanClass) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public TransformerBeanDescriptor getTransformerBeanDescriptorForClass(
			Class<?> transformerBeanClass) {
		for (TransformerBeanDescriptor descriptor : _transformerBeanDescriptors) {
			if (descriptor.getBeanClass() == transformerBeanClass) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public Collection<TransformerBeanDescriptor> getTransformerBeanDescriptors() {
		return _transformerBeanDescriptors;
	}

	public void setAnalyzerClassNames(Collection<String> classNames)
			throws ClassNotFoundException {
		for (String className : classNames) {
			Class<?> c = Class.forName(className);
			AnalyzerBeanDescriptor descriptor = getAnalyzerBeanDescriptorForClass(c);
			if (descriptor == null) {
				addAnalyzerBeanDescriptor(new AnalyzerBeanDescriptor(c));
			}
		}
	}

	public void setTransformerClassNames(Collection<String> classNames)
			throws ClassNotFoundException {
		for (String className : classNames) {
			Class<?> c = Class.forName(className);
			TransformerBeanDescriptor descriptor = getTransformerBeanDescriptorForClass(c);
			if (descriptor == null) {
				addTransformerBeanDescriptor(new TransformerBeanDescriptor(c));
			}
		}
	}
}
