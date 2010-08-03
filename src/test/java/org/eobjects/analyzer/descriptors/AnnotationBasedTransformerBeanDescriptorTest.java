package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.ConcatenatorTransformer;
import org.eobjects.analyzer.beans.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.TokenizerTransformer;
import org.eobjects.analyzer.data.DataTypeFamily;

import junit.framework.TestCase;

public class AnnotationBasedTransformerBeanDescriptorTest extends TestCase {

	public void testGetDataTypeFamilies() throws Exception {
		TransformerBeanDescriptor descriptor = new AnnotationBasedTransformerBeanDescriptor(
				TokenizerTransformer.class);
		assertEquals(DataTypeFamily.STRING, descriptor.getInputDataTypeFamily());
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());

		descriptor = new AnnotationBasedTransformerBeanDescriptor(
				ConvertToNumberTransformer.class);
		assertEquals(DataTypeFamily.STRING, descriptor.getInputDataTypeFamily());
		assertEquals(DataTypeFamily.NUMBER,
				descriptor.getOutputDataTypeFamily());

		descriptor = new AnnotationBasedTransformerBeanDescriptor(
				ConvertToStringTransformer.class);
		assertEquals(DataTypeFamily.UNDEFINED,
				descriptor.getInputDataTypeFamily());
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());
	}
	
	public void testConcatenator() throws Exception {
		TransformerBeanDescriptor descriptor = new AnnotationBasedTransformerBeanDescriptor(
				ConcatenatorTransformer.class);
		assertEquals(DataTypeFamily.UNDEFINED,
				descriptor.getInputDataTypeFamily());
		assertEquals(DataTypeFamily.STRING,
				descriptor.getOutputDataTypeFamily());		
	}
}
