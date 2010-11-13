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
