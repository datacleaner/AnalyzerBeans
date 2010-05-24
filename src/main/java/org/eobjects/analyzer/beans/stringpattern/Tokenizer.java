package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;

public interface Tokenizer {

	public List<Token> tokenize(String s);
}
