package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.annotations.RendererBean;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * A very (very!) simple renderer that simply "renders" the toString() method of
 * results. Mostly used for testing (or result types that implement a meaningful
 * toString() method.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(TextRenderingFormat.class)
public class DefaultTextRenderer implements Renderer<AnalyzerResult, String> {

	@Override
	public String render(AnalyzerResult result) {
		return result.toString();
	}

}
