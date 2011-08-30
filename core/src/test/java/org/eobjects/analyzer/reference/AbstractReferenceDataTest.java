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
package org.eobjects.analyzer.reference;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;

public class AbstractReferenceDataTest extends TestCase {

	public void testSerializationAndDeserializationOfDictionaries() throws Exception {
		JaxbConfigurationReader reader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-reference-data-types.xml"));
		ReferenceDataCatalog referenceDataCatalog = configuration.getReferenceDataCatalog();

		String[] dictionaryNames = referenceDataCatalog.getDictionaryNames();

		for (String name : dictionaryNames) {
			Dictionary dict = referenceDataCatalog.getDictionary(name);

			if (dict instanceof AbstractReferenceData) {
				System.out.println("Cloning dictionary: " + dict);
				Object clone = SerializationUtils.clone(dict);
				if (!dict.equals(clone)) {
					dict.equals(clone);
				}
				assertEquals(dict, clone);
			}
		}
	}

	public void testSerializationAndDeserializationOfSynonymCatalogs() throws Exception {
		JaxbConfigurationReader reader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-reference-data-types.xml"));
		ReferenceDataCatalog referenceDataCatalog = configuration.getReferenceDataCatalog();

		String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();

		for (String name : synonymCatalogNames) {
			SynonymCatalog sc = referenceDataCatalog.getSynonymCatalog(name);

			if (sc instanceof AbstractReferenceData) {
				System.out.println("Cloning synonym catalog: " + sc);
				Object clone = SerializationUtils.clone(sc);
				assertEquals(sc, clone);
			}
		}
	}

	public void testSerializationAndDeserializationOfStringPatterns() throws Exception {
		JaxbConfigurationReader reader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration configuration = reader.create(new File(
				"src/test/resources/example-configuration-all-reference-data-types.xml"));
		ReferenceDataCatalog referenceDataCatalog = configuration.getReferenceDataCatalog();

		String[] patternNames = referenceDataCatalog.getStringPatternNames();

		for (String name : patternNames) {
			StringPattern pattern = referenceDataCatalog.getStringPattern(name);

			if (pattern instanceof AbstractReferenceData) {
				System.out.println("Cloning string pattern: " + pattern);
				Object clone = SerializationUtils.clone(pattern);
				if (!pattern.equals(clone)) {
					System.out.println();
				}
				assertEquals(pattern, clone);
			}
		}
	}
}
