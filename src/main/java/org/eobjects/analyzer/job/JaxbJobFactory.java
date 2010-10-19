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

import org.eobjects.analyzer.beans.api.ExploringAnalyzer;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.ExploringAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.LazyFilterOutcome;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.jaxb.AnalysisType;
import org.eobjects.analyzer.job.jaxb.AnalyzerType;
import org.eobjects.analyzer.job.jaxb.ColumnType;
import org.eobjects.analyzer.job.jaxb.ColumnsType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType.Property;
import org.eobjects.analyzer.job.jaxb.DataContextType;
import org.eobjects.analyzer.job.jaxb.FilterType;
import org.eobjects.analyzer.job.jaxb.InputType;
import org.eobjects.analyzer.job.jaxb.Job;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.job.jaxb.ObjectFactory;
import org.eobjects.analyzer.job.jaxb.OutcomeType;
import org.eobjects.analyzer.job.jaxb.OutputType;
import org.eobjects.analyzer.job.jaxb.SourceType;
import org.eobjects.analyzer.job.jaxb.TransformationType;
import org.eobjects.analyzer.job.jaxb.TransformerDescriptorType;
import org.eobjects.analyzer.job.jaxb.TransformerType;
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.StringConversionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Column;

public class JaxbJobFactory {

	private static final Logger logger = LoggerFactory.getLogger(JaxbJobFactory.class);

	private JAXBContext _jaxbContext;
	private AnalyzerBeansConfiguration _configuration;

	public JaxbJobFactory(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
		try {
			_jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
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

			unmarshaller.setEventHandler(new JaxbValidationEventHandler());
			Job job = (Job) unmarshaller.unmarshal(inputStream);
			return create(job);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalysisJobBuilder create(Job job) {
		JobMetadataType metadata = job.getJobMetadata();
		if (metadata != null) {
			logger.info("Job name: {}", metadata.getJobName());
			logger.info("Job version: {}", metadata.getJobVersion());
			logger.info("Job description: {}", metadata.getJobDescription());
			logger.info("Author: {}", metadata.getAuthor());
			logger.info("Created date: {}", metadata.getCreatedDate());
			logger.info("Updated date: {}", metadata.getUpdatedDate());
		}

		SourceType source = job.getSource();

		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(_configuration);

		DataContextType dataContext = source.getDataContext();
		String ref = dataContext.getRef();
		if (StringUtils.isNullOrEmpty(ref)) {
			throw new IllegalStateException("Datastore ref cannot be null");
		}

		Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(ref);
		if (datastore == null) {
			throw new IllegalStateException("No such datastore: " + ref);
		}
		DataContextProvider dataContextProvider = datastore.getDataContextProvider();
		SchemaNavigator schemaNavigator = dataContextProvider.getSchemaNavigator();

		analysisJobBuilder.setDataContextProvider(dataContextProvider);

		Map<String, InputColumn<?>> inputColumns = new HashMap<String, InputColumn<?>>();

		ColumnsType columnsType = source.getColumns();
		if (columnsType != null) {
			List<ColumnType> columns = columnsType.getColumn();
			for (ColumnType column : columns) {
				String path = column.getPath();
				if (StringUtils.isNullOrEmpty(path)) {
					throw new IllegalStateException("Column path cannot be null");
				}
				Column physicalColumn = schemaNavigator.convertToColumn(path);
				if (physicalColumn == null) {
					throw new IllegalStateException("No such column: " + path);
				}
				MetaModelInputColumn inputColumn = new MetaModelInputColumn(physicalColumn);
				String id = column.getId();
				if (StringUtils.isNullOrEmpty(id)) {
					throw new IllegalStateException("Source column id cannot be null");
				}

				registerInputColumn(inputColumns, id, inputColumn);
				analysisJobBuilder.addSourceColumn(inputColumn);
			}
		}

		Map<String, FilterOutcome> outcomeMapping = new HashMap<String, FilterOutcome>();

		TransformationType transformation = job.getTransformation();
		if (transformation != null) {
			List<Object> transformersAndFilters = transformation.getTransformerOrFilter();

			Map<TransformerType, TransformerJobBuilder<?>> transformerJobBuilders = new HashMap<TransformerType, TransformerJobBuilder<?>>();
			Map<FilterType, FilterJobBuilder<?, ?>> filterJobBuilders = new HashMap<FilterType, FilterJobBuilder<?, ?>>();

			// iterate to initialize transformers
			for (Object o : transformersAndFilters) {
				if (o instanceof TransformerType) {
					TransformerType transformer = (TransformerType) o;
					ref = transformer.getDescriptor().getRef();
					if (StringUtils.isNullOrEmpty(ref)) {
						throw new IllegalStateException("Transformer descriptor ref cannot be null");
					}
					TransformerBeanDescriptor<?> transformerBeanDescriptor = _configuration.getDescriptorProvider()
							.getTransformerBeanDescriptorByDisplayName(ref);
					if (transformerBeanDescriptor == null) {
						throw new IllegalStateException("No such transformer descriptor: " + ref);
					}
					TransformerJobBuilder<?> transformerJobBuilder = analysisJobBuilder
							.addTransformer(transformerBeanDescriptor);

					applyProperties(transformerJobBuilder, transformer.getProperties(), schemaNavigator);

					transformerJobBuilders.put(transformer, transformerJobBuilder);
				}
			}

			// iterate again to set up transformed column dependencies
			List<TransformerType> unconfiguredTransformerKeys = new LinkedList<TransformerType>(
					transformerJobBuilders.keySet());
			while (!unconfiguredTransformerKeys.isEmpty()) {
				boolean progress = false;
				for (Iterator<TransformerType> it = unconfiguredTransformerKeys.iterator(); it.hasNext();) {
					boolean configurable = true;

					TransformerType unconfiguredTransformerKey = it.next();
					List<InputType> input = unconfiguredTransformerKey.getInput();
					for (InputType inputType : input) {
						ref = inputType.getRef();
						if (StringUtils.isNullOrEmpty(ref)) {
							throw new IllegalStateException("Transformer input column ref cannot be null");
						}
						if (!inputColumns.containsKey(ref)) {
							configurable = false;
							break;
						}
					}

					if (configurable) {
						progress = true;
						TransformerJobBuilder<?> transformerJobBuilder = transformerJobBuilders
								.get(unconfiguredTransformerKey);

						for (InputType inputType : input) {
							InputColumn<?> inputColumn = inputColumns.get(inputType.getRef());
							String name = inputType.getName();
							if (StringUtils.isNullOrEmpty(name)) {
								transformerJobBuilder.addInputColumn(inputColumn);
							} else {
								ConfiguredPropertyDescriptor configuredProperty = transformerJobBuilder.getDescriptor()
										.getConfiguredProperty(name);
								transformerJobBuilder.addInputColumn(inputColumn, configuredProperty);
							}
						}

						List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
						List<OutputType> output = unconfiguredTransformerKey.getOutput();

						assert outputColumns.size() == output.size();

						for (int i = 0; i < output.size(); i++) {
							OutputType o1 = output.get(i);
							MutableInputColumn<?> o2 = outputColumns.get(i);
							String name = o1.getName();
							if (!StringUtils.isNullOrEmpty(name)) {
								o2.setName(name);
							}
							String id = o1.getId();
							if (StringUtils.isNullOrEmpty(id)) {
								throw new IllegalStateException("Transformer output column id cannot be null");
							}
							registerInputColumn(inputColumns, id, o2);
						}

						it.remove();
					}
				}

				if (!progress) {
					StringBuilder sb = new StringBuilder();
					for (TransformerType transformerType : unconfiguredTransformerKeys) {
						if (sb.length() != 0) {
							sb.append(", ");
						}
						TransformerDescriptorType descriptor = transformerType.getDescriptor();
						sb.append(descriptor.getRef());
						sb.append("(input: ");

						List<InputType> input = transformerType.getInput();
						int i = 0;
						for (InputType inputType : input) {
							if (i != 0) {
								sb.append(", ");
							}
							sb.append(inputType.getRef());
							i++;
						}
						sb.append(")");
					}
					throw new IllegalStateException("Could not connect column dependencies for transformers: "
							+ sb.toString());
				}
			}

			// iterate again to initialize all filters and collect all filter
			// outcomes
			for (Object o : transformersAndFilters) {
				if (o instanceof FilterType) {
					FilterType filter = (FilterType) o;
					ref = filter.getDescriptor().getRef();
					if (StringUtils.isNullOrEmpty(ref)) {
						throw new IllegalStateException("Filter descriptor ref cannot be null");
					}
					FilterBeanDescriptor<?, ?> filterBeanDescriptor = _configuration.getDescriptorProvider()
							.getFilterBeanDescriptorByDisplayName(ref);
					if (filterBeanDescriptor == null) {
						throw new IllegalStateException("No such filter descriptor: " + ref);
					}
					FilterJobBuilder<?, ?> filterJobBuilder = analysisJobBuilder.addFilter(filterBeanDescriptor);

					List<InputType> input = filter.getInput();
					for (InputType inputType : input) {
						ref = inputType.getRef();

						if (StringUtils.isNullOrEmpty(ref)) {
							throw new IllegalStateException("Filter input column ref cannot be null");
						}

						InputColumn<?> inputColumn = inputColumns.get(ref);
						if (inputColumn == null) {
							throw new IllegalStateException("No such input column: " + ref);
						}
						String name = inputType.getName();
						if (StringUtils.isNullOrEmpty(name)) {
							filterJobBuilder.addInputColumn(inputColumn);
						} else {
							ConfiguredPropertyDescriptor propertyDescriptor = filterJobBuilder.getDescriptor()
									.getConfiguredProperty(name);
							filterJobBuilder.addInputColumn(inputColumn, propertyDescriptor);
						}
					}

					applyProperties(filterJobBuilder, filter.getProperties(), schemaNavigator);

					filterJobBuilders.put(filter, filterJobBuilder);

					List<OutcomeType> outcomeTypes = filter.getOutcome();
					for (OutcomeType outcomeType : outcomeTypes) {
						String categoryName = outcomeType.getCategory();
						Enum<?> category = filterJobBuilder.getDescriptor().getCategoryByName(categoryName);
						if (category == null) {
							throw new IllegalStateException("No such outcome category name: " + categoryName + " (in "
									+ filterJobBuilder.getDescriptor().getDisplayName());
						}
						outcomeMapping.put(outcomeType.getId(), new LazyFilterOutcome(filterJobBuilder, category));
					}
				}
			}

			// iterate again to set up filter outcome dependencies
			for (Object o : transformersAndFilters) {
				if (o instanceof TransformerType) {
					ref = ((TransformerType) o).getRequires();
					if (ref != null) {
						TransformerJobBuilder<?> builder = transformerJobBuilders.get(o);
						FilterOutcome requirement = outcomeMapping.get(ref);
						if (requirement == null) {
							throw new IllegalStateException("No such outcome id: " + ref);
						}
						builder.setRequirement(requirement);
					}
				} else if (o instanceof FilterType) {
					ref = ((FilterType) o).getRequires();
					if (ref != null) {
						FilterJobBuilder<?, ?> builder = filterJobBuilders.get(o);
						FilterOutcome requirement = outcomeMapping.get(ref);
						if (requirement == null) {
							throw new IllegalStateException("No such outcome id: " + ref);
						}
						builder.setRequirement(requirement);
					}
				} else {
					throw new IllegalStateException("Unexpected transformation child element: " + o);
				}
			}
		}

		AnalysisType analysis = job.getAnalysis();
		List<AnalyzerType> analyzers = analysis.getAnalyzer();
		for (AnalyzerType analyzerType : analyzers) {
			ref = analyzerType.getDescriptor().getRef();
			if (StringUtils.isNullOrEmpty(ref)) {
				throw new IllegalStateException("Analyzer descriptor ref cannot be null");
			}

			AnalyzerBeanDescriptor<?> descriptor = _configuration.getDescriptorProvider()
					.getAnalyzerBeanDescriptorByDisplayName(ref);

			if (descriptor == null) {
				throw new IllegalStateException("No such analyzer descriptor: " + ref);
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

					if (StringUtils.isNullOrEmpty(ref)) {
						throw new IllegalStateException("Analyzer input column ref cannot be null");
					}

					InputColumn<?> inputColumn = inputColumns.get(ref);
					if (inputColumn == null) {
						throw new IllegalStateException("No such input column: " + ref);
					}
					String name = inputType.getName();
					if (StringUtils.isNullOrEmpty(name)) {
						analyzerJobBuilder.addInputColumn(inputColumn);
					} else {
						ConfiguredPropertyDescriptor propertyDescriptor = analyzerJobBuilder.getDescriptor()
								.getConfiguredProperty(name);
						analyzerJobBuilder.addInputColumn(inputColumn, propertyDescriptor);
					}
				}
				applyProperties(analyzerJobBuilder, analyzerType.getProperties(), schemaNavigator);

				ref = analyzerType.getRequires();
				if (ref != null) {
					FilterOutcome requirement = outcomeMapping.get(ref);
					if (requirement == null) {
						throw new IllegalStateException("No such outcome id: " + ref);
					}
					analyzerJobBuilder.setRequirement(requirement);
				}
			} else if (descriptor.isExploringAnalyzer()) {
				@SuppressWarnings("unchecked")
				Class<? extends ExploringAnalyzer<?>> beanClass = (Class<? extends ExploringAnalyzer<?>>) descriptor
						.getBeanClass();
				ExploringAnalyzerJobBuilder<? extends ExploringAnalyzer<?>> analyzerJobBuilder = analysisJobBuilder
						.addExploringAnalyzer(beanClass);
				applyProperties(analyzerJobBuilder, analyzerType.getProperties(), schemaNavigator);

				if (analyzerType.getRequires() != null) {
					throw new IllegalStateException("Cannot add outcome requirement to exploring analyzer: "
							+ analyzerJobBuilder.getDescriptor().getDisplayName());
				}
			} else {
				throw new IllegalStateException("AnalyzerBeanDescriptor is neither row processing or exploring: "
						+ descriptor);
			}
		}

		return analysisJobBuilder;
	}

	private void registerInputColumn(Map<String, InputColumn<?>> inputColumns, String id, InputColumn<?> inputColumn) {
		if (inputColumns.containsKey(id)) {
			throw new IllegalStateException("Column id is not unique: " + id);
		}
		inputColumns.put(id, inputColumn);
	}

	private void applyProperties(AbstractBeanJobBuilder<? extends BeanDescriptor<?>, ?, ?> builder,
			ConfiguredPropertiesType configuredPropertiesType, SchemaNavigator schemaNavigator) {
		if (configuredPropertiesType != null) {
			List<Property> properties = configuredPropertiesType.getProperty();
			BeanDescriptor<?> descriptor = builder.getDescriptor();
			for (Property property : properties) {
				String name = property.getName();
				String stringValue = property.getValue();

				ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(name);

				if (configuredProperty == null) {
					throw new IllegalStateException("No such property: " + name);
				}

				Object value = StringConversionUtils.deserialize(stringValue, configuredProperty.getType(), schemaNavigator,
						_configuration.getReferenceDataCatalog());

				logger.debug("Setting property '{}' to {}", name, value);
				builder.setConfiguredProperty(configuredProperty, value);
			}
		}
	}
}
