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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.jaxb.AnalysisType;
import org.eobjects.analyzer.job.jaxb.AnalyzerDescriptorType;
import org.eobjects.analyzer.job.jaxb.AnalyzerType;
import org.eobjects.analyzer.job.jaxb.ColumnType;
import org.eobjects.analyzer.job.jaxb.ColumnsType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType;
import org.eobjects.analyzer.job.jaxb.ConfiguredPropertiesType.Property;
import org.eobjects.analyzer.job.jaxb.DataContextType;
import org.eobjects.analyzer.job.jaxb.FilterDescriptorType;
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
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.convert.StringConverter;
import org.eobjects.metamodel.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbJobWriter implements JobWriter<OutputStream> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbJobWriter.class);

    private final AnalyzerBeansConfiguration _configuration;
    private final JAXBContext _jaxbContext;
    private final JaxbJobMetadataFactory _jobMetadataFactory;

    public JaxbJobWriter(AnalyzerBeansConfiguration configuration, JaxbJobMetadataFactory jobMetadataFactory) {
        _configuration = configuration;
        _jobMetadataFactory = jobMetadataFactory;
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public JaxbJobWriter(AnalyzerBeansConfiguration configuration) {
        this(configuration, new JaxbJobMetadataFactoryImpl());
    }

    @Override
    public void write(final AnalysisJob analysisJob, final OutputStream outputStream) {
        logger.debug("write({},{}}", analysisJob, outputStream);
        final Job jobType = new Job();

        try {
            JobMetadataType jobMetadata = _jobMetadataFactory.create(analysisJob);
            jobType.setJobMetadata(jobMetadata);
        } catch (Exception e) {
            logger.warn("Exception occurred while creating job metadata", e);
        }

        final SourceType sourceType = new SourceType();
        sourceType.setColumns(new ColumnsType());
        jobType.setSource(sourceType);

        final Datastore datastore = analysisJob.getDatastore();
        final DataContextType dataContextType = new DataContextType();
        if (datastore == null) {
            logger.warn("No datastore specified for analysis job: {}", analysisJob);
        } else {
            dataContextType.setRef(datastore.getName());
        }
        sourceType.setDataContext(dataContextType);

        // mappings for lookup of ID's
        final Map<InputColumn<?>, String> columnMappings = new LinkedHashMap<InputColumn<?>, String>();
        final Map<Outcome, String> outcomeMappings = new LinkedHashMap<Outcome, String>();

        // mappings for lookup of component's elements
        final Map<TransformerJob, TransformerType> transformerMappings = new LinkedHashMap<TransformerJob, TransformerType>();
        final Map<FilterJob, FilterType> filterMappings = new LinkedHashMap<FilterJob, FilterType>();
        final Map<AnalyzerJob, AnalyzerType> analyzerMappings = new LinkedHashMap<AnalyzerJob, AnalyzerType>();
        final Map<MergedOutcomeJob, MergedOutcomeType> mergedOutcomeMappings = new LinkedHashMap<MergedOutcomeJob, MergedOutcomeType>();

        // register all source columns
        final Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
        for (InputColumn<?> inputColumn : sourceColumns) {
            final ColumnType jaxbColumn = new ColumnType();
            final Column physicalColumn = inputColumn.getPhysicalColumn();
            jaxbColumn.setPath(physicalColumn.getQualifiedLabel());
            jaxbColumn.setId(getId(inputColumn, columnMappings));

            final org.eobjects.metamodel.schema.ColumnType columnType = physicalColumn.getType();
            if (columnType != null) {
                jaxbColumn.setType(columnType.toString());
            }

            sourceType.getColumns().getColumn().add(jaxbColumn);
        }

        // adds all components to the job and their corresponding mappings
        addComponents(jobType, analysisJob, transformerMappings, filterMappings, analyzerMappings,
                mergedOutcomeMappings);

        // add all transformed columns to their originating components and the
        // mappings
        addTransformedColumns(columnMappings, transformerMappings, mergedOutcomeMappings);

        // register all requirements
        addRequirements(outcomeMappings, transformerMappings, filterMappings, mergedOutcomeMappings, analyzerMappings,
                columnMappings);

        addConfiguration(analysisJob, transformerMappings, filterMappings, analyzerMappings, columnMappings);

        try {
            final Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            marshaller.marshal(jobType, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addConfiguration(final AnalysisJob analysisJob,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings, final Map<AnalyzerJob, AnalyzerType> analyzerMappings,
            final Map<InputColumn<?>, String> columnMappings) {

        final StringConverter stringConverter = new StringConverter(_configuration.getInjectionManager(analysisJob));

        // configure transformers
        for (Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            TransformerJob job = entry.getKey();
            TransformerType elementType = entry.getValue();
            BeanConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType
                    .setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter));
        }

        // configure filters
        for (Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            FilterJob job = entry.getKey();
            FilterType elementType = entry.getValue();
            BeanConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType
                    .setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter));
        }

        // configure analyzers
        for (Entry<AnalyzerJob, AnalyzerType> entry : analyzerMappings.entrySet()) {
            AnalyzerJob job = entry.getKey();
            AnalyzerType elementType = entry.getValue();
            BeanConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType
                    .setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter));
        }
    }

    private List<InputType> createInputConfiguration(final BeanConfiguration configuration,
            Set<ConfiguredPropertyDescriptor> configuredProperties, final Map<InputColumn<?>, String> columnMappings,
            final StringConverter stringConverter) {

        // sort the properties in order to make the result deterministic
        configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(configuredProperties);

        final int numInputProperties = configuredProperties.size();
        final List<InputType> result = new ArrayList<InputType>();
        for (ConfiguredPropertyDescriptor property : configuredProperties) {
            if (property.isInputColumn()) {
                final Object value = configuration.getProperty(property);
                if (value != null) {
                    final InputColumn<?>[] columns;
                    if (property.isArray()) {
                        columns = (InputColumn<?>[]) value;
                    } else {
                        columns = new InputColumn<?>[1];
                        columns[0] = (InputColumn<?>) value;
                    }

                    for (final InputColumn<?> inputColumn : columns) {
                        if (inputColumn != null) {
                            final InputType inputType = new InputType();
                            if (inputColumn instanceof ExpressionBasedInputColumn) {
                                ExpressionBasedInputColumn<?> expressionBasedInputColumn = (ExpressionBasedInputColumn<?>) inputColumn;
                                Object columnValue = expressionBasedInputColumn.getExpression();
                                inputType
                                        .setValue(stringConverter.serialize(columnValue, property.getCustomConverter()));
                            } else {
                                inputType.setRef(getId(inputColumn, columnMappings));
                            }
                            if (numInputProperties != 1) {
                                inputType.setName(property.getName());
                            }
                            result.add(inputType);
                        }
                    }
                }
            }
        }
        return result;
    }

    private ConfiguredPropertiesType createPropertyConfiguration(final BeanConfiguration configuration,
            Set<ConfiguredPropertyDescriptor> configuredProperties, StringConverter stringConverter) {

        // sort the properties in order to make the result deterministic
        configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(configuredProperties);

        List<Property> result = new ArrayList<Property>();
        for (ConfiguredPropertyDescriptor property : configuredProperties) {
            if (!property.isInputColumn()) {
                Object value = configuration.getProperty(property);
                String stringValue = stringConverter.serialize(value, property.getCustomConverter());

                final Property propertyType = new Property();
                propertyType.setName(property.getName());

                if (stringValue != null && stringValue.indexOf('\n') != -1) {
                    // multi-line values are put as simple content of the
                    // property
                    propertyType.setValue(stringValue);
                } else {
                    // single-line values are preferred as an attribute for
                    // backwards compatibility
                    propertyType.setValueAttribute(stringValue);
                }
                result.add(propertyType);
            }
        }
        ConfiguredPropertiesType configuredPropertiesType = new ConfiguredPropertiesType();
        configuredPropertiesType.getProperty().addAll(result);
        return configuredPropertiesType;
    }

    private void addTransformedColumns(final Map<InputColumn<?>, String> columnMappings,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<MergedOutcomeJob, MergedOutcomeType> mergedOutcomeMappings) {
        // register all transformed columns
        for (Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            final TransformerJob transformerJob = entry.getKey();
            final TransformerType transformerType = entry.getValue();
            final InputColumn<?>[] columns = transformerJob.getOutput();
            for (InputColumn<?> inputColumn : columns) {
                final String id = getId(inputColumn, columnMappings);
                final OutputType outputType = new OutputType();
                outputType.setId(id);
                outputType.setName(inputColumn.getName());
                if (inputColumn instanceof MutableInputColumn) {
                    final boolean hidden = ((MutableInputColumn<?>) inputColumn).isHidden();
                    if (hidden) {
                        outputType.setHidden(hidden);
                    }
                }

                transformerType.getOutput().add(outputType);
            }
        }

        // register all merged columns
        for (Entry<MergedOutcomeJob, MergedOutcomeType> entry : mergedOutcomeMappings.entrySet()) {
            MergedOutcomeJob mergedOutcomeJob = entry.getKey();
            MergedOutcomeType mergedOutcomeType = entry.getValue();
            InputColumn<?>[] columns = mergedOutcomeJob.getOutput();
            for (InputColumn<?> inputColumn : columns) {
                String id = getId(inputColumn, columnMappings);
                OutputType outputType = new OutputType();
                outputType.setId(id);
                outputType.setName(inputColumn.getName());
                mergedOutcomeType.getOutput().add(outputType);
            }
        }
    }

    private void addRequirements(final Map<Outcome, String> outcomeMappings,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings,
            final Map<MergedOutcomeJob, MergedOutcomeType> mergedOutcomeMappings,
            final Map<AnalyzerJob, AnalyzerType> analyzerMappings, final Map<InputColumn<?>, String> columnMappings) {

        // add requirements based on all transformer requirements
        for (Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            TransformerJob job = entry.getKey();
            Outcome[] requirements = job.getRequirements();
            if (requirements != null && requirements.length != 0) {
                assert requirements.length == 1;
                String id = getId(requirements[0], outcomeMappings, true);
                entry.getValue().setRequires(id);
            }
        }

        // add requirements based on all filter requirements
        for (Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            FilterJob job = entry.getKey();
            Outcome[] requirements = job.getRequirements();
            if (requirements != null && requirements.length != 0) {
                assert requirements.length == 1;
                String id = getId(requirements[0], outcomeMappings, true);
                entry.getValue().setRequires(id);
            }
        }

        // add requirements based on all merged outcome requirements
        for (Entry<MergedOutcomeJob, MergedOutcomeType> entry : mergedOutcomeMappings.entrySet()) {
            MergedOutcomeJob job = entry.getKey();
            MergedOutcomeType mergedOutcomeType = entry.getValue();
            MergeInput[] mergeInputs = job.getMergeInputs();
            for (MergeInput mergeInput : mergeInputs) {
                Outcome requirement = mergeInput.getOutcome();
                String id = getId(requirement, outcomeMappings, true);
                MergedOutcomeType.Outcome mergeInputType = new MergedOutcomeType.Outcome();
                mergeInputType.setRef(id);
                InputColumn<?>[] columns = mergeInput.getInputColumns();

                for (InputColumn<?> inputColumn : columns) {
                    InputType inputType = new InputType();
                    inputType.setRef(getId(inputColumn, columnMappings));
                    mergeInputType.getInput().add(inputType);
                }

                mergedOutcomeType.getOutcome().add(mergeInputType);
            }

            // add the single outcome element of this merged outcome to the
            // mappings
            mergedOutcomeType.setId(getId(job.getOutcome(), outcomeMappings, true));
        }

        // add requirements based on all analyzer requirements
        for (Entry<AnalyzerJob, AnalyzerType> entry : analyzerMappings.entrySet()) {
            AnalyzerJob job = entry.getKey();
            Outcome[] requirements = job.getRequirements();
            if (requirements != null && requirements.length != 0) {
                assert requirements.length == 1;
                String id = getId(requirements[0], outcomeMappings, true);
                entry.getValue().setRequires(id);
            }
        }

        // add outcome elements only for those filter requirements that
        // have been mapped
        for (Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            FilterJob job = entry.getKey();
            FilterType filterType = entry.getValue();
            FilterOutcome[] outcomes = job.getOutcomes();
            for (FilterOutcome outcome : outcomes) {
                // note that we DONT use the getId(...) method here
                String id = getId(outcome, outcomeMappings, false);
                // only the outcome element if it is being mapped
                if (id != null) {
                    OutcomeType outcomeType = new OutcomeType();
                    outcomeType.setCategory(outcome.getCategory().name());
                    outcomeType.setId(id);
                    filterType.getOutcome().add(outcomeType);
                }
            }
        }
    }

    private String getId(Outcome requirement, Map<Outcome, String> outcomeMappings, boolean create) {
        String id = outcomeMappings.get(requirement);
        if (id == null) {
            if (create) {
                id = "outcome_" + outcomeMappings.size();
                outcomeMappings.put(requirement, id);
            }
        }
        return id;
    }

    private void addComponents(final Job jobType, final AnalysisJob analysisJob,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings, final Map<AnalyzerJob, AnalyzerType> analyzerMappings,
            final Map<MergedOutcomeJob, MergedOutcomeType> mergedOutcomeMappings) {
        final TransformationType transformationType = new TransformationType();
        jobType.setTransformation(transformationType);

        final AnalysisType analysisType = new AnalysisType();
        jobType.setAnalysis(analysisType);

        // add all transformers to the transformation element
        final Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
        for (TransformerJob transformerJob : transformerJobs) {
            TransformerType transformerType = new TransformerType();
            transformerType.setName(transformerJob.getName());
            TransformerDescriptorType descriptorType = new TransformerDescriptorType();
            descriptorType.setRef(transformerJob.getDescriptor().getDisplayName());
            transformerType.setDescriptor(descriptorType);
            transformationType.getTransformerOrFilterOrMergedOutcome().add(transformerType);
            transformerMappings.put(transformerJob, transformerType);
        }

        // add all filters to the transformation element
        Collection<FilterJob> filterJobs = analysisJob.getFilterJobs();
        for (FilterJob filterJob : filterJobs) {
            FilterType filterType = new FilterType();
            filterType.setName(filterJob.getName());
            FilterDescriptorType descriptorType = new FilterDescriptorType();
            descriptorType.setRef(filterJob.getDescriptor().getDisplayName());
            filterType.setDescriptor(descriptorType);
            transformationType.getTransformerOrFilterOrMergedOutcome().add(filterType);
            filterMappings.put(filterJob, filterType);
        }

        // add all merged outcomes to the transformation element
        Collection<MergedOutcomeJob> mergedOutcomeJobs = analysisJob.getMergedOutcomeJobs();
        for (MergedOutcomeJob mergedOutcomeJob : mergedOutcomeJobs) {
            MergedOutcomeType mergedOutcomeType = new MergedOutcomeType();
            mergedOutcomeType.setName(mergedOutcomeJob.getName());
            transformationType.getTransformerOrFilterOrMergedOutcome().add(mergedOutcomeType);
            mergedOutcomeMappings.put(mergedOutcomeJob, mergedOutcomeType);
        }

        // add all analyzers to the analysis element
        Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            AnalyzerType analyzerType = new AnalyzerType();
            analyzerType.setName(analyzerJob.getName());
            AnalyzerDescriptorType descriptorType = new AnalyzerDescriptorType();
            descriptorType.setRef(analyzerJob.getDescriptor().getDisplayName());
            analyzerType.setDescriptor(descriptorType);
            analysisType.getAnalyzer().add(analyzerType);
            analyzerMappings.put(analyzerJob, analyzerType);
        }
    }

    private static String getId(InputColumn<?> inputColumn, Map<InputColumn<?>, String> columnMappings) {
        if (inputColumn == null) {
            throw new IllegalArgumentException("InputColumn cannot be null");
        }
        String id = columnMappings.get(inputColumn);
        if (id == null) {
            id = "col_" + columnMappings.size();
            columnMappings.put(inputColumn, id);
        }
        return id;
    }
}
