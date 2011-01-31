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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.StringProperty;
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

	@StringProperty(multiline = true)
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
				char[] buffer = new char[1024];

				Reader reader = (Reader) value;

				StringBuilder sb = new StringBuilder();
				try {
					for (int read = reader.read(buffer); read != -1; read = reader.read(buffer)) {
						char[] charsToWrite = buffer;
						if (read != buffer.length) {
							charsToWrite = Arrays.copyOf(charsToWrite, read);
						}
						sb.append(charsToWrite);
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				} finally {
					try {
						reader.close();
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
