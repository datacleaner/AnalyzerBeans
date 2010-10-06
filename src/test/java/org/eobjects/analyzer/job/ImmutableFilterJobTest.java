package org.eobjects.analyzer.job;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.SingleWordFilter;
import org.eobjects.analyzer.descriptors.AnnotationBasedFilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

public class ImmutableFilterJobTest extends TestCase {

	public void testGetOutcomes() throws Exception {
		FilterBeanDescriptor<?, ?> descriptor = AnnotationBasedFilterBeanDescriptor.create(SingleWordFilter.class);
		BeanConfiguration configuration = new ImmutableBeanConfiguration(new HashMap<ConfiguredPropertyDescriptor, Object>());

		ImmutableFilterJob job = new ImmutableFilterJob(descriptor, configuration, null);
		assertNull(job.getRequirement());

		FilterOutcome[] outcomes1 = job.getOutcomes();
		assertEquals(2, outcomes1.length);
		assertEquals("Outcome[category=VALID]", outcomes1[0].toString());
		assertEquals("Outcome[category=INVALID]", outcomes1[1].toString());

		FilterOutcome[] outcomes2 = job.getOutcomes();
		assertEquals(2, outcomes2.length);
		assertEquals("Outcome[category=VALID]", outcomes2[0].toString());
		assertEquals("Outcome[category=INVALID]", outcomes2[1].toString());

		// the arrays are not the same, but their contents are equal
		assertNotSame(outcomes1, outcomes2);

		assertNotSame(outcomes1[0], outcomes2[0]);
		assertEquals(outcomes1[0], outcomes2[0]);
		assertNotSame(outcomes1[1], outcomes2[1]);
		assertEquals(outcomes1[1], outcomes2[1]);
	}
}
