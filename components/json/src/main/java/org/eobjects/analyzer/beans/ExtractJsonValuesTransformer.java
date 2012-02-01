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

@TransformerBean("Extract values from JSON document")
@Description("Extract values from a JSON document")
public class ExtractJsonValuesTransformer implements
		Transformer<Map<String, ?>> {

	private final ObjectMapper mapper = new ObjectMapper();

	@Configured
	private InputColumn<String> json;

	public ExtractJsonValuesTransformer() {

	}

	public ExtractJsonValuesTransformer(InputColumn<String> json) {
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
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		final Map<String, ?>[] result = new Map[] { jsonMap };
		return result;
	}

}
