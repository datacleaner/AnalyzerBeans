package org.eobjects.analyzer.engine;

import java.lang.reflect.Method;

import org.eobjects.analyzer.annotations.Run;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.Row;

public class RunDescriptor {

	private Method _method;
	private int _parameters;
	private Short _dataContextIndex;
	private Short _distinctCountIndex;
	private Short _rowIndex;

	public RunDescriptor(Method method, Run runAnnotation,
			ExecutionType executionType) {
		_method = method;
		Class<?>[] parameterTypes = method.getParameterTypes();
		_parameters = parameterTypes.length;
		for (short i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			if (DataContext.class.isAssignableFrom(parameterType)) {
				_dataContextIndex = i;
			} else if (parameterType == Long.class
					|| parameterType == long.class) {
				_distinctCountIndex = i;
			} else if (parameterType == Row.class) {
				_rowIndex = i;
			} else {
				throw new IllegalArgumentException(
						"Illegal parameter of type '" + parameterType
								+ "' in @Run annotated method");
			}
		}

		if (executionType == ExecutionType.EXPLORING) {
			if (_dataContextIndex == null || _rowIndex != null
					|| _distinctCountIndex != null) {
				throw new IllegalArgumentException(
						"For EXPLORING execution type analysers a DataContext parameter and no other parameters are required in @Run annotated methods");
			}
		} else if (executionType == ExecutionType.ROW_PROCESSING) {
			if (_dataContextIndex != null || _rowIndex == null) {
				throw new IllegalArgumentException(
						"For ROW_PROCESSING execution type analysers a Row parameter, an optional Long parameter and no other parameters are required in @Run annotated methods");
			}
		} else {
			throw new IllegalArgumentException("Unsupported execution type '"
					+ executionType + "'");
		}
	}

	public void processRow(Object analyser, Row row, Long distinctCount) {
		Object[] values = new Object[_parameters];
		values[_rowIndex] = row;
		if (_distinctCountIndex != null) {
			values[_distinctCountIndex] = distinctCount;
		}
		try {
			_method.invoke(analyser, values);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke row processing analysis @Run method "
							+ _method, e);
		}
	}

	public void explore(Object analyser, DataContext dataContext) {
		Object[] values = new Object[_parameters];
		values[_dataContextIndex] = dataContext;
		try {
			_method.invoke(analyser, values);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Could not invoke exploring analysis @Run method "
							+ _method + " on analyser: " + analyser, e);
		}
	}

	@Override
	public String toString() {
		return "RunDescriptor[method=" + _method + "]";
	}
}