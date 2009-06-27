package org.eobjects.analyzer.engine;

/**
 * Determines the type of mechanism to use for executing Analysers.
 */
public enum ExecutionType {

	/**
	 * Execution type for analysers that needs to have full control of it's
	 * DataContext and query-generation. Typically EXPLORING analysers are used
	 * to analyse metadata and certain analysis tasks that can be substantially
	 * optimised by specialised queries.
	 */
	EXPLORING,

	/**
	 * Execution type for analysers that process the rows yielded by executing a
	 * predefined query. Hence ROW_PROCESSING analysers are not in control of
	 * the DataContext and are not able to execute queries themselves. The
	 * up-side of this approach is that queries can be shared between analysers
	 * since the same query can be used to activate multiple processors.
	 */
	ROW_PROCESSING
}