package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Close;
import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.Initialize;
import org.eobjects.analyzer.annotations.Provided;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBeanDescriptor implements BeanDescriptor {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final Class<?> _beanClass;
	protected final Set<InitializeMethodDescriptor> _initializeMethods = new HashSet<InitializeMethodDescriptor>();
	protected final Set<ConfiguredPropertyDescriptor> _configuredProperties = new HashSet<ConfiguredPropertyDescriptor>();
	protected final Set<ProvidedPropertyDescriptor> _providedProperties = new HashSet<ProvidedPropertyDescriptor>();
	protected final Set<CloseMethodDescriptor> _closeMethods = new HashSet<CloseMethodDescriptor>();

	public AbstractBeanDescriptor(Class<?> beanClass,
			boolean requireInputColumns) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass cannot be null");
		}
		_beanClass = beanClass;

		if (_beanClass.isInterface()
				|| Modifier.isAbstract(_beanClass.getModifiers())) {
			throw new DescriptorException("Bean (" + _beanClass
					+ ") is not a non-abstract class");
		}

		Field[] fields = beanClass.getDeclaredFields();
		for (Field field : fields) {

			Configured configuredAnnotation = field
					.getAnnotation(Configured.class);
			Provided providedAnnotation = field.getAnnotation(Provided.class);

			if (configuredAnnotation != null && providedAnnotation != null) {
				throw new DescriptorException(
						"The field "
								+ field
								+ " is annotated with both @Configured and @Provided, which are mutually exclusive.");
			}

			if (configuredAnnotation != null) {
				if (!field.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Configured field: {}",
							field);
				}
				_configuredProperties.add(new ConfiguredPropertyDescriptorImpl(
						field));
			}

			if (providedAnnotation != null) {
				if (!field.isAnnotationPresent(Inject.class)) {
					logger.warn(
							"No @Inject annotation found for @Provided field: {}",
							field);
				}
				_providedProperties.add(new ProvidedPropertyDescriptorImpl(
						field));
			}
		}

		if (ReflectionUtils.isCloseable(beanClass)) {
			try {
				Method method = beanClass.getMethod("close", new Class<?>[0]);
				_closeMethods.add(new CloseMethodDescriptorImpl(method));
			} catch (NoSuchMethodException e) {
				// This is impossible since all closeable's have a no-arg close
				// method
				assert false;
			}
		}

		Method[] methods = beanClass.getDeclaredMethods();
		for (Method method : methods) {
			Initialize initializeAnnotation = method
					.getAnnotation(Initialize.class);

			// @PostConstruct is a valid substitution for @Initialize
			PostConstruct postConstructAnnotation = method
					.getAnnotation(PostConstruct.class);
			if (initializeAnnotation != null || postConstructAnnotation != null) {
				_initializeMethods.add(new InitializeMethodDescriptorImpl(
						method));
			}

			Close closeAnnotation = method.getAnnotation(Close.class);

			// @PreDestroy is a valid substitution for @Close
			PreDestroy preDestroyAnnotation = method
					.getAnnotation(PreDestroy.class);

			if (closeAnnotation != null || preDestroyAnnotation != null) {
				_closeMethods.add(new CloseMethodDescriptorImpl(method));
			}
		}

		if (requireInputColumns) {
			int numConfiguredColumns = 0;
			int numConfiguredColumnArrays = 0;
			for (ConfiguredPropertyDescriptor cd : _configuredProperties) {
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
	}

	@Override
	public Class<?> getBeanClass() {
		return _beanClass;
	}

	@Override
	public ConfiguredPropertyDescriptor getConfiguredProperty(
			String configuredName) {
		for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	@Override
	public ConfiguredPropertyDescriptor getConfiguredPropertyForInput() {
		Set<ConfiguredPropertyDescriptor> descriptors = _configuredProperties;
		for (ConfiguredPropertyDescriptor configuredDescriptor : descriptors) {
			if (configuredDescriptor.isInputColumn()) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	@Override
	public DataTypeFamily getInputDataTypeFamily() {
		ConfiguredPropertyDescriptor configuredDescriptor = getConfiguredPropertyForInput();
		if (configuredDescriptor == null) {
			return DataTypeFamily.UNDEFINED;
		}
		if (configuredDescriptor.getTypeArgumentCount() == 0) {
			return DataTypeFamily.UNDEFINED;
		}
		Type typeParameter = configuredDescriptor.getTypeArgument(0);
		return DataTypeFamily.valueOf(typeParameter);
	}

	@Override
	public int hashCode() {
		return _beanClass.hashCode();
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
			return this._beanClass == that._beanClass;
		}
		return false;
	}

	@Override
	public int compareTo(BeanDescriptor o) {
		if (o == null) {
			return 1;
		}
		Class<?> otherBeanClass = o.getBeanClass();
		if (otherBeanClass == null) {
			return 1;
		}
		String thisBeanClassName = this.getBeanClass().toString();
		String thatBeanClassName = otherBeanClass.toString();
		return thisBeanClassName.compareTo(thatBeanClassName);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[beanClass="
				+ _beanClass.getName() + "]";
	}

	@Override
	public Set<CloseMethodDescriptor> getCloseMethods() {
		return Collections.unmodifiableSet(_closeMethods);
	}

	@Override
	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
		return Collections.unmodifiableSet(_configuredProperties);
	}

	@Override
	public Set<InitializeMethodDescriptor> getInitializeMethods() {
		return Collections.unmodifiableSet(_initializeMethods);
	}

	@Override
	public Set<ProvidedPropertyDescriptor> getProvidedProperties() {
		return Collections.unmodifiableSet(_providedProperties);
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _beanClass.getAnnotation(annotationClass);
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return CollectionUtils.set(_beanClass.getAnnotations());
	}
}
