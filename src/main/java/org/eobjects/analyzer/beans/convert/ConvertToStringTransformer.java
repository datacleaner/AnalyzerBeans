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
package org.eobjects.analyzer.beans.convert;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Attempts to convert anything to a String value.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to string")
@Description("Converts anything to a string (or null).")
public class ConvertToStringTransformer implements Transformer<String> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Configured(required = false)
	String nullReplacement;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(input.getName() + " (as string)");
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
				stringValue = sb.toString();
			} else {
				stringValue = value.toString();
			}
		}
		return stringValue;
	}
}
