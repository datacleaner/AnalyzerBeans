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

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.lifecycle.MemberInjectionPoint;

final class InitializeMethodDescriptorImpl implements InitializeMethodDescriptor {

	private final Method _method;
	private final Class<?>[] _parameterTypes;

	protected InitializeMethodDescriptorImpl(Method method) {
		_parameterTypes = method.getParameterTypes();
		if (method.getReturnType() != void.class) {
			throw new DescriptorException("Initialize methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public Class<?>[] getParameterTypes() {
		return _parameterTypes;
	}

	public void initialize(Object bean, InjectionManager injectionManager) throws IllegalStateException {
		Object[] arguments = new Object[_parameterTypes.length];
		for (int i = 0; i < arguments.length; i++) {
			@SuppressWarnings({ "rawtypes" })
			InjectionPoint<?> injectionPoint = new MemberInjectionPoint(_method, i, bean);

			arguments[i] = injectionManager.getInstance(injectionPoint);
		}

		try {
			_method.invoke(bean, arguments);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke initializing method " + _method, e);
		}
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
