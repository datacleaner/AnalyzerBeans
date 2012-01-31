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

import java.util.Map;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Transformer for extracting values from maps.
 * 
 * @author Kasper Sørensen
 * @author Shekhar Gulati
 * @author Saurabh Gupta
 */
@TransformerBean("Extract values from key/value map")
@Alias("Extract values from map")
@Categorized(DataStructuresCategory.class)
public class ExtractFromMapTransformer implements Transformer<Object> {

	@Inject
	@Configured
	InputColumn<Map<String, ?>> mapColumn;

	@Inject
	@Configured
	String[] keys;

	@Inject
	@Configured
	Class<?>[] types;

	@Inject
	@Configured
	boolean verifyTypes = false;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(keys, types);
	}

	@Override
	public Object[] transform(InputRow row) {
		final Map<String, ?> map = row.getValue(mapColumn);
		final Object[] result = new Object[keys.length];

		if (map == null) {
			return result;
		}

		for (int i = 0; i < keys.length; i++) {
			Object value = map.get(keys[i]);
			if (verifyTypes) {
				value = types[i].cast(value);
			}
			result[i] = value;
		}

		return result;
	}

}
