package org.eobjects.analyzer.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Close;
import org.eobjects.analyzer.annotations.Require;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;

public class AnalyzerBeanDescriptor {

	private Class<?> _analyzerClass;
	private String _displayName;
	private ExecutionType _executionType;
	private List<RequireDescriptor> _requireDescriptors = new LinkedList<RequireDescriptor>();
	private List<RunDescriptor> _runDescriptors = new LinkedList<RunDescriptor>();
	private List<ResultDescriptor> _resultDescriptors = new LinkedList<ResultDescriptor>();
	private List<CloseDescriptor> _closeDescriptors = new LinkedList<CloseDescriptor>();

	public AnalyzerBeanDescriptor(Class<?> analyzerClass) throws IllegalArgumentException {
		_analyzerClass = analyzerClass;
		AnalyzerBean analyzerAnnotation = _analyzerClass.getAnnotation(AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new IllegalArgumentException(analyzerClass + " doesn't implement the AnalyzerBean annotation");
		}
		_displayName = analyzerAnnotation.displayName();
		_executionType = analyzerAnnotation.execution();

		Field[] fields = _analyzerClass.getFields();
		for (Field field : fields) {
			Require requireAnnotation = field.getAnnotation(Require.class);
			if (requireAnnotation != null) {
				_requireDescriptors.add(new RequireDescriptor(field, requireAnnotation));
			}
		}

		Method[] methods = _analyzerClass.getMethods();
		for (Method method : methods) {
			Require requireAnnotation = method.getAnnotation(Require.class);
			if (requireAnnotation != null) {
				_requireDescriptors.add(new RequireDescriptor(method, requireAnnotation));
			}

			Run runAnnotation = method.getAnnotation(Run.class);
			if (runAnnotation != null) {
				_runDescriptors.add(new RunDescriptor(method, runAnnotation, _executionType));
			}

			Result resultAnnotation = method.getAnnotation(Result.class);
			if (resultAnnotation != null) {
				_resultDescriptors.add(new ResultDescriptor(method, resultAnnotation));
			}

			Close closeAnnotation = method.getAnnotation(Close.class);
			if (closeAnnotation != null) {
				_closeDescriptors.add(new CloseDescriptor(method, closeAnnotation));
			}
		}

		if (_runDescriptors.isEmpty()) {
			throw new IllegalArgumentException(analyzerClass + " doesn't define any Run annotated methods");
		}

		if (_resultDescriptors.isEmpty()) {
			throw new IllegalArgumentException(analyzerClass + " doesn't define any Result annotated methods");
		}

		// Make the descriptor lists read-only
		_requireDescriptors = Collections.unmodifiableList(_requireDescriptors);
		_runDescriptors = Collections.unmodifiableList(_runDescriptors);
		_resultDescriptors = Collections.unmodifiableList(_resultDescriptors);
	}

	public Class<?> getAnalyzerClass() {
		return _analyzerClass;
	}

	public String getDisplayName() {
		return _displayName;
	}

	public ExecutionType getExecutionType() {
		return _executionType;
	}

	public boolean isExploringExecutionType() {
		return _executionType == ExecutionType.EXPLORING;
	}

	public boolean isRowProcessingExecutionType() {
		return _executionType == ExecutionType.ROW_PROCESSING;
	}

	public List<RequireDescriptor> getRequireDescriptors() {
		return _requireDescriptors;
	}

	public RequireDescriptor getRequireDescriptor(String requireName) {
		for (RequireDescriptor requireDescriptor : _requireDescriptors) {
			if (requireName.equals(requireDescriptor.getName())) {
				return requireDescriptor;
			}
		}
		return null;
	}

	public List<ResultDescriptor> getResultDescriptors() {
		return _resultDescriptors;
	}

	public List<RunDescriptor> getRunDescriptors() {
		return _runDescriptors;
	}
	
	public List<CloseDescriptor> getCloseDescriptors() {
		return _closeDescriptors;
	}

	@Override
	public String toString() {
		return "AnalyzerBeanDescriptor[analyzerClass=" + _analyzerClass + "]";
	}

	@Override
	public int hashCode() {
		return _analyzerClass.hashCode();
	}
}