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
import dk.eobjects.metamodel.schema.Table;

public final class AnalysisRunner {

	private static final Log _log = LogFactory.getLog(AnalysisRunner.class);
	private List<AnalysisJob> _jobs = new LinkedList<AnalysisJob>();
	private List<AnalysisResult> _results = new ArrayList<AnalysisResult>();
	private AnnotationScanner _scanner;

	public void execute(DataContext dataContext) {
		if (_scanner == null) {
			_scanner = new AnnotationScanner();
		}
		Map<Class<?>, AnalyzerBeanDescriptor> descriptors = _scanner.getDescriptors();
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
				throw new UnsupportedOperationException("Analysis execution type " + descriptor.getExecutionType()
						+ " is not supported by AnalysisRunner");
			}
		}

		// Initialise all jobs before we run anything
		List<Object[]> analyzersAndDescriptors = new LinkedList<Object[]>();
		for (AnalysisJob job : explorerJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptors.get(analyzerClass);
			Object analyzer = initializeExploringAnalyzer(job, descriptor, dataContext);
			analyzersAndDescriptors.add(new Object[] { analyzer, descriptor });
		}
		Map<Table, SharedQueryRunnerGroup> sharedQueryAnalyzerGroups = new HashMap<Table, SharedQueryRunnerGroup>();
		for (AnalysisJob job : rowProcessingJobs) {
			Class<?> analyzerClass = job.getAnalyzerClass();
			AnalyzerBeanDescriptor descriptor = descriptors.get(analyzerClass);
			initializeRowProcessingAnalyzers(job, descriptor, sharedQueryAnalyzerGroups, dataContext);
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

	private void initializeRowProcessingAnalyzers(AnalysisJob job, AnalyzerBeanDescriptor analyzerDescriptor,
			Map<Table, SharedQueryRunnerGroup> sharedTableQueries, DataContext dataContext) {
		try {
			// Create list of all RequireDescriptors except the ones who require
			// a column array.
			List<RequireDescriptor> requireDescriptorsExceptColumns = new LinkedList<RequireDescriptor>(
					analyzerDescriptor.getRequireDescriptors());
			for (Iterator<RequireDescriptor> it = requireDescriptorsExceptColumns.iterator(); it.hasNext();) {
				RequireDescriptor requireDescriptor = it.next();
				if (requireDescriptor.isColumn() && requireDescriptor.isArray()) {
					it.remove();
				}
			}

			// Create an analyzer instance for each table that is represented in
			// the column array based RequireDescriptors
			Map<String, String[]> columnProperties = job.getColumnProperties();
			Set<Entry<String, String[]>> columnPropertyEntries = columnProperties.entrySet();
			for (Entry<String, String[]> entry : columnPropertyEntries) {
				String requireName = entry.getKey();
				RequireDescriptor requireDescriptor = analyzerDescriptor.getRequireDescriptor(requireName);
				if (requireDescriptor == null) {
					throw new IllegalStateException("Analyzer class '" + analyzerDescriptor.getAnalyzerClass()
							+ "' does not specify @Require field or method with name: " + requireName);
				}
				if (requireDescriptor.isArray()) {
					String[] columnNames = entry.getValue();
					Column[] columns = convertToColumns(dataContext, columnNames);
					Table[] tables = MetaModelHelper.getTables(columns);
					for (Table table : tables) {
						Column[] columnsForAnalyzer = MetaModelHelper.getTableColumns(table, columns);

						Object analyzer = analyzerDescriptor.getAnalyzerClass().newInstance();

						// First assign the columns to the analyzer
						requireDescriptor.assignValue(analyzer, columnsForAnalyzer);

						// Then initialize the rest of the properties
						initializeRequiredProperties(analyzer, job, requireDescriptorsExceptColumns, dataContext);

						// Add the analysis to a shared query group
						SharedQueryRunnerGroup group = sharedTableQueries.get(table);
						if (group == null) {
							group = new SharedQueryRunnerGroup(table);
							sharedTableQueries.put(table, group);
						}
						group.registerAnalyzer(analyzer, analyzerDescriptor, columnsForAnalyzer);
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not initialize analyzer based on job: " + job, e);
		}
	}

	private void runExplorerAnalyzer(Object analyzer, AnalyzerBeanDescriptor analyzerDescriptor, DataContext dataContext) {
		List<RunDescriptor> runDescriptors = analyzerDescriptor.getRunDescriptors();
		for (RunDescriptor runDescriptor : runDescriptors) {
			runDescriptor.explore(analyzer, dataContext);
		}
	}

	private void initializeRequiredProperties(Object analyzer, AnalysisJob job,
			List<RequireDescriptor> requireDescriptors, DataContext dataContext) {
		for (RequireDescriptor requireDescriptor : requireDescriptors) {
			Object requireValue = null;
			String requireName = requireDescriptor.getName();
			if (requireDescriptor.isBoolean()) {
				requireValue = job.getBooleanProperties().get(requireName);
			} else if (requireDescriptor.isInteger()) {
				requireValue = job.getIntegerProperties().get(requireName);
			} else if (requireDescriptor.isLong()) {
				requireValue = job.getLongProperties().get(requireName);
			} else if (requireDescriptor.isDouble()) {
				requireValue = job.getDoubleProperties().get(requireName);
			} else if (requireDescriptor.isString()) {
				requireValue = job.getStringProperties().get(requireName);
			} else if (requireDescriptor.isColumn()) {
				String[] columnNames = job.getColumnProperties().get(requireName);
				requireValue = convertToColumns(dataContext, columnNames);
			} else if (requireDescriptor.isTable()) {
				String[] tableNames = job.getTableProperties().get(requireName);
				requireValue = convertToTables(dataContext, tableNames);
			}
			if (requireDescriptor.isArray()) {
				requireDescriptor.assignValue(analyzer, requireValue);
			} else {
				if (requireValue.getClass().isArray()) {
					requireValue = Array.get(requireValue, 0);
				}
				requireDescriptor.assignValue(analyzer, requireValue);
			}
		}
	}

	private Object initializeExploringAnalyzer(AnalysisJob job, AnalyzerBeanDescriptor analyzerDescriptor,
			DataContext dataContext) {
		List<RequireDescriptor> requireDescriptors = analyzerDescriptor.getRequireDescriptors();
		try {
			Object analyzer = analyzerDescriptor.getAnalyzerClass().newInstance();
			initializeRequiredProperties(analyzer, job, requireDescriptors, dataContext);
			return analyzer;
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not initialize analyzer based on job: " + job, e);
		}
	}

	private Table[] convertToTables(DataContext dataContext, String[] tableNames) {
		Table[] result = new Table[tableNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dataContext.getTableByQualifiedLabel(tableNames[i]);
		}
		return result;
	}

	private Column[] convertToColumns(DataContext dataContext, String[] columnNames) {
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