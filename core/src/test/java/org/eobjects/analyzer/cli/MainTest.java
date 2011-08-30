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
package org.eobjects.analyzer.cli;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.eobjects.analyzer.cli.Main;

public class MainTest extends TestCase {

	public void testUsage() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-usage".split(" "));

		String out1 = stringWriter.toString();

		String[] lines = out1.split("\n");

		assertEquals(8, lines.length);

		assertEquals(
				"-conf (-configuration, --configuration-file) FILE          : XML file describing the configuration of AnalyzerBeans",
				lines[0].trim());
		assertEquals(
				"-ds (-datastore, --datastore-name) VAL                     : Name of datastore when printing a list of schemas, tables",
				lines[1].trim());
		assertEquals("or columns", lines[2].trim());
		assertEquals("-job (--job-file) FILE                                     : An analysis job XML file to execute",
				lines[3].trim());
		assertEquals(
				"-list [ANALYZERS | TRANSFORMERS | FILTERS | DATASTORES | S : Used to print a list of various elements available in the",
				lines[4].trim());
		assertEquals("CHEMAS | TABLES | COLUMNS]                                 : configuration", lines[5].trim());
		assertEquals(
				"-s (-schema, --schema-name) VAL                            : Name of schema when printing a list of tables or columns",
				lines[6].trim());
		assertEquals(
				"-t (-table, --table-name) VAL                              : Name of table when printing a list of columns",
				lines[7].trim());

		// again without the -usage flag
		stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));
		Main.main(new String[0]);

		String out2 = stringWriter.toString();
		assertEquals(out1, out2);
	}

	public void testListDatastores() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -list DATASTORES".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		assertEquals("Datastores:\n-----------\nall_datastores\nemployees_csv\norderdb\n", out);
	}

	public void testListSchemas() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -ds orderdb -list SCHEMAS".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		assertEquals("Schemas:\n" + "--------\n" + "INFORMATION_SCHEMA\n" + "PUBLIC\n", out);
	}

	public void testListTables() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -ds orderdb -schema PUBLIC -list TABLES".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		assertEquals(
				"Tables:\n-------\nCUSTOMERS\nCUSTOMER_W_TER\nDEPARTMENT_MANAGERS\nDIM_TIME\nEMPLOYEES\nOFFICES\nORDERDETAILS\nORDERFACT\nORDERS\nPAYMENTS\nPRODUCTS\nQUADRANT_ACTUALS\nTRIAL_BALANCE\n",
				out);
	}

	public void testListColumns() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -ds orderdb -schema PUBLIC -table EMPLOYEES -list COLUMNS".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		assertEquals(
				"Columns:\n--------\nEMPLOYEENUMBER\nLASTNAME\nFIRSTNAME\nEXTENSION\nEMAIL\nOFFICECODE\nREPORTSTO\nJOBTITLE\n",
				out);
	}

	public void testListTransformers() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -list TRANSFORMERS".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		String[] lines = out.split("\n");

		assertEquals("Transformers:", lines[0]);

		assertTrue(out.indexOf("name: Email standardizer") != -1);
		assertTrue(out.indexOf("Output type is: STRING") != -1);
	}

	public void testListFilters() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -list FILTERS".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		String[] lines = out.split("\n");

		assertEquals("Filters:", lines[0]);

		assertTrue(out.indexOf("name: Not null") != -1);
		assertTrue(out.indexOf("- Outcome category: VALID") != -1);
	}

	public void testListAnalyzers() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));

		Main.main("-conf examples/conf.xml -list ANALYZERS".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		String[] lines = out.split("\n");

		assertEquals("Analyzers:", lines[0]);

		assertTrue(out.indexOf("name: Pattern finder") != -1);
		assertTrue(out.indexOf("name: String analyzer") != -1);
		assertTrue(out.indexOf("name: Compare schema structures") != -1);
	}

	public void testExampleEmployeesJob() throws Exception {
		StringWriter stringWriter = new StringWriter();
		Main.setOut(new PrintWriter(stringWriter));
		Main.main("-conf examples/conf.xml -job examples/employees_job.xml".split(" "));

		String out = stringWriter.toString().replaceAll("\r\n", "\n");
		String[] lines = out.split("\n");
		assertEquals("SUCCESS!", lines[0]);

		assertTrue(out.indexOf("Top values:\n" + " - company.com: 4\n" + " - eobjects.org: 2") != -1);

		assertTrue(lines.length > 80);
		assertTrue(lines.length < 90);
	}
}
