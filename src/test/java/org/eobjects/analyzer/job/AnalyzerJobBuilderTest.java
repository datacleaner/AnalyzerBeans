package org.eobjects.analyzer.job;

import org.eobjects.analyzer.beans.mock.ExploringBeanMock;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.TransformedInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;

import junit.framework.TestCase;

public class AnalyzerJobBuilderTest extends TestCase {

	public void testExploringAnalyzerConfiguration() throws Exception {
		AnalyzerBeanDescriptor descriptor = new AnalyzerBeanDescriptor(
				ExploringBeanMock.class);
		assertTrue(descriptor.isExploringAnalyzer());
		assertFalse(descriptor.isRowProcessingAnalyzer());

		AnalyzerJobBuilder ajb = new AnalyzerJobBuilder(descriptor, null);
		assertFalse(ajb.isInputColumnsConfigurable());

		try {
			TransformedInputColumn<String> inputColumn = new TransformedInputColumn<String>(
					"haps", DataTypeFamily.STRING, new PrefixedIdGenerator(
							"prefix"));
			ajb.addInputColumn(inputColumn);
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals(
					"This analyzer doesn't support InputColumn configuration",
					e.getMessage());
		}
	}
}
