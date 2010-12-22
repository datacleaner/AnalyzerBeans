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
package org.eobjects.analyzer.job.runner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.reference.Dictionary;

/**
 * Class that manages the reference data that is being used within the execution
 * of a job. It will make sure that any @Initialize or @Close methods are called
 * before and after the job executes.
 * 
 * @author Kasper Sørensen
 */
public class ReferenceDataActivationManager {

	private final Map<String, Dictionary> _dictionaries = new HashMap<String, Dictionary>();

	public Collection<Dictionary> getDictionaries() {
		return _dictionaries.values();
	}

	public boolean accepts(Object obj) {
		return obj instanceof Dictionary;
	}

	public void register(Object value) {
		if (value instanceof Dictionary) {
			Dictionary dictionary = (Dictionary) value;
			_dictionaries.put(dictionary.getName(), dictionary);
		} else {
			// TODO: collect other types of reference data as well
		}
	}
}
