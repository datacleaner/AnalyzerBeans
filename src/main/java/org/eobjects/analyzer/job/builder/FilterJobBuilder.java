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

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableFilterJob;

public final class FilterJobBuilder<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanWithInputColumnsBuilder<FilterBeanDescriptor<F, C>, F, FilterJobBuilder<F, C>> {

	private final AnalysisJobBuilder _analysisJobBuilder;

	// We keep a cached version of the resulting filter job because of
	// references coming from other objects, particular LazyFilterOutcome.
	private FilterJob _cachedJob;

	public FilterJobBuilder(AnalysisJobBuilder analysisJobBuilder, FilterBeanDescriptor<F, C> descriptor) {
		super(descriptor, FilterJobBuilder.class);
		_analysisJobBuilder = analysisJobBuilder;
	}

	public FilterJob toFilterJob() {
		if (!isConfigured()) {
			throw new IllegalStateException("Filter job is not correctly configured");
		}

		if (_cachedJob == null) {
			_cachedJob = new ImmutableFilterJob(getName(), getDescriptor(), new ImmutableBeanConfiguration(
					getConfiguredProperties()), getRequirement());
		} else {
			ImmutableFilterJob newFilterJob = new ImmutableFilterJob(getName(), getDescriptor(),
					new ImmutableBeanConfiguration(getConfiguredProperties()), getRequirement());
			if (!newFilterJob.equals(_cachedJob)) {
				_cachedJob = newFilterJob;
			}
		}
		return _cachedJob;
	}

	@Override
	public String toString() {
		return "FilterJobBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns() + "]";
	}

	@Override
	public void onConfigurationChanged() {
		super.onConfigurationChanged();
		List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(
				_analysisJobBuilder.getFilterChangeListeners());
		for (FilterChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}

	@Override
	public void onRequirementChanged() {
		super.onRequirementChanged();
		List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(
				_analysisJobBuilder.getFilterChangeListeners());
		for (FilterChangeListener listener : listeners) {
			listener.onRequirementChanged(this);
		}
	}
}
