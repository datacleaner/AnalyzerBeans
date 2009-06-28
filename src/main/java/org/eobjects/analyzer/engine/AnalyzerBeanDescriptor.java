package org.eobjects.analyzer.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.annotations.AnalyzerBean;
import org.eobjects.analyzer.annotations.Close;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.annotations.Result;
import org.eobjects.analyzer.annotations.Run;

public class AnalyzerBeanDescriptor {

	private Class<?> _analyzerClass;
	private String _displayName;
	private ExecutionType _executionType;
	private List<ConfiguredDescriptor> _configuredDescriptors = new LinkedList<ConfiguredDescriptor>();
	private List<ProvidedDescriptor> _providedDescriptors = new LinkedList<ProvidedDescriptor>();
	private List<InitializeDescriptor> _initializeDescriptors = new LinkedList<InitializeDescriptor>();
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
		if (_displayName == null || _displayName.trim().equals("")) {
			_displayName = AnnotationHelper.explodeCamelCase(_analyzerClass.getSimpleName());
		}
		_executionType = analyzerAnnotation.execution();

		Field[] fields = _analyzerClass.getFields();
		for (Field field : fields) {
			Configured configuredAnnotation = field.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				_configuredDescriptors.add(new ConfiguredDescriptor(field, configuredAnnotation));
			}

			Provided providedAnnotation = field.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				_providedDescriptors.add(new ProvidedDescriptor(field, providedAnnotation));
			}
		}

		Method[] methods = _analyzerClass.getMethods();
		for (Method method : methods) {
			Configured configuredAnnotation = method.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				_configuredDescriptors.add(new ConfiguredDescriptor(method, configuredAnnotation));
			}

			Provided providedAnnotation = method.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				_providedDescriptors.add(new ProvidedDescriptor(method, providedAnnotation));
			}

			Initialize initializeAnnotation = method.getAnnotation(Initialize.class);
			if (initializeAnnotation != null) {
				_initializeDescriptors.add(new InitializeDescriptor(method, initializeAnnotation));
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
		_configuredDescriptors = Collections.unmodifiableList(_configuredDescriptors);
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

	public List<ConfiguredDescriptor> getConfiguredDescriptors() {
		return _configuredDescriptors;
	}
	
	public List<InitializeDescriptor> getInitializeDescriptors() {
		return _initializeDescriptors;
	}
	
	public List<ProvidedDescriptor> getProvidedDescriptors() {
		return _providedDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptor(String configuredName) {
		for (ConfiguredDescriptor configuredDescriptor : _configuredDescriptors) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
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