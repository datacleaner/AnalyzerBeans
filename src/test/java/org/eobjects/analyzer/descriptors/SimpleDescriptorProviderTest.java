package org.eobjects.analyzer.descriptors;

import java.util.Arrays;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;

import junit.framework.TestCase;

public class SimpleDescriptorProviderTest extends TestCase {

	public void testSetBeanClassNames() throws Exception {
		SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();

		assertNull(descriptorProvider.getAnalyzerBeanDescriptorForClass(ValueDistributionAnalyzer.class));
		assertNull(descriptorProvider.getTransformerBeanDescriptorForClass(ConvertToBooleanTransformer.class));

		descriptorProvider.setAnalyzerClassNames(Arrays.asList(ValueDistributionAnalyzer.class.getName(),
				StringAnalyzer.class.getName()));

		assertEquals(2, descriptorProvider.getAnalyzerBeanDescriptors().size());

		descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName(),
				ConvertToDateTransformer.class.getName()));

		assertEquals(2, descriptorProvider.getTransformerBeanDescriptors().size());

		descriptorProvider.setTransformerClassNames(Arrays.asList(ConvertToBooleanTransformer.class.getName()));

		assertEquals(2, descriptorProvider.getTransformerBeanDescriptors().size());

		assertEquals(
				"AnnotationBasedAnalyzerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer]",
				descriptorProvider.getAnalyzerBeanDescriptorForClass(ValueDistributionAnalyzer.class).toString());

		assertEquals(
				"AnnotationBasedTransformerBeanDescriptor[beanClass=org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer]",
				descriptorProvider.getTransformerBeanDescriptorForClass(ConvertToBooleanTransformer.class).toString());
	}
}
