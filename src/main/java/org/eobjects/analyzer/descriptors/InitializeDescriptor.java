package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Initialize;

public class InitializeDescriptor {

	private Method _method;

	public InitializeDescriptor(Method method, Initialize initializeAnnotation) {
		_method = method;
		_method.setAccessible(true);
	}

	public void initialize(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke @Initialize method " + _method, e);
		}
	}

	@Override
	public String toString() {
		return "InitializeDescriptor[method=" + _method + "]";
	}
}
