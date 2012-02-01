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
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String[] { json };
	}

}
