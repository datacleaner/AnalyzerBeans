package org.eobjects.analyzer.job.builder;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ImmutableAnalyzerJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;

public final class ExploringAnalyzerJobBuilder<A extends ExploringAnalyzer<?>> extends
		AbstractBeanJobBuilder<AnalyzerBeanDescriptor<A>, A, ExploringAnalyzerJobBuilder<A>> implements
		AnalyzerJobBuilder<A> {

	private final AnalysisJobBuilder _analysisJobBuilder;

	public ExploringAnalyzerJobBuilder(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeanDescriptor<A> descriptor) {
		super(descriptor, ExploringAnalyzerJobBuilder.class);
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException("Exploring Analyzer job is not correctly configured");
		}

		return new ImmutableAnalyzerJob(getDescriptor(), new ImmutableBeanConfiguration(getConfiguredProperties()), null);
	}

	@Override
	public AnalyzerJob[] toAnalyzerJobs() throws IllegalStateException {
		return new AnalyzerJob[] { toAnalyzerJob() };
	}

	@Override
	public String toString() {
		return "ExploringAnalyzerJobBuilder[analyzer=" + getDescriptor().getDisplayName() + "]";
	}

	@Override
	public void onConfigurationChanged() {
		super.onConfigurationChanged();
		List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(
				_analysisJobBuilder.getAnalyzerChangeListeners());
		for (AnalyzerChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}
}
