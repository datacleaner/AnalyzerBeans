package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Close;

public class CloseDescriptor {

	private Method _method;

	public CloseDescriptor(Method method) {
		this(method, null);
	}

	public CloseDescriptor(Method method, Close closeAnnotation)
			throws IllegalArgumentException {
		if (method.getParameterTypes().length != 0) {
			throw new IllegalArgumentException(
					"@Close annotated methods cannot have parameters");
		}
		if (method.getReturnType() != Void.class) {
			throw new IllegalArgumentException(
					"@Close annotated methods can only be void");
		}
		_method = method;
		_method.setAccessible(true);
	}

	public void close(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke @Close method "
					+ _method, e);
		}
	}

	@Override
	public String toString() {
		return "CloseDescriptor[method=" + _method + "]";
	}
}
