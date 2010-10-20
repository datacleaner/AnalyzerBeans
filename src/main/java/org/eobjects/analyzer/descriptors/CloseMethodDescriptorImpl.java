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
