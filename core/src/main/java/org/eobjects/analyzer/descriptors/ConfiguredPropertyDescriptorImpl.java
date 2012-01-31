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

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.util.ReflectionUtils;

final class ConfiguredPropertyDescriptorImpl extends AbstractPropertyDescriptor implements
		ConfiguredPropertyDescriptor {
	
	private static final long serialVersionUID = 1L;

	protected ConfiguredPropertyDescriptorImpl(Field field, ComponentDescriptor<?> componentDescriptor)
			throws DescriptorException {
		super(field, componentDescriptor);
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

	@SuppressWarnings("deprecation")
	@Override
	public org.eobjects.analyzer.data.DataTypeFamily getInputColumnDataTypeFamily() {
		if (isInputColumn()) {
			int count = getTypeArgumentCount();
			if (count == 0) {
				return org.eobjects.analyzer.data.DataTypeFamily.UNDEFINED;
			}
			Type typeArgument = getTypeArgument(0);
			return org.eobjects.analyzer.data.DataTypeFamily.valueOf(typeArgument);
		}
		return null;
	}

	@Override
	public int compareTo(PropertyDescriptor o) {
		Configured conf1 = getAnnotation(Configured.class);
		final int order1 = conf1.order();
		Configured conf2 = o.getAnnotation(Configured.class);
		final int order2;
		if (conf2 == null) {
			order2 = Integer.MAX_VALUE;
		} else {
			order2 = conf2.order();
		}
		int diff = order1 - order2;
		if (diff == 0) {
			return super.compareTo(o);
		}
		return diff;
	}

	@Override
	public Class<? extends Converter<?>> getCustomConverter() {
		Convertable convertable = getAnnotation(Convertable.class);
		if (convertable != null) {
			return convertable.value();
		}
		return null;
	}

	@Override
	public String[] getAliases() {
		Alias alias = getAnnotation(Alias.class);
		if (alias == null) {
			return new String[0];
		}
		return alias.value();
	}
}
