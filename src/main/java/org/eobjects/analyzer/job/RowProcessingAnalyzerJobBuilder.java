package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class RowProcessingAnalyzerJobBuilder<A extends RowProcessingAnalyzer<?>>
		extends
		AbstractBeanWithInputColumnsBuilder<AnalyzerBeanDescriptor<A>, A, RowProcessingAnalyzerJobBuilder<A>> {

	public RowProcessingAnalyzerJobBuilder(AnalyzerBeanDescriptor<A> descriptor) {
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
