package org.eobjects.analyzer.engine;

import java.lang.reflect.Method;
import java.util.List;

import org.eobjects.analyzer.annotations.Initialize;

public class InitializeDescriptor {

	private Method _method;

	public InitializeDescriptor(Method method, Initialize initializeAnnotation) {
		_method = method;
	}

	public void initialize(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke @Initialize method " + _method, e);
		}
	}

	public static void initialize(Object analyzerBean, AnalyzerBeanDescriptor analyzerBeanDescriptor)
			throws IllegalStateException {
		List<InitializeDescriptor> initializeDescriptors = analyzerBeanDescriptor.getInitializeDescriptors();
		for (InitializeDescriptor initializeDescriptor : initializeDescriptors) {
			initializeDescriptor.initialize(analyzerBean);
		}
	}

	@Override
	public String toString() {
		return "InitializeDescriptor[method=" + _method + "]";
	}
}
