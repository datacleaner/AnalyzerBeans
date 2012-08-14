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
package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import junit.framework.TestCase;

public class CompletenessAnalyzerTest extends TestCase {

    public void testSimpleScenario() throws Exception {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();

        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<String>("bar");
        final InputColumn<?> col3 = new MockInputColumn<String>("baz");

        final CompletenessAnalyzer analyzer = new CompletenessAnalyzer();
        analyzer._annotationFactory = annotationFactory;
        analyzer._invalidRecords = annotationFactory.createAnnotation();
        analyzer._valueColumns = new InputColumn[] { col1, col2, col3 };
        analyzer._conditions = new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_NULL,
                CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL, CompletenessAnalyzer.Condition.NOT_NULL };

        analyzer.init();

        analyzer.run(new MockInputRow(1001).put(col1, null).put(col2, null).put(col3, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "").put(col3, ""), 1);

        assertEquals(2, analyzer.getResult().getRowCount());
        assertEquals(0, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "not blank").put(col3, ""), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "not blank").put(col2, "not blank").put(col3, "not blank"), 1);

        assertEquals(4, analyzer.getResult().getRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, null).put(col2, "not blank").put(col3, ""), 1);

        assertEquals(5, analyzer.getResult().getRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(3, analyzer.getResult().getInvalidRowCount());
    }
}
