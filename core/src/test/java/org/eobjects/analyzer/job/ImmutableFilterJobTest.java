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
package org.eobjects.analyzer.job;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;

public class ImmutableFilterJobTest extends TestCase {

	public void testGetOutcomes() throws Exception {
		FilterBeanDescriptor<?, ?> descriptor = Descriptors.ofFilter(MaxRowsFilter.class);
		BeanConfiguration configuration = new ImmutableBeanConfiguration(new HashMap<ConfiguredPropertyDescriptor, Object>());

		ImmutableFilterJob job = new ImmutableFilterJob("foo", descriptor, configuration, null);
		assertEquals("foo", job.getName());
		assertEquals(null, job.getComponentRequirement());

		FilterOutcome[] outcomes1 = job.getOutcomes();
		assertEquals(2, outcomes1.length);
		assertEquals("FilterOutcome[category=VALID]", outcomes1[0].toString());
		assertEquals("FilterOutcome[category=INVALID]", outcomes1[1].toString());

		FilterOutcome[] outcomes2 = job.getOutcomes();
		assertEquals(2, outcomes2.length);
		assertEquals("FilterOutcome[category=VALID]", outcomes2[0].toString());
		assertEquals("FilterOutcome[category=INVALID]", outcomes2[1].toString());

		// the arrays are not the same, but their contents are equal
		assertNotSame(outcomes1, outcomes2);

		assertNotSame(outcomes1[0], outcomes2[0]);
		assertEquals(outcomes1[0], outcomes2[0]);
		assertNotSame(outcomes1[1], outcomes2[1]);
		assertEquals(outcomes1[1], outcomes2[1]);
	}
}
