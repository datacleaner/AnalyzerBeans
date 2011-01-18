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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class AnnotationBasedTransformerBeanDescriptor<T extends Transformer<?>> extends AbstractBeanDescriptor<T>
		implements TransformerBeanDescriptor<T> {

	private final String _displayName;

	public static <T extends Transformer<?>> AnnotationBasedTransformerBeanDescriptor<T> create(Class<T> transformerClass) {
		return new AnnotationBasedTransformerBeanDescriptor<T>(transformerClass);
	}

	private AnnotationBasedTransformerBeanDescriptor(Class<T> transformerClass) throws DescriptorException {
		super(transformerClass, false);

		if (!ReflectionUtils.is(transformerClass, Transformer.class)) {
			throw new DescriptorException(transformerClass + " does not implement " + Transformer.class.getName());
		}

		TransformerBean transformerAnnotation = transformerClass.getAnnotation(TransformerBean.class);
		if (transformerAnnotation == null) {
			throw new DescriptorException(transformerClass + " doesn't implement the TransformerBean annotation");
		}

		String displayName = transformerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(transformerClass.getSimpleName(), false);
		}
		_displayName = displayName;
		
		visitClass();
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public DataTypeFamily getOutputDataTypeFamily() {
		Type[] interfaces = getComponentClass().getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType() == Transformer.class) {
					Class<?> typeParameter = ReflectionUtils.getTypeParameter(pType, 0);
					return DataTypeFamily.valueOf(typeParameter);
				}
			}
		}
		return DataTypeFamily.UNDEFINED;
	}
}
