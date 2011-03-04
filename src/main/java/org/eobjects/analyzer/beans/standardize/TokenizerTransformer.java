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
package org.eobjects.analyzer.beans.standardize;

import java.util.StringTokenizer;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Tokenizes values into a configurable amount of tokens.
 * 
 * @author Kasper Sørensen
 */
@TransformerBean("Tokenizer")
@Description("Tokenizes a String value.")
public class TokenizerTransformer implements Transformer<String> {

	@Configured("Number of tokens")
	@Description("Defines the max amount of tokens to expect")
	Integer numTokens;

	@Configured
	InputColumn<String> column;

	@Configured
	@Description("Characters to tokenize by")
	char[] delimiters = new char[] { ' ', '\t', '\n', '\r', '\f' };

	public TokenizerTransformer() {
	}

	public TokenizerTransformer(InputColumn<String> column, Integer numTokens) {
		this.column = column;
		this.numTokens = numTokens;
	}

	@Override
	public OutputColumns getOutputColumns() {
		String[] names = new String[numTokens];
		for (int i = 0; i < names.length; i++) {
			names[i] = column.getName() + " (token " + (i + 1) + ")";
		}
		return new OutputColumns(names);
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		String[] result = new String[numTokens];

		if (value != null) {
			int i = 0;
			StringTokenizer st = new StringTokenizer(value, new String(delimiters));
			while (i < result.length && st.hasMoreTokens()) {
				result[i] = st.nextToken();
				i++;
			}
		}

		return result;
	}

}
