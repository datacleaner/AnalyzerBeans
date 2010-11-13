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

@TransformerBean("String length")
@Description("Counts the length of Strings and create a separate column with this metric.")
public class StringLengthTransformer implements Transformer<Number> {

	@Configured
	InputColumn<String> column;

	public StringLengthTransformer() {
	}

	public StringLengthTransformer(InputColumn<String> column) {
		this.column = column;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(column.getName() + " length");
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		return transform(value);
	}

	protected Number[] transform(String value) {
		Integer length = null;
		if (value != null) {
			length = value.length();
		}
		return new Number[] { length };
	}

}
