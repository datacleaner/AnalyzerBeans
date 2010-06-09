package org.eobjects.analyzer.descriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.job.AnalysisJob;

public class JobListDescriptorProvider implements DescriptorProvider {

	private Map<Class<?>, AnalyzerBeanDescriptor> _descriptors;

	public JobListDescriptorProvider(List<AnalysisJob> jobs) {
		_descriptors = new HashMap<Class<?>, AnalyzerBeanDescriptor>();
		for (AnalysisJob job : jobs) {
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
