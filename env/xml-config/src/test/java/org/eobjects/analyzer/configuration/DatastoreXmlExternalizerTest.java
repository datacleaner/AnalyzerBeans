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
package org.eobjects.analyzer.configuration;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatastoreXmlExternalizerTest extends TestCase {

    public void testExternalizeCsvDatastore() throws Exception {
        CsvDatastore ds = new CsvDatastore("foo", "foo.txt");
        ds.setDescription("bar");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element elem = new DatastoreXmlExternalizer().externalize(ds, "baz.txt", doc);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        Source source = new DOMSource(elem);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(baos);

        transformer.transform(source, outputTarget);

        String str = new String(baos.toByteArray());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<csv-datastore description=\"bar\" name=\"foo\"><filename>baz.txt</filename>"
                + "<quote-char>\"</quote-char><separator-char>,</separator-char>"
                + "<escape-char>\\</escape-char><encoding>UTF-8</encoding>"
                + "<fail-on-inconsistencies>true</fail-on-inconsistencies>"
                + "<header-line-number>1</header-line-number></csv-datastore>", str);
    }
}
