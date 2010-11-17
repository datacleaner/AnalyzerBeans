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

import java.util.Date;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Attempts to convert anything to a Number (Double) value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to number")
@Description("Converts anything to a number (or null).")
public class ConvertToNumberTransformer implements Transformer<Number> {

	@Inject
	@Configured
	InputColumn<?> input;

	@Configured(required = false)
	Number nullReplacement;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(input.getName() + " (as number)");
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Number n = transformValue(value);
		if (n == null) {
			n = nullReplacement;
		}
		return new Number[] { n };
	}

	public static Number transformValue(Object value) {
		Number n = null;
		if (value != null) {
			if (value instanceof Number) {
				n = (Number) value;
			} else if (value instanceof Boolean) {
				if (Boolean.TRUE.equals(value)) {
					n = 1;
				} else {
					n = 0;
				}
			} else if (value instanceof Date) {
				Date d = (Date) value;
				n = d.getTime();
			} else if (value instanceof Character) {
				Character c = (Character) value;
				if (!Character.isDigit(c)) {
					// return the integer value of the character
					n = (int) c;
				}
			} else {
				String stringValue = value.toString();
				if (n == null) {
					try {
						n = Double.parseDouble(stringValue);
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
		}
		return n;
	}

	public void setInput(InputColumn<String> input) {
		this.input = input;
	}
}
