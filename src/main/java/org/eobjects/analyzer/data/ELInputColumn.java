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

import java.util.List;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.eobjects.analyzer.util.StringUtils;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * InputColumn that evaluates an EL expression in order to return a computed
 * value. This can be used as a lightweight alternative to eg. JavaScript
 * transformation.
 * 
 * @author Kasper Sørensen
 */
public class ELInputColumn extends AbstractExpressionBasedInputColumn<String> {

	private final ExpressionFactory _factory;
	private final String _expression;

	public ELInputColumn(String expression) {
		_expression = expression;
		_factory = new ExpressionFactoryImpl();
	}

	@Override
	public String evaluate(InputRow row) {
		SimpleContext context = new SimpleContext();
		List<InputColumn<?>> inputColumns = row.getInputColumns();
		for (InputColumn<?> inputColumn : inputColumns) {
			if (!(inputColumn instanceof ExpressionBasedInputColumn)) {
				Object value = row.getValue(inputColumn);
				Class<?> javaType = inputColumn.getDataTypeFamily().getJavaType();
				ValueExpression valueExpression = _factory.createValueExpression(value, javaType);
				String variableName = inputColumn.getName();
				variableName = StringUtils.replaceWhitespaces(variableName, "_");
				context.setVariable(variableName, valueExpression);
			}
		}

		ValueExpression valueExpression = _factory.createValueExpression(context, _expression, String.class);
		return (String) valueExpression.getValue(context);
	}

	@Override
	public String getExpression() {
		return _expression;
	}

	@Override
	public DataTypeFamily getDataTypeFamily() {
		return DataTypeFamily.STRING;
	}

}
