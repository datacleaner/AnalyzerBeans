package org.eobjects.analyzer.descriptors;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.beans.Transformer;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.util.ReflectionUtils;

public class TransformerBeanDescriptor extends AbstractBeanDescriptor {

	private String displayName;

	public TransformerBeanDescriptor(Class<?> transformerClass)
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
		
		displayName = transformerAnnotation.value();
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = ReflectionUtils.explodeCamelCase(
					transformerClass.getSimpleName(), false);
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public DataTypeFamily getOutputDataTypeFamily() {
		Type[] interfaces = getBeanClass().getGenericInterfaces();
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