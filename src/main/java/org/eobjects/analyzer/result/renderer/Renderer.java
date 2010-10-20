package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * 
 * @author Kasper Sørensen
 * 
 * @param <I>
 *            the input of the renderer, ie. the result type to render
 * @param <O>
 *            the output type of the renderer. This should be the same as or a
 *            subclass of the output class of the matching RenderingFormat.
 * 
 * @see RendererBean
 * @see RenderingFormat
 */
public interface Renderer<I extends AnalyzerResult, O> {

	public O render(I result);
}
