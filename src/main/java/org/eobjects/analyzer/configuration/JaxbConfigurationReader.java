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
package org.eobjects.analyzer.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eobjects.analyzer.configuration.jaxb.AccessDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.BerkeleyDbStorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType.Package;
import org.eobjects.analyzer.configuration.jaxb.CombinedStorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.CompositeDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.Configuration;
import org.eobjects.analyzer.configuration.jaxb.ConfigurationMetadataType;
import org.eobjects.analyzer.configuration.jaxb.CsvDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.CustomElementType;
import org.eobjects.analyzer.configuration.jaxb.CustomElementType.Property;
import org.eobjects.analyzer.configuration.jaxb.DatastoreCatalogType;
import org.eobjects.analyzer.configuration.jaxb.DatastoreDictionaryType;
import org.eobjects.analyzer.configuration.jaxb.DatastoreSynonymCatalogType;
import org.eobjects.analyzer.configuration.jaxb.DbaseDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.ExcelDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.FixedWidthDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.H2StorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.HsqldbStorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.InMemoryStorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.JdbcDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.MultithreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.ObjectFactory;
import org.eobjects.analyzer.configuration.jaxb.OpenOfficeDatabaseDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType.Dictionaries;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType.StringPatterns;
import org.eobjects.analyzer.configuration.jaxb.ReferenceDataCatalogType.SynonymCatalogs;
import org.eobjects.analyzer.configuration.jaxb.RegexPatternType;
import org.eobjects.analyzer.configuration.jaxb.SasDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.SimplePatternType;
import org.eobjects.analyzer.configuration.jaxb.SinglethreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.StorageProviderType;
import org.eobjects.analyzer.configuration.jaxb.TextFileDictionaryType;
import org.eobjects.analyzer.configuration.jaxb.TextFileSynonymCatalogType;
import org.eobjects.analyzer.configuration.jaxb.ValueListDictionaryType;
import org.eobjects.analyzer.configuration.jaxb.XmlDatastoreType;
import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.connection.SasDatastore;
import org.eobjects.analyzer.connection.XmlDatastore;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;
import org.eobjects.analyzer.descriptors.SimpleComponentDescriptor;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.DatastoreSynonymCatalog;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceData;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.reference.TextFileDictionary;
import org.eobjects.analyzer.reference.TextFileSynonymCatalog;
import org.eobjects.analyzer.storage.BerkeleyDbStorageProvider;
import org.eobjects.analyzer.storage.CombinedStorageProvider;
import org.eobjects.analyzer.storage.H2StorageProvider;
import org.eobjects.analyzer.storage.HsqldbStorageProvider;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringConversionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration reader that uses the JAXB model to read XML file based
 * configurations for AnalyzerBeans.
 * 
 * @author Kasper Sørensen
 * @author Nancy Sharma
 */
public final class JaxbConfigurationReader implements ConfigurationReader<InputStream> {

	private static final Logger logger = LoggerFactory.getLogger(JaxbConfigurationReader.class);

	private final JAXBContext _jaxbContext;
	private final ConfigurationReaderInterceptor _interceptor;

	public JaxbConfigurationReader() {
		this(null);
	}

	public JaxbConfigurationReader(ConfigurationReaderInterceptor configurationReaderCallback) {
		if (configurationReaderCallback == null) {
			configurationReaderCallback = new DefaultConfigurationReaderInterceptor();
		}
		_interceptor = configurationReaderCallback;
		try {
			_jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AnalyzerBeansConfiguration read(InputStream input) {
		return create(input);
	}

	public AnalyzerBeansConfiguration create(File file) {
		try {
			return create(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public AnalyzerBeansConfiguration create(InputStream inputStream) {
		Configuration configuration = unmarshall(inputStream);
		return create(configuration);
	}

	public Configuration unmarshall(InputStream inputStream) {
		try {
			Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

			unmarshaller.setEventHandler(new JaxbValidationEventHandler());
			Configuration configuration = (Configuration) unmarshaller.unmarshal(inputStream);
			return configuration;
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
		DescriptorProvider descriptorProvider = createDescriptorProvider(configuration, taskRunner);
		DatastoreCatalog datastoreCatalog = createDatastoreCatalog(configuration.getDatastoreCatalog());
		ReferenceDataCatalog referenceDataCatalog = createReferenceDataCatalog(configuration.getReferenceDataCatalog(),
				datastoreCatalog);
		StorageProvider storageProvider = createStorageProvider(configuration.getStorageProvider(), datastoreCatalog,
				referenceDataCatalog);

		return new AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog, descriptorProvider, taskRunner,
				storageProvider);
	}

	private DescriptorProvider createDescriptorProvider(Configuration configuration, TaskRunner taskRunner) {
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
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
		return descriptorProvider;
	}

	private StorageProvider createStorageProvider(StorageProviderType storageProviderType,
			DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog) {
		if (storageProviderType == null) {
			// In-memory is the default storage provider
			return new InMemoryStorageProvider();
		}

		CombinedStorageProviderType combinedStorageProvider = storageProviderType.getCombined();
		if (combinedStorageProvider != null) {
			final StorageProviderType collectionsStorage = combinedStorageProvider.getCollectionsStorage();
			final StorageProviderType rowAnnotationStorage = combinedStorageProvider.getRowAnnotationStorage();

			final StorageProvider collectionsStorageProvider = createStorageProvider(collectionsStorage, datastoreCatalog,
					referenceDataCatalog);
			final StorageProvider rowAnnotationStorageProvider = createStorageProvider(rowAnnotationStorage,
					datastoreCatalog, referenceDataCatalog);

			return new CombinedStorageProvider(collectionsStorageProvider, rowAnnotationStorageProvider);
		}

		InMemoryStorageProviderType inMemoryStorageProvider = storageProviderType.getInMemory();
		if (inMemoryStorageProvider != null) {
			int maxRowsThreshold = inMemoryStorageProvider.getMaxRowsThreshold();
			return new InMemoryStorageProvider(maxRowsThreshold);
		}

		CustomElementType customStorageProvider = storageProviderType.getCustomStorageProvider();
		if (customStorageProvider != null) {
			return createCustomElement(customStorageProvider, StorageProvider.class, datastoreCatalog, referenceDataCatalog,
					true);
		}

		BerkeleyDbStorageProviderType berkeleyDbStorageProvider = storageProviderType.getBerkeleyDb();
		if (berkeleyDbStorageProvider != null) {
			File parentDirectory = new File(_interceptor.getTemporaryStorageDirectory());
			return new BerkeleyDbStorageProvider(parentDirectory);
		}

		HsqldbStorageProviderType hsqldbStorageProvider = storageProviderType.getHsqldb();
		if (hsqldbStorageProvider != null) {
			String directoryPath = hsqldbStorageProvider.getTempDirectory();
			if (directoryPath == null) {
				directoryPath = _interceptor.getTemporaryStorageDirectory();
			}

			directoryPath = _interceptor.createFilename(directoryPath);

			if (directoryPath == null) {
				return new HsqldbStorageProvider();
			} else {
				return new HsqldbStorageProvider(directoryPath);
			}
		}

		H2StorageProviderType h2StorageProvider = storageProviderType.getH2Database();
		if (h2StorageProvider != null) {
			String directoryPath = h2StorageProvider.getTempDirectory();
			if (directoryPath == null) {
				directoryPath = _interceptor.getTemporaryStorageDirectory();
			}

			directoryPath = _interceptor.createFilename(directoryPath);

			if (directoryPath == null) {
				return new H2StorageProvider();
			} else {
				return new H2StorageProvider(directoryPath);
			}
		}

		throw new IllegalStateException("Unknown storage provider type: " + storageProviderType);
	}

	private ReferenceDataCatalog createReferenceDataCatalog(ReferenceDataCatalogType referenceDataCatalog,
			DatastoreCatalog datastoreCatalog) {
		List<Dictionary> dictionaryList = new ArrayList<Dictionary>();
		List<SynonymCatalog> synonymCatalogList = new ArrayList<SynonymCatalog>();

		List<StringPattern> stringPatterns = new ArrayList<StringPattern>();

		if (referenceDataCatalog != null) {

			Dictionaries dictionaries = referenceDataCatalog.getDictionaries();
			if (dictionaries != null) {
				for (Object dictionaryType : dictionaries.getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary()) {
					if (dictionaryType instanceof DatastoreDictionaryType) {
						DatastoreDictionaryType ddt = (DatastoreDictionaryType) dictionaryType;

						String name = ddt.getName();
						checkName(name, Dictionary.class, dictionaryList);

						String dsName = ddt.getDatastoreName();
						String columnPath = ddt.getColumnPath();

						DatastoreDictionary dict = new DatastoreDictionary(name, dsName, columnPath);
						dict.setDescription(ddt.getDescription());

						dictionaryList.add(dict);
					} else if (dictionaryType instanceof TextFileDictionaryType) {
						TextFileDictionaryType tfdt = (TextFileDictionaryType) dictionaryType;

						String name = tfdt.getName();
						checkName(name, Dictionary.class, dictionaryList);

						String filename = _interceptor.createFilename(tfdt.getFilename());
						String encoding = tfdt.getEncoding();
						if (encoding == null) {
							encoding = FileHelper.UTF_8_ENCODING;
						}
						TextFileDictionary dict = new TextFileDictionary(name, filename, encoding);
						dict.setDescription(tfdt.getDescription());
						dictionaryList.add(dict);
					} else if (dictionaryType instanceof ValueListDictionaryType) {
						ValueListDictionaryType vldt = (ValueListDictionaryType) dictionaryType;

						String name = vldt.getName();
						checkName(name, Dictionary.class, dictionaryList);

						List<String> values = vldt.getValue();
						SimpleDictionary dict = new SimpleDictionary(name, values);
						dict.setDescription(vldt.getDescription());
						dictionaryList.add(dict);
					} else if (dictionaryType instanceof CustomElementType) {
						Dictionary customDictionary = createCustomElement((CustomElementType) dictionaryType,
								Dictionary.class, datastoreCatalog, null, false);
						checkName(customDictionary.getName(), Dictionary.class, dictionaryList);
						dictionaryList.add(customDictionary);
					} else {
						throw new IllegalStateException("Unsupported dictionary type: " + dictionaryType);
					}
				}
			}

			SynonymCatalogs synonymCatalogs = referenceDataCatalog.getSynonymCatalogs();
			if (synonymCatalogs != null) {
				for (Object synonymCatalogType : synonymCatalogs
						.getTextFileSynonymCatalogOrDatastoreSynonymCatalogOrCustomSynonymCatalog()) {
					if (synonymCatalogType instanceof TextFileSynonymCatalogType) {
						TextFileSynonymCatalogType tfsct = (TextFileSynonymCatalogType) synonymCatalogType;

						String name = tfsct.getName();
						checkName(name, SynonymCatalog.class, synonymCatalogList);

						String filename = _interceptor.createFilename(tfsct.getFilename());
						String encoding = tfsct.getEncoding();
						if (encoding == null) {
							encoding = FileHelper.UTF_8_ENCODING;
						}
						Boolean caseSensitive = tfsct.isCaseSensitive();
						if (caseSensitive == null) {
							caseSensitive = true;
						}
						TextFileSynonymCatalog sc = new TextFileSynonymCatalog(name, filename, caseSensitive.booleanValue(),
								encoding);
						sc.setDescription(tfsct.getDescription());
						synonymCatalogList.add(sc);
					} else if (synonymCatalogType instanceof CustomElementType) {
						SynonymCatalog customSynonymCatalog = createCustomElement((CustomElementType) synonymCatalogType,
								SynonymCatalog.class, datastoreCatalog, null, false);
						checkName(customSynonymCatalog.getName(), SynonymCatalog.class, synonymCatalogList);
						synonymCatalogList.add(customSynonymCatalog);
					} else if (synonymCatalogType instanceof DatastoreSynonymCatalogType) {
						DatastoreSynonymCatalogType datastoreSynonymCatalogType = (DatastoreSynonymCatalogType) synonymCatalogType;

						String name = datastoreSynonymCatalogType.getName();
						checkName(name, SynonymCatalog.class, synonymCatalogList);

						String dataStoreName = datastoreSynonymCatalogType.getDatastoreName();
						String masterTermColumnPath = datastoreSynonymCatalogType.getMasterTermColumnPath();

						String[] synonymColumnPaths = datastoreSynonymCatalogType.getSynonymColumnPath().toArray(
								new String[0]);
						DatastoreSynonymCatalog sc = new DatastoreSynonymCatalog(name, dataStoreName, masterTermColumnPath,
								synonymColumnPaths);
						sc.setDescription(datastoreSynonymCatalogType.getDescription());
						synonymCatalogList.add(sc);
					} else {
						throw new IllegalStateException("Unsupported synonym catalog type: " + synonymCatalogType);
					}
				}
			}

			StringPatterns stringPatternTypes = referenceDataCatalog.getStringPatterns();
			if (stringPatternTypes != null) {
				for (Object obj : stringPatternTypes.getRegexPatternOrSimplePattern()) {
					if (obj instanceof RegexPatternType) {
						RegexPatternType regexPatternType = (RegexPatternType) obj;

						String name = regexPatternType.getName();
						checkName(name, StringPattern.class, stringPatterns);

						String expression = regexPatternType.getExpression();
						boolean matchEntireString = regexPatternType.isMatchEntireString();
						RegexStringPattern sp = new RegexStringPattern(name, expression, matchEntireString);
						sp.setDescription(regexPatternType.getDescription());
						stringPatterns.add(sp);
					} else if (obj instanceof SimplePatternType) {
						SimplePatternType simplePatternType = (SimplePatternType) obj;

						String name = simplePatternType.getName();
						checkName(name, StringPattern.class, stringPatterns);

						String expression = simplePatternType.getExpression();
						SimpleStringPattern sp = new SimpleStringPattern(name, expression);
						sp.setDescription(simplePatternType.getDescription());
						stringPatterns.add(sp);
					} else {
						throw new IllegalStateException("Unsupported string pattern type: " + obj);
					}
				}
			}
		}

		return new ReferenceDataCatalogImpl(dictionaryList, synonymCatalogList, stringPatterns);
	}

	private DatastoreCatalog createDatastoreCatalog(DatastoreCatalogType datastoreCatalogType) {
		Map<String, Datastore> datastores = new HashMap<String, Datastore>();

		List<Object> datastoreTypes = datastoreCatalogType.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore();

		List<CsvDatastoreType> csvDatastores = CollectionUtils.filterOnClass(datastoreTypes, CsvDatastoreType.class);
		for (CsvDatastoreType csvDatastoreType : csvDatastores) {
			String name = csvDatastoreType.getName();
			checkName(name, Datastore.class, datastores);

			String filename = _interceptor.createFilename(csvDatastoreType.getFilename());
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

			Boolean failOnInconsistencies = csvDatastoreType.isFailOnInconsistencies();
			if (failOnInconsistencies == null) {
				failOnInconsistencies = true;
			}

			CsvDatastore ds = new CsvDatastore(name, filename, quoteChar, separatorChar, encoding, failOnInconsistencies);
			ds.setDescription(csvDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<FixedWidthDatastoreType> fixedWidthDatastores = CollectionUtils.filterOnClass(datastoreTypes,
				FixedWidthDatastoreType.class);
		for (FixedWidthDatastoreType fixedWidthDatastore : fixedWidthDatastores) {
			String name = fixedWidthDatastore.getName();
			checkName(name, Datastore.class, datastores);

			String filename = _interceptor.createFilename(fixedWidthDatastore.getFilename());
			String encoding = fixedWidthDatastore.getEncoding();
			if (!StringUtils.isNullOrEmpty(encoding)) {
				encoding = FileHelper.UTF_8_ENCODING;
			}

			Boolean failOnInconsistencies = fixedWidthDatastore.isFailOnInconsistencies();
			if (failOnInconsistencies == null) {
				failOnInconsistencies = true;
			}

			final FixedWidthDatastore ds;
			final Integer fixedValueWidth = fixedWidthDatastore.getFixedValueWidth();
			if (fixedValueWidth == null) {
				final List<Integer> valueWidthsBoxed = fixedWidthDatastore.getValueWidth();
				int[] valueWidths = new int[valueWidthsBoxed.size()];
				for (int i = 0; i < valueWidths.length; i++) {
					valueWidths[i] = valueWidthsBoxed.get(i).intValue();
				}
				ds = new FixedWidthDatastore(name, filename, encoding, valueWidths, failOnInconsistencies);
			} else {
				ds = new FixedWidthDatastore(name, filename, encoding, fixedValueWidth, failOnInconsistencies);
			}
			ds.setDescription(fixedWidthDatastore.getDescription());
			datastores.put(name, ds);
		}

		List<SasDatastoreType> sasDatastores = CollectionUtils.filterOnClass(datastoreTypes, SasDatastoreType.class);
		for (SasDatastoreType sasDatastoreType : sasDatastores) {
			final String name = sasDatastoreType.getName();
			checkName(name, Datastore.class, datastores);
			final File directory = new File(sasDatastoreType.getDirectory());
			final SasDatastore ds = new SasDatastore(name, directory);
			ds.setDescription(sasDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<AccessDatastoreType> accessDatastores = CollectionUtils
				.filterOnClass(datastoreTypes, AccessDatastoreType.class);
		for (AccessDatastoreType accessDatastoreType : accessDatastores) {
			String name = accessDatastoreType.getName();
			checkName(name, Datastore.class, datastores);
			String filename = _interceptor.createFilename(accessDatastoreType.getFilename());
			AccessDatastore ds = new AccessDatastore(name, filename);
			ds.setDescription(accessDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<XmlDatastoreType> xmlDatastores = CollectionUtils.filterOnClass(datastoreTypes, XmlDatastoreType.class);
		for (XmlDatastoreType xmlDatastoreType : xmlDatastores) {
			String name = xmlDatastoreType.getName();
			checkName(name, Datastore.class, datastores);
			String filename = _interceptor.createFilename(xmlDatastoreType.getFilename());
			XmlDatastore ds = new XmlDatastore(name, filename);
			ds.setDescription(xmlDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<ExcelDatastoreType> excelDatastores = CollectionUtils.filterOnClass(datastoreTypes, ExcelDatastoreType.class);
		for (ExcelDatastoreType excelDatastoreType : excelDatastores) {
			String name = excelDatastoreType.getName();
			checkName(name, Datastore.class, datastores);
			String filename = _interceptor.createFilename(excelDatastoreType.getFilename());
			ExcelDatastore ds = new ExcelDatastore(name, filename);
			ds.setDescription(excelDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<JdbcDatastoreType> jdbcDatastores = CollectionUtils.filterOnClass(datastoreTypes, JdbcDatastoreType.class);
		for (JdbcDatastoreType jdbcDatastoreType : jdbcDatastores) {
			String name = jdbcDatastoreType.getName();
			checkName(name, Datastore.class, datastores);

			JdbcDatastore ds;

			String datasourceJndiUrl = jdbcDatastoreType.getDatasourceJndiUrl();
			if (datasourceJndiUrl == null) {
				String url = jdbcDatastoreType.getUrl();
				String driver = jdbcDatastoreType.getDriver();
				String username = jdbcDatastoreType.getUsername();
				String password = jdbcDatastoreType.getPassword();
				ds = new JdbcDatastore(name, url, driver, username, password);
			} else {
				ds = new JdbcDatastore(name, datasourceJndiUrl);
			}

			ds.setDescription(jdbcDatastoreType.getDescription());

			datastores.put(name, ds);
		}

		List<DbaseDatastoreType> dbaseDatastores = CollectionUtils.filterOnClass(datastoreTypes, DbaseDatastoreType.class);
		for (DbaseDatastoreType dbaseDatastoreType : dbaseDatastores) {
			String name = dbaseDatastoreType.getName();
			checkName(name, Datastore.class, datastores);

			String filename = _interceptor.createFilename(dbaseDatastoreType.getFilename());
			DbaseDatastore ds = new DbaseDatastore(name, filename);

			ds.setDescription(dbaseDatastoreType.getDescription());

			datastores.put(name, ds);
		}

		List<OpenOfficeDatabaseDatastoreType> odbDatastores = CollectionUtils.filterOnClass(datastoreTypes,
				OpenOfficeDatabaseDatastoreType.class);
		for (OpenOfficeDatabaseDatastoreType odbDatastoreType : odbDatastores) {
			String name = odbDatastoreType.getName();
			checkName(name, Datastore.class, datastores);

			String filename = _interceptor.createFilename(odbDatastoreType.getFilename());
			OdbDatastore ds = new OdbDatastore(name, filename);
			ds.setDescription(odbDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		List<CustomElementType> customDatastores = CollectionUtils.filterOnClass(datastoreTypes, CustomElementType.class);
		for (CustomElementType customElementType : customDatastores) {
			Datastore ds = createCustomElement(customElementType, Datastore.class, null, null, true);
			String name = ds.getName();
			checkName(name, Datastore.class, datastores);
			datastores.put(name, ds);
		}

		List<CompositeDatastoreType> compositeDatastores = CollectionUtils.filterOnClass(datastoreTypes,
				CompositeDatastoreType.class);
		for (CompositeDatastoreType compositeDatastoreType : compositeDatastores) {
			String name = compositeDatastoreType.getName();
			checkName(name, Datastore.class, datastores);

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

			CompositeDatastore ds = new CompositeDatastore(name, childDatastores);
			ds.setDescription(compositeDatastoreType.getDescription());
			datastores.put(name, ds);
		}

		DatastoreCatalogImpl result = new DatastoreCatalogImpl(datastores.values());
		return result;
	}

	/**
	 * Checks if a string is a valid name of a component.
	 * 
	 * @param name
	 *            the name to be validated
	 * @param type
	 *            the type of component (used for error messages)
	 * @param previousEntries
	 *            the previous entries of that component type (for uniqueness
	 *            check)
	 * @throws IllegalStateException
	 *             if the name is invalid
	 */
	private static void checkName(final String name, Class<?> type, final Map<String, ?> previousEntries)
			throws IllegalStateException {
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalStateException(type.getSimpleName() + " name cannot be null");
		}
		if (previousEntries.containsKey(name)) {
			throw new IllegalStateException(type.getSimpleName() + " name is not unique: " + name);
		}
	}

	/**
	 * Checks if a string is a valid name of a component.
	 * 
	 * @param name
	 *            the name to be validated
	 * @param type
	 *            the type of component (used for error messages)
	 * @param previousEntries
	 *            the previous entries of that component type (for uniqueness
	 *            check)
	 * @throws IllegalStateException
	 *             if the name is invalid
	 */
	private static void checkName(String name, Class<?> type, List<? extends ReferenceData> previousEntries)
			throws IllegalStateException {
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalStateException(type.getSimpleName() + " name cannot be null");
		}
		for (ReferenceData referenceData : previousEntries) {
			if (name.equals(referenceData.getName())) {
				throw new IllegalStateException(type.getSimpleName() + " name is not unique: " + name);
			}
		}
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
			taskRunner = createCustomElement(customTaskrunner, TaskRunner.class, null, null, true);
		} else {
			// default task runner type is multithreaded
			taskRunner = new MultiThreadedTaskRunner();
		}

		return taskRunner;
	}

	/**
	 * Creates a custom component based on an element which specified just a
	 * class name and an optional set of properties.
	 * 
	 * @param <E>
	 * @param customElementType
	 *            the JAXB custom element type
	 * @param expectedClazz
	 *            an expected class or interface that the component should honor
	 * @param datastoreCatalog
	 *            the datastore catalog (for lookups/injections)
	 * @param referenceDataCatalog
	 *            the reference data catalog (for lookups/injections)
	 * @param initialize
	 *            whether or not to call any initialize methods on the component
	 *            (reference data should not be initialized, while eg. custom
	 *            task runners support this.
	 * @return the custom component
	 */
	@SuppressWarnings("unchecked")
	private <E> E createCustomElement(CustomElementType customElementType, Class<E> expectedClazz,
			DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog, boolean initialize) {
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

		E result = (E) ReflectionUtils.newInstance(foundClass);

		SimpleComponentDescriptor<?> descriptor = SimpleComponentDescriptor.create(foundClass);

		List<Property> propertyTypes = customElementType.getProperty();
		if (propertyTypes != null) {
			for (Property property : propertyTypes) {
				String propertyName = property.getName();
				String propertyValue = property.getValue();

				ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(propertyName);
				if (configuredProperty == null) {
					logger.warn("Missing configured property name: {}", propertyName);
					if (logger.isInfoEnabled()) {
						Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
						for (ConfiguredPropertyDescriptor configuredPropertyDescriptor : configuredProperties) {
							logger.info("Available configured property name: {}, {}",
									configuredPropertyDescriptor.getName(), configuredPropertyDescriptor.getType());
						}
					}
					throw new IllegalStateException("No such property in " + foundClass.getName() + ": " + propertyName);
				}

				Object configuredValue = StringConversionUtils.deserialize(propertyValue, configuredProperty.getType(),
						null, null, datastoreCatalog);

				configuredProperty.setValue(result, configuredValue);
			}
		}

		if (initialize) {
			Set<InitializeMethodDescriptor> initializeMethods = descriptor.getInitializeMethods();
			for (InitializeMethodDescriptor initializeMethod : initializeMethods) {
				initializeMethod.initialize(result, datastoreCatalog, referenceDataCatalog);
			}
		}

		return result;
	}
}
