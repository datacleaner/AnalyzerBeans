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

import java.io.Serializable;

import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.util.InputColumnComparator;

import org.eobjects.metamodel.schema.Column;

/**
 * Represents an InputColumn that is a result of a transformer.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public class TransformedInputColumn<E> implements MutableInputColumn<E>, Serializable, Comparable<InputColumn<E>> {

    private static final long serialVersionUID = 1L;

    private final String _id;
    private int sortNumber;
    private Class<?> _dataType;
    private String _name;
    private String _initialName;

    public TransformedInputColumn(String name, IdGenerator idGenerator) {
        _name = name;
        _initialName = name;
        _id = idGenerator.nextId();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getInitialName() {
        return _initialName;
    }

    public void setInitialName(String initialName) {
        _initialName = initialName;
    }

    @Override
    public void setName(String name) {
        _name = name;
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setDataType(Class<?> dataType) {
        _dataType = dataType;
    }

    @Override
    public boolean isPhysicalColumn() {
        return false;
    }

    @Override
    public boolean isVirtualColumn() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DataTypeFamily getDataTypeFamily() {
        return DataTypeFamily.valueOf(_dataType);
    }

    @Override
    public String toString() {
        return "TransformedInputColumn[id=" + _id + ",name=" + _name + "]";
    }

    @Override
    public Column getPhysicalColumn() throws IllegalStateException {
        return null;
    }
    
    public int getSortNumber() {
        return sortNumber;
    }
    
    public void setSortNumber(int sortNumber) {
        this.sortNumber = sortNumber;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends E> getDataType() {
        return (Class<? extends E>) _dataType;
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransformedInputColumn) {
            TransformedInputColumn<?> that = (TransformedInputColumn<?>) obj;
            return getId().equals(that.getId());
        }
        return false;
    }

    @Override
    public int compareTo(InputColumn<E> o) {
        return InputColumnComparator.compareInputColumns(this, o);
    }
}
