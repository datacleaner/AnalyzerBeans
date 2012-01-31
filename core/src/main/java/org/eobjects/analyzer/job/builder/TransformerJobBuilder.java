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
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableTransformerJob;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.analyzer.util.StringUtils;

/**
 * @author Kasper Sørensen
 * 
 * @param <T>
 *            the transformer type being configured
 */
public final class TransformerJobBuilder<T extends Transformer<?>> extends
		AbstractBeanWithInputColumnsBuilder<TransformerBeanDescriptor<T>, T, TransformerJobBuilder<T>> implements
		InputColumnSourceJob, InputColumnSinkJob, OutcomeSinkJob {

	private final List<MutableInputColumn<?>> _outputColumns = new ArrayList<MutableInputColumn<?>>();
	private final List<String> _automaticOutputColumnNames = new ArrayList<String>();
	private final IdGenerator _idGenerator;
	private final List<TransformerChangeListener> _transformerChangeListeners;

	public TransformerJobBuilder(AnalysisJobBuilder analysisJobBuilder, TransformerBeanDescriptor<T> descriptor,
			IdGenerator idGenerator, List<TransformerChangeListener> transformerChangeListeners) {
		super(analysisJobBuilder, descriptor, TransformerJobBuilder.class);
		_idGenerator = idGenerator;
		_transformerChangeListeners = transformerChangeListeners;
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		if (!isConfigured()) {
			// as long as the transformer is not configured, just return an
			// empty list
			return Collections.emptyList();
		}

		final Transformer<?> component = getConfigurableBean();
		final TransformerBeanDescriptor<T> descriptor = getDescriptor();

		final InjectionManager injectionManager = new InjectionManagerImpl(null, null, new InMemoryStorageProvider());
		final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, null);

		// mimic the configuration of a real transformer bean instance
		final BeanConfiguration beanConfiguration = new ImmutableBeanConfiguration(getConfiguredProperties());
		lifeCycleHelper.assignConfiguredProperties(descriptor, component, beanConfiguration);
		lifeCycleHelper.assignProvidedProperties(descriptor, component);
		
		// only validate, don't initialize
		lifeCycleHelper.validate(descriptor, component);

		final OutputColumns outputColumns = component.getOutputColumns();
		if (outputColumns == null) {
			throw new IllegalStateException("getOutputColumns() returned null on transformer: " + component);
		}
		boolean changed = false;

		// adjust the amount of output columns
		int expectedCols = outputColumns.getColumnCount();
		int existingCols = _outputColumns.size();
		if (expectedCols != existingCols) {
			changed = true;
			int colDiff = expectedCols - existingCols;
			if (colDiff > 0) {
				for (int i = 0; i < colDiff; i++) {
					int nextIndex = _outputColumns.size();
					String name = outputColumns.getColumnName(nextIndex);
					if (name == null) {
						name = descriptor.getDisplayName() + " (" + (nextIndex + 1) + ")";
					}
					_outputColumns.add(new TransformedInputColumn<Object>(name, _idGenerator));
					_automaticOutputColumnNames.add(name);
				}
			} else if (colDiff < 0) {
				for (int i = 0; i < Math.abs(colDiff); i++) {
					// remove from the tail
					_outputColumns.remove(_outputColumns.size() - 1);
					_automaticOutputColumnNames.remove(_automaticOutputColumnNames.size() - 1);
				}
			}
		}

		// automatically update names and types of columns if they have not been
		// manually
		// set
		for (int i = 0; i < expectedCols; i++) {
			final String proposedName = outputColumns.getColumnName(i);
			Class<?> dataType = outputColumns.getColumnType(i);
			if (dataType == null) {
				dataType = descriptor.getOutputDataType();
			}

			TransformedInputColumn<?> col = (TransformedInputColumn<?>) _outputColumns.get(i);
			col.setInitialName(proposedName);
			if (dataType != col.getDataType()) {
				col.setDataType(dataType);
				changed = true;
			}

			String automaticName = _automaticOutputColumnNames.get(i);
			String columnName = col.getName();
			if (StringUtils.isNullOrEmpty(columnName) || automaticName.equals(columnName)) {
				if (proposedName != null) {
					col.setName(proposedName);
					_automaticOutputColumnNames.set(i, proposedName);
				}
			}
		}

		if (changed) {
			// notify listeners
			onOutputChanged();
		}

		return Collections.unmodifiableList(_outputColumns);
	}

	public void onOutputChanged() {

		// notify listeners
		for (TransformerChangeListener listener : _transformerChangeListeners) {
			listener.onOutputChanged(this, _outputColumns);
		}
	}

	public TransformerJob toTransformerJob() throws IllegalStateException {
		return toTransformerJob(true);
	}

	public TransformerJob toTransformerJob(boolean validate) {
		if (!isConfigured()) {
			throw new IllegalStateException("Transformer job is not correctly configured");
		}

		return new ImmutableTransformerJob(getName(), getDescriptor(), new ImmutableBeanConfiguration(
				getConfiguredProperties()), getOutputColumns(), getRequirement());
	}

	@Override
	public String toString() {
		return "TransformerJobBuilder[transformer=" + getDescriptor().getDisplayName() + ",inputColumns="
				+ getInputColumns() + "]";
	}

	public MutableInputColumn<?> getOutputColumnByName(String name) {
		if (name != null) {
			List<MutableInputColumn<?>> outputColumns = getOutputColumns();
			for (MutableInputColumn<?> inputColumn : outputColumns) {
				if (name.equals(inputColumn.getName())) {
					return inputColumn;
				}
			}
		}
		return null;
	}

	@Override
	public void onConfigurationChanged() {
		super.onConfigurationChanged();

		// trigger getOutputColumns which will notify consumers in the case of
		// output changes
		if (isConfigured()) {
			getOutputColumns();
		}

		List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(getAnalysisJobBuilder()
				.getTransformerChangeListeners());
		for (TransformerChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}

	@Override
	public void onRequirementChanged() {
		super.onRequirementChanged();
		List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(getAnalysisJobBuilder()
				.getTransformerChangeListeners());
		for (TransformerChangeListener listener : listeners) {
			listener.onRequirementChanged(this);
		}
	}

	@Override
	public InputColumn<?>[] getInput() {
		return getInputColumns().toArray(new InputColumn<?>[0]);
	}

	@Override
	public MutableInputColumn<?>[] getOutput() {
		return getOutputColumns().toArray(new MutableInputColumn<?>[0]);
	}
}
