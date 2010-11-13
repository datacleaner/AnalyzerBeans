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
import java.util.Set;

import org.eobjects.analyzer.util.CollectionUtils;

public final class CloseMethodDescriptorImpl implements CloseMethodDescriptor {

	private final Method _method;

	public CloseMethodDescriptorImpl(Method method) {
		if (method.getParameterTypes().length != 0) {
			throw new DescriptorException("Close methods cannot have parameters");
		}
		if (method.getReturnType() != void.class) {
			throw new DescriptorException("Close methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public void close(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke closing method " + _method, e);
		}
	}

	@Override
	public String toString() {
		return "CloseMethodDescriptorImpl[method=" + _method.getName() + "]";
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return CollectionUtils.set(_method.getAnnotations());
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _method.getAnnotation(annotationClass);
	}
}
