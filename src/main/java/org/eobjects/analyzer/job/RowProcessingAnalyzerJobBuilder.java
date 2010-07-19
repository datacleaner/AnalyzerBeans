package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class RowProcessingAnalyzerJobBuilder extends
		AbstractBeanWithInputColumnsBuilder<AnalyzerBeanDescriptor, RowProcessingAnalyzerJobBuilder> {

	public RowProcessingAnalyzerJobBuilder(AnalyzerBeanDescriptor descriptor) {
		super(descriptor, RowProcessingAnalyzerJobBuilder.class);
	}

	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException(
					"Row processing Analyzer job is not correctly configured");
		}

		return new ImmutableAnalyzerJob(getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()));
	}

	@Override
	public String toString() {
		return "RowProcessingAnalyzerJobBuilder[analyzer="
				+ getDescriptor().getDisplayName() + ",inputColumns="
				+ getInputColumns() + "]";
	}
}
