package org.eobjects.analyzer.engine;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.TableAnalysisResult;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public final class AnalysisRunner {

	private static final Log _log = LogFactory.getLog(AnalysisRunner.class);
	private ProvidedCollectionProvider providedCollectionProvider = new ProvidedCollectionProvider();
	private List<AnalysisJob> _jobs = new LinkedList<AnalysisJob>();
	private List<AnalysisResult> _results = new ArrayList<AnalysisResult>();
	private AnnotationScanner _scanner;

	public void execute(DataContext dataContext) {
		if (_scanner == null) {
			_scanner = new AnnotationScanner();
		}
		Map<Class<?>, AnalyzerBeanDescriptor> descriptors = _scanner
				.getDescriptors();
		List<AnalysisJob> explorerJobs = new LinkedList<AnalysisJob>();
		List<AnalysisJob> rowProcessingJobs = new LinkedList<AnalysisJob>();

		// Categorise jobs corresponding to their execution type
		for (AnalysisJob job : _jobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptors.get(analyzerClass);
			if (descriptor == null) {
				descriptor = new AnalyzerBeanDescriptor(analyzerClass);
				_scanner.putDescriptor(analyzerClass, descriptor);
			}
			if (descriptor.isExploringExecutionType()) {
				explorerJobs.add(job);
			} else if (descriptor.isRowProcessingExecutionType()) {
				rowProcessingJobs.add(job);
			} else {
				throw new UnsupportedOperationException(
						"Analysis execution type "
								+ descriptor.getExecutionType()
								+ " is not supported by AnalysisRunner");
			}
		}

		// Initialise all jobs before we run anything
		List<Object[]> analyzersAndDescriptors = new LinkedList<Object[]>();
		for (AnalysisJob job : explorerJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptors.get(analyzerClass);
			Object analyzer = initializeExploringAnalyzer(job, descriptor,
					dataContext);
			analyzersAndDescriptors.add(new Object[] { analyzer, descriptor });
		}
		Map<Table, SharedQueryRunnerGroup> sharedQueryAnalyzerGroups = new HashMap<Table, SharedQueryRunnerGroup>();
		for (AnalysisJob job : rowProcessingJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptors.get(analyzerClass);
			initializeRowProcessingAnalyzers(job, descriptor,
					sharedQueryAnalyzerGroups, dataContext);
		}

		// Execute explorer jobs
		for (Object[] analyzerAndDescriptor : analyzersAndDescriptors) {
			Object analyzer = analyzerAndDescriptor[0];
			AnalyzerBeanDescriptor descriptor = (AnalyzerBeanDescriptor) analyzerAndDescriptor[1];
			if (_log.isInfoEnabled()) {
				_log.info("Running exploring analyzer: " + analyzer);
			}
			runExplorerAnalyzer(analyzer, descriptor, dataContext);

			_results.addAll(ResultDescriptor.getResults(analyzer, descriptor));
		}

		// Execute row processing jobs (which have been grouped)
		for (SharedQueryRunnerGroup group : sharedQueryAnalyzerGroups.values()) {
			if (_log.isInfoEnabled()) {
				_log.info("Running shared query group: " + group);
			}
			group.run(dataContext);
			List<TableAnalysisResult> results = group.getResults();
			for (TableAnalysisResult analysisResult : results) {
				if (analysisResult.getTable() == null) {
					analysisResult.setTable(group.getTable());
				}
			}

			_results.addAll(results);
		}
	}

	public List<AnalysisResult> getResults() {
		return Collections.unmodifiableList(_results);
	}

	private void initializeRowProcessingAnalyzers(AnalysisJob job,
			AnalyzerBeanDescriptor analyzerBeanDescriptor,
			Map<Table, SharedQueryRunnerGroup> sharedTableQueries,
			DataContext dataContext) {
		try {
			// Create list of all ConfiguredDescriptors except the ones who
			// require a column array.
			List<ConfiguredDescriptor> configuredDescriptorsExceptColumns = new LinkedList<ConfiguredDescriptor>(
					analyzerBeanDescriptor.getConfiguredDescriptors());
			for (Iterator<ConfiguredDescriptor> it = configuredDescriptorsExceptColumns
					.iterator(); it.hasNext();) {
				ConfiguredDescriptor configuredDescriptor = it.next();
				if (configuredDescriptor.isColumn()
						&& configuredDescriptor.isArray()) {
					it.remove();
				}
			}

			// Create an analyzer instance for each table that is represented in
			// the column array based ConfiguredDescriptors
			Map<String, String[]> columnProperties = job.getColumnProperties();
			Set<Entry<String, String[]>> columnPropertyEntries = columnProperties
					.entrySet();
			for (Entry<String, String[]> entry : columnPropertyEntries) {
				String configuredName = entry.getKey();
				ConfiguredDescriptor configuredDescriptor = analyzerBeanDescriptor
						.getConfiguredDescriptor(configuredName);
				if (configuredDescriptor == null) {
					throw new IllegalStateException(
							"Analyzer class '"
									+ analyzerBeanDescriptor.getAnalyzerClass()
									+ "' does not specify @Configured field or method with name: "
									+ configuredName);
				}
				if (configuredDescriptor.isArray()) {
					String[] columnNames = entry.getValue();
					Column[] columns = convertToColumns(dataContext,
							columnNames);
					Table[] tables = MetaModelHelper.getTables(columns);
					for (Table table : tables) {
						Column[] columnsForAnalyzer = MetaModelHelper
								.getTableColumns(table, columns);

						Object analyzerBean = analyzerBeanDescriptor
								.getAnalyzerClass().newInstance();

						// First assign the columns to the analyzer
						configuredDescriptor.assignValue(analyzerBean,
								columnsForAnalyzer);

						// Then initialize the rest of the properties
						initializeProperties(analyzerBean, job, dataContext,
								analyzerBeanDescriptor);
						InitializeDescriptor.initialize(analyzerBean,
								analyzerBeanDescriptor);

						// Add the analysis to a shared query group
						SharedQueryRunnerGroup group = sharedTableQueries
								.get(table);
						if (group == null) {
							group = new SharedQueryRunnerGroup(table);
							sharedTableQueries.put(table, group);
						}
						group.registerAnalyzer(analyzerBean,
								analyzerBeanDescriptor, columnsForAnalyzer);
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not initialize analyzer based on job: " + job, e);
		}
	}

	private void runExplorerAnalyzer(Object analyzer,
			AnalyzerBeanDescriptor analyzerDescriptor, DataContext dataContext) {
		List<RunDescriptor> runDescriptors = analyzerDescriptor
				.getRunDescriptors();
		for (RunDescriptor runDescriptor : runDescriptors) {
			runDescriptor.explore(analyzer, dataContext);
		}
	}

	private void initializeConfiguredProperties(Object analyzer,
			AnalysisJob job, List<ConfiguredDescriptor> configuredDescriptors,
			DataContext dataContext) {
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			Object configuredValue = null;
			String configuredName = configuredDescriptor.getName();
			if (configuredDescriptor.isBoolean()) {
				configuredValue = job.getBooleanProperties()
						.get(configuredName);
			} else if (configuredDescriptor.isInteger()) {
				configuredValue = job.getIntegerProperties()
						.get(configuredName);
			} else if (configuredDescriptor.isLong()) {
				configuredValue = job.getLongProperties().get(configuredName);
			} else if (configuredDescriptor.isDouble()) {
				configuredValue = job.getDoubleProperties().get(configuredName);
			} else if (configuredDescriptor.isString()) {
				configuredValue = job.getStringProperties().get(configuredName);
			} else if (configuredDescriptor.isColumn()) {
				String[] columnNames = job.getColumnProperties().get(
						configuredName);
				configuredValue = convertToColumns(dataContext, columnNames);
			} else if (configuredDescriptor.isTable()) {
				String[] tableNames = job.getTableProperties().get(
						configuredName);
				configuredValue = convertToTables(dataContext, tableNames);
			} else if (configuredDescriptor.isSchema()) {
				String[] schemaNames = job.getSchemaProperties().get(
						configuredName);
				configuredValue = convertToSchemas(dataContext, schemaNames);
			}
			if (configuredDescriptor.isArray()) {
				configuredDescriptor.assignValue(analyzer, configuredValue);
			} else {
				if (configuredValue.getClass().isArray()) {
					configuredValue = Array.get(configuredValue, 0);
				}
				configuredDescriptor.assignValue(analyzer, configuredValue);
			}
		}
	}

	private Object initializeExploringAnalyzer(AnalysisJob job,
			AnalyzerBeanDescriptor analyzerBeanDescriptor,
			DataContext dataContext) {
		try {
			Object analyzerBean = analyzerBeanDescriptor.getAnalyzerClass()
					.newInstance();
			initializeProperties(analyzerBean, job, dataContext,
					analyzerBeanDescriptor);
			InitializeDescriptor.initialize(analyzerBean,
					analyzerBeanDescriptor);
			return analyzerBean;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not initialize analyzer based on job: " + job, e);
		}
	}

	private void initializeProperties(Object analyzerBean, AnalysisJob job,
			DataContext dataContext,
			AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		List<ConfiguredDescriptor> configuredDescriptors = analyzerBeanDescriptor
				.getConfiguredDescriptors();
		initializeConfiguredProperties(analyzerBean, job,
				configuredDescriptors, dataContext);

		providedCollectionProvider.provide(analyzerBean, analyzerBeanDescriptor);
	}

	private Table[] convertToTables(DataContext dataContext, String[] tableNames) {
		Table[] result = new Table[tableNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getTableByQualifiedLabel(tableNames[i]);
		}
		return result;
	}

	private Schema[] convertToSchemas(DataContext dataContext,
			String[] schemaNames) {
		Schema[] result = new Schema[schemaNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getSchemaByName(schemaNames[i]);
		}
		return result;
	}

	private Column[] convertToColumns(DataContext dataContext,
			String[] columnNames) {
		Column[] result = new Column[columnNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getColumnByQualifiedLabel(columnNames[i]);
		}
		return result;
	}

	public void setJobs(List<AnalysisJob> jobs) {
		_jobs = jobs;
	}

	public void addJob(AnalysisJob job) {
		_jobs.add(job);
	}

	public List<AnalysisJob> getJobs() {
		return Collections.unmodifiableList(_jobs);
	}

	public void setScanner(AnnotationScanner scanner) {
		_scanner = scanner;
	}

	public AnnotationScanner getScanner() {
		if (_scanner == null) {
			_scanner = new AnnotationScanner();
		}
		return _scanner;
	}

	public List<AnalysisResult> getTableResults() {
		ArrayList<AnalysisResult> list = new ArrayList<AnalysisResult>();
		for (AnalysisResult result : _results) {
			list.add(result);
		}
		return list;
	}

	public List<AnalysisResult> getErrorResults() {
		ArrayList<AnalysisResult> list = new ArrayList<AnalysisResult>();
		for (AnalysisResult result : _results) {
			if (!result.isSuccesful()) {
				list.add(result);
			}
		}
		return list;
	}
}