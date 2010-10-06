package org.eobjects.analyzer.descriptors;

import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.NotNullFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;

public class AnnotationBasedFilterDescriptorTest extends TestCase {

	FilterBeanDescriptor<NotNullFilter, ValidationCategory> desc = AnnotationBasedFilterBeanDescriptor
			.create(NotNullFilter.class);

	public void testGetCategoryEnum() throws Exception {
		Class<ValidationCategory> categoryEnum = desc.getCategoryEnum();

		assertEquals(ValidationCategory.class, categoryEnum);
	}

	public void testGetCategoryNames() throws Exception {
		Set<String> categoryNames = desc.getCategoryNames();
		categoryNames = new TreeSet<String>(categoryNames);
		assertEquals("[INVALID, VALID]", categoryNames.toString());
	}
}
