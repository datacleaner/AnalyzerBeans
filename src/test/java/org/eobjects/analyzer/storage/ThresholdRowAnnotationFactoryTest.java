package org.eobjects.analyzer.storage;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class ThresholdRowAnnotationFactoryTest extends TestCase {

	public void testMakePersistent() throws Exception {
		RowAnnotationFactory mockFactory = new InMemoryRowAnnotationFactory();

		ThresholdRowAnnotationFactory thresholdFactory = new ThresholdRowAnnotationFactory(100, mockFactory);

		RowAnnotationImpl a1 = (RowAnnotationImpl) thresholdFactory.createAnnotation();
		RowAnnotation a2 = thresholdFactory.createAnnotation();
		RowAnnotation a3 = thresholdFactory.createAnnotation();

		InputColumn<String> col1 = new MockInputColumn<String>("col1", String.class);

		for (int i = 0; i < 99; i++) {
			InputRow row = new MockInputRow(i).put(col1, "v" + i);
			thresholdFactory.annotate(row, 2, a1);
			thresholdFactory.annotate(row, 1, a2);
			thresholdFactory.annotate(row, 1, a3);
		}

		assertEquals(198, a1.getRowCount());
		assertEquals(99, a2.getRowCount());
		assertEquals(99, a3.getRowCount());

		// nothing should have happened yet
		assertEquals(0, mockFactory.getRows(a1).length);

		// add the 100th row, which will succeed the threshold
		InputRow rowNo100 = new MockInputRow(100).put(col1, "v100");
		thresholdFactory.annotate(rowNo100, 1, a1);

		assertEquals(100, mockFactory.getRows(a1).length);
		assertEquals(199, a1.getRowCount());
	}
}
