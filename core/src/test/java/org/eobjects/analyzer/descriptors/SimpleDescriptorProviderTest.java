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

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.mock.AnalyzerMock;
import org.eobjects.analyzer.beans.mock.TransformerMock;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;

public class SimpleDescriptorProviderTest extends TestCase {

	public void testSetBeanClassNames() throws Exception {
		SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);

		assertNull(descriptorProvider.getAnalyzerBeanDescriptorForClass(ValueDistributionAnalyzer.class));
		assertNull(descriptorProvider.getTransformerBeanDescriptorForClass(ConvertToBooleanTransformer.class));

		descriptorProvider.setAnalyzerClassNames(Arrays.asList(ValueDistributionAnalyzer.class.getName(),
				AnalyzerMock.class.getName()));

		assertEquals(2, descriptorProvider.getAnalyzerBeanDescriptors().size());

		descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName(),
				ConvertToDateTransformer.class.getName()));

		assertEquals(2, descriptorProvider.getTransformerBeanDescriptors().size());

		descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName()));

		assertEquals(2, descriptorProvider.getTransformerBeanDescriptors().size());

		assertEquals(
				"AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer]",
				descriptorProvider.getAnalyzerBeanDescriptorForClass(ValueDistributionAnalyzer.class).toString());

		assertEquals(
				"AnnotationBasedTransformerBeanDescriptor[org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer]",
				descriptorProvider.getTransformerBeanDescriptorForClass(ConvertToBooleanTransformer.class).toString());
	}

	public void testGetBeanByAlias() throws Exception {
		SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);
		descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(TransformerMock.class));

		TransformerBeanDescriptor<?> descriptor1 = descriptorProvider
				.getTransformerBeanDescriptorByDisplayName("Transformer mock");
		TransformerBeanDescriptor<?> descriptor2 = descriptorProvider
				.getTransformerBeanDescriptorByDisplayName("Mock transformer");

		assertSame(descriptor1, descriptor2);
	}
}
