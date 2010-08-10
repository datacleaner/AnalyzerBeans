package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RenderingFormat;

public interface RendererBeanDescriptor extends Comparable<RendererBeanDescriptor> {

	public Class<? extends Renderer<?, ?>> getBeanClass();

	public Class<? extends RenderingFormat<?>> getRenderingFormat();

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
