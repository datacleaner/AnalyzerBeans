package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.job.SimpleAnalyzerJob;

public class JobListDescriptorProvider implements DescriptorProvider {

	private Map<Class<?>, AnalyzerBeanDescriptor> _descriptors;

	public JobListDescriptorProvider(
			Collection<? extends SimpleAnalyzerJob> jobs) {
		_descriptors = new HashMap<Class<?>, AnalyzerBeanDescriptor>();
		for (SimpleAnalyzerJob job : jobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
					analyzerClass);
			_descriptors.put(analyzerClass, descriptor);
		}
	}

	@Override
	public AnalyzerBeanDescriptor getAnalyzerBeanDescriptorForClass(
			Class<?> analyzerBeanClass) {
		return _descriptors.get(analyzerBeanClass);
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getAnalyzerBeanDescriptors() {
		return _descriptors.values();
	}

	@Override
	public Collection<TransformerBeanDescriptor> getTransformerBeanDescriptors() {
		// transformers are not supported because they are not included in
		// simple analyzer jobs
		return Collections.emptySet();
	}

	@Override
	public TransformerBeanDescriptor getTransformerBeanDescriptorForClass(
			Class<?> transformerBeanClass) {
		// transformers are not supported because they are not included in
		// simple analyzer jobs
		return null;
	}

}
