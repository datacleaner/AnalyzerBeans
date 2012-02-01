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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.junit.Test;

public class JsonCreatorTransformerTest {

    @Test
    public void testStringColumnsToJson() {
        InputColumn<String> col1 = new MockInputColumn<String>("name", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("country", String.class);
        JsonCreatorTransformer jsonTransformer = new JsonCreatorTransformer(col1, col2);
        assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());
        
        String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(col1, "shekhar").put(col2, "India"));
        assertEquals(1, jsonDocs.length);
        assertEquals("{\"name\":\"shekhar\",\"country\":\"India\"}", jsonDocs[0]);
    }
    
    @Test
	public void testStringAndMapColumnsToJson() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("name", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("country", String.class);
        Map<String, String> stringMap = new HashMap<String,String>();
        stringMap.put("GivenName", "Ankit");
        stringMap.put("FamilyName", "Kumar");
        InputColumn<Map> col3 = new MockInputColumn<Map>("NamesMap", Map.class);
        JsonCreatorTransformer jsonTransformer = new JsonCreatorTransformer(col1, col2, col3);
        assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());
        
        String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(col1, "shekhar").put(col2, "India").put(col3, stringMap));
        assertEquals(1, jsonDocs.length);
        assertEquals("{\"name\":\"shekhar\",\"country\":\"India\",\"NamesMap\":{\"GivenName\":\"Ankit\",\"FamilyName\":\"Kumar\"}}", jsonDocs[0]);
	}
    
    @Test
	public void testComplexCollectionColumnsToJson() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("name", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("country", String.class);
        Map<String, String> namesMap = new HashMap<String,String>();
        namesMap.put("GivenName", "Ankit");
        namesMap.put("FamilyName", "Kumar");
        List<Map<String,String>> addresses = new ArrayList<Map<String, String>>();
        InputColumn<List> col3 = new MockInputColumn<List>("address", List.class);
        Map<String, String> addressMap1 = new HashMap<String,String>();
        addressMap1.put("Street", "Utrechtseweg");
        addressMap1.put("HouseNumber", "310");
        addressMap1.put("City", "Arnhem");
        addressMap1.put("Postcode", "6812AR");
        addressMap1.put("Country", "Netherlands");
        Map<String, String> addressMap2 = new HashMap<String,String>();
        addressMap2.put("Street", "Silversteyn");
        addressMap2.put("HouseNumber", "893");
        addressMap2.put("City", "Arnhem");
        addressMap2.put("Postcode", "6812AB");
        addressMap2.put("Country", "Netherlands");
        addresses.add(addressMap1);
        addresses.add(addressMap2);
        JsonCreatorTransformer jsonTransformer = new JsonCreatorTransformer(col1, col2, col3);
        assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());
        
        String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(col1, "shekhar").put(col2, "India").put(col3, addresses));
        assertEquals(1, jsonDocs.length);
        assertEquals("{\"name\":\"shekhar\",\"country\":\"India\",\"address\":[{\"Postcode\":\"6812AR\",\"Street\":\"Utrechtseweg\",\"HouseNumber\":\"310\",\"Country\":\"Netherlands\",\"City\":\"Arnhem\"},{\"Postcode\":\"6812AB\",\"Street\":\"Silversteyn\",\"HouseNumber\":\"893\",\"Country\":\"Netherlands\",\"City\":\"Arnhem\"}]}", jsonDocs[0]);
	}

}
