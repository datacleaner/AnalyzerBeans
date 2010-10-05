package org.eobjects.analyzer.beans.standardize;

import java.util.StringTokenizer;

import org.eobjects.analyzer.beans.api.Configured;
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
public class TokenizerTransformer implements Transformer<String> {

	@Configured("Number of tokens")
	Integer numTokens;

	@Configured
	InputColumn<String> column;

	public TokenizerTransformer() {
	}

	public TokenizerTransformer(InputColumn<String> column, Integer numTokens) {
		this.column = column;
		this.numTokens = numTokens;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(numTokens);
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		String[] result = new String[numTokens];

		int i = 0;
		StringTokenizer st = new StringTokenizer(value);
		while (i < result.length && st.hasMoreTokens()) {
			result[i] = st.nextToken();
			i++;
		}

		return result;
	}

}
