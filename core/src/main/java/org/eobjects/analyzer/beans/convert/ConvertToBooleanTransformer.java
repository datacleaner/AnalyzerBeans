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

/**
 * Attempts to convert anything to a Boolean value
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Convert to boolean")
@Description("Converts anything to a boolean (or null).")
@Categorized({ ConversionCategory.class })
public class ConvertToBooleanTransformer implements Transformer<Boolean> {

	public static final String[] DEFAULT_TRUE_TOKENS = new String[] { "true", "yes", "1", "x" };
	public static final String[] DEFAULT_FALSE_TOKENS = new String[] { "false", "no", "0", "-" };

	@Inject
	@Configured
	InputColumn<?> column;

	@Configured(required = false)
	Boolean nullReplacement;

	@Configured
	@Description("Text tokens that will translate to 'true'")
	String[] _trueTokens = DEFAULT_TRUE_TOKENS;

	@Configured
	@Description("Text tokens that will translate to 'false'")
	String[] _falseTokens = DEFAULT_FALSE_TOKENS;

	public ConvertToBooleanTransformer(InputColumn<?> column) {
		this();
		this.column = column;
	}

	public ConvertToBooleanTransformer() {
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(column.getName() + " (as boolean)");
	}

	@Override
	public Boolean[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(column);
		Boolean b = transformValue(value, _trueTokens, _falseTokens);
		if (b == null) {
			b = nullReplacement;
		}
		return new Boolean[] { b };
	}

	public static Boolean transformValue(final Object value) {
		return transformValue(value, DEFAULT_TRUE_TOKENS, DEFAULT_FALSE_TOKENS);
	}

	public static Boolean transformValue(final Object value, final String[] trueTokens, final String[] falseTokens) {
		Boolean b = null;
		if (value != null) {
			if (value instanceof String) {
				String stringValue = (String) value;
				stringValue = stringValue.trim();

				for (String token : trueTokens) {
					if (token.equalsIgnoreCase(stringValue)) {
						b = true;
						break;
					}
				}
				if (b == null) {
					for (String token : falseTokens) {
						if (token.equalsIgnoreCase(stringValue)) {
							b = false;
							break;
						}
					}
				}
			} else if (value instanceof Number) {
				Number numberValue = (Number) value;
				if (numberValue.intValue() == 1) {
					b = true;
				} else if (numberValue.intValue() == 0) {
					b = false;
				}
			} else if (value instanceof Boolean) {
				b = (Boolean) value;
			}
		}
		return b;
	}

}
