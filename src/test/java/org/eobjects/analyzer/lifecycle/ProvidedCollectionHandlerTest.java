package org.eobjects.analyzer.lifecycle;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ProvidedCollectionHandlerTest extends TestCase {

	ProvidedCollectionHandler handler = new ProvidedCollectionHandler();

	public void testCreateMap() throws Exception {
		Map<String, Long> map = handler.createMap(String.class, Long.class);
		assertNotNull(map);
	}

	public void testCreateList() throws Exception {
		List<String> list = handler.createList(String.class);
		assertNotNull(list);
	}
}
