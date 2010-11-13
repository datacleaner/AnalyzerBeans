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
package org.eobjects.analyzer.beans.valuedist;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.AnnotationBasedAnalyzerBeanDescriptor;

import dk.eobjects.metamodel.schema.MutableColumn;

public class ValueDistributionAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor<?> desc = AnnotationBasedAnalyzerBeanDescriptor.create(ValueDistributionAnalyzer.class);
		assertEquals(true, desc.isRowProcessingAnalyzer());
		assertEquals(false, desc.isExploringAnalyzer());
		assertEquals(0, desc.getInitializeMethods().size());
		assertEquals(4, desc.getConfiguredProperties().size());
		assertEquals(1, desc.getProvidedProperties().size());
		assertEquals("Value distribution", desc.getDisplayName());
	}

	public void testGetNullAndUniqueCount() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(new MetaModelInputColumn(new MutableColumn("col")),
				true, null, null);
		vd.setValueDistribution(new HashMap<String, Integer>());

		assertEquals(0, vd.getResult().getUniqueCount());
		assertEquals(0, vd.getResult().getNullCount());

		vd.runInternal("hello", 1);
		assertEquals(1, vd.getResult().getUniqueCount());

		vd.runInternal("world", 1);
		assertEquals(2, vd.getResult().getUniqueCount());

		vd.runInternal("foobar", 2);
		assertEquals(2, vd.getResult().getUniqueCount());

		vd.runInternal("world", 1);
		assertEquals(1, vd.getResult().getUniqueCount());

		vd.runInternal("hello", 3);
		assertEquals(0, vd.getResult().getUniqueCount());

		vd.runInternal(null, 1);
		assertEquals(0, vd.getResult().getUniqueCount());
		assertEquals(1, vd.getResult().getNullCount());

		vd.runInternal(null, 3);
		assertEquals(0, vd.getResult().getUniqueCount());
		assertEquals(4, vd.getResult().getNullCount());
	}

	public void testGetValueDistribution() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(new MetaModelInputColumn(new MutableColumn("col")),
				true, null, null);
		vd.setValueDistribution(new HashMap<String, Integer>());

		vd.runInternal("hello", 1);
		vd.runInternal("hello", 1);
		vd.runInternal("world", 3);

		ValueCountList topValues = vd.getResult().getTopValues();
		assertEquals(2, topValues.getActualSize());
		assertEquals("[world->3]", topValues.getValueCounts().get(0).toString());
		assertEquals("[hello->2]", topValues.getValueCounts().get(1).toString());

		assertEquals(0, vd.getResult().getNullCount());
		assertEquals(0, vd.getResult().getUniqueCount());
	}

}
