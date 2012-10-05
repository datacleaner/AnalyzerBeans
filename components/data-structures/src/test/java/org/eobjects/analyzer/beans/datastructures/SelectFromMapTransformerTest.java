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
package org.eobjects.analyzer.beans.datastructures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class SelectFromMapTransformerTest extends TestCase {

    public void testTransform() throws Exception {
        final SelectFromMapTransformer trans = new SelectFromMapTransformer();
        final InputColumn<Map<String, ?>> col = new MockInputColumn<Map<String, ?>>("foo");
        trans.mapColumn = col;
        trans.keys = new String[] { "id", "Name.GivenName", "email.address", "Name.FamilyName",
                "Name.Something.That.Does.Not.Exist" };
        trans.types = new Class[] { Integer.class, String.class, String.class, String.class, String.class };
        trans.verifyTypes = true;

        assertEquals(
                "OutputColumns[id, Name.GivenName, email.address, Name.FamilyName, Name.Something.That.Does.Not.Exist]",
                trans.getOutputColumns().toString());

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 1001);
        map.put("email.address", "foo@bar.com");

        final Map<String, Object> nestedMap = new HashMap<String, Object>();
        nestedMap.put("GivenName", "John");
        nestedMap.put("FamilyName", "Doe");
        nestedMap.put("Titulation", "Mr");

        map.put("Name", nestedMap);

        Object[] result = trans.transform(new MockInputRow().put(col, map));

        assertEquals(5, result.length);
        assertEquals("[1001, John, foo@bar.com, Doe, null]", Arrays.toString(result));
    }
}
