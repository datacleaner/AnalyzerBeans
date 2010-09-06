package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

import dk.eobjects.metamodel.schema.Column;

public class AnalysisJobBuilder {

	private AnalyzerBeansConfiguration _configuration;
	private DataContextProvider _dataContextProvider;
	private List<MetaModelInputColumn> _sourceColumns = new ArrayList<MetaModelInputColumn>();
	private List<TransformerJobBuilder<?>> _transformerJobBuilders = new ArrayList<TransformerJobBuilder<?>>();
	private List<AnalyzerJobBuilder<?>> _analyzerJobBuilders = new ArrayList<AnalyzerJobBuilder<?>>();
	private IdGenerator transformedColumnIdGenerator = new PrefixedIdGenerator(
			"trans");

	public AnalysisJobBuilder(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
	}

	public AnalysisJobBuilder setDatastore(String datastoreName) {
		Datastore datastore = _configuration.getDatastoreCatalog()
				.getDatastore(datastoreName);
		if (datastore == null) {
			throw new IllegalArgumentException("No such datastore: "
					+ datastoreName);
		}
		return setDatastore(datastore);
	}

	public AnalysisJobBuilder setDatastore(Datastore datastore) {
		if (datastore == null) {
			throw new IllegalArgumentException("Datastore cannot be null");
		}
		DataContextProvider dataContextProvider = datastore
				.getDataContextProvider();
		return setDataContextProvider(dataContextProvider);
	}

	public AnalysisJobBuilder setDataContextProvider(
			DataContextProvider dataContextProvider) {
		if (dataContextProvider == null) {
			throw new IllegalArgumentException(
					"DataContextProvider cannot be null");
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
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(Column... columns) {
		for (Column column : columns) {
			addSourceColumn(column);
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(
			MetaModelInputColumn... inputColumns) {
		for (MetaModelInputColumn metaModelInputColumn : inputColumns) {
			addSourceColumn(metaModelInputColumn);
		}
		return this;
	}

	public AnalysisJobBuilder removeSourceColumn(Column column) {
		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		return removeSourceColumn(inputColumn);
	}

	public AnalysisJobBuilder removeSourceColumn(
			MetaModelInputColumn inputColumn) {
		_sourceColumns.remove(inputColumn);
		// TODO: Notify consumers
		return this;
	}

	public List<MetaModelInputColumn> getSourceColumns() {
		return Collections.unmodifiableList(_sourceColumns);
	}

	public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(
			Class<T> transformerClass) {
		TransformerBeanDescriptor<T> descriptor = _configuration
				.getDescriptorProvider().getTransformerBeanDescriptorForClass(
						transformerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: "
					+ transformerClass);
		}
		return addTransformer(descriptor);
	}

	public List<TransformerJobBuilder<?>> getTransformerJobBuilders() {
		return Collections.unmodifiableList(_transformerJobBuilders);
	}

	public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(
			TransformerBeanDescriptor<T> descriptor) {
		TransformerJobBuilder<T> transformerJobBuilder = new TransformerJobBuilder<T>(
				descriptor, transformedColumnIdGenerator);
		_transformerJobBuilders.add(transformerJobBuilder);
		return transformerJobBuilder;
	}

	public AnalysisJobBuilder removeTransformer(TransformerJobBuilder<?> tjb) {
		_transformerJobBuilders.remove(tjb);
		return this;
	}

	public List<AnalyzerJobBuilder<?>> getAnalyzerJobBuilders() {
		return Collections.unmodifiableList(_analyzerJobBuilders);
	}

	public <A extends ExploringAnalyzer<?>> ExploringAnalyzerJobBuilder<A> addExploringAnalyzer(
			Class<A> analyzerClass) {
		AnalyzerBeanDescriptor<A> descriptor = _configuration
				.getDescriptorProvider().getAnalyzerBeanDescriptorForClass(
						analyzerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: "
					+ analyzerClass);
		}
		ExploringAnalyzerJobBuilder<A> analyzerJobBuilder = new ExploringAnalyzerJobBuilder<A>(
				descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);
		return analyzerJobBuilder;
	}

	public <A extends RowProcessingAnalyzer<?>> RowProcessingAnalyzerJobBuilder<A> addRowProcessingAnalyzer(
			Class<A> analyzerClass) {
		AnalyzerBeanDescriptor<A> descriptor = _configuration
				.getDescriptorProvider().getAnalyzerBeanDescriptorForClass(
						analyzerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: "
					+ analyzerClass);
		}
		RowProcessingAnalyzerJobBuilder<A> analyzerJobBuilder = new RowProcessingAnalyzerJobBuilder<A>(
				descriptor);
		_analyzerJobBuilders.add(analyzerJobBuilder);
		return analyzerJobBuilder;
	}

	public AnalysisJobBuilder removeAnalyzer(
			RowProcessingAnalyzerJobBuilder<?> ajb) {
		_analyzerJobBuilders.remove(ajb);
		return this;
	}

	public Collection<InputColumn<?>> getAvailableInputColumns(
			DataTypeFamily dataTypeFamily) {
		if (dataTypeFamily == null) {
			dataTypeFamily = DataTypeFamily.UNDEFINED;
		}

		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		List<MetaModelInputColumn> sourceColumns = getSourceColumns();
		for (MetaModelInputColumn sourceColumn : sourceColumns) {
			if (dataTypeFamily == DataTypeFamily.UNDEFINED
					|| sourceColumn.getDataTypeFamily() == dataTypeFamily) {
				result.add(sourceColumn);
			}
		}

		for (TransformerJobBuilder<?> transformerJobBuilder : _transformerJobBuilders) {
			List<MutableInputColumn<?>> outputColumns = transformerJobBuilder
					.getOutputColumns();
			for (MutableInputColumn<?> outputColumn : outputColumns) {
				if (dataTypeFamily == DataTypeFamily.UNDEFINED
						|| outputColumn.getDataTypeFamily() == dataTypeFamily) {
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
			throw new IllegalStateException(
					"Analysis job is not correctly configured");
		}

		Collection<TransformerJob> transformerJobs = new LinkedList<TransformerJob>();
		for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
			try {
				TransformerJob transformerJob = tjb.toTransformerJob();
				transformerJobs.add(transformerJob);
			} catch (IllegalStateException e) {
				throw new IllegalStateException(
						"Could not create transformer job from builder: " + tjb
								+ ", (" + e.getMessage() + ")", e);
			}
		}

		Collection<AnalyzerJob> analyzerJobs = new LinkedList<AnalyzerJob>();
		for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
			try {
				AnalyzerJob analyzerJob = ajb.toAnalyzerJob();
				analyzerJobs.add(analyzerJob);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(
						"Could not create analyzer job from builder: " + ajb
								+ ", (" + e.getMessage() + ")", e);
			}
		}

		return new ImmutableAnalysisJob(_dataContextProvider, _sourceColumns,
				transformerJobs, analyzerJobs);
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
}