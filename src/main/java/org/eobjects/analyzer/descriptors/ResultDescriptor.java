package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.result.AnalyzerBeanResult;

public class ResultDescriptor {

	private String _name;
	private boolean _array;
	private Method _method;

	public ResultDescriptor(Method method, Result resultAnnotation)
			throws IllegalArgumentException {
		_method = method;
		_name = resultAnnotation.value();
		if (method.getParameterTypes().length != 0) {
			throw new IllegalArgumentException(
					"@Result annotated methods cannot have parameters");
		}
		Class<?> returnType = method.getReturnType();
		if (returnType.isArray()) {
			_array = true;
			validateType(returnType.getComponentType());
		} else {
			_array = false;
			validateType(returnType);
		}
	}

	private void validateType(Class<?> type) throws IllegalArgumentException {
		if (AnalyzerBeanResult.class != type) {
			throw new IllegalArgumentException(
					"Unsupported return type for @Result annotated method: "
							+ type);
		}
	}

	public String getName() {
		return _name;
	}

	public boolean isArray() {
		return _array;
	}

	@Override
	public String toString() {
		return "ResultDescriptor[method=" + _method + "]";
	}

	public AnalyzerBeanResult[] getResults(Object analyzerBean) {
		try {
			Object resultObject = _method.invoke(analyzerBean, new Object[0]);
			Class<? extends Object> resultClass = resultObject.getClass();
			AnalyzerBeanResult[] result;
			if (resultClass.isArray()) {
				int length = Array.getLength(resultObject);
				result = new AnalyzerBeanResult[length];
				for (int i = 0; i < result.length; i++) {
					result[i] = (AnalyzerBeanResult) Array.get(resultObject, i);
				}
			} else {
				result = new AnalyzerBeanResult[] { (AnalyzerBeanResult) resultObject };
			}
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke @Result method " + _method, e);
		}
	}

	public AnalyzerBeanResult getResult(Object analyzerBean) {
		try {
			Object resultObject = _method.invoke(analyzerBean, new Object[0]);
			Class<? extends Object> resultClass = resultObject.getClass();
			if (resultClass.isArray()) {
				throw new IllegalArgumentException(
						"Invoked @Result method returns an array of AnalysisResult objects but getResult() was used to retrieve it. Please use getResults() instead");
			}
			return (AnalyzerBeanResult) resultObject;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke @Result method " + _method, e);
		}
	}
}
