package org.eobjects.analyzer.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.jaxb.AnalysisType;
import org.eobjects.analyzer.job.jaxb.AnalyzerType;
import org.eobjects.analyzer.job.jaxb.ColumnType;
import org.eobjects.analyzer.job.jaxb.ColumnsType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType.Property;
import org.eobjects.analyzer.job.jaxb.DataContextType;
import org.eobjects.analyzer.job.jaxb.InputType;
import org.eobjects.analyzer.job.jaxb.Job;
import org.eobjects.analyzer.job.jaxb.ObjectFactory;
import org.eobjects.analyzer.job.jaxb.OutputType;
import org.eobjects.analyzer.job.jaxb.SourceType;
import org.eobjects.analyzer.job.jaxb.TransformationType;
import org.eobjects.analyzer.job.jaxb.TransformerDescriptorType;
import org.eobjects.analyzer.job.jaxb.TransformerType;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Column;

public class JaxbJobFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(JaxbJobFactory.class);

	private JAXBContext _jaxbContext;
	private AnalyzerBeansConfiguration _configuration;

	public JaxbJobFactory(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
		try {
			_jaxbContext = JAXBContext.newInstance(ObjectFactory.class
					.getPackage().getName());
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	public AnalysisJobBuilder create(File file) {
		try {
			return create(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalysisJobBuilder create(InputStream inputStream) {
		try {
			Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

			unmarshaller.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					int severity = event.getSeverity();
					if (severity == ValidationEvent.WARNING) {
						logger.warn("encountered JAXB parsing warning: "
								+ event.getMessage());
						return true;
					}

					logger.warn("encountered JAXB parsing error: "
							+ event.getMessage());
					return false;
				}
			});
			Job job = (Job) unmarshaller.unmarshal(inputStream);
			return create(job);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalysisJobBuilder create(Job job) {
		SourceType source = job.getSource();

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
				_configuration);

		DataContextType dataContext = source.getDataContext();
		String ref = dataContext.getRef();
		if (isNullOrEmpty(ref)) {
			throw new IllegalStateException("Datastore ref cannot be null");
		}

		Datastore datastore = _configuration.getDatastoreCatalog()
				.getDatastore(ref);
		if (datastore == null) {
			throw new IllegalStateException("No such datastore: " + ref);
		}
		DataContextProvider dataContextProvider = datastore
				.getDataContextProvider();
		SchemaNavigator schemaNavigator = dataContextProvider
				.getSchemaNavigator();

		analysisJobBuilder.setDataContextProvider(dataContextProvider);

		Map<String, InputColumn<?>> inputColumns = new HashMap<String, InputColumn<?>>();

		ColumnsType columnsType = source.getColumns();
		List<ColumnType> columns = columnsType.getColumn();
		for (ColumnType column : columns) {
			String path = column.getPath();
			if (isNullOrEmpty(path)) {
				throw new IllegalStateException("Column path cannot be null");
			}
			Column physicalColumn = schemaNavigator.convertToColumn(path);
			if (physicalColumn == null) {
				throw new IllegalStateException("No such column: " + path);
			}
			MetaModelInputColumn inputColumn = new MetaModelInputColumn(
					physicalColumn);
			String id = column.getId();
			if (isNullOrEmpty(id)) {
				throw new IllegalStateException(
						"Source column id cannot be null");
			}

			registerInputColumn(inputColumns, id, inputColumn);
			analysisJobBuilder.addSourceColumn(inputColumn);
		}

		TransformationType transformation = job.getTransformation();
		List<TransformerType> transformers = transformation.getTransformer();

		Map<TransformerType, TransformerJobBuilder<?>> transformerJobBuilders = new HashMap<TransformerType, TransformerJobBuilder<?>>();

		for (TransformerType transformer : transformers) {
			TransformerDescriptorType descriptor = transformer.getDescriptor();
			ref = descriptor.getRef();
			if (isNullOrEmpty(ref)) {
				throw new IllegalStateException(
						"Transformer descriptor ref cannot be null");
			}
			TransformerBeanDescriptor<?> transformerBeanDescriptor = _configuration
					.getDescriptorProvider()
					.getTransformerBeanDescriptorByDisplayName(ref);
			if (transformerBeanDescriptor == null) {
				throw new IllegalStateException(
						"No such transformer descriptor: " + ref);
			}
			TransformerJobBuilder<?> transformerJobBuilder = analysisJobBuilder
					.addTransformer(transformerBeanDescriptor);

			ConfiguredPropertiesType properties = transformer.getProperties();
			applyProperties(transformerJobBuilder, properties);

			transformerJobBuilders.put(transformer, transformerJobBuilder);
		}

		List<TransformerType> unconfiguredTransformerKeys = new LinkedList<TransformerType>(
				transformerJobBuilders.keySet());
		while (!unconfiguredTransformerKeys.isEmpty()) {
			for (Iterator<TransformerType> it = unconfiguredTransformerKeys
					.iterator(); it.hasNext();) {
				boolean configurable = true;

				TransformerType unconfiguredTransformerKey = it.next();
				List<InputType> input = unconfiguredTransformerKey.getInput();
				for (InputType inputType : input) {
					ref = inputType.getRef();
					if (isNullOrEmpty(ref)) {
						throw new IllegalStateException(
								"Transformer input column ref cannot be null");
					}
					if (!inputColumns.containsKey(ref)) {
						configurable = false;
						break;
					}
				}

				if (configurable) {
					TransformerJobBuilder<?> transformerJobBuilder = transformerJobBuilders
							.get(unconfiguredTransformerKey);

					for (InputType inputType : input) {
						InputColumn<?> inputColumn = inputColumns.get(inputType
								.getRef());
						transformerJobBuilder.addInputColumn(inputColumn);
					}

					List<MutableInputColumn<?>> outputColumns = transformerJobBuilder
							.getOutputColumns();
					List<OutputType> output = unconfiguredTransformerKey
							.getOutput();

					assert outputColumns.size() == output.size();

					for (int i = 0; i < output.size(); i++) {
						OutputType o1 = output.get(i);
						MutableInputColumn<?> o2 = outputColumns.get(i);
						String name = o1.getName();
						if (!isNullOrEmpty(name)) {
							o2.setName(name);
						}
						String id = o1.getId();
						if (isNullOrEmpty(id)) {
							throw new IllegalStateException(
									"Transformer output column id cannot be null");
						}
						registerInputColumn(inputColumns, id, o2);
					}

					it.remove();
				}
			}
		}

		AnalysisType analysis = job.getAnalysis();
		List<AnalyzerType> analyzers = analysis.getAnalyzer();
		for (AnalyzerType analyzerType : analyzers) {
			ref = analyzerType.getDescriptor().getRef();
			if (isNullOrEmpty(ref)) {
				throw new IllegalStateException(
						"Analyzer descriptor ref cannot be null");
			}

			AnalyzerBeanDescriptor<?> descriptor = _configuration
					.getDescriptorProvider()
					.getAnalyzerBeanDescriptorByDisplayName(ref);

			if (descriptor == null) {
				throw new IllegalStateException("No such analyzer descriptor: "
						+ ref);
			}

			if (descriptor.isRowProcessingAnalyzer()) {
				@SuppressWarnings("unchecked")
				Class<? extends RowProcessingAnalyzer<?>> beanClass = (Class<? extends RowProcessingAnalyzer<?>>) descriptor
						.getBeanClass();
				RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> analyzerJobBuilder = analysisJobBuilder
						.addRowProcessingAnalyzer(beanClass);

				List<InputType> input = analyzerType.getInput();
				for (InputType inputType : input) {
					ref = inputType.getRef();
					if (isNullOrEmpty(ref)) {
						throw new IllegalStateException(
								"Analyzer input column ref cannot be null");
					}

					InputColumn<?> inputColumn = inputColumns.get(ref);
					analyzerJobBuilder.addInputColumn(inputColumn);
				}
				applyProperties(analyzerJobBuilder,
						analyzerType.getProperties());
			} else if (descriptor.isExploringAnalyzer()) {
				@SuppressWarnings("unchecked")
				Class<? extends ExploringAnalyzer<?>> beanClass = (Class<? extends ExploringAnalyzer<?>>) descriptor
						.getBeanClass();
				ExploringAnalyzerJobBuilder<? extends ExploringAnalyzer<?>> analyzerJobBuilder = analysisJobBuilder
						.addExploringAnalyzer(beanClass);
				applyProperties(analyzerJobBuilder,
						analyzerType.getProperties());
			} else {
				throw new IllegalStateException(
						"AnalyzerBeanDescriptor is neither row processing or exploring: "
								+ descriptor);
			}
		}

		return analysisJobBuilder;
	}

	private void registerInputColumn(Map<String, InputColumn<?>> inputColumns,
			String id, InputColumn<?> inputColumn) {
		if (inputColumns.containsKey(id)) {
			throw new IllegalStateException("Column id is not unique: " + id);
		}
		inputColumns.put(id, inputColumn);
	}

	private boolean isNullOrEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	private void applyProperties(
			AbstractBeanJobBuilder<? extends BeanDescriptor<?>, ?, ?> builder,
			ConfiguredPropertiesType configuredPropertiesType) {
		if (configuredPropertiesType != null) {
			List<Property> properties = configuredPropertiesType.getProperty();
			BeanDescriptor<?> descriptor = builder.getDescriptor();
			for (Property property : properties) {
				String name = property.getName();
				String value = property.getValue();
				
				ConfiguredPropertyDescriptor configuredProperty = descriptor
				.getConfiguredProperty(name);
				
				// TODO: Convert value according to configuredProperty's type
				builder.setConfiguredProperty(configuredProperty, value);
			}
		}
	}
}
