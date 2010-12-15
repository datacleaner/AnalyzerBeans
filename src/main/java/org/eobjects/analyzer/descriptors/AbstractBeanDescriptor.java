/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBeanDescriptor<B> extends AbstractDescriptor<B> implements BeanDescriptor<B> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBeanDescriptor.class);

	protected final Set<InitializeMethodDescriptor> _initializeMethods = new HashSet<InitializeMethodDescriptor>();
	protected final Set<ConfiguredPropertyDescriptor> _configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>();
	protected final Set<ProvidedPropertyDescriptor> _providedProperties = new HashSet<ProvidedPropertyDescriptor>();
	protected final Set<CloseMethodDescriptor> _closeMethods = new HashSet<CloseMethodDescriptor>();
	private final boolean _requireInputColumns;

	public AbstractBeanDescriptor(Class<B> beanClass, boolean requireInputColumns) {
		super(beanClass);

		_requireInputColumns = requireInputColumns;
	}

	@Override
	protected void visitClass() {
		super.visitClass();

		if (ReflectionUtils.isCloseable(getBeanClass())) {
			try {
				Method method = getBeanClass().getMethod("close", new Class<?>[0]);
				_closeMethods.add(new CloseMethodDescriptorImpl(method));
			} catch (NoSuchMethodException e) {
				// This is impossible since all closeable's have a no-arg close
				// method
				assert false;
			}
		}

		if (_requireInputColumns) {
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
				throw new DescriptorException(getBeanClass()
						+ " does not define a @Configured InputColumn or InputColumn-array");
			}
		}
	}

	@Override
	protected void visitField(Field field) {
		Configured configuredAnnotation = field.getAnnotation(Configured.class);
		Provided providedAnnotation = field.getAnnotation(Provided.class);

		if (configuredAnnotation != null && providedAnnotation != null) {
			throw new DescriptorException("The field " + field
					+ " is annotated with both @Configured and @Provided, which are mutually exclusive.");
		}

		if (configuredAnnotation != null) {
			if (!field.isAnnotationPresent(Inject.class)) {
				logger.info("No @Inject annotation found for @Configured field: {}", field);
			}
			_configuredProperties.add(new ConfiguredPropertyDescriptorImpl(field, this));
		}

		if (providedAnnotation != null) {
			if (!field.isAnnotationPresent(Inject.class)) {
				logger.info("No @Inject annotation found for @Provided field: {}", field);
			}
			_providedProperties.add(new ProvidedPropertyDescriptorImpl(field, this));
		}
	}

	@Override
	protected void visitMethod(Method method) {
		Initialize initializeAnnotation = method.getAnnotation(Initialize.class);

		// @PostConstruct is a valid substitution for @Initialize
		PostConstruct postConstructAnnotation = method.getAnnotation(PostConstruct.class);
		if (initializeAnnotation != null || postConstructAnnotation != null) {
			_initializeMethods.add(new InitializeMethodDescriptorImpl(method));
		}

		Close closeAnnotation = method.getAnnotation(Close.class);

		// @PreDestroy is a valid substitution for @Close
		PreDestroy preDestroyAnnotation = method.getAnnotation(PreDestroy.class);

		if (closeAnnotation != null || preDestroyAnnotation != null) {
			_closeMethods.add(new CloseMethodDescriptorImpl(method));
		}
	}

	@Override
	public ConfiguredPropertyDescriptor getConfiguredProperty(String configuredName) {
		for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
			if (configuredName.equals(configuredDescriptor.getName())) {
				return configuredDescriptor;
			}
		}
		return null;
	}

	@Override
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
		Set<ConfiguredPropertyDescriptor> descriptors = new TreeSet<ConfiguredPropertyDescriptor>(_configuredProperties);
		for (Iterator<ConfiguredPropertyDescriptor> it = descriptors.iterator(); it.hasNext();) {
			ConfiguredPropertyDescriptor propertyDescriptor = it.next();
			if (!propertyDescriptor.isInputColumn()) {
				it.remove();
			}
		}
		return descriptors;
	}

	@Override
	public String getDescription() {
		Description description = getAnnotation(Description.class);
		if (description == null) {
			return null;
		}
		return description.value();
	}

	@Override
	public int compareTo(BeanDescriptor<?> o) {
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
		return getBeanClass().getAnnotation(annotationClass);
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return CollectionUtils.set(getBeanClass().getAnnotations());
	}
}
