package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface CloseMethodDescriptor {

	public void close(Object bean);

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
