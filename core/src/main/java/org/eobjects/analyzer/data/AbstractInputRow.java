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
 * Abstract implementation of the InputRow.
 * 
 * @author Kasper Sørensen
 */
public abstract class AbstractInputRow implements InputRow {

	private static final long serialVersionUID = 1L;

	@Override
	public final <E> E getValue(InputColumn<E> column) {
		if (column == null) {
			return null;
		}
		if (column instanceof ExpressionBasedInputColumn) {
			ExpressionBasedInputColumn<E> ebic = (ExpressionBasedInputColumn<E>) column;
			E value = ebic.evaluate(this);
			return value;
		}
		return getValueInternal(column);
	}

	protected abstract <E> E getValueInternal(InputColumn<E> column);
}
