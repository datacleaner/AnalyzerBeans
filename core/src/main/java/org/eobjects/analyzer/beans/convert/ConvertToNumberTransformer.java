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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.ConversionCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to convert anything to a Number (Double) value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to number")
@Description("Converts anything to a number (or null).")
@Categorized({ ConversionCategory.class })
public class ConvertToNumberTransformer implements Transformer<Number> {

	private static final Logger logger = LoggerFactory.getLogger(ConvertToNumberTransformer.class);

	@Inject
	@Configured
	InputColumn<?> input;

	@Inject
	@Configured
	char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

	@Inject
	@Configured
	char thousandSeparator = DecimalFormatSymbols.getInstance().getGroupingSeparator();

	@Inject
	@Configured
	char minusSign = DecimalFormatSymbols.getInstance().getMinusSign();

	@Inject
	@Configured(required = false)
	Number nullReplacement;

	// no-args constructor
	public ConvertToNumberTransformer() {
	}

	public ConvertToNumberTransformer(char decimalSeparator, char thousandSeparator, char minusSign) {
		this();
		this.decimalSeparator = decimalSeparator;
		this.thousandSeparator = thousandSeparator;
		this.minusSign = minusSign;
	}

	public DecimalFormat getDecimalFormat() {
		DecimalFormat format = new DecimalFormat();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(decimalSeparator);
		symbols.setGroupingSeparator(thousandSeparator);
		symbols.setMinusSign(minusSign);
		format.setDecimalFormatSymbols(symbols);
		return format;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(input.getName() + " (as number)");
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(input);
		Number n = transform(value);
		if (n == null) {
			n = nullReplacement;
		}
		return new Number[] { n };
	}

	protected Number transform(Object value) {
		return transformValue(value, getDecimalFormat());
	}

	public static Number transformValue(Object value) {
		// use java's normal decimal symbols
		DecimalFormat format = new DecimalFormat();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		symbols.setMinusSign('-');
		format.setDecimalFormatSymbols(symbols);
		return transformValue(value, format);
	}
	
	public static Number transformValue(Object value, DecimalFormat decimalFormat) {
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

				try {
					if (stringValue.indexOf('%') != -1) {
						n = Percentage.parsePercentage(stringValue);
					} else {
						n = decimalFormat.parse(stringValue);
					}
				} catch (Exception e) {
					logger.info("Error occured parsing string as number: {}", stringValue);
				}
			}
		}
		return n;
	}

	public void setInput(InputColumn<String> input) {
		this.input = input;
	}
}
