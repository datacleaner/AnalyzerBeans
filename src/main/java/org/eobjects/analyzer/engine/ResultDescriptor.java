package org.eobjects.analyzer.engine;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.result.AnalysisResult;

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
		if (AnalysisResult.class != type) {
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

	public AnalysisResult[] getResults(Object analyser) {
		try {
			Object resultObject = _method.invoke(analyser, new Object[0]);
			Class<? extends Object> resultClass = resultObject.getClass();
			AnalysisResult[] result;
			if (resultClass.isArray()) {
				int length = Array.getLength(resultObject);
				result = new AnalysisResult[length];
				for (int i = 0; i < result.length; i++) {
					result[i] = (AnalysisResult) Array.get(resultObject, i);
				}
			} else {
				result = new AnalysisResult[] { (AnalysisResult) resultObject };
			}
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke @Result method " + _method, e);
		}
	}

	public AnalysisResult getResult(Object analyser) {
		try {
			Object resultObject = _method.invoke(analyser, new Object[0]);
			Class<? extends Object> resultClass = resultObject.getClass();
			if (resultClass.isArray()) {
				throw new IllegalArgumentException(
						"Invoked @Result method returns an array of AnalysisResult objects but getResult() was used to retrieve it. Please use getResults() instead");
			}
			return (AnalysisResult) resultObject;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke @Result method " + _method, e);
		}
	}

	public static List<AnalysisResult> getResults(Object analyser,
			AnalyzerBeanDescriptor analyserDescriptor) {
		List<AnalysisResult> results = new LinkedList<AnalysisResult>();
		List<ResultDescriptor> resultDescriptors = analyserDescriptor
				.getResultDescriptors();
		for (ResultDescriptor resultDescriptor : resultDescriptors) {
			if (resultDescriptor.isArray()) {
				AnalysisResult[] analysisResult = resultDescriptor
						.getResults(analyser);
				for (AnalysisResult result : analysisResult) {
					if (result.getAnalyserClass() == null) {
						result.setAnalyserClass(analyserDescriptor
								.getAnalyzerClass());
					}
					results.add(result);
				}
			} else {
				AnalysisResult result = resultDescriptor.getResult(analyser);
				if (result.getAnalyserClass() == null) {
					result.setAnalyserClass(analyserDescriptor
							.getAnalyzerClass());
				}
				results.add(result);
			}
		}
		return results;
	}
}
