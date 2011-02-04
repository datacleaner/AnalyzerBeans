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
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableTransformerJob;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.lifecycle.AssignConfiguredCallback;
import org.eobjects.analyzer.lifecycle.AssignProvidedCallback;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleState;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
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

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final List<MutableInputColumn<?>> _outputColumns = new ArrayList<MutableInputColumn<?>>();
	private final List<String> _automaticOutputColumnNames = new ArrayList<String>();
	private final IdGenerator _idGenerator;
	private final List<TransformerChangeListener> _transformerChangeListeners;

	public TransformerJobBuilder(AnalysisJobBuilder analysisJobBuilder, TransformerBeanDescriptor<T> descriptor,
			IdGenerator idGenerator, List<TransformerChangeListener> transformerChangeListeners) {
		super(descriptor, TransformerJobBuilder.class);
		_analysisJobBuilder = analysisJobBuilder;
		_idGenerator = idGenerator;
		_transformerChangeListeners = transformerChangeListeners;
	}

	public List<MutableInputColumn<?>> getOutputColumns() {
		if (!isConfigured()) {
			// as long as the transformer is not configured, just return an
			// empty list
			return Collections.emptyList();
		}

		final Transformer<?> bean = getConfigurableBean();

		// mimic the configuration of a real transformer bean instance
		final AssignConfiguredCallback assignConfiguredCallback = new AssignConfiguredCallback(
				new ImmutableBeanConfiguration(getConfiguredProperties()), null);
		assignConfiguredCallback.onEvent(LifeCycleState.ASSIGN_CONFIGURED, bean, getDescriptor());

		final AssignProvidedCallback assignProvidedCallback = new AssignProvidedCallback(new InMemoryStorageProvider(),
				new InMemoryRowAnnotationFactory(), null);
		assignProvidedCallback.onEvent(LifeCycleState.ASSIGN_PROVIDED, bean, getDescriptor());

		final InitializeCallback initializeCallback = new InitializeCallback();
		initializeCallback.onEvent(LifeCycleState.INITIALIZE, bean, getDescriptor());

		final OutputColumns outputColumns = bean.getOutputColumns();
		if (outputColumns == null) {
			throw new IllegalStateException("getOutputColumns() returned null on transformer: " + bean);
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
						name = getDescriptor().getDisplayName() + " (" + (nextIndex + 1) + ")";
					}
					DataTypeFamily type = getDescriptor().getOutputDataTypeFamily();
					_outputColumns.add(new TransformedInputColumn<Object>(name, type, _idGenerator));
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

		// automatically update names of columns if they have not been manually
		// set
		for (int i = 0; i < expectedCols; i++) {
			MutableInputColumn<?> col = _outputColumns.get(i);
			String automaticName = _automaticOutputColumnNames.get(i);
			String columnName = col.getName();
			if (StringUtils.isNullOrEmpty(columnName) || automaticName.equals(columnName)) {
				String proposedName = outputColumns.getColumnName(i);
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

		List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(
				_analysisJobBuilder.getTransformerChangeListeners());
		for (TransformerChangeListener listener : listeners) {
			listener.onConfigurationChanged(this);
		}
	}

	@Override
	public void onRequirementChanged() {
		super.onRequirementChanged();
		List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(
				_analysisJobBuilder.getTransformerChangeListeners());
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
