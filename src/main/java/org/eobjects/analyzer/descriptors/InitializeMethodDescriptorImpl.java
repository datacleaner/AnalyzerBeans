package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import org.eobjects.analyzer.util.CollectionUtils;

public class InitializeMethodDescriptorImpl implements
		InitializeMethodDescriptor {

	private Method _method;

	public InitializeMethodDescriptorImpl(Method method) {
		if (method.getParameterTypes().length != 0) {
			throw new DescriptorException(
					"Initialize methods cannot have parameters");
		}
		if (method.getReturnType() != void.class) {
			throw new DescriptorException(
					"Initialize methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public void initialize(Object bean) throws IllegalStateException {
		try {
			_method.invoke(bean);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not invoke initializing method " + _method, e);
		}
	}

	@Override
	public String toString() {
		return "InitializeMethodDescriptorImpl[method=" + _method.getName() + "]";
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
