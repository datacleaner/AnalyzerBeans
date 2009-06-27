package org.eobjects.analyzer.engine;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.eobjects.analyzer.annotations.Close;

public class CloseDescriptor {

	private Method _method;

	public CloseDescriptor(Method method, Close closeAnnotation) throws IllegalArgumentException {
		if (method.getParameterTypes().length != 0) {
			throw new IllegalArgumentException("@Close annotated methods cannot have parameters");
		}
		if (method.getReturnType() != Void.class) {
			throw new IllegalArgumentException("@Close annotated methods can only be void");
		}
		_method = method;
	}

	public void close(Object analyzerBean) throws IllegalStateException {
		try {
			_method.invoke(analyzerBean);
		} catch (Exception e) {
			throw new IllegalStateException("Could not invoke @Close method " + _method, e);
		}
	}

	public static void close(Object analyzerBean, AnalyzerBeanDescriptor analyzerBeanDescriptor)
			throws IllegalStateException {
		List<CloseDescriptor> closeDescriptors = analyzerBeanDescriptor.getCloseDescriptors();
		for (CloseDescriptor closeDescriptor : closeDescriptors) {
			closeDescriptor.close(analyzerBean);
		}
		if (analyzerBean instanceof Closeable) {
			try {
				((Closeable) analyzerBean).close();
			} catch (IOException e) {
				throw new IllegalStateException("Could not invoke java.io.Closeable.close() method", e);
			}
		}
	}

	@Override
	public String toString() {
		return "CloseDescriptor[method=" + _method + "]";
	}
}
