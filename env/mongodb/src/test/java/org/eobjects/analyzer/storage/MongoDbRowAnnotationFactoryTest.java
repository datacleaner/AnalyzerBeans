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
package org.eobjects.analyzer.storage;

import java.util.Random;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoDbRowAnnotationFactoryTest extends TestCase {

	private DB db = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			db = new Mongo().getDB("analyzerbeans");
			assertTrue(db.isAuthenticated());
		} catch (Throwable e) {
			e.printStackTrace();
			db = null;
		}
	}

	public void testInsertAndUpdateRow() throws Exception {
		if (db == null) {
			System.out.println("Skipping MongoDb test because it seems that a local server is not running!");
			return;
		}

		DBCollection col = db.getCollection(getName());
		if (col.count() > 0l) {
			col.drop();
			col = db.getCollection(getName());
		}

		MongoDbRowAnnotationFactory annotationFactory = new MongoDbRowAnnotationFactory(col);

		RowAnnotation ann1 = annotationFactory.createAnnotation();
		RowAnnotation ann2 = annotationFactory.createAnnotation();

		MockInputColumn<String> column = new MockInputColumn<String>("Greeting", String.class);

		annotationFactory.annotate(new MockInputRow(13371337).put(column, "hello"), 2, ann1);
		annotationFactory.annotate(new MockInputRow(13371337).put(column, "hello"), 2, ann2);
		annotationFactory.annotate(new MockInputRow(13371338).put(column, "hi"), 3, ann1);

		InputRow[] rows1 = annotationFactory.getRows(ann1);
		InputRow[] rows2 = annotationFactory.getRows(ann2);

		assertEquals(2, rows1.length);
		assertEquals(5, ann1.getRowCount());
		assertEquals("hello", rows1[0].getValue(column));
		assertEquals("hi", rows1[1].getValue(column));

		assertEquals(1, rows2.length);
		assertEquals(2, ann2.getRowCount());
		assertEquals("hello", rows2[0].getValue(column));

		annotationFactory.reset(ann1);

		rows1 = annotationFactory.getRows(ann1);
		assertEquals(0, rows1.length);
		assertEquals(0, ann1.getRowCount());

		rows1 = annotationFactory.getRows(ann1);
		assertEquals(0, rows1.length);
		assertEquals(0, ann1.getRowCount());

		annotationFactory.annotate(new MockInputRow(13371339).put(column, "morning"), 1, ann1);

		rows1 = annotationFactory.getRows(ann1);

		assertEquals(1, rows1.length);
		assertEquals(1, ann1.getRowCount());
		assertEquals("morning", rows1[0].getValue(column));

		annotationFactory.reset(ann1);
		annotationFactory.reset(ann2);
		Random r = new Random(); 
		for (int i = 0; i < 1000; i++) {
			int rand = r.nextInt(2);
			if (rand == 0) {
				annotationFactory.annotate(new MockInputRow().put(column, "mrrrh1"), 1, ann1);
			} else {
				annotationFactory.annotate(new MockInputRow().put(column, "mrrrh2"), 1, ann2);
			}
		}

		rows1 = annotationFactory.getRows(ann1);
		rows2 = annotationFactory.getRows(ann2);

		assertTrue(rows1.length > 0);
		for (InputRow inputRow : rows1) {
			assertEquals("mrrrh1", inputRow.getValue(column));
		}

		assertTrue(rows2.length > 0);
		for (InputRow inputRow : rows2) {
			assertEquals("mrrrh2", inputRow.getValue(column));
		}

		assertEquals(1000, rows1.length + rows2.length);
		assertEquals(1000, ann1.getRowCount() + ann2.getRowCount());

		col.drop();
	}
}
