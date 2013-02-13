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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.schema.Column;

/**
 * Helper class to perform the somewhat intricate
 * {@link AnalysisJobBuilder#importJob(AnalysisJob)} operation.
 */
final class AnalysisJobBuilderImportHelper {

    private final AnalysisJobBuilder _builder;

    public AnalysisJobBuilderImportHelper(AnalysisJobBuilder builder) {
        _builder = builder;
    }

    public void importJob(AnalysisJob job) {
        _builder.setDatastore(job.getDatastore());

        final Collection<InputColumn<?>> sourceColumns = job.getSourceColumns();
        for (InputColumn<?> inputColumn : sourceColumns) {
            _builder.addSourceColumn((MetaModelInputColumn) inputColumn);
        }

        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        // map that translates original component jobs to their builder objects
        final Map<ComponentJob, Object> componentBuilders = new IdentityHashMap<ComponentJob, Object>();
        addComponentBuilders(job.getFilterJobs(), componentBuilders);
        addComponentBuilders(job.getMergedOutcomeJobs(), componentBuilders);
        addComponentBuilders(job.getExplorerJobs(), componentBuilders);
        addComponentBuilders(job.getTransformerJobs(), componentBuilders);
        addComponentBuilders(job.getAnalyzerJobs(), componentBuilders);

        // re-build filter requirements
        for (Entry<ComponentJob, Object> entry : componentBuilders.entrySet()) {
            ComponentJob componentJob = entry.getKey();
            if (componentJob instanceof ConfigurableBeanJob<?>) {
                Outcome[] requirements = ((ConfigurableBeanJob<?>) componentJob).getRequirements();
                if (requirements != null && requirements.length > 0) {
                    assert requirements.length == 1;

                    final AbstractBeanWithInputColumnsBuilder<?, ?, ?> builder = (AbstractBeanWithInputColumnsBuilder<?, ?, ?>) entry
                            .getValue();

                    final Outcome originalRequirement = requirements[0];
                    final Outcome requirement = findImportedRequirement(originalRequirement, componentBuilders);
                    builder.setRequirement(requirement);
                }
            } else if (componentJob instanceof MergedOutcomeJob) {
                final MergedOutcomeJobBuilder builder = (MergedOutcomeJobBuilder) entry.getValue();
                final MergedOutcomeJob mergedOutcomeJob = (MergedOutcomeJob) componentJob;
                final MergeInput[] mergeInputs = mergedOutcomeJob.getMergeInputs();
                for (MergeInput mergeInput : mergeInputs) {
                    final Outcome requirement = findImportedRequirement(mergeInput.getOutcome(), componentBuilders);
                    final MergeInputBuilder mergedOutcomeBuilder = builder.addMergedOutcome(requirement);

                    // we need to also build input columns here. There's a risk
                    // that these input columns are not available (imported)
                    // yet.
                    final InputColumn<?>[] inputColumns = mergeInput.getInputColumns();
                    for (InputColumn<?> originalInputColumn : inputColumns) {
                        final InputColumn<?> inputColumn = findImportedInputColumn(originalInputColumn,
                                componentBuilders, sourceColumnFinder);
                        mergedOutcomeBuilder.addInputColumn(inputColumn);
                    }
                }
            }
        }

        // re-build input column dependencies
        for (Entry<ComponentJob, Object> entry : componentBuilders.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            if (componentJob instanceof ConfigurableBeanJob) {
                final ConfigurableBeanJob<?> configurableBeanJob = (ConfigurableBeanJob<?>) componentJob;
                final Set<ConfiguredPropertyDescriptor> inputColumnProperties = configurableBeanJob.getDescriptor()
                        .getConfiguredPropertiesForInput(true);

                final AbstractBeanWithInputColumnsBuilder<?, ?, ?> builder = (AbstractBeanWithInputColumnsBuilder<?, ?, ?>) entry
                        .getValue();

                for (ConfiguredPropertyDescriptor inputColumnProperty : inputColumnProperties) {
                    final Object originalInputColumnValue = configurableBeanJob.getConfiguration().getProperty(
                            inputColumnProperty);
                    final Object newInputColumnValue = findImportedInputColumns(originalInputColumnValue,
                            componentBuilders, sourceColumnFinder);
                    builder.setConfiguredProperty(inputColumnProperty, newInputColumnValue);
                }
            }
        }
    }

    private Object findImportedInputColumns(Object originalInputColumnValue,
            Map<ComponentJob, Object> componentBuilders, SourceColumnFinder sourceColumnFinder) {
        if (originalInputColumnValue == null) {
            return null;
        }

        if (originalInputColumnValue instanceof InputColumn) {
            return findImportedInputColumn((InputColumn<?>) originalInputColumnValue, componentBuilders,
                    sourceColumnFinder);
        }

        if (originalInputColumnValue.getClass().isArray()) {
            int length = Array.getLength(originalInputColumnValue);
            InputColumn<?>[] value = new InputColumn[length];
            for (int i = 0; i < value.length; i++) {
                InputColumn<?> element = (InputColumn<?>) Array.get(originalInputColumnValue, i);
                value[i] = findImportedInputColumn(element, componentBuilders, sourceColumnFinder);
            }
            return value;
        }

        throw new UnsupportedOperationException("Unknown input column value type: " + originalInputColumnValue);
    }

    private InputColumn<?> findImportedInputColumn(InputColumn<?> originalInputColumn,
            Map<ComponentJob, Object> componentBuilders, SourceColumnFinder sourceColumnFinder) {
        if (originalInputColumn.isPhysicalColumn()) {
            Column physicalColumn = originalInputColumn.getPhysicalColumn();
            return _builder.getSourceColumnByName(physicalColumn.getQualifiedLabel());
        }

        final InputColumnSourceJob originalSourceJob = sourceColumnFinder.findInputColumnSource(originalInputColumn);
        final InputColumnSourceJob newSourceJob = (InputColumnSourceJob) componentBuilders.get(originalSourceJob);

        if (newSourceJob == null) {
            throw new IllegalStateException("Could not find builder corresponding to " + originalSourceJob
                    + " in builder map: " + componentBuilders);
        }

        final String originalColumnName = originalInputColumn.getName();
        final InputColumn<?>[] candidates = newSourceJob.getOutput();
        for (InputColumn<?> candidate : candidates) {
            if (candidate.getName().equals(originalColumnName)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not determine a replacement input column for '" + originalColumnName
                + "' in output column candidate set: " + Arrays.toString(candidates));
    }

    private Outcome findImportedRequirement(Outcome originalRequirement, Map<ComponentJob, Object> componentBuilders) {
        final OutcomeSourceJob sourceJob = originalRequirement.getSourceJob();
        final Object builder = componentBuilders.get(sourceJob);
        if (builder == null) {
            throw new IllegalStateException("Could not find builder corresponding to " + sourceJob
                    + " in builder map: " + componentBuilders);
        }

        if (builder instanceof MergedOutcomeJobBuilder) {
            return ((MergedOutcomeJobBuilder) builder).getOutcomes()[0];
        } else if (builder instanceof FilterJobBuilder<?, ?>) {
            final FilterOutcome filterOutcome = (FilterOutcome) originalRequirement;
            final Enum<?> category = filterOutcome.getCategory();
            return ((FilterJobBuilder<?, ?>) builder).getOutcome(category);
        } else {
            throw new UnsupportedOperationException("Unsupported outcome builder type: " + builder);
        }
    }

    private void addComponentBuilders(Collection<? extends ComponentJob> componentJobs,
            Map<ComponentJob, Object> componentBuilders) {
        for (ComponentJob componentJob : componentJobs) {
            Object builder = _builder.addComponent(componentJob);
            componentBuilders.put(componentJob, builder);
        }
    }
}
