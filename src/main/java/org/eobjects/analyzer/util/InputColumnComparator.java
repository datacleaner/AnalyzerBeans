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
package org.eobjects.analyzer.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.TransformedInputColumn;

import org.eobjects.metamodel.schema.Column;

/**
 * Comparator for input columns. Physical columns will be ordered first, sorted
 * by their column numbers.
 * 
 * @author Kasper Sørensen
 */
public class InputColumnComparator implements Comparator<InputColumn<?>>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(InputColumn<?> o1, InputColumn<?> o2) {
		if (o1.isPhysicalColumn() && o2.isPhysicalColumn()) {
			Column physicalColumn1 = o1.getPhysicalColumn();
			Column physicalColumn2 = o2.getPhysicalColumn();
			int result = physicalColumn1.getColumnNumber() - physicalColumn2.getColumnNumber();
			if (result == 0) {
				result = physicalColumn1.compareTo(physicalColumn2);
			}
			return result;
		}

		if (o1.isVirtualColumn() && o2.isVirtualColumn()) {
			if (o1 instanceof TransformedInputColumn<?> && o2 instanceof TransformedInputColumn<?>) {
				String id1 = ((TransformedInputColumn<?>) o1).getId();
				String id2 = ((TransformedInputColumn<?>) o2).getId();
				return id1.compareTo(id2);
			}
			int result = o1.getName().compareTo(o2.getName());
			if (result == 0) {
				result = o1.hashCode() - o2.hashCode();
			}
			return result;
		}

		if (o1.isPhysicalColumn() && o2.isVirtualColumn()) {
			return -1;
		} else {
			return 1;
		}
	}

}
