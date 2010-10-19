package org.eobjects.analyzer.result;

import dk.eobjects.metamodel.DataContext;

/**
 * AnalyzerResult sub-interface that supports datacontext aware operations. The
 * DataContext to use is injected using the setter method defined in this
 * interface.
 * 
 * @author Kasper Sørensen
 */
public interface DataContextAwareAnalyzerResult extends AnalyzerResult {

	public void setDataContext(DataContext dataContext);
}
