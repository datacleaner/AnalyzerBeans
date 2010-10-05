package org.eobjects.analyzer.descriptors;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class AnnotationBasedTransformerBeanDescriptor<T extends Transformer<?>>
		extends AbstractBeanDescriptor<T> implements
		TransformerBeanDescriptor<T> {

	private final String _displayName;

	public static <T extends Transformer<?>> AnnotationBasedTransformerBeanDescriptor<T> create(
			Class<T> transformerClass) {
		return new AnnotationBasedTransformerBeanDescriptor<T>(transformerClass);
	}

	private AnnotationBasedTransformerBeanDescriptor(Class<T> transformerClass)
			throws DescriptorException {
		super(transformerClass, true);

		if (!ReflectionUtils.is(transformerClass, Transformer.class)) {
			throw new DescriptorException(transformerClass
					+ " does not implement " + Transformer.class.getName());
		}

		TransformerBean transformerAnnotation = transformerClass
				.getAnnotation(TransformerBean.class);
		if (transformerAnnotation == null) {
			throw new DescriptorException(transformerClass
					+ " doesn't implement the TransformerBean annotation");
		}

		String displayName = transformerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(
					transformerClass.getSimpleName(), false);
		}
		_displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public DataTypeFamily getOutputDataTypeFamily() {
		Type[] interfaces = getBeanClass().getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType() == Transformer.class) {
					Class<?> typeParameter = ReflectionUtils.getTypeParameter(
							pType, 0);
					return DataTypeFamily.valueOf(typeParameter);
				}
			}
		}
		return DataTypeFamily.UNDEFINED;
	}
}
