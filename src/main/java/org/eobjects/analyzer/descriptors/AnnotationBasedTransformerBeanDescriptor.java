package org.eobjects.analyzer.descriptors;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public class AnnotationBasedTransformerBeanDescriptor extends
		AbstractBeanDescriptor implements TransformerBeanDescriptor {

	private String _displayName;

	public AnnotationBasedTransformerBeanDescriptor(Class<?> transformerClass)
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

		_displayName = transformerAnnotation.value();
		if (_displayName == null || _displayName.trim().length() == 0) {
			_displayName = ReflectionUtils.explodeCamelCase(
					transformerClass.getSimpleName(), false);
		}
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
