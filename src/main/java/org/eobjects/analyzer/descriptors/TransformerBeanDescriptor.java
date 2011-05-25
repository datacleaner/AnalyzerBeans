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
package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.data.DataTypeFamily;

/**
 * Descriptor interface for {@link Transformer}s.
 * 
 * @author Kasper Sørensen
 * 
 * @param <B>
 */
public interface TransformerBeanDescriptor<B extends Transformer<?>> extends BeanDescriptor<B> {

	/**
	 * Gets the output data's {@link DataTypeFamily}.
	 * 
	 * @return a {@link DataTypeFamily} value that represents the data type of
	 *         this transformer bean's output.
	 */
	public DataTypeFamily getOutputDataTypeFamily();
}
