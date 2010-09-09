package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public interface PropertyDescriptor {

	public String getName();

	public void setValue(Object bean, Object value)
			throws IllegalArgumentException;

	public Object getValue(Object bean) throws IllegalArgumentException;

	public Set<Annotation> getAnnotations();

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

	/**
	 * Gets the property type, as specified by the field representing the
	 * property
	 * 
	 * @return
	 */
	public Class<?> getType();

	/**
	 * @return whether or not the type of the property type is an array
	 */
	public boolean isArray();

	/**
	 * @return the type of the property or the component type of the array, if
	 *         the property type is an array
	 */
	public Class<?> getBaseType();

	public int getTypeArgumentCount();

	public Type getTypeArgument(int i) throws IndexOutOfBoundsException;

}
