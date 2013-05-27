package org.eobjects.analyzer.beans.filter;

import java.io.File;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.metamodel.util.FileHelper;

import junit.framework.TestCase;

public class CaptureChangedRecordsFilterTest extends TestCase {

    public void testInitializeAndClose() throws Exception {
        File file = new File("target/test_capture_changed_records_filter.properties");
        file.delete();

        MockInputColumn<Object> column = new MockInputColumn<Object>("Foo LastModified");

        CaptureChangedRecordsFilter filter = new CaptureChangedRecordsFilter();

        filter.captureStateFile = file;
        filter.lastModifiedColumn = column;
        filter.initialize();

        assertFalse(file.exists());

        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-02")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-03")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-01")));

        filter.close();

        assertTrue(file.exists());

        String[] lines = FileHelper.readFileAsString(file).split("\n");

        // the first line is a comment with a date of writing
        assertEquals(2, lines.length);

        assertEquals("Foo\\ LastModified.GreatestLastModifiedTimestamp=1357167600000", lines[1]);

        filter = new CaptureChangedRecordsFilter();

        filter.captureStateFile = file;
        filter.lastModifiedColumn = column;
        filter.initialize();
        
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "2013-01-02")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "2013-01-03")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "2013-01-01")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-04")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-05")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "2013-01-08")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "2012-12-01")));
        
        filter.close();
        
        lines = FileHelper.readFileAsString(file).split("\n");

        // the first line is a comment with a date of writing
        assertEquals(2, lines.length);

        assertEquals("Foo\\ LastModified.GreatestLastModifiedTimestamp=1357599600000", lines[1]);
    }
}
