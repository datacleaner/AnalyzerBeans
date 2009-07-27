package org.eobjects.analyzer.descriptors;

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
import org.eobjects.analyzer.beans.ExploringAnalyzer;
import org.eobjects.analyzer.beans.RowProcessingAnalyzer;

public class AnalyzerBeanDescriptor implements
		Comparable<AnalyzerBeanDescriptor> {

	private Class<?> analyzerClass;
	private String displayName;
	private boolean exploringAnalyzer;
	private boolean rowProcessingAnalyzer;

	private List<CloseDescriptor> closeDescriptors = new LinkedList<CloseDescriptor>();
	private List<ConfiguredDescriptor> configuredDescriptors = new LinkedList<ConfiguredDescriptor>();
	private List<InitializeDescriptor> initializeDescriptors = new LinkedList<InitializeDescriptor>();
	private List<ProvidedDescriptor> providedDescriptors = new LinkedList<ProvidedDescriptor>();
	private List<ResultDescriptor> resultDescriptors = new LinkedList<ResultDescriptor>();

	public AnalyzerBeanDescriptor(Class<?> analyzerClass)
			throws DescriptorException {
		if (analyzerClass == null) {
			throw new IllegalArgumentException("analyzerClass cannot be null");
		}
		this.analyzerClass = analyzerClass;

		rowProcessingAnalyzer = AnnotationHelper.is(analyzerClass,
				RowProcessingAnalyzer.class);
		exploringAnalyzer = AnnotationHelper.is(analyzerClass,
				ExploringAnalyzer.class);

		if (!rowProcessingAnalyzer && !exploringAnalyzer) {
			throw new DescriptorException(analyzerClass
					+ " does not implement either "
					+ RowProcessingAnalyzer.class.getName() + " or "
					+ ExploringAnalyzer.class.getName());
		}

		AnalyzerBean analyzerAnnotation = analyzerClass
				.getAnnotation(AnalyzerBean.class);
		if (analyzerAnnotation == null) {
			throw new DescriptorException(analyzerClass
					+ " doesn't implement the AnalyzerBean annotation");
		}
		displayName = analyzerAnnotation.value();
		if (displayName == null || displayName.trim().equals("")) {
			displayName = AnnotationHelper.explodeCamelCase(analyzerClass
					.getSimpleName(), false);
		}

		Field[] fields = analyzerClass.getDeclaredFields();
		for (Field field : fields) {
			Configured configuredAnnotation = field
					.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				configuredDescriptors.add(new ConfiguredDescriptor(field,
						configuredAnnotation));
			}

			Provided providedAnnotation = field.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				providedDescriptors.add(new ProvidedDescriptor(field,
						providedAnnotation));
			}
		}

		if (AnnotationHelper.isCloseable(analyzerClass)) {
			try {
				Method method = analyzerClass.getMethod("close",
						new Class<?>[0]);
				closeDescriptors.add(new CloseDescriptor(method));
			} catch (NoSuchMethodException e) {
				// This is impossible since all closeable's have a no-arg close
				// method
				assert false;
			}
		}

		Method[] methods = analyzerClass.getDeclaredMethods();
		for (Method method : methods) {
			Configured configuredAnnotation = method
					.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				configuredDescriptors.add(new ConfiguredDescriptor(method,
						configuredAnnotation));
			}

			Provided providedAnnotation = method.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				providedDescriptors.add(new ProvidedDescriptor(method,
						providedAnnotation));
			}

			Initialize initializeAnnotation = method
					.getAnnotation(Initialize.class);
			if (initializeAnnotation != null) {
				initializeDescriptors.add(new InitializeDescriptor(method,
						initializeAnnotation));
			}

			Result resultAnnotation = method.getAnnotation(Result.class);
			if (resultAnnotation != null) {
				resultDescriptors.add(new ResultDescriptor(method,
						resultAnnotation));
			}

			Close closeAnnotation = method.getAnnotation(Close.class);
			if (closeAnnotation != null) {
				closeDescriptors.add(new CloseDescriptor(method,
						closeAnnotation));
			}
		}

		if (resultDescriptors.isEmpty()) {
			throw new DescriptorException(analyzerClass
					+ " doesn't define any @Result annotated methods");
		}

		if (rowProcessingAnalyzer) {
			boolean hasConfiguredColumnArray = false;
			for (ConfiguredDescriptor cd : configuredDescriptors) {
				if (cd.isArray() && cd.isColumn()) {
					hasConfiguredColumnArray = true;
					break;
				}
			}
			if (!hasConfiguredColumnArray) {
				throw new DescriptorException(analyzerClass
						+ " does not define any @Configured column-arrays");
			}
		}

		// Make the descriptor lists read-only
		closeDescriptors = Collections.unmodifiableList(closeDescriptors);
		configuredDescriptors = Collections
				.unmodifiableList(configuredDescriptors);
		initializeDescriptors = Collections
				.unmodifiableList(initializeDescriptors);
		providedDescriptors = Collections.unmodifiableList(providedDescriptors);
		resultDescriptors = Collections.unmodifiableList(resultDescriptors);
	}

	public Class<?> getAnalyzerClass() {
		return analyzerClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isExploringAnalyzer() {
		return exploringAnalyzer;
	}

	public boolean isRowProcessingAnalyzer() {
		return rowProcessingAnalyzer;
	}

	public List<ConfiguredDescriptor> getConfiguredDescriptors() {
		return configuredDescriptors;
	}

	public List<InitializeDescriptor> getInitializeDescriptors() {
		return initializeDescriptors;
	}

	public List<ProvidedDescriptor> getProvidedDescriptors() {
		return providedDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptor(String configuredName) {
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	public List<ResultDescriptor> getResultDescriptors() {
		return resultDescriptors;
	}

	public List<CloseDescriptor> getCloseDescriptors() {
		return closeDescriptors;
	}

	@Override
	public String toString() {
		return "AnalyzerBeanDescriptor[analyzerClass=" + analyzerClass + "]";
	}

	@Override
	public int hashCode() {
		return analyzerClass.hashCode();
	}

	@Override
	public int compareTo(AnalyzerBeanDescriptor o) {
		String thisAnalyzerClassName = this.getAnalyzerClass().toString();
		String thatAnalyzerClassName = o.getAnalyzerClass().toString();
		return thisAnalyzerClassName.compareTo(thatAnalyzerClassName);
	}
}