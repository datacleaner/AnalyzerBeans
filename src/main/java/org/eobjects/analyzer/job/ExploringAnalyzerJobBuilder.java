package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

public class ExploringAnalyzerJobBuilder<A extends ExploringAnalyzer<?>>
		extends
		AbstractBeanJobBuilder<AnalyzerBeanDescriptor<A>, A, ExploringAnalyzerJobBuilder<A>>
		implements AnalyzerJobBuilder<A> {

	public ExploringAnalyzerJobBuilder(AnalyzerBeanDescriptor<A> descriptor) {
		super(descriptor, ExploringAnalyzerJobBuilder.class);
	}

	@Override
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
