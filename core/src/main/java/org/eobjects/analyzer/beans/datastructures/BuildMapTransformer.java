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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer for building maps based on values in a row.
 * 
 * @author Kasper Sørensen
 * @author Shekhar Gulati
 * @author Saurabh Gupta
 */
@TransformerBean("Build key/value map")
@Alias("Build map")
@Description("Transformer capable of building a map of keys and values")
@Categorized(DataStructuresCategory.class)
public class BuildMapTransformer implements Transformer<Map<String, ?>> {

	private static final Logger logger = LoggerFactory.getLogger(BuildMapTransformer.class);

	@Inject
	@Configured
	InputColumn<?>[] values;

	@Inject
	@Configured
	String[] keys;

	@Inject
	@Configured
	boolean retainKeyOrder = false;

	@Inject
	@Configured
	boolean includeNullValues = false;

	@Override
	public OutputColumns getOutputColumns() {
		StringBuilder sb = new StringBuilder("Map: ");
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			sb.append(key);
			if (sb.length() > 30) {
				sb.append("...");
				break;
			}

			if (i + 1 < keys.length) {
				sb.append(",");
			}
		}
		OutputColumns outputColumns = new OutputColumns(new String[] { sb.toString() }, new Class[] { Map.class });
		return outputColumns;
	}

	@Override
	public Map<String, ?>[] transform(InputRow row) {
		final Map<String, Object> map;
		if (retainKeyOrder) {
			map = new LinkedHashMap<String, Object>();
		} else {
			map = new HashMap<String, Object>();
		}
		for (int i = 0; i < keys.length; i++) {
			final String key = keys[i];
			final Object value = row.getValue(values[i]);
			if (value == null && !includeNullValues) {
				logger.debug("Ignoring null value for {} in row: {}", key, row);
			} else {
				map.put(key, value);
			}
		}

		@SuppressWarnings("unchecked")
		Map<String, ?>[] result = new Map[] { map };

		return result;
	}

}
