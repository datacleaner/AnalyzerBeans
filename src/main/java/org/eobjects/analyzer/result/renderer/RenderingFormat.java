package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.beans.api.RendererBean;

/**
 * Represents a rendering format to be used for rendering. AnalyzerBeans ships
 * with a couple of built-in rendering formats (eg. HTML and Text), but it is
 * also possible to roll your own. Simply create a class that implements this
 * interface and reference the class in the @RendererBean annotation when
 * implementing renderers.
 * 
 * @author Kasper Sørensen
 * 
 * @param <T>
 * 
 * @see RendererBean
 * @see Renderer
 */
public interface RenderingFormat<T> {

	public Class<T> getOutputClass();
}
