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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.junit.Test;

public class CreateJsonTransformerTest {

	@Test
	public void testCreateSimpleJsonOfDataTypes() throws Exception {
		InputColumn<Object> col = new MockInputColumn<Object>("obj",
				Object.class);
		CreateJsonTransformer jsonTransformer = new CreateJsonTransformer(col);

		assertEquals("OutputColumns[obj (as JSON)]", jsonTransformer
				.getOutputColumns().toString());

		assertEquals("123",
				jsonTransformer.transform(new MockInputRow().put(col, 123))[0]);
		assertEquals(
				"123.0",
				jsonTransformer.transform(new MockInputRow().put(col, 123.0))[0]);

		assertEquals(
				"\"str\"",
				jsonTransformer.transform(new MockInputRow().put(col, "str"))[0]);

		assertEquals("true",
				jsonTransformer.transform(new MockInputRow().put(col, true))[0]);

		assertEquals("null",
				jsonTransformer.transform(new MockInputRow().put(col, null))[0]);
	}

	@Test
	public void testSimpleMapToJson() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", "shekhar");
		map.put("country", "India");

		InputColumn<Map<?, ?>> col = new MockInputColumn<Map<?, ?>>("map",
				MockInputColumn.MAP_CLASS);
		CreateJsonTransformer jsonTransformer = new CreateJsonTransformer(col);
		assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());

		String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(
				col, map));
		assertEquals(1, jsonDocs.length);
		assertEquals("{\"name\":\"shekhar\",\"country\":\"India\"}",
				jsonDocs[0]);
	}

	@Test
	public void testSimpleList() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("hello");
		list.add("world");

		InputColumn<List<?>> col = new MockInputColumn<List<?>>("list",
				MockInputColumn.LIST_CLASS);

		CreateJsonTransformer jsonTransformer = new CreateJsonTransformer(col);
		assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());

		String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(
				col, list));
		assertEquals(1, jsonDocs.length);
		assertEquals("[\"hello\",\"world\"]", jsonDocs[0]);
	}

	@Test
	public void testComplexCollectionColumnsToJson() throws Exception {
		Map<String, String> namesMap = new HashMap<String, String>();
		namesMap.put("GivenName", "Ankit");
		namesMap.put("FamilyName", "Kumar");
		List<Map<String, String>> addresses = new ArrayList<Map<String, String>>();
		Map<String, String> addressMap1 = new HashMap<String, String>();
		addressMap1.put("Street", "Utrechtseweg");
		addressMap1.put("HouseNumber", "310");
		addressMap1.put("City", "Arnhem");
		addressMap1.put("Postcode", "6812AR");
		addressMap1.put("Country", "Netherlands");
		Map<String, String> addressMap2 = new HashMap<String, String>();
		addressMap2.put("Street", "Silversteyn");
		addressMap2.put("HouseNumber", "893");
		addressMap2.put("City", "Arnhem");
		addressMap2.put("Postcode", "6812AB");
		addressMap2.put("Country", "Netherlands");
		addresses.add(addressMap1);
		addresses.add(addressMap2);

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", namesMap);
		map.put("addresses", addresses);
		map.put("country", "India");

		InputColumn<Map<?, ?>> col = new MockInputColumn<Map<?, ?>>("name",
				MockInputColumn.MAP_CLASS);
		CreateJsonTransformer jsonTransformer = new CreateJsonTransformer(col);
		assertEquals(1, jsonTransformer.getOutputColumns().getColumnCount());

		String[] jsonDocs = jsonTransformer.transform(new MockInputRow().put(
				col, map));
		assertEquals(1, jsonDocs.length);
		assertEquals(
				"{'name':{'GivenName':'Ankit','FamilyName':'Kumar'},"
						+ "'addresses':[{'Postcode':'6812AR','Street':'Utrechtseweg','HouseNumber':'310','Country':'Netherlands','City':'Arnhem'},"
						+ "{'Postcode':'6812AB','Street':'Silversteyn','HouseNumber':'893','Country':'Netherlands','City':'Arnhem'}],'country':'India'}",
				jsonDocs[0].replaceAll("\"", "'"));
	}

}
