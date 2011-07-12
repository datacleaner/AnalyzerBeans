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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;

public final class InitializeMethodDescriptorImpl implements InitializeMethodDescriptor {

	private static final Class<?>[] ALLOWED_PARAMETER_TYPES = new Class[] { DatastoreCatalog.class, ReferenceDataCatalog.class };

	private final Method _method;
	private final Class<?>[] _parameterTypes;

	public InitializeMethodDescriptorImpl(Method method) {
		_parameterTypes = method.getParameterTypes();
		for (Class<?> parameterType : _parameterTypes) {
			boolean accepted = false;
			for (Class<?> allowedClass : ALLOWED_PARAMETER_TYPES) {
				if (parameterType == allowedClass) {
					accepted = true;
					break;
				}
			}
			if (!accepted) {
				throwIllegalParameterException(parameterType);
			}
		}
		if (method.getReturnType() != void.class) {
			throw new DescriptorException("Initialize methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public Class<?>[] getParameterTypes() {
		return _parameterTypes;
	}

	public void initialize(Object bean, DatastoreCatalog datastoreCatalog, ReferenceDataCatalog referenceDataCatalog)
			throws IllegalStateException {
		Object[] arguments = new Object[_parameterTypes.length];
		for (int i = 0; i < arguments.length; i++) {
			Class<?> parameterType = _parameterTypes[i];
			if (parameterType == DatastoreCatalog.class) {
				arguments[i] = datastoreCatalog;
			} else if (parameterType == ReferenceDataCatalog.class) {
				arguments[i] = referenceDataCatalog;
			} else {
				throwIllegalParameterException(parameterType);
			}
		}

		try {
			_method.invoke(bean, arguments);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke initializing method " + _method, e);
		}
	}

	private void throwIllegalParameterException(Class<?> parameterType) {
		throw new DescriptorException("Initialize methods can only have parameters with the following types: "
				+ Arrays.toString(ALLOWED_PARAMETER_TYPES) + ", found: " + parameterType);
	}

	@Override
	public String toString() {
		return "InitializeMethodDescriptorImpl[method=" + _method.getName() + "]";
	}

	@Override
	public Set<Annotation> getAnnotations() {
		Annotation[] annotations = _method.getAnnotations();
		return new HashSet<Annotation>(Arrays.asList(annotations));
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _method.getAnnotation(annotationClass);
	}
}
