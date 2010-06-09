package org.eobjects.analyzer.beans;

import java.util.HashMap;

import junit.framework.TestCase;

public class ValueDistributionAnalyzerTest extends TestCase {

	public void testGetNullAndUniqueCount() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer();
		vd.setValueDistribution(new HashMap<String, Long>());

		assertEquals(0, vd.getUniqueCount());
		assertEquals(0, vd.getNullCount());

		vd.runInternal("hello", 1);
		assertEquals(1, vd.getUniqueCount());

		vd.runInternal("world", 1);
		assertEquals(2, vd.getUniqueCount());

		vd.runInternal("foobar", 2);
		assertEquals(2, vd.getUniqueCount());

		vd.runInternal("world", 1);
		assertEquals(1, vd.getUniqueCount());

		vd.runInternal("hello", 3);
		assertEquals(0, vd.getUniqueCount());

		vd.runInternal(null, 1);
		assertEquals(0, vd.getUniqueCount());
		assertEquals(1, vd.getNullCount());

		vd.runInternal(null, 3);
		assertEquals(0, vd.getUniqueCount());
		assertEquals(4, vd.getNullCount());
	}

	public void testGetValueDistribution() throws Exception {
		ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer();
		vd.setValueDistribution(new HashMap<String, Long>());

		vd.runInternal("hello", 1);
		vd.runInternal("hello", 1);
		vd.runInternal("world", 3);

		assertEquals(2, vd.getValueDistribution().size());
		assertEquals(2, vd.getValueDistribution().get("hello").intValue());
		assertEquals(3, vd.getValueDistribution().get("world").intValue());
		assertNull(vd.getValueDistribution().get("foo"));
		assertNull(vd.getValueDistribution().get(null));

		assertEquals(0, vd.getNullCount());
		assertEquals(0, vd.getUniqueCount());
	}

}
