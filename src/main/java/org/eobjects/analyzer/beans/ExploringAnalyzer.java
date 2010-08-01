package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.result.AnalyzerResult;

import dk.eobjects.metamodel.DataContext;

public interface ExploringAnalyzer<R extends AnalyzerResult> extends Analyzer<R> {

	public void run(DataContext dc);
}
