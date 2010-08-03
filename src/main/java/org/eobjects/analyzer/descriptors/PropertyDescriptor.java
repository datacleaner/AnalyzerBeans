package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public interface PropertyDescriptor {

	public String getName();

	public void assignValue(Object bean, Object value)
			throws IllegalArgumentException;

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);
	
	public Class<?> getBaseType();

	public int getTypeArgumentCount();

	public Type getTypeArgument(int i) throws IndexOutOfBoundsException;
	
	public boolean isArray();
}
