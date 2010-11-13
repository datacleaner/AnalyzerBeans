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
package org.eobjects.analyzer.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.descriptors.AbstractDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptorImpl;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptorImpl;

/**
 * A descriptor for any class that is annotated with @Configured and @Initialize
 * 
 * @author Kasper Sørensen
 */
public class CustomComponentDescriptor<B> extends AbstractDescriptor<B> {
	
	private List<ConfiguredPropertyDescriptor> configuredProperties = new ArrayList<ConfiguredPropertyDescriptor>();
	private List<InitializeMethodDescriptor> initializeMethods = new ArrayList<InitializeMethodDescriptor>();

	public CustomComponentDescriptor(Class<B> beanClass) {
		super(beanClass);
		visitClass();
	}

	@Override
	protected void visitField(Field field) {
		if (field.isAnnotationPresent(Configured.class)) {
			ConfiguredPropertyDescriptor cpd = new ConfiguredPropertyDescriptorImpl(field, null);
			configuredProperties.add(cpd);
		}
	}

	@Override
	protected void visitMethod(Method method) {
		if (method.isAnnotationPresent(Initialize.class)) {
			InitializeMethodDescriptor imd = new InitializeMethodDescriptorImpl(method);
			initializeMethods.add(imd);
		}
	}

	public ConfiguredPropertyDescriptor getConfiguredProperty(String propertyName) {
		for (ConfiguredPropertyDescriptor property : configuredProperties) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}
		return null;
	}

	public List<InitializeMethodDescriptor> getInitializeMethods() {
		return initializeMethods;
	}
	
	public List<ConfiguredPropertyDescriptor> getConfiguredProperties() {
		return configuredProperties;
	}
}
