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
package org.eobjects.analyzer.util;

import java.io.FileInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.ReferenceData;

public class ReadObjectBuilderTest extends TestCase {

	public void testDeserializeLegacyDatastores() throws Exception {
		ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(new FileInputStream(
				"src/test/resources/analyzerbeans-0.4-datastores.dat"));
		Object deserializedObject = objectInputStream.readObject();
		objectInputStream.close();
		assertTrue(deserializedObject instanceof List);

		@SuppressWarnings("unchecked")
		List<Datastore> list = (List<Datastore>) deserializedObject;
		assertEquals(8, list.size());

		assertEquals("JdbcDatastore[name=my_jdbc_connection,url=jdbc:hsqldb:res:metamodel]", list.get(0).toString());
		assertEquals("DbaseDatastore[name=my_dbase]", list.get(1).toString());
		assertEquals(
				"CsvDatastore[name=my_csv, filename=src/test/resources/employees.csv, quoteChar='\"', separatorChar=',', encoding=null]",
				list.get(2).toString());
		assertEquals("ExcelDatastore[name=my_xml]", list.get(3).toString());
		assertEquals("OdbDatastore[name=my_odb]", list.get(4).toString());
		assertEquals("ExcelDatastore[name=my_excel_2003]", list.get(5).toString());
		assertEquals("CompositeDatastore[name=my_composite]", list.get(6).toString());
		assertEquals("AccessDatastore[name=my_access]", list.get(7).toString());
	}

	public void testDeserializeLegacyReferenceData() throws Exception {
		ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(new FileInputStream(
				"src/test/resources/analyzerbeans-0.4-reference-data.dat"));
		Object deserializedObject = objectInputStream.readObject();
		objectInputStream.close();
		assertTrue(deserializedObject instanceof List);

		@SuppressWarnings("unchecked")
		List<ReferenceData> list = (List<ReferenceData>) deserializedObject;
		assertEquals(6, list.size());

		assertEquals("DatastoreDictionary[name=datastore_dict]", list.get(0).toString());
		assertEquals("TextFileDictionary[name=textfile_dict, filename=src/test/resources/lastnames.txt, encoding=UTF-8]",
				list.get(1).toString());
		assertEquals("SimpleDictionary[name=valuelist_dict]", list.get(2).toString());
		assertEquals(
				"TextFileSynonymCatalog[name=textfile_syn, filename=src/test/resources/synonym-countries.txt, caseSensitive=false, encoding=UTF-8]",
				list.get(3).toString());
		assertEquals("RegexStringPattern[name=regex danish email, expression=[a-z]+@[a-z]+\\.dk, matchEntireString=true]", list.get(4).toString());
		assertEquals("SimpleStringPattern[name=simple email, expression=aaaa@aaaaa.aa]", list.get(5).toString());
	}
}
