package org.eobjects.analyzer.job;

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
import org.eobjects.analyzer.util.SchemaNavigator;

import dk.eobjects.metamodel.schema.Column;

public class AnalysisJobBuilder {

	private final AnalyzerBeansConfiguration _configuration;
	private final IdGenerator transformedColumnIdGenerator = new PrefixedIdGenerator("trans");

	// the configurable components
	private DataContextProvider _dataContextProvider;
	private final List<MetaModelInputColumn> _sourceColumns = new ArrayList<MetaModelInputColumn>();
	private final List<FilterJobBuilder<?, ?>> _filterJobBuilders = new ArrayList<FilterJobBuilder<?, ?>>();
	private final List<TransformerJobBuilder<?>> _transformerJobBuilders = new ArrayList<TransformerJobBuilder<?>>();
	private final List<AnalyzerJobBuilder<?>> _analyzerJobBuilders = new ArrayList<AnalyzerJobBuilder<?>>();

	// listeners, typically for UI that uses the builders
	private final List<SourceColumnChangeListener> _sourceColumnListeners = new LinkedList<SourceColumnChangeListener>();
	private final List<AnalyzerChangeListener> _analyzerChangeListeners = new LinkedList<AnalyzerChangeListener>();

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
		if (datastore == null) {
			throw new IllegalArgumentException("Datastore cannot be null");
		}
		DataContextProvider dataContextProvider = datastore.getDataContextProvider();
		return setDataContextProvider(dataContextProvider);
	}

	public AnalysisJobBuilder setDataContextProvider(DataContextProvider dataContextProvider) {
		if (dataContextProvider == null) {
			throw new IllegalArgumentException("DataContextProvider cannot be null");
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
			for (SourceColumnChangeListener listener : _sourceColumnListeners) {
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
		_sourceColumns.remove(inputColumn);
		for (SourceColumnChangeListener listener : _sourceColumnListeners) {
			listener.onRemove(inputColumn);
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
		TransformerJobBuilder<T> transformerJobBuilder = new TransformerJobBuilder<T>(descriptor,
				transformedColumnIdGenerator);
		_transformerJobBuilders.add(transformerJobBuilder);
		return transformerJobBuilder;
	}

	public AnalysisJobBuilder removeTransformer(TransformerJobBuilder<?> tjb) {
		_transformerJobBuilders.remove(tjb);
		// TODO: Notify transformed column consumers
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
		FilterJobBuilder<F, C> fjb = new FilterJobBuilder<F, C>(descriptor);
		_filterJobBuilders.add(fjb);
		return fjb;
	}

	public AnalysisJobBuilder removeFilter(FilterJobBuilder<?, ?> fjb) {
		_filterJobBuilders.remove(fjb);
		// TODO: Notify outcome consumers
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
		ExploringAnalyzerJobBuilder<A> analyzerJobBuilder = new ExploringAnalyzerJobBuilder<A>(descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);
		for (AnalyzerChangeListener listener : _analyzerChangeListeners) {
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
		RowProcessingAnalyzerJobBuilder<A> analyzerJobBuilder = new RowProcessingAnalyzerJobBuilder<A>(descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);
		for (AnalyzerChangeListener listener : _analyzerChangeListeners) {
			listener.onAdd(analyzerJobBuilder);
		}
		return analyzerJobBuilder;
	}

	public AnalysisJobBuilder removeAnalyzer(RowProcessingAnalyzerJobBuilder<?> ajb) {
		_analyzerJobBuilders.remove(ajb);
		for (AnalyzerChangeListener listener : _analyzerChangeListeners) {
			listener.onRemove(ajb);
		}
		return this;
	}

	public Collection<InputColumn<?>> getAvailableInputColumns(DataTypeFamily dataTypeFamily) {
		if (dataTypeFamily == null) {
			dataTypeFamily = DataTypeFamily.UNDEFINED;
		}

		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		List<MetaModelInputColumn> sourceColumns = getSourceColumns();
		for (MetaModelInputColumn sourceColumn : sourceColumns) {
			if (dataTypeFamily == DataTypeFamily.UNDEFINED || sourceColumn.getDataTypeFamily() == dataTypeFamily) {
				result.add(sourceColumn);
			}
		}

		for (TransformerJobBuilder<?> transformerJobBuilder : _transformerJobBuilders) {
			List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
			for (MutableInputColumn<?> outputColumn : outputColumns) {
				if (dataTypeFamily == DataTypeFamily.UNDEFINED || outputColumn.getDataTypeFamily() == dataTypeFamily) {
					result.add(outputColumn);
				}
			}
		}

		return result;
	}

	public boolean isConfigured() {
		if (_dataContextProvider == null) {
			return false;
		}

		if (_analyzerJobBuilders.isEmpty()) {
			return false;
		}

		for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
			if (!fjb.isConfigured()) {
				return false;
			}
		}

		for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
			if (!tjb.isConfigured()) {
				return false;
			}
		}

		for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
			if (!ajb.isConfigured()) {
				return false;
			}
		}

		return true;
	}

	public AnalysisJob toAnalysisJob() throws IllegalStateException {
		if (!isConfigured()) {
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
				AnalyzerJob analyzerJob = ajb.toAnalyzerJob();
				analyzerJobs.add(analyzerJob);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Could not create analyzer job from builder: " + ajb + ", ("
						+ e.getMessage() + ")", e);
			}
		}

		return new ImmutableAnalysisJob(_dataContextProvider, _sourceColumns, filterJobs, transformerJobs, analyzerJobs);
	}

	public InputColumn<?> getSourceColumnByName(String name) {
		if (name != null) {
			for (MetaModelInputColumn inputColumn : _sourceColumns) {
				if (name.equals(inputColumn.getName())) {
					return inputColumn;
				}
			}
		}
		return null;
	}

	public List<SourceColumnChangeListener> getSourceColumnListeners() {
		return _sourceColumnListeners;
	}

	public List<AnalyzerChangeListener> getAnalyzerChangeListeners() {
		return _analyzerChangeListeners;
	}

	/**
	 * Convenience method to get all input columns (both source or from
	 * transformers) that comply to a given data type family.
	 * 
	 * @param dataTypeFamily
	 * @return
	 */
	public List<InputColumn<?>> getInputColumns(DataTypeFamily dataTypeFamily) {
		if (dataTypeFamily == null) {
			throw new IllegalArgumentException("dataTypeFamily cannot be null. Use " + DataTypeFamily.UNDEFINED
					+ " for all input columns");
		}
		List<InputColumn<?>> inputColumns = new ArrayList<InputColumn<?>>();
		List<MetaModelInputColumn> sourceColumns = getSourceColumns();
		for (MetaModelInputColumn col : sourceColumns) {
			if (dataTypeFamily == DataTypeFamily.UNDEFINED || col.getDataTypeFamily() == dataTypeFamily) {
				inputColumns.add(col);
			}
		}

		List<TransformerJobBuilder<?>> transformerJobBuilders = getTransformerJobBuilders();
		for (TransformerJobBuilder<?> transformerJobBuilder : transformerJobBuilders) {
			List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
			for (MutableInputColumn<?> col : outputColumns) {
				if (dataTypeFamily == DataTypeFamily.UNDEFINED || col.getDataTypeFamily() == dataTypeFamily) {
					inputColumns.add(col);
				}
			}
		}

		return inputColumns;
	}
}