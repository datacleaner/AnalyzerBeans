package org.eobjects.analyzer.beans.valuedist;

import java.util.HashMap;

import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;

import junit.framework.TestCase;

public class ValueDistributionAnalyzerTest extends TestCase {

	public void testGetNullAndUniqueCount() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer();
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
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer();
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
