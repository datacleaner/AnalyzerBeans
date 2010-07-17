package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

import dk.eobjects.metamodel.schema.Column;

public class AnalysisJobBuilder {

	private AnalyzerBeansConfiguration _configuration;
	private DataContextProvider _dataContextProvider;
	private List<MetaModelInputColumn> _sourceColumns = new ArrayList<MetaModelInputColumn>();
	private List<TransformerJobBuilder> _transformerJobBuilders = new ArrayList<TransformerJobBuilder>();
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

	public TransformerJobBuilder addTransformer(
			Class<? extends Transformer<?>> transformerClass) {
		TransformerBeanDescriptor descriptor = _configuration
				.getDescriptorProvider().getTransformerBeanDescriptorForClass(
						transformerClass);
		if (descriptor == null) {
			throw new IllegalArgumentException("No descriptor found for: "
					+ transformerClass);
		}
		return addTransformer(descriptor);
	}

	public TransformerJobBuilder addTransformer(
			TransformerBeanDescriptor descriptor) {
		TransformerJobBuilder transformerJobBuilder = new TransformerJobBuilder(
				descriptor, transformedColumnIdGenerator, this);
		_transformerJobBuilders.add(transformerJobBuilder);
		return transformerJobBuilder;
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

		for (TransformerJobBuilder transformerJobBuilder : _transformerJobBuilders) {
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
}