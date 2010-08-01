package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.Transformer;

public class LazyDescriptorProvider implements DescriptorProvider {

	private Map<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor>();
	private Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor>();

	@Override
	public AnalyzerBeanDescriptor getAnalyzerBeanDescriptorForClass(
			Class<? extends Analyzer<?>> analyzerBeanClass) {
		AnalyzerBeanDescriptor descriptor = _analyzerBeanDescriptors
				.get(analyzerBeanClass);
		if (descriptor == null) {
			descriptor = new AnalyzerBeanDescriptor(analyzerBeanClass);
			_analyzerBeanDescriptors.put(analyzerBeanClass, descriptor);
		}
		return descriptor;
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getAnalyzerBeanDescriptors() {
		return Collections.unmodifiableCollection(_analyzerBeanDescriptors
				.values());
	}

	@Override
	public Collection<TransformerBeanDescriptor> getTransformerBeanDescriptors() {
		return Collections.unmodifiableCollection(_transformerBeanDescriptors
				.values());
	}

	@Override
	public TransformerBeanDescriptor getTransformerBeanDescriptorForClass(
			Class<? extends Transformer<?>> transformerBeanClass) {
		TransformerBeanDescriptor descriptor = _transformerBeanDescriptors
				.get(transformerBeanClass);
		if (descriptor == null) {
			descriptor = new TransformerBeanDescriptor(transformerBeanClass);
			_transformerBeanDescriptors.put(transformerBeanClass, descriptor);
		}
		return descriptor;
	}

}
