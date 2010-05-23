package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;

import javax.annotation.PreDestroy;

import org.eobjects.analyzer.annotations.Close;

public class CloseDescriptor {

	private Method _method;

	public CloseDescriptor(Method method) {
		if (method.getParameterTypes().length != 0) {
			throw new DescriptorException(
					"@Close annotated methods cannot have parameters");
		}
		if (method.getReturnType() != void.class) {
			throw new DescriptorException(
					"@Close annotated methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public CloseDescriptor(Method method, Close closeAnnotation)
			throws DescriptorException {
		this(method);
	}

	public CloseDescriptor(Method method, PreDestroy preDestroyAnnotation) {
		this(method);
	}

	public void close(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke closing method "
					+ _method, e);
		}
	}

	@Override
	public String toString() {
		return "CloseDescriptor[method=" + _method + "]";
	}
}
