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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Parse JSON document")
@Description("Extract values from a JSON document")
public class ParseJsonTransformer implements
		Transformer<Map<String, ?>> {

	private final ObjectMapper mapper = new ObjectMapper();

	@Configured
	private InputColumn<String> json;

	public ParseJsonTransformer() {

	}

	public ParseJsonTransformer(InputColumn<String> json) {
		this.json = json;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(json.getName() + " (as Map)");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ?>[] transform(InputRow inputRow) {
		final String jsonString = inputRow.getValue(json);

		if (StringUtils.isBlank(jsonString)) {
			return new Map[] { Collections.emptyMap() };
		}

		Map<String, Object> jsonMap = Collections.emptyMap();
		try {
			jsonMap = mapper.readValue(jsonString, Map.class);
		} catch (JsonParseException e) {
			throw new IllegalStateException("Exception while parsing Json.");
		} catch (JsonMappingException e) {
			throw new IllegalStateException("Exception while Json mapping.");
		} catch (IOException e) {
			throw new IllegalStateException("IOException while parsing Json.");
		}
		final Map<String, ?>[] result = new Map[] { jsonMap };
		return result;
	}
}
