package org.eobjects.analyzer.storage;

import org.easymock.EasyMock;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class ThresholdRowAnnotationFactoryTest extends TestCase {

	public void testMakePersistent() throws Exception {
		RowAnnotationFactory mockFactory = EasyMock.createMock(RowAnnotationFactory.class);

		ThresholdRowAnnotationFactory thresholdFactory = new ThresholdRowAnnotationFactory(100, mockFactory);

		RowAnnotation a1 = thresholdFactory.createAnnotation();
		RowAnnotation a2 = thresholdFactory.createAnnotation();
		RowAnnotation a3 = thresholdFactory.createAnnotation();

		InputColumn<String> col1 = new MockInputColumn<String>("col1", String.class);

		for (int i = 0; i < 99; i++) {
			InputRow row = new MockInputRow(1).put(col1, "v" + i);
			thresholdFactory.annotate(row, 2, a1);
			thresholdFactory.annotate(row, 1, a2);
			thresholdFactory.annotate(row, 1, a3);
		}

		assertEquals(198, a1.getRowCount());
		assertEquals(99, a2.getRowCount());
		assertEquals(99, a3.getRowCount());

		// nothing should have happened yet
		EasyMock.replay(mockFactory);
		EasyMock.verify(mockFactory);
		EasyMock.reset(mockFactory);

		InputRow rowNo100 = new MockInputRow(1).put(col1, "v100");
		
		InputRow[] rows = thresholdFactory.getRows(a1);
		for (InputRow row : rows) {
			mockFactory.annotate(row, 1, a1);
		}
		mockFactory.annotate(rowNo100, 1, a1);
		
		EasyMock.replay(mockFactory);

		thresholdFactory.annotate(rowNo100, 1, a1);

		EasyMock.verify(mockFactory);

		assertEquals(199, a1.getRowCount());
	}
}
