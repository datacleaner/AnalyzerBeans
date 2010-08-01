package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.result.AnalyzerResult;


public interface Analyzer<R extends AnalyzerResult> {

	public R getResult();
}
