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

    private final List<FilterChangeListener> _localChangeListeners;

    public FilterJobBuilder(AnalysisJobBuilder analysisJobBuilder, FilterBeanDescriptor<F, C> descriptor) {
        super(analysisJobBuilder, descriptor, FilterJobBuilder.class);
        _outcomes = new EnumMap<C, FilterOutcome>(descriptor.getOutcomeCategoryEnum());
        EnumSet<C> categories = descriptor.getOutcomeCategories();
        for (C category : categories) {
            _outcomes.put(category, new LazyFilterOutcome(this, category));
        }

        _localChangeListeners = new ArrayList<FilterChangeListener>(0);
    }

    public FilterJob toFilterJob() {
        return toFilterJob(true);
    }

    public FilterJob toFilterJob(boolean validate) {
        if (validate && !isConfigured(true)) {
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

    /**
     * Builds a temporary list of all listeners, both global and local
     * 
     * @return
     */
    private List<FilterChangeListener> getAllListeners() {
        List<FilterChangeListener> globalChangeListeners = getAnalysisJobBuilder().getFilterChangeListeners();
        List<FilterChangeListener> list = new ArrayList<FilterChangeListener>(globalChangeListeners.size()
                + _localChangeListeners.size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    @Override
    public String toString() {
        return "FilterJobBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns()
                + "]";
    }

    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();
        List<FilterChangeListener> listeners = getAllListeners();
        for (FilterChangeListener listener : listeners) {
            listener.onConfigurationChanged(this);
        }
    }

    @Override
    public void onRequirementChanged() {
        super.onRequirementChanged();
        List<FilterChangeListener> listeners = getAllListeners();
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

    /**
     * Notification method invoked when transformer is removed.
     */
    protected void onRemoved() {
        List<FilterChangeListener> listeners = getAllListeners();
        for (FilterChangeListener listener : listeners) {
            listener.onRemove(this);
        }
    }
    
    /**
     * Adds a change listener to this component
     * 
     * @param listener
     */
    public void addChangeListener(FilterChangeListener listener) {
        _localChangeListeners.add(listener);
    }

    /**
     * Removes a change listener from this component
     * 
     * @param listener
     * @return whether or not the listener was found and removed.
     */
    public boolean removeChangeListener(FilterChangeListener listener) {
        return _localChangeListeners.remove(listener);
    }
}
