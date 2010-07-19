package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class ExploringAnalyzerJobBuilder extends
		AbstractBeanJobBuilder<AnalyzerBeanDescriptor> {

	public ExploringAnalyzerJobBuilder(AnalyzerBeanDescriptor descriptor) {
		super(descriptor);
	}

	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException(
					"Exploring Analyzer job is not correctly configured");
		}

		return new ImmutableAnalyzerJob(getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()));
	}

	@Override
	public String toString() {
		return "ExploringAnalyzerJobBuilder[analyzer="
				+ getDescriptor().getDisplayName() + "]";
	}
}
