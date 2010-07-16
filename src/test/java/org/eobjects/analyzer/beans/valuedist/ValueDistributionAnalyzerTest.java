package org.eobjects.analyzer.beans.valuedist;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

import dk.eobjects.metamodel.schema.Column;

public class ValueDistributionAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		AnalyzerBeanDescriptor desc = new AnalyzerBeanDescriptor(
				ValueDistributionAnalyzer.class);
		assertEquals(true, desc.isRowProcessingAnalyzer());
		assertEquals(false, desc.isExploringAnalyzer());
		assertEquals(0, desc.getInitializeDescriptors().size());
		assertEquals(4, desc.getConfiguredDescriptors().size());
		assertEquals(1, desc.getProvidedDescriptors().size());
		assertEquals(1, desc.getResultDescriptors().size());
		assertEquals("Value distribution", desc.getDisplayName());
	}

	public void testGetNullAndUniqueCount() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
				new MetaModelInputColumn(new Column("col")), true, null, null);
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
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
				new MetaModelInputColumn(new Column("col")), true, null, null);
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
