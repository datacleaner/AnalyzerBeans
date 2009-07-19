package org.eobjects.analyzer.annotations;

/**
 * Determines the type of mechanism to use for executing AnalyzerBean.
 */
public enum ExecutionType {

	/**
	 * Execution type for AnalyzerBeans that needs to have full control of it's
	 * DataContext and query-generation. Typically EXPLORING AnalyzerBeans are
	 * used to analyse metadata and certain analysis tasks that can be
	 * substantially optimised by specialised queries.
	 */
	EXPLORING,

	/**
	 * Execution type for AnalyzerBeans that process the rows yielded by
	 * executing a predefined query. Hence ROW_PROCESSING AnalyzerBean are not
	 * in control of the DataContext and are not able to execute queries
	 * themselves. The up-side of this approach is that queries can be shared
	 * between AnalyzerBeans since the same query can be used to activate
	 * multiple processors.
	 * 
	 * All ROW_PROCESSING AnalyzerBeans must include at least one @Configured
	 * column-array, used to inject the columns that are being analysed by the
	 * instance of the bean
	 */
	ROW_PROCESSING
}