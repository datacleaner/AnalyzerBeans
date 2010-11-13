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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ImmutableAnalyzerJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.util.ReflectionUtils;

import dk.eobjects.metamodel.schema.Table;

public final class RowProcessingAnalyzerJobBuilder<A extends RowProcessingAnalyzer<?>> extends
		AbstractBeanWithInputColumnsBuilder<AnalyzerBeanDescriptor<A>, A, RowProcessingAnalyzerJobBuilder<A>> implements
		AnalyzerJobBuilder<A> {

	private final AnalysisJobBuilder _analysisJobBuilder;

	/**
	 * Field that determines if this analyzer is applicable for building
	 * multiple jobs where the input columns have been partitioned based on
	 * input size (single or multiple) and originating table
	 */
	private final boolean _multipleJobsSupported;
	private final List<InputColumn<?>> _inputColumns;
	private final ConfiguredPropertyDescriptor _inputProperty;

	public RowProcessingAnalyzerJobBuilder(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeanDescriptor<A> descriptor) {
		super(descriptor, RowProcessingAnalyzerJobBuilder.class);
		_analysisJobBuilder = analysisJobBuilder;

		Set<ConfiguredPropertyDescriptor> inputProperties = descriptor.getConfiguredPropertiesForInput();
		if (inputProperties.size() == 1) {
			_multipleJobsSupported = true;
			_inputColumns = new ArrayList<InputColumn<?>>();
			_inputProperty = inputProperties.iterator().next();
		} else {
			_multipleJobsSupported = false;
			_inputColumns = null;
			_inputProperty = null;
		}
	}

	@Override
	public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
		AnalyzerJob[] analyzerJobs = toAnalyzerJobs();

		if (analyzerJobs == null || analyzerJobs.length == 0) {
			return null;
		}

		if (analyzerJobs.length > 1) {
			throw new IllegalStateException("This builder generates " + analyzerJobs.length
					+ " jobs, but a single job was requested");
		}

		return analyzerJobs[0];
	}

	@Override
	public AnalyzerJob[] toAnalyzerJobs() throws IllegalStateException {
		Map<ConfiguredPropertyDescriptor, Object> configuredProperties = getConfiguredProperties();
		if (!_multipleJobsSupported) {
			ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getDescriptor(), new ImmutableBeanConfiguration(
					configuredProperties), getRequirement());
			return new AnalyzerJob[] { job };
		}

		if (_inputColumns.isEmpty()) {
			throw new IllegalStateException("No input column configured");
		}

		Map<Table, List<InputColumn<?>>> originatingTables = new LinkedHashMap<Table, List<InputColumn<?>>>();
		for (InputColumn<?> inputColumn : _inputColumns) {
			Table table = _analysisJobBuilder.getOriginatingTable(inputColumn);
			List<InputColumn<?>> list = originatingTables.get(table);
			if (list == null) {
				list = new LinkedList<InputColumn<?>>();
			}
			list.add(inputColumn);
			originatingTables.put(table, list);
		}

		List<AnalyzerJob> jobs = new ArrayList<AnalyzerJob>();
		Set<Table> tables = originatingTables.keySet();

		for (Table table : tables) {
			List<InputColumn<?>> columns = originatingTables.get(table);
			if (_inputProperty.isArray()) {
				jobs.add(createPartitionedJob(columns.toArray(new InputColumn[columns.size()]), configuredProperties));
			} else {
				for (InputColumn<?> column : columns) {
					jobs.add(createPartitionedJob(column, configuredProperties));
				}
			}
		}

		if (!isConfigured()) {
			throw new IllegalStateException("Row processing Analyzer job is not correctly configured");
		}

		return jobs.toArray(new AnalyzerJob[jobs.size()]);
	}

	@Override
	public RowProcessingAnalyzerJobBuilder<A> addInputColumn(InputColumn<?> inputColumn,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_multipleJobsSupported) {
			_inputColumns.add(inputColumn);
			return this;
		} else {
			return super.addInputColumn(inputColumn, propertyDescriptor);
		}
	}

	@Override
	public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException) {
		if (_multipleJobsSupported && configuredProperty == _inputProperty) {
			if (_inputColumns.isEmpty()) {
				if (throwException) {
					throw new IllegalStateException("No input columns have been added!");
				} else {
					return false;
				}
			}
			return true;
		}
		return super.isConfigured(configuredProperty, throwException);
	}

	private AnalyzerJob createPartitionedJob(Object columnValue,
			Map<ConfiguredPropertyDescriptor, Object> configuredProperties) {
		Map<ConfiguredPropertyDescriptor, Object> jobProperties = new HashMap<ConfiguredPropertyDescriptor, Object>(
				configuredProperties);
		jobProperties.put(_inputProperty, columnValue);
		ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getDescriptor(), new ImmutableBeanConfiguration(jobProperties),
				getRequirement());
		return job;
	}

	@Override
	public String toString() {
		return "RowProcessingAnalyzerJobBuilder[analyzer=" + getDescriptor().getDisplayName() + ",inputColumns="
				+ getInputColumns() + "]";
	}

	@Override
	public RowProcessingAnalyzerJobBuilder<A> setConfiguredProperty(ConfiguredPropertyDescriptor configuredProperty,
			Object value) {
		if (_multipleJobsSupported && configuredProperty.isInputColumn()) {
			_inputColumns.clear();
			if (ReflectionUtils.isArray(value)) {
				int length = Array.getLength(value);
				for (int i = 0; i < length; i++) {
					InputColumn<?> inputColumn = (InputColumn<?>) Array.get(value, i);
					_inputColumns.add(inputColumn);
				}
			} else {
				_inputColumns.add((InputColumn<?>) value);
			}
			return this;
		} else {
			return super.setConfiguredProperty(configuredProperty, value);
		}
	}

	@Override
	public Object getConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_multipleJobsSupported && propertyDescriptor.isInputColumn()) {
			return _inputColumns.toArray(new InputColumn[_inputColumns.size()]);
		} else {
			return super.getConfiguredProperty(propertyDescriptor);
		}
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

	@Override
	public void onRequirementChanged() {
		super.onRequirementChanged();
		List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(
				_analysisJobBuilder.getAnalyzerChangeListeners());
		for (AnalyzerChangeListener listener : listeners) {
			listener.onRequirementChanged(this);
		}
	}

	public boolean isMultipleJobsSupported() {
		return _multipleJobsSupported;
	}
}
