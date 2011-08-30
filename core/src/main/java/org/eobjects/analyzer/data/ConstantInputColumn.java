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
package org.eobjects.analyzer.data;

/**
 * Defines an InputColumn which has a fixed/constant value, regardless of the
 * row.
 * 
 * These columns can be used for various purposes, eg. to mark a filter outcome
 * in the data, to open jobs as templates, even though the new datastore is
 * missing some columns etc.
 * 
 * @author Kasper Sørensen
 */
public final class ConstantInputColumn extends AbstractExpressionBasedInputColumn<String> {

	private final String _value;

	public ConstantInputColumn(String value) {
		super();
		if (value == null) {
			throw new IllegalArgumentException("value cannot be null");
		}
		_value = value;
	}

	@Override
	public String getExpression() {
		return _value;
	}

	@Override
	public DataTypeFamily getDataTypeFamily() {
		return DataTypeFamily.STRING;
	}

	@Override
	public String evaluate(InputRow row) {
		return _value;
	}
}
