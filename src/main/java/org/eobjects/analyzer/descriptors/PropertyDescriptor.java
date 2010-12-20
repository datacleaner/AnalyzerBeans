/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public interface PropertyDescriptor extends Comparable<PropertyDescriptor> {

	public String getName();

	public void setValue(Object bean, Object value) throws IllegalArgumentException;

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

	public ComponentDescriptor<?> getComponentDescriptor();

	public int getTypeArgumentCount();

	public Type getTypeArgument(int i) throws IndexOutOfBoundsException;

}
