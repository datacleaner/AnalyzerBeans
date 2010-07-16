package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.job.SimpleAnalyzerJob;

public class JobListDescriptorProvider implements DescriptorProvider {

	private Map<Class<?>, AnalyzerBeanDescriptor> _descriptors;

	public JobListDescriptorProvider(Collection<? extends SimpleAnalyzerJob> jobs) {
		_descriptors = new HashMap<Class<?>, AnalyzerBeanDescriptor>();
		for (SimpleAnalyzerJob job : jobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
					analyzerClass);
			_descriptors.put(analyzerClass, descriptor);
		}
	}

	@Override
	public AnalyzerBeanDescriptor getDescriptorForClass(
			Class<?> analyzerBeanClass) {
		return _descriptors.get(analyzerBeanClass);
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getDescriptors() {
		return _descriptors.values();
	}

}
