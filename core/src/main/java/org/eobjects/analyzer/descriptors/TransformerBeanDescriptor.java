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

/**
 * Descriptor interface for {@link Transformer}s.
 * 
 * 
 * 
 * @param <B>
 */
public interface TransformerBeanDescriptor<B extends Transformer<?>> extends BeanDescriptor<B> {

	/**
	 * Gets the output data's {@link org.eobjects.analyzer.data.DataTypeFamily}.
	 * 
	 * @return a {@link org.eobjects.analyzer.data.DataTypeFamily} value that
	 *         represents the data type of this transformer bean's output.
	 * 
	 * @deprecated use {@link #getOutputDataType()} instead.
	 */
	@Deprecated
	public org.eobjects.analyzer.data.DataTypeFamily getOutputDataTypeFamily();

	/**
	 * Gets the output data's default data type. Note that individual output
	 * columns of the transformer can override this data type.
	 * 
	 * @return the default data type of the transformed columns.
	 */
	public Class<?> getOutputDataType();

}
