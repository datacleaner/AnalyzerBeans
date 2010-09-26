package org.eobjects.analyzer.beans;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Attempts to convert anything to a String value.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to string")
public class ConvertToStringTransformer implements Transformer<String> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Configured(required = false)
	String nullReplacement;

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public String[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		String stringValue = transformValue(value);
		if (stringValue == null) {
			stringValue = nullReplacement;
		}
		return new String[] { stringValue };
	}

	public static String transformValue(Object value) {
		String stringValue = null;
		if (value != null) {
			if (value instanceof InputStream) {
				value = new InputStreamReader(new BufferedInputStream((InputStream) value));
			}
			if (value instanceof Reader) {
				StringBuilder sb = new StringBuilder();
				Reader reader = (Reader) value;
				BufferedReader br = new BufferedReader(reader);
				try {
					for (String line = br.readLine(); line != null; line = br.readLine()) {
						sb.append(line);
						sb.append('\n');
					}
					int lastIndexOf = sb.lastIndexOf("\n");
					if (lastIndexOf != -1) {
						sb.deleteCharAt(lastIndexOf);
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				} finally {
					try {
						br.close();
					} catch (Exception e) {
						// do nothing
					}
				}
				value = sb.toString();
			} else {
				stringValue = value.toString();
			}
		}
		return stringValue;
	}
}
