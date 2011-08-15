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

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A descriptor for simple components. Simple components covers reference data
 * types (Dictionary, SynonymCatalog, StringPattern) as well as custom
 * configuration components.
 * 
 * Simple components support the @Configured, @Initialize and @Close annotations
 * as well as the Closeable interface.
 * 
 * @see Initialize
 * @see Close
 * @see Configured
 * @see Closeable
 * 
 * @author Kasper Sørensen
 */
class SimpleComponentDescriptor<B> extends AbstractDescriptor<B> implements ComponentDescriptor<B> {

	private static final Logger logger = LoggerFactory.getLogger(SimpleComponentDescriptor.class);

	protected final Set<ConfiguredPropertyDescriptor> _configuredProperties;;
	protected final Set<InitializeMethodDescriptor> _initializeMethods;
	protected final Set<CloseMethodDescriptor> _closeMethods;

	/**
	 * Constructor for inheriting from SimpleComponentDescriptor
	 * 
	 * @param beanClass
	 */
	protected SimpleComponentDescriptor(Class<B> beanClass) {
		this(beanClass, false);
	}

	protected SimpleComponentDescriptor(final Class<B> beanClass, final boolean initialize) {
		super(beanClass);
		_configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>();
		_initializeMethods = new HashSet<InitializeMethodDescriptor>();
		_closeMethods = new HashSet<CloseMethodDescriptor>();
		if (initialize) {
			visitClass();
		}
	}

	@Override
	protected void visitClass() {
		super.visitClass();

		if (ReflectionUtils.isCloseable(getComponentClass())) {
			try {
				Method method = getComponentClass().getMethod("close", new Class<?>[0]);
				CloseMethodDescriptorImpl cmd = new CloseMethodDescriptorImpl(method);
				_closeMethods.add(cmd);
			} catch (Exception e) {
				// This should be impossible since all closeable's have a no-arg
				// close() method
				logger.error("Unexpected exception while getting close() method from Closeable", e);
				assert false;
			}
		}
	}

	@Override
	protected void visitField(Field field) {
		Configured configuredAnnotation = field.getAnnotation(Configured.class);
		if (configuredAnnotation != null) {
			if (!field.isAnnotationPresent(Inject.class)) {
				logger.debug("No @Inject annotation found for @Configured field: {}", field);
			}
			_configuredProperties.add(new ConfiguredPropertyDescriptorImpl(field, this));
		}

		if (field.isAnnotationPresent(Configured.class)) {
			ConfiguredPropertyDescriptor cpd = new ConfiguredPropertyDescriptorImpl(field, this);
			_configuredProperties.add(cpd);
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

	public Set<InitializeMethodDescriptor> getInitializeMethods() {
		return Collections.unmodifiableSet(_initializeMethods);
	}

	public Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
		return Collections.unmodifiableSet(_configuredProperties);
	}

	public Set<CloseMethodDescriptor> getCloseMethods() {
		return Collections.unmodifiableSet(_closeMethods);
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
	public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays) {
		Set<ConfiguredPropertyDescriptor> set = new TreeSet<ConfiguredPropertyDescriptor>();
		for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
			final boolean include;
			if (includeArrays) {
				include = ReflectionUtils.is(configuredDescriptor.getBaseType(), type);
			} else {
				Class<?> baseType = configuredDescriptor.getType();
				if (baseType.isArray() == type.isArray()) {
					include = ReflectionUtils.is(baseType, type);
				} else {
					include = false;
				}
			}
			if (include) {
				set.add(configuredDescriptor);
			}
		}
		return set;
	}

	@Override
	public int compareTo(ComponentDescriptor<?> o) {
		if (o == null) {
			return 1;
		}
		Class<?> otherBeanClass = o.getComponentClass();
		if (otherBeanClass == null) {
			return 1;
		}
		String thisBeanClassName = this.getComponentClass().toString();
		String thatBeanClassName = otherBeanClass.toString();
		return thisBeanClassName.compareTo(thatBeanClassName);
	}
}
