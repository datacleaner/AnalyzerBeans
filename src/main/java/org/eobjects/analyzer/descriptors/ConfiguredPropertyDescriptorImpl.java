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

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class ConfiguredPropertyDescriptorImpl extends AbstractPropertyDescriptor implements
		ConfiguredPropertyDescriptor {

	public ConfiguredPropertyDescriptorImpl(Field field, BeanDescriptor<?> beanDescriptor) throws DescriptorException {
		super(field, beanDescriptor);
	}

	@Override
	public String getName() {
		Configured configured = getAnnotation(Configured.class);
		if (configured != null) {
			String value = configured.value();
			if (value != null && value.length() > 0) {
				return value;
			}
		}
		return ReflectionUtils.explodeCamelCase(super.getName(), true);
	}

	@Override
	public String getDescription() {
		Description desc = getAnnotation(Description.class);
		if (desc == null) {
			return null;
		}
		return desc.value();
	}

	@Override
	public boolean isInputColumn() {
		Class<?> baseType = getBaseType();
		boolean result = ReflectionUtils.isInputColumn(baseType);
		return result;
	}

	@Override
	public boolean isRequired() {
		Configured configured = getAnnotation(Configured.class);
		if (configured == null) {
			return true;
		}
		return configured.required();
	}

	@Override
	public DataTypeFamily getInputColumnDataTypeFamily() {
		if (isInputColumn()) {
			int count = getTypeArgumentCount();
			if (count == 0) {
				return DataTypeFamily.UNDEFINED;
			}
			Type typeArgument = getTypeArgument(0);
			return DataTypeFamily.valueOf(typeArgument);
		}
		return null;
	}
}
