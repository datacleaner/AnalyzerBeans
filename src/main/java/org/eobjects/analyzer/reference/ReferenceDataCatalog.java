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

import java.io.Serializable;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;

/**
 * Represents a catalog of items that are considered as reference data that a
 * user can choose to utilize in various analyzers, transformers etc.
 * 
 * Reference data is typically reusable between jobs which is why it is
 * contained within the configuration of AnalyzerBeans. For example you could
 * have a dictionary of valid values for a particular entity type. This
 * dictionary is then resuable both as input to a Dictionary validation filter
 * and an analyzer that will match values against different dictionaries.
 * 
 * All reference data types (Dictionary, SynonymCatalog etc.) is injectable into
 * components using the @Configured annotation.
 * 
 * @see AnalyzerBeansConfiguration
 * @see Configured
 * 
 * @author Kasper Sørensen
 */
public interface ReferenceDataCatalog extends Serializable {

	public String[] getDictionaryNames();

	public Dictionary getDictionary(String name);

	public String[] getSynonymCatalogNames();

	public SynonymCatalog getSynonymCatalog(String name);

	public String[] getStringPatternNames();

	public StringPattern getStringPattern(String name);
}
