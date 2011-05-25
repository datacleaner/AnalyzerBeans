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
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableFilterJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;

public final class FilterJobBuilder<F extends Filter<C>, C extends Enum<C>> extends
		AbstractBeanWithInputColumnsBuilder<FilterBeanDescriptor<F, C>, F, FilterJobBuilder<F, C>> implements
		OutcomeSourceJob {

	// We keep a cached version of the resulting filter job because of
	// references coming from other objects, particular LazyFilterOutcome.
	private FilterJob _cachedJob;
	private EnumMap<C, FilterOutcome> _outcomes;

	public FilterJobBuilder(AnalysisJobBuilder analysisJobBuilder, FilterBeanDescriptor<F, C> descriptor) {
		super(analysisJobBuilder, descriptor, FilterJobBuilder.class);
		_outcomes = new EnumMap<C, FilterOutcome>(descriptor.getOutcomeCategoryEnum());
		EnumSet<C> categories = descriptor.getOutcomeCategories();
		for (C category : categories) {
			_outcomes.put(category, new LazyFilterOutcome(this, category));
		}
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
				getAnalysisJobBuilder().getFilterChangeListeners());
		for (FilterChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}

	@Override
	public void onRequirementChanged() {
		super.onRequirementChanged();
		List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(
				getAnalysisJobBuilder().getFilterChangeListeners());
		for (FilterChangeListener listener : listeners) {
			listener.onRequirementChanged(this);
		}
	}

	@Override
	public FilterOutcome[] getOutcomes() {
		Collection<FilterOutcome> outcomes = _outcomes.values();
		return outcomes.toArray(new FilterOutcome[outcomes.size()]);
	}

	public FilterOutcome getOutcome(C category) {
		FilterOutcome outcome = _outcomes.get(category);
		if (outcome == null) {
			throw new IllegalArgumentException(category + " is not a valid category for " + this);
		}
		return outcome;
	}

	public FilterOutcome getOutcome(Object category) {
		FilterOutcome outcome = _outcomes.get(category);
		if (outcome == null) {
			throw new IllegalArgumentException(category + " is not a valid category for " + this);
		}
		return outcome;
	}
}
