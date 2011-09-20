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
package org.eobjects.analyzer.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.SourceColumnMapping;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.ELInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.ExplorerBeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExplorerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergeInputBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.jaxb.AnalysisType;
import org.eobjects.analyzer.job.jaxb.AnalyzerType;
import org.eobjects.analyzer.job.jaxb.ColumnType;
import org.eobjects.analyzer.job.jaxb.ColumnsType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType.Property;
import org.eobjects.analyzer.job.jaxb.DataContextType;
import org.eobjects.analyzer.job.jaxb.ExplorerType;
import org.eobjects.analyzer.job.jaxb.FilterType;
import org.eobjects.analyzer.job.jaxb.InputType;
import org.eobjects.analyzer.job.jaxb.Job;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.job.jaxb.MergedOutcomeType;
import org.eobjects.analyzer.job.jaxb.ObjectFactory;
import org.eobjects.analyzer.job.jaxb.OutcomeType;
import org.eobjects.analyzer.job.jaxb.OutputType;
import org.eobjects.analyzer.job.jaxb.SourceType;
import org.eobjects.analyzer.job.jaxb.TransformationType;
import org.eobjects.analyzer.job.jaxb.TransformerDescriptorType;
import org.eobjects.analyzer.job.jaxb.TransformerType;
import org.eobjects.analyzer.job.jaxb.VariableType;
import org.eobjects.analyzer.job.jaxb.VariablesType;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.StringConversionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbJobReader implements JobReader<InputStream> {

	private static final Logger logger = LoggerFactory
			.getLogger(JaxbJobReader.class);

	private final JAXBContext _jaxbContext;
	private final AnalyzerBeansConfiguration _configuration;

	public JaxbJobReader(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
		try {
			_jaxbContext = JAXBContext.newInstance(ObjectFactory.class
					.getPackage().getName(), ObjectFactory.class.getClassLoader());
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AnalysisJob read(InputStream inputStream)
			throws NoSuchDatastoreException {
		AnalysisJobBuilder ajb = create(inputStream);
		return ajb.toAnalysisJob();
	}

	@Override
	public AnalysisJob read(InputStream inputStream,
			SourceColumnMapping sourceColumnMapping) {
		AnalysisJobBuilder ajb = create(inputStream, sourceColumnMapping);
		return ajb.toAnalysisJob();
	}

	public AnalysisJobMetadata readMetadata(File file) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(
					new FileInputStream(file));
			return readMetadata(inputStream);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public AnalysisJobMetadata readMetadata(InputStream inputStream) {
		Job job = unmarshallJob(inputStream);
		return readMetadata(job);
	}

	public AnalysisJobMetadata readMetadata(Job job) {
		final String datastoreName = job.getSource().getDataContext().getRef();
		final List<String> sourceColumnPaths = getSourceColumnPaths(job);
		final Map<String, String> variables = getVariables(job);

		final String jobName;
		final String jobVersion;
		final String jobDescription;
		final String author;
		final Date createdDate;
		final Date updatedDate;

		JobMetadataType metadata = job.getJobMetadata();
		if (metadata == null) {
			jobName = null;
			jobVersion = null;
			jobDescription = null;
			author = null;
			createdDate = null;
			updatedDate = null;
		} else {
			jobName = metadata.getJobName();
			jobVersion = metadata.getJobVersion();
			jobDescription = metadata.getJobDescription();
			author = metadata.getAuthor();

			final XMLGregorianCalendar createdDateCal = metadata
					.getCreatedDate();

			if (createdDateCal == null) {
				createdDate = null;
			} else {
				createdDate = createdDateCal.toGregorianCalendar().getTime();
			}

			final XMLGregorianCalendar updatedDateCal = metadata
					.getUpdatedDate();

			if (updatedDateCal == null) {
				updatedDate = null;
			} else {
				updatedDate = updatedDateCal.toGregorianCalendar().getTime();
			}
		}

		return new ImmutableAnalysisJobMetadata(jobName, jobVersion,
				jobDescription, author, createdDate, updatedDate,
				datastoreName, sourceColumnPaths, variables);
	}

	public Map<String, String> getVariables(Job job) {
		final Map<String, String> result = new HashMap<String, String>();

		VariablesType variablesType = job.getSource().getVariables();
		if (variablesType != null) {
			List<VariableType> variables = variablesType.getVariable();
			for (VariableType variableType : variables) {
				String id = variableType.getId();
				String value = variableType.getValue();
				result.put(id, value);
			}
		}

		return result;
	}

	public List<String> getSourceColumnPaths(Job job) {
		final List<String> paths;

		final ColumnsType columnsType = job.getSource().getColumns();
		if (columnsType != null) {
			final List<ColumnType> columns = columnsType.getColumn();
			paths = new ArrayList<String>(columns.size());
			for (ColumnType columnType : columns) {
				final String path = columnType.getPath();
				paths.add(path);
			}
		} else {
			paths = Collections.emptyList();
		}
		return paths;
	}

	public AnalysisJobBuilder create(File file) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(
					new FileInputStream(file));
			return create(inputStream);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalysisJobBuilder create(InputStream inputStream)
			throws NoSuchDatastoreException {
		return create(unmarshallJob(inputStream), null, null);
	}

	public AnalysisJobBuilder create(InputStream inputStream,
			SourceColumnMapping sourceColumnMapping)
			throws NoSuchDatastoreException {
		return create(inputStream, sourceColumnMapping, null);
	}

	public AnalysisJobBuilder create(InputStream inputStream,
			SourceColumnMapping sourceColumnMapping,
			Map<String, String> variableOverrides)
			throws NoSuchDatastoreException {
		return create(unmarshallJob(inputStream), sourceColumnMapping,
				variableOverrides);
	}

	public AnalysisJobBuilder create(InputStream inputStream,
			Map<String, String> variableOverrides)
			throws NoSuchDatastoreException {
		return create(unmarshallJob(inputStream), null, variableOverrides);
	}

	private Job unmarshallJob(InputStream inputStream) {
		try {
			Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

			unmarshaller.setEventHandler(new JaxbValidationEventHandler());
			Job job = (Job) unmarshaller.unmarshal(inputStream);
			return job;
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalysisJobBuilder create(Job job) {
		return create(job, null, null);
	}

	public AnalysisJobBuilder create(Job job,
			SourceColumnMapping sourceColumnMapping,
			Map<String, String> variableOverrides)
			throws NoSuchDatastoreException {
		if (job == null) {
			throw new IllegalArgumentException("Job cannot be null");
		}
		if (sourceColumnMapping != null && !sourceColumnMapping.isSatisfied()) {
			throw new IllegalArgumentException(
					"Source column mapping is not satisfied!");
		}

		final Map<String, String> variables = getVariables(job);
		if (variableOverrides != null) {
			final Set<Entry<String, String>> entrySet = variableOverrides
					.entrySet();
			for (Entry<String, String> entry : entrySet) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				String originalValue = variables.put(key, value);
				logger.info(
						"Overriding variable: {}={} (original value was {})",
						new Object[] { key, value, originalValue });
			}
		}

		final JobMetadataType metadata = job.getJobMetadata();
		if (metadata != null) {
			logger.info("Job name: {}", metadata.getJobName());
			logger.info("Job version: {}", metadata.getJobVersion());
			logger.info("Job description: {}", metadata.getJobDescription());
			logger.info("Author: {}", metadata.getAuthor());
			logger.info("Created date: {}", metadata.getCreatedDate());
			logger.info("Updated date: {}", metadata.getUpdatedDate());
		}

		final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(
				_configuration);

		String ref;
		final Datastore datastore;
		final DataContextProvider dataContextProvider;

		final SourceType source = job.getSource();

		if (sourceColumnMapping == null) {
			// use automatic mapping if no explicit mapping is supplied
			DataContextType dataContext = source.getDataContext();
			ref = dataContext.getRef();
			if (StringUtils.isNullOrEmpty(ref)) {
				throw new IllegalStateException("Datastore ref cannot be null");
			}

			datastore = _configuration.getDatastoreCatalog().getDatastore(ref);
			if (datastore == null) {
				throw new NoSuchDatastoreException(ref);
			}
			dataContextProvider = datastore.getDataContextProvider();

			List<String> sourceColumnPaths = getSourceColumnPaths(job);
			sourceColumnMapping = new SourceColumnMapping(sourceColumnPaths);
			sourceColumnMapping.autoMap(datastore);
		} else {
			datastore = sourceColumnMapping.getDatastore();
			dataContextProvider = datastore.getDataContextProvider();
		}

		// map column id's to input columns

		analysisJobBuilder.setDatastore(datastore);

		final Map<String, InputColumn<?>> inputColumns = new HashMap<String, InputColumn<?>>();

		final ColumnsType columnsType = source.getColumns();
		if (columnsType != null) {
			List<ColumnType> columns = columnsType.getColumn();
			for (ColumnType column : columns) {
				String path = column.getPath();
				if (StringUtils.isNullOrEmpty(path)) {
					throw new IllegalStateException(
							"Column path cannot be null");
				}
				Column physicalColumn = sourceColumnMapping.getColumn(path);
				if (physicalColumn == null) {
					throw new IllegalStateException("No such column: " + path);
				}
				MetaModelInputColumn inputColumn = new MetaModelInputColumn(
						physicalColumn);
				String id = column.getId();
				if (StringUtils.isNullOrEmpty(id)) {
					throw new IllegalStateException(
							"Source column id cannot be null");
				}

				registerInputColumn(inputColumns, id, inputColumn);
				analysisJobBuilder.addSourceColumn(inputColumn);
			}
		}

		final SchemaNavigator schemaNavigator = dataContextProvider
				.getSchemaNavigator();
		final Map<String, Outcome> outcomeMapping = new HashMap<String, Outcome>();

		final TransformationType transformation = job.getTransformation();
		if (transformation != null) {

			final List<Object> transformersAndFilters = transformation
					.getTransformerOrFilterOrMergedOutcome();

			final Map<TransformerType, TransformerJobBuilder<?>> transformerJobBuilders = new HashMap<TransformerType, TransformerJobBuilder<?>>();
			final Map<FilterType, FilterJobBuilder<?, ?>> filterJobBuilders = new HashMap<FilterType, FilterJobBuilder<?, ?>>();

			// iterate to initialize transformers
			for (Object o : transformersAndFilters) {
				if (o instanceof TransformerType) {
					TransformerType transformer = (TransformerType) o;
					ref = transformer.getDescriptor().getRef();
					if (StringUtils.isNullOrEmpty(ref)) {
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

					transformerJobBuilder.setName(transformer.getName());

					applyProperties(transformerJobBuilder,
							transformer.getProperties(), schemaNavigator,
							variables);

					transformerJobBuilders.put(transformer,
							transformerJobBuilder);
				}
			}

			// iterate again to set up transformed column dependencies
			List<TransformerType> unconfiguredTransformerKeys = new LinkedList<TransformerType>(
					transformerJobBuilders.keySet());
			while (!unconfiguredTransformerKeys.isEmpty()) {
				boolean progress = false;
				for (Iterator<TransformerType> it = unconfiguredTransformerKeys
						.iterator(); it.hasNext();) {
					boolean configurable = true;

					TransformerType unconfiguredTransformerKey = it.next();
					List<InputType> input = unconfiguredTransformerKey
							.getInput();
					for (InputType inputType : input) {
						ref = inputType.getRef();
						if (StringUtils.isNullOrEmpty(ref)) {
							String value = inputType.getValue();
							if (StringUtils.isNullOrEmpty(value)) {
								throw new IllegalStateException(
										"Transformer input column ref & value cannot be null");
							}
						} else if (!inputColumns.containsKey(ref)) {
							configurable = false;
							break;
						}
					}

					if (configurable) {
						progress = true;
						TransformerJobBuilder<?> transformerJobBuilder = transformerJobBuilders
								.get(unconfiguredTransformerKey);

						for (InputType inputType : input) {
							String name = inputType.getName();
							ref = inputType.getRef();
							InputColumn<?> inputColumn;
							if (StringUtils.isNullOrEmpty(ref)) {
								inputColumn = createExpressionBasedInputColumn(inputType);
							} else {
								inputColumn = inputColumns.get(ref);
							}
							if (StringUtils.isNullOrEmpty(name)) {
								transformerJobBuilder
										.addInputColumn(inputColumn);
							} else {
								ConfiguredPropertyDescriptor configuredProperty = transformerJobBuilder
										.getDescriptor().getConfiguredProperty(
												name);
								transformerJobBuilder.addInputColumn(
										inputColumn, configuredProperty);
							}
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
							if (!StringUtils.isNullOrEmpty(name)) {
								o2.setName(name);
							}
							String id = o1.getId();
							if (StringUtils.isNullOrEmpty(id)) {
								throw new IllegalStateException(
										"Transformer output column id cannot be null");
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
						TransformerDescriptorType descriptor = transformerType
								.getDescriptor();
						sb.append(descriptor.getRef());
						sb.append("(input: ");

						List<InputType> input = transformerType.getInput();
						int i = 0;
						for (InputType inputType : input) {
							if (i != 0) {
								sb.append(", ");
							}
							ref = inputType.getRef();
							if (StringUtils.isNullOrEmpty(ref)) {
								sb.append("value=" + inputType.getValue());
							} else {
								sb.append("ref=" + ref);
							}
							i++;
						}
						sb.append(")");
					}
					throw new IllegalStateException(
							"Could not connect column dependencies for transformers: "
									+ sb.toString());
				}
			}

			// iterate again to initialize all Filters and collect all outcomes
			for (Object o : transformersAndFilters) {
				if (o instanceof FilterType) {
					FilterType filter = (FilterType) o;

					ref = filter.getDescriptor().getRef();

					if (StringUtils.isNullOrEmpty(ref)) {
						throw new IllegalStateException(
								"Filter descriptor ref cannot be null");
					}
					FilterBeanDescriptor<?, ?> filterBeanDescriptor = _configuration
							.getDescriptorProvider()
							.getFilterBeanDescriptorByDisplayName(ref);
					if (filterBeanDescriptor == null) {
						throw new IllegalStateException(
								"No such filter descriptor: " + ref);
					}
					FilterJobBuilder<?, ?> filterJobBuilder = analysisJobBuilder
							.addFilter(filterBeanDescriptor);

					filterJobBuilder.setName(filter.getName());

					List<InputType> input = filter.getInput();
					for (InputType inputType : input) {
						ref = inputType.getRef();

						InputColumn<?> inputColumn;
						if (StringUtils.isNullOrEmpty(ref)) {
							inputColumn = createExpressionBasedInputColumn(inputType);
						} else {
							inputColumn = inputColumns.get(ref);
							if (inputColumn == null) {
								throw new IllegalStateException(
										"No such input column: " + ref);
							}
						}

						String name = inputType.getName();
						if (StringUtils.isNullOrEmpty(name)) {
							filterJobBuilder.addInputColumn(inputColumn);
						} else {
							ConfiguredPropertyDescriptor propertyDescriptor = filterJobBuilder
									.getDescriptor()
									.getConfiguredProperty(name);
							filterJobBuilder.addInputColumn(inputColumn,
									propertyDescriptor);
						}
					}

					applyProperties(filterJobBuilder, filter.getProperties(),
							schemaNavigator, variables);

					filterJobBuilders.put(filter, filterJobBuilder);

					List<OutcomeType> outcomeTypes = filter.getOutcome();
					for (OutcomeType outcomeType : outcomeTypes) {
						String categoryName = outcomeType.getCategory();
						Enum<?> category = filterJobBuilder.getDescriptor()
								.getOutcomeCategoryByName(categoryName);
						if (category == null) {
							throw new IllegalStateException(
									"No such outcome category name: "
											+ categoryName
											+ " (in "
											+ filterJobBuilder.getDescriptor()
													.getDisplayName());
						}

						String id = outcomeType.getId();
						if (StringUtils.isNullOrEmpty(id)) {
							throw new IllegalStateException(
									"Outcome id cannot be null");
						}
						if (outcomeMapping.containsKey(id)) {
							throw new IllegalStateException("Outcome id '" + id
									+ "' is not unique");
						}
						outcomeMapping.put(id,
								filterJobBuilder.getOutcome(category));
					}
				}

				if (o instanceof MergedOutcomeType) {
					MergedOutcomeType mergedOutcomeType = (MergedOutcomeType) o;
					String id = mergedOutcomeType.getId();
					if (StringUtils.isNullOrEmpty(id)) {
						throw new IllegalStateException(
								"Outcome id cannot be null");
					}
					if (outcomeMapping.containsKey(id)) {
						throw new IllegalStateException("Outcome id '" + id
								+ "' is not unique");
					}

					MergedOutcomeJobBuilder mojb = analysisJobBuilder
							.addMergedOutcomeJobBuilder();
					mojb.setName(mergedOutcomeType.getName());
					outcomeMapping.put(id, new LazyMergedOutcome(mojb));
				}
			}

			// iterate again to initialize all MergedOutcomes and collect all
			// outcomes
			for (Object o : transformersAndFilters) {
				if (o instanceof MergedOutcomeType) {
					MergedOutcomeType mergedOutcomeType = (MergedOutcomeType) o;
					String id = mergedOutcomeType.getId();

					// we added this element during the previous iteration
					LazyMergedOutcome outcome = (LazyMergedOutcome) outcomeMapping
							.get(id);
					MergedOutcomeJobBuilder builder = outcome.getBuilder();

					// map the input requirements and columns
					List<MergedOutcomeType.Outcome> mergedOutcomes = ((MergedOutcomeType) o)
							.getOutcome();
					for (MergedOutcomeType.Outcome mergedOutcome : mergedOutcomes) {
						ref = mergedOutcome.getRef();
						if (StringUtils.isNullOrEmpty(ref)) {
							throw new IllegalStateException(
									"Merged outcome ref cannot be null");
						}
						Outcome outcomeToMerge = outcomeMapping.get(ref);
						MergeInputBuilder mergedOutcomeBuilder = builder
								.addMergedOutcome(outcomeToMerge);

						List<InputType> inputs = mergedOutcome.getInput();
						for (InputType inputType : inputs) {
							ref = inputType.getRef();
							InputColumn<?> inputColumn;
							if (StringUtils.isNullOrEmpty(ref)) {
								inputColumn = createExpressionBasedInputColumn(inputType);
							} else {
								inputColumn = inputColumns.get(ref);
								if (inputColumn == null) {
									throw new IllegalStateException(
											"No such input column: " + ref);
								}
							}
							mergedOutcomeBuilder.addInputColumn(inputColumn);
						}
					}

					// map the output columns
					List<MutableInputColumn<?>> outputColumns = builder
							.getOutputColumns();

					List<OutputType> output = ((MergedOutcomeType) o)
							.getOutput();

					assert output.size() == outputColumns.size();

					for (int i = 0; i < output.size(); i++) {
						OutputType outputType = output.get(i);
						MutableInputColumn<?> outputColumn = outputColumns
								.get(i);
						id = outputType.getId();
						String name = outputType.getName();

						if (!StringUtils.isNullOrEmpty(name)) {
							outputColumn.setName(name);
						}

						registerInputColumn(inputColumns, id, outputColumn);
					}
				}
			}

			// iterate again to set up filter outcome dependencies
			for (Object o : transformersAndFilters) {
				if (o instanceof TransformerType) {
					ref = ((TransformerType) o).getRequires();
					if (ref != null) {
						TransformerJobBuilder<?> builder = transformerJobBuilders
								.get(o);
						Outcome requirement = outcomeMapping.get(ref);
						if (requirement == null) {
							throw new IllegalStateException(
									"No such outcome id: " + ref);
						}
						builder.setRequirement(requirement);
					}
				} else if (o instanceof FilterType) {
					ref = ((FilterType) o).getRequires();
					if (ref != null) {
						FilterJobBuilder<?, ?> builder = filterJobBuilders
								.get(o);
						Outcome requirement = outcomeMapping.get(ref);
						if (requirement == null) {
							throw new IllegalStateException(
									"No such outcome id: " + ref);
						}
						builder.setRequirement(requirement);
					}
				} else if (o instanceof MergedOutcomeType) {
					// do nothing
				} else {
					throw new IllegalStateException(
							"Unexpected transformation child element: " + o);
				}
			}
		}

		AnalysisType analysis = job.getAnalysis();

		List<AnalyzerType> analyzers = CollectionUtils2.filterOnClass(
				analysis.getAnalyzerOrExplorer(), AnalyzerType.class);
		for (AnalyzerType analyzerType : analyzers) {
			ref = analyzerType.getDescriptor().getRef();
			if (StringUtils.isNullOrEmpty(ref)) {
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

			Class<? extends Analyzer<?>> beanClass = descriptor
					.getComponentClass();
			AnalyzerJobBuilder<? extends Analyzer<?>> analyzerJobBuilder = analysisJobBuilder
					.addAnalyzer(beanClass);
			analyzerJobBuilder.setName(analyzerType.getName());

			List<InputType> input = analyzerType.getInput();
			for (InputType inputType : input) {
				ref = inputType.getRef();

				InputColumn<?> inputColumn;
				if (StringUtils.isNullOrEmpty(ref)) {
					inputColumn = createExpressionBasedInputColumn(inputType);
				} else {
					inputColumn = inputColumns.get(ref);
					if (inputColumn == null) {
						throw new IllegalStateException(
								"No such input column: " + ref);
					}
				}

				String name = inputType.getName();
				if (StringUtils.isNullOrEmpty(name)) {
					analyzerJobBuilder.addInputColumn(inputColumn);
				} else {
					ConfiguredPropertyDescriptor propertyDescriptor = analyzerJobBuilder
							.getDescriptor().getConfiguredProperty(name);
					if (propertyDescriptor == null) {
						throw new IllegalStateException(
								"No such input property name: " + name);
					}
					analyzerJobBuilder.addInputColumn(inputColumn,
							propertyDescriptor);
				}
			}
			applyProperties(analyzerJobBuilder, analyzerType.getProperties(),
					schemaNavigator, variables);

			ref = analyzerType.getRequires();
			if (ref != null) {
				Outcome requirement = outcomeMapping.get(ref);
				if (requirement == null) {
					throw new IllegalStateException("No such outcome id: "
							+ ref);
				}
				analyzerJobBuilder.setRequirement(requirement);
			}

		}

		List<ExplorerType> explorers = CollectionUtils2.filterOnClass(
				analysis.getAnalyzerOrExplorer(), ExplorerType.class);
		for (ExplorerType explorerType : explorers) {

			ref = explorerType.getDescriptor().getRef();
			if (StringUtils.isNullOrEmpty(ref)) {
				throw new IllegalStateException(
						"Explorer descriptor ref cannot be null");
			}

			ExplorerBeanDescriptor<?> descriptor = _configuration
					.getDescriptorProvider()
					.getExplorerBeanDescriptorByDisplayName(ref);

			if (descriptor == null) {
				throw new IllegalStateException("No such explorer descriptor: "
						+ ref);
			}

			Class<? extends Explorer<?>> beanClass = descriptor
					.getComponentClass();

			ExplorerJobBuilder<? extends Explorer<?>> explorerJobBuilder = analysisJobBuilder
					.addExplorer(beanClass);
			explorerJobBuilder.setName(explorerType.getName());
			applyProperties(explorerJobBuilder, explorerType.getProperties(),
					schemaNavigator, variables);
		}

		dataContextProvider.close();

		return analysisJobBuilder;
	}

	private InputColumn<?> createExpressionBasedInputColumn(InputType inputType) {
		String expression = inputType.getValue();
		if (StringUtils.isNullOrEmpty(expression)) {
			throw new IllegalStateException(
					"Input ref & value cannot both be null");
		}
		if (expression.indexOf("#{") == -1) {
			return new ConstantInputColumn(expression);
		} else {
			return new ELInputColumn(expression);
		}
	}

	private void registerInputColumn(Map<String, InputColumn<?>> inputColumns,
			String id, InputColumn<?> inputColumn) {
		if (StringUtils.isNullOrEmpty(id)) {
			throw new IllegalStateException("Column id cannot be null");
		}
		if (inputColumns.containsKey(id)) {
			throw new IllegalStateException("Column id is not unique: " + id);
		}
		inputColumns.put(id, inputColumn);
	}

	private void applyProperties(
			AbstractBeanJobBuilder<? extends BeanDescriptor<?>, ?, ?> builder,
			ConfiguredPropertiesType configuredPropertiesType,
			SchemaNavigator schemaNavigator, Map<String, String> variables) {
		if (configuredPropertiesType != null) {
			List<Property> properties = configuredPropertiesType.getProperty();
			BeanDescriptor<?> descriptor = builder.getDescriptor();
			for (Property property : properties) {
				final String name = property.getName();
				final ConfiguredPropertyDescriptor configuredProperty = descriptor
						.getConfiguredProperty(name);

				if (configuredProperty == null) {
					throw new IllegalStateException("No such property: " + name);
				}

				String stringValue = property.getValue();
				if (stringValue == null) {
					String variableRef = property.getRef();
					if (variableRef == null) {
						throw new IllegalStateException(
								"Neither value nor ref was specified for property: "
										+ name);
					}

					stringValue = variables.get(variableRef);

					if (stringValue == null) {
						throw new IllegalStateException("No such variable: "
								+ variableRef);
					}
				}

				Object value = StringConversionUtils.deserialize(stringValue,
						configuredProperty.getType(), schemaNavigator,
						_configuration.getReferenceDataCatalog(),
						_configuration.getDatastoreCatalog());

				logger.debug("Setting property '{}' to {}", name, value);
				builder.setConfiguredProperty(configuredProperty, value);
			}
		}
	}
}
