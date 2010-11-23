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
package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.CharIterator;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("Whitespace trimmer")
@Description("Trims your String values either on left, right or both sides.")
public class WhitespaceTrimmerTransformer implements Transformer<String> {

	@Configured
	InputColumn<String> column;

	@Configured
	boolean trimLeft = true;

	@Configured
	boolean trimRight = true;

	@Configured
	boolean trimMultipleToSingleSpace = false;

	public WhitespaceTrimmerTransformer() {
	}

	public WhitespaceTrimmerTransformer(boolean trimLeft, boolean trimRight, boolean trimMultipleToSingleSpace) {
		this();
		this.trimLeft = trimLeft;
		this.trimRight = trimRight;
		this.trimMultipleToSingleSpace = trimMultipleToSingleSpace;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(column.getName() + " (trimmed)");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		value = transform(value);
		return new String[] { value };
	}

	public String transform(String value) {
		if (value == null) {
			return null;
		}
		if (trimLeft && trimRight) {
			value = value.trim();
		} else {
			if (trimLeft) {
				value = StringUtils.leftTrim(value);
			}
			if (trimRight) {
				value = StringUtils.rightTrim(value);
			}
		}
		if (trimMultipleToSingleSpace) {
			CharIterator ci = new CharIterator(value);

			boolean previousWhiteSpace = false;
			while (ci.hasNext()) {
				ci.next();
				boolean currentWhiteSpace = ci.isWhitespace();
				if (currentWhiteSpace) {
					ci.set(' ');
					if (previousWhiteSpace) {
						ci.remove();
					}
				}
				previousWhiteSpace = currentWhiteSpace;
			}

			value = ci.toString();
		}
		return value;
	}
}
