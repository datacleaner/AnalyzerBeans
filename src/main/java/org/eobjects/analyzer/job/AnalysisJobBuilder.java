package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

import dk.eobjects.metamodel.schema.Column;

public class AnalysisJobBuilder {

	private AnalyzerBeansConfiguration _configuration;
	private DataContextProvider _dataContextProvider;
	private List<Column> _sourceColumns = new ArrayList<Column>();
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
		if (!_sourceColumns.contains(column)) {
			_sourceColumns.add(column);
		}
		return this;
	}

	public AnalysisJobBuilder addSourceColumns(Column... columns) {
		for (Column column : columns) {
			addSourceColumn(column);
		}
		return this;
	}

	public AnalysisJobBuilder removeSourceColumn(Column column) {
		_sourceColumns.remove(column);
		// TODO: Notify consumers
		return this;
	}

	public List<Column> getSourceColumns() {
		return Collections.unmodifiableList(_sourceColumns);
	}

	public TransformerJobBuilder addTransformer(
			Class<? extends Transformer<?>> transformerClass) {
		TransformerBeanDescriptor descriptor = _configuration
				.getDescriptorProvider().getTransformerBeanDescriptorForClass(
						transformerClass);
		return addTransformer(descriptor);
	}

	public TransformerJobBuilder addTransformer(
			TransformerBeanDescriptor descriptor) {
		return new TransformerJobBuilder(descriptor,
				transformedColumnIdGenerator, this);
	}
}