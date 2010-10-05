package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eobjects.analyzer.result.renderer.RenderingFormat;

/**
 * Annotation used to mark a class as a renderer for AnalyzerResults.
 * 
 * @author Kasper Sørensen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface RendererBean {

	public Class<? extends RenderingFormat<?>> value();
}
