package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public final class RowProcessingAnalyzerJobBuilder<A extends RowProcessingAnalyzer<?>>
		extends
		AbstractBeanWithInputColumnsBuilder<AnalyzerBeanDescriptor<A>, A, RowProcessingAnalyzerJobBuilder<A>>
		implements AnalyzerJobBuilder<A> {

	public RowProcessingAnalyzerJobBuilder(AnalyzerBeanDescriptor<A> descriptor) {
		super(descriptor, RowProcessingAnalyzerJobBuilder.class);
	}

	@Override
	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException(
					"Row processing Analyzer job is not correctly configured");
		}

		return new ImmutableAnalyzerJob(getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()),
				getRequirement());
	}

	@Override
	public String toString() {
		return "RowProcessingAnalyzerJobBuilder[analyzer="
				+ getDescriptor().getDisplayName() + ",inputColumns="
				+ getInputColumns() + "]";
	}
}
