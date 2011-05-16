/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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

	public ExploringAnalyzerJobBuilder(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeanDescriptor<A> descriptor) {
		super(analysisJobBuilder, descriptor, ExploringAnalyzerJobBuilder.class);
	}

	@Override
	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		if (!isConfigured()) {
			throw new IllegalStateException("Exploring Analyzer job is not correctly configured");
		}

		return new ImmutableAnalyzerJob(getName(), getDescriptor(),
				new ImmutableBeanConfiguration(getConfiguredProperties()), null);
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
		List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(getAnalysisJobBuilder()
				.getAnalyzerChangeListeners());
		for (AnalyzerChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}
}
