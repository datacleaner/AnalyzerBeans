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
package org.eobjects.analyzer.reference;

import java.util.List;

import org.eobjects.analyzer.beans.stringpattern.DefaultTokenizer;
import org.eobjects.analyzer.beans.stringpattern.Token;
import org.eobjects.analyzer.beans.stringpattern.TokenPattern;
import org.eobjects.analyzer.beans.stringpattern.TokenPatternImpl;
import org.eobjects.analyzer.beans.stringpattern.Tokenizer;
import org.eobjects.analyzer.beans.stringpattern.TokenizerConfiguration;

import dk.eobjects.metamodel.util.BaseObject;

/**
 * Represents a string pattern that is based on a sequence of token types. The
 * pattern format is similar to the one used by the Pattern finder analyzer,
 * which makes it ideal for reusing discovered patterns.
 * 
 * @see TokenPattern
 * 
 * @author Kasper Sørensen
 */
public class SimpleStringPattern extends BaseObject implements StringPattern {

	private static final long serialVersionUID = 1L;
	private final String _name;
	private final String _expression;
	private transient TokenPatternImpl _tokenPattern;
	private transient DefaultTokenizer _tokenizer;
	private transient TokenizerConfiguration _configuration;

	public SimpleStringPattern(String name, String expression) {
		_name = name;
		_expression = expression;
	}

	private Tokenizer getTokenizer() {
		if (_tokenizer == null) {
			_tokenizer = new DefaultTokenizer(getConfiguration());
		}
		return _tokenizer;
	}

	private TokenizerConfiguration getConfiguration() {
		if (_configuration == null) {
			// TODO: Ideally we should provide all the configuration options in
			// the constructor
			_configuration = new TokenizerConfiguration();
		}
		return _configuration;
	}

	private TokenPattern getTokenPattern() {
		if (_tokenPattern == null) {
			List<Token> tokens = getTokenizer().tokenize(_expression);
			_tokenPattern = new TokenPatternImpl(_expression, tokens, getConfiguration());
		}
		return _tokenPattern;
	}

	@Override
	public String getName() {
		return _name;
	}

	public String getExpression() {
		return _expression;
	}

	@Override
	public boolean matches(String string) {
		if (string == null) {
			return false;
		}
		List<Token> tokens = getTokenizer().tokenize(string);
		return getTokenPattern().match(tokens);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
		identifiers.add(_expression);
	}

	@Override
	public String toString() {
		return "SimpleStringPattern[name=" + _name + ",expression=" + _expression + "]";
	}
}
