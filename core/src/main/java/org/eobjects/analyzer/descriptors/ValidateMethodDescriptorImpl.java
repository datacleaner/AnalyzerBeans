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

import org.eobjects.analyzer.util.ReflectionUtils;

final class ValidateMethodDescriptorImpl implements ValidateMethodDescriptor {

	private final Method _method;

	public ValidateMethodDescriptorImpl(Method method) {
		_method = method;
	}

	@Override
	public void validate(Object component) {
		try {
			_method.invoke(component);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke validation method " + _method, e);
		}
	}


	@Override
	public String toString() {
		return "ValidateMethodDescriptorImpl[method=" + _method.getName() + "]";
	}

	@Override
	public Set<Annotation> getAnnotations() {
		Annotation[] annotations = _method.getAnnotations();
		return new HashSet<Annotation>(Arrays.asList(annotations));
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return ReflectionUtils.getAnnotation(_method, annotationClass);
	}

}
