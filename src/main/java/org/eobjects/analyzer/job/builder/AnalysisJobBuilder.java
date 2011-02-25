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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableAnalysisJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.PrefixedIdGenerator;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry to the Job Builder API. Use this class to build jobs either
 * programmatically, while parsing a marshalled job-representation (such as an
 * XML job definition) or for making an end-user able to build a job in a UI.
 * 
 * The AnalysisJobBuilder supports a wide variety of listeners to make it
 * possible to be informed of changes to the state and dependencies between the
 * components/beans that defines the job.
 * 
 * @author Kasper Sørensen
 */
public final class AnalysisJobBuilder implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilder.class);

	private final AnalyzerBeansConfiguration _configuration;
	private final IdGenerator _transformedColumnIdGenerator = new PrefixedIdGenerator("trans");

	// the configurable components
	private DataContextProvider _dataContextProvider;
	private final List<MetaModelInputColumn> _sourceColumns = new ArrayList<MetaModelInputColumn>();
	private final List<FilterJobBuilder<?, ?>> _filterJobBuilders = new ArrayList<FilterJobBuilder<?, ?>>();
	private final List<TransformerJobBuilder<?>> _transformerJobBuilders = new ArrayList<TransformerJobBuilder<?>>();
	private final List<AnalyzerJobBuilder<?>> _analyzerJobBuilders = new ArrayList<AnalyzerJobBuilder<?>>();
	private final List<MergedOutcomeJobBuilder> _mergedOutcomeJobBuilders = new ArrayList<MergedOutcomeJobBuilder>();

	// listeners, typically for UI that uses the builders
	private final List<SourceColumnChangeListener> _sourceColumnListeners = new ArrayList<SourceColumnChangeListener>();
	private final List<AnalyzerChangeListener> _analyzerChangeListeners = new ArrayList<AnalyzerChangeListener>();
	private final List<TransformerChangeListener> _transformerChangeListeners = new ArrayList<TransformerChangeListener>();
	private final List<FilterChangeListener> _filterChangeListeners = new ArrayList<FilterChangeListener>();
	private final List<MergedOutcomeChangeListener> _mergedOutcomeChangeListener = new ArrayList<MergedOutcomeChangeListener>();
	private Outcome _defaultRequirement;

	public AnalysisJobBuilder(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
	}

	public AnalysisJobBuilder setDatastore(String datastoreName) {
		Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);
		if (datastore == null) {
			throw new IllegalArgumentException("No such datastore: " + datastoreName);
		}
		return setDatastore(datastore);
	}

	public AnalysisJobBuilder setDatastore(Datastore datastore) {
		final DataContextProvider dataContextProvider;
		if (datastore == null) {
			dataContextProvider = null;
		} else {
			dataContextProvider = datastore.getDataContextProvider();
		}
		return setDataContextProvider(dataContextProvider);
	}

	public AnalysisJobBuilder setDataContextProvider(DataContextProvider dataContextProvider) {
		if (_dataContextProvider != null) {
			_dataContextProvider.close();
		}
		_dataContextProvider = dataContextProvider;
		return this;
	}

	public DataContextProvider getDataContextProvider() {
		return _dataContextProvider;
	}

	public AnalysisJobBuilder addSourceColumn(Column column) {
		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		return addSourceColumn(inputColumn);
	}

	public AnalysisJobBuilder addSourceColumn(MetaModelInputColumn inputColumn) {
		if (!_sourceColumns.contains(inputColumn)) {
			_sourceColumns.add(inputColumn);

			List<SourceColumnChangeListener> listeners = new ArrayList<SourceColumnChangeListener>(_sourceColumnListeners);
			for (SourceColumnChangeListener listener : listeners) {
				listener.onAdd(inputColumn);
			}
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(Column... columns) {
		for (Column column : columns) {
			addSourceColumn(column);
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(MetaModelInputColumn... inputColumns) {
		for (MetaModelInputColumn metaModelInputColumn : inputColumns) {
			addSourceColumn(metaModelInputColumn);
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(String... columnNames) {
		if (_dataContextProvider == null) {
			throw new IllegalStateException(
					"Cannot add source columns by name when no Datastore or DataContextProvider has been set");
		}
		SchemaNavigator schemaNavigator = _dataContextProvider.getSchemaNavigator();
		Column[] columns = new Column[columnNames.length];
		for (int i = 0; i < columns.length; i++) {
			String columnName = columnNames[i];
			Column column = schemaNavigator.convertToColumn(columnName);
			if (column == null) {
				throw new IllegalArgumentException("No such column: " + columnName);
			}
			columns[i] = column;
		}
		return addSourceColumns(columns);
	}

	public AnalysisJobBuilder removeSourceColumn(Column column) {
		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		return removeSourceColumn(inputColumn);
	}

	public AnalysisJobBuilder removeSourceColumn(MetaModelInputColumn inputColumn) {
		boolean removed = _sourceColumns.remove(inputColumn);
		if (removed) {
			List<SourceColumnChangeListener> listeners = new ArrayList<SourceColumnChangeListener>(_sourceColumnListeners);
			for (SourceColumnChangeListener listener : listeners) {
				listener.onRemove(inputColumn);
			}
		}
		return this;
	}

	public boolean containsSourceColumn(Column column) {
		for (MetaModelInputColumn sourceColumn : _sourceColumns) {
			if (sourceColumn.getPhysicalColumn().equals(column)) {
				return true;
			}
		}
		return false;
	}

	public List<MetaModelInputColumn> getSourceColumns() {
		return Collections.unmodifiableList(_sourceColumns);
	}

	public MergedOutcomeJobBuilder addMergedOutcomeJobBuilder() {
		MergedOutcomeJobBuilder mojb = new MergedOutcomeJobBuilder(_transformedColumnIdGenerator);
		_mergedOutcomeJobBuilders.add(mojb);

		List<MergedOutcomeChangeListener> listeners = new ArrayList<MergedOutcomeChangeListener>(
				_mergedOutcomeChangeListener);
		for (MergedOutcomeChangeListener listener : listeners) {
			listener.onAdd(mojb);
		}
		return mojb;
	}

	public AnalysisJobBuilder removeMergedOutcomeJobBuilder(MergedOutcomeJobBuilder mojb) {
		boolean removed = _mergedOutcomeJobBuilders.remove(mojb);
		if (removed) {
			List<MergedOutcomeChangeListener> listeners = new ArrayList<MergedOutcomeChangeListener>(
					_mergedOutcomeChangeListener);
			for (MergedOutcomeChangeListener listener : listeners) {
				listener.onRemove(mojb);
			}
		}
		return this;
	}

	public List<MergedOutcomeJobBuilder> getMergedOutcomeJobBuilders() {
		return Collections.unmodifiableList(_mergedOutcomeJobBuilders);
	}

	public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(Class<T> transformerClass) {
		TransformerBeanDescriptor<T> descriptor = _configuration.getDescriptorProvider()
				.getTransformerBeanDescriptorForClass(transformerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: " + transformerClass);
		}
		return addTransformer(descriptor);
	}

	public List<TransformerJobBuilder<?>> getTransformerJobBuilders() {
		return Collections.unmodifiableList(_transformerJobBuilders);
	}

	public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(TransformerBeanDescriptor<T> descriptor) {
		TransformerJobBuilder<T> tjb = new TransformerJobBuilder<T>(this, descriptor, _transformedColumnIdGenerator,
				_transformerChangeListeners);
		if (tjb.getRequirement() == null) {
			tjb.setRequirement(_defaultRequirement);
		}
		_transformerJobBuilders.add(tjb);

		// make a copy since some of the listeners may add additional listeners
		// which will otherwise cause ConcurrentModificationExceptions
		List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(_transformerChangeListeners);
		for (TransformerChangeListener listener : listeners) {
			listener.onAdd(tjb);
		}
		return tjb;
	}

	public AnalysisJobBuilder removeTransformer(TransformerJobBuilder<?> tjb) {
		boolean removed = _transformerJobBuilders.remove(tjb);
		if (removed) {
			// make a copy since some of the listeners may add additional
			// listeners
			// which will otherwise cause ConcurrentModificationExceptions
			List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(_transformerChangeListeners);
			for (TransformerChangeListener listener : listeners) {
				listener.onOutputChanged(tjb, new LinkedList<MutableInputColumn<?>>());
				listener.onRemove(tjb);
			}
		}
		return this;
	}

	public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(Class<F> filterClass) {
		FilterBeanDescriptor<F, C> descriptor = _configuration.getDescriptorProvider().getFilterBeanDescriptorForClass(
				filterClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: " + filterClass);
		}
		return addFilter(descriptor);
	}

	public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(FilterBeanDescriptor<F, C> descriptor) {
		FilterJobBuilder<F, C> fjb = new FilterJobBuilder<F, C>(this, descriptor);
		return addFilter(fjb);
	}

	public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(FilterJobBuilder<F, C> fjb) {
		_filterJobBuilders.add(fjb);

		if (fjb.getRequirement() == null) {
			fjb.setRequirement(_defaultRequirement);
		}

		List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(_filterChangeListeners);
		for (FilterChangeListener listener : listeners) {
			listener.onAdd(fjb);
		}
		return fjb;
	}

	public AnalysisJobBuilder removeFilter(FilterJobBuilder<?, ?> filterJobBuilder) {
		boolean removed = _filterJobBuilders.remove(filterJobBuilder);

		if (removed) {
			final Outcome previousRequirement = filterJobBuilder.getRequirement();

			// clean up components who depend on this filter
			Outcome[] outcomes = filterJobBuilder.getOutcomes();
			for (final Outcome outcome : outcomes) {
				if (outcome.equals(_defaultRequirement)) {
					setDefaultRequirement(null);
				}

				for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
					if (ajb instanceof RowProcessingAnalyzerJobBuilder) {
						RowProcessingAnalyzerJobBuilder<?> rowProcessingAnalyzerJobBuilder = (RowProcessingAnalyzerJobBuilder<?>) ajb;
						Outcome requirement = rowProcessingAnalyzerJobBuilder.getRequirement();
						if (outcome.equals(requirement)) {
							rowProcessingAnalyzerJobBuilder.setRequirement(previousRequirement);
						}
					}
				}

				for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
					Outcome requirement = tjb.getRequirement();
					if (outcome.equals(requirement)) {
						tjb.setRequirement(previousRequirement);
					}
				}

				for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
					Outcome requirement = fjb.getRequirement();
					if (outcome.equals(requirement)) {
						fjb.setRequirement(previousRequirement);
					}
				}
			}

			List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(_filterChangeListeners);
			for (FilterChangeListener listener : listeners) {
				listener.onRemove(filterJobBuilder);
			}
		}
		return this;
	}

	public List<AnalyzerJobBuilder<?>> getAnalyzerJobBuilders() {
		return Collections.unmodifiableList(_analyzerJobBuilders);
	}

	public List<FilterJobBuilder<?, ?>> getFilterJobBuilders() {
		return Collections.unmodifiableList(_filterJobBuilders);
	}

	public <A extends ExploringAnalyzer<?>> ExploringAnalyzerJobBuilder<A> addExploringAnalyzer(Class<A> analyzerClass) {
		AnalyzerBeanDescriptor<A> descriptor = _configuration.getDescriptorProvider().getAnalyzerBeanDescriptorForClass(
				analyzerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: " + analyzerClass);
		}
		ExploringAnalyzerJobBuilder<A> analyzerJobBuilder = new ExploringAnalyzerJobBuilder<A>(this, descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);

		// make a copy since some of the listeners may add additional listeners
		// which will otherwise cause ConcurrentModificationExceptions
		List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(_analyzerChangeListeners);
		for (AnalyzerChangeListener listener : listeners) {
			listener.onAdd(analyzerJobBuilder);
		}
		return analyzerJobBuilder;
	}

	public <A extends RowProcessingAnalyzer<?>> RowProcessingAnalyzerJobBuilder<A> addRowProcessingAnalyzer(
			AnalyzerBeanDescriptor<A> descriptor) {
		RowProcessingAnalyzerJobBuilder<A> analyzerJobBuilder = new RowProcessingAnalyzerJobBuilder<A>(this, descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);

		if (analyzerJobBuilder.getRequirement() == null) {
			analyzerJobBuilder.setRequirement(_defaultRequirement);
		}

		// make a copy since some of the listeners may add additional listeners
		// which will otherwise cause ConcurrentModificationExceptions
		List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(_analyzerChangeListeners);
		for (AnalyzerChangeListener listener : listeners) {
			listener.onAdd(analyzerJobBuilder);
		}
		return analyzerJobBuilder;
	}

	public <A extends RowProcessingAnalyzer<?>> RowProcessingAnalyzerJobBuilder<A> addRowProcessingAnalyzer(
			Class<A> analyzerClass) {
		AnalyzerBeanDescriptor<A> descriptor = _configuration.getDescriptorProvider().getAnalyzerBeanDescriptorForClass(
				analyzerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: " + analyzerClass);
		}
		return addRowProcessingAnalyzer(descriptor);
	}

	public AnalysisJobBuilder removeAnalyzer(RowProcessingAnalyzerJobBuilder<?> ajb) {
		boolean removed = _analyzerJobBuilders.remove(ajb);
		if (removed) {
			for (AnalyzerChangeListener listener : _analyzerChangeListeners) {
				listener.onRemove(ajb);
			}
		}
		return this;
	}

	public AnalysisJobBuilder removeAnalyzer(ExploringAnalyzerJobBuilder<?> ajb) {
		boolean removed = _analyzerJobBuilders.remove(ajb);
		if (removed) {
			for (AnalyzerChangeListener listener : _analyzerChangeListeners) {
				listener.onRemove(ajb);
			}
		}
		return this;
	}

	public List<InputColumn<?>> getAvailableInputColumns(DataTypeFamily dataTypeFamily) {
		SourceColumnFinder finder = new SourceColumnFinder();
		finder.addSources(this);
		return finder.findInputColumns(dataTypeFamily);
	}

	/**
	 * Used to verify whether or not the builder's configuration is valid and
	 * all properties are satisfied.
	 * 
	 * @param throwException
	 *            whether or not an exception should be thrown in case of
	 *            invalid configuration. Typically an exception message will
	 *            contain more detailed information about the cause of the
	 *            validation error, whereas a boolean contains no details.
	 * @return true if the analysis job builder is correctly configured
	 * @throws IllegalStateException
	 */
	public boolean isConfigured(final boolean throwException) throws IllegalStateException,
			UnconfiguredConfiguredPropertyException {
		if (_dataContextProvider == null) {
			if (throwException) {
				throw new IllegalStateException("No Datastore or DataContextProvider set");
			}
			return false;
		}

		boolean exploringAnalyzers = false;
		for (AnalyzerJobBuilder<?> analyzerJobBuilder : _analyzerJobBuilders) {
			if (analyzerJobBuilder.getDescriptor().isExploringAnalyzer()) {
				exploringAnalyzers = true;
				break;
			}
		}

		if (!exploringAnalyzers && _sourceColumns.isEmpty()) {
			if (throwException) {
				throw new IllegalStateException("No source columns in job");
			}
			return false;
		}

		if (_analyzerJobBuilders.isEmpty()) {
			if (throwException) {
				throw new IllegalStateException("No Analyzers in job");
			}
			return false;
		}

		for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
			if (!fjb.isConfigured(throwException)) {
				return false;
			}
		}

		for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
			if (!tjb.isConfigured(throwException)) {
				return false;
			}
		}

		for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
			if (!ajb.isConfigured(throwException)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Used to verify whether or not the builder's configuration is valid and
	 * all properties are satisfied.
	 * 
	 * @return true if the analysis job builder is correctly configured
	 */
	public boolean isConfigured() {
		return isConfigured(false);
	}

	public AnalysisJob toAnalysisJob() throws IllegalStateException {
		if (!isConfigured(true)) {
			throw new IllegalStateException("Analysis job is not correctly configured");
		}

		Collection<FilterJob> filterJobs = new LinkedList<FilterJob>();
		for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
			try {
				FilterJob filterJob = fjb.toFilterJob();
				filterJobs.add(filterJob);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not create filter job from builder: " + fjb + ", (" + e.getMessage()
						+ ")", e);
			}
		}

		Collection<MergedOutcomeJob> mergedOutcomeJobs = new LinkedList<MergedOutcomeJob>();
		for (MergedOutcomeJobBuilder mojb : _mergedOutcomeJobBuilders) {
			try {
				MergedOutcomeJob mergedOutcomeJob = mojb.toMergedOutcomeJob();
				mergedOutcomeJobs.add(mergedOutcomeJob);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not create merged outcome job from builder: " + mojb + ", ("
						+ e.getMessage() + ")", e);
			}
		}

		Collection<TransformerJob> transformerJobs = new LinkedList<TransformerJob>();
		for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
			try {
				TransformerJob transformerJob = tjb.toTransformerJob();
				transformerJobs.add(transformerJob);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not create transformer job from builder: " + tjb + ", ("
						+ e.getMessage() + ")", e);
			}
		}

		Collection<AnalyzerJob> analyzerJobs = new LinkedList<AnalyzerJob>();
		for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
			try {
				AnalyzerJob[] analyzerJob = ajb.toAnalyzerJobs();
				for (AnalyzerJob job : analyzerJob) {
					analyzerJobs.add(job);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Could not create analyzer job from builder: " + ajb + ", ("
						+ e.getMessage() + ")", e);
			}
		}

		DataContextProvider dcp = _dataContextProvider;
		Datastore datastore = dcp.getDatastore();
		return new ImmutableAnalysisJob(datastore, _sourceColumns, filterJobs, transformerJobs, analyzerJobs,
				mergedOutcomeJobs);
	}

	public InputColumn<?> getSourceColumnByName(String name) {
		if (name != null) {
			for (MetaModelInputColumn inputColumn : _sourceColumns) {
				if (name.equals(inputColumn.getName())) {
					return inputColumn;
				}
			}

			for (MetaModelInputColumn inputColumn : _sourceColumns) {
				if (name.equalsIgnoreCase(inputColumn.getName())) {
					return inputColumn;
				}
			}
		}
		return null;
	}

	public TransformerJobBuilder<?> getOriginatingTransformer(InputColumn<?> outputColumn) {
		SourceColumnFinder finder = new SourceColumnFinder();
		finder.addSources(this);
		InputColumnSourceJob source = finder.findInputColumnSource(outputColumn);
		if (source instanceof TransformerJobBuilder) {
			return (TransformerJobBuilder<?>) source;
		}
		return null;
	}

	public Table getOriginatingTable(InputColumn<?> inputColumn) {
		SourceColumnFinder finder = new SourceColumnFinder();
		finder.addSources(this);
		return finder.findOriginatingTable(inputColumn);
	}

	public Table getOriginatingTable(AbstractBeanWithInputColumnsBuilder<?, ?, ?> beanJobBuilder) {
		List<InputColumn<?>> inputColumns = beanJobBuilder.getInputColumns();
		if (inputColumns.isEmpty()) {
			return null;
		} else {
			return getOriginatingTable(inputColumns.get(0));
		}
	}

	public List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> getAvailableUnfilteredBeans(
			FilterJobBuilder<?, ?> filterJobBuilder) {
		List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> result = new ArrayList<AbstractBeanWithInputColumnsBuilder<?, ?, ?>>();
		if (filterJobBuilder.isConfigured()) {
			final Table requiredTable = getOriginatingTable(filterJobBuilder);

			for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
				if (fjb != filterJobBuilder) {
					if (fjb.getRequirement() == null) {
						Table foundTable = getOriginatingTable(fjb);
						if (requiredTable == null || requiredTable.equals(foundTable)) {
							result.add(fjb);
						}
					}
				}
			}

			for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
				if (tjb.getRequirement() == null) {
					Table foundTable = getOriginatingTable(tjb);
					if (requiredTable == null || requiredTable.equals(foundTable)) {
						result.add(tjb);
					}
				}
			}

			for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
				if (ajb instanceof RowProcessingAnalyzerJobBuilder<?>) {
					RowProcessingAnalyzerJobBuilder<?> rpajb = (RowProcessingAnalyzerJobBuilder<?>) ajb;
					if (rpajb.getRequirement() == null) {
						Table foundTable = getOriginatingTable(rpajb);
						if (requiredTable == null || requiredTable.equals(foundTable)) {
							result.add(rpajb);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Sets a default requirement for all newly added and existing row
	 * processing component, unless they have another requirement.
	 * 
	 * @param filterJobBuilder
	 * @param category
	 */
	public void setDefaultRequirement(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
		setDefaultRequirement(filterJobBuilder.getOutcome(category));
	}

	/**
	 * Sets a default requirement for all newly added and existing row
	 * processing component, unless they have another requirement.
	 * 
	 * @param defaultRequirement
	 */
	public void setDefaultRequirement(final Outcome defaultRequirement) {
		_defaultRequirement = defaultRequirement;
		if (defaultRequirement != null) {
			for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
				if (ajb instanceof RowProcessingAnalyzerJobBuilder) {
					RowProcessingAnalyzerJobBuilder<?> rowProcessingAnalyzerJobBuilder = (RowProcessingAnalyzerJobBuilder<?>) ajb;
					Outcome requirement = rowProcessingAnalyzerJobBuilder.getRequirement();
					if (requirement == null) {
						rowProcessingAnalyzerJobBuilder.setRequirement(defaultRequirement);
					}
				}
			}

			for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
				if (tjb.getRequirement() == null) {
					tjb.setRequirement(defaultRequirement);
				}
			}

			final FilterJobBuilder<?, ?> sourceFilterJobBuilder;
			if (defaultRequirement instanceof LazyFilterOutcome) {
				sourceFilterJobBuilder = ((LazyFilterOutcome) defaultRequirement).getFilterJobBuilder();
			} else {
				logger.warn("Default requirement is not a LazyFilterOutcome. This might cause self-referring requirements.");
				sourceFilterJobBuilder = null;
			}

			for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
				if (fjb != sourceFilterJobBuilder && fjb.getRequirement() == null) {
					fjb.setRequirement(defaultRequirement);
				}
			}
		}
	}

	/**
	 * Gets a default requirement, which will be applied to all newly added row
	 * processing components.
	 * 
	 * @return a default requirement, which will be applied to all newly added
	 *         row processing components.
	 */
	public Outcome getDefaultRequirement() {
		return _defaultRequirement;
	}

	public List<SourceColumnChangeListener> getSourceColumnListeners() {
		return _sourceColumnListeners;
	}

	public List<AnalyzerChangeListener> getAnalyzerChangeListeners() {
		return _analyzerChangeListeners;
	}

	public List<TransformerChangeListener> getTransformerChangeListeners() {
		return _transformerChangeListeners;
	}

	public List<FilterChangeListener> getFilterChangeListeners() {
		return _filterChangeListeners;
	}

	@Override
	public void close() {
		if (_dataContextProvider != null) {
			_dataContextProvider.close();
		}
	}
}