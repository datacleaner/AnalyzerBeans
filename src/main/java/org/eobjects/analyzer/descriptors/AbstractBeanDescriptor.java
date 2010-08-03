package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Close;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBeanDescriptor implements
		Comparable<AbstractBeanDescriptor> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Class<?> beanClass;
	protected List<CloseDescriptor> closeDescriptors = new LinkedList<CloseDescriptor>();
	protected List<ConfiguredDescriptor> configuredDescriptors = new LinkedList<ConfiguredDescriptor>();
	protected List<InitializeDescriptor> initializeDescriptors = new LinkedList<InitializeDescriptor>();
	protected List<ProvidedDescriptor> providedDescriptors = new LinkedList<ProvidedDescriptor>();

	public AbstractBeanDescriptor(Class<?> beanClass,
			boolean requireInputColumns) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		this.beanClass = beanClass;

		Field[] fields = beanClass.getDeclaredFields();
		for (Field field : fields) {

			Configured configuredAnnotation = field
					.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				if (!field.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Configured field: {}",
							field);
				}
				configuredDescriptors.add(new ConfiguredDescriptor(field,
						configuredAnnotation));
			}

			Provided providedAnnotation = field.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				if (!field.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Provided field: {}",
							field);
				}
				providedDescriptors.add(new ProvidedDescriptor(field,
						providedAnnotation));
			}
		}

		if (ReflectionUtils.isCloseable(beanClass)) {
			try {
				Method method = beanClass.getMethod("close", new Class<?>[0]);
				closeDescriptors.add(new CloseDescriptor(method));
			} catch (NoSuchMethodException e) {
				// This is impossible since all closeable's have a no-arg close
				// method
				assert false;
			}
		}

		Method[] methods = beanClass.getDeclaredMethods();
		for (Method method : methods) {
			Configured configuredAnnotation = method
					.getAnnotation(Configured.class);
			if (configuredAnnotation != null) {
				if (!method.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Configured method: {}",
							method);
				}
				configuredDescriptors.add(new ConfiguredDescriptor(method,
						configuredAnnotation));
			}

			Provided providedAnnotation = method.getAnnotation(Provided.class);
			if (providedAnnotation != null) {
				if (!method.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Provided method: {}",
							method);
				}
				providedDescriptors.add(new ProvidedDescriptor(method,
						providedAnnotation));
			}

			Initialize initializeAnnotation = method
					.getAnnotation(Initialize.class);
			if (initializeAnnotation != null) {
				initializeDescriptors.add(new InitializeDescriptor(method,
						initializeAnnotation));
			}

			// @PostConstruct is a valid substitution for @Initialize
			PostConstruct postConstructAnnotation = method
					.getAnnotation(PostConstruct.class);
			if (postConstructAnnotation != null) {
				initializeDescriptors.add(new InitializeDescriptor(method,
						postConstructAnnotation));
			}

			Close closeAnnotation = method.getAnnotation(Close.class);
			if (closeAnnotation != null) {
				closeDescriptors.add(new CloseDescriptor(method,
						closeAnnotation));
			}

			// @PreDestroy is a valid substitution for @Close
			PreDestroy preDestroyAnnotation = method
					.getAnnotation(PreDestroy.class);
			if (preDestroyAnnotation != null) {
				closeDescriptors.add(new CloseDescriptor(method,
						preDestroyAnnotation));
			}
		}

		if (requireInputColumns) {
			int numConfiguredColumns = 0;
			int numConfiguredColumnArrays = 0;
			for (ConfiguredDescriptor cd : configuredDescriptors) {
				if (cd.isInputColumn()) {
					if (cd.isArray()) {
						numConfiguredColumnArrays++;
					} else {
						numConfiguredColumns++;
					}
				}
			}
			int totalColumns = numConfiguredColumns + numConfiguredColumnArrays;
			if (totalColumns == 0) {
				throw new DescriptorException(
						beanClass
								+ " does not define a @Configured InputColumn or InputColumn-array");
			}
			if (totalColumns > 1) {
				throw new DescriptorException(
						beanClass
								+ " defines multiple @Configured InputColumns, cannot determine which one to use for transformation");
			}
		}

		// Make the descriptor lists read-only
		closeDescriptors = Collections.unmodifiableList(closeDescriptors);
		configuredDescriptors = Collections
				.unmodifiableList(configuredDescriptors);
		initializeDescriptors = Collections
				.unmodifiableList(initializeDescriptors);
		providedDescriptors = Collections.unmodifiableList(providedDescriptors);
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public List<InitializeDescriptor> getInitializeDescriptors() {
		return initializeDescriptors;
	}

	public List<ProvidedDescriptor> getProvidedDescriptors() {
		return providedDescriptors;
	}

	public List<ConfiguredDescriptor> getConfiguredDescriptors() {
		return configuredDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptor(String configuredName) {
		for (ConfiguredDescriptor configuredDescriptor : configuredDescriptors) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	public List<CloseDescriptor> getCloseDescriptors() {
		return closeDescriptors;
	}

	public ConfiguredDescriptor getConfiguredDescriptorForInput() {
		List<ConfiguredDescriptor> descriptors = getConfiguredDescriptors();
		for (ConfiguredDescriptor configuredDescriptor : descriptors) {
			if (configuredDescriptor.isInputColumn()) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	public DataTypeFamily getInputDataTypeFamily() {
		ConfiguredDescriptor configuredDescriptor = getConfiguredDescriptorForInput();
		if (configuredDescriptor == null) {
			return DataTypeFamily.UNDEFINED;
		}
		Type genericType = configuredDescriptor.getGenericType();
		Class<?> typeParameter = ReflectionUtils.getTypeParameter(genericType,
				0);
		return DataTypeFamily.valueOf(typeParameter);
	}

	@Override
	public int hashCode() {
		return beanClass.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass() == this.getClass()) {
			AbstractBeanDescriptor that = (AbstractBeanDescriptor) obj;
			return this.beanClass == that.beanClass;
		}
		return false;
	}

	@Override
	public int compareTo(AbstractBeanDescriptor o) {
		String thisAnalyzerClassName = this.getBeanClass().toString();
		String thatAnalyzerClassName = o.getBeanClass().toString();
		return thisAnalyzerClassName.compareTo(thatAnalyzerClassName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[beanClass=" + beanClass.getName()
				+ "]";
	}
}
