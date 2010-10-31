package org.eobjects.analyzer.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eobjects.analyzer.configuration.jaxb.AccessDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType.Package;
import org.eobjects.analyzer.configuration.jaxb.CompositeDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.Configuration;
import org.eobjects.analyzer.configuration.jaxb.ConfigurationMetadataType;
import org.eobjects.analyzer.configuration.jaxb.CsvDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.CustomElementType;
import org.eobjects.analyzer.configuration.jaxb.CustomElementType.Property;
import org.eobjects.analyzer.configuration.jaxb.DatastoreCatalogType;
import org.eobjects.analyzer.configuration.jaxb.DatastoreDictionaryType;
import org.eobjects.analyzer.configuration.jaxb.ExcelDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.JdbcDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.MultithreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.ObjectFactory;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType.Dictionaries;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType.SynonymCatalogs;
import org.eobjects.analyzer.configuration.jaxb.SinglethreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.TextFileDictionaryType;
import org.eobjects.analyzer.configuration.jaxb.TextFileSynonymCatalogType;
import org.eobjects.analyzer.configuration.jaxb.ValueListDictionaryType;
import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;
import org.eobjects.analyzer.job.JaxbJobFactory;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.HsqldbCollectionProvider;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.reference.TextBasedDictionary;
import org.eobjects.analyzer.reference.TextBasedSynonymCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringConversionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.util.FileHelper;

public final class JaxbConfigurationFactory {

	private static final Logger logger = LoggerFactory.getLogger(JaxbJobFactory.class);

	private JAXBContext _jaxbContext;

	public JaxbConfigurationFactory() {
		try {
			_jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	public AnalyzerBeansConfiguration create(File file) {
		try {
			return create(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalyzerBeansConfiguration create(InputStream inputStream) {
		try {
			Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

			unmarshaller.setEventHandler(new JaxbValidationEventHandler());
			Configuration configuration = (Configuration) unmarshaller.unmarshal(inputStream);
			return create(configuration);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalyzerBeansConfiguration create(Configuration configuration) {
		ConfigurationMetadataType metadata = configuration.getConfigurationMetadata();
		if (metadata != null) {
			logger.info("Configuration name: {}", metadata.getConfigurationName());
			logger.info("Configuration version: {}", metadata.getConfigurationVersion());
			logger.info("Configuration description: {}", metadata.getConfigurationDescription());
			logger.info("Author: {}", metadata.getAuthor());
			logger.info("Created date: {}", metadata.getCreatedDate());
			logger.info("Updated date: {}", metadata.getUpdatedDate());
		}

		TaskRunner taskRunner = createTaskRunner(configuration);
		DatastoreCatalog datastoreCatalog = createDatastoreCatalog(configuration.getDatastoreCatalog());

		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
		ClasspathScannerType classpathScanner = configuration.getClasspathScanner();
		if (classpathScanner != null) {
			List<Package> packages = classpathScanner.getPackage();
			for (Package pkg : packages) {
				String packageName = pkg.getValue();
				if (packageName != null) {
					packageName = packageName.trim();
					Boolean recursive = pkg.isRecursive();
					if (recursive == null) {
						recursive = true;
					}
					descriptorProvider.scanPackage(packageName, recursive);
				}
			}
		}

		ReferenceDataCatalog referenceDataCatalog = createReferenceDataCatalog(configuration.getReferenceDataCatalog(),
				datastoreCatalog);

		// TODO: Make this components configurable as well
		CollectionProvider collectionProvider = new HsqldbCollectionProvider();
		return new AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog, descriptorProvider, taskRunner,
				collectionProvider);
	}

	private ReferenceDataCatalog createReferenceDataCatalog(ReferenceDataCatalogType referenceDataCatalog,
			DatastoreCatalog datastoreCatalog) {
		List<Dictionary> dictionaryList = new ArrayList<Dictionary>();
		List<SynonymCatalog> synonymCatalogList = new ArrayList<SynonymCatalog>();

		if (referenceDataCatalog != null) {

			Dictionaries dictionaries = referenceDataCatalog.getDictionaries();
			if (dictionaries != null) {
				for (Object dictionaryType : dictionaries.getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary()) {
					if (dictionaryType instanceof DatastoreDictionaryType) {
						DatastoreDictionaryType ddt = (DatastoreDictionaryType) dictionaryType;

						String name = ddt.getName();
						String dsName = ddt.getDatastoreName();
						String columnPath = ddt.getColumnPath();

						dictionaryList.add(new DatastoreDictionary(name, datastoreCatalog, dsName, columnPath));
					} else if (dictionaryType instanceof TextFileDictionaryType) {
						TextFileDictionaryType tfdt = (TextFileDictionaryType) dictionaryType;
						String name = tfdt.getName();
						String filename = tfdt.getFilename();
						String encoding = tfdt.getEncoding();
						if (encoding == null) {
							encoding = FileHelper.UTF_8_ENCODING;
						}
						dictionaryList.add(new TextBasedDictionary(name, filename, encoding));
					} else if (dictionaryType instanceof ValueListDictionaryType) {
						ValueListDictionaryType vldt = (ValueListDictionaryType) dictionaryType;
						String name = vldt.getName();
						List<String> values = vldt.getValue();
						dictionaryList.add(new SimpleDictionary(name, values));
					} else if (dictionaryType instanceof CustomElementType) {
						Dictionary customDictionary = createCustomElement((CustomElementType) dictionaryType,
								Dictionary.class);
						dictionaryList.add(customDictionary);
					} else {
						throw new IllegalStateException("Unsupported dictionary type: " + dictionaryType);
					}
				}
			}

			SynonymCatalogs synonymCatalogs = referenceDataCatalog.getSynonymCatalogs();
			if (synonymCatalogs != null) {
				for (Object synonymCatalogType : synonymCatalogs.getTextFileSynonymCatalogOrCustomSynonymCatalog()) {
					if (synonymCatalogType instanceof TextFileSynonymCatalogType) {
						TextFileSynonymCatalogType tfsct = (TextFileSynonymCatalogType) synonymCatalogType;
						String name = tfsct.getName();
						String filename = tfsct.getFilename();
						String encoding = tfsct.getEncoding();
						if (encoding == null) {
							encoding = FileHelper.UTF_8_ENCODING;
						}
						Boolean caseSensitive = tfsct.isCaseSensitive();
						if (caseSensitive == null) {
							caseSensitive = true;
						}
						synonymCatalogList.add(new TextBasedSynonymCatalog(name, filename, caseSensitive.booleanValue(),
								encoding));
					} else if (synonymCatalogType instanceof CustomElementType) {
						SynonymCatalog customSynonymCatalog = createCustomElement((CustomElementType) synonymCatalogType,
								SynonymCatalog.class);
						synonymCatalogList.add(customSynonymCatalog);
					} else {
						throw new IllegalStateException("Unsupported synonym catalog type: " + synonymCatalogType);
					}
				}
			}
		}

		return new ReferenceDataCatalogImpl(dictionaryList, synonymCatalogList);
	}

	private DatastoreCatalog createDatastoreCatalog(DatastoreCatalogType datastoreCatalogType) {
		Map<String, Datastore> datastores = new HashMap<String, Datastore>();

		List<Object> datastoreTypes = datastoreCatalogType.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore();

		List<CsvDatastoreType> csvDatastores = CollectionUtils.filterOnClass(datastoreTypes, CsvDatastoreType.class);
		for (CsvDatastoreType csvDatastoreType : csvDatastores) {
			String name = csvDatastoreType.getName();
			if (StringUtils.isNullOrEmpty(name)) {
				throw new IllegalStateException("Datastore name cannot be null");
			}

			if (datastores.containsKey(name)) {
				throw new IllegalStateException("Datastore name is not unique: " + name);
			}

			String filename = csvDatastoreType.getFilename();
			String quoteCharString = csvDatastoreType.getQuoteChar();
			Character quoteChar = null;
			String separatorCharString = csvDatastoreType.getSeparatorChar();
			Character separatorChar = null;

			if (!StringUtils.isNullOrEmpty(separatorCharString)) {
				assert separatorCharString.length() == 1;
				separatorChar = separatorCharString.charAt(0);
			}

			if (!StringUtils.isNullOrEmpty(quoteCharString)) {
				assert quoteCharString.length() == 1;
				quoteChar = quoteCharString.charAt(0);
			}

			String encoding = csvDatastoreType.getEncoding();
			if (!StringUtils.isNullOrEmpty(encoding)) {
				encoding = FileHelper.UTF_8_ENCODING;
			}

			datastores.put(name, new CsvDatastore(name, filename, quoteChar, separatorChar, encoding));
		}

		List<AccessDatastoreType> accessDatastores = CollectionUtils
				.filterOnClass(datastoreTypes, AccessDatastoreType.class);
		for (AccessDatastoreType accessDatastoreType : accessDatastores) {
			String name = accessDatastoreType.getName();
			if (StringUtils.isNullOrEmpty(name)) {
				throw new IllegalStateException("Datastore name cannot be null");
			}

			if (datastores.containsKey(name)) {
				throw new IllegalStateException("Datastore name is not unique: " + name);
			}
			String filename = accessDatastoreType.getFilename();
			datastores.put(name, new AccessDatastore(name, filename));
		}

		List<ExcelDatastoreType> excelDatastores = CollectionUtils.filterOnClass(datastoreTypes, ExcelDatastoreType.class);
		for (ExcelDatastoreType excelDatastoreType : excelDatastores) {
			String name = excelDatastoreType.getName();
			if (StringUtils.isNullOrEmpty(name)) {
				throw new IllegalStateException("Datastore name cannot be null");
			}

			if (datastores.containsKey(name)) {
				throw new IllegalStateException("Datastore name is not unique: " + name);
			}
			String filename = excelDatastoreType.getFilename();
			datastores.put(name, new ExcelDatastore(name, filename));
		}

		List<JdbcDatastoreType> jdbcDatastores = CollectionUtils.filterOnClass(datastoreTypes, JdbcDatastoreType.class);
		for (JdbcDatastoreType jdbcDatastoreType : jdbcDatastores) {
			String name = jdbcDatastoreType.getName();
			if (StringUtils.isNullOrEmpty(name)) {
				throw new IllegalStateException("Datastore name cannot be null");
			}

			if (datastores.containsKey(name)) {
				throw new IllegalStateException("Datastore name is not unique: " + name);
			}

			JdbcDatastore datastore;

			String datasourceJndiUrl = jdbcDatastoreType.getDatasourceJndiUrl();
			if (datasourceJndiUrl == null) {
				String url = jdbcDatastoreType.getUrl();
				String driver = jdbcDatastoreType.getDriver();
				String username = jdbcDatastoreType.getUsername();
				String password = jdbcDatastoreType.getPassword();
				datastore = new JdbcDatastore(name, url, driver, username, password);
			} else {
				datastore = new JdbcDatastore(name, datasourceJndiUrl);
			}

			datastores.put(name, datastore);
		}

		List<CompositeDatastoreType> compositeDatastores = CollectionUtils.filterOnClass(datastoreTypes,
				CompositeDatastoreType.class);
		for (CompositeDatastoreType compositeDatastoreType : compositeDatastores) {
			String name = compositeDatastoreType.getName();
			if (StringUtils.isNullOrEmpty(name)) {
				throw new IllegalStateException("Datastore name cannot be null");
			}

			if (datastores.containsKey(name)) {
				throw new IllegalStateException("Datastore name is not unique: " + name);
			}

			List<String> datastoreNames = compositeDatastoreType.getDatastoreName();
			List<Datastore> childDatastores = new ArrayList<Datastore>(datastoreNames.size());
			for (String datastoreName : datastoreNames) {
				Datastore datastore = datastores.get(datastoreName);
				if (datastore == null) {
					throw new IllegalStateException("No such datastore: " + datastoreName
							+ " (found in composite datastore: " + name + ")");
				}
				childDatastores.add(datastore);
			}

			datastores.put(name, new CompositeDatastore(name, childDatastores));
		}

		DatastoreCatalogImpl result = new DatastoreCatalogImpl(datastores.values());
		return result;
	}

	private TaskRunner createTaskRunner(Configuration configuration) {
		SinglethreadedTaskrunnerType singlethreadedTaskrunner = configuration.getSinglethreadedTaskrunner();
		MultithreadedTaskrunnerType multithreadedTaskrunner = configuration.getMultithreadedTaskrunner();
		CustomElementType customTaskrunner = configuration.getCustomTaskrunner();

		TaskRunner taskRunner;
		if (singlethreadedTaskrunner != null) {
			taskRunner = new SingleThreadedTaskRunner();
		} else if (multithreadedTaskrunner != null) {
			Short maxThreads = multithreadedTaskrunner.getMaxThreads();
			if (maxThreads != null) {
				taskRunner = new MultiThreadedTaskRunner(maxThreads.intValue());
			} else {
				taskRunner = new MultiThreadedTaskRunner();
			}
		} else if (customTaskrunner != null) {
			taskRunner = createCustomElement(customTaskrunner, TaskRunner.class);
		} else {
			// default task runner type is multithreaded
			taskRunner = new MultiThreadedTaskRunner();
		}

		return taskRunner;
	}

	@SuppressWarnings("unchecked")
	private <E> E createCustomElement(CustomElementType customElementType, Class<E> expectedClazz) {
		E result = null;
		Class<?> foundClass;
		String className = customElementType.getClassName();

		assert className != null;
		try {
			foundClass = Class.forName(className);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		if (!ReflectionUtils.is(foundClass, expectedClazz)) {
			throw new IllegalStateException(className + " is not a valid " + expectedClazz);
		}
		try {
			result = (E) foundClass.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		CustomComponentDescriptor<E> descriptor = new CustomComponentDescriptor<E>((Class<E>) foundClass);

		List<Property> propertyTypes = customElementType.getProperty();
		if (propertyTypes != null) {
			for (Property property : propertyTypes) {
				String propertyName = property.getName();
				String propertyValue = property.getValue();

				ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(propertyName);
				if (configuredProperty == null) {
					throw new IllegalStateException("No such property in " + foundClass.getName() + ": " + propertyName);
				}

				Object configuredValue = StringConversionUtils.deserialize(propertyValue, configuredProperty.getType(),
						null, null);

				configuredProperty.setValue(result, configuredValue);
			}
		}

		List<InitializeMethodDescriptor> initializeMethods = descriptor.getInitializeMethods();
		for (InitializeMethodDescriptor initializeMethod : initializeMethods) {
			initializeMethod.initialize(result);
		}

		return result;
	}
}
