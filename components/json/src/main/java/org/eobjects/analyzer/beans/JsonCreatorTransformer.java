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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;


@TransformerBean("Create JSON document")
@Description("Joins several columns into a single JSON document.")
public class JsonCreatorTransformer implements Transformer<String> {

	@Configured
	InputColumn<?>[] columns;

	private final ObjectMapper mapper = new ObjectMapper();

	public JsonCreatorTransformer() {
	}

	public JsonCreatorTransformer(InputColumn<?>... columns) {
		this.columns = columns;
	}

	@Override
	public OutputColumns getOutputColumns() {
		StringBuilder sb = new StringBuilder("JSON document of ");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(columns[i].getName());
			if (i == 4) {
				sb.append("...");
				// only include a preview of columns in the default name
				break;
			}
		}
		return new OutputColumns(sb.toString());
	}

	@Override
	public String[] transform(InputRow row) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (InputColumn<?> inputColumn : columns) {
			Object value = row.getValue(inputColumn);
			String key = inputColumn.getName();
			map.put(key, value);
		}
		String json = null;
		try {
			json = mapper.writeValueAsString(map);
		} catch (JsonGenerationException e) {
			throw new IllegalStateException("Exception while generating Json.");
		} catch (JsonMappingException e) {
			throw new IllegalStateException("Exception while Json mapping.");
		} catch (IOException e) {
			throw new IllegalStateException("IOException while Json mapping.");
		}
		return new String[] { json };
	}

}
