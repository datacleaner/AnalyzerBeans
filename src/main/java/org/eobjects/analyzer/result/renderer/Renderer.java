package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.result.AnalyzerResult;

public interface Renderer<I extends AnalyzerResult, O> {

	public O render(I result);
}
