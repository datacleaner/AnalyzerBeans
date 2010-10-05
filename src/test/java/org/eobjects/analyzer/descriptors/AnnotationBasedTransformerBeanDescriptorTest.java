package org.eobjects.analyzer.descriptors;

import java.util.Set;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.beans.transform.ConcatenatorTransformer;
import org.eobjects.analyzer.data.DataTypeFamily;

import junit.framework.TestCase;

public class AnnotationBasedTransformerBeanDescriptorTest extends TestCase {

	public void testGetDataTypeFamilies() throws Exception {
		TransformerBeanDescriptor<?> descriptor = AnnotationBasedTransformerBeanDescriptor
				.create(TokenizerTransformer.class);
		assertEquals(DataTypeFamily.STRING, getDataTypeFamily(descriptor));
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());

		descriptor = AnnotationBasedTransformerBeanDescriptor
				.create(ConvertToNumberTransformer.class);
		assertEquals(DataTypeFamily.UNDEFINED, getDataTypeFamily(descriptor));
		assertEquals(DataTypeFamily.NUMBER,
				descriptor.getOutputDataTypeFamily());

		descriptor = AnnotationBasedTransformerBeanDescriptor
				.create(ConvertToStringTransformer.class);
		assertEquals(DataTypeFamily.UNDEFINED, getDataTypeFamily(descriptor));
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());
	}

	private DataTypeFamily getDataTypeFamily(
			TransformerBeanDescriptor<?> descriptor) {
		Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor
				.getConfiguredPropertiesForInput();
		assertEquals(1, configuredProperties.size());
		ConfiguredPropertyDescriptor propertyDescriptor = configuredProperties
				.iterator().next();
		return propertyDescriptor.getInputColumnDataTypeFamily();
	}

	public void testConcatenator() throws Exception {
		TransformerBeanDescriptor<?> descriptor = AnnotationBasedTransformerBeanDescriptor
				.create(ConcatenatorTransformer.class);
		assertEquals(DataTypeFamily.UNDEFINED, getDataTypeFamily(descriptor));
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());
	}
}
