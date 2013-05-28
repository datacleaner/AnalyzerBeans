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
package org.eobjects.analyzer.beans.filter;

import java.io.File;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.FileResource;

import junit.framework.TestCase;

public class CaptureChangedRecordsFilterTest extends TestCase {

    public void testInitializeAndClose() throws Exception {
        File file = new File("target/test_capture_changed_records_filter.properties");
        file.delete();

        MockInputColumn<Object> column = new MockInputColumn<Object>("Foo LastModified");

        CaptureChangedRecordsFilter filter = new CaptureChangedRecordsFilter();

        filter.captureStateFile = new FileResource(file);
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

        filter.captureStateFile = new FileResource(file);
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
