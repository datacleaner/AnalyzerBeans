package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.beans.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerBeanDescriptor extends AbstractBeanDescriptor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String displayName;

	private List<CloseDescriptor> closeDescriptors = new LinkedList<CloseDescriptor>();
	private List<ConfiguredDescriptor> configuredDescriptors = new LinkedList<ConfiguredDescriptor>();
	private List<InitializeDescriptor> initializeDescriptors = new LinkedList<InitializeDescriptor>();
	private List<ProvidedDescriptor> providedDescriptors = new LinkedList<ProvidedDescriptor>();

	public TransformerBeanDescriptor(Class<?> transformerClass)
			throws DescriptorException {
		super(transformerClass);

		if (!AnnotationHelper.is(transformerClass, Transformer.class)) {
			throw new DescriptorException(transformerClass
					+ " does not implement " + Transformer.class.getName());
		}

		TransformerBean transformerAnnotation = transformerClass
				.getAnnotation(TransformerBean.class);
		if (transformerAnnotation == null) {
			throw new DescriptorException(transformerClass
					+ " doesn't implement the TransformerBean annotation");
		}
		displayName = transformerAnnotation.value();
		if (displayName == null || displayName.trim().equals("")) {
			displayName = AnnotationHelper.explodeCamelCase(
					transformerClass.getSimpleName(), false);
		}

		Field[] fields = transformerClass.getDeclaredFields();
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

		if (AnnotationHelper.isCloseable(transformerClass)) {
			try {
				Method method = transformerClass.getMethod("close",
						new Class<?>[0]);
				closeDescriptors.add(new CloseDescriptor(method));
			} catch (NoSuchMethodException e) {
				// This is impossible since all closeable's have a no-arg close
				// method
				assert false;
			}
		}

		Method[] methods = transformerClass.getDeclaredMethods();
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
					transformerClass
							+ " does not define a @Configured InputColumn or InputColumn-array");
		}
		if (totalColumns > 1) {
			throw new DescriptorException(
					transformerClass
							+ " defines multiple @Configured InputColumns, cannot determine which one to use for transformation");
		}

		// Make the descriptor lists read-only
		closeDescriptors = Collections.unmodifiableList(closeDescriptors);
		configuredDescriptors = Collections
				.unmodifiableList(configuredDescriptors);
		initializeDescriptors = Collections
				.unmodifiableList(initializeDescriptors);
		providedDescriptors = Collections.unmodifiableList(providedDescriptors);
	}

	public String getDisplayName() {
		return displayName;
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

	public List<CloseDescriptor> getCloseDescriptors() {
		return closeDescriptors;
	}
}