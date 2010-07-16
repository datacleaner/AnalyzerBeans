package org.eobjects.analyzer.beans;

import java.util.StringTokenizer;

import org.eobjects.analyzer.annotations.Configured;
import org.eobjects.analyzer.annotations.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.VirtualStringInputColumn;

@TransformerBean("Tokenizer")
public class TokenizerTransformer implements Transformer<String> {

	@Configured("Token names")
	String[] tokenNames;

	@Configured
	InputColumn<String> column;
	
	public TokenizerTransformer() {
	}
	
	public TokenizerTransformer(String[] tokenNames, InputColumn<String> column) {
		this.tokenNames = tokenNames;
		this.column = column;
	}

	@Override
	public InputColumn<String>[] getVirtualInputColumns() {
		@SuppressWarnings("unchecked")
		InputColumn<String>[] result = new InputColumn[tokenNames.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new VirtualStringInputColumn(tokenNames[i]);
		}
		return result;
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);

		int i = 0;
		String[] result = new String[tokenNames.length];
		StringTokenizer st = new StringTokenizer(value);
		while (i < result.length && st.hasMoreTokens()) {
			result[i] = st.nextToken();
			i++;
		}
		
		return result;
	}

}
